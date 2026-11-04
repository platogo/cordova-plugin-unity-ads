import Foundation
import UIKit
import UnityAds
#if canImport(Cordova)
import Cordova
#endif

// Loads and shows an ad and answers the show calls.
//
// Only this class talks to the SDK load/show API, so the Unity Ads 5.0.0 migration
// (UADSRewardedAd.load + show) stays contained here.
final class AdShowController: NSObject, UnityAdsLoadDelegate, UnityAdsShowDelegate {
    private weak var plugin: CDVPlugin?
    private var callbackId: String?

    init(plugin: CDVPlugin) {
        self.plugin = plugin
    }

    func show(serverId: String?, placementId: String?, callbackId: String) {
        if !UnityAds.isInitialized() {
            send(PluginError.result(message: "Unity Ads not initialized", code: PluginError.notInitialized), to: callbackId)
            return
        }
        guard let serverId = serverId else {
            send(PluginError.result(message: "Server ID missing", code: PluginError.serverIdMissing), to: callbackId)
            return
        }
        guard let placementId = placementId else {
            send(PluginError.result(message: "Placement ID missing", code: PluginError.placementIdMissing), to: callbackId)
            return
        }
        self.callbackId = callbackId
        setServerId(serverId)
        UnityAds.load(placementId, loadDelegate: self)
    }

    // The WebView navigated or reloaded, nobody is listening to the old callback anymore
    func reset() {
        callbackId = nil
    }

    // MARK: - UnityAdsLoadDelegate

    func unityAdsAdLoaded(_ placementId: String) {
        guard let viewController = plugin?.viewController else { return }
        UnityAds.show(viewController, placementId: placementId, showDelegate: self)
    }

    func unityAdsAdFailed(toLoad placementId: String, withError error: UnityAdsLoadError, withMessage message: String) {
        finish(with: PluginError.result(message: message, error: error))
    }

    // MARK: - UnityAdsShowDelegate

    func unityAdsShowStart(_ placementId: String) {
        // Ad show started
    }

    func unityAdsShowClick(_ placementId: String) {
        // A click does not mean the ad was watched, the result comes with unityAdsShowComplete
    }

    func unityAdsShowComplete(_ placementId: String, withFinish state: UnityAdsShowCompletionState) {
        switch state {
        case .showCompletionStateCompleted:
            finish(with: CDVPluginResult(status: .ok, messageAs: "completed"))
        case .showCompletionStateSkipped:
            finish(with: PluginError.result(message: "skipped", code: PluginError.skipped))
        @unknown default:
            finish(with: PluginError.result(message: "unknown", code: PluginError.unknown))
        }
    }

    func unityAdsShowFailed(_ placementId: String, withError error: UnityAdsShowError, withMessage message: String) {
        finish(with: PluginError.result(message: message, error: error))
    }

    // MARK: - Helpers

    // The server ID (sid) is sent back in Unity's server-to-server redeem callback.
    // UADSPlayerMetaData builds on UADSMetaData, deprecated with Unity Ads 4.19.0, swap it here when migrating to 5.0.0.
    private func setServerId(_ serverId: String) {
        let metaData = UADSPlayerMetaData()
        metaData.setServerId(serverId)
        metaData.commit()
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
