package com.example.butter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

/**
 * This class allows us to customize the scanning functionality and add a back button
 * in AndroidManifest.xml
 *
 * @Author Angela Dakay (angelcache)
 */
public class CaptureAct extends CaptureActivity {
    private static final String TAG = "CaptureAct";
    private DecoratedBarcodeView barcodeScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_scanner);

        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null) {
                    // Barcode successfully scanned
                    Log.d(TAG, "Scanned barcode: " + result.getText());

                    // See if the barcode is valid
                    String scannedText = result.getText();

                    if (isValidEventID(scannedText)) {
                        // Handle the scanned barcode data
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("eventID", result.getText());
                        setResult(RESULT_OK, resultIntent);
                        finish();  // Finish the CaptureAct activity after scanning
                    } else {
                        Toast.makeText(CaptureAct.this, "Scanned an invalid QR Code.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        });
    }

    private boolean isValidEventID(String scannedText) {
        // Using regex to mach format [Event name]-[Event Unique ID]
        String regex = "^[A-Za-z0-9_]+-[a-f0-9]{16}$";

        // Return true if scanned barcode matches regex, false otherwise
        return scannedText != null && scannedText.matches(regex);
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();  // Resume the camera preview
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();  // Pause the camera preview
    }
}
