package com.example.barcode2ds;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rscja.barcode.BarcodeDecoder;
import com.rscja.barcode.BarcodeFactory;
import com.rscja.deviceapi.entity.BarcodeEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnScan;
    Button btnStop;
    TextView tvData;
    Spinner timeSpinner;
    TextView dateTextView;
    Calendar calendar;

    String TAG = "MainActivity_2D";
    BarcodeDecoder barcodeDecoder = BarcodeFactory.getInstance().getBarcodeDecoder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScan = findViewById(R.id.btnScan);
        tvData = findViewById(R.id.tvData);
        btnStop = findViewById(R.id.btnStop);
        btnScan.setOnClickListener(this);
        btnStop.setOnClickListener(this);

        // Spinner setup
        timeSpinner = findViewById(R.id.spinner_time);
        ArrayList<String> timeList = new ArrayList<>();
        for (int i = 2; i <= 24; i += 2) {
            timeList.add(i + ":00");
        }
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeList);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);

        // DatePicker setup
        dateTextView = findViewById(R.id.textview_date);
        calendar = Calendar.getInstance();
        updateDateLabel();

        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, date, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        new InitTask().execute();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        close();
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnScan:
                start();
                break;
            case R.id.btnStop:
                stop();
                break;
        }
    }

    private void start() {
        barcodeDecoder.startScan();
    }

    private void stop() {
        barcodeDecoder.stopScan();
    }

    private void open() {
        barcodeDecoder.open(this);
        Log.e(TAG, "open()==========================:" + barcodeDecoder.open(this));
        barcodeDecoder.setDecodeCallback(new BarcodeDecoder.DecodeCallback() {
            @Override
            public void onDecodeComplete(BarcodeEntity barcodeEntity) {
                Log.e(TAG, "BarcodeDecoder==========================:" + barcodeEntity.getResultCode());
                if (barcodeEntity.getResultCode() == BarcodeDecoder.DECODE_SUCCESS) {
                    tvData.setText("data:" + barcodeEntity.getBarcodeData());
                    Log.e(TAG, "data==========================:" + barcodeEntity.getBarcodeData());
                } else {
                    tvData.setText("fail");
                }
            }
        });
    }

    private void close() {
        barcodeDecoder.close();
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

    private void updateDateLabel() {
        String myFormat = "dd/MM/yyyy"; // In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dateTextView.setText(sdf.format(calendar.getTime()));
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel();
        }
    };
}
