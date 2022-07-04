# cordova-plugin-unity-ads

## Usage

### initialize

`UnityAds.initialize(UnityAdsGameId: string, testMode: boolean, debugMode: boolean): Promise`

`Promise` will be resolved on success and rejected on failure.

### show ad

`UnityAds.show(serverId?: string, videoAdPlacementId?: string): Promise`

`Promise` will be resolved after video has been successfully watched.
`Promise` will be rejected if video has been skiped or other failures.

## How to update the SDK

- Download the SDK version from Unity: [Android](https://github.com/Unity-Technologies/unity-ads-android/releases) & [iOS](https://github.com/Unity-Technologies/unity-ads-ios/releases)
- Copy the files into the `libs` folder
