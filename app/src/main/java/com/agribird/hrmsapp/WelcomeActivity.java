package com.agribird.hrmsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        sharedPreferences = getSharedPreferences("HRMS_SESSION", MODE_PRIVATE);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
                boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
                String userName = sharedPreferences.getString("userName", "");
                String userRole = sharedPreferences.getString("userRole", "");
                String userEmail = sharedPreferences.getString("userEmail", "");

                if (isLoggedIn && rememberMe) {
                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    intent.putExtra("Name", userName);
                    intent.putExtra("Role", userRole);
                    intent.putExtra("Email", userEmail);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

                finish();
            }
        }, 3000);
    }
}