// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "PdfPrinter",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "PdfPrinter",
            targets: ["PdfPrinterPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "PdfPrinterPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/PdfPrinterPlugin"),
        .testTarget(
            name: "PdfPrinterPluginTests",
            dependencies: ["PdfPrinterPlugin"],
            path: "ios/Tests/PdfPrinterPluginTests")
    ]
)