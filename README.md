# Multi Cloud File Manager - Rclone for Android
[![license: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

A cloud file manager for Android, powered by rclone.

## Screenshots
<table>
  <tr style="border:none">
    <td style="border:none">
      <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" width="360vh" />
    </td>
    <td style="border:none">
      <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.jpg" width="360vh" />
    </td>
    <td style="border:none">
      <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.jpg" width="360vh" />
    </td>
  </tr>
</table>

## Features
- **File Management**: List, view, download, upload, move, rename, delete files and folders.
- **Streaming**: Stream media files, serve files and directories over FTP, HTTP, WebDAV or DLNA.
- **Integration**: Access local storage devices and share files with the application to store them on a remote.
- **Cloud Storage**: Supports wide range of cloud storage providers via rclone.
- **Material 3 Design**: Modern UI with dark theme support.
- **Architecture**: Supports ARM, ARM64, x86 and x64 devices (Android 7+).
- **Storage Access Framework (SAF)**: SD card and USB device access.
- **Intent Service & Task Management**: Trigger tasks programmatically and schedule regular sync runs.

## Installation
Grab the latest version of the signed APK and install it on your phone.

| CPU architecture | Target | APK identifier |
|:---|:---|:---:|
| ARM 32 Bit | Older devices | `armeabi-v7a` |
| **ARM 64 Bit** | **Most devices** | `arm64-v8a` |
| Intel/AMD 32 Bit | TV boxes & tablets | `x86` |
| Intel/AMD 64 Bit | Emulators & PC | `x86_64` |

If unsure, use `-universal-release.apk`.

[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/org.openinit.multicloudfilemanager)
[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" alt="Get it on IzzyOnDroid" height="80">](https://apt.izzysoft.de/packages/org.openinit.multicloudfilemanager)

## Intent Integration
External applications can trigger sync tasks using Android Intents:

| Field | Value |
|:---|:---|
| **Package Name** | `org.openinit.multicloudfilemanager` |
| **Class Name** | `org.openinit.multicloudfilemanager.Services.SyncService` |
| **Action** | `START_TASK` |
| **Extra (Int)** | `task` (ID of the saved task) |
| **Extra (Boolean)** | `notification` (`true` or `false`) |

## Developing
Prerequisites:
- Go 1.20+
- Java 17
- Android SDK (or NDK version specified in `gradle.properties`)

```sh
# Build OSS Debug APK
./gradlew assembleOssDebug

# Build OSS Release APK
./gradlew assembleOssRelease
```

## Contributing
See [CONTRIBUTING.md](./CONTRIBUTING.md) for contribution guidelines.

## License & Attribution
- **Main Application License**: [GPLv3](./LICENSE)
- **Original Core Components License**: [MIT](./LICENSE_rcloneExplorer-1.7.4)
- **Full Legal Notices, Lineage & Third-Party Attributions**: [ATTRIBUTION.md](./ATTRIBUTION.md)
