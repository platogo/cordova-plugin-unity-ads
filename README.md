# cordova-plugin-unity-ads

## initialize
`UnityAds.initialize(UnityAdsGameId: string, testMode: boolean, debugMode: boolean): Promise`

`Promise` will be resolved on success and rejected on failure.

## show ad
`UnityAds.show(serverId?: string, videoAdPlacementId?: string): Promise`

`Promise` will be resolved after video has been successfully watched.  
`Promise` will be rejected if video has been skiped or other failures.
