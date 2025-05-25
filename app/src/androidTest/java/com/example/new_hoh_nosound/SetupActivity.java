package com.example.new_hoh_nosound;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class SetupActivity extends AppCompatActivity {
    private EditText edtUsername, edtPhone;
    private Button btnMeasure;
    private TextView txtInstruction, txtResult;

    private List<Integer> heartRates = new ArrayList<>();
    private Handler handler = new Handler();
    private BLEManager bleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        edtUsername = findViewById(R.id.edtUsername);
        edtPhone = findViewById(R.id.edtPhone);
        btnMeasure = findViewById(R.id.btnMeasure);
        txtInstruction = findViewById(R.id.txtInstruction);
        txtResult = findViewById(R.id.txtResult);
        txtInstruction.setText("Start this measurement only while resting. Please breathe calmly and preferably sit down.");

        bleManager = new BLEManager(this, data -> {
            // Callback: Add received heart rate sample
            heartRates.add(data);
        });

        btnMeasure.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            if (username.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPrefManager.saveUserInfo(this, username, phone);
            heartRates.clear();
            txtResult.setText("Measuring...");
            bleManager.startScan();
            handler.postDelayed(() -> {
                bleManager.stopScan();
                float avg = 0f;
                if (!heartRates.isEmpty()) {
                    int sum = 0;
                    for (int hr : heartRates) sum += hr;
                    avg = (float) sum / heartRates.size();
                }
                SharedPrefManager.saveBaseline(this, avg);
                txtResult.setText("Baseline set to: " + avg + " BPM");
            }, 15000); // 15 seconds
        });
    }
}