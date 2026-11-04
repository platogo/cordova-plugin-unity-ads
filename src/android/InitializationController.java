package com.platogo.cordova.unityads;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes the Unity Ads SDK and answers the initialize calls.
 *
 * The app can call initialize again while the first call is still running (bootup init and
 * a "watch ad" tap right after). The SDK is only asked once, every waiting call gets the same result.
 *
 * Only this class talks to the SDK initialization API, so the Unity Ads 5.0.0 migration
 * (InitializationConfiguration + InitializationListener) stays contained here.
 */
final class InitializationController implements IUnityAdsInitializationListener {
    private static final String TAG = "UnityAds";

    private final CordovaInterface cordova;

    // Guarded by this: initialize() runs on the WebCore thread, the SDK calls back on other threads.
    private final List<CallbackContext> pendingCallbacks = new ArrayList<>();
    private boolean initializing = false;

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

        synchronized (this) {
            pendingCallbacks.add(callbackContext);
            if (initializing) {
                // The SDK is already initializing, this call is answered together with the first one
                return;
            }
            initializing = true;
        }

        // UnityAds.initialize does its setup synchronously and can block for hundreds of ms,
        // run it off the WebCore thread as recommended by the Cordova Android plugin guide
        cordova.getThreadPool().execute(() -> {
            applyLogLevel(debugMode);
            UnityAds.initialize(cordova.getContext().getApplicationContext(), gameId, testMode, this);
        });
    }

    // The WebView navigated or reloaded, nobody is listening to the old callbacks anymore.
    // A running initialization is kept, so the SDK is not initialized a second time.
    synchronized void reset() {
        pendingCallbacks.clear();
    }

    // MARK: - IUnityAdsInitializationListener

    @Override
    public void onInitializationComplete() {
        for (CallbackContext callback : takePendingCallbacks()) {
            callback.success();
        }
    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
        Log.w(TAG, "onInitializationFailed: " + message);
        String payload = PluginErrors.payload(message, PluginErrors.code(error));
        for (CallbackContext callback : takePendingCallbacks()) {
            callback.error(payload);
        }
    }

    // MARK: - Helpers

    // Ends the running initialization, a failed one can be retried by the next initialize call
    private synchronized List<CallbackContext> takePendingCallbacks() {
        List<CallbackContext> callbacks = new ArrayList<>(pendingCallbacks);
        pendingCallbacks.clear();
        initializing = false;
        return callbacks;
    }

    // Unity Ads 5.0.0 replaces this with InitializationConfiguration.withLogLevel
    private static void applyLogLevel(boolean debugMode) {
        UnityAds.setDebugMode(debugMode);
    }
}
