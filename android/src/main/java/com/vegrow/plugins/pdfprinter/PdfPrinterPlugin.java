package com.vegrow.plugins.pdfprinter;

import android.content.Intent;
import android.webkit.URLUtil;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.ByteArrayInputStream;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

@CapacitorPlugin(name = "PdfPrinter")
public class PdfPrinterPlugin extends Plugin {

    @PluginMethod
    public void printPDF(PluginCall call) {
        String contentType = call.getString("contentType");
        String content = call.getString("content");
        String paperType = call.getString("paperType");
        String layout = call.getString("layout");

        if (contentType == null || content == null) {
            call.reject("Must provide contentType and content");
            return;
        }
        
        Intent intent = new Intent(getContext(), PdfPrinterActivity.class);
        intent.putExtra("content", content);
        intent.putExtra("contentType", contentType);
        intent.putExtra("paperType", paperType);
        intent.putExtra("layout", layout);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);

        call.resolve();
    }

    @PluginMethod
    public void printPDFviaIPP(PluginCall call) {
        String printerUrl = call.getString("printerUrl");
        String pdfUrl = call.getString("content");
        String paperType = call.getString("paperType", "ISO_A4");
        String layout = call.getString("layout", "portrait");

        if (printerUrl == null || pdfUrl == null) {
            call.reject("Must provide printer URL and PDF URL");
            return;
        }

        if (!URLUtil.isValidUrl(pdfUrl)) {
            call.reject("Invalid PDF URL");
            return;
        }

        try {
            // Download PDF content
            URL url = new URL(pdfUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // Create print request attributes
            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            
            // Set paper size
            if (paperType.equals("ISO_A5")) {
                attributes.add(MediaSizeName.ISO_A5);
            } else {
                attributes.add(MediaSizeName.ISO_A4);
            }

            // Set orientation
            if (layout.equals("landscape")) {
                attributes.add(OrientationRequested.LANDSCAPE);
            } else {
                attributes.add(OrientationRequested.PORTRAIT);
            }

            // Set number of copies
            attributes.add(new Copies(1));

            // Find printer service
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            PrintService selectedPrinter = null;

            for (PrintService service : services) {
                if (service.getName().contains(new URI(printerUrl).getHost())) {
                    selectedPrinter = service;
                    break;
                }
            }

            if (selectedPrinter == null) {
                call.reject("Printer not found: " + printerUrl);
                return;
            }

            // Create print job with downloaded PDF
            DocPrintJob job = selectedPrinter.createPrintJob();
            try (InputStream pdfStream = connection.getInputStream()) {
                Doc doc = new SimpleDoc(pdfStream, DocFlavor.INPUT_STREAM.PDF, null);
                job.print(doc, attributes);
            }

            call.resolve();

        } catch (Exception e) {
            call.reject("Printing failed: " + e.getMessage());
        }
    }
}
