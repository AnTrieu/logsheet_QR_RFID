package com.example.barcode2ds;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RFID {
    private static final String TAG = "RFID";
    private RFIDWithUHFUART mReader;
    private Button scanButton;
    private Context context;
    private OnRFIDScannedListener listener;

    public interface OnRFIDScannedListener {
        void onRFIDsScanned(List<String> rfidCodes);
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

    private class ScanTask extends AsyncTask<Void, Void, List<UHFTAGInfo>> {
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
        protected List<UHFTAGInfo> doInBackground(Void... voids) {
            List<UHFTAGInfo> results = new ArrayList<>();
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 3000) {
                UHFTAGInfo result = mReader.inventorySingleTag();
                if (result != null) {
                    results.add(result);
                }
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<UHFTAGInfo> results) {
            super.onPostExecute(results);
            progressDialog.dismiss();
            if (!results.isEmpty()) {
                processScannedRFIDs(results);
            } else {
                Toast.makeText(context, "No tags found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processScannedRFIDs(List<UHFTAGInfo> results) {
        Set<String> uniqueRFIDs = new HashSet<>();
        for (UHFTAGInfo result : results) {
            String scannedRFID = result.getEPC();
            if (scannedRFID.length() >= 4) {
                scannedRFID = scannedRFID.substring(0, 4);
                uniqueRFIDs.add(scannedRFID);
            }
        }

        if (!uniqueRFIDs.isEmpty()) {
            List<String> rfidList = new ArrayList<>(uniqueRFIDs);
            if (listener != null) {
                listener.onRFIDsScanned(rfidList);
            }
            SETUP.setCurrentRFID(rfidList.get(rfidList.size() - 1));
        } else {
            Toast.makeText(context, "No valid RFID codes found", Toast.LENGTH_SHORT).show();
        }
    }
}