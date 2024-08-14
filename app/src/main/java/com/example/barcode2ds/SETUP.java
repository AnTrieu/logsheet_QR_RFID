package com.example.barcode2ds;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
        View dialogView = inflater.inflate(R.layout.popup_setup, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        final Spinner spFrequency = dialogView.findViewById(R.id.spFrequency);
        final Spinner spPower = dialogView.findViewById(R.id.spPower);
        final EditText etWriteData = dialogView.findViewById(R.id.etWriteData);
        Button btnSetFrequency = dialogView.findViewById(R.id.btnSetFrequency);
        Button btnSetPower = dialogView.findViewById(R.id.btnSetPower);
        Button btnWriteRFID = dialogView.findViewById(R.id.btnWriteRFID);

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

        btnSetFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedFrequency = spFrequency.getSelectedItemPosition();
                setFrequency(selectedFrequency);
            }
        });

        btnSetPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPower = spPower.getSelectedItemPosition() + 1;
                setPower(selectedPower);
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

    private void setFrequency(int mode) {
        if (mReader.setFrequencyMode((byte) mode)) {
            Toast.makeText(context, R.string.uhf_msg_set_frequency_succ, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.uhf_msg_set_frequency_fail, Toast.LENGTH_SHORT).show();
        }
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
        } else {
            Toast.makeText(context, R.string.uhf_msg_write_fail, Toast.LENGTH_SHORT).show();
        }
    }
}