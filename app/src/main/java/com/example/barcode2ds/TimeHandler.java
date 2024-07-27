package com.example.barcode2ds;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

public class TimeHandler {

    public static void setupTimeSpinner(Context context, Spinner timeSpinner) {
        ArrayList<String> timeList = new ArrayList<>();
        for (int i = 2; i <= 24; i += 2) {
            timeList.add(i + ":00");
        }
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, timeList);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);
    }
}
