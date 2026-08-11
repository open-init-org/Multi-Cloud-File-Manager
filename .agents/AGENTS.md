# Multi Cloud File Manager - Developer & Agent Guide

This repository contains **Multi Cloud File Manager** (formerly known as *Round Sync*, *RCX*, and *rcloneExplorer*), an open-source cloud file manager for Android powered by [rclone](https://rclone.org/).

---

## 1. Project Overview & Identifiers

- **Application ID / Package Name:** `org.openinit.multicloudfilemanager`
- **Debug Application ID:** `org.openinit.multicloudfilemanager.debug`
- **Historical References:** Historical branding (*Round Sync*) and maintainer information are preserved in [`ROUND_SYNC.md`](../ROUND_SYNC.md).
- **License:** GPLv3 ([`LICENSE`](../LICENSE)).

---

## 2. Directory & Module Structure

- **`app/`**: Main Android application module (Kotlin/Java, ViewBinding, Material 3, Datastore, WorkManager).
- **`rclone/`**: Go code and bindings for rclone core integration compiled via C-shared library.
- **`safdav/`**: Storage Access Framework (SAF) & WebDAV/HTTP server integration module.
- **`scripts/`**: Helper scripts for build, profanity/translation checks, and CI support.
- **`.github/workflows/`**: GitHub Actions CI/CD workflows:
  - `android.yml`: Builds Debug APKs and creates GitHub Releases on tag push.
  - `dependencies.yml`: Verifies dependency changes and checks for non-FOSS libraries.
  - `lint.yml`: Runs Android lint checks.
  - `translations.yml`: Validates strings and translation updates.

---

## 3. Build Requirements & Commands

### Prerequisites
- **JDK:** Java 21
- **Go:** Read dynamically from `gradle.properties` (`org.openinit.multicloudfilemanager.goVersion`)
- **Android NDK:** Read dynamically from `gradle.properties` (`org.openinit.multicloudfilemanager.ndkVersion`)

### Key Gradle Commands
```bash
# Build OSS Debug APKs (arm, arm64, x86, x64, universal)
./gradlew assembleOssDebug

# Build OSS Release APKs
./gradlew assembleOssRelease

# Run Android Lint (bypassing full rclone rebuild if unchanged)
./gradlew lint -x :rclone:buildAll
```

---

## 4. Intent Integration Interface

External applications or task managers (e.g., Tasker, Automate) can trigger sync tasks using Android Intents:

| Component | Target Value |
|---|---|
| **Package Name** | `org.openinit.multicloudfilemanager` |
| **Class Name** | `org.openinit.multicloudfilemanager.Services.SyncService` |
| **Action** | `START_TASK` |
| **Extra (Int)** | `task` (ID of the saved task) |
| **Extra (Boolean)** | `notification` (`true` or `false`) |

---

## 5. CI/CD & Release Workflow

- Releases are triggered on pushing tags (e.g., `git push origin v3.0.2`).
- The `android.yml` workflow reads `goVersion` and `ndkVersion` from `gradle.properties`, exports `GO_VERSION` to `$GITHUB_ENV`, builds the application, and publishes release assets using `softprops/action-gh-release@v2`.

---

## 6. Maintenance & Guidelines

- **Relative Paths Policy**: MUST ALWAYS use relative paths (e.g., `docs/safdav-architecture.md`, `app/src/...`) in documentation, responses, and code comments. NEVER hardcode machine-specific absolute file system paths (such as `/home/username/...` or `file:///home/...`).
- **Changelog Updates**: The in-app changelog asset [`app/src/main/assets/changelog.md`](../app/src/main/assets/changelog.md) MUST ALWAYS be updated whenever a new feature, bug fix, refactoring, or version release is made. Keep entries clean starting from version `3.0.0` onwards.
- **Architecture Documentation**:
  - `safdav` (Storage Access Framework WebDAV adapter): [`docs/safdav-architecture.md`](../docs/safdav-architecture.md)
  - `librclone.so` (Native Go core compilation & Android W^X execution mechanics): [`docs/rclone-core-integration.md`](../docs/rclone-core-integration.md)


## 7. Memory & Context for AI Agents

See [`MEMORY.md`](../.agents/MEMORY.md) for persistent memory, architectural context, conventions, and notes for AI coding assistants operating on this codebase. Always update this file with new context, conventions, or architectural notes to ensure AI agents have the latest information.