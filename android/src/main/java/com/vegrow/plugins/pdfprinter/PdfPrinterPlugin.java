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
        String contentType = call.getString("contentType");
        String content = call.getString("content");
        String paperType = call.getString("paperType");
        String layout = call.getString("layout")

        if (contentType == null || content == null) {
            call.reject("Must provide contentType and content");
            return;
        }
        
        Intent intent = new Intent(getContext(), PdfPrinterActivity.class);
        intent.putExtra("content", content);
        intent.putExtra("contentType", contentType);
        intent.putExtra("paperType", paperType);
        intent.putExtra("layout", layout)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);

        call.resolve();
    }
}
