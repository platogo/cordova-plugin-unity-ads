package com.platogo.cordova.unityads;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.metadata.PlayerMetaData;
import com.unity3d.ads.UnityAdsShowOptions;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class UnityAdsPlugin extends CordovaPlugin {
    private CallbackContext callbackID;
    private static final String TAG = "UnityAds";

    private AdsListener adsListener = new AdsListener();

    public static String[] getVideoAdsParameters(JSONArray args) {
        try {
            String serverId = args.getString(0);
            String placementId = args.getString(1);
            return new String[]{ serverId, placementId };
        } catch (JSONException e) {
            Log.w(TAG, "get getVideoAdsParameters failed" + e.getMessage());
            return null;
        }
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        callbackID = callbackContext;
        if ("initialize".equals(action)) {
            adsListener.initialize(args, callbackContext);
            return true;
        } else if ("show".equals(action)) {
            String[] videoAdsParameters = getVideoAdsParameters(args);
            if (UnityAds.isInitialized() && videoAdsParameters != null) {
                adsListener.show(videoAdsParameters);
            }
            return true;
        }
        return false; // Returning false results in a "MethodNotFound" error.
    }

    /*
     * Returns application context
     */
    private Context getApplicationContext() {
        return this.getApplicationActivity().getApplicationContext();
    }

    /*
     * Returns application activity
     */
    private Activity getApplicationActivity() {
        return this.cordova.getActivity();
    }

    private class AdsListener implements IUnityAdsInitializationListener {
        private IUnityAdsShowListener showListener = new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                Log.d(TAG, String.format("videoAdPlacementId: %s %s", placementId, "onUnityAdsError"));
                Log.d(TAG, String.format("%s", message));
                callbackID.error(String.format("[\"%s\",\"%s\"]", message, error.name()));
            }

            @Override
            public void onUnityAdsShowStart(String placementId) {
                Log.v("UnityAdsExample", "onUnityAdsShowStart: " + placementId);
            }

            @Override
            public void onUnityAdsShowClick(String placementId) {
                Log.v(TAG, "CLICKED" + placementId);
                callbackID.success();
            }

            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                    callbackID.success();
                } else if (state == UnityAds.UnityAdsShowCompletionState.SKIPPED) {
                    callbackID.error("VIDEO_SKIPPED");
                } else {
                    callbackID.error("DID FINISH WITH ERROR");
                }
            }
        };

        private IUnityAdsLoadListener adsLoadListener = new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String videoAdPlacementId) {
                UnityAds.show(getApplicationActivity(), videoAdPlacementId, new UnityAdsShowOptions(), showListener);
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                Log.d(TAG, String.format("videoAdPlacementId: %s %s", placementId, "onUnityAdsFailedToLoad"));
                Log.d(TAG, String.format("%s", message));
                callbackID.error(String.format("[\"%s\",\"%s\"]", message, error.name()));
            }
        };

        public void show(String[] videoAdParameters) {
            String serverId = videoAdParameters[0];
            if (serverId != null) {
                PlayerMetaData playerMetaData = new PlayerMetaData(getApplicationContext());
                playerMetaData.setServerId(serverId);
                playerMetaData.commit();
            } else {
                Log.w(TAG, "serverId arg missing!");
            }

            String videoAdPlacementId =  videoAdParameters[1];
            UnityAds.load(videoAdPlacementId, adsLoadListener);
        }

        private void initialize(JSONArray args, CallbackContext callbackContext) {
            callbackID = callbackContext;
            String gameId;
            boolean testMode = false;
            boolean debugMode = false;

            try {
                gameId = args.getString(0);
            } catch (JSONException e) {
                callbackContext.error("Invalid Game ID");
                return;
            }

            try {
                testMode = args.getBoolean(1);
            } catch (JSONException e) {
                Log.w(TAG, "Warning: Test mode not set");
            }

            try {
                debugMode = args.getBoolean(2);
            } catch (JSONException e) {
                Log.w(TAG, "Warning: Debug mode not set");
            }

            if (gameId == "null") {
                callbackContext.error("Invalid Game ID");
                return;
            }

            UnityAds.setDebugMode(debugMode);
            UnityAds.initialize(getApplicationContext(), gameId, testMode, this);
        }

        @Override
        public void onInitializationComplete() {
            callbackID.success();
        }

        @Override
        public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
            Log.w(TAG, "onInitializationFailed" + message);
            callbackID.error(String.format("[\"%s\",\"%s\"]", message, error.name()));
        }
    }
}
