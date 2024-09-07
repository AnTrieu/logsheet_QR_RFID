package com.example.barcode2ds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.List;
import java.util.Map;

public class SETUP {
    private static final String TAG = "SETUP";
    private Context context;
    private RFIDWithUHFUART mReader;
    private TextView tvCurrentFrequency;
    private TextView tvCurrentPower;
    private TextView tvCurrentRFID;
    private Spinner spFrequency;
    private Spinner spPower;
    private static String currentRFID = "Unknown";
    private List<String> scannedRFIDs = new ArrayList<>();
    private RFID rfid;

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

    private EditText textAPI;
    private Button btnAPI;
    private OnApiUrlChangedListener apiUrlChangedListener;

    public SETUP(Context context, RFIDWithUHFUART reader, RFID rfid) {
        this.context = context;
        this.mReader = reader;
        this.rfid = rfid;
    }

    public void showSetupPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.popup_setup, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        spFrequency = dialogView.findViewById(R.id.spFrequency);
        spPower = dialogView.findViewById(R.id.spPower);
        final EditText etWriteData = dialogView.findViewById(R.id.etWriteData);
        Button btnSetFrequency = dialogView.findViewById(R.id.btnSetFrequency);
        Button btnSetPower = dialogView.findViewById(R.id.btnSetPower);
        Button btnWriteRFID = dialogView.findViewById(R.id.btnWriteRFID);
        tvCurrentFrequency = dialogView.findViewById(R.id.tvCurrentFrequency);
        tvCurrentPower = dialogView.findViewById(R.id.tvCurrentPower);
        tvCurrentRFID = dialogView.findViewById(R.id.tvCurrentRFID);

        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(frequencyModeMap.keySet()));
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFrequency.setAdapter(frequencyAdapter);

        ArrayAdapter<CharSequence> powerAdapter = ArrayAdapter.createFromResource(context,
                R.array.power_levels, android.R.layout.simple_spinner_item);
        powerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPower.setAdapter(powerAdapter);

        updateCurrentValues();
        updateCurrentRFIDs(scannedRFIDs);

        Button btnClose = dialogView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

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

        Button btnScanRfid = dialogView.findViewById(R.id.btnScanRfid);
        btnScanRfid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rfid != null) {
                    rfid.startScan();
                } else {
                    ToastManager.showToast(context, "Máy quét RFID chưa được khởi tạo");
                }
            }
        });

        btnWriteRFID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = etWriteData.getText().toString();
                writeRFID(data);
            }
        });

        setupApiUrlInput(dialogView);

        dialog.show();
    }

    public interface OnApiUrlChangedListener {
        void onApiUrlChanged(String newUrl);
    }

    public void setOnApiUrlChangedListener(OnApiUrlChangedListener listener) {
        this.apiUrlChangedListener = listener;
    }

    private void setupApiUrlInput(View dialogView) {
        textAPI = dialogView.findViewById(R.id.textAPI);
        btnAPI = dialogView.findViewById(R.id.btnAPI);

        final String savedUrl = APIManager.getApiUrl(context);
        if (!savedUrl.isEmpty()) {
            textAPI.setText(savedUrl);
        }

        btnAPI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = textAPI.getText().toString().trim();
                if (url.isEmpty()) {
                    ToastManager.showToast(context, "Chưa nhập địa chỉ API");
                } else {
                    if (!url.equals(savedUrl)) {
                        APIManager.saveApiUrl(context, url);
                        updateApiUrl(url);
                        ToastManager.showToast(context, "Đã cập nhật địa chỉ API");
                        if (apiUrlChangedListener != null) {
                            apiUrlChangedListener.onApiUrlChanged(url);
                        }
                    }
                }
            }
        });
    }

    private void updateApiUrl(String url) {
        RecorderFetcher.updateApiUrl(url);
        Sync.updateApiUrl(url);
        Tagpoint.updateApiUrl(url);
    }

    private void updateCurrentValues() {
        byte frequencyMode = (byte) mReader.getFrequencyMode();
        String frequencyString = reverseFrequencyModeMap.get(frequencyMode);
        if (frequencyString != null) {
            tvCurrentFrequency.setText("Current Frequency: " + frequencyString);
            ArrayAdapter adapter = (ArrayAdapter) spFrequency.getAdapter();
            int position = adapter.getPosition(frequencyString);
            if (position != -1) {
                spFrequency.setSelection(position);
            }
        } else {
            tvCurrentFrequency.setText("Current Frequency: Unknown (Mode: " + frequencyMode + ")");
        }

        int power = mReader.getPower();
        tvCurrentPower.setText("Current Power: " + power + " dBm");
        if (power >= 1 && power <= 30) {
            spPower.setSelection(power - 1);
        }

        updateCurrentRFIDs(scannedRFIDs);
    }

    public void updateCurrentRFIDs(List<String> rfidValues) {
        this.scannedRFIDs = rfidValues;
        if (tvCurrentRFID != null) {
            tvCurrentRFID.setText("Scanned RFIDs: " + rfidValues.size());
            setupRFIDSelection();
        }
    }

    public void updateScannedRFIDs(List<String> rfidCodes) {
        this.scannedRFIDs = rfidCodes;
        updateCurrentRFIDs(rfidCodes);
    }

    private void setupRFIDSelection() {
        tvCurrentRFID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRFIDSelectionDialog();
            }
        });
    }

    private void showRFIDSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select RFID");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(scannedRFIDs);

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedRFID = arrayAdapter.getItem(which);
                currentRFID = selectedRFID;
                tvCurrentRFID.setText("Selected RFID: " + selectedRFID);
            }
        });

        builder.show();
    }

    private void setFrequency(String selectedFrequency) {
        Byte mode = frequencyModeMap.get(selectedFrequency);

        if (mode == null) {
            ToastManager.showToast(context, "Thiết lập tần số thất bại");
            return;
        }

        if (mReader.setFrequencyMode(mode)) {
            Toast.makeText(context, R.string.uhf_msg_set_frequency_succ, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.uhf_msg_set_frequency_fail, Toast.LENGTH_SHORT).show();
        }

        updateCurrentValues();
    }

    private void setPower(int power) {
        if (mReader.setPower(power)) {
            Toast.makeText(context, R.string.uhf_msg_set_power_succ, Toast.LENGTH_SHORT).show();
            spPower.setSelection(power - 1);
            tvCurrentPower.setText("Current Power: " + power + " dBm");
        } else {
            Toast.makeText(context, R.string.uhf_msg_set_power_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private void writeRFID(String data) {
        if (data.isEmpty()) {
            Toast.makeText(context, R.string.uhf_msg_write_data_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mReader.writeData("00000000", RFIDWithUHFUART.Bank_EPC, 2, data.length() / 4, data)) {
            Toast.makeText(context, R.string.uhf_msg_write_succ, Toast.LENGTH_SHORT).show();
            currentRFID = data;
            tvCurrentRFID.setText("Selected RFID: " + data);
        } else {
            Toast.makeText(context, R.string.uhf_msg_write_fail, Toast.LENGTH_SHORT).show();
        }
    }

    public static void setCurrentRFID(String rfid) {
        currentRFID = rfid;
    }
}