import Foundation
import UIKit
import UnityAds

@objc(UnityAdsPlugin)
class UnityAdsPlugin: CDVPlugin, UnityAdsInitializationDelegate, UnityAdsShowDelegate, UnityAdsLoadDelegate {
    var currentCallbackId: String?
    
    @objc func initialize(_ command: CDVInvokedUrlCommand) {
        self.currentCallbackId = command.callbackId
        if UnityAds.isInitialized() {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }
        guard let gameId = command.argument(at: 0) as? String, !gameId.isEmpty else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Game ID missing")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }
        let testMode = (command.argument(at: 1) as? Bool) ?? false
        let debugMode = (command.argument(at: 2) as? Bool) ?? false
        UnityAds.setDebugMode(debugMode)
        UnityAds.initialize(gameId, testMode: testMode, initializationDelegate: self)
    }
    
    
    @objc func show(_ command: CDVInvokedUrlCommand) {
        self.currentCallbackId = command.callbackId
        if !UnityAds.isInitialized() {
            return
        }
        guard let serverId = command.argument(at: 0) as? String, !serverId.isEmpty else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Server ID missing")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }
        guard let placementId = command.argument(at: 1) as? String, !placementId.isEmpty else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Placement ID missing")
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
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "\(error): \(message)")
        if let callbackId = self.currentCallbackId {
            self.commandDelegate.send(pluginResult, callbackId: callbackId)
        }
    }
    
    // MARK: - UnityAdsShowDelegate
    
    func unityAdsShowStart(_ placementId: String) {
        // Ad show started
    }
    
    func unityAdsShowClick(_ placementId: String) {
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "adClicked")
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
            result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "skipped")
        @unknown default:
            result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "unknown")
        }
        if let callbackId = self.currentCallbackId {
            self.commandDelegate.send(result, callbackId: callbackId)
        }
    }
    
    func unityAdsShowFailed(_ placementId: String, withError error: UnityAdsShowError, withMessage message: String) {
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "\(error): \(message)")
        if let callbackId = self.currentCallbackId {
            self.commandDelegate.send(pluginResult, callbackId: callbackId)
        }
    }
    
    // MARK: - UnityAdsLoadDelegate
    
    func unityAdsAdLoaded(_ placementId: String) {
        UnityAds.show(self.viewController, placementId: placementId, showDelegate: self)
    }
    
    func unityAdsAdFailed(toLoad placementId: String, withError error: UnityAdsLoadError, withMessage message: String) {
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "\(error): \(message)")
        if let callbackId = self.currentCallbackId {
            self.commandDelegate.send(pluginResult, callbackId: callbackId)
        }
    }
    
}
