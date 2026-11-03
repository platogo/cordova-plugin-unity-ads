# Change Log

## [4.21.0]

### Changes
- Initialize and show keep separate callbacks, so a show call can no longer receive the initialize result and vice versa
- A click on the ad no longer resolves `show`, only a completed ad does
- Android: `show` rejects with `NOT_INITIALIZED` or `PLACEMENT_ID_MISSING` instead of never answering
- Android: missing or `null` arguments are no longer sent to the SDK as the string `"null"`, a missing game ID rejects with `GAME_ID_MISSING` (same as iOS)
- Android: `initialize` resolves right away when the SDK is already initialized (same as iOS)
- Android: the SDK initialization runs on Cordova's thread pool instead of blocking the WebCore thread
- Android: the ad is shown on the UI thread, unknown completion states report `UNKNOWN` (same as iOS)
- Callbacks are cleared in `onReset` when the WebView reloads
- iOS: the SDK initialization runs in the background instead of blocking the main thread
- iOS: `Cordova` import guarded with `#if canImport(Cordova)` as recommended by the Cordova iOS plugin guide
- Bump `UnityAds` SDK to `4.21.0` on `Android` & `iOS`

## [4.19.1]

### Changes
- Android: error payload is built with `JSONArray` so SDK messages containing line breaks can be parsed

## [4.19.0]

### Changes
- Updated Unity Ads SDK to 4.19.0
- Aligned Plugin version with Unity Ads SDK version (for easier comparison and tracking of changes)

## [2.1.0]

### Changes

- (iOS) Now error codes for return values to CDV are parsed using the error enums and return proper strings instead of raw values
- Error format is harmonized between Android & iOS

## [2.0.0]

### Changes

- [BREAKING] Now the plugin requires targeting iOS 15.0

## [1.0.0]

### Changes

- Initial stable release
- Usage of Swift for iOS native code
- Native SDKs for UnityAds 4.16.3
