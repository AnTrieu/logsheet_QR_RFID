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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SETUP {
    private static final String TAG = "SETUP";
    private Context context;
    private RFIDWithUHFUART mReader;
    private TextView tvCurrentFrequency;
    private TextView tvCurrentPower;
    private TextView tvCurrentRFID;
    private Spinner spFrequency;
    private static String currentRFID = "Unknown";

    private static final Map<String, Byte> frequencyModeMap = new HashMap<>();
    private static final Map<Byte, String> reverseFrequencyModeMap = new HashMap<>();

    static {
        frequencyModeMap.put("China 840-845MHz", (byte) 0x01);
        frequencyModeMap.put("China 920-925MHz", (byte) 0x02);
        frequencyModeMap.put("ETSI 865-868MHz", (byte) 0x04);
        frequencyModeMap.put("United States 902-928MHz", (byte) 0x08);
        frequencyModeMap.put("Korea", (byte) 0x16);
        frequencyModeMap.put("Japan", (byte) 0x32);
        frequencyModeMap.put("South Africa 915-919MHz", (byte) 0x33);
        frequencyModeMap.put("New Zealand 922-927MHz", (byte) 0x34);
        frequencyModeMap.put("Morocco", (byte) 0x80);

        for (Map.Entry<String, Byte> entry : frequencyModeMap.entrySet()) {
            reverseFrequencyModeMap.put(entry.getValue(), entry.getKey());
        }
    }

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
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(frequencyModeMap.keySet()));
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
                String selectedFrequency = (String) spFrequency.getSelectedItem();
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
        String frequencyString = reverseFrequencyModeMap.get(frequencyMode);
        if (frequencyString != null) {
            tvCurrentFrequency.setText("Current Frequency: " + frequencyString);
            // Set the spinner to the current frequency
            ArrayAdapter adapter = (ArrayAdapter) spFrequency.getAdapter();
            int position = adapter.getPosition(frequencyString);
            if (position != -1) {
                spFrequency.setSelection(position);
            }
        } else {
            tvCurrentFrequency.setText("Current Frequency: Unknown (Mode: " + frequencyMode + ")");
        }

        // Log the current frequency mode
        Log.d(TAG, "Current frequency mode: " + frequencyMode);

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

    private void setFrequency(String selectedFrequency) {
        Byte mode = frequencyModeMap.get(selectedFrequency);

        if (mode == null) {
            Toast.makeText(context, "Invalid frequency mode", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Setting frequency mode to: " + mode);
        if (mReader.setFrequencyMode(mode)) {
            Toast.makeText(context, R.string.uhf_msg_set_frequency_succ, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Set frequency successful");
        } else {
            Toast.makeText(context, R.string.uhf_msg_set_frequency_fail, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Set frequency failed");
        }

        // Verify the set frequency
        updateCurrentValues();
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