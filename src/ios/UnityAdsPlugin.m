#import "UnityAdsPlugin.h"
#import <Cordova/CDVPlugin.h>
#import <UnityAds/UnityAds.h>

@implementation UnityAdsPlugin

@synthesize initializeCallbackId;
@synthesize showCallbackId;

- (void)initialize:(CDVInvokedUrlCommand*)command
{
    NSString* gameId = [command.arguments objectAtIndex:0];

    self.initializeCallbackId = command.callbackId;
    ViewController* vc = [ViewController alloc];
    [vc initialize:self];
    [UnityAds initialize:gameId delegate:[vc self]];
}

- (void)show:(CDVInvokedUrlCommand*)command
{
    self.showCallbackId = command.callbackId;
    
    [UnityAds show:self.viewController];
}

@end

@implementation ViewController

@synthesize unityAdsPlugin;

- (void)initialize:(UnityAdsPlugin *)unityAdsPlugin_ {
    self.unityAdsPlugin = unityAdsPlugin_;
}

- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)unityAdsDidError:(UnityAdsError)error withMessage:(nonnull NSString *)message {

}

- (void)unityAdsDidFinish:(nonnull NSString *)placementId withFinishState:(UnityAdsFinishState)state {
    if (state == kUnityAdsFinishStateCompleted) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];;
        [unityAdsPlugin.commandDelegate sendPluginResult:pluginResult callbackId:unityAdsPlugin.showCallbackId];
    }
}

- (void)unityAdsDidStart:(nonnull NSString *)placementId {

}

- (void)unityAdsReady:(nonnull NSString *)placementId {
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];;
    [unityAdsPlugin.commandDelegate sendPluginResult:pluginResult callbackId:unityAdsPlugin.initializeCallbackId];
}

- (void)unityAdsDidClick:(nonnull NSString *)placementId {
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];;
    [unityAdsPlugin.commandDelegate sendPluginResult:pluginResult callbackId:unityAdsPlugin.showCallbackId];
}

- (void)unityAdsPlacementStateChanged:(nonnull NSString *)placementId oldState:(UnityAdsPlacementState)oldState newState:(UnityAdsPlacementState)newState {
    
}

@end
