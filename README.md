# cordova-plugin-unity-ads

## Usage

### Initialize

```typescript
UnityAds.initialize(UnityAdsGameId: string, testMode: boolean, debugMode: boolean): Promise
```

- The `Promise` will be resolved on success and rejected on failure (for example the device is not capable of receiving ads)

### Show an ad

```typescript
UnityAds.show(serverId: string, videoAdPlacementId: string): Promise
```

- Both arguments are required
- The `Promise` will be resolved after video has been successfully watched.
- The `Promise` will be rejected if video could not be loaded, a failure happened while showing the video, the video was skipped, canceled or another issue
- The `Promise` will be rejected right away, without loading an ad, with
  - `NOT_INITIALIZED` if `initialize` has not succeeded yet
  - `SERVER_ID_MISSING` if `serverId` is missing
  - `PLACEMENT_ID_MISSING` if `videoAdPlacementId` is missing
  - `ALREADY_SHOWING` if another ad is still loading or showing

## How to update the SDK

- Make sure to check the [Changelog](https://docs.unity.com/en-us/grow/ads/changelog) from Unity
- Always match the `UnityAds` version when bumping the plugin versions
- Bump the `package.json` version
- Bump the SDK version in the `plugin.xml` file
  - For `Android` in the `UNITY_ADS_VERSION` preference
  - For `iOS` in the podspec definition `<pod name="UnityAds" spec="X.Y.Z" />` where `X.Y.Z` is the exact version
