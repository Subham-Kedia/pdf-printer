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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import java.util.HashMap;

public class PdfPrinterActivity extends AppCompatActivity {

    private String pdfUrl;
    // private static final String ACTION_USB_PERMISSION = "com.vegrow.plugins.pdfprinter.USB_PERMISSION";
    // private UsbManager usbManager;
    // private UsbEndpoint endpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        pdfUrl = getIntent().getStringExtra("pdf_url");
        String paper_type = getIntent().getStringExtra("paper_type"); // ISO_A4 or ISO_A5

        if (pdfUrl == null || !URLUtil.isValidUrl(pdfUrl)) {
            Toast.makeText(this, "Invalid PDF URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new Thread(() -> {
            try {
                File pdfFile = downloadPdf(pdfUrl);
                runOnUiThread(() -> printPdfFile(pdfFile, paper_type));
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

    private void printPdfFile(File file, String paper_type) {
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
//                    finish(); // Close activity after writing
                }
            }
        };

        PrintAttributes.MediaSize mediaSize = Objects.equals(paper_type, "ISO_A5") ?
        PrintAttributes.MediaSize.ISO_A5 :
        PrintAttributes.MediaSize.ISO_A4;

        PrintAttributes.Builder builder = new PrintAttributes.Builder()
                .setMediaSize(mediaSize)
                .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS);
        printManager.print("PDF Document", adapter, null);
    }

    // private void printPdfFile(File file) {
    //     Log.d("USB", "Printing method called");
    //     UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    //     HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

    //     for (UsbDevice device : deviceList.values()) {
    //         int vendorId = device.getVendorId();
    //         int productId = device.getProductId();

    //         if (vendorId == 1008) {
    //             if (productId == 61994) {
    //                 if (usbManager.hasPermission(device)) {
    //                     UsbDeviceConnection connection = usbManager.openDevice(device);
    //                     connection.claimInterface(device.getInterface(0), true);
    //                     Bitmap bitmap = renderPdfToBitmap(file, 2100, 2970);
    //                     sendBitmapToPrinter(connection, device.getInterface(0), bitmap);
    //                 }
    //             } else if (productId == 0x1785) {
    //                 if (usbManager.hasPermission(device)) {
    //                     UsbDeviceConnection connection = usbManager.openDevice(device);
    //                     connection.claimInterface(device.getInterface(0), true);
    //                     Bitmap bitmap = renderPdfToBitmap(file, 1480, 2100);
    //                     sendBitmapToPrinter(connection, device.getInterface(0), bitmap);
    //                 }
    //             }
    //         }
    //     }
    // }


    // private Bitmap renderPdfToBitmap(File file, int width, int height) {
    //     try {
    //         ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    //         PdfRenderer renderer = new PdfRenderer(fileDescriptor);
    //         PdfRenderer.Page page = renderer.openPage(0);

    //         Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    //         Matrix matrix = new Matrix();
    //         float scaleX = (float) width / page.getWidth();
    //         float scaleY = (float) height / page.getHeight();
    //         matrix.setScale(scaleX, scaleY);

    //         page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);

    //         page.close();
    //         renderer.close();
    //         fileDescriptor.close();

    //         return bitmap;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return null;
    //     }
    // }


    // private void sendBitmapToPrinter(UsbDeviceConnection connection, UsbInterface usbInterface, Bitmap bitmap) {
    //     if (bitmap == null) {
    //         Log.e("USB", "Bitmap is null");
    //         return;
    //     }
    //     try {
    //         int width = bitmap.getWidth();
    //         int height = bitmap.getHeight();

    //         UsbEndpoint endpoint = usbInterface.getEndpoint(0);
            
    //         for (int y = 0; y < height; y++) {
    //             byte[] row = new byte[width];

    //             for (int x = 0; x < width; x++) {
    //                 int pixel = bitmap.getPixel(x, y);
    //                 int r = (pixel >> 16) & 0xFF;
    //                 int g = (pixel >> 8) & 0xFF;
    //                 int b = pixel & 0xFF;
    //                 int gray = (r + g + b) / 3;
    //                 row[x] = (byte) (gray < 128 ? 0x00 : 0xFF);
    //             }

    //             connection.bulkTransfer(endpoint, row, row.length, 1000);
    //         }

    //         connection.releaseInterface(usbInterface);
    //         connection.close();
    //         Log.d("USB", "Bitmap sent to printer.");
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         Log.e("USB", "Failed to send bitmap to printer: " + e.getMessage());
    //     }
    // }


    // private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
    //     public void onReceive(Context context, Intent intent) {
    //         if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
    //             synchronized (this) {
    //                 UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
    //                 if (device == null) {
    //                     Log.e("USB", "Received permission result but device is null.");
    //                     return;
    //                 }

    //                 if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
    //                     Log.d("USB", "Permission granted for device: " + device.getDeviceName());
    //                     // TODO: Proceed to communicate with device
    //                 } else {
    //                     Log.d("USB", "Permission denied for device: " + device.getDeviceName());
    //                 }
    //             }
    //         }
    //     }
    // };

//    private void connectToUsbPrinter(UsbDevice device) {
//        UsbInterface usbInterface = device.getInterface(0);
//        endpoint = usbInterface.getEndpoint(0);

//        UsbDeviceConnection connection = usbManager.openDevice(device);
//        if (connection != null && connection.claimInterface(usbInterface, true)) {
//            Log.d("USB", "Connection established with USB printer");

//            // Example: Send raw data
//            byte[] data = "Hello Printer\n".getBytes();
//            int result = connection.bulkTransfer(endpoint, data, data.length, 1000);
//            Log.d("USB", "Bytes sent: " + result);

//            // Always release resources
//            connection.releaseInterface(usbInterface);
//            connection.close();
//        } else {
//            Log.d("USB", "Failed to connect to USB printer");
//        }
//    }
}
