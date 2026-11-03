package com.platogo.cordova.unityads;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.ads.metadata.PlayerMetaData;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;

import android.app.Activity;
import android.util.Log;

public class UnityAdsPlugin extends CordovaPlugin
        implements IUnityAdsInitializationListener, IUnityAdsLoadListener, IUnityAdsShowListener {
    private static final String TAG = "UnityAds";
    private static final String ACTION_INITIALIZE = "initialize";
    private static final String ACTION_SHOW = "show";

    // Init and show keep their own callback so one call can never answer the other.
    // volatile: execute() runs on the WebCore thread, the SDK calls back on other threads.
    private volatile CallbackContext initCallback;
    private volatile CallbackContext showCallback;

    public static String getErrorMessage(String message, String error) {
        // The message comes from the Unity Ads SDK and can contain characters that are not
        // valid inside a JSON string literal, most notably newlines.
        // We see this in Sentry errors coming from Unity containing "errorEnum":"JSON_PARSE_FAILED"
        // Building the array with JSONArray escapes them, so the client can parse it and read the error enum.
        JSONArray errorMessage = new JSONArray();
        errorMessage.put(message);
        errorMessage.put(error);

        return errorMessage.toString();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (ACTION_INITIALIZE.equals(action)) {
            initialize(args, callbackContext);
            return true;
        }
        if (ACTION_SHOW.equals(action)) {
            show(args, callbackContext);
            return true;
        }
        return false; // Returning false results in a "MethodNotFound" error.
    }

    @Override
    public void onReset() {
        // The WebView navigated or reloaded, nobody is listening to the old callbacks anymore
        initCallback = null;
        showCallback = null;
    }

    // MARK: - Actions

    private void initialize(JSONArray args, CallbackContext callbackContext) {
        if (UnityAds.isInitialized()) {
            callbackContext.success();
            return;
        }

        String gameId = getStringArgument(args, 0);
        if (gameId == null) {
            callbackContext.error(getErrorMessage("Game ID missing", "GAME_ID_MISSING"));
            return;
        }
        boolean testMode = args.optBoolean(1, false);
        boolean debugMode = args.optBoolean(2, false);

        initCallback = callbackContext;
        // UnityAds.initialize does its setup synchronously and can block for hundreds of ms,
        // run it off the WebCore thread as recommended by the Cordova Android plugin guide
        cordova.getThreadPool().execute(() -> {
            UnityAds.setDebugMode(debugMode);
            UnityAds.initialize(cordova.getContext().getApplicationContext(), gameId, testMode, this);
        });
    }

    private void show(JSONArray args, CallbackContext callbackContext) {
        if (!UnityAds.isInitialized()) {
            callbackContext.error(getErrorMessage("Unity Ads not initialized", "NOT_INITIALIZED"));
            return;
        }

        String serverId = getStringArgument(args, 0);
        String placementId = getStringArgument(args, 1);
        if (placementId == null) {
            callbackContext.error(getErrorMessage("Placement ID missing", "PLACEMENT_ID_MISSING"));
            return;
        }

        if (serverId != null) {
            PlayerMetaData playerMetaData = new PlayerMetaData(cordova.getContext().getApplicationContext());
            playerMetaData.setServerId(serverId);
            playerMetaData.commit();
        } else {
            Log.w(TAG, "serverId arg missing!");
        }

        showCallback = callbackContext;
        UnityAds.load(placementId, this);
    }

    // MARK: - IUnityAdsInitializationListener

    @Override
    public void onInitializationComplete() {
        CallbackContext callback = initCallback;
        initCallback = null;
        if (callback != null) {
            callback.success();
        }
    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
        Log.w(TAG, "onInitializationFailed: " + message);
        CallbackContext callback = initCallback;
        initCallback = null;
        if (callback != null) {
            callback.error(getErrorMessage(message, error.name()));
        }
    }

    // MARK: - IUnityAdsLoadListener

    @Override
    public void onUnityAdsAdLoaded(String placementId) {
        Activity activity = cordova.getActivity();
        if (activity == null) {
            finishShowWithError("Activity not available to show the ad", "ACTIVITY_MISSING");
            return;
        }
        // Showing the ad is UI work, Cordova recommends running it on the UI thread
        activity.runOnUiThread(() -> UnityAds.show(activity, placementId, new UnityAdsShowOptions(), this));
    }

    @Override
    public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
        Log.d(TAG, String.format("onUnityAdsFailedToLoad %s: %s", placementId, message));
        finishShowWithError(message, error.name());
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
            CallbackContext callback = showCallback;
            showCallback = null;
            if (callback != null) {
                callback.success();
            }
            return;
        }
        if (UnityAds.UnityAdsShowCompletionState.SKIPPED.equals(state)) {
            finishShowWithError("skipped", "SKIPPED");
            return;
        }
        finishShowWithError("unknown", "UNKNOWN");
    }

    @Override
    public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
        Log.d(TAG, String.format("onUnityAdsShowFailure %s: %s", placementId, message));
        finishShowWithError(message, error.name());
    }

    // MARK: - Helpers

    // JSONArray.getString/optString turn a JS null/undefined into the string "null", treat those as missing
    private static String getStringArgument(JSONArray args, int index) {
        if (args.isNull(index)) {
            return null;
        }
        String value = args.optString(index, "").trim();
        return value.isEmpty() ? null : value;
    }

    private void finishShowWithError(String message, String error) {
        CallbackContext callback = showCallback;
        showCallback = null;
        if (callback != null) {
            callback.error(getErrorMessage(message, error));
        }
    }
}
