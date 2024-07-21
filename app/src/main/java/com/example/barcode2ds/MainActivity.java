package com.example.barcode2ds;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.rscja.barcode.BarcodeDecoder;
import com.rscja.barcode.BarcodeFactory;
import com.rscja.deviceapi.entity.BarcodeEntity;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Spinner timeSpinner;
    Spinner recordersSpinner;
    TextView dateTextView;
    Calendar calendar;
    Button button2, button3, button4, button5, button8;

    String TAG = "MainActivity_2D";
    BarcodeDecoder barcodeDecoder = BarcodeFactory.getInstance().getBarcodeDecoder();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Recorders Spinner setup
        recordersSpinner = findViewById(R.id.spinner_recorders);
        new LoadRecordersTask().execute();

        new InitTask().execute();

        // Animation setup
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button8 = findViewById(R.id.button8);

        setButtonAnimation(button2);
        setButtonAnimation(button3);
        setButtonAnimation(button4);
        setButtonAnimation(button5);
        setButtonAnimation(button8);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        close();
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setButtonAnimation(final Button button) {
        final int normalColor = button.getBackgroundTintList().getDefaultColor();
        final int pressedColor = adjustColorBrightness(normalColor, -0.2f);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        button.setScaleX(0.9f);
                        button.setScaleY(0.9f);
                        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(pressedColor));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        button.setScaleX(1.0f);
                        button.setScaleY(1.0f);
                        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(normalColor));
                        break;
                }
                return false;
            }
        });
    }

    private int adjustColorBrightness(int color, float factor) {
        int a = android.graphics.Color.alpha(color);
        int r = Math.round(android.graphics.Color.red(color) * (1 + factor));
        int g = Math.round(android.graphics.Color.green(color) * (1 + factor));
        int b = Math.round(android.graphics.Color.blue(color) * (1 + factor));
        return android.graphics.Color.argb(a, Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }

    private void open() {
        barcodeDecoder.open(this);
        Log.e(TAG, "open()==========================:" + barcodeDecoder.open(this));
        barcodeDecoder.setDecodeCallback(new BarcodeDecoder.DecodeCallback() {
            @Override
            public void onDecodeComplete(BarcodeEntity barcodeEntity) {
                Log.e(TAG, "BarcodeDecoder==========================:" + barcodeEntity.getResultCode());
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

    private class LoadRecordersTask extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> recordersList = new ArrayList<>();
            // Giả sử bạn có URL của máy chủ
            String urlString = "http://example.com/api/recorders";

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                // Giả sử kết quả trả về là một JSON Array
                JSONArray jsonArray = new JSONArray(result.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    recordersList.add(jsonArray.getString(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return recordersList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> recordersList) {
            super.onPostExecute(recordersList);
            if (recordersList != null && !recordersList.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, recordersList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                recordersSpinner.setAdapter(adapter);
            } else {
                // Xử lý khi không có dữ liệu
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, new String[]{"Người ghi ⭣"});
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                recordersSpinner.setAdapter(adapter);
            }
        }
    }
}
