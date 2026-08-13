# Sync Progress And Logs Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make long-running syncs visibly advance from startup to progress and always leave a terminal success, failure, or cancellation entry in the notification and Logs screen.

**Architecture:** Keep rclone process ownership in `SyncWorker`, but separate JSON-line classification from process lifecycle handling. `handleSync()` will consume rclone's `NOTICE` stats events as well as errors, update the foreground notification from `StatusObject`, wait for the process, and convert its exit status into the existing `FAILURE_REASON` flow. Lifecycle entries will be written through `SyncLog` at the points where the worker actually starts, finishes, fails, or is cancelled.

**Tech Stack:** Kotlin, Android WorkManager, rclone JSON logs, JUnit 4 local tests, Gradle.

---

### Task 1: Add a testable rclone output classifier

**Files:**
- Create: `app/src/main/java/org/openinit/multicloudfilemanager/workmanager/SyncOutputParser.kt`
- Test: `app/src/test/java/org/openinit/multicloudfilemanager/workmanager/SyncOutputParserTest.kt`

- [ ] **Step 1: Write the failing tests**

Create a pure unit-testable parser contract. It must classify JSON lines by `level`, identify whether a line contains `stats`, and reject malformed or unrelated output without throwing:

```kotlin
class SyncOutputParserTest {
    @Test
    fun noticeStatsLineIsProgress() {
        val event = requireNotNull(SyncOutputParser.parse(
            "{\"level\":\"notice\",\"msg\":\"\",\"stats\":{\"bytes\":10}}"
        ))

        assertEquals(SyncOutputParser.Level.NOTICE, event.level)
        assertTrue(event.hasStats)
    }

    @Test
    fun errorLineIsError() {
        val event = requireNotNull(SyncOutputParser.parse(
            "{\"level\":\"error\",\"msg\":\"network failed\"}"
        ))

        assertEquals(SyncOutputParser.Level.ERROR, event.level)
        assertFalse(event.hasStats)
    }

    @Test
    fun malformedLineIsIgnored() {
        assertNull(SyncOutputParser.parse("not json"))
    }
}
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run: `./gradlew :app:testOssDebugUnitTest --tests org.openinit.multicloudfilemanager.workmanager.SyncOutputParserTest`

Expected: FAIL because `SyncOutputParser` does not exist yet.

- [ ] **Step 3: Implement the minimal parser**

Define an immutable event with `Level` values `NOTICE`, `WARNING`, `ERROR`, and `OTHER`, plus the original `JSONObject` and `hasStats`. `parse(line)` must construct `JSONObject(line)`, map the optional lowercase `level` with `valueOf(raw.uppercase())` guarded by fallback `OTHER`, set `hasStats` from `json.has("stats")`, and return `null` for `JSONException`.

- [ ] **Step 4: Run the focused test and verify it passes**

Run: `./gradlew :app:testOssDebugUnitTest --tests org.openinit.multicloudfilemanager.workmanager.SyncOutputParserTest`

Expected: PASS.

- [ ] **Step 5: Commit the parser and tests**

```bash
git add app/src/main/java/org/openinit/multicloudfilemanager/workmanager/SyncOutputParser.kt app/src/test/java/org/openinit/multicloudfilemanager/workmanager/SyncOutputParserTest.kt
git commit -m "test: classify rclone sync output"
```

### Task 2: Consume NOTICE stats and record process lifecycle

**Files:**
- Modify: `app/src/main/java/org/openinit/multicloudfilemanager/workmanager/SyncWorker.kt:58-222`
- Test: `app/src/test/java/org/openinit/multicloudfilemanager/workmanager/SyncWorkerTest.kt`

- [ ] **Step 1: Write the failing exit-status tests**

Add an `internal` pure helper in the worker's companion object and test its exact mapping. This keeps the process decision deterministic without starting WorkManager:

```kotlin
class SyncWorkerTest {
    @Test
    fun zeroExitCodeIsSuccess() {
        assertEquals(
            SyncWorker.FAILURE_REASON.NO_FAILURE,
            SyncWorker.failureReasonForExitCode(0)
        )
    }

    @Test
    fun nonZeroExitCodeIsRcloneError() {
        assertEquals(
            SyncWorker.FAILURE_REASON.RCLONE_ERROR,
            SyncWorker.failureReasonForExitCode(1)
        )
    }
}
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run: `./gradlew :app:testOssDebugUnitTest --tests org.openinit.multicloudfilemanager.workmanager.SyncWorkerTest`

Expected: FAIL because `failureReasonForExitCode` does not exist yet.

- [ ] **Step 3: Update `handleSync()` to process progress events**

Add this helper to the companion object and replace the current `level == "error"` / `level == "warning"` branches with the parser result:

```kotlin
internal fun failureReasonForExitCode(exitCode: Int): FAILURE_REASON =
    if (exitCode == 0) FAILURE_REASON.NO_FAILURE else FAILURE_REASON.RCLONE_ERROR

val event = SyncOutputParser.parse(line) ?: continue
when (event.level) {
    SyncOutputParser.Level.NOTICE,
    SyncOutputParser.Level.WARNING,
    SyncOutputParser.Level.ERROR -> {
        if (event.level == SyncOutputParser.Level.ERROR && sIsLoggingEnabled) {
            log2File?.log(line)
        }
        statusObject.parseLoglineToStatusObject(event.json)
        updateForegroundNotification(mNotificationManager.updateSyncNotification(
            title,
            statusObject.notificationContent.ifBlank {
                mContext.getString(R.string.sync_service_notification_running)
            },
            statusObject.notificationBigText,
            statusObject.notificationPercent,
            ongoingNotificationID
        ))
    }
    SyncOutputParser.Level.OTHER -> Unit
}
```

The event must expose the original `JSONObject` as `json`. This makes the existing `StatusObject` logic consume rclone's `NOTICE` stats without changing its formatting behavior. Add a running-state string resource if the first non-stats line arrives before the first stats event; do not leave the notification blank.

- [ ] **Step 4: Handle process startup and termination explicitly**

In `handleTask()`, if `Rclone.sync(...)` returns `null`, set `failureReason = FAILURE_REASON.RCLONE_ERROR`, write an error log containing the task title and startup failure, and skip `handleSync()`.

In `handleSync()`, write the existing start entry immediately before consuming output, wait for the process, and inspect `exitValue()`:

```kotlin
val exitCode = localProcessReference.waitFor()
if (exitCode == 0) {
    SyncLog.info(mContext, title, mContext.getString(R.string.operation_sync_completed))
} else {
    failureReason = FAILURE_REASON.RCLONE_ERROR
    SyncLog.error(
        mContext,
        title,
        mContext.getString(R.string.operation_sync_failed_exit, exitCode)
    )
}
```

Keep `cancelSyncNotification()` in a `finally` block so the foreground notification cannot remain active when stream parsing or waiting throws. Preserve the existing `onStopped()` cancellation entry and make sure it is not followed by a success entry.

- [ ] **Step 5: Add terminal strings**

Modify `app/src/main/res/values/strings.xml` with localized-default strings used by the worker:

```xml
<string name="sync_service_notification_running">Sync in progress...</string>
<string name="operation_sync_completed">Sync completed</string>
<string name="operation_sync_failed_exit">Sync failed (rclone exit code %d)</string>
```

Use the existing English fallback convention; do not change the already translated strings in this task unless the repository's translation workflow requires generated updates.

- [ ] **Step 6: Run focused tests and verify they pass**

Run: `./gradlew :app:testOssDebugUnitTest --tests org.openinit.multicloudfilemanager.workmanager.SyncOutputParserTest --tests org.openinit.multicloudfilemanager.workmanager.SyncWorkerTest`

Expected: PASS, with NOTICE stats updating the status path and non-zero exits selecting the failure path.

- [ ] **Step 7: Commit the worker changes**

```bash
git add app/src/main/java/org/openinit/multicloudfilemanager/workmanager/SyncWorker.kt app/src/main/res/values/strings.xml app/src/test/java/org/openinit/multicloudfilemanager/workmanager/SyncWorkerTest.kt
git commit -m "fix: expose sync progress and completion"
```

### Task 3: Verify the user-visible notification and Logs behavior

**Files:**
- No source changes expected: `LogFragment.onResume()` already reloads `SyncLog` from disk.

- [ ] **Step 1: Run the existing unit-test suite**

Run: `./gradlew :app:testOssDebugUnitTest`

Expected: PASS.

- [ ] **Step 2: Build the debug application**

Run: `./gradlew :app:assembleOssDebug`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Perform a manual sync verification**

With a small local test remote and logging enabled, start a task and verify all of the following:

1. The notification changes from `Starting Sync...` to transferred bytes/total bytes and a percentage after the first rclone stats line.
2. The notification eventually disappears and a success notification is shown for exit code `0`.
3. Logs contains `Started Sync`, `Sync completed`, and the existing success summary.
4. A deliberately invalid remote produces a failure notification, a non-zero-exit log entry, and no success entry.
5. Cancelling the WorkManager job produces only the cancellation terminal state and does not leave an ongoing notification.

- [ ] **Step 4: Check the final diff without touching unrelated work**

Run: `git diff --stat` and `git status --short`.

Confirm that pre-existing staged modifications, especially the unrelated UI and file-properties changes, remain untouched. Do not reset, checkout, or re-stage those files as part of this task.

## Self-Review

- The plan covers the reported notification symptom by consuming the configured `NOTICE` stats stream.
- The plan covers the reported Logs symptom by adding explicit process-start failure and terminal lifecycle entries.
- Non-zero rclone exit codes are no longer reported as successful WorkManager results.
- No cloud integration test or production credential is required; parser/lifecycle tests remain deterministic local tests and the real process path is validated manually.
