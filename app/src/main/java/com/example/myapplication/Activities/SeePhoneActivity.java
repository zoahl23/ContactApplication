package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.PhoneNumber;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class SeePhoneActivity extends AppCompatActivity {
    ImageView imgAvtS, imgQr;
    TextView tvNameS, tvSdtS;
    Button btnQuayLai, btnTaoQr, btnLuuQr;
    Bitmap bitmapQrImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_phone);

        imgAvtS = findViewById(R.id.imgAvtS);
        tvNameS = findViewById(R.id.tvNameS);
        tvSdtS = findViewById(R.id.tvSdtS);
        btnQuayLai = findViewById(R.id.btnQuayLai);

        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        PhoneNumber pn = (PhoneNumber) data.get("pn_value");

        // lấy địa chỉ ảnh trên firebase = "http://...." kiểu vậy
        String imageUri = String.valueOf(pn.getAvt());
        // hiển thị ảnh
        Glide.with(this).load(imageUri).into(imgAvtS);
        tvNameS.setText(pn.getTen());
        tvSdtS.setText(pn.getSdt());

        btnLuuQr = findViewById(R.id.btnLuuQr);
        btnTaoQr = findViewById(R.id.btnTaoQr);
        imgQr = findViewById(R.id.imgQr);

        btnTaoQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pn.getSdt().length() > 0) {
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    try {
                        BitMatrix bitMatrix = multiFormatWriter.encode(pn.getSdt(), BarcodeFormat.QR_CODE, 150, 150);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        bitmapQrImage = barcodeEncoder.createBitmap(bitMatrix);
                        imgQr.setImageBitmap(bitmapQrImage);
                    } catch (Exception e) {
                        Log.d("Lỗi tạo QR: ", e.toString());
                        Toast.makeText(SeePhoneActivity.this, "Lỗi tạo QR: " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnLuuQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImg();
            }
        });

        btnQuayLai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    protected void saveImg() {
        Uri img;
        ContentResolver contentResolver = getContentResolver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            img = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }
        else  {
            img = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        // trả về ngày giờ hiện tại và lưu dưới dạng phía dưới
        String timeStamp = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(new Date());
        ContentValues contentValues = new ContentValues();
        // tên ảnh
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, timeStamp + ".jpg");
        // kiểu file
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");
        // cầu
        Uri uri = contentResolver.insert(img, contentValues);
        try {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) imgQr.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);
            Toast.makeText(this, "Đã lưu ảnh " + timeStamp + ".jpg", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.d("Lỗi lưu ảnh", e.toString());
            Toast.makeText(SeePhoneActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}