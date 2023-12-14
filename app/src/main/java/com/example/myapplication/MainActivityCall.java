package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivityCall extends AppCompatActivity {
    Button btnHuy;
    ImageView imgAvtC;
    TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_call);
        imgAvtC = findViewById(R.id.imgAvtC);
        tvName = findViewById(R.id.tvName);
        btnHuy = findViewById(R.id.btnHuy);

        Intent intent = getIntent(); // lấy intent ở main trước
        Bundle data = intent.getExtras(); // lấy dữ liệu
        int a = data.getInt("avtCall");
        String b = data.getString("tenCall");

        tvName.setText(b); // cài vào file này
        imgAvtC.setImageResource(a);


        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivityCall.this, "Đã hủy", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


}