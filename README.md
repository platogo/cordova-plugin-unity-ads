# cordova-plugin-unity-ads

## Usage

### Initialize

```typescript
UnityAds.initialize(UnityAdsGameId: string, testMode: boolean, debugMode: boolean): Promise
```

- The `Promise` will be resolved on success and rejected on failure (for example the device is not capable of receiving ads)

### Show an ad

```typescript
UnityAds.show(serverId?: string, videoAdPlacementId?: string): Promise
```

- The `Promise` will be resolved after video has been successfully watched.
- The `Promise` will be rejected if video could not be loaded, a failure happened while showing the video, the video was skipped, canceled or another issue

## How to update the SDK

- Make sure to check the [Changelog](https://docs.unity.com/en-us/grow/ads/changelog) from Unity
- Bump the SDK version in the `plugin.xml` file
  - For `Android` in the `UNITY_ADS_VERSION` preference
  - For `iOS` in the podpsec definition `<pod name="UnityAds" spec="X.Y.Z" />` where `X.Y.Z` is the exact version
