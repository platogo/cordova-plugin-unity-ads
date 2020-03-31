package com.platogo.cordova.unityads;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.mediation.IUnityAdsExtendedListener;
import com.unity3d.ads.metadata.PlayerMetaData;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class UnityAdsPlugin extends CordovaPlugin {
    private CallbackContext callbackID;
    private static final String TAG = "UnityAds";


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("initialize".equals(action)) {
            callbackID = callbackContext;
            new AdsListener().initialize(args, callbackContext);
            return true;
        } else if ("show".equals(action)) {
            if (UnityAds.isReady()) {
                String serverId = args.getString(0);
                String videoAdPlacementId = null;

                try {
                    videoAdPlacementId = args.getString(1);
                } catch (JSONException e){
                    Log.w(TAG, "Warning: Video Ad Placement id mode not set");
                }


                if (serverId != "null") {
                    PlayerMetaData playerMetaData = new PlayerMetaData(getApplicationContext());
                    playerMetaData.setServerId(serverId);
                    playerMetaData.commit();
                }


                callbackID = callbackContext;
                if (videoAdPlacementId != null) {
                    UnityAds.show(cordova.getActivity(), videoAdPlacementId);
                } else {
                    UnityAds.show(cordova.getActivity());
                }
            }
            return true;
        }
        return false;  // Returning false results in a "MethodNotFound" error.
    }

    /*
     * Returns application context
     */
    private Context getApplicationContext(){
        return this.getApplicationActivity().getApplicationContext();
    }

    /*
     * Returns application context
     */
    private Activity getApplicationActivity(){
        return this.cordova.getActivity();
    }

    private class AdsListener implements IUnityAdsExtendedListener {
        private void initialize(JSONArray args, CallbackContext callbackContext) {
            callbackID = callbackContext;
            if (UnityAds.isInitialized()) {
                callbackID.success();
                return;
            }

            String gameId;
            Boolean testMode = false;
            Boolean debugMode = false;

            try{
                gameId = args.getString(0);
            }catch(JSONException e){
                callbackContext.error("Invalid Game ID");
                return;
            }

            try {
                testMode = args.getBoolean(1);
            } catch (JSONException e){
                Log.w(TAG, "Warning: Test mode not set");
            }

            try {
                debugMode = args.getBoolean(2);
            } catch (JSONException e){
                Log.w(TAG, "Warning: Debug mode not set");
            }

            if (gameId == "null") {
                callbackContext.error("Invalid Game ID");
                return;
            }

            UnityAds.setDebugMode(debugMode);
            UnityAds.initialize(cordova.getActivity(), gameId, this, testMode);
        }

        @Override
        public void onUnityAdsReady(String s) {
            callbackID.success();
        }

        @Override
        public void onUnityAdsStart(String s) {

        }

        @Override
        public void onUnityAdsFinish(String s, UnityAds.FinishState finishState) {
            if (finishState != UnityAds.FinishState.SKIPPED) {
                callbackID.success();
            } else {
                callbackID.error("VIDEO_SKIPPED");
            }
        }

        @Override
        public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {
            callbackID.error(s);
        }

        @Override
        public void onUnityAdsClick(String s) {
            callbackID.success();
        }

        @Override
        public void onUnityAdsPlacementStateChanged(String s, UnityAds.PlacementState placementState, UnityAds.PlacementState placementState1) {

        }
    }
}

