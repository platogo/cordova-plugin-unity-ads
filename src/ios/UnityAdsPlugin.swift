import Foundation
import UIKit
import UnityAds

@objc(UnityAdsPlugin)
class UnityAdsPlugin: CDVPlugin, UnityAdsInitializationDelegate, UnityAdsShowDelegate, UnityAdsLoadDelegate {
    var currentCallbackId: String?
    
    // MARK: - Cordova exposed methods
    
    @objc func initialize(_ command: CDVInvokedUrlCommand) {
        self.currentCallbackId = command.callbackId
        if UnityAds.isInitialized() {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }
        guard let gameId = command.argument(at: 0) as? String, !gameId.isEmpty else {
            let pluginResult = constructPluginError(message: "Game ID missing", error: "GAME_ID_MISSING")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }
        let testMode = (command.argument(at: 1) as? Bool) ?? false
        let debugMode = (command.argument(at: 2) as? Bool) ?? false
        UnityAds.setDebugMode(false)
        UnityAds.initialize(gameId, testMode: false, initializationDelegate: self)
    }
    
    
    @objc func show(_ command: CDVInvokedUrlCommand) {
        self.currentCallbackId = command.callbackId
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
        let metaData = UADSPlayerMetaData()
        metaData.setServerId(serverId)
        metaData.commit()
        UnityAds.load(placementId, loadDelegate: self)
    }
    
    // MARK: - UnityAdsInitializationDelegate
    
    func initializationComplete() {
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        if let callbackId = self.currentCallbackId {
            self.commandDelegate.send(pluginResult, callbackId: callbackId)
        }
    }
    
    func initializationFailed(_ error: UnityAdsInitializationError, withMessage message: String) {
        let pluginResult = constructPluginError(message: message, error: error)
        if let callbackId = self.currentCallbackId {
            self.commandDelegate.send(pluginResult, callbackId: callbackId)
        }
    }
    
    // MARK: - UnityAdsShowDelegate
    
    func unityAdsShowStart(_ placementId: String) {
        // Ad show started
    }
    
    func unityAdsShowClick(_ placementId: String) {
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        if let callbackId = self.currentCallbackId {
            self.commandDelegate.send(pluginResult, callbackId: callbackId)
        }
    }

    func unityAdsShowComplete(_ placementId: String, withFinish state: UnityAdsShowCompletionState) {
        var result: CDVPluginResult
        switch state {
        case .showCompletionStateCompleted:
            result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "completed")
        case .showCompletionStateSkipped:
            result = constructPluginError(message: "skipped", error: "SKIPPED")
        @unknown default:
            result = constructPluginError(message: "unknown", error: "unknown")
        }
        if let callbackId = self.currentCallbackId {
            self.commandDelegate.send(result, callbackId: callbackId)
        }
    }
    
    func unityAdsShowFailed(_ placementId: String, withError error: UnityAdsShowError, withMessage message: String) {
        let pluginResult = constructPluginError(message: message, error: error)
        if let callbackId = self.currentCallbackId {
            self.commandDelegate.send(pluginResult, callbackId: callbackId)
        }
    }
    
    // MARK: - UnityAdsLoadDelegate
    
    func unityAdsAdLoaded(_ placementId: String) {
        UnityAds.show(self.viewController, placementId: placementId, showDelegate: self)
    }
    
    func unityAdsAdFailed(toLoad placementId: String, withError error: UnityAdsLoadError, withMessage message: String) {
        let pluginResult = constructPluginError(message: message, error: error)
        if let callbackId = self.currentCallbackId {
            self.commandDelegate.send(pluginResult, callbackId: callbackId)
        }
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
                return CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: jsonString)
            } else {
               return CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "[\"Error when parsing SDK failure\", \"JSON_PARSE_FAILED\"]")
            }
        } catch {
            return CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "[\"Error when parsing SDK failure\", \"JSON_PARSE_FAILED\"]")        }
    }
    
}
