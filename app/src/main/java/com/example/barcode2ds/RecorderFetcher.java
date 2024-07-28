package com.example.barcode2ds;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RecorderFetcher {

    private Context context;
    private Spinner spinner;
    private String token = "sdfghjkxcvbnmasdfghjkwerg5fabdsfghjkjhgfdsrtyueso";
    private String urlString = "https://det.app/DETAPI/LOGSHEET/logsheetdata";

    public RecorderFetcher(Context context, Spinner spinner) {
        this.context = context;
        this.spinner = spinner;
    }

    public void fetchRecorders() {
        new LoadRecordersTask().execute();
    }

    private class LoadRecordersTask extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> recordersList = new ArrayList<>();
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");

                JSONObject postData = new JSONObject();
                postData.put("action", "getdata_nguoighi");
                postData.put("tokenapi", token);

                OutputStream os = urlConnection.getOutputStream();
                os.write(postData.toString().getBytes());
                os.flush();
                os.close();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject response = new JSONObject(result.toString());
                if (response.getString("error").isEmpty()) {
                    JSONArray dataArray = response.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        recordersList.add(dataArray.getJSONObject(i).getString("giatri"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return recordersList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, result);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
    }
}
