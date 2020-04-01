#import "UnityAdsPlugin.h"
@implementation UnityAdsPlugin

@synthesize callbackId;

- (void)initialize:(CDVInvokedUrlCommand*)command
{
    if ([UnityAds isInitialized]) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];;
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    NSString* gameId = [command argumentAtIndex:0];
    BOOL testMode = [[command argumentAtIndex:1] boolValue]; // is NO if not passed as argument
    
    BOOL debugMode = [[command argumentAtIndex:2] boolValue]; // is NO if not passed as argument
    if (gameId != nil && [gameId length] > 0) {
        self.callbackId = command.callbackId;
        ViewController* vc = [ViewController alloc];
        [vc initialize:self];
        [UnityAds setDebugMode:debugMode];
        [UnityAds initialize:gameId delegate:[vc self] testMode: testMode];
    } else {        
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: @"Game id imssing:"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void)show:(CDVInvokedUrlCommand*)command
{
    self.callbackId = command.callbackId;
    
    if([UnityAds isReady]) {
        if (command.arguments.count > 0) {
            NSString* serverId = [command argumentAtIndex:0];
            id playerMetaData = [[UADSPlayerMetaData alloc] init];
            [playerMetaData setServerId:serverId];
            [playerMetaData commit];
        }
        
        if (command.arguments.count > 1) {
            NSString* videoAdPlacementId = [command argumentAtIndex:1];
            if (videoAdPlacementId != nil) {
                [UnityAds show:self.viewController placementId:videoAdPlacementId];
                return;
            }
        }
        [UnityAds show:self.viewController];
        
    }
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
    NSString *errorMessage = @"";
    
    if (error == kUnityAdsErrorNotInitialized) {
        errorMessage = [@"NOT_INITIALIZED - " stringByAppendingString:message];
    } else if (error == kUnityAdsErrorInitializedFailed) {
        errorMessage = [@"INITIALIZE_FAILED - " stringByAppendingString:message];
    } else {
        errorMessage = [@"INTERNAL_ERROR - " stringByAppendingString:message];
    }
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: errorMessage];
    [unityAdsPlugin.commandDelegate sendPluginResult:pluginResult callbackId:unityAdsPlugin.callbackId];
}


- (void)unityAdsDidFinish:(nonnull NSString *)placementId withFinishState:(UnityAdsFinishState)state {
    if (state == kUnityAdsFinishStateCompleted) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];;
        [unityAdsPlugin.commandDelegate sendPluginResult:pluginResult callbackId:unityAdsPlugin.callbackId];
    } else if (state == kUnityAdsFinishStateSkipped) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: @"VIDEO_SKIPPED"];
        [unityAdsPlugin.commandDelegate sendPluginResult:pluginResult callbackId:unityAdsPlugin.callbackId];
    } else if (state == kUnityAdsFinishStateError) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: @"DID FINISH WITH ERROR"];
        [unityAdsPlugin.commandDelegate sendPluginResult:pluginResult callbackId:unityAdsPlugin.callbackId];
    }
}

- (void)unityAdsDidStart:(nonnull NSString *)placementId {

}

- (void)unityAdsReady:(nonnull NSString *)placementId {
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];;
    [unityAdsPlugin.commandDelegate sendPluginResult:pluginResult callbackId:unityAdsPlugin.callbackId];
}

- (void)unityAdsDidClick:(nonnull NSString *)placementId {
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];;
    [unityAdsPlugin.commandDelegate sendPluginResult:pluginResult callbackId:unityAdsPlugin.callbackId];
}

- (void)unityAdsPlacementStateChanged:(nonnull NSString *)placementId oldState:(UnityAdsPlacementState)oldState newState:(UnityAdsPlacementState)newState {
    
}

@end
