var exec = require('cordova/exec');

function UnityAds() { }

UnityAds.prototype.initialize = function (gameId, testMode, debugMode) {
    return new Promise(function (resolve, reject) {
        exec(resolve, reject, 'UnityAdsPlugin', 'initialize', [gameId, testMode, debugMode]);
    })
}

UnityAds.prototype.show = function (serverId, videoAdPlacementId) {
    return new Promise(function (resolve, reject) {
        exec(resolve, reject, 'UnityAdsPlugin', 'show', [serverId, videoAdPlacementId]);
    })
}

module.exports = new UnityAds();
