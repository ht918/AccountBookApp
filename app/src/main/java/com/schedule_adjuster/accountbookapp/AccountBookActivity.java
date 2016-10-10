package com.schedule_adjuster.accountbookapp;


import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;

import java.util.Date;

public class AccountBookActivity extends AppCompatActivity{
    public void resetExpense(long id,SQLiteDatabase db){
        SharedPreferences sharedPreferences = getSharedPreferences("AMOUNT_DATA",MODE_PRIVATE);
        int wallet,mizuho,sumitomo,amountInt;
        wallet = sharedPreferences.getInt("wallet",0);
        mizuho = sharedPreferences.getInt("mizuho",0);
        sumitomo = sharedPreferences.getInt("sumitomo",0);

        Cursor cursor = db.query("expenses",null,"_id=?",new String[]{Long.toString(id)},null,null,null,null);
        try{
            if(cursor.moveToNext()){
                int amount = cursor.getInt(cursor.getColumnIndex("amount")) * - 1;
                String big,small;
                big = cursor.getString(cursor.getColumnIndex("big"));
                small = cursor.getString(cursor.getColumnIndex("small"));
                if(big.equals("食費") || big.equals("食費外")){
                    wallet -= amount;
                }else if(small.equals("引き出し")){ //引き出し
                    wallet += amount;
                    if(big.equals("みずほ銀行")) mizuho -= amount;
                    else sumitomo -= amount;
                }else if(small.equals("預け入れ")){ //預け入れ
                    wallet -= amount;
                    if(big.equals("みずほ銀行")) mizuho += amount;
                    else sumitomo += amount;
                }else if(small.equals("振り込み")){ //振り込み
                    if(big.equals("みずほ銀行")) mizuho -= amount;
                    else sumitomo -= amount;
                }else if(small.equals("小遣い")) {
                    mizuho += amount; //小遣い
                }else if(small.equals("引き落とし"))
                    sumitomo -= amount; //引き落とし
            }
        } finally {
            cursor.close();
        }



        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("wallet",wallet);
        editor.putInt("mizuho",mizuho);
        editor.putInt("sumitomo",sumitomo);
        Date date = new Date();
        editor.putLong("update",date.getTime());
        editor.commit();
    }
}
