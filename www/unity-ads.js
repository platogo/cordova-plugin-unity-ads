var exec = require('cordova/exec');

function UnityAds() { }

UnityAds.prototype.initialize = (gameId, testMode) => {
    return new Promise((resolve, reject) => {
        exec(resolve, reject, 'UnityAdsPlugin', 'initialize', [gameId, testMode]);
    })
}

UnityAds.prototype.show = (serverId) => {
    return new Promise((resolve, reject) => {
        exec(resolve, reject, 'UnityAdsPlugin', 'show', [serverId]);
    })
}

module.exports = new UnityAds();
