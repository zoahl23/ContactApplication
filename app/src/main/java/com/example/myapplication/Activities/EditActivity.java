package com.example.myapplication.Activities;

import static android.Manifest.permission.CAMERA;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
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
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.bumptech.glide.Glide;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.PhoneNumber;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.util.regex.Pattern;

public class EditActivity extends AppCompatActivity {
    private EditText suaEdtTen, suaEdtSdt, suaEdtMail;
    private FloatingActionButton btnSua, btnScanQrU;
    private CircularImageView imgDoiAnh;
    private ActionBar actionBar;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    //lấy toàn bộ key của database
    private DatabaseReference get_key = database.getReference();
    //trỏ vào key cụ thể ở đây là key contact để lấy data trong key đó
    private DatabaseReference contactsRef=get_key.child("contact");
    private FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();
    private StorageReference storageReference=firebaseStorage.getReference();

    private static final int CAMERA_REQUEST = 1;

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
                    Toast.makeText(EditActivity.this, readResult.toString(), Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
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
                Toast.makeText(EditActivity.this, "Lỗi: " + e.toString(), Toast.LENGTH_SHORT ).show();
            }
        }
    });

    ActivityResultLauncher scanCodeFromCameraLauncher = registerForActivityResult(new ScanContract(), new ActivityResultCallback<ScanIntentResult>() {
        @Override
        public void onActivityResult(ScanIntentResult result) {
            if (result.getContents().length() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // lấy action bar
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            // tiêu đề
            actionBar.setTitle("Edit Contact");
            // hiển thị nút back (có hàm xử lý phía dưới)
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        suaEdtSdt = findViewById(R.id.suaEdtSdt);
        suaEdtTen = findViewById(R.id.suaEdtTen);
        suaEdtMail = findViewById(R.id.suaEdtMail);
        btnSua = findViewById(R.id.btnSua);
        btnScanQrU = findViewById(R.id.btnScanQrU);
        imgDoiAnh = findViewById(R.id.imgDoiAnh);
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        //get khóa chính của csdl để sử dụng các thông tin chính xác cho user đó
        String key=data.getString("key");


        contactsRef.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PhoneNumber phoneNumber=snapshot.getValue(PhoneNumber.class);
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
        imgDoiAnh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                builder.setTitle("Chọn ảnh");

                String[] options = {"Chụp ảnh mới", "Tải ảnh lên"};

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // tạm thời chưa làm được
                            // Chụp ảnh mới
                            if(ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED){
                                //permisson granted
                                //continue the action
                                Intent cameraItent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(cameraItent, CAMERA_REQUEST);
                            }else {
                                //permession not granted
                                // ask for the permisson
                                ActivityCompat.requestPermissions(EditActivity.this, new String[]{CAMERA}, 1);
//                                Toast.makeText(AddPhoneActivity.this, "Bạn chưa cấp quyền truy cập camera", Toast.LENGTH_SHORT).show();
                            }
//                            if (!checkCameraPermission()) {
//                                // ko cấp quyền truy cập camera
//                            }
//                            else {
//                                pickFromCamera();
//                            }
                        } else if (which == 1) {
                            // Tải ảnh từ thư viện
//                            chonAnh.launch("image/*");
                            chonAnhLauncher.launch("image/*");
//                            if (!checkStoragePermission()) {
//                                // ko cho truy cập vào ảnh
//                                Toast.makeText(EditActivity.this, "Bạn chưa cấp quyền truy cập ảnh", Toast.LENGTH_SHORT).show();
//                            }
//                            else {
//                                chonAnhLauncher.launch("image/*");
//                            }
                        }
                    }
                });

                builder.show();
            }
        });

        // scan qr
        btnScanQrU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                builder.setTitle("Chọn tùy chọn QR");
                String[] options = {"Tải ảnh QR", "Quét QR"};

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Tải ảnh QR
                            scanFromImageLauncher.launch("image/*");
                        }
                        else if (which == 1) {
                            // Quét QR
                            scanCodeFromCamera();
                        }
                    }
                });

                builder.show();
            }
        });

        //chức năng giống hệt bên thêm user vào list danh bạ (Trong mục AddPhoneActivity)
        btnSua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ten = suaEdtTen.getText().toString().trim();
                String sdt = suaEdtSdt.getText().toString().trim();
                String mail = suaEdtMail.getText().toString().trim();
                // Kiểm tra chuỗi email
                String emailPattern = "^[^\\s@]+@[^\\s@]+\\.com$";
                if (ten.length() == 0) {
                    Toast.makeText(EditActivity.this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
                }
                else if (sdt.length() != 10 || !sdt.startsWith("0")) {
                    Toast.makeText(EditActivity.this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
                }
                else if (!Pattern.matches(emailPattern, mail)) {
                    Toast.makeText(EditActivity.this, "Mail không hợp lệ", Toast.LENGTH_SHORT).show();
                }
                else {
                    PhoneNumber phoneNumber=new PhoneNumber(key,ten,sdt,"",mail);
                    StorageReference anhDaiDienRef=storageReference.child("avt").child(key + ".jpg");
                    BitmapDrawable bitmapDrawable=(BitmapDrawable) imgDoiAnh.getDrawable();
                    Bitmap bitmap=bitmapDrawable.getBitmap();
                    ByteArrayOutputStream baoStream= new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,baoStream);
                    byte[] imageData=baoStream.toByteArray();
                    if (bitmap == null) {
                        Toast.makeText(EditActivity.this, "Bạn chưa tải ảnh lên", Toast.LENGTH_SHORT).show();
                    }
                    else {
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
            }
        });

    }

    private boolean checkStoragePermission() {
        // kiểm tra quyền

        boolean readPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        boolean writePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return readPermission && writePermission;
    }

    // xử lý kết quả trả về từ onActivityResult()
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // ảnh được chọn từ thư viện của máy ảnh

        if (resultCode == RESULT_OK) {
            // ảnh được chọn
            if (requestCode == CAMERA_REQUEST) {
                // set image
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imgDoiAnh.setImageBitmap(photo);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // xử lý nút back trên actionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // nhấn vào nút back
        if (id == android.R.id.home) {
            // quay về MainActivity
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
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