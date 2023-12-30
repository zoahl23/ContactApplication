package com.example.myapplication.Activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.CallAdapter;
import com.example.myapplication.model.CallHistory;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListCallActivity extends AppCompatActivity {

    private ActionBar actionBar;

    // Trong class ListCallActivity

    // Thêm phương thức này để lấy lịch sử cuộc gọi và lưu nó vào Firebase
    private void captureCallHistory(String phoneNumber) {
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
            do {
                // Trích xuất chi tiết cuộc gọi
                @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
                @SuppressLint("Range") long timestamp = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                @SuppressLint("Range") int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));

                // Lưu lịch sử cuộc gọi vào Firebase
                // TODO: Thực hiện logic lưu trữ Firebase ở đây
                // Ví dụ cấu trúc: /call_history/{so_dien_thoai}/{id_cuoc_goi}/...

                // Khởi tạo tham chiếu cơ sở dữ liệu Firebase
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                // Tạo một khóa duy nhất cho mỗi cuộc gọi (có thể sử dụng push())
                String callId = databaseReference.child("call_history").child(phoneNumber).push().getKey();

                // Tạo một bản đồ để lưu trữ chi tiết cuộc gọi
                Map<String, Object> callDetails = new HashMap<>();
                callDetails.put("type", type);
                callDetails.put("timestamp", timestamp);
                callDetails.put("duration", duration);

                // Cập nhật lịch sử cuộc gọi vào Firebase
                databaseReference.child("call_history").child(phoneNumber).child(callId).updateChildren(callDetails);
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    // Cập nhật phương thức onCreate để gọi captureCallHistory
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_call);

        // lấy action bar
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            // tiêu đề
            actionBar.setTitle("Call History");
            // hiển thị nút back (có hàm xử lý phía dưới)
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        String phone = data.getString("call");

        // Thu thập lịch sử cuộc gọi cho số điện thoại cụ thể
        captureCallHistory(phone);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // nhấn vào nút back
        if (id == android.R.id.home) {
            // quay về SeePhoneActivity
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}