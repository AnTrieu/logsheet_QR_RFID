package com.example.barcode2ds;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class Clear {
    private static final String PREF_NAME = "TagpointPrefs";
    private static final String PREF_CHANGES_KEY = "userChanges";
    private Context context;
    private Tagpoint tagpoint;

    public Clear(Context context, Tagpoint tagpoint) {
        this.context = context;
        this.tagpoint = tagpoint;
    }

    public interface OnClearCompletedListener {
        void onClearCompleted();
    }

    private OnClearCompletedListener clearCompletedListener;

    public void setOnClearCompletedListener(OnClearCompletedListener listener) {
        this.clearCompletedListener = listener;
    }

    public void clearTagpointData() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PREF_CHANGES_KEY);
        editor.remove(Tagpoint.PREF_DATA_KEY);
        editor.apply();
        ToastManager.showToast(context, "Đã xóa dữ liệu tagname");
        tagpoint.reInitialize();
        tagpoint.clearResultSpinner();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) tagpoint.getResultSpinner().getAdapter();
        adapter.clear();
        adapter.add("Mô tả RFID code");
        adapter.notifyDataSetChanged();
        if (clearCompletedListener != null) {
            clearCompletedListener.onClearCompleted();
        }
    }
}