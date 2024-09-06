package com.example.barcode2ds;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RecorderFetcher {

    public interface RecorderFetchListener {
        void onFetchComplete(HashMap<String, String> recordersMap, String lastSelectedRecorder);
        void onFetchFailed(Exception e);
    }

    private static final String TOKEN = "sdfghjkxcvbnmasdfghjkwerg5fabdsfghjkjhgfdsrtyueso";
    private static String URL_STRING = "";

    public static void updateApiUrl(String url) {
        URL_STRING = url;
    }
    private static final String PREF_NAME = "RecorderData";
    private static final String LAST_SELECTED_KEY = "lastSelectedRecorder";
    private static final String SERVER_DATA_KEY = "serverData";

    public static void fetchRecorders(final Context context, final RecorderFetchListener listener) {
        if (isNetworkAvailable(context)) {
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
                        String lastSelected = getLastSelectedRecorder(context);
                        String updatedLastSelected = updateLastSelectedRecorder(result, lastSelected);
                        saveLastSelectedRecorder(context, updatedLastSelected);
                        listener.onFetchComplete(result, updatedLastSelected);
                    }
                }
            }.execute();
        } else {
            HashMap<String, String> localData = getRecordersFromLocal(context);
            String lastSelected = getLastSelectedRecorder(context);
            listener.onFetchComplete(localData, lastSelected);
        }
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static void saveRecordersToLocal(Context context, HashMap<String, String> recordersMap) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONObject jsonObject = new JSONObject(recordersMap);
        editor.putString(SERVER_DATA_KEY, jsonObject.toString());
        editor.apply();
    }

    public static HashMap<String, String> getRecordersFromLocal(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString(SERVER_DATA_KEY, "{}");
        HashMap<String, String> recordersMap = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String key = it.next();
                recordersMap.put(key, jsonObject.getString(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return recordersMap;
    }

    public static void saveLastSelectedRecorder(Context context, String recorder) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LAST_SELECTED_KEY, recorder);
        editor.apply();
    }

    public static String getLastSelectedRecorder(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LAST_SELECTED_KEY, "");
    }

    private static String updateLastSelectedRecorder(HashMap<String, String> newData, String lastSelected) {
        if (!lastSelected.isEmpty()) {
            String[] parts = lastSelected.split(";");
            if (parts.length == 2) {
                String ma = parts[1];
                if (newData.containsKey(ma)) {
                    String newGiatri = newData.get(ma);
                    return newGiatri + ";" + ma;
                }
            }
        }
        return "";
    }
}