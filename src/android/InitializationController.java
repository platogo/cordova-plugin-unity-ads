package com.platogo.cordova.unityads;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;

import android.util.Log;

/**
 * Initializes the Unity Ads SDK and answers the initialize calls.
 *
 * Only this class talks to the SDK initialization API, so the Unity Ads 5.0.0 migration
 * (InitializationConfiguration + InitializationListener) stays contained here.
 */
final class InitializationController implements IUnityAdsInitializationListener {
    private static final String TAG = "UnityAds";

    private final CordovaInterface cordova;
    // volatile: initialize() runs on the WebCore thread, the SDK calls back on other threads.
    private volatile CallbackContext callback;

    InitializationController(CordovaInterface cordova) {
        this.cordova = cordova;
    }

    void initialize(String gameId, boolean testMode, boolean debugMode, CallbackContext callbackContext) {
        if (UnityAds.isInitialized()) {
            callbackContext.success();
            return;
        }
        if (gameId == null) {
            callbackContext.error(PluginErrors.payload("Game ID missing", PluginErrors.GAME_ID_MISSING));
            return;
        }

        callback = callbackContext;
        // UnityAds.initialize does its setup synchronously and can block for hundreds of ms,
        // run it off the WebCore thread as recommended by the Cordova Android plugin guide
        cordova.getThreadPool().execute(() -> {
            applyLogLevel(debugMode);
            UnityAds.initialize(cordova.getContext().getApplicationContext(), gameId, testMode, this);
        });
    }

    // The WebView navigated or reloaded, nobody is listening to the old callback anymore
    void reset() {
        callback = null;
    }

    // MARK: - IUnityAdsInitializationListener

    @Override
    public void onInitializationComplete() {
        CallbackContext current = callback;
        callback = null;
        if (current != null) {
            current.success();
        }
    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
        Log.w(TAG, "onInitializationFailed: " + message);
        CallbackContext current = callback;
        callback = null;
        if (current != null) {
            current.error(PluginErrors.payload(message, PluginErrors.code(error)));
        }
    }

    // MARK: - Helpers

    // Unity Ads 5.0.0 replaces this with InitializationConfiguration.withLogLevel
    private static void applyLogLevel(boolean debugMode) {
        UnityAds.setDebugMode(debugMode);
    }
}
