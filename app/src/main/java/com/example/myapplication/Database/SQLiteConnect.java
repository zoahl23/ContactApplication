package com.example.myapplication.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLiteConnect extends SQLiteOpenHelper {
    public SQLiteConnect(@Nullable Context context,
                         @Nullable String name,
                         @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }// ko động đến, hàm ko ảnh hưởng

    // Truy vấn không trả về kết quả: CREATE, DELETE, UPDATE, INSERT, ...
    public void queryData (String query) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL(query);
    }

    public Cursor getData(String query) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor data = sqLiteDatabase.rawQuery(query, null);
        return data;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
