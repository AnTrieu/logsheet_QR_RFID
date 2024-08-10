package com.example.barcode2ds;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class RFID {
    private static final String TAG = "RFID";
    private RFIDWithUHFUART mReader;
    private TextView resultTextView;
    private Button scanButton;
    private Context context;
    private static final String SERVER_URL = "https://det.app/DETAPI/LOGSHEET/logsheetdata";
    private static final String TOKEN = "sdfghjkxcvbnmasdfghjkwerg5fabdsfghjkjhgfdsrtyueso";
    private static final String PREF_NAME = "RFIDPrefs";
    private static final String PREF_DATA_KEY = "cachedData";

    private List<RFIDData> rfidDataList;

    public RFID(Context context, TextView resultTextView, Button scanButton) {
        this.context = context;
        this.resultTextView = resultTextView;
        this.scanButton = scanButton;
        this.rfidDataList = new ArrayList<>();
        initUHF();

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });

        loadCachedData();
        if (isNetworkAvailable()) {
            new FetchDataTask().execute();
        }
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

    private class FetchDataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String postData = "action=getdata_logsheet_info&tokenapi=" + TOKEN;
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = postData.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        return response.toString();
                    }
                } else {
                    Log.e(TAG, "HTTP error code: " + responseCode);
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching data", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                parseAndSaveData(result);
            } else {
                Toast.makeText(context, "Failed to fetch data from server", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseAndSaveData(String jsonString) {
        try {
            Log.d(TAG, "Received JSON: " + jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);

            if (!jsonObject.has("error")) {
                Log.e(TAG, "JSON does not contain 'error' field");
                Toast.makeText(context, "Invalid server response", Toast.LENGTH_SHORT).show();
                return;
            }

            String error = jsonObject.getString("error");
            if (error.isEmpty()) {
                if (!jsonObject.has("data")) {
                    Log.e(TAG, "JSON does not contain 'data' field");
                    Toast.makeText(context, "Invalid server response", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONArray dataArray = jsonObject.getJSONArray("data");
                rfidDataList.clear();
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject item = dataArray.getJSONObject(i);
                    RFIDData rfidData = new RFIDData(
                            item.getString("idinfo"),
                            item.getString("rfidcode"),
                            item.getString("rfiddes"),
                            0  // Assuming 'stt' is not provided in the new format
                    );
                    rfidDataList.add(rfidData);
                }
                saveDataToCache(jsonString);
                Log.d(TAG, "Parsed " + rfidDataList.size() + " RFID data items");
                Toast.makeText(context, "Data updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Server returned error: " + error);
                Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON", e);
            Toast.makeText(context, "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error", e);
            Toast.makeText(context, "Unexpected error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int parseIntSafely(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0; // or any default value you prefer
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
            if (result == null) {
                resultTextView.setText("No tag found");
            } else {
                //String scannedRFID = result.getEPC();
                String scannedRFID ="3102";
                RFIDData matchedData = findMatchingRFIDData(scannedRFID);
                if (matchedData != null) {
                    resultTextView.setText(matchedData.getRfiddes());
                } else {
                    resultTextView.setText("No matching RFID data found for: " + scannedRFID);
                }
            }
        }
    }

    private void saveDataToCache(String data) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_DATA_KEY, data);
        editor.apply();
    }

    private void loadCachedData() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String cachedData = prefs.getString(PREF_DATA_KEY, null);
        if (cachedData != null) {
            parseAndSaveData(cachedData);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private RFIDData findMatchingRFIDData(String scannedRFID) {
        for (RFIDData data : rfidDataList) {
            if (data.getRfidcode().equals(scannedRFID)) {
                return data;
            }
        }
        return null;
    }

    private static class RFIDData {
        private String idinfo, rfidcode, rfiddes, qrcode, tagdes;
        private int min, max, stt;

        public RFIDData(String idinfo, String rfidcode, String rfiddes, int min) {
            this.idinfo = idinfo;
            this.rfidcode = rfidcode;
            this.rfiddes = rfiddes;
            this.qrcode = qrcode;
            this.tagdes = tagdes;
            this.min = min;
            this.max = max;
            this.stt = stt;
        }

        public String getRfidcode() {
            return rfidcode;
        }

        public String getRfiddes() {
            return rfiddes;
        }
    }
}