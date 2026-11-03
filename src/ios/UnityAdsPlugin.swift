import Foundation
import UIKit
import UnityAds
#if canImport(Cordova)
import Cordova
#endif

@objc(UnityAdsPlugin)
class UnityAdsPlugin: CDVPlugin, UnityAdsInitializationDelegate, UnityAdsShowDelegate, UnityAdsLoadDelegate {
    // Init and show keep their own callback so one call can never answer the other.
    private var initCallbackId: String?
    private var showCallbackId: String?

    // The WebView navigated or reloaded, nobody is listening to the old callbacks anymore
    override func onReset() {
        initCallbackId = nil
        showCallbackId = nil
    }

    // MARK: - Cordova exposed methods

    @objc func initialize(_ command: CDVInvokedUrlCommand) {
        // The first call into the SDK (even UnityAds.isInitialized) sets it up and blocks for hundreds of ms
        // (Cordova logs a THREAD WARNING), so nothing here touches UnityAds on the main thread,
        // as recommended by the Cordova iOS plugin guide
        self.commandDelegate.run(inBackground: {
            if UnityAds.isInitialized() {
                let pluginResult = CDVPluginResult(status: .ok)
                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
                return
            }
            guard let gameId = command.argument(at: 0) as? String, !gameId.isEmpty else {
                let pluginResult = self.constructPluginError(message: "Game ID missing", error: "GAME_ID_MISSING")
                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
                return
            }
            let testMode = (command.argument(at: 1) as? Bool) ?? false
            let debugMode = (command.argument(at: 2) as? Bool) ?? false
            self.initCallbackId = command.callbackId
            UnityAds.setDebugMode(debugMode)
            UnityAds.initialize(gameId, testMode: testMode, initializationDelegate: self)
        })
    }


    @objc func show(_ command: CDVInvokedUrlCommand) {
        if !UnityAds.isInitialized() {
            let pluginResult = constructPluginError(message: "Unity Ads not initialized", error: "NOT_INITIALIZED")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }
        guard let serverId = command.argument(at: 0) as? String, !serverId.isEmpty else {
            let pluginResult = constructPluginError(message: "Server ID missing", error: "SERVER_ID_MISSING")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }
        guard let placementId = command.argument(at: 1) as? String, !placementId.isEmpty else {
            let pluginResult = constructPluginError(message: "Placement ID missing", error: "PLACEMENT_ID_MISSING")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }
        showCallbackId = command.callbackId
        let metaData = UADSPlayerMetaData()
        metaData.setServerId(serverId)
        metaData.commit()
        UnityAds.load(placementId, loadDelegate: self)
    }

    // MARK: - UnityAdsInitializationDelegate

    func initializationComplete() {
        finishInitialization(with: CDVPluginResult(status: .ok))
    }

    func initializationFailed(_ error: UnityAdsInitializationError, withMessage message: String) {
        finishInitialization(with: constructPluginError(message: message, error: error))
    }

    // MARK: - UnityAdsShowDelegate

    func unityAdsShowStart(_ placementId: String) {
        // Ad show started
    }

    func unityAdsShowClick(_ placementId: String) {
        // A click does not mean the ad was watched, the result comes with unityAdsShowComplete
    }

    func unityAdsShowComplete(_ placementId: String, withFinish state: UnityAdsShowCompletionState) {
        var result: CDVPluginResult
        switch state {
        case .showCompletionStateCompleted:
            result = CDVPluginResult(status: .ok, messageAs: "completed")
        case .showCompletionStateSkipped:
            result = constructPluginError(message: "skipped", error: "SKIPPED")
        @unknown default:
            result = constructPluginError(message: "unknown", error: "UNKNOWN")
        }
        finishShow(with: result)
    }

    func unityAdsShowFailed(_ placementId: String, withError error: UnityAdsShowError, withMessage message: String) {
        finishShow(with: constructPluginError(message: message, error: error))
    }

    // MARK: - UnityAdsLoadDelegate

    func unityAdsAdLoaded(_ placementId: String) {
        UnityAds.show(self.viewController, placementId: placementId, showDelegate: self)
    }

    func unityAdsAdFailed(toLoad placementId: String, withError error: UnityAdsLoadError, withMessage message: String) {
        finishShow(with: constructPluginError(message: message, error: error))
    }

    // MARK: - Callback Helpers

    private func finishInitialization(with result: CDVPluginResult) {
        guard let callbackId = initCallbackId else { return }
        initCallbackId = nil
        self.commandDelegate.send(result, callbackId: callbackId)
    }

    private func finishShow(with result: CDVPluginResult) {
        guard let callbackId = showCallbackId else { return }
        showCallbackId = nil
        self.commandDelegate.send(result, callbackId: callbackId)
    }

    // MARK: - Error String switches

    private func loadErrorToString(_ error: UnityAdsLoadError) -> String {
        switch error {
            case .initializeFailed:
                return "INITIALIZE_FAILED"
            case .internal:
                return "INTERNAL_ERROR"
            case .invalidArgument:
                return "INVALID_ARGUMENT"
            case .noFill:
                return "NO_FILL"
            case .timeout:
                return "TIMEOUT"
            @unknown default:
                return "UNKNOWN"
        }
    }

    private func showErrorToString(_ error: UnityAdsShowError) -> String {
        switch error {
            case .showErrorNotInitialized:
                return "NOT_INITIALIZED"
            case .showErrorNotReady:
                return "NOT_READY"
            case .showErrorVideoPlayerError:
                return "VIDEO_PLAYER_ERROR"
            case .showErrorInvalidArgument:
                return "INVALID_ARGUMENT"
            case .showErrorNoConnection:
                return "NO_CONNECTION"
            case .showErrorAlreadyShowing:
                return "ALREADY_SHOWING"
            case .showErrorInternalError:
                return "INTERNAL_ERROR"
            case .showErrorTimeout:
                return "TIMEOUT"
            @unknown default:
                return "UNKNOWN"
        }
    }

    private func initErrorToString(_ error: UnityAdsInitializationError) -> String {
        switch error {
            case .initializationErrorAdBlockerDetected:
                return "AD_BLOCKER_DETECTED"
            case .initializationErrorInternalError:
                return "INTERNAL_ERROR"
            case .initializationErrorInvalidArgument:
                return "INVALID_ARGUMENT"
            @unknown default:
                return "UNKNOWN"
        }
    }

    // MARK: - Plugin Error Helpers


    private func constructPluginError(message: String, error: UnityAdsShowError) -> CDVPluginResult {
        return constructPluginErrorResult(message: message, errorEnum: showErrorToString(error))
    }

    private func constructPluginError(message: String, error: UnityAdsLoadError) -> CDVPluginResult {
        return constructPluginErrorResult(message: message, errorEnum: loadErrorToString(error))
    }

    private func constructPluginError(message: String, error: UnityAdsInitializationError) -> CDVPluginResult {
        return constructPluginErrorResult(message: message, errorEnum: initErrorToString(error))
    }

    private func constructPluginError(message: String, error: String) -> CDVPluginResult {
        return constructPluginErrorResult(message: message, errorEnum: error)
    }

    private func constructPluginErrorResult(message: String, errorEnum: String) -> CDVPluginResult {
        do {
            let messageStructure = [message, errorEnum]
            let jsonData = try JSONSerialization.data(withJSONObject: messageStructure)
            if let jsonString = String(data: jsonData, encoding: .utf8) {
                return CDVPluginResult(status: .error, messageAs: jsonString)
            } else {
                return CDVPluginResult(status: .error, messageAs: "[\"Error when parsing SDK failure\", \"JSON_PARSE_FAILED\"]")
            }
        } catch {
            return CDVPluginResult(status: .error, messageAs: "[\"Error when parsing SDK failure\", \"JSON_PARSE_FAILED\"]")
        }
    }

}
