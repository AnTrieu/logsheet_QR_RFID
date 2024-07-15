package com.example.barcode2ds;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import android.os.Bundle;
import android.util.Base64;
import android.util.DebugUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.barcode.BarcodeDecoder;
import com.rscja.barcode.BarcodeFactory;
import com.rscja.barcode.BarcodeUtility;
import com.rscja.deviceapi.entity.BarcodeEntity;
 


public class MainActivity extends Activity implements View.OnClickListener {
        Button btnScan;
        Button btnStop;
        TextView tvData;
        String TAG="MainActivity_2D";
        BarcodeDecoder barcodeDecoder= BarcodeFactory.getInstance().getBarcodeDecoder();


    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            /*btnScan=(Button)findViewById(R.id.btnScan);
            tvData=(TextView)findViewById(R.id.tvData);
            btnStop=(Button)findViewById(R.id.btnStop);
            btnScan.setOnClickListener(this);
            btnStop.setOnClickListener(this);
            new InitTask().execute();

        }
*/
        @Override
        protected void onDestroy() {
            Log.i(TAG,"onDestroy");
            close();
            super.onDestroy();
            android.os.Process.killProcess(Process.myPid());
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
               case  R.id.btnScan:
                   start();
                break;
                case  R.id.btnStop:
                    stop();
                break;
            }
        }


    public class InitTask extends AsyncTask<String, Integer, Boolean> {
            ProgressDialog mypDialog;
            @Override
            protected Boolean doInBackground(String... params) {
                // TODO Auto-generated method stub
                open();
                Log.e(TAG,"doInBackground==========================:");
                return true;
            }
            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                mypDialog.cancel();
            }
            @Override
            protected void onPreExecute() {
                // TODO Auto-generated method stub
                super.onPreExecute();
                mypDialog = new ProgressDialog(MainActivity.this);
                mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mypDialog.setMessage("init...");
                mypDialog.setCanceledOnTouchOutside(false);
                mypDialog.setCancelable(false);
                mypDialog.show();
            }
        }

        private void start(){
            barcodeDecoder.startScan();
        }
        private void stop(){
            barcodeDecoder.stopScan();
        }
        private void open(){
            barcodeDecoder.open(this);
            Log.e(TAG,"open()==========================:"+ barcodeDecoder.open(this));
           /* todo success Sound
            BarcodeUtility.getInstance().enablePlaySuccessSound(this,true);
            */
            barcodeDecoder.setDecodeCallback(new BarcodeDecoder.DecodeCallback() {
                @Override
                public void onDecodeComplete(BarcodeEntity barcodeEntity) {
                    Log.e(TAG,"BarcodeDecoder==========================:"+barcodeEntity.getResultCode());
                    if(barcodeEntity.getResultCode() == BarcodeDecoder.DECODE_SUCCESS){
                        tvData.setText("data:"+barcodeEntity.getBarcodeData());
                        Log.e(TAG,"data==========================:"+barcodeEntity.getBarcodeData());
                    }else{
                        tvData.setText("fail");
                    }
                }
            });

        }
        private void close(){
            barcodeDecoder.close();
        }


}
