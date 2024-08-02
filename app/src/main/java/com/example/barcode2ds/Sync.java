package com.example.barcode2ds;

import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Sync {

    private static final String TOKEN = "sdfghjkxcvbnmasdfghjkwerg5fabdsfghjkjhgfdsrtyueso";
    private static final String URL = "https://det.app/DETAPI/LOGSHEET/logsheetdata";

    public static void syncData(final Context context, TextView dateTextView, AutoCompleteTextView timeACTV, AutoCompleteTextView recordersACTV, LinearLayout scrollLinearLayout) {
        String idinfo = "31142"; // Replace with actual idinfo from the logsheet data
        String ngayghi = dateTextView.getText().toString();
        String thoigianghi = timeACTV.getText().toString();
        String nguoighi = recordersACTV.getText().toString();

        // Lấy giá trị từ các EditText trong solieulayout.xml
        LinearLayout soLieuLayout = (LinearLayout) scrollLinearLayout.getChildAt(0); // Assuming the first child is the latest one
        EditText editTextValue = soLieuLayout.findViewById(R.id.editTextValue);
        EditText editTextNote = soLieuLayout.findViewById(R.id.editTextNote);

        String giatri = editTextValue.getText().toString();
        String ghichu = editTextNote.getText().toString();

        FormBody formBody = new FormBody.Builder()
                .add("action", "savedata_syncline")
                .add("tokenapi", TOKEN)
                .add("idinfo", idinfo)
                .add("ngayghi", ngayghi)
                .add("thoigianghi", thoigianghi)
                .add("nguoighi", nguoighi)
                .add("giatri", giatri)
                .add("ghichu", ghichu)
                .build();

        NetworkHandler.post(URL, formBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ((MainActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Failed to sync data", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        final String error = jsonResponse.getString("error");
                        final String message = jsonResponse.getString("message");

                        ((MainActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (error.isEmpty()) {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ((MainActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Failed to parse server response", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    ((MainActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Error syncing data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
