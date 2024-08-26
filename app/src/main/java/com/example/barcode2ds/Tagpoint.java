package com.example.barcode2ds;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Tagpoint {
    private static final String TAG = "Tagpoint";
    private static final String SERVER_URL = "https://det.app/DETAPI/LOGSHEET/logsheetdata";
    private static final String TOKEN = "sdfghjkxcvbnmasdfghjkwerg5fabdsfghjkjhgfdsrtyueso";
    private static final String PREF_NAME = "TagpointPrefs";
    private static final String PREF_DATA_KEY = "cachedData";
    private static final String PREF_CHANGES_KEY = "userChanges";

    private Context context;
    private List<TagpointData> tagpointDataList;
    private LinearLayout scrollLinearLayout;
    private SharedPreferences prefs;
    private EditText mainQRCodeEditText;
    private List<String> currentRFIDCodes = new ArrayList<>();
    private AutoCompleteTextView resultTextView;

    public Tagpoint(Context context, LinearLayout scrollLinearLayout, EditText mainQRCodeEditText, AutoCompleteTextView resultTextView) {
        this.context = context;
        this.scrollLinearLayout = scrollLinearLayout;
        this.mainQRCodeEditText = mainQRCodeEditText;
        this.resultTextView = resultTextView;
        this.tagpointDataList = new ArrayList<>();
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadCachedData();
        if (isNetworkAvailable()) {
            new FetchDataTask().execute();
        }

        this.mainQRCodeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                processQRCode(s.toString());
            }
        });
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
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes("utf-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    return response.toString();
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
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_DATA_KEY, data);
        editor.apply();
    }

    private void loadCachedData() {
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

    public void processRFIDCodes(List<String> rfidCodes) {
        if (!rfidCodes.equals(currentRFIDCodes)) {
            currentRFIDCodes = new ArrayList<>(rfidCodes);
            List<TagpointData> matchingData = new ArrayList<>();
            for (String rfidCode : rfidCodes) {
                matchingData.addAll(findMatchingTagpointData(rfidCode));
            }
            displayTagpoints(matchingData);
            updateResultTextView(matchingData);
        }
    }

    private void updateResultTextView(List<TagpointData> matchingData) {
        if (!matchingData.isEmpty()) {
            StringBuilder result = new StringBuilder();
            for (TagpointData data : matchingData) {
                result.append(data.getRfiddes()).append("\n");
            }
            resultTextView.setText(result.toString().trim());
        } else {
            resultTextView.setText("No matching RFID data found for: " + currentRFIDCodes);
        }
    }

    public void reInitialize() {
        tagpointDataList.clear();
        scrollLinearLayout.removeAllViews();
        if (isNetworkAvailable()) {
            new FetchDataTask().execute();
        } else {
            loadCachedData();
        }
        currentRFIDCodes.clear();
        mainQRCodeEditText.setText("");
        resultTextView.setText("");
    }

    public void processQRCode(final String qrCode) {
        if (currentRFIDCodes.isEmpty()) {
            return;
        }

        List<TagpointData> matchingRFIDTagpoints = new ArrayList<>();
        for (String rfidCode : currentRFIDCodes) {
            matchingRFIDTagpoints.addAll(findMatchingTagpointData(rfidCode));
        }

        Collections.sort(matchingRFIDTagpoints, new Comparator<TagpointData>() {
            @Override
            public int compare(TagpointData a, TagpointData b) {
                if (a.getQrcode().equals(qrCode) && !b.getQrcode().equals(qrCode)) {
                    return -1;
                } else if (!a.getQrcode().equals(qrCode) && b.getQrcode().equals(qrCode)) {
                    return 1;
                }
                return 0;
            }
        });

        displayTagpoints(matchingRFIDTagpoints);
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

    private void displayTagpoints(List<TagpointData> dataList) {
        scrollLinearLayout.removeAllViews();
        String currentQRCode = mainQRCodeEditText.getText().toString();
        for (TagpointData data : dataList) {
            createTagpoint(data, currentQRCode);
        }
    }

    private void createTagpoint(final TagpointData data, String currentQRCode) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View tagpointView = inflater.inflate(R.layout.solieulayout, null);

        final EditText editTextValue = tagpointView.findViewById(R.id.editTextValue);
        final EditText editTextNote = tagpointView.findViewById(R.id.editTextNote);
        EditText editTextText = tagpointView.findViewById(R.id.editTextText);

        editTextText.setText(data.getTagdes());

        boolean isMatchingQRCode = data.getQrcode().equals(currentQRCode);

        if (isMatchingQRCode) {
            tagpointView.setBackgroundColor(Color.parseColor("#d5e8d4"));
            tagpointView.setBackground(context.getResources().getDrawable(R.drawable.tagpoint_border_green));

            // Cho phép chỉnh sửa
            enableEditing(editTextValue);
            enableEditing(editTextNote);
        } else {
            tagpointView.setBackgroundColor(Color.parseColor("#fff2cc"));
            tagpointView.setBackground(context.getResources().getDrawable(R.drawable.tagpoint_border_orange));

            // Vô hiệu hóa chỉnh sửa
            disableEditing(editTextValue);
            disableEditing(editTextNote);
        }

        scrollLinearLayout.addView(tagpointView);

        View spacerView = new View(context);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (5 * context.getResources().getDisplayMetrics().density)
        );
        spacerView.setLayoutParams(spacerParams);
        scrollLinearLayout.addView(spacerView);

        // Load saved values
        JSONObject savedValues = loadSavedValues(data.getIdinfo());
        if (savedValues != null) {
            editTextValue.setText(savedValues.optString("value", ""));
            editTextNote.setText(savedValues.optString("note", ""));
            if (isMatchingQRCode) {
                validateAndColorValue(editTextValue, data);
            }
        }

        if (isMatchingQRCode) {
            // Chỉ thêm TextWatcher nếu QR code phù hợp
            addTextWatchers(editTextValue, editTextNote, data);
        }
    }

    private void enableEditing(EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setClickable(true);
        editText.setLongClickable(true);
        editText.setCursorVisible(true);
    }

    private void disableEditing(EditText editText) {
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setClickable(false);
        editText.setLongClickable(false);
        editText.setCursorVisible(false);
    }

    // Add TextWatcher for editTextValue
    private void addTextWatchers(final EditText editTextValue, final EditText editTextNote, final TagpointData data) {
        editTextValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                validateAndColorValue(editTextValue, data);
                saveValues(data.getIdinfo(), value, editTextNote.getText().toString());
            }
        });
        
        // Add TextWatcher for editTextNote
        editTextNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                saveValues(data.getIdinfo(), editTextValue.getText().toString(), s.toString());
            }
        });
    }

    private void validateAndColorValue(EditText editText, TagpointData data) {
        String value = editText.getText().toString();
        if (!value.isEmpty()) {
            try {
                double numValue = Double.parseDouble(value);
                boolean isValid = true;
                String message = "";

                if (data.getMin() != null && !data.getMin().isEmpty() && numValue < Double.parseDouble(data.getMin())) {
                    isValid = false;
                    message = "Giá trị nhập phải lớn hơn " + data.getMin();
                } else if (data.getMax() != null && !data.getMax().isEmpty() && numValue > Double.parseDouble(data.getMax())) {
                    isValid = false;
                    message = "Giá trị nhập phải nhỏ hơn " + data.getMax();
                }

                if (isValid) {
                    editText.setTextColor(Color.BLACK);
                } else {
                    editText.setTextColor(Color.RED);
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                editText.setTextColor(Color.RED);
                Toast.makeText(context, "Vui lòng nhập một số hợp lệ", Toast.LENGTH_SHORT).show();
            }
        } else {
            editText.setTextColor(Color.BLACK);
        }
    }

    private void saveValues(String idinfo, String value, String note) {
        try {
            JSONObject changes = new JSONObject(prefs.getString(PREF_CHANGES_KEY, "{}"));
            JSONObject values = new JSONObject();
            values.put("value", value);
            values.put("note", note);
            changes.put(idinfo, values);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_CHANGES_KEY, changes.toString());
            editor.apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error saving values", e);
        }
    }

    private JSONObject loadSavedValues(String idinfo) {
        try {
            JSONObject changes = new JSONObject(prefs.getString(PREF_CHANGES_KEY, "{}"));
            return changes.optJSONObject(idinfo);
        } catch (JSONException e) {
            Log.e(TAG, "Error loading saved values", e);
            return null;
        }
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

        public String getIdinfo() { return idinfo; }
        public String getRfidcode() { return rfidcode; }
        public String getRfiddes() { return rfiddes; }
        public String getQrcode() { return qrcode; }
        public String getTagdes() { return tagdes; }
        public String getMin() { return min; }
        public String getMax() { return max; }
    }
}