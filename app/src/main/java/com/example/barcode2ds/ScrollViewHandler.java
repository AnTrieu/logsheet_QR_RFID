package com.example.barcode2ds;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ScrollViewHandler {

    private static final String URL = "https://det.app/DETAPI/LOGSHEET/logsheetdata";
    private static final String TOKEN = "sdfghjkxcvbnmasdfghjkwerg5fabdsfghjkjhgfdsrtyueso";

    private Context context;
    private LinearLayout scrollLinearLayout;
    private EditText editTextRFID;
    private TextView textViewQRCode;
    private OkHttpClient client;

    // Constructor with RFID and QR Code fields
    public ScrollViewHandler(Context context, LinearLayout scrollLinearLayout, EditText editTextRFID, TextView textViewQRCode) {
        this.context = context;
        this.scrollLinearLayout = scrollLinearLayout;
        this.editTextRFID = editTextRFID;
        this.textViewQRCode = textViewQRCode;
        this.client = new OkHttpClient();

        // Set up listeners for RFID and QR code inputs
        setupInputListeners();
    }

    // Original constructor
    public ScrollViewHandler(MainActivity context, LinearLayout scrollLinearLayout) {
        this.context = context;
        this.scrollLinearLayout = scrollLinearLayout;
    }

    private void setupInputListeners() {
        // Listener for RFID input
        editTextRFID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                checkAndSearchLogsheet();
            }
        });

        // Listener for QR code input
        textViewQRCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                checkAndSearchLogsheet();
            }
        });
    }

    private void checkAndSearchLogsheet() {
        String rfidcode = editTextRFID.getText().toString();
        String qrcode = textViewQRCode.getText().toString();

        if (!rfidcode.isEmpty() && !qrcode.isEmpty()) {
            fetchLogsheetData(rfidcode, qrcode);
        }
    }

    private void fetchLogsheetData(final String rfidcode, final String qrcode) {
        FormBody formBody = new FormBody.Builder()
                .add("action", "getdata_logsheet_info")
                .add("tokenapi", TOKEN)
                .add("rfidcode", rfidcode)
                .add("qrcode", qrcode)
                .build();

        Request request = new Request.Builder()
                .url(URL)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showErrorMessage("Failed to fetch data: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    parseAndDisplayLogsheetData(responseData, rfidcode, qrcode);
                } else {
                    showErrorMessage("Server returned error: " + response.code());
                }
            }
        });
    }

    private void parseAndDisplayLogsheetData(final String responseData, final String rfidcode, final String qrcode) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String storedRfidcode = jsonObject.getString("rfidcode");
                String storedQrcode = jsonObject.getString("qrcode");

                if (rfidcode.equals(storedRfidcode) && qrcode.equals(storedQrcode)) {
                    final String tagdes = jsonObject.getString("tagdes");
                    // Run on UI thread to update the UI
                    ((android.app.Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addNewSoLieu(rfidcode, qrcode, tagdes);
                        }
                    });
                    return;
                }
            }
            // If we've gone through all objects and haven't found a match
            showNotFoundMessage();
        } catch (JSONException e) {
            e.printStackTrace();
            showErrorMessage("Error parsing data: " + e.getMessage());
        }
    }

    private void addNewSoLieu(String rfidcode, String qrcode, String tagdes) {
        scrollLinearLayout.removeAllViews(); // Clear existing views

        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout newSoLieuLayout = (LinearLayout) inflater.inflate(R.layout.solieulayout, null);

        EditText editTextRFID = newSoLieuLayout.findViewById(R.id.editTextValue);
        EditText editTextQRCode = newSoLieuLayout.findViewById(R.id.editTextNote);
        EditText editTextTagDes = newSoLieuLayout.findViewById(R.id.editTextText);

        editTextRFID.setText(rfidcode);
        editTextQRCode.setText(qrcode);
        editTextTagDes.setText(tagdes);

        scrollLinearLayout.addView(newSoLieuLayout);
    }

    private void showNotFoundMessage() {
        ((android.app.Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "Không tìm thấy đối tượng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showErrorMessage(final String message) {
        ((android.app.Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
