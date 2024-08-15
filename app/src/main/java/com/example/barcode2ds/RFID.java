package com.example.barcode2ds;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

public class RFID {
    private static final String TAG = "RFID";
    private RFIDWithUHFUART mReader;
    private Button scanButton;
    private Context context;
    private OnRFIDScannedListener listener;

    public interface OnRFIDScannedListener {
        void onRFIDScanned(String rfidCode);
    }

    public void setOnRFIDScannedListener(OnRFIDScannedListener listener) {
        this.listener = listener;
    }

    public RFID(Context context, Button scanButton) {
        this.context = context;
        this.scanButton = scanButton;
        initUHF();

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
    }

    private void initUHF() {
        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mReader != null) {
            new InitTask().execute();
        }
    }

    private class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog progressDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            return mReader.init();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (!result) {
                Toast.makeText(context, "RFID init failed", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Initializing...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    public void startScan() {
        if (mReader != null) {
            new ScanTask().execute();
        } else {
            Toast.makeText(context, "RFID reader not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    private class ScanTask extends AsyncTask<Void, Void, UHFTAGInfo> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Scanning...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected UHFTAGInfo doInBackground(Void... voids) {
            return mReader.inventorySingleTag();
        }

        @Override
        protected void onPostExecute(UHFTAGInfo result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result != null) {
                String scannedRFID = result.getEPC();
                if (scannedRFID.length() >= 4) {
                    scannedRFID = scannedRFID.substring(0, 4);
                    if (listener != null) {
                        listener.onRFIDScanned(scannedRFID);
                    }
                    SETUP.setCurrentRFID(scannedRFID); // Cập nhật giá trị RFID trong SETUP
                } else {
                    Toast.makeText(context, "RFID code too short", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "No tag found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public RFIDWithUHFUART getReader() {
        return mReader;
    }
}