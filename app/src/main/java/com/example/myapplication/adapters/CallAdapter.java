package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.CallHistory;
import com.example.myapplication.model.PhoneNumber;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class CallAdapter extends BaseAdapter {

    private Context context;
    private List<CallHistory> callHistoryData;

    private DatabaseReference databaseReference;

    @Override
    public int getCount() {
        return callHistoryData.size();
    }

    @Override
    public Object getItem(int position) {
        return callHistoryData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Cập nhật constructor để chấp nhận dữ liệu Firebase
    public CallAdapter(Context context, List<CallHistory> callHistoryData, DatabaseReference databaseReference) {
        this.context = context;
        this.callHistoryData = callHistoryData;
        this.databaseReference = databaseReference;
    }

    // Trong phương thức getView
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.lv_call_history, parent, false);
        }

        CallHistory callHistory = callHistoryData.get(position);

        // Đặt số điện thoại
        TextView numberView = view.findViewById(R.id.number);
        numberView.setText(callHistory.getNumber());

        // Đặt loại cuộc gọi
        TextView typeView = view.findViewById(R.id.type);
        typeView.setText(callHistory.getType());

        // Đặt timestamp
        TextView timestampView = view.findViewById(R.id.timestamp);
        timestampView.setText(callHistory.getTimestamp());

        // Đặt thời lượng
        TextView durationView = view.findViewById(R.id.duration);
        durationView.setText(String.format("00:00:%d", callHistory.getDuration()));

        return view;
    }
}
