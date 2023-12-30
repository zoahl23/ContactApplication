package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.myapplication.Activities.AddPhoneActivity;
import com.example.myapplication.Activities.EditActivity;
import com.example.myapplication.Activities.SeePhoneActivity;
import com.example.myapplication.adapters.PhoneAdapter;
import com.example.myapplication.model.PhoneNumber;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView lvPhoneNumber;
    private ArrayList<PhoneNumber> listPN,listPNFilter;
    private FloatingActionButton btnAddPhone;
    private ActionBar actionBar;
    private PhoneAdapter adapterPN;
    //khởi tạo biến database để sử dụng các chức năng của firebase
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    //lấy toàn bộ key của database
    private DatabaseReference get_key = database.getReference();
    //trỏ vào key cụ thể ở đây là key contact để lấy data trong key đó
    private DatabaseReference contactsRef=get_key.child("contact");

    String data_search="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // lấy action bar
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            // tiêu đề
            actionBar.setTitle("Contact App");
        }

        btnAddPhone = findViewById(R.id.btnAddPhone);
        lvPhoneNumber = findViewById(R.id.lvPhoneNumber);
        listPN = new ArrayList<>();
        listPNFilter= new ArrayList<>();
        adapterPN = new PhoneAdapter(MainActivity.this, R.layout.lv_phone_number, listPN);
        lvPhoneNumber.setAdapter(adapterPN);
        docDuLieuFireBase();
        registerForContextMenu(lvPhoneNumber);

        // nhấn vào item để chuyển qua trang xem chi tiết
        lvPhoneNumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Lấy vị trí của item được nhấp vào
//                int position = adapterView.getPositionForView(view);

                // Gửi intent đến SeePhoneActivity với key
//                Intent xemChiTietIntent = new Intent(MainActivity.this, SeePhoneActivity.class);
//                xemChiTietIntent.putExtra("key", listPN.get(position).getKey());
//                startActivity(xemChiTietIntent);
//                Toast.makeText(MainActivity.this, listPN.get(i).getTen(), Toast.LENGTH_SHORT).show();
                Intent xemChiTietIntent = new Intent(getBaseContext(), SeePhoneActivity.class);
                //hàm getData_test đươc tạo ở trong phần PhoneAdapter.java
                listPNFilter=adapterPN.getListPNFilter();

                if(adapterPN.getData_test().length()>0){
//                    for(int p=0;p<listPN.size();p++){
//                        if(listPN.get(p).getTen().contains(adapterPN.getData_test())){
//                            xemChiTietIntent.putExtra("key", listPN.get(p).getKey());
//                            startActivity(xemChiTietIntent);
//                            Toast.makeText(MainActivity.this, "p", Toast.LENGTH_SHORT).show();
//                        }
//                    }
                    int position = adapterView.getPositionForView(view);
                    xemChiTietIntent.putExtra("key", listPNFilter.get(position).getKey());
                    startActivity(xemChiTietIntent);
                    Toast.makeText(MainActivity.this, "Xem Chi Tiết...", Toast.LENGTH_SHORT).show();
                }
                else{
                    xemChiTietIntent.putExtra("key", listPN.get(i).getKey());
                    startActivity(xemChiTietIntent);
//                    Toast.makeText(MainActivity.this, "i", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnAddPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddPhoneActivity.class));
            }
        });

    }
    //lấy data trong cơ sở dữ liệu rồi add vào list PN
    public void docDuLieuFireBase(){
        //sử dụng biến contactsRef đã khởi tạo ở trên
        //sự kiện này để firebase cập nhật data liên tục theo thời gian thực tế
        contactsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listPN.clear();
                for(DataSnapshot data:snapshot.getChildren()){
                    PhoneNumber phoneNumber=data.getValue(PhoneNumber.class);
                    if(phoneNumber!=null){
                        listPN.add(phoneNumber);
                    }
                    System.out.println(data.getValue().toString());
                }
                //thông báo sự thay đổi
                adapterPN.notifyDataSetChanged();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    //code khi bấm chuột trái trỏ lên các chức năng thao tác với liên hệ
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info=(AdapterView.AdapterContextMenuInfo) menuInfo;
        PhoneNumber phoneNumber= (PhoneNumber) adapterPN.getListPN().get(info.position);
        String title="Thao tác với: "+phoneNumber.getTen();
        menu.setHeaderTitle(title);
        menu.add(0,v.getId(),0,"Gọi Điện Thoại");
        menu.add(0,v.getId(),1,"Nhắn tin");
        menu.add(0,v.getId(),2,"Sửa Thông Tin");
        menu.add(0,v.getId(),3,"Xóa Liên Hệ");

    }
    //code thao tác chức năng sửa xóa gọi
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info
                = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        PhoneNumber phoneNumber=adapterPN.getListPN().get(info.position);
        switch (item.getOrder()){
            //case 0 ứng với gọi điện thoại
            case 0:
                Uri phoneURI=Uri.parse("tel: "+phoneNumber.getSdt());
                Intent goiDienThoai=new Intent(Intent.ACTION_DIAL,phoneURI);
                startActivity(goiDienThoai);
                Toast.makeText(this,"Gọi Tới Số: "+ phoneNumber.getSdt(),
                        Toast.LENGTH_SHORT).show();
                break;
            // case gửi sms
            case 1:
                Uri smsURI=Uri.parse("smsto: "+ phoneNumber.getSdt());
                Intent nhanTin= new Intent(Intent.ACTION_SENDTO, smsURI);
                startActivity(nhanTin);
                Toast.makeText(this,"Nhắn tin tới số: "+ phoneNumber.getSdt(),
                        Toast.LENGTH_SHORT).show();
                break;
            //sửa thông tin liên hệ
            case 2:
                Intent editTT=new Intent(MainActivity.this, EditActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("key",phoneNumber.getKey());
                editTT.putExtras(bundle);
                startActivity(editTT);
                break;
            //xóa liên hệ
            case 3:
                String thongDiep="Bạn Thực Sự Muốn Xóa "
                        + phoneNumber.getTen()+" - "+ phoneNumber.getSdt()+"?";
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Xóa Liên Hệ");
                builder.setMessage(thongDiep);
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        contactsRef.child(phoneNumber.getKey()).removeValue();
                        docDuLieuFireBase();
                    }
                });
                builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
                Toast.makeText(this,"Xóa Thông Tin: "+ phoneNumber.getSdt(),
                        Toast.LENGTH_SHORT).show();
                break;
        }
        return true;

    }

    ActivityResultLauncher addPhoneLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        docDuLieuFireBase();
                    }
                }
            });
    // xử lý nút tìm kiếm dữ liệu trong danh bạ
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                data_search=s;
                searchView.clearFocus();
                adapterPN.getFilter().filter(s);
                return false;

            }
            @Override
            public boolean onQueryTextChange(String s) {
                adapterPN.getFilter().filter(s);
                return false;
            }
        });
        MenuItem menuItemS = menu.findItem(R.id.sort_bar);
        menuItemS.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Sắp xếp theo");
                String[] options = {"Tên liên lạc", "Số điện thoại", "Gmail"};

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Xếp theo tên tăng dần
                            Collections.sort(listPN, new Comparator<PhoneNumber>() {
                                @Override
                                public int compare(PhoneNumber o1, PhoneNumber o2) {
                                    return (o1.getTen().compareTo(o2.getTen()));
                                }

                            });
                            adapterPN = new PhoneAdapter(MainActivity.this, R.layout.lv_phone_number, listPN);
                            lvPhoneNumber.setAdapter(adapterPN);
                            registerForContextMenu(lvPhoneNumber);
                        }
                        else if (which == 1) {
                            // Xếp theo số điện thoại tăng dần
                            Collections.sort(listPN, new Comparator<PhoneNumber>() {
                                @Override
                                public int compare(PhoneNumber o1, PhoneNumber o2) {
                                    if (Integer.parseInt(o1.getSdt()) < Integer.parseInt(o2.getSdt())) {
                                        return 1;
                                    } else {
                                        if (Integer.parseInt(o1.getSdt()) == Integer.parseInt(o2.getSdt())) {
                                            return 0;
                                        } else {
                                            return -1;
                                        }
                                    }

                                }

                            });
                            adapterPN = new PhoneAdapter(MainActivity.this, R.layout.lv_phone_number, listPN);
                            lvPhoneNumber.setAdapter(adapterPN);
                            registerForContextMenu(lvPhoneNumber);
                        }
                        else if (which == 2) {
                            // Xếp theo Mail
                            Collections.sort(listPN, new Comparator<PhoneNumber>() {
                                @Override
                                public int compare(PhoneNumber o1, PhoneNumber o2) {
                                    return (o1.getMail().compareTo(o2.getMail()));
                                }

                            });
                            adapterPN = new PhoneAdapter(MainActivity.this, R.layout.lv_phone_number, listPN);
                            lvPhoneNumber.setAdapter(adapterPN);
                            registerForContextMenu(lvPhoneNumber);
                        }
                    }
                });

                builder.show();


                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

}