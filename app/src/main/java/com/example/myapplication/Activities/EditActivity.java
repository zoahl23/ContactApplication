package com.example.myapplication.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.Database.SQLiteConnect;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.PhoneNumber;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class EditActivity extends AppCompatActivity {
    EditText suaEdtTen, suaEdtSdt, suaEdtMail;
    Button btnSua, btnHuySua;
    TextView tieuDeSuaTT;
    // Ánh xạ giao diện tải ảnh lên
    ImageView imgDoiAnh;
    Button btnDoiAnh;
    // khi chụp xong sẽ cập nhật tự động vào đường dẫn này
    String currenPhotoPath;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    //lấy toàn bộ key của database
    DatabaseReference get_key = database.getReference();
    //trỏ vào key cụ thể ở đây là key contact để lấy data trong key đó
    DatabaseReference contactsRef=get_key.child("contact");
    FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();
    StorageReference storageReference=firebaseStorage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        suaEdtSdt = findViewById(R.id.suaEdtSdt);
        suaEdtTen = findViewById(R.id.suaEdtTen);
        suaEdtMail = findViewById(R.id.suaEdtMail);
        btnSua = findViewById(R.id.btnSua);
        btnHuySua = findViewById(R.id.btnHuySua);
        imgDoiAnh = findViewById(R.id.imgDoiAnh);
        btnDoiAnh = findViewById(R.id.btnDoiAnh);
        tieuDeSuaTT=findViewById(R.id.tvTieuDeSuaTT);
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        //get khóa chính của csdl để sử dụng các thông tin chính xác cho user đó
        String key=data.getString("key");


        contactsRef.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PhoneNumber phoneNumber=snapshot.getValue(PhoneNumber.class);
                tieuDeSuaTT.setText("Sửa Liên Hệ\n"+phoneNumber.getTen()+" - "+phoneNumber.getSdt());
                suaEdtTen.setText(phoneNumber.getTen());
                suaEdtSdt.setText(phoneNumber.getSdt());
                suaEdtMail.setText(phoneNumber.getMail());
                Glide.with(getBaseContext()).load(phoneNumber.getAvt()).into(imgDoiAnh);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ActivityResultLauncher chonAnhLauncher= registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        imgDoiAnh.setImageURI(result);
                    }
                }
        );
        //chọn ảnh trong album
        btnDoiAnh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chonAnhLauncher.launch("image/*");
            }
        });
        //chức năng giống hệt bên thêm user vào list danh bạ (Trong mục AddPhoneActivity)
        btnSua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ten = suaEdtTen.getText().toString().trim();
                String sdt = suaEdtSdt.getText().toString().trim();
                String mail = suaEdtMail.getText().toString().trim();
                if(ten.length()>0&&sdt.length()>0&&mail.length()>0){

                    PhoneNumber phoneNumber=new PhoneNumber(key,ten,sdt,"",mail);
                    StorageReference anhDaiDienRef=storageReference.child("avt").child(key + ".jpg");
                    BitmapDrawable bitmapDrawable=(BitmapDrawable) imgDoiAnh.getDrawable();
                    Bitmap bitmap=bitmapDrawable.getBitmap();
                    ByteArrayOutputStream baoStream= new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,baoStream);
                    byte[] imageData=baoStream.toByteArray();
                    anhDaiDienRef.putBytes(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            anhDaiDienRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String anhDaiDien=uri.toString();
                                    phoneNumber.setAvt(anhDaiDien);
                                    contactsRef.child(key).setValue(phoneNumber).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(EditActivity.this,"Sửa Liên Hệ Thành Công!",Toast.LENGTH_SHORT).show();
                                                makeNotification("Thông báo", "Cập nhập thành công");
                                            }
                                        }
                                    });
                                }
                            });
                            finish();
                        }
                    });
                }
            }
        });

    }

    // hàm hiển thị thông báo
    protected void makeNotification(String title, String text) {
        String chanelId = "CHANNEL_ID_NOTIFICATION"; // xác định kênh thông báo
        NotificationCompat.Builder buider = new NotificationCompat.Builder(getApplicationContext(), chanelId); // xây dựng thông báo
        buider.setSmallIcon(R.drawable.ic_notifications) // biểu tượng thông báo
                .setContentTitle(title) // tiêu đề thông báo
                .setContentText(text)  // nội dung thông báo
                .setAutoCancel(true)  // tự động mất nếu chạm vào thông báo
                .setPriority(NotificationCompat.PRIORITY_DEFAULT); // ưu tiên thông báo

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // cờ này để xác định thông báo đã tồn tại trên đỉnh chưa

        // hành động sẽ xảy ra trong tương lai, tức là mở thông báo (notificationActivity) khi kích hoạt
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, intent, PendingIntent.FLAG_MUTABLE);
        buider.setContentIntent(pendingIntent);
        // hiển thị thông báo, Android 8.0 trở lên mới hiển thị
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // kiểm tra thông báo đã tồn tại chưa
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    notificationManager.getNotificationChannel(chanelId);
            if (notificationChannel == null) {
                // nếu chưa tồn tại thì khởi tạo 1 thông báo với mức độ ưu tiên cao
                int importance = NotificationManager.IMPORTANCE_HIGH;
                // mô tả
                notificationChannel = new NotificationChannel(chanelId, "Mô tả", importance);
                notificationChannel.enableVibration(true); // bật chế độ rung cho thông báo
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        // hiển thị thông báo đã được xây dựng
        notificationManager.notify(0, buider.build());
    }
}