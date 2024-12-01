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
 * to the scanner. It also checks whether the event is valid or not.
 * 
 * @author Angela Dakay (angelcache)
 */
public class CaptureAct extends CaptureActivity {
    private static final String TAG = "CaptureAct";
    private DecoratedBarcodeView barcodeScannerView;

    /**
     * Gets the barcode Results: returns eventID when finished and handles when it's an invalid
     * QR Code (Puts out a Toast message)
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
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

    /**
     * Checks if result is a valid Event ID
     * @param scannedText the scanned result which will be checked if it follows Event ID format
     * @return whether or not scannedText is a valid Event ID
     */
    private boolean isValidEventID(String scannedText) {
        // Using regex to mach format [Event name]-[Event Unique ID]
        String regex = "^[A-Za-z0-9_]+-[a-f0-9]{16}$";

        // Return true if scanned barcode matches regex, false otherwise
        return scannedText != null && scannedText.matches(regex);
    }

    /**
     * Resumes the camera preview
     */
    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
    }

    /**
     * Pause the camera preview
     */
    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }
}
