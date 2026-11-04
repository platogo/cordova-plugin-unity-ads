package com.platogo.cordova.unityads;

import org.json.JSONArray;

/** Reads the arguments passed from JS through cordova.exec. */
final class PluginArgs {
    private PluginArgs() {}

    // JSONArray.getString/optString turn a JS null/undefined into the string "null", treat those as missing
    static String getString(JSONArray args, int index) {
        if (args.isNull(index)) {
            return null;
        }
        String value = args.optString(index, "").trim();
        return value.isEmpty() ? null : value;
    }

    static boolean getBoolean(JSONArray args, int index) {
        return args.optBoolean(index, false);
    }
}
