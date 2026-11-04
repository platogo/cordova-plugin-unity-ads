import Foundation
#if canImport(Cordova)
import Cordova
#endif

// Cordova entry point. Reads the JS arguments and hands each action to its controller,
// the controllers own all Unity Ads SDK calls and their callback state.
@objc(UnityAdsPlugin)
class UnityAdsPlugin: CDVPlugin {
    private var initializationController: InitializationController!
    private var adShowController: AdShowController!

    override func pluginInitialize() {
        super.pluginInitialize()
        initializationController = InitializationController(plugin: self)
        adShowController = AdShowController(plugin: self)
    }

    // The WebView navigated or reloaded, nobody is listening to the old callbacks anymore
    override func onReset() {
        initializationController.reset()
        adShowController.reset()
    }

    // MARK: - Cordova exposed methods

    @objc func initialize(_ command: CDVInvokedUrlCommand) {
        initializationController.initialize(
            gameId: stringArgument(command, at: 0),
            testMode: boolArgument(command, at: 1),
            debugMode: boolArgument(command, at: 2),
            callbackId: command.callbackId
        )
    }

    @objc func show(_ command: CDVInvokedUrlCommand) {
        adShowController.show(
            serverId: stringArgument(command, at: 0),
            placementId: stringArgument(command, at: 1),
            callbackId: command.callbackId
        )
    }

    // MARK: - Argument helpers

    // Missing, null and empty strings are all treated as missing
    private func stringArgument(_ command: CDVInvokedUrlCommand, at index: UInt) -> String? {
        guard let value = command.argument(at: index) as? String, !value.isEmpty else { return nil }
        return value
    }

    private func boolArgument(_ command: CDVInvokedUrlCommand, at index: UInt) -> Bool {
        return (command.argument(at: index) as? Bool) ?? false
    }
}
