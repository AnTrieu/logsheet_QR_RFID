//package com.example.barcode2ds;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import com.example.rfidlibrary.RFIDScanner; // Import lớp từ SDK RFID
//
//public class RFID extends Activity {
//    private Button button8;
//    private TextView textView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main); // Đảm bảo rằng layout của bạn có tên chính xác
//
//        button8 = findViewById(R.id.button8);
//        textView = findViewById(R.id.TextView);
//
//        button8.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startRFIDScan(); // Bắt đầu quét RFID
//            }
//        });
//    }
//
//    private void startRFIDScan() {
//        RFIDScanner scanner = new RFIDScanner(); // Tạo một đối tượng RFIDScanner từ SDK
//        scanner.startScan(new RFIDScanner.ScanCallback() {
//            @Override
//            public void onScanResult(String result) {
//                textView.setText(result); // Đặt kết quả quét vào TextView
//            }
//        });
//    }
//}
