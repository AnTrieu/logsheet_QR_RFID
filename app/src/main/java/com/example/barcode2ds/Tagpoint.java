package com.example.barcode2ds;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

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

public class Tagpoint {
    private static final String TAG = "Tagpoint";
    private static final String SERVER_URL = "https://det.app/DETAPI/LOGSHEET/logsheetdata";
    private static final String TOKEN = "sdfghjkxcvbnmasdfghjkwerg5fabdsfghjkjhgfdsrtyueso";
    private static final String PREF_NAME = "TagpointPrefs";
    private static final String PREF_DATA_KEY = "cachedData";

    private Context context;
    private List<TagpointData> tagpointDataList;
    private LinearLayout scrollLinearLayout;

    public Tagpoint(Context context, LinearLayout scrollLinearLayout) {
        this.context = context;
        this.scrollLinearLayout = scrollLinearLayout;
        this.tagpointDataList = new ArrayList<>();
        loadCachedData();
        if (isNetworkAvailable()) {
            new FetchDataTask().execute();
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
            JSONObject jsonObject = new JSONObject(jsonString);
            String error = jsonObject.optString("error", null);
            if (error != null && error.isEmpty()) {
                JSONArray dataArray = jsonObject.getJSONArray("data");
                tagpointDataList.clear();
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject item = dataArray.getJSONObject(i);
                    TagpointData tagpointData = new TagpointData(
                            item.getString("idinfo"),
                            item.getString("rfidcode"),
                            item.getString("rfiddes"),
                            item.getString("qrcode"),
                            item.getString("tagdes"),
                            item.optString("min", ""),
                            item.optString("max", ""),
                            item.optInt("stt", 0)
                    );
                    tagpointDataList.add(tagpointData);
                }
                saveDataToCache(jsonString);
                Log.d(TAG, "Parsed " + tagpointDataList.size() + " tagpoint data items");
                Toast.makeText(context, "Data updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Server returned error: " + error);
                Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON", e);
            Toast.makeText(context, "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void processRFIDCode(String rfidCode) {
        List<TagpointData> matchingData = findMatchingTagpointData(rfidCode);
        scrollLinearLayout.removeAllViews();
        for (TagpointData data : matchingData) {
            createTagpoint(data);
        }
    }

    private List<TagpointData> findMatchingTagpointData(String rfidCode) {
        List<TagpointData> matchingData = new ArrayList<>();
        for (TagpointData data : tagpointDataList) {
            if (data.getRfidcode().equals(rfidCode)) {
                matchingData.add(data);
            }
        }
        return matchingData;
    }

    private void createTagpoint(TagpointData data) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View tagpointView = inflater.inflate(R.layout.solieulayout, null);

        EditText editTextValue = tagpointView.findViewById(R.id.editTextValue);
        EditText editTextNote = tagpointView.findViewById(R.id.editTextNote);
        EditText editTextText = tagpointView.findViewById(R.id.editTextText);

        editTextText.setText(data.getTagdes());

        scrollLinearLayout.addView(tagpointView);
    }

    private static class TagpointData {
        private String idinfo, rfidcode, rfiddes, qrcode, tagdes, min, max;
        private int stt;

        public TagpointData(String idinfo, String rfidcode, String rfiddes, String qrcode, String tagdes, String min, String max, int stt) {
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

        public String getTagdes() {
            return tagdes;
        }
    }
}