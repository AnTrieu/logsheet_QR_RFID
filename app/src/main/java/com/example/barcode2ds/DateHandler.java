//Đo ngày
package com.example.barcode2ds;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateHandler {

    private Calendar calendar;
    private Context context;
    private TextView dateTextView;
    private SharedPreferences prefs;
    private static final String PREF_NAME = "TagpointPrefs";
    private static final String PREF_DATE_KEY = "savedDate";

    public DateHandler(final Context context, TextView dateTextView) {
        this.context = context;
        this.dateTextView = dateTextView;
        calendar = Calendar.getInstance();
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Load saved date
        loadSavedDate();

        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(context, date, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateDateLabel() {
        String myFormat = "dd/MM/yyyy"; // In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dateTextView.setText(sdf.format(calendar.getTime()));

        // Save the date
        saveDate(sdf.format(calendar.getTime()));
    }

    private DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel();
        }
    };

    private void saveDate(String date) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_DATE_KEY, date);
        editor.apply();
    }

    private void loadSavedDate() {
        String savedDate = prefs.getString(PREF_DATE_KEY, null);
        if (savedDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                calendar.setTime(sdf.parse(savedDate));
                updateDateLabel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

