package com.example.barcode2ds;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.Calendar;

public class TimeHandler {

    public static void setupTimeAutoCompleteTextView(Context context, final AutoCompleteTextView timeACTV) {
        ArrayList<String> timeList = new ArrayList<>();
        for (int i = 2; i <= 24; i += 2) {
            timeList.add(String.format("%02d:00", i));
        }

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, timeList);
        timeACTV.setAdapter(timeAdapter);

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int nearestEvenHour = (currentHour % 2 == 0) ? currentHour : currentHour + 1;

        String defaultTime = String.format("%02d:00", nearestEvenHour);
        timeACTV.setText(defaultTime, false);

        timeACTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeACTV.showDropDown();
            }
        });
    }
}