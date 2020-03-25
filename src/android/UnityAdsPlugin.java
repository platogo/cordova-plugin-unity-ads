package com.platogo.cordova.unityads;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.mediation.IUnityAdsExtendedListener;
import com.unity3d.ads.metadata.PlayerMetaData;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.content.Context;

public class UnityAdsPlugin extends CordovaPlugin {
    private CallbackContext initializeCallback;
    private CallbackContext showCallback;


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        // your init code here
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("initialize".equals(action)) {
            showCallback = callbackContext; // TODO: needed to ensure callback is set in onUnityAdsError -> think about better solution
            new AdsListener().initialize(args, callbackContext);
            return true;
        } else if ("show".equals(action)) {
            if (UnityAds.isReady()) {
                String serverId = args.getString(0);

                if (serverId != "null") {
                    PlayerMetaData playerMetaData = new PlayerMetaData(getApplicationContext());
                    playerMetaData.setServerId(serverId);
                    playerMetaData.commit();
                }


                showCallback = callbackContext;
                UnityAds.show(cordova.getActivity());
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
            initializeCallback = callbackContext;
            String gameId;

            try{
                gameId = args.getString(0);
            }catch(JSONException e){
                callbackContext.error("Invalid Game ID");
                return;
            }

            if (gameId == "null") {
                callbackContext.error("Invalid Game ID");
                return;
            }

            UnityAds.initialize(cordova.getActivity(), gameId, this);
        }

        @Override
        public void onUnityAdsReady(String s) {
            initializeCallback.success();
        }

        @Override
        public void onUnityAdsStart(String s) {

        }

        @Override
        public void onUnityAdsFinish(String s, UnityAds.FinishState finishState) {
            if (finishState != UnityAds.FinishState.SKIPPED) {
                showCallback.success();
            } else {
                showCallback.error("VIDEO_SKIPPED");
            }
        }

        @Override
        public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {
            showCallback.error(s);
        }

        @Override
        public void onUnityAdsClick(String s) {
            showCallback.success();
        }

        @Override
        public void onUnityAdsPlacementStateChanged(String s, UnityAds.PlacementState placementState, UnityAds.PlacementState placementState1) {

        }
    }
}

