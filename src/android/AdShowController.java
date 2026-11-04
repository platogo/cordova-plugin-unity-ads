package com.platogo.cordova.unityads;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.ads.metadata.PlayerMetaData;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;

import android.app.Activity;
import android.util.Log;

/**
 * Loads and shows an ad and answers the show calls.
 *
 * Only this class talks to the SDK load/show API, so the Unity Ads 5.0.0 migration
 * (RewardedAd.load + RewardedAd.show) stays contained here.
 */
final class AdShowController implements IUnityAdsLoadListener, IUnityAdsShowListener {
    private static final String TAG = "UnityAds";

    private final CordovaInterface cordova;
    // volatile: show() runs on the WebCore thread, the SDK calls back on other threads.
    private volatile CallbackContext callback;

    AdShowController(CordovaInterface cordova) {
        this.cordova = cordova;
    }

    void show(String serverId, String placementId, CallbackContext callbackContext) {
        if (!UnityAds.isInitialized()) {
            callbackContext.error(PluginErrors.payload("Unity Ads not initialized", PluginErrors.NOT_INITIALIZED));
            return;
        }
        if (placementId == null) {
            callbackContext.error(PluginErrors.payload("Placement ID missing", PluginErrors.PLACEMENT_ID_MISSING));
            return;
        }

        if (serverId != null) {
            setServerId(serverId);
        } else {
            Log.w(TAG, "serverId arg missing!");
        }

        callback = callbackContext;
        UnityAds.load(placementId, this);
    }

    // The WebView navigated or reloaded, nobody is listening to the old callback anymore
    void reset() {
        callback = null;
    }

    // MARK: - IUnityAdsLoadListener

    @Override
    public void onUnityAdsAdLoaded(String placementId) {
        Activity activity = cordova.getActivity();
        if (activity == null) {
            finishWithError("Activity not available to show the ad", PluginErrors.ACTIVITY_MISSING);
            return;
        }
        // Showing the ad is UI work, Cordova recommends running it on the UI thread
        activity.runOnUiThread(() -> UnityAds.show(activity, placementId, new UnityAdsShowOptions(), this));
    }

    @Override
    public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
        Log.d(TAG, String.format("onUnityAdsFailedToLoad %s: %s", placementId, message));
        finishWithError(message, PluginErrors.code(error));
    }

    // MARK: - IUnityAdsShowListener

    @Override
    public void onUnityAdsShowStart(String placementId) {
        Log.v(TAG, "onUnityAdsShowStart: " + placementId);
    }

    @Override
    public void onUnityAdsShowClick(String placementId) {
        // A click does not mean the ad was watched, the result comes with onUnityAdsShowComplete
        Log.v(TAG, "onUnityAdsShowClick: " + placementId);
    }

    @Override
    public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
        if (UnityAds.UnityAdsShowCompletionState.COMPLETED.equals(state)) {
            finishWithSuccess();
            return;
        }
        if (UnityAds.UnityAdsShowCompletionState.SKIPPED.equals(state)) {
            finishWithError("skipped", PluginErrors.SKIPPED);
            return;
        }
        finishWithError("unknown", PluginErrors.UNKNOWN);
    }

    @Override
    public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
        Log.d(TAG, String.format("onUnityAdsShowFailure %s: %s", placementId, message));
        finishWithError(message, PluginErrors.code(error));
    }

    // MARK: - Helpers

    // The server ID (sid) is sent back in Unity's server-to-server redeem callback.
    // PlayerMetaData is deprecated with Unity Ads 4.19.0, swap it here when migrating to 5.0.0.
    private void setServerId(String serverId) {
        PlayerMetaData playerMetaData = new PlayerMetaData(cordova.getContext().getApplicationContext());
        playerMetaData.setServerId(serverId);
        playerMetaData.commit();
    }

    private void finishWithSuccess() {
        CallbackContext current = callback;
        callback = null;
        if (current != null) {
            current.success();
        }
    }

    private void finishWithError(String message, String code) {
        CallbackContext current = callback;
        callback = null;
        if (current != null) {
            current.error(PluginErrors.payload(message, code));
        }
    }
}
