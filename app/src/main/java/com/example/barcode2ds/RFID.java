package com.example.barcode2ds;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

public class RFID {

    private static final String TAG = "RFIDActivity";
    private RFIDWithUHFUART mReader;
    private TextView resultTextView;
    private Button scanButton;
    private Context context;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    public RFID(Context context, TextView resultTextView, Button scanButton) {
        this.context = context;
        this.resultTextView = resultTextView;
        this.scanButton = scanButton;
        checkReadWritePermission();
        initUHF();

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
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
            if (result == null) {
                resultTextView.setText("No tag found");
            } else {
                resultTextView.setText("" + result.getEPC());
            }
        }
    }

    private void initUHF() {
        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception ex) {
            toastMessage(ex.getMessage());
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

    private void checkReadWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((AppCompatActivity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((AppCompatActivity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }
        }
    }

    private void toastMessage(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Read external storage permission granted");
            } else {
                Log.e(TAG, "Read external storage permission denied");
            }
        }
    }
}
