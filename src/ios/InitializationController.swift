import Foundation
import UnityAds
#if canImport(Cordova)
import Cordova
#endif

// Initializes the Unity Ads SDK and answers the initialize calls.
//
// The app can call initialize again while the first call is still running (bootup init and
// a "watch ad" tap right after). The SDK is only asked once, every waiting call gets the same result.
//
// All state is only read and written on the main thread: Cordova calls the plugin methods there
// and the SDK callbacks are moved there before touching it.
//
// Only this class talks to the SDK initialization API, so the Unity Ads 5.0.0 migration
// (UADSInitializationConfiguration + completion) stays contained here.
final class InitializationController: NSObject, UnityAdsInitializationDelegate {
    private weak var plugin: CDVPlugin?
    private var pendingCallbackIds: [String] = []
    private var initializing = false
    private var initialized = false

    init(plugin: CDVPlugin) {
        self.plugin = plugin
    }

    func initialize(gameId: String?, testMode: Bool, debugMode: Bool, callbackId: String) {
        if initialized {
            send(CDVPluginResult(status: .ok), to: callbackId)
            return
        }
        guard let gameId = gameId else {
            send(PluginError.result(message: "Game ID missing", code: PluginError.gameIdMissing), to: callbackId)
            return
        }

        pendingCallbackIds.append(callbackId)
        if initializing {
            // The SDK is already initializing, this call is answered together with the first one
            return
        }
        initializing = true

        // The first call into the SDK (even UnityAds.isInitialized) sets it up and blocks for hundreds of ms
        // (Cordova logs a THREAD WARNING), so nothing here touches UnityAds on the main thread,
        // as recommended by the Cordova iOS plugin guide
        plugin?.commandDelegate.run(inBackground: {
            if UnityAds.isInitialized() {
                DispatchQueue.main.async { self.finish(with: CDVPluginResult(status: .ok), succeeded: true) }
                return
            }
            self.applyLogLevel(debugMode: debugMode)
            UnityAds.initialize(gameId, testMode: testMode, initializationDelegate: self)
        })
    }

    // The WebView navigated or reloaded, nobody is listening to the old callbacks anymore.
    // A running initialization is kept, so the SDK is not initialized a second time.
    func reset() {
        pendingCallbackIds.removeAll()
    }

    // MARK: - UnityAdsInitializationDelegate

    func initializationComplete() {
        DispatchQueue.main.async {
            self.finish(with: CDVPluginResult(status: .ok), succeeded: true)
        }
    }

    func initializationFailed(_ error: UnityAdsInitializationError, withMessage message: String) {
        let result = PluginError.result(message: message, error: error)
        DispatchQueue.main.async {
            self.finish(with: result, succeeded: false)
        }
    }

    // MARK: - Helpers

    // Unity Ads 5.0.0 replaces this with UADSInitializationConfigurationBuilder.with(logLevel:)
    private func applyLogLevel(debugMode: Bool) {
        UnityAds.setDebugMode(debugMode)
    }

    // Ends the running initialization, a failed one can be retried by the next initialize call
    private func finish(with result: CDVPluginResult, succeeded: Bool) {
        initialized = succeeded
        initializing = false
        let callbackIds = pendingCallbackIds
        pendingCallbackIds.removeAll()
        for callbackId in callbackIds {
            send(result, to: callbackId)
        }
    }

    private func send(_ result: CDVPluginResult, to callbackId: String) {
        plugin?.commandDelegate.send(result, callbackId: callbackId)
    }
}
