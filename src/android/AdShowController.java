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

import java.util.Optional;

/**
 * Loads and shows an ad and answers the show calls.
 *
 * Only this class talks to the SDK load/show API, so the Unity Ads 5.0.0 migration
 * (RewardedAd.load + RewardedAd.show) stays contained here.
 */
final class AdShowController implements IUnityAdsLoadListener, IUnityAdsShowListener {
    private static final String TAG = "UnityAds";

    private final CordovaInterface cordova;

    // Guarded by this: show() runs on the WebCore thread, the SDK calls back on other threads.
    // showInProgress outlives the callback on reset, an ad that is still on screen
    // must not be answered to a show call made after the reload.
    private Optional<CallbackContext> callback = Optional.empty();
    private boolean showInProgress = false;

    AdShowController(CordovaInterface cordova) {
        this.cordova = cordova;
    }

    void show(Optional<String> serverIdArg, Optional<String> placementIdArg, CallbackContext callbackContext) {
        if (!UnityAds.isInitialized()) {
            callbackContext.error(PluginErrors.payload("Unity Ads not initialized", PluginErrors.NOT_INITIALIZED));
            return;
        }
        if (!serverIdArg.isPresent()) {
            callbackContext.error(PluginErrors.payload("Server ID missing", PluginErrors.SERVER_ID_MISSING));
            return;
        }
        if (!placementIdArg.isPresent()) {
            callbackContext.error(PluginErrors.payload("Placement ID missing", PluginErrors.PLACEMENT_ID_MISSING));
            return;
        }
        String serverId = serverIdArg.get();
        String placementId = placementIdArg.get();

        synchronized (this) {
            if (showInProgress) {
                // Answering this call would drop the pending one, so the pending one keeps the slot
                callbackContext.error(PluginErrors.payload("An ad is already loading or showing", PluginErrors.ALREADY_SHOWING));
                return;
            }
            showInProgress = true;
            callback = Optional.of(callbackContext);
        }

        setServerId(serverId);
        UnityAds.load(placementId, this);
    }

    // The WebView navigated or reloaded, nobody is listening to the old callback anymore
    synchronized void reset() {
        callback = Optional.empty();
    }

    // MARK: - IUnityAdsLoadListener

    @Override
    public void onUnityAdsAdLoaded(String placementId) {
        Optional<Activity> activity = Optional.ofNullable(cordova.getActivity());
        if (!activity.isPresent()) {
            finishWithError("Activity not available to show the ad", PluginErrors.ACTIVITY_MISSING);
            return;
        }
        // Showing the ad is UI work, Cordova recommends running it on the UI thread
        Activity presenter = activity.get();
        presenter.runOnUiThread(() -> UnityAds.show(presenter, placementId, new UnityAdsShowOptions(), this));
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
        takeCallback().ifPresent(CallbackContext::success);
    }

    private void finishWithError(String message, String code) {
        String payload = PluginErrors.payload(message, code);
        takeCallback().ifPresent(current -> current.error(payload));
    }

    // Ends the running show, the next show call can start.
    // Empty when the WebView was reset while the ad was showing.
    private synchronized Optional<CallbackContext> takeCallback() {
        Optional<CallbackContext> current = callback;
        callback = Optional.empty();
        showInProgress = false;
        return current;
    }
}
