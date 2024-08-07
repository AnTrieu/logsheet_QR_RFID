package com.example.barcode2ds;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ScrollViewHandler {

    private Context context;
    private LinearLayout scrollLinearLayout;

    public ScrollViewHandler(Context context, LinearLayout scrollLinearLayout) {
        this.context = context;
        this.scrollLinearLayout = scrollLinearLayout;

        // Fetch and compare data
        fetchAndCompareData();
    }

    private void fetchAndCompareData() {
        String logsheetData = LogsheetFetcher.getStoredLogsheetData(context);
        if (logsheetData != null) {
            try {
                JSONArray jsonArray = new JSONArray(logsheetData);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String rfidcode = jsonObject.getString("rfidcode");
                    String qrcode = jsonObject.getString("qrcode");
                    String tagdes = jsonObject.getString("tagdes");

                    // Add new SoLieu layout if the condition is met
                    addNewSoLieu(rfidcode, qrcode, tagdes);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addNewSoLieu(String rfidcode, String qrcode, String tagdes) {
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout newSoLieuLayout = (LinearLayout) inflater.inflate(R.layout.solieulayout, null);

        EditText editTextRFID = newSoLieuLayout.findViewById(R.id.editTextValue);
        EditText editTextQRCode = newSoLieuLayout.findViewById(R.id.editTextNote);
        EditText editTextTagDes = newSoLieuLayout.findViewById(R.id.editTextText);

        editTextRFID.setText(rfidcode);
        editTextQRCode.setText(qrcode);
        editTextTagDes.setText(tagdes);

        scrollLinearLayout.addView(newSoLieuLayout, 0);
    }
}
