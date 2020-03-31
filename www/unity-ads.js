var exec = require('cordova/exec');

function UnityAds() { }

UnityAds.prototype.initialize = (gameId, testMode, debugMode) => {
    return new Promise((resolve, reject) => {
        exec(resolve, reject, 'UnityAdsPlugin', 'initialize', [gameId, testMode, debugMode]);
    })
}

UnityAds.prototype.show = (serverId, videoAdPlacementId) => {
    return new Promise((resolve, reject) => {
        exec(resolve, reject, 'UnityAdsPlugin', 'show', [serverId, videoAdPlacementId]);
    })
}

module.exports = new UnityAds();
