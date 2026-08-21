### Language Selector

Set a language for each app on Android 13+. This is a [VegaBobo/Language-Selector](https://github.com/VegaBobo/Language-Selector) fork with a HyperOS / Miuix interface.

Shizuku is the recommended service, with root access also supported. Get the APK from [Releases](https://github.com/FreeTeaspoon/Language-Selector/releases).

<div>
<img src="https://raw.githubusercontent.com/FreeTeaspoon/Language-Selector/main/other/preview_1.jpg" alt="App list" width="200"/>
<img src="https://raw.githubusercontent.com/FreeTeaspoon/Language-Selector/main/other/preview_2.jpg" alt="App language" width="200"/>
<img src="https://raw.githubusercontent.com/FreeTeaspoon/Language-Selector/main/other/preview_3.jpg" alt="About" width="200"/>
</div>

### Features

- Set a language per app
- Pin languages for the list and the quick settings tile

It does not translate apps. It only sets the locale the app already ships. Skip system apps.

### Usage

1. Install and start Shizuku, or make root access available.
2. Open Language Selector and allow access to the installed-app list when prompted.
3. Grant Shizuku access if requested, then pick an app and a language.

Long-press a language to pin it. Pinned languages also show up on the QS tile.

### Release builds

Release tasks require a production signing key. Keep the keystore and passwords outside
the repository, for example in the user Gradle properties file or CI secrets:

- `releaseStoreFile`
- `releaseStorePassword`
- `releaseKeyAlias`
- `releaseKeyPassword`

When passwords are supplied directly, all four signing properties are required. On macOS,
the build can instead read the keystore password from Keychain. That setup uses
`releaseStoreFile` and `releaseKeyAlias` together with these non-secret properties:

- `releaseKeychainService`
- `releaseKeychainAccount`

Release tasks fail when production signing is not configured. Debug builds do not need
release signing properties.
