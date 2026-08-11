### 3.0.6
* **UI:** Streamlined About screen layout into a consolidated Maintainer card with an "Author & Previous Maintainers" item pointing to `contributors.md`.
* **Build:** Upgraded project toolchain to Java 21 LTS (`sourceCompatibility`, `targetCompatibility`, `jvmTarget` and CI workflows).
* **Build:** Updated Kotlin toolchain to version `2.0.21` and `kotlinx-serialization-json` to `1.7.3`.
* **Fix:** Fixed application crash when toggling or prompting biometric authentication in SettingsActivity.
* **Fix:** Fixed ClassNotFoundException when inflating custom preference components in Settings XML layouts.
* **Refactor:** Full package rename from `ca.pkay.rcloneexplorer` to `org.openinit.multicloudfilemanager`.
* **Refactor:** Unified legacy `rcx` folder structure into `main` package and updated `CrashLogger` for FOSS compliance.
* **Docs:** Added `NOTICE` and `ATTRIBUTION.md` legal attribution files.
* **Docs:** Added technical architecture documentation for the Storage Access Framework WebDAV adapter (`safdav`).
* **Docs:** Added technical documentation for `librclone.so` and native Rclone core compilation.

***

### 3.0.5
* **Fix:** Stability and crash fixes for settings and layout inflations.

***

### 3.0.4
* **New:** Android 14 compatibility and UI enhancements.
* **Fix:** Performance and stability improvements.

***

### 3.0.3
* **Fix:** Dependency verification and CI build workflow updates.

***

### 3.0.2
* **New:** GitHub Actions release workflow automation.

***

### 3.0.1
* **Fix:** Storage Access Framework (SAF) & WebDAV/HTTP integration fixes.

***

### 3.0.0
* **New:** Initial release under Multi Cloud File Manager (renamed from Round Sync / RCX).
