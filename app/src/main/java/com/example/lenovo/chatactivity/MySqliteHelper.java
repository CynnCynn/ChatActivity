package com.example.lenovo.chatactivity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lenovo on 2017/11/3.
 */

public class MySqliteHelper extends SQLiteOpenHelper {
 public MySqliteHelper(Context context){
     super(context,"userdb.db",null,3);

 }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table users(id int primary key ,name text,pwd text)");
        String sql = "insert into users values (0,0,0)"; //id 自增加
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
