//Danh sách người ghi
package com.example.barcode2ds;

import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RecorderFetcher {

    public interface RecorderFetchListener {
        void onFetchComplete(ArrayList<String> recordersList);
        void onFetchFailed(Exception e);
    }

    private static final String TOKEN = "sdfghjkxcvbnmasdfghjkwerg5fabdsfghjkjhgfdsrtyueso";
    private static final String URL_STRING = "https://det.app/DETAPI/LOGSHEET/logsheetdata";

    public static void fetchRecorders(final RecorderFetchListener listener) {
        new AsyncTask<Void, Void, ArrayList<String>>() {
            private Exception fetchException = null;

            @Override
            protected ArrayList<String> doInBackground(Void... voids) {
                ArrayList<String> recordersList = new ArrayList<>();

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
                            recordersList.add(dataArray.getJSONObject(i).getString("giatri"));
                        }
                    }
                } catch (Exception e) {
                    fetchException = e;
                }

                return recordersList;
            }

            @Override
            protected void onPostExecute(ArrayList<String> result) {
                if (fetchException != null) {
                    listener.onFetchFailed(fetchException);
                } else {
                    listener.onFetchComplete(result);
                }
            }
        }.execute();
    }
}
