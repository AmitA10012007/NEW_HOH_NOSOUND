package com.example.heartapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Button btnSetup = findViewById(R.id.btnSetup);
        Button btnSleep = findViewById(R.id.btnSleep);
        btnSetup.setOnClickListener(v -> startActivity(new Intent(this, SetupActivity.class)));
        btnSleep.setOnClickListener(v -> startActivity(new Intent(this, SleepModeActivity.class)));
    }
}