package com.example.myapplication.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.myapplication.Database.SQLiteConnect;
import com.example.myapplication.MainActivity;
import com.example.myapplication.NotificationActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapters.PhoneAdapter;
import com.example.myapplication.model.PhoneNumber;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class AddPhoneActivity extends AppCompatActivity {
    EditText addEdtTen, addEdtSdt, addEdtMail;
    Button btnThemMoi, btnHuyThem, btnTaiQr, btnQuetQr;

    // Ánh xạ giao diện tải ảnh lên
    ImageView imgTaiAnh;
    Button btnTaiAnh;
    // khi chụp xong sẽ cập nhật tự động vào đường dẫn này
    String currenPhotoPath;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    //lấy toàn bộ key của database
    DatabaseReference get_key = database.getReference();
    //trỏ vào key cụ thể ở đây là key contact để lấy data trong key đó
    DatabaseReference contactsRef=get_key.child("contact");
    FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();
    StorageReference storageReference=firebaseStorage.getReference();


    ActivityResultLauncher scanFromImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(result);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int[] pixels = new int[width * height];
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                    bitmap.recycle();
                    RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                    BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                    MultiFormatReader reader = new MultiFormatReader();
                    Result readResult = reader.decode(binaryBitmap);
                    Toast.makeText(AddPhoneActivity.this, readResult.toString(), Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddPhoneActivity.this);
                    builder.setTitle("Số điện thoại vừa quét được là: ");
                    builder.setMessage(readResult.toString());
                    builder.setPositiveButton("Copy to clipbroad", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText("qr_content", readResult.toString());
                            clipboardManager.setPrimaryClip(clipData);
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                }
            } catch (Exception e) {
                Toast.makeText(AddPhoneActivity.this, "Lỗi: " + e.toString(), Toast.LENGTH_SHORT ).show();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_phone);

        addEdtTen = findViewById(R.id.addEdtTen);
        addEdtSdt = findViewById(R.id.addEdtSdt);
        addEdtMail = findViewById(R.id.addEdtMail);
        btnThemMoi = findViewById(R.id.btnThemMoi);
        btnHuyThem = findViewById(R.id.btnHuyThem);
        imgTaiAnh = findViewById(R.id.imgTaiAnh);
        btnTaiAnh = findViewById(R.id.btnTaiAnh);
        btnTaiQr = findViewById(R.id.btnTaiQr);
        btnQuetQr = findViewById(R.id.btnQuetQr);

        // kiểm tra phiên bản của máy đang chạy có đủ cao không, yêu cầu Android 8.0 trở lên mới hiển thị
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // kiểm tra ứng dụng đã được cấp quyền thông báo chưa
            if (ContextCompat.checkSelfPermission(AddPhoneActivity.this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                // nếu quyền chưa được cấp, yêu cầu quyền
                ActivityCompat.requestPermissions(AddPhoneActivity.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                // mảng chứa các yêu cầu quyền
            }
        }

        ActivityResultLauncher chonAnh=registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        imgTaiAnh.setImageURI(result);
                        Log.d("Image URI 2", String.valueOf(result));
                    }
                }
        );
        btnTaiQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanFromImageLauncher.launch("image/*");
            }
        });

        btnQuetQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanCodeFromCamera();
            }
        });

        btnTaiAnh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chonAnh.launch("image/*");
            }
        });
        btnThemMoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ten = addEdtTen.getText().toString().trim();
                String sdt = addEdtSdt.getText().toString().trim();
                String mail = addEdtMail.getText().toString().trim();
                if(ten.length()>0 && sdt.length()>0 && mail.length()>0){
                    //tạo ra 1 khóa chính ngẫu nhiên cho từng user nhưng phải xóa các kí tự đặc biệt vì firebase k cho phép
                    String key=contactsRef.push().getKey().toString().replace("-", "");
                    PhoneNumber phoneNumber=new PhoneNumber(key,ten,sdt,"",mail);
                    //đặt tên ảnh trùng với key
                    StorageReference anhDaiDienRef=storageReference.child("avt").child(key + ".jpg");
                    //từ đoạn này là nén ảnh để đẩy lên trên firebase
                    BitmapDrawable bitmapDrawable=(BitmapDrawable) imgTaiAnh.getDrawable();
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
                                    //vì key tên số điện thoại đã được add sẵn ở trên nên ở đây mình chỉ cần add link ảnh là sẽ tự động lưu toàn bộ data của user lên firebase
                                    phoneNumber.setAvt(anhDaiDien);
                                    contactsRef.child(key).setValue(phoneNumber).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(AddPhoneActivity.this,"Thêm Liên Hệ Thành Công!",Toast.LENGTH_SHORT).show();
                                                makeNotification("Thông báo", "Bạn vừa thêm thành công một liên hệ mới");
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

        btnHuyThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    ActivityResultLauncher scanCodeFromCameraLauncher = registerForActivityResult(new ScanContract(), new ActivityResultCallback<ScanIntentResult>() {
        @Override
        public void onActivityResult(ScanIntentResult result) {
            if (result.getContents().length() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddPhoneActivity.this);
                builder.setTitle("Số điện thoại vừa quét được là: ");
                builder.setMessage(result.getContents());
                builder.setPositiveButton("Sao chép", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("qr_content", result.getContents());
                        // đưa vào bộ nhớ đệm
                        clipboardManager.setPrimaryClip(clipData);
                        // nhấn xong thì ẩn cửa sổ
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        }
    });

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
    protected void scanCodeFromCamera() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Nhấn nút tăng âm lượng để bật flash");
        // tức khi quét sẽ tạo tiếng kêu beep, để false là ko kêu
        options.setBeepEnabled(false);
        // tự động xoay hướng khi xoay màn hình
        options.setOrientationLocked(false);
        //
        options.setCaptureActivity(MyCaptureActivity.class);
        scanCodeFromCameraLauncher.launch(options);
    }
}