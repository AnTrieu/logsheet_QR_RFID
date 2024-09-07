package com.example.barcode2ds;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

public class RFID_setup {
    private static final String TAG = "RFID_setup";
    private RFIDWithUHFUART mReader;
    private Context context;
    private OnRFIDScannedListener listener;

    public interface OnRFIDScannedListener {
        void onRFIDScanned(String rfidCode);
    }

    public void setOnRFIDScannedListener(OnRFIDScannedListener listener) {
        this.listener = listener;
    }

    public RFID_setup(Context context) {
        this.context = context;
        initUHF();
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

    private class ScanTask extends AsyncTask<Void, Void, String> {
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
        protected String doInBackground(Void... voids) {
            UHFTAGInfo result = mReader.inventorySingleTag();
            if (result != null) {
                return result.getEPC();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result != null) {
                if (listener != null) {
                    listener.onRFIDScanned(result);
                }
                ToastManager.showToast(context, "RFID đã được quét thành công: " + result);
            } else {
                ToastManager.showToast(context, "Không tìm thấy mã RFID nào");
            }
        }
    }
}