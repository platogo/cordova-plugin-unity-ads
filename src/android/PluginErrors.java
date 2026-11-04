package com.platogo.cordova.unityads;

import org.json.JSONArray;

/**
 * Owns the error codes the app relies on (UnityAdsPluginError in the app reads them).
 *
 * SDK error enums are mapped to codes here and nowhere else. Unity Ads 5.0.0 replaces the
 * per-operation enums with UnityAdsError (numeric code + message), migrating then only means
 * mapping those numeric codes to the same strings in this class.
 */
final class PluginErrors {
    // Codes set by the plugin itself, the SDK does not report these
    static final String GAME_ID_MISSING = "GAME_ID_MISSING";
    static final String PLACEMENT_ID_MISSING = "PLACEMENT_ID_MISSING";
    static final String NOT_INITIALIZED = "NOT_INITIALIZED";
    static final String ACTIVITY_MISSING = "ACTIVITY_MISSING";
    static final String SKIPPED = "SKIPPED";
    static final String UNKNOWN = "UNKNOWN";

    private PluginErrors() {}

    // Maps a (deprecated) UnityAds error enum to the code sent to the app.
    // The enum names already match the codes the app expects.
    static String code(Enum<?> sdkError) {
        return sdkError == null ? UNKNOWN : sdkError.name();
    }

    static String payload(String message, String code) {
        // The message comes from the Unity Ads SDK and can contain characters that are not
        // valid inside a JSON string literal, most notably newlines.
        // We see this in Sentry errors coming from Unity containing "errorEnum":"JSON_PARSE_FAILED"
        // Building the array with JSONArray escapes them, so the client can parse it and read the error enum.
        JSONArray errorMessage = new JSONArray();
        errorMessage.put(message);
        errorMessage.put(code);

        return errorMessage.toString();
    }
}
