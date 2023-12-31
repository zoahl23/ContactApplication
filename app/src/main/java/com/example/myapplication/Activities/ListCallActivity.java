package com.example.myapplication.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.CallAdapter;
import com.example.myapplication.model.CallHistory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListCallActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private ListView lvCallHistory;
    ArrayList<CallHistory> listCallHistory;
    CallAdapter callAdapter;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private DatabaseReference contacref = databaseReference.child("call_history");


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

        lvCallHistory = findViewById(R.id.lvCallHistory);

        listCallHistory = new ArrayList<>();
        callAdapter = new CallAdapter(ListCallActivity.this, R.layout.lv_call_history, listCallHistory);
        lvCallHistory.setAdapter(callAdapter);
        docDulieu();
    }

    private void docDulieu() {
        contacref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listCallHistory.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    CallHistory ch = data.getValue(CallHistory.class);
                    if (ch != null) {
                        listCallHistory.add(ch);
                    }
                    System.out.println(data.getValue().toString());
                    Log.d("ValueCallNe", data.getValue().toString());
                }
                callAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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