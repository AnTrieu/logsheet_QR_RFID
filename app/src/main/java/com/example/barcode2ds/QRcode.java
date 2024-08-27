package com.example.barcode2ds;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;

import com.rscja.barcode.BarcodeDecoder;
import com.rscja.deviceapi.entity.BarcodeEntity;

public class QRcode {
    private Context context;
    private BarcodeDecoder barcodeDecoder;
    private EditText editTextText2;
    private String TAG = "QRcode_2D";

    public QRcode(Context context, BarcodeDecoder barcodeDecoder, EditText editTextText2) {
        this.context = context;
        this.barcodeDecoder = barcodeDecoder;
        this.editTextText2 = editTextText2;
        setDecodeCallback();
    }

    private void setDecodeCallback() {
        barcodeDecoder.setDecodeCallback(new BarcodeDecoder.DecodeCallback() {
            @Override
            public void onDecodeComplete(BarcodeEntity barcodeEntity) {
                Log.e(TAG, "BarcodeDecoder==========================:" + barcodeEntity.getResultCode());
                if (barcodeEntity.getResultCode() == BarcodeDecoder.DECODE_SUCCESS) {
                    editTextText2.setText(barcodeEntity.getBarcodeData());
                    Log.e(TAG, "data==========================:" + barcodeEntity.getBarcodeData());
                    stop();
                } else {
                    editTextText2.setText("fail");
                }
            }
        });
    }

    public void start() {
        barcodeDecoder.startScan();
    }

    public void stop() {
        barcodeDecoder.stopScan();
    }

    public void startScan() {
        start();
    }
}
