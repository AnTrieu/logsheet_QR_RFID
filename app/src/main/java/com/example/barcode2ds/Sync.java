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
import java.util.Iterator;

public class Sync {
    private static String SERVER_URL = "";

    public static void updateApiUrl(String url) {
        SERVER_URL = url;
    }
    private static final String TOKEN = "sdfghjkxcvbnmasdfghjkwerg5fabdsfghjkjhgfdsrtyueso";
    private static final String PREF_NAME = "TagpointPrefs";
    private static final String PREF_CHANGES_KEY = "userChanges";

    private Context context;
    private Clear clear;

    public Sync(Context context, Clear clear) {
        this.context = context;
        this.clear = clear;
    }

    public void syncData() {
        if (!isNetworkAvailable()) {
            ToastManager.showToast(context, "Upload dữ liệu không khả dụng (Không có kết nối internet)");
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
                        boolean success = uploadTagpoint(idinfo, value, tagpointData);
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
                ToastManager.showToast(context, "Không có tagpoint nào để lưu");
            } else {
                clear.clearTagpointData();
            }
        }

        private boolean uploadTagpoint(String idinfo, String value, JSONObject tagpointData) {
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String ngayghi = tagpointData.optString("ngayghi", "");
                String thoigianghi = tagpointData.optString("thoigianghi", "");
                String nguoighi = tagpointData.optString("nguoighi", "");
                String note = tagpointData.optString("note", "");

                // Kiểm tra nếu các thông tin cần thiết bị trống
                if (ngayghi.isEmpty() || thoigianghi.isEmpty() || nguoighi.isEmpty()) {
                    publishProgress("Thiếu thông tin cho tagpoint: " + idinfo);
                    return false;
                }

                String postData = "action=savedata_syncline" +
                        "&tokenapi=" + TOKEN +
                        "&idinfo=" + idinfo +
                        "&ngayghi=" + ngayghi +
                        "&thoigianghi=" + thoigianghi +
                        "&nguoighi=" + nguoighi +
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