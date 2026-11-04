package com.platogo.cordova.unityads;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;

/**
 * Cordova entry point. Reads the JS arguments and hands each action to its controller,
 * the controllers own all Unity Ads SDK calls and their callback state.
 */
public class UnityAdsPlugin extends CordovaPlugin {
    private static final String ACTION_INITIALIZE = "initialize";
    private static final String ACTION_SHOW = "show";

    private InitializationController initializationController;
    private AdShowController adShowController;

    @Override
    protected void pluginInitialize() {
        initializationController = new InitializationController(cordova);
        adShowController = new AdShowController(cordova);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (ACTION_INITIALIZE.equals(action)) {
            initializationController.initialize(
                    PluginArgs.getString(args, 0),
                    PluginArgs.getBoolean(args, 1),
                    PluginArgs.getBoolean(args, 2),
                    callbackContext);
            return true;
        }
        if (ACTION_SHOW.equals(action)) {
            adShowController.show(
                    PluginArgs.getString(args, 0),
                    PluginArgs.getString(args, 1),
                    callbackContext);
            return true;
        }
        return false; // Returning false results in a "MethodNotFound" error.
    }

    @Override
    public void onReset() {
        // The WebView navigated or reloaded, nobody is listening to the old callbacks anymore
        initializationController.reset();
        adShowController.reset();
    }
}
