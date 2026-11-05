package com.platogo.cordova.unityads;

import org.json.JSONArray;

import java.util.Optional;

/** Reads the arguments passed from JS through cordova.exec. */
final class PluginArgs {
    private PluginArgs() {}

    // JSONArray.getString/optString turn a JS null/undefined into the string "null", treat those as missing.
    // Blank strings are missing as well.
    static Optional<String> getString(JSONArray args, int index) {
        if (args.isNull(index)) {
            return Optional.empty();
        }
        return Optional.of(args.optString(index, "").trim()).filter(value -> !value.isEmpty());
    }

    static boolean getBoolean(JSONArray args, int index) {
        return args.optBoolean(index, false);
    }
}
