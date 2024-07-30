package com.example.barcode2ds;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;

public class TimeHandler {

    public static void setupTimeAutoCompleteTextView(Context context, final AutoCompleteTextView timeACTV) {
        ArrayList<String> timeList = new ArrayList<>();
        for (int i = 2; i <= 24; i += 2) {
            timeList.add(i + ":00");
        }
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, timeList);
        timeACTV.setAdapter(timeAdapter);
        timeACTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeACTV.showDropDown();  // Hiển thị dropdown khi bấm vào
            }
        });
    }
}
