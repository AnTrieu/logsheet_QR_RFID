package com.example.barcode2ds;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ScrollViewHandler {

    private Context context;
    private LinearLayout scrollLinearLayout;

    public ScrollViewHandler(Context context, LinearLayout scrollLinearLayout) {
        this.context = context;
        this.scrollLinearLayout = scrollLinearLayout;
    }

    public void addNewSoLieu() {
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout newSoLieuLayout = (LinearLayout) inflater.inflate(R.layout.solieulayout, null);

        EditText editTextValue = newSoLieuLayout.findViewById(R.id.editTextValue);
        EditText editTextNote = newSoLieuLayout.findViewById(R.id.editTextNote);
        EditText editTextText = newSoLieuLayout.findViewById(R.id.editTextText);

        scrollLinearLayout.addView(newSoLieuLayout, 0);
    }
}
