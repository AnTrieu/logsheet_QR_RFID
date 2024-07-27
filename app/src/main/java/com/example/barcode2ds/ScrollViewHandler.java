package com.example.barcode2ds;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        TextView textView2 = newSoLieuLayout.findViewById(R.id.textView2);
        TextView textView3 = newSoLieuLayout.findViewById(R.id.textView3);
        EditText editTextText = newSoLieuLayout.findViewById(R.id.editTextText);

        textView2.setText("Không xác định");
        textView3.setText("Không xác định");

        scrollLinearLayout.addView(newSoLieuLayout, 0);
    }
}
