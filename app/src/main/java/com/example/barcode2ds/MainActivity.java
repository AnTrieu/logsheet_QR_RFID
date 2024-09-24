package com.example.barcode2ds;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.rscja.barcode.BarcodeDecoder;
import com.rscja.barcode.BarcodeFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rscja.deviceapi.RFIDWithUHFUART;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    TextView recordersTextView, dateTextView, resultTextView;
    AutoCompleteTextView timeACTV;
    Button button2, button3, button4, button5, button8;
    LinearLayout scrollLinearLayout;
    DateHandler dateHandler;

    EditText editTextText2;
    String TAG = "MainActivity_2D";
    BarcodeDecoder barcodeDecoder = BarcodeFactory.getInstance().getBarcodeDecoder();
    QRcode qrCode;
    RFID rfid;
    private Tagpoint tagpoint;
    private Clear clear;
    private Sync sync;
    private Button btnView;
    private ArrayAdapter<String> recordersAdapter;
    private HashMap<String, String> recordersMap;
    private RFIDWithUHFUART mReader;
    private SETUP setup;
    private PopupWindow popupWindow;
    private Spinner resultSpinner;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        initializeViews();
        setupDateHandler();
        setupRecordersTextView();
        setupTimeACTV();
        setupButtons();
        initializeResultSpinner();
        setupTagpoint();
        setupQRCode();
        setupRFID();
        setupClear();
        setupSync();



        try {
            mReader = RFIDWithUHFUART.getInstance();
            setup = new SETUP(this, mReader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String savedUrl = APIManager.getApiUrl(this);
        if (!savedUrl.isEmpty()) {
            RecorderFetcher.updateApiUrl(savedUrl);
            Sync.updateApiUrl(savedUrl);
            Tagpoint.updateApiUrl(savedUrl);
        } else {
            ToastManager.showToast(this, "Chưa nhập địa chỉ API");
        }

        new InitTask().execute();

        loadAndApplySavedApiUrl();

        setup.setOnApiUrlChangedListener(new SETUP.OnApiUrlChangedListener() {
            @Override
            public void onApiUrlChanged(String newUrl) {
                reinitializeWithNewUrl(newUrl);
            }
        });
    }

    private void loadAndApplySavedApiUrl() {
        String savedUrl = APIManager.getApiUrl(this);
        if (!savedUrl.isEmpty()) {
            reinitializeWithNewUrl(savedUrl);
        } else {
            ToastManager.showToast(this, "Chưa có địa chỉ API được lưu. Vui lòng nhập trong phần Setup.");
        }
    }

    private void reinitializeWithNewUrl(String newUrl) {
        //Các đối tượng
        RecorderFetcher.updateApiUrl(newUrl);
        Sync.updateApiUrl(newUrl);
        Tagpoint.updateApiUrl(newUrl);

        // Danh sách người ghi
        setupRecordersTextView();

        // Đồng bộ dữ liệu
        sync = new Sync(this, dateTextView, timeACTV, recordersTextView, clear);

        // Tagname
        tagpoint = new Tagpoint(this, scrollLinearLayout, editTextText2, resultSpinner);
    }

    private void initializeViews() {
        dateTextView = findViewById(R.id.textview_date);
        recordersTextView = findViewById(R.id.TV_recorders);
        timeACTV = findViewById(R.id.ACTV_time);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button8 = findViewById(R.id.button8);
        btnView = findViewById(R.id.btnView);
        scrollLinearLayout = findViewById(R.id.scrollLinearLayout);
        editTextText2 = findViewById(R.id.editTextText2);
        resultSpinner = findViewById(R.id.rfiddes);
    }

    private void initializeResultSpinner() {
        List<String> initialList = new ArrayList<>();
        initialList.add("Mô tả RFID code");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, initialList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resultSpinner.setAdapter(adapter);
    }

    private void setupDateHandler() {
        dateHandler = new DateHandler(this, dateTextView);
    }

    private void setupRecordersTextView() {
        recordersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        recordersMap = new HashMap<>();

        RecorderFetcher.fetchRecorders(this, new RecorderFetcher.RecorderFetchListener() {
            @Override
            public void onFetchComplete(final HashMap<String, String> fetchedRecordersMap, final String lastSelectedRecorder) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateRecordersList(fetchedRecordersMap, lastSelectedRecorder);
                    }
                });
            }

            @Override
            public void onFetchFailed(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        e.printStackTrace();
                        ToastManager.showToast(MainActivity.this, "Lấy dữ liệu người ghi thất bại");
                    }
                });
            }
        });

        recordersTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecordersPopup();
            }
        });
    }

    private void updateRecordersList(HashMap<String, String> fetchedRecordersMap, String lastSelectedRecorder) {
        recordersAdapter.clear();
        recordersMap.clear();

        for (Map.Entry<String, String> entry : fetchedRecordersMap.entrySet()) {
            String ma = entry.getKey();
            String giatri = entry.getValue();
            recordersAdapter.add(giatri);
            recordersMap.put(giatri, ma);
        }

        if (!lastSelectedRecorder.isEmpty()) {
            String[] parts = lastSelectedRecorder.split(";");
            if (parts.length == 2 && fetchedRecordersMap.containsKey(parts[1])) {
                recordersTextView.setText(parts[0]);
            } else {
                recordersTextView.setText("");
            }
        } else {
            recordersTextView.setText("");
        }
    }

    private void showRecordersPopup() {
        ListView listView = new ListView(this);
        listView.setAdapter(recordersAdapter);

        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
        popupWindow = new PopupWindow(listView, recordersTextView.getWidth(), height, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setOutsideTouchable(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedGiatri = recordersAdapter.getItem(position);
                recordersTextView.setText(selectedGiatri);
                String selectedMa = recordersMap.get(selectedGiatri);
                RecorderFetcher.saveLastSelectedRecorder(MainActivity.this, selectedGiatri + ";" + selectedMa);
                popupWindow.dismiss();
            }
        });

        popupWindow.showAsDropDown(recordersTextView, 0, -height - recordersTextView.getHeight());
    }

    private void setupTimeACTV() {
        TimeHandler.setupTimeAutoCompleteTextView(this, timeACTV);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupButtons() {
        AnimationHandler.setButtonAnimation(button2);
        AnimationHandler.setButtonAnimation(button3);
        AnimationHandler.setButtonAnimation(button4);
        AnimationHandler.setButtonAnimation(button5);
        AnimationHandler.setButtonAnimation(button8);

        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTagpointHistory();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear.clearTagpointData();
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setup.showSetupPopup();
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sync.syncData();
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrCode.start();
            }
        });

        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rfid.startScan();
            }
        });
    }

    private void showTagpointHistory() {
        if (tagpoint != null) {
            tagpoint.showTagpointsHistoryPopup(this);
        } else {
            ToastManager.showToast(this, "Tagpoint chưa được khởi tạo");
        }
    }

    private void setupTagpoint() {
        tagpoint = new Tagpoint(this, scrollLinearLayout, editTextText2, resultSpinner);
    }

    private void setupQRCode() {
        qrCode = new QRcode(this, barcodeDecoder, editTextText2);
        editTextText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                tagpoint.processQRCode(s.toString());
            }
        });
    }

    private void setupRFID() {
        rfid = new RFID(this, button8);
        rfid.setOnRFIDScannedListener(new RFID.OnRFIDScannedListener() {
            @Override
            public void onRFIDsScanned(List<String> rfidCodes) {
                tagpoint.processRFIDCodes(rfidCodes);
            }
        });
    }

    private void setupClear() {
        clear = new Clear(this, tagpoint);
        clear.setOnClearCompletedListener(new Clear.OnClearCompletedListener() {
            @Override
            public void onClearCompleted() {
                // Khởi tạo lại Tagpoint sau khi xóa
                setupTagpoint();
                // Cập nhật lại RFID
                rfid.setOnRFIDScannedListener(new RFID.OnRFIDScannedListener() {
                    @Override
                    public void onRFIDsScanned(List<String> rfidCodes) {
                        tagpoint.processRFIDCodes(rfidCodes);
                    }
                });
            }
        });
    }

    private void setupSync() {
        sync = new Sync(this, dateTextView, timeACTV, recordersTextView, clear);
    }

    @Override
    protected void onPause() {
        super.onPause();
        String selectedGiatri = recordersTextView.getText().toString();
        if (!selectedGiatri.isEmpty()) {
            String selectedMa = recordersMap.get(selectedGiatri);
            if (selectedMa != null) {
                RecorderFetcher.saveLastSelectedRecorder(this, selectedGiatri + ";" + selectedMa);
            }
        }
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Application");
        builder.setMessage("Choose an option:");
        builder.setPositiveButton("Log out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logOut();
            }
        });
        builder.setNegativeButton("Turn off", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void logOut() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        Intent loginIntent = new Intent(MainActivity.this, login.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        close();
        super.onDestroy();
        Process.killProcess(Process.myPid());
    }

    private void open() {
        barcodeDecoder.open(this);
        Log.e(TAG, "open()==========================:" + barcodeDecoder.open(this));
    }

    private void close() {
        barcodeDecoder.close();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 291 || keyCode == 294) {
            rfid.startScan();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            open();
            Log.e(TAG, "doInBackground==========================:");
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mypDialog = new ProgressDialog(MainActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("init...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.setCancelable(false);
            mypDialog.show();
        }
    }
}