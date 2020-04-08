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
    private boolean isInitialized = false;


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
                String videoAdPlacementId = args.getString(1);

                if (serverId != "null") {
                    PlayerMetaData playerMetaData = new PlayerMetaData(getApplicationContext());
                    playerMetaData.setServerId(serverId);
                    playerMetaData.commit();
                }


                callbackID = callbackContext;
                if (videoAdPlacementId != "null") {
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
            if (!isInitialized) {
                isInitialized = true;
                callbackID.success();
            }
        }

        @Override
        public void onUnityAdsStart(String s) {

        }

        @Override
        public void onUnityAdsFinish(String s, UnityAds.FinishState finishState) {
            if (finishState == UnityAds.FinishState.COMPLETED) {
                callbackID.success();
            } else if (finishState == UnityAds.FinishState.SKIPPED) {
                callbackID.error("VIDEO_SKIPPED");
            } else {
                callbackID.error("DID FINISH WITH ERROR");
            }
        }

        @Override
        public void onUnityAdsError(UnityAds.UnityAdsError error, String message) {
            String msg;

            Log.d(TAG, String.format("%s", "onUnityAdsError"));
            Log.d(TAG, String.format("%s", message));

            if(error == UnityAds.UnityAdsError.NOT_INITIALIZED){
                msg = String.format("[\"%s\",\"%s\"]", message, "NOT_INITIALIZED");
            }else if(error == UnityAds.UnityAdsError.INITIALIZE_FAILED){
                msg = String.format("[\"%s\",\"%s\"]", message, "INITIALIZE_FAILED");
            }else{
                msg = String.format("[\"%s\",\"%s\"]", message, "INTERNAL_ERROR");
            }

            callbackID.error(msg);
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

