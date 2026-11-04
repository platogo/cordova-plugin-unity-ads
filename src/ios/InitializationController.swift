import Foundation
import UnityAds
#if canImport(Cordova)
import Cordova
#endif

// Initializes the Unity Ads SDK and answers the initialize calls.
//
// Only this class talks to the SDK initialization API, so the Unity Ads 5.0.0 migration
// (UADSInitializationConfiguration + completion) stays contained here.
final class InitializationController: NSObject, UnityAdsInitializationDelegate {
    private weak var plugin: CDVPlugin?
    private var callbackId: String?

    init(plugin: CDVPlugin) {
        self.plugin = plugin
    }

    func initialize(gameId: String?, testMode: Bool, debugMode: Bool, callbackId: String) {
        // The first call into the SDK (even UnityAds.isInitialized) sets it up and blocks for hundreds of ms
        // (Cordova logs a THREAD WARNING), so nothing here touches UnityAds on the main thread,
        // as recommended by the Cordova iOS plugin guide
        plugin?.commandDelegate.run(inBackground: {
            if UnityAds.isInitialized() {
                self.send(CDVPluginResult(status: .ok), to: callbackId)
                return
            }
            guard let gameId = gameId else {
                self.send(PluginError.result(message: "Game ID missing", code: PluginError.gameIdMissing), to: callbackId)
                return
            }
            self.callbackId = callbackId
            self.applyLogLevel(debugMode: debugMode)
            UnityAds.initialize(gameId, testMode: testMode, initializationDelegate: self)
        })
    }

    // The WebView navigated or reloaded, nobody is listening to the old callback anymore
    func reset() {
        callbackId = nil
    }

    // MARK: - UnityAdsInitializationDelegate

    func initializationComplete() {
        finish(with: CDVPluginResult(status: .ok))
    }

    func initializationFailed(_ error: UnityAdsInitializationError, withMessage message: String) {
        finish(with: PluginError.result(message: message, error: error))
    }

    // MARK: - Helpers

    // Unity Ads 5.0.0 replaces this with UADSInitializationConfigurationBuilder.with(logLevel:)
    private func applyLogLevel(debugMode: Bool) {
        UnityAds.setDebugMode(debugMode)
    }

    private func finish(with result: CDVPluginResult) {
        guard let callbackId = callbackId else { return }
        self.callbackId = nil
        send(result, to: callbackId)
    }

    private func send(_ result: CDVPluginResult, to callbackId: String) {
        plugin?.commandDelegate.send(result, callbackId: callbackId)
    }
}
