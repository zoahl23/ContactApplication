package com.example.myapplication.adapters;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.PhoneNumber;

import java.util.ArrayList;

public class PhoneAdapter extends ArrayAdapter implements Filterable {

    Activity context;
    int resource;
    ArrayList<PhoneNumber> listPN, listPNBack, listPNFilter;
    String data_test="";
    public PhoneAdapter(Activity context, int resource, ArrayList<PhoneNumber> listPN) {
        super (context, resource);
        this.context = context;
        this.resource = resource;
        this.listPN = this.listPNBack = listPN;
    }
    //tạo 1 hàm return ra data_test
    public String getData_test(){
        return data_test;
    }

    public int getCount() {
        return listPN.size();
    }
    // Hàm thay đổi theo lọc dlieu
    public ArrayList<PhoneNumber> getListPN() {
        return listPN;
    }
    @NonNull
    //hàm này xử lý lệnh tìm kiếm được sử dụng trong MainActivity
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String s = charSequence.toString().toLowerCase().trim();
                if (s.length() < 0) {
                    listPNFilter = listPNBack;
                }
                else{
                    listPNFilter = new ArrayList<>();
                    for (PhoneNumber p: listPNBack) {
                        if (p.getTen().toLowerCase().contains(s) ||
                                p.getSdt().toLowerCase().contains(s) ||
                                    p.getMail().toLowerCase().contains(s)) {
                            listPNFilter.add(p);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = listPNFilter;
                return filterResults;
            }
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                listPN = (ArrayList<PhoneNumber>) results.values;
                notifyDataSetChanged();
            }

        };
    }
    //Hàm này ánh xạ listview
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = this.context.getLayoutInflater();
        View customView = layoutInflater.inflate(this.resource, null);
        ImageView imgAvt = customView.findViewById(R.id.imgAvt);
        TextView tvTen = customView.findViewById(R.id.tvTen);
        TextView tvSdt = customView.findViewById(R.id.tvSdt);
        TextView tvMail = customView.findViewById(R.id.tvMail);
        PhoneNumber p = this.listPN.get(position);
        Glide.with(context.getBaseContext()).load(p.getAvt()).into(imgAvt);
        tvTen.setText(p.getTen());
        tvSdt.setText(p.getSdt());
        tvMail.setText(p.getMail());
        return customView;
    }

}
