package com.vegrow.plugins.pdfprinter;

import android.content.Intent;
import android.webkit.URLUtil;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.model.Types;
import com.hp.jipp.model.IppRequest;
import com.hp.jipp.trans.IppClient;
import com.hp.jipp.trans.IppResponse;

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
        String paperInput = call.getString("paperType", "iso_a4_210x297mm");
        String layout = call.getString("layout", "portrait");

        String paperType;
        if ("ISO_A4".equalsIgnoreCase(paperInput)) {
            paperType = "iso_a4_210x297mm";
        } else if ("A5".equalsIgnoreCase(paperInput)) {
            paperType = "iso_a5_148x210mm";
        } else {
            // Default to A4 if unknown
            paperType = "iso_a4_210x297mm";
        }

        if (printerUrl == null || pdfUrl == null) {
            call.reject("Must provide printer URL and PDF URL");
            return;
        }

        if (!URLUtil.isValidUrl(pdfUrl)) {
            call.reject("Invalid PDF URL");
            return;
        }

        new Thread(() -> {
        try {
            // Step 1: Download the PDF
            URL url = new URL(pdfUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream inputStream = new BufferedInputStream(conn.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            inputStream.close();
            byte[] pdfBytes = baos.toByteArray();
            InputStream pdfStream = new ByteArrayInputStream(pdfBytes);

            // Step 2: Prepare IPP client
            URI printerUri = new URI(printerUrl);
            IppClient client = new IppClient.Builder()
                    .uri(printerUri)
                    .build();

            // Step 3: Create IPP print job
            IppRequest request = IppRequest.printJob(printerUri.toString(), pdfStream)
                    .attribute(Types.requestingUserName.of("AndroidKiosk"))
                    .attribute(Types.documentFormat.of("application/pdf"))
                    .attribute(Types.media.of(paperType)) // e.g. "iso_a4_210x297mm"
                    .attribute(Types.orientationRequested.of(
                            layout.equalsIgnoreCase("landscape")
                                    ? Types.orientationLandscape
                                    : Types.orientationPortrait
                    ))
                    .attribute(Types.copies.of(1))
                    .build();

            // Step 4: Send request
            IppResponse response = client.send(request);

            getActivity().runOnUiThread(() -> {
                if (response.getStatus().isSuccessful()) {
                    call.resolve();
                } else {
                    call.reject("IPP printing failed: " + response.getStatus().toString());
                }
            });
        } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    call.reject("Printing failed: " + e.getMessage(), e);
                });
            }
        }).start();
    }
}
