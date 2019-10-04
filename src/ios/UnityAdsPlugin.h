#import <Cordova/CDVPlugin.h>

@interface UnityAdsPlugin : CDVPlugin

- (void) initialize:(CDVInvokedUrlCommand *)command;
- (void) show:(CDVInvokedUrlCommand *)command;

@end
