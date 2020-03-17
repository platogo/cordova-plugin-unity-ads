var exec = require('cordova/exec');

function UnityAds() { }

UnityAds.prototype.initialize = (gameId) => {
    return new Promise((resolve, reject) => {
        exec(resolve, reject, 'UnityAdsPlugin', 'initialize', [gameId]);
    })
}

UnityAds.prototype.show = (serverId) => {
    return new Promise((resolve, reject) => {
        exec(resolve, reject, 'UnityAdsPlugin', 'show', [serverId]);
    })
}

module.exports = new UnityAds();
