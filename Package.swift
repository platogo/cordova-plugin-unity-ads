// swift-tools-version:5.9

import PackageDescription

let UnityAdsVersion: Version = "4.19.0"

let package = Package(
    name: "cordova-plugin-unity-ads",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "cordova-plugin-unity-ads",
            targets: ["cordova-plugin-unity-ads"]
        )
    ],
    dependencies: [
        .package(url: "https://github.com/apache/cordova-ios.git", branch: "master"),
        .package(url: "https://github.com/Unity-Technologies/Unity-Ads-Swift-Package.git", exact: UnityAdsVersion)
    ],
    targets: [
        .target(
            name: "cordova-plugin-unity-ads",
            dependencies: [
                .product(name: "Cordova", package: "cordova-ios"),
                .product(name: "UnityAds", package: "Unity-Ads-Swift-Package")
            ],
            path: "src/ios",
            publicHeadersPath: "."
        )
    ]
)