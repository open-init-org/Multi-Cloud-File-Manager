### 3.0.9
* **UI:** Moved the three-dot options menu on Gallery View thumbnails from the top-right corner to the bottom info bar, alongside the filename and file details — improving visibility and usability.
* **Feature:** Added "Folder Size" section to the folder properties dialog — tap Calculate to recursively count files and compute total occupied space via `rclone size`.
* **UI:** Moved "About" from General Settings to the Navigation Drawer (hamburger menu) for faster access.
* **UI:** Removed Import/Export from the Navigation Drawer — these actions remain accessible inside Settings.
* **UI:** Added clear logs button to Log screen toolbar with confirmation dialog.
* **UI:** Added filter by type (All / Errors only / Info only) via toolbar PopupMenu in Log screen.
* **UI:** Updated "About" icon in Navigation Drawer to match TwoTone style of other menu items (`ic_twotone_info_24`).
* **Performance:** Added `--fast-list` flag to `rclone lsjson` directory listing queries for faster navigation in supported cloud providers (Google Drive, S3, B2, Swift).

***

### 3.0.8
* **Fix:** Fixed broken image thumbnails in Gallery View (Grid mode) by removing layout color tinting and dynamically handling vector vs bitmap icons.
* **Fix:** Fixed Photo Viewer (`PhotoViewerActivity`) side navigation image loading by fixing double-slash URL paths, resetting `PhotoView` matrix transformation on view recycling, and using persistent Glide cache keys.
* **UI:** Added centered indeterminate loading progress indicators (`ProgressBar`) for grid thumbnails and full-screen photo views.
* **Performance:** Added direct filesystem loading for local storage items (`RemoteItem.LOCAL`) without HTTP server overhead.

***

### 3.0.7
* **UI:** Streamlined About screen layout into a consolidated Maintainer card with an "Author & Previous Maintainers" item pointing to `contributors.md`.
* **Build:** Upgraded project toolchain to Java 21 LTS (`sourceCompatibility`, `targetCompatibility`, `jvmTarget` and CI workflows).
* **Build:** Updated Kotlin toolchain to version `2.0.21` and `kotlinx-serialization-json` to `1.7.3`.
* **UI:** Created dedicated Security settings category (`Settings -> Security`) and moved biometric lock preferences into `SecurityPreferencesFragment`.
* **Fix:** Prevented biometric lock bypass by immediately terminating application activity when authentication is canceled or fails (`BiometricLockManager.checkAndPromptLock`).
* **Build:** Automated `versionName` retrieval dynamically from Git/GitHub tags (`git describe --tags`) in `build.gradle`.

***

### 3.0.6
* **UI:** Modernized overall application theme with a Cyber Cyan & Electric Blue Material Design 3 palette, refined dark surfaces, and custom navigation gradients.
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
