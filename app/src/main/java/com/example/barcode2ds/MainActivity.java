package com.example.barcode2ds;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.rscja.barcode.BarcodeDecoder;
import com.rscja.barcode.BarcodeFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    AutoCompleteTextView recordersACTV, timeACTV;
    TextView dateTextView, resultTextView;
    Button button2, button3, button4, button5, button8;
    LinearLayout scrollLinearLayout;
    DateHandler dateHandler;

    EditText editTextText2;
    String TAG = "MainActivity_2D";
    BarcodeDecoder barcodeDecoder = BarcodeFactory.getInstance().getBarcodeDecoder();
    QRcode qrCode;
    RFID rfid;
    private Tagpoint tagpoint;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        dateTextView = findViewById(R.id.textview_date);
        dateHandler = new DateHandler(this, dateTextView);

        recordersACTV = findViewById(R.id.ACTV_recorders);
        RecorderFetcher.fetchRecorders(this, new RecorderFetcher.RecorderFetchListener() {
            @Override
            public void onFetchComplete(final HashMap<String, String> recordersMap) {
                ArrayList<String> recordersList = new ArrayList<>(recordersMap.values());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, recordersList);
                recordersACTV.setAdapter(adapter);
                recordersACTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recordersACTV.showDropDown();
                    }
                });
            }

            @Override
            public void onFetchFailed(Exception e) {
                e.printStackTrace();
            }
        });

        timeACTV = findViewById(R.id.ACTV_time);
        TimeHandler.setupTimeAutoCompleteTextView(this, timeACTV);

        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button8 = findViewById(R.id.button8);

        scrollLinearLayout = findViewById(R.id.scrollLinearLayout);
        tagpoint = new Tagpoint(this, scrollLinearLayout);

        AnimationHandler.setButtonAnimation(button2);
        AnimationHandler.setButtonAnimation(button3);
        AnimationHandler.setButtonAnimation(button4);
        AnimationHandler.setButtonAnimation(button5);
        AnimationHandler.setButtonAnimation(button8);

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sync.syncData(MainActivity.this, dateTextView, timeACTV, recordersACTV, scrollLinearLayout);
            }
        });

        editTextText2 = findViewById(R.id.editTextText2);
        qrCode = new QRcode(this, barcodeDecoder, editTextText2);

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrCode.start();
            }
        });

        resultTextView = findViewById(R.id.TextView);
        rfid = new RFID(this, resultTextView, button8);
        rfid.setOnRFIDScannedListener(new RFID.OnRFIDScannedListener() {
            @Override
            public void onRFIDScanned(String rfidCode) {
                tagpoint.processRFIDCode(rfidCode);
            }
        });

        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rfid.startScan();
            }
        });

        new InitTask().execute();
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