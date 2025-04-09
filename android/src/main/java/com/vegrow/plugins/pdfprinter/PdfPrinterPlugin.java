package com.vegrow.plugins.pdfprinter;

import android.content.Intent;
import android.webkit.URLUtil;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "PdfPrinter")
public class PdfPrinterPlugin extends Plugin {

    @PluginMethod
    public void printPDF(PluginCall call) {
        String url = call.getString("url");
        if (url == null || !URLUtil.isValidUrl(url)) {
            call.reject("Invalid or missing PDF URL");
            return;
        }

        Intent intent = new Intent(getContext(), PdfPrinterActivity.class);
        intent.putExtra("pdf_url", url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);

        call.resolve();
    }
}
