var exec = require('cordova/exec');

function UnityAds() { }

UnityAds.prototype.initialize = (gameId) => {
    return new Promise((resolve, reject) => {
        exec(resolve, reject, 'UnityAdsPlugin', 'initialize', [gameId]);
    })
}

UnityAds.prototype.show = () => {
    return new Promise((resolve, reject) => {
        exec(resolve, reject, 'UnityAdsPlugin', 'show', []);
    })
}

module.exports = new UnityAds();
