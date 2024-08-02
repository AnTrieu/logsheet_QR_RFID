package com.example.barcode2ds;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    AutoCompleteTextView recordersACTV, timeACTV;
    TextView dateTextView;
    Button button2, button3, button4, button5, button8;
    LinearLayout scrollLinearLayout;
    DateHandler dateHandler;
    ScrollViewHandler scrollViewHandler;

    private static final String TOKEN = "sdfghjkxcvbnmasdfghjkwerg5fabdsfghjkjhgfdsrtyueso";
    private static final String URL = "https://det.app/DETAPI/LOGSHEET/logsheetdata";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // DatePicker setup
        dateTextView = findViewById(R.id.textview_date);
        dateHandler = new DateHandler(this, dateTextView);

        // Recorders AutoCompleteTextView setup
        recordersACTV = findViewById(R.id.ACTV_recorders);
        RecorderFetcher.fetchRecorders(new RecorderFetcher.RecorderFetchListener() {
            @Override
            public void onFetchComplete(final ArrayList<String> recordersList) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, recordersList);
                recordersACTV.setAdapter(adapter);
                recordersACTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recordersACTV.showDropDown();  // Hiển thị dropdown khi bấm vào
                    }
                });
            }

            @Override
            public void onFetchFailed(Exception e) {
                e.printStackTrace();
            }
        });

        // Time AutoCompleteTextView setup
        timeACTV = findViewById(R.id.ACTV_time);
        TimeHandler.setupTimeAutoCompleteTextView(this, timeACTV);

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

        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollViewHandler.addNewSoLieu();
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sync.syncData(MainActivity.this, dateTextView, timeACTV, recordersACTV, scrollLinearLayout);
            }
        });

        fetchLogsheetData();
    }

    private void fetchLogsheetData() {
        FormBody formBody = new FormBody.Builder()
                .add("action", "getdata_logsheet_info")
                .add("tokenapi", TOKEN)
                .build();

        NetworkHandler.post(URL, formBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Failed to fetch logsheet data", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    // Handle the response data as needed
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Logsheet data fetched successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Error fetching logsheet data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
