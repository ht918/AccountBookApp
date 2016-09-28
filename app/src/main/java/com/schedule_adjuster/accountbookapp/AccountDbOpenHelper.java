package com.schedule_adjuster.accountbookapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AccountDbOpenHelper extends SQLiteOpenHelper {

    public AccountDbOpenHelper(Context context) {
        super(context,"AccountDB",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE expenses( _id integer primary key autoincrement, big text,small text,detail text,amount integer,date text,upload integer);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
