//Lấy dữ liệu logsheet từ server
package com.example.barcode2ds;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LogsheetFetcher {
    private static final String URL = "https://det.app/DETAPI/LOGSHEET/logsheetdata";
    private static final String TOKEN = "sdfghjkxcvbnmasdfghjkwerg5fabdsfghjkjhgfdsrtyueso";
    private static final String PREFS_NAME = "LogsheetPrefs";
    private static final String KEY_LOGSHEET_DATA = "logsheet_data";

    public interface LogsheetFetchListener {
        void onFetchComplete(String responseData);
        void onFetchFailed(Exception e);
    }

    public static void fetchLogsheetData(final Context context, final LogsheetFetchListener listener) {
        FormBody formBody = new FormBody.Builder()
                .add("action", "getdata_logsheet_info")
                .add("tokenapi", TOKEN)
                .build();

        NetworkHandler.post(URL, formBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.onFetchFailed(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // Lưu trữ dữ liệu vào SharedPreferences
                    SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(KEY_LOGSHEET_DATA, responseData);
                    editor.apply();

                    if (listener != null) {
                        listener.onFetchComplete(responseData);
                    }
                } else {
                    if (listener != null) {
                        listener.onFetchFailed(new IOException("Error fetching logsheet data"));
                    }
                }
            }
        });
    }

    public static String getStoredLogsheetData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_LOGSHEET_DATA, null);
    }
}
