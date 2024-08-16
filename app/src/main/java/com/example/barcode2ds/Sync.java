package com.example.barcode2ds;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Sync {
    private static final String SERVER_URL = "https://det.app/DETAPI/LOGSHEET/logsheetdata";
    private static final String TOKEN = "sdfghjkxcvbnmasdfghjkwerg5fabdsfghjkjhgfdsrtyueso";
    private static final String PREF_NAME = "TagpointPrefs";
    private static final String PREF_CHANGES_KEY = "userChanges";

    private Context context;
    private TextView dateTextView;
    private TextView timeTextView;
    private TextView recorderTextView;
    private Clear clear;

    public Sync(Context context, TextView dateTextView, TextView timeTextView, TextView recorderTextView, Clear clear) {
        this.context = context;
        this.dateTextView = dateTextView;
        this.timeTextView = timeTextView;
        this.recorderTextView = recorderTextView;
        this.clear = clear;
    }

    public void syncData() {
        if (TextUtils.isEmpty(dateTextView.getText()) ||
                TextUtils.isEmpty(timeTextView.getText()) ||
                TextUtils.isEmpty(recorderTextView.getText())) {
            Toast.makeText(context, "Thiếu dữ liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(context, "Upload dữ liệu không khả dụng (Không có kết nối internet)", Toast.LENGTH_LONG).show();
            return;
        }

        new SyncTask().execute();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private class SyncTask extends AsyncTask<Void, String, Boolean> {
        private boolean anyTagpointUploaded = false;

        @Override
        protected Boolean doInBackground(Void... voids) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String changesJson = prefs.getString(PREF_CHANGES_KEY, "{}");

            try {
                JSONObject changes = new JSONObject(changesJson);
                Iterator<String> keys = changes.keys();

                while (keys.hasNext()) {
                    String idinfo = keys.next();
                    JSONObject tagpointData = changes.getJSONObject(idinfo);

                    String value = tagpointData.optString("value", "");

                    if (!value.isEmpty()) {
                        boolean success = uploadTagpoint(idinfo, value, tagpointData.optString("note", ""));
                        if (success) {
                            anyTagpointUploaded = true;
                        }
                    }
                }

                return anyTagpointUploaded;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Toast.makeText(context, "Không có tagpoint nào để lưu", Toast.LENGTH_SHORT).show();
            } else {
                clear.clearTagpointData();
            }
        }

        private boolean uploadTagpoint(String idinfo, String value, String note) {
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                // Get the 'ma' value corresponding to the 'giatri' in the RecorderTextView
                String recorderValue = recorderTextView.getText().toString();
                String recorderMa = "";
                HashMap<String, String> recordersMap = RecorderFetcher.getRecordersFromLocal(context);
                for (Map.Entry<String, String> entry : recordersMap.entrySet()) {
                    if (entry.getValue().equals(recorderValue)) {
                        recorderMa = entry.getKey();
                        break;
                    }
                }

                // Get the time value directly from the ACTV_time
                String timeValue = timeTextView.getText().toString();

                String postData = "action=savedata_syncline" +
                        "&tokenapi=" + TOKEN +
                        "&idinfo=" + idinfo +
                        "&ngayghi=" + dateTextView.getText().toString() +
                        "&thoigianghi=" + timeValue +
                        "&nguoighi=" + recorderMa +
                        "&giatri=" + value +
                        "&ghichu=" + note;

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String error = jsonResponse.optString("error", null);
                    String message = jsonResponse.optString("message", "");

                    if (error != null && error.isEmpty()) {
                        publishProgress(message);
                        return true;
                    } else {
                        publishProgress("Error: " + error);
                        return false;
                    }
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show();
        }
    }
}