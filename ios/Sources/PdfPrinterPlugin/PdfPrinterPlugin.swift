import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(PdfPrinterPlugin)
public class PdfPrinterPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "PdfPrinterPlugin"
    public let jsName = "PdfPrinter"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "printPDF", returnType: nil)
    ]
    private let implementation = PdfPrinter()

    @objc func printPDF(_ call: CAPPluginCall) {
        let url = call.getString("url") ?? ""
        implementation.echo(value: url)
        call.resolve(nil)
    }
}
