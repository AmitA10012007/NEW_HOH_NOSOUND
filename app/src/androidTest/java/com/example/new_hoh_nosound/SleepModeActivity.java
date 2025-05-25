package com.example.new_hoh_nosound;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class SleepModeActivity extends AppCompatActivity {
    private static final String TAG = "SleepModeActivity";

    private static final int BLE_PERMISSION_REQUEST_CODE = 100;
    private static final int CALL_PHONE_PERMISSION_REQUEST_CODE = 101;
    private static final int HEART_RATE_BUFFER_SIZE = 60; // readings (1 per second)
    private static final float EMERGENCY_THRESHOLD_MULTIPLIER = 1.15f;
    private static final long TTS_DELAY_BEFORE_BEEP_MS = 8000;
    private static final int BEEP_INTERVAL_MS = 600;
    private static final int MAX_BEEPS = 200; // 2 minutes at 100BPM

    private TextView txtSleepMsg;
    private Button btnBack;
    private BLEManager bleManager;
    private Queue<Integer> heartRateBuffer = new LinkedList<>();
    private Handler mainHandler = new Handler();
    private boolean isAlertTriggered = false;

    private String username, emergencyPhoneNumber;
    private float baselineHeartRate;

    private TextToSpeech tts;
    private boolean isTtsInitialized = false;
    private ToneGenerator toneGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleepmode);

        username = SharedPrefManager.getUsername(this);
        emergencyPhoneNumber = SharedPrefManager.getPhone(this);
        baselineHeartRate = SharedPrefManager.getBaseline(this);

        txtSleepMsg = findViewById(R.id.txtSleepMsg);
        txtSleepMsg.setText("Good night " + username + ". Don’t worry – we’ve got a hand on your heart.");
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Initialize TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true;
                tts.setLanguage(Locale.US);
            }
        });

        // Check and request BLE permissions, then start BLE scan
        if (checkAndRequestBlePermissions()) {
            startBleScan();
        }

        // Check and request CALL_PHONE
        checkAndRequestCallPermission();
    }

    private boolean checkAndRequestBlePermissions() {
        String[] blePermissions = {
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
        };
        boolean allGranted = true;
        for (String perm : blePermissions) {
            if (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (!allGranted) {
            ActivityCompat.requestPermissions(this, blePermissions, BLE_PERMISSION_REQUEST_CODE);
        }
        return allGranted;
    }

    private void checkAndRequestCallPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE_PERMISSION_REQUEST_CODE);
        }
    }

    private void startBleScan() {
        bleManager = new BLEManager(this, heartRate -> {
            if (isAlertTriggered) return;
            runOnUiThread(() -> {
                heartRateBuffer.add(heartRate);
                if (heartRateBuffer.size() > HEART_RATE_BUFFER_SIZE) heartRateBuffer.poll();
                checkForEmergency();
            });
        });
        bleManager.startScan();
    }

    private void checkForEmergency() {
        if (heartRateBuffer.size() < HEART_RATE_BUFFER_SIZE) return;
        float sum = 0;
        for (int hr : heartRateBuffer) sum += hr;
        float avg = sum / heartRateBuffer.size();
        if (avg > baselineHeartRate * EMERGENCY_THRESHOLD_MULTIPLIER && !isAlertTriggered) {
            isAlertTriggered = true;
            mainHandler.post(this::triggerEmergency);
        }
    }

    private void triggerEmergency() {
        txtSleepMsg.setText("EMERGENCY DETECTED! Contacting help for " + username);

        // 1. Call the emergency number
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + emergencyPhoneNumber));
            try {
                startActivity(callIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Could not start emergency call.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "CALL_PHONE permission denied. Cannot call.", Toast.LENGTH_LONG).show();
        }

        // 2. TTS message
        String ttsMsg = "This is an emergency alert. The user " + username + " is experiencing a cardiac event. Please call an ambulance and begin first aid if you are certified.";
        if (isTtsInitialized && tts != null) {
            tts.speak(ttsMsg, TextToSpeech.QUEUE_FLUSH, null, "tts1");
            mainHandler.postDelayed(this::playBeep, TTS_DELAY_BEFORE_BEEP_MS);
        } else {
            playBeep();
        }
    }

    private void playBeep() {
        if (toneGenerator != null) toneGenerator.release();
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        mainHandler.post(new Runnable() {
            int count = 0;
            @Override
            public void run() {
                if (count < MAX_BEEPS) {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
                    mainHandler.postDelayed(this, BEEP_INTERVAL_MS);
                    count++;
                } else {
                    toneGenerator.release();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLE_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) if (result != PackageManager.PERMISSION_GRANTED) allGranted = false;
            if (allGranted) startBleScan();
            else Toast.makeText(this, "Bluetooth permissions required for sleep mode.", Toast.LENGTH_LONG).show();
        }
        else if (requestCode == CALL_PHONE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Emergency call feature will not work without CALL_PHONE permission.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleManager != null) bleManager.stopScan();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
        mainHandler.removeCallbacksAndMessages(null);
    }
}