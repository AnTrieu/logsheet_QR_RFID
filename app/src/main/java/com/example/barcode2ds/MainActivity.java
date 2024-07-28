package com.example.barcode2ds;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Spinner timeSpinner;
    Spinner recordersSpinner;
    TextView dateTextView;
    Button button2, button3, button4, button5, button8;
    LinearLayout scrollLinearLayout;
    DateHandler dateHandler;
    ScrollViewHandler scrollViewHandler;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Spinner setup for time
        timeSpinner = findViewById(R.id.spinner_time);
        TimeHandler.setupTimeSpinner(this, timeSpinner);

        // DatePicker setup
        dateTextView = findViewById(R.id.textview_date);
        dateHandler = new DateHandler(this, dateTextView);

        // Recorders Spinner setup
        recordersSpinner = findViewById(R.id.spinner_recorders);
        new LoadRecordersTask().execute();

        // Animation setup
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button8 = findViewById(R.id.button8);

        scrollLinearLayout = findViewById(R.id.scrollLinearLayout);
        scrollViewHandler = new ScrollViewHandler(this, scrollLinearLayout);

        AnimationHandler.setButtonAnimation(button2);
        AnimationHandler.setButtonAnimation(button3);
        AnimationHandler.setButtonAnimation(button4);
        AnimationHandler.setButtonAnimation(button5);
        AnimationHandler.setButtonAnimation(button8);

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollViewHandler.addNewSoLieu();
            }
        });
    }

    private class LoadRecordersTask extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> recordersList = new ArrayList<>();
            String token = "sdfghjkxcvbnmasdfghjkwerg5fabdsfghjkjhgfdsrtyueso";
            String urlString = "https://det.app/DETAPI/LOGSHEET/logsheetdata";

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.getOutputStream().write(("action=getdata_nguoighi&tokenapi=" + token).getBytes());

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
                e.printStackTrace();
            }

            return recordersList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, result);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            recordersSpinner.setAdapter(adapter);
        }
    }
}
