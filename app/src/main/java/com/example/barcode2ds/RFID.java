/*
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
import java.util.List;

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
                ToastManager.showToast(context, "Khởi tạo RFID không thành công");
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
            ToastManager.showToast(context, "Đầu đọc RFID chưa được khởi tạo");
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
            while (System.currentTimeMillis() - startTime < 1000) {
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
                ToastManager.showToast(context, "Không tìm thấy mã RFID nào");
            }
        }
    }

    private void processScannedRFIDs(List<UHFTAGInfo> results) {
        List<String> uniqueRFIDs = new ArrayList<>();
        for (UHFTAGInfo result : results) {
            String scannedRFID = result.getEPC();
            if (scannedRFID.length() >= 4) {
                scannedRFID = scannedRFID.substring(0, 4);
                if (!uniqueRFIDs.contains(scannedRFID)) {
                    uniqueRFIDs.add(scannedRFID);
                }
            }
        }

        if (!uniqueRFIDs.isEmpty()) {
            if (listener != null) {
                listener.onRFIDsScanned(uniqueRFIDs);
            }ToastManager.showToast(context, uniqueRFIDs.size() + " giá trị RFID đã được quét thành công");
        } else {
            ToastManager.showToast(context, "Không có giá trị RFID hợp lệ");
        }
}*/


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
import java.util.List;

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
                ToastManager.showToast(context, "Khởi tạo RFID không thành công");
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
            ToastManager.showToast(context, "Đầu đọc RFID chưa được khởi tạo");
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
            while (System.currentTimeMillis() - startTime < 1000) {
                // Simulating scan results with test values
                String[] testValues = {"3101", "3102", "3103éàèù", "3104éàèù"};
                for (String value : testValues) {
                    UHFTAGInfo tagInfo = new UHFTAGInfo();
                    tagInfo.setEPC(value);
                    results.add(tagInfo);
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
                ToastManager.showToast(context, "Không tìm thấy mã RFID nào");
            }
        }
    }

    private void processScannedRFIDs(List<UHFTAGInfo> results) {
        List<String> uniqueRFIDs = new ArrayList<>();
        for (UHFTAGInfo result : results) {
            String scannedRFID = result.getEPC();
            if (scannedRFID.length() >= 4) {
                scannedRFID = scannedRFID.substring(0, 4);
                if (!uniqueRFIDs.contains(scannedRFID)) {
                    uniqueRFIDs.add(scannedRFID);
                }
            }
        }

        if (!uniqueRFIDs.isEmpty()) {
            if (listener != null) {
                listener.onRFIDsScanned(uniqueRFIDs);
            }
            ToastManager.showToast(context, uniqueRFIDs.size() + " giá trị RFID đã được quét thành công");
        } else {
            ToastManager.showToast(context, "Không có giá trị RFID hợp lệ");
        }
    }
}