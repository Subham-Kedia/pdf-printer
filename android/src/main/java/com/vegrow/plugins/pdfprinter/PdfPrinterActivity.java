package com.vegrow.plugins.pdfprinter;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PdfPrinterActivity extends AppCompatActivity {

    private String pdfUrl;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pdfUrl = getIntent().getStringExtra("pdf_url");

        if (pdfUrl == null || !URLUtil.isValidUrl(pdfUrl)) {
            Toast.makeText(this, "Invalid PDF URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new Thread(() -> {
            try {
                File pdfFile = downloadPdf(pdfUrl);
                Log.d("PdfPrinterActivity", "PDF downloaded to: " + pdfFile.getAbsolutePath());
                runOnUiThread(() -> printPdfFile(pdfFile));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to download PDF", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private File downloadPdf(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        File file = File.createTempFile("print-", ".pdf", getCacheDir());
        try (InputStream in = connection.getInputStream();
             OutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }

        return file;
    }

    private void printPdfFile(File file) {
        PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);

        PrintDocumentAdapter adapter = new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                                 CancellationSignal cancellationSignal,
                                 LayoutResultCallback callback, Bundle extras) {
                if (cancellationSignal.isCanceled()) {
                    callback.onLayoutCancelled();
                    return;
                }

                PrintDocumentInfo info = new PrintDocumentInfo.Builder("document.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build();

                callback.onLayoutFinished(info, true);
            }

            @Override
            public void onWrite(@NonNull PageRange[] pages, @NonNull ParcelFileDescriptor destination,
                                @NonNull CancellationSignal cancellationSignal,
                                @NonNull WriteResultCallback callback) {
                try (InputStream in = new FileInputStream(file);
                     OutputStream out = new FileOutputStream(destination.getFileDescriptor())) {
                    byte[] buffer = new byte[4096];
                    int size;
                    while ((size = in.read(buffer)) > 0) {
                        out.write(buffer, 0, size);
                    }
                    callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onWriteFailed(e.getMessage());
                } finally {
                    finish();
                }
            }
        };

        PrintAttributes attributes = new PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
            .build();

        printManager.print("PDF Document", adapter, attributes);
    }
}
