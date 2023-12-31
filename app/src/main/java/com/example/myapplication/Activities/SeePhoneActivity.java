package com.example.myapplication.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.bumptech.glide.Glide;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.PhoneNumber;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SeePhoneActivity extends AppCompatActivity {
    private ImageView imgQr;
    private CircularImageView imgAvtS;
    private TextView tvNameS, tvSdtS, tvMailS;
    private Button btnCallLog, btnZalo;
    private FloatingActionButton btnCall, btnChat, btnTaoQr, btnMail;
    private Bitmap bitmapQrImage;
    private ActionBar actionBar;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    //lấy toàn bộ key của database
    private DatabaseReference get_key = database.getReference();
    //trỏ vào key cụ thể ở đây là key contact để lấy data trong key đó
    private DatabaseReference contactsRef=get_key.child("contact");
    private FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();
    private StorageReference storageReference=firebaseStorage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_phone);

        // lấy action bar
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            // tiêu đề
            actionBar.setTitle("Phone Details");
            // hiển thị nút back (có hàm xử lý phía dưới)
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        imgAvtS = findViewById(R.id.imgAvtS);
        tvNameS = findViewById(R.id.tvNameS);
        tvSdtS = findViewById(R.id.tvSdtS);
        tvMailS = findViewById(R.id.tvMailS);

        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        //get khóa chính của csdl để sử dụng các thông tin chính xác cho user đó
        String key=data.getString("key");

        contactsRef.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PhoneNumber phoneNumber=snapshot.getValue(PhoneNumber.class);
                tvNameS.setText(phoneNumber.getTen());
                tvSdtS.setText(phoneNumber.getSdt());
                tvMailS.setText(phoneNumber.getMail());
                Glide.with(getBaseContext()).load(phoneNumber.getAvt()).into(imgAvtS);
                try {
                    if (phoneNumber.getSdt() != null && !phoneNumber.getSdt().isEmpty()) {
                        saveCallHistory(phoneNumber.getSdt());
                    } else {
                        Toast.makeText(SeePhoneActivity.this, "Diện thoại null", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(SeePhoneActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    Log.d("loilon", e.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnCallLog = findViewById(R.id.btnCallLog);
        btnTaoQr = findViewById(R.id.btnTaoQr);
        btnZalo = findViewById(R.id.btnZalo);
        btnMail = findViewById(R.id.btnMail);
        btnCall = findViewById(R.id.btnCall);
        btnChat = findViewById(R.id.btnChat);
        imgQr = findViewById(R.id.imgQr);

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactsRef.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PhoneNumber pn = snapshot.getValue(PhoneNumber.class);
                        Uri smsURI=Uri.parse("smsto: "+ pn.getSdt());
                        Intent nhanTin= new Intent(Intent.ACTION_SENDTO, smsURI);
                        startActivity(nhanTin);
                        Toast.makeText(SeePhoneActivity.this,"Nhắn tin tới số: "+ pn.getSdt(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactsRef.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PhoneNumber pn = snapshot.getValue(PhoneNumber.class);
                        Uri phoneURI=Uri.parse("tel: "+ pn.getSdt());
                        Intent goiDienThoai=new Intent(Intent.ACTION_DIAL,phoneURI);
                        startActivity(goiDienThoai);
                        Toast.makeText(SeePhoneActivity.this,"Gọi Tới Số: "+ pn.getSdt(),
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        btnTaoQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactsRef.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PhoneNumber pn = snapshot.getValue(PhoneNumber.class);
                        if (pn.getSdt().length() > 0) {
                            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                            try {
                                BitMatrix bitMatrix = multiFormatWriter.encode(pn.getSdt(), BarcodeFormat.QR_CODE, 150, 150);
                                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                bitmapQrImage = barcodeEncoder.createBitmap(bitMatrix);
                                showQrCodeAlert();
                                imgQr.setImageBitmap(bitmapQrImage);
                            } catch (Exception e) {
                                Log.d("Lỗi tạo QR: ", e.toString());
                                Toast.makeText(SeePhoneActivity.this, "Lỗi tạo QR: " + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        btnMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactsRef.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PhoneNumber pn = snapshot.getValue(PhoneNumber.class);
                        Intent intentMail = new Intent(Intent.ACTION_SEND);
                        intentMail.putExtra(Intent.EXTRA_EMAIL, new String[]{pn.getMail()});
                        intentMail.putExtra(Intent.EXTRA_SUBJECT, "Gửi Mail từ App Contact");
                        intentMail.putExtra(Intent.EXTRA_TEXT, "hihi");
                        intentMail.setType("message/rfc822");
                        Log.d("mail", pn.getMail());
                        startActivity(intentMail);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        btnZalo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactsRef.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PhoneNumber pn = snapshot.getValue(PhoneNumber.class);
                        Intent intentZalo = new Intent();
                        intentZalo.setAction(Intent.ACTION_SEND);
                        intentZalo.setType("text/plain");
                        intentZalo.putExtra(Intent.EXTRA_TEXT, pn.getSdt());
                        intentZalo.setPackage("com.zing.zalo");

                        Log.d("zalo", String.valueOf(intentZalo));

                        // Kiểm tra xem có ứng dụng Zalo đã được cài đặt hay chưa
                        if (intentZalo.resolveActivity(getPackageManager()) != null) {
                            startActivity(intentZalo);
                        } else {
                            // intentZalo.setData(Uri.parse("https://zalo.me/"));
                            // thông báo cho người dùng
                            Toast.makeText(getApplicationContext(), "Ứng dụng Zalo chưa được cài đặt", Toast.LENGTH_SHORT).show();
                            makeNotification("Lỗi", "Ứng dụng Zalo chưa được cài đặt");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        btnCallLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactsRef.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PhoneNumber pn = snapshot.getValue(PhoneNumber.class);
                        // chuyển trang
                        Intent listCall=new Intent(SeePhoneActivity.this, ListCallActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putString("call",pn.getSdt());
                        listCall.putExtras(bundle);
                        startActivity(listCall);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    public String formatDurationSee(int durationInSeconds) {
        int hours = durationInSeconds / 3600;
        int minutes = (durationInSeconds % 3600) / 60;
        int seconds = durationInSeconds % 60;

        return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Thêm phương thức này để lấy lịch sử cuộc gọi và lưu nó vào Firebase
    private void saveCallHistory(String phoneNumber) {
        // Sử dụng ContentResolver để truy vấn lịch sử cuộc gọi
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                CallLog.Calls.CONTENT_URI,
                null,
                CallLog.Calls.NUMBER + "=?",
                new String[]{phoneNumber},
                CallLog.Calls.DATE + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            // Khởi tạo tham chiếu cơ sở dữ liệu Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            // Xóa tất cả dữ liệu cũ liên quan đến số điện thoại
            databaseReference.child("call_history").removeValue();
            do {
                // Trích xuất chi tiết cuộc gọi
                @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
                @SuppressLint("Range") long timestamp = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                @SuppressLint("Range") int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));

                // Lưu lịch sử cuộc gọi vào Firebase

                // Tạo một khóa duy nhất cho mỗi cuộc gọi (có thể sử dụng push())
                String timestampString = Long.toString(timestamp);
                String callId = databaseReference.child("call_history").child(timestampString).push().getKey();

                // Chuyển đổi timestamp thành Date
                Date date = new Date(timestamp);

                // Format Date thành chuỗi theo định dạng mong muốn
                String formattedDate = new SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH).format(date);

                String formattedDuration = formatDurationSee(duration);

                // Tạo một bản đồ để lưu trữ chi tiết cuộc gọi
                Map<String, Object> callDetails = new HashMap<>();
                callDetails.put("id", timestampString);
                callDetails.put("number", number);
                callDetails.put("type", type);
                callDetails.put("timestamp", formattedDate);
                callDetails.put("duration", formattedDuration);

                // Cập nhật lịch sử cuộc gọi vào Firebase
                databaseReference.child("call_history").child(timestampString).updateChildren(callDetails);
            } while (cursor.moveToNext());

            cursor.close();
        }
        else {
            // Khởi tạo tham chiếu cơ sở dữ liệu Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            // Xóa tất cả dữ liệu cũ liên quan đến số điện thoại
            databaseReference.child("call_history").removeValue();
        }
    }

    private void showQrCodeAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mã QR");

        // Inflate layout tùy chỉnh cho AlertDialog
        View customLayout = getLayoutInflater().inflate(R.layout.alert_qr_code, null);
        PhotoView photoView = customLayout.findViewById(R.id.photoView);
        photoView.setImageBitmap(bitmapQrImage);

        // Đặt layout tùy chỉnh cho AlertDialog
        builder.setView(customLayout);

        builder.setPositiveButton("Lưu ảnh", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveImg();
            }
        });

        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
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
            makeNotification("Thông báo", "Đã lưu ảnh thành công");
        } catch (Exception e) {
            Log.d("Lỗi lưu ảnh", e.toString());
            Toast.makeText(SeePhoneActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
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

    // hàm chuyển trang edit
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);
        MenuItem menuItemA = menu.findItem(R.id.edit_phone);
        menuItemA.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                // lấy key
//                Intent intent = getIntent();
//                Bundle data = intent.getExtras();
//                PhoneNumber pn = (PhoneNumber) data.get("pn_value");
                Intent intent = getIntent();
                Bundle data = intent.getExtras();
                //get khóa chính của csdl để sử dụng các thông tin chính xác cho user đó
                String key =data.getString("key");
                // chuyển trang
                Intent editTT=new Intent(SeePhoneActivity.this, EditActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("key",key ); // gửi key qua pn.getKey()
                editTT.putExtras(bundle);
                startActivity(editTT);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}