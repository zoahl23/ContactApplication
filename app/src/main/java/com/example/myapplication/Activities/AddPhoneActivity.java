package com.example.myapplication.Activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

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
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.PhoneNumber;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

public class AddPhoneActivity extends AppCompatActivity {
    private EditText addEdtTen, addEdtSdt, addEdtMail;
    private FloatingActionButton btnThemMoi, btnScanQr;
    private CircularImageView imgTaiAnh;

    private ActionBar actionBar;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    //lấy toàn bộ key của database
    private DatabaseReference get_key = database.getReference();
    //trỏ vào key cụ thể ở đây là key contact để lấy data trong key đó
    private DatabaseReference contactsRef=get_key.child("contact");
    private FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();
    private StorageReference storageReference=firebaseStorage.getReference();

    // hằng số chọn ảnh
    private static final int IMAGE_PICK_CAMERA_CODE = 104;
    // mảng quyền
    private String[] cameraPermission; // chụp và lưu
    private String[] storagePermission; // chỉ lưu
    // variables (sẽ chứa dữ liệu để lưu trữ)
    private Uri imageUri;


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

        // mảng quyền
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // lấy action bar
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            // tiêu đề
            actionBar.setTitle("Add Phone");
            // hiển thị nút back (có hàm xử lý phía dưới)
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        addEdtTen = findViewById(R.id.addEdtTen);
        addEdtSdt = findViewById(R.id.addEdtSdt);
        addEdtMail = findViewById(R.id.addEdtMail);
        btnThemMoi = findViewById(R.id.btnThemMoi);
        imgTaiAnh = findViewById(R.id.imgTaiAnh);
        btnScanQr = findViewById(R.id.btnScanQr);

        // kiểm tra phiên bản của máy đang chạy có đủ cao không, yêu cầu Android 8.0 trở lên mới hiển thị
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // kiểm tra ứng dụng đã được cấp quyền thông báo chưa
//            if (ContextCompat.checkSelfPermission(AddPhoneActivity.this,
//                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//
//                // nếu quyền chưa được cấp, yêu cầu quyền
//                ActivityCompat.requestPermissions(AddPhoneActivity.this,
//                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
//                // mảng chứa các yêu cầu quyền
//            }
//        }

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
        btnScanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddPhoneActivity.this);
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

        imgTaiAnh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddPhoneActivity.this);
                builder.setTitle("Chọn ảnh");

                String[] options = {"Chụp ảnh mới", "Tải ảnh lên"};

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // tạm thời chưa làm được
                            // Chụp ảnh mới
                            if (!checkCameraPermission()) {
                                // ko cấp quyền truy cập camera
                                Toast.makeText(AddPhoneActivity.this, "Bạn chưa cấp quyền truy cập camera", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                pickFromCamera();
                            }
                        } else if (which == 1) {
                            // Tải ảnh từ thư viện
//                            chonAnh.launch("image/*");
                            if (!checkStoragePermission()) {
                                // ko cho truy cập vào ảnh
                                Toast.makeText(AddPhoneActivity.this, "Bạn chưa cấp quyền truy cập ảnh", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                chonAnh.launch("image/*");
                            }
                        }
                    }
                });

                builder.show();
            }
        });
        btnThemMoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ten = addEdtTen.getText().toString().trim();
                String sdt = addEdtSdt.getText().toString().trim();
                String mail = addEdtMail.getText().toString().trim();
                // Kiểm tra chuỗi email
                String emailPattern = "^[^\\s@]+@[^\\s@]+\\.com$";
                if (ten.length() == 0) {
                    Toast.makeText(AddPhoneActivity.this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
                }
                else if (sdt.length() != 10 || !sdt.startsWith("0")) {
                    Toast.makeText(AddPhoneActivity.this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
                }
                else if (!Pattern.matches(emailPattern, mail)) {
                    Toast.makeText(AddPhoneActivity.this, "Mail không hợp lệ", Toast.LENGTH_SHORT).show();
                }
                else {
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
                    if (bitmap == null) {
                        Toast.makeText(AddPhoneActivity.this, "Bạn chưa tải ảnh lên", Toast.LENGTH_SHORT).show();
                    }
                    else {
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
            }
        });
    }

    // lấy ảnh từ camera
    private void pickFromCamera() {
        // chọn hình ảnh từ máy ảnh, hình ảnh sẽ được trả về trong phương thức onActivityResult
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Image_Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image description");
        // put image uri
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // mở camera chụp ảnh
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        // kiểm tra quyền

        boolean readPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        boolean writePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return readPermission && writePermission;
    }

    private boolean checkCameraPermission() {
        // kiểm tra quyền camera

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    // xử lý kết quả trả về từ onActivityResult()
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // ảnh được chọn từ thư viện của máy ảnh

        if (resultCode == RESULT_OK) {
            // ảnh được chọn
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                // set image
                imgTaiAnh.setImageURI(imageUri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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