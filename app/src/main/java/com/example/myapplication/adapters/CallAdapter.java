package com.example.myapplication.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.myapplication.R;
import com.example.myapplication.model.CallHistory;

import java.util.ArrayList;

public class CallAdapter extends ArrayAdapter {

    Activity context;
    int resource;
    ArrayList<CallHistory> listCallHistory;

    public CallAdapter(Activity context, int resource, ArrayList<CallHistory> listCallHistory) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.listCallHistory = listCallHistory;
    }

    @Override
    public int getCount() {
        return listCallHistory.size();
    }

    public ArrayList<CallHistory> getListCallHistory() {
        return listCallHistory;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View customView = inflater.inflate(resource, null);

        CircularImageView type = customView.findViewById(R.id.type);
        TextView number = customView.findViewById(R.id.number);
        TextView timestamp = customView.findViewById(R.id.timestamp);
        TextView duration = customView.findViewById(R.id.duration);

        CallHistory ch = listCallHistory.get(position);

        type.setImageResource(R.drawable.baseline_call_to_made_24);
        number.setText(ch.getNumber());
        timestamp.setText(ch.getTimestamp());
        duration.setText(ch.getDuration());

        return customView;
    }
}
