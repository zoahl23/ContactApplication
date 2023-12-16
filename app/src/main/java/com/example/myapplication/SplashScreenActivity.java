package com.example.myapplication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreenActivity extends AppCompatActivity {

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // lấy action bar
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            // ẩn
            actionBar.hide();
        }

        // delay để hiển thị
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                // chuyển từ trang hiện tại sang main
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);

                // khỏi tạo quá trình chuyển đến
                startActivity(intent);

                finish();
            }
        }, 5000); // delay 5s

    }
}