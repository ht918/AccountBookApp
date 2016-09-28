package com.schedule_adjuster.accountbookapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

public class BookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        AccountDbOpenHelper accountDbOpenHelper = new AccountDbOpenHelper(this);
        SQLiteDatabase db = accountDbOpenHelper.getReadableDatabase();

        ListView expenseListView = (ListView)findViewById(R.id.expenseList);

        Cursor c = db.query("expenses",new String[] {"big","small","detail","amount","date"},null,null,null,null,null);
        boolean mov = c.moveToFirst();
        ArrayList<String> expenseList = new ArrayList<String>();
        while(mov){
            String s = String.format("%s/%s",c.getString(0),c.getString(1));
            if(StringUtils.isNotEmpty(c.getString(2))){
                s += String.format(" (%s)",c.getString(2));
            }
            s += String.format(" %d円 [%s]",c.getInt(3),c.getString(4));
            expenseList.add(s);
            mov = c.moveToNext();
        }
        c.close();
        db.close();
        String[] sArray = new String[expenseList.size()];
        expenseList.toArray(sArray);
        expenseListView.setAdapter(new ArrayAdapter<String>(getApplicationContext(),R.layout.custom_text_list_item,sArray));
    }
}
