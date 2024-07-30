package com.example.barcode2ds;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    AutoCompleteTextView recordersACTV, timeACTV;
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

        // DatePicker setup
        dateTextView = findViewById(R.id.textview_date);
        dateHandler = new DateHandler(this, dateTextView);

        // Recorders AutoCompleteTextView setup
        recordersACTV = findViewById(R.id.ACTV_recorders);
        RecorderFetcher.fetchRecorders(new RecorderFetcher.RecorderFetchListener() {
            @Override
            public void onFetchComplete(ArrayList<String> recordersList) {
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

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollViewHandler.addNewSoLieu();
            }
        });
    }
}
