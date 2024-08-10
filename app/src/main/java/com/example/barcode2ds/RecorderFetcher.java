package com.example.barcode2ds;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RecorderFetcher {

    public interface RecorderFetchListener {
        void onFetchComplete(HashMap<String, String> recordersMap);
        void onFetchFailed(Exception e);
    }

    private static final String TOKEN = "sdfghjkxcvbnmasdfghjkwerg5fabdsfghjkjhgfdsrtyueso";
    private static final String URL_STRING = "https://det.app/DETAPI/LOGSHEET/logsheetdata";
    private static final String PREF_NAME = "RecorderData";
    private static final String PREF_KEY = "recordersMap";

    public static void fetchRecorders(final Context context, final RecorderFetchListener listener) {
        if (isNetworkAvailable(context)) {
            // Có mạng -> Fetch data từ server và cập nhật vào SharedPreferences
            new AsyncTask<Void, Void, HashMap<String, String>>() {
                private Exception fetchException = null;

                @Override
                protected HashMap<String, String> doInBackground(Void... voids) {
                    HashMap<String, String> recordersMap = new HashMap<>();

                    try {
                        URL url = new URL(URL_STRING);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setDoOutput(true);
                        urlConnection.getOutputStream().write(("action=getdata_nguoighi&tokenapi=" + TOKEN).getBytes());

                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        JSONObject jsonObject = new JSONObject(result.toString());
                        if (jsonObject.getString("error").isEmpty()) {
                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject dataObject = dataArray.getJSONObject(i);
                                String ma = dataObject.getString("ma");
                                String giatri = dataObject.getString("giatri");
                                recordersMap.put(ma, giatri);
                            }
                        }
                    } catch (Exception e) {
                        fetchException = e;
                    }

                    return recordersMap;
                }

                @Override
                protected void onPostExecute(HashMap<String, String> result) {
                    if (fetchException != null) {
                        listener.onFetchFailed(fetchException);
                    } else {
                        saveRecordersToLocal(context, result);
                        listener.onFetchComplete(result);
                    }
                }
            }.execute();
        } else {
            // Không có mạng -> Lấy data từ SharedPreferences
            HashMap<String, String> localData = getRecordersFromLocal(context);
            listener.onFetchComplete(localData);
        }
    }

    // Kiểm tra kết nối mạng
    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Lưu data vào SharedPreferences
    private static void saveRecordersToLocal(Context context, HashMap<String, String> recordersMap) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Map.Entry<String, String> entry : recordersMap.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.apply();
    }

    // Lấy data từ SharedPreferences
    private static HashMap<String, String> getRecordersFromLocal(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        HashMap<String, String> recordersMap = new HashMap<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            recordersMap.put(entry.getKey(), entry.getValue().toString());
        }
        return recordersMap;
    }
}
