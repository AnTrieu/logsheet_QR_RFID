package com.example.barcode2ds;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.rscja.deviceapi.RFIDWithUHFUART;

public class SETUP {
    private Context context;
    private RFIDWithUHFUART mReader;

    public SETUP(Context context, RFIDWithUHFUART reader) {
        this.context = context;
        this.mReader = reader;
    }

    public void showSetupPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_setup, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        final Spinner spinnerFrequency = dialogView.findViewById(R.id.spinnerFrequency);
        final EditText editTextPower = dialogView.findViewById(R.id.editTextPower);
        Button btnApplySettings = dialogView.findViewById(R.id.btnApplySettings);
        Button btnRewriteRFID = dialogView.findViewById(R.id.btnRewriteRFID);

        btnApplySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String frequency = spinnerFrequency.getSelectedItem().toString();
                int power = Integer.parseInt(editTextPower.getText().toString());
                applyRFIDSettings(frequency, power);
            }
        });

        btnRewriteRFID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRewriteRFIDPopup();
            }
        });

        dialog.show();
    }

    private void applyRFIDSettings(String frequency, int power) {
        // Implement the logic to apply RFID settings
        // This is a placeholder implementation
        boolean success = mReader.setPower(power);
        if (success) {
            Toast.makeText(context, "RFID settings applied successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to apply RFID settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRewriteRFIDPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_rewrite_rfid, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        final EditText editTextNewData = dialogView.findViewById(R.id.editTextNewData);
        Button btnRewrite = dialogView.findViewById(R.id.btnRewrite);

        btnRewrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newData = editTextNewData.getText().toString();
                rewriteRFIDChip(newData);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void rewriteRFIDChip(String newData) {
        // Implement the logic to rewrite RFID chip
        // This is a placeholder implementation
        Toast.makeText(context, "RFID chip rewritten with: " + newData, Toast.LENGTH_SHORT).show();
    }
}