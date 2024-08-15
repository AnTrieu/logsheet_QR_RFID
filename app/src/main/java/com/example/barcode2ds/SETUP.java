package com.example.barcode2ds;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.deviceapi.RFIDWithUHFUART;

public class SETUP {
    private Context context;
    private RFIDWithUHFUART mReader;
    private TextView tvCurrentFrequency;
    private TextView tvCurrentPower;
    private TextView tvCurrentRFID;
    private Spinner spFrequency;
    private static String currentRFID = "Unknown";

    public SETUP(Context context, RFIDWithUHFUART reader) {
        this.context = context;
        this.mReader = reader;
    }

    public void showSetupPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.popup_setup, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        spFrequency = dialogView.findViewById(R.id.spFrequency);
        final Spinner spPower = dialogView.findViewById(R.id.spPower);
        final EditText etWriteData = dialogView.findViewById(R.id.etWriteData);
        Button btnSetFrequency = dialogView.findViewById(R.id.btnSetFrequency);
        Button btnSetPower = dialogView.findViewById(R.id.btnSetPower);
        Button btnWriteRFID = dialogView.findViewById(R.id.btnWriteRFID);
        tvCurrentFrequency = dialogView.findViewById(R.id.tvCurrentFrequency);
        tvCurrentPower = dialogView.findViewById(R.id.tvCurrentPower);
        tvCurrentRFID = dialogView.findViewById(R.id.tvCurrentRFID);

        // Setup frequency spinner
        ArrayAdapter<CharSequence> frequencyAdapter = ArrayAdapter.createFromResource(context,
                R.array.frequency_modes, android.R.layout.simple_spinner_item);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFrequency.setAdapter(frequencyAdapter);

        // Setup power spinner
        ArrayAdapter<CharSequence> powerAdapter = ArrayAdapter.createFromResource(context,
                R.array.power_levels, android.R.layout.simple_spinner_item);
        powerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPower.setAdapter(powerAdapter);

        // Display current values
        updateCurrentValues();

        btnSetFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedFrequency = spFrequency.getSelectedItemPosition();
                setFrequency(selectedFrequency);
                updateCurrentValues();
            }
        });

        btnSetPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPower = spPower.getSelectedItemPosition() + 1;
                setPower(selectedPower);
                updateCurrentValues();
            }
        });

        btnWriteRFID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = etWriteData.getText().toString();
                writeRFID(data);
            }
        });

        dialog.show();
    }

    private void updateCurrentValues() {
        // Get current frequency
        byte frequencyMode = (byte) mReader.getFrequencyMode();
        String[] frequencyModes = context.getResources().getStringArray(R.array.frequency_modes);
        if (frequencyMode >= 0 && frequencyMode < frequencyModes.length) {
            tvCurrentFrequency.setText("Current Frequency: " + frequencyModes[frequencyMode]);
            // Set the spinner to the current frequency
            spFrequency.setSelection(frequencyMode);
        } else {
            tvCurrentFrequency.setText("Current Frequency: Unknown (Mode: " + frequencyMode + ")");
        }

        // Log the current frequency mode
        Log.d("SETUP", "Current frequency mode: " + frequencyMode);

        // Get current power
        int power = mReader.getPower();
        tvCurrentPower.setText("Current Power: " + power);

        // Update RFID
        updateCurrentRFID(currentRFID);
    }

    public void updateCurrentRFID(String rfidValue) {
        if (tvCurrentRFID != null) {
            tvCurrentRFID.setText("Current RFID: " + rfidValue);
        }
        currentRFID = rfidValue;
    }

    private void setFrequency(int mode) {
        Log.d("SETUP", "Setting frequency mode to: " + mode);
        if (mReader.setFrequencyMode((byte) mode)) {
            Toast.makeText(context, R.string.uhf_msg_set_frequency_succ, Toast.LENGTH_SHORT).show();
            Log.d("SETUP", "Set frequency successful");
        } else {
            Toast.makeText(context, R.string.uhf_msg_set_frequency_fail, Toast.LENGTH_SHORT).show();
            Log.e("SETUP", "Set frequency failed");
        }

        // Verify the set frequency
        byte newFrequencyMode = (byte) mReader.getFrequencyMode();
        Log.d("SETUP", "New frequency mode after setting: " + newFrequencyMode);
    }

    private void setPower(int power) {
        if (mReader.setPower(power)) {
            Toast.makeText(context, R.string.uhf_msg_set_power_succ, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.uhf_msg_set_power_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private void writeRFID(String data) {
        if (data.isEmpty()) {
            Toast.makeText(context, R.string.uhf_msg_write_data_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        // Assuming we're writing to EPC memory bank
        if (mReader.writeData("00000000", RFIDWithUHFUART.Bank_EPC, 2, data.length() / 4, data)) {
            Toast.makeText(context, R.string.uhf_msg_write_succ, Toast.LENGTH_SHORT).show();
            updateCurrentRFID(data); // Update the RFID value after successful write
        } else {
            Toast.makeText(context, R.string.uhf_msg_write_fail, Toast.LENGTH_SHORT).show();
        }
    }

    public static void setCurrentRFID(String rfid) {
        currentRFID = rfid;
    }
}