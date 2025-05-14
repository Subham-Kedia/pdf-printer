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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.model.Types;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.IppInputStream;
import com.hp.jipp.encoding.IppOutputStream;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;

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

            // Step 2: Prepare IPP packet for print job
            URI printerUri = new URI(printerUrl);
            
            // Create the IPP packet directly with operation attributes
            // Operation code 0x0002 is the standard IPP code for Print-Job operation
            IppPacket.Builder builder = new IppPacket.Builder(0x0002, 1); // Print job operation with ID=1
            
            // Add operation attributes
            // Use standard IPP attribute names and values
            // These are the IPP attribute names according to RFC 8011
            builder.putOperationAttributes(
                    Types.requestingUserName.of("AndroidKiosk"),  // "requesting-user-name"
                    Types.documentFormat.of("application/pdf"),   // "document-format"
                    Types.media.of(paperType),                    // "media"
                    Types.orientationRequested.of(layout.equalsIgnoreCase("landscape") ? 4 : 3), // "orientation-requested"
                    Types.copies.of(1)                           // "copies"
            );

            // Build the IPP packet for print-job operation
            IppPacket printJobPacket = builder.build();
            
            // Step 3: Send IPP request directly using HTTP
            // Convert IPP URL to HTTP URL if needed
            String uriString = printerUri.toString();
            if (uriString.startsWith("ipp://")) {
                uriString = "http://" + uriString.substring(6); // Convert ipp:// to http://
            } else if (uriString.startsWith("ipps://")) {
                uriString = "https://" + uriString.substring(7); // Convert ipps:// to https://
            }
            
            // Create HTTP connection
            URL httpUrl = new URL(uriString);
            HttpURLConnection http = (HttpURLConnection) httpUrl.openConnection();
            http.setDoOutput(true);
            http.setDoInput(true);
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/ipp");
            
            // Write IPP packet to output stream
            try (DataOutputStream out = new DataOutputStream(http.getOutputStream())) {
                // First write the IPP packet
                ByteArrayOutputStream packetBytes = new ByteArrayOutputStream();
                new IppOutputStream(packetBytes).write(printJobPacket);
                out.write(packetBytes.toByteArray());
                
                // Then write the PDF data
                byte[] buffer1 = new byte[8192];
                int bytesRead;
                while ((bytesRead = pdfStream.read(buffer1)) != -1) {
                    out.write(buffer1, 0, bytesRead);
                }
            }
            
            // Read response
            int statusCode = http.getResponseCode();
            IppPacket responsePacket = null;
            if (statusCode < 400) {
                // Read response packet
                try (InputStream in = new BufferedInputStream(http.getInputStream())) {
                    responsePacket = new IppInputStream(in).readPacket();
                }
            }

            IppPacket finalResponsePacket = responsePacket;
            getActivity().runOnUiThread(() -> {
                if (statusCode < 300 && finalResponsePacket != null && finalResponsePacket.getCode() < 0x0200) {
                    // Both HTTP status code and IPP status code indicate success
                    call.resolve();
                } else { 
                    // Either HTTP or IPP operation failed
                    String errorMsg = "Printing failed: HTTP status " + statusCode;
                    if (finalResponsePacket != null) {
                        errorMsg += ", IPP status code: " + finalResponsePacket.getCode();
                    }
                    call.reject(errorMsg);
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
