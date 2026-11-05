import Foundation
import UnityAds
#if canImport(Cordova)
import Cordova
#endif

// Owns the error codes the app relies on (UnityAdsPluginError in the app reads them).
//
// SDK error enums are mapped to codes here and nowhere else. Unity Ads 5.0.0 replaces the
// per-operation enums with UnityAdsError (numeric code + message), migrating then only means
// mapping those numeric codes to the same strings in this file.
protocol PluginErrorCode {
    var pluginErrorCode: String { get }
}

enum PluginError {
    // Codes set by the plugin itself, the SDK does not report these
    static let gameIdMissing = "GAME_ID_MISSING"
    static let placementIdMissing = "PLACEMENT_ID_MISSING"
    static let serverIdMissing = "SERVER_ID_MISSING"
    static let notInitialized = "NOT_INITIALIZED"
    static let alreadyShowing = "ALREADY_SHOWING"
    // Same code as Android, where the Activity is what presents the ad
    static let activityMissing = "ACTIVITY_MISSING"
    static let skipped = "SKIPPED"
    static let unknown = "UNKNOWN"

    private static let jsonParseFailedPayload = "[\"Error when parsing SDK failure\", \"JSON_PARSE_FAILED\"]"

    static func result(message: String, error: PluginErrorCode) -> CDVPluginResult {
        return result(message: message, code: error.pluginErrorCode)
    }

    static func result(message: String, code: String) -> CDVPluginResult {
        guard let jsonData = try? JSONSerialization.data(withJSONObject: [message, code]),
              let jsonString = String(data: jsonData, encoding: .utf8) else {
            return CDVPluginResult(status: .error, messageAs: jsonParseFailedPayload)
        }
        return CDVPluginResult(status: .error, messageAs: jsonString)
    }
}

// MARK: - SDK error mapping (deprecated with Unity Ads 4.19.0, removed in 5.0.0)

extension UnityAdsInitializationError: PluginErrorCode {
    var pluginErrorCode: String {
        switch self {
        case .initializationErrorAdBlockerDetected:
            return "AD_BLOCKER_DETECTED"
        case .initializationErrorInternalError:
            return "INTERNAL_ERROR"
        case .initializationErrorInvalidArgument:
            return "INVALID_ARGUMENT"
        @unknown default:
            return PluginError.unknown
        }
    }
}

extension UnityAdsLoadError: PluginErrorCode {
    var pluginErrorCode: String {
        switch self {
        case .initializeFailed:
            return "INITIALIZE_FAILED"
        case .internal:
            return "INTERNAL_ERROR"
        case .invalidArgument:
            return "INVALID_ARGUMENT"
        case .noFill:
            return "NO_FILL"
        case .timeout:
            return "TIMEOUT"
        @unknown default:
            return PluginError.unknown
        }
    }
}

extension UnityAdsShowError: PluginErrorCode {
    var pluginErrorCode: String {
        switch self {
        case .showErrorNotInitialized:
            return "NOT_INITIALIZED"
        case .showErrorNotReady:
            return "NOT_READY"
        case .showErrorVideoPlayerError:
            return "VIDEO_PLAYER_ERROR"
        case .showErrorInvalidArgument:
            return "INVALID_ARGUMENT"
        case .showErrorNoConnection:
            return "NO_CONNECTION"
        case .showErrorAlreadyShowing:
            return "ALREADY_SHOWING"
        case .showErrorInternalError:
            return "INTERNAL_ERROR"
        case .showErrorTimeout:
            return "TIMEOUT"
        @unknown default:
            return PluginError.unknown
        }
    }
}
