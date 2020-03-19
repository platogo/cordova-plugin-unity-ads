#import <Cordova/CDVPlugin.h>
#import <UnityAds/UnityAds.h>
@interface UnityAdsPlugin: CDVPlugin
//@synthesize initializeCallbackId;
//@synthesize showCallbackId;
@property (nonatomic, retain) NSString* initializeCallbackId;
@property (nonatomic, retain) NSString* showCallbackId;
- (void)initialize:(CDVInvokedUrlCommand*)command;
- (void)show:(CDVInvokedUrlCommand*)command;
@end

@interface ViewController: UIViewController<UnityAdsDelegate>
@property (nonatomic, retain) UnityAdsPlugin* unityAdsPlugin;
- (void)initialize:(UnityAdsPlugin *)unityAdsPlugin;
- (void)unityAdsDidError:(UnityAdsError)error withMessage:(nonnull NSString *)message;
- (void)unityAdsDidFinish:(nonnull NSString *)placementId withFinishState:(UnityAdsFinishState)state;
- (void)unityAdsDidStart:(nonnull NSString *)placementId;
- (void)unityAdsReady:(nonnull NSString *)placementId;
- (void)unityAdsDidClick:(nonnull NSString *)placementId;
- (void)unityAdsPlacementStateChanged:(nonnull NSString *)placementId oldState:(UnityAdsPlacementState)oldState newState:(UnityAdsPlacementState)newState;
@end
