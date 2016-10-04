package com.schedule_adjuster.accountbookapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
        updateExpenses();
    }
    final Activity activity = this;

    public void updateExpenses(){

        final AccountDbOpenHelper accountDbOpenHelper = new AccountDbOpenHelper(this);
        SQLiteDatabase db = accountDbOpenHelper.getReadableDatabase();

        ListView expenseListView = (ListView)findViewById(R.id.expenseList);

        Cursor c = db.query("expenses",new String[] {"big","small","detail","amount","date","_id"},null,null,null,null,null);
        boolean mov = c.moveToFirst();
        ArrayList<String> expenseList = new ArrayList<String>();
        ArrayList<Integer> dbIdList = new ArrayList<Integer>();
        while(mov){
            String s = String.format("%s/%s",c.getString(0),c.getString(1));
            if(StringUtils.isNotEmpty(c.getString(2))){
                s += String.format(" (%s)",c.getString(2));
            }
            s += String.format(" %d円 [%s]",c.getInt(3),c.getString(4));
            expenseList.add(s);
            dbIdList.add(c.getInt(5));
            mov = c.moveToNext();
        }
        c.close();
        db.close();
        String[] sArray = new String[expenseList.size()];
        expenseList.toArray(sArray);
        expenseListView.setAdapter(new AccountDbAdapter(getApplicationContext(),R.layout.custom_text_list_item,expenseList,dbIdList));
        expenseListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("View ID",Integer.toString((int)id));
                AlertDialog.Builder alertDlg = new AlertDialog.Builder(activity);
                alertDlg.setTitle("削除確認");
                alertDlg.setMessage("この支出を削除してもよろしいですか？");
                final int itemId = (int)id;
                alertDlg.setPositiveButton("削除する", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = accountDbOpenHelper.getWritableDatabase();
                        db.beginTransaction();
                        try{
                            db.delete("expenses","_id=?",new String[]{Integer.toString(itemId)});
                            db.setTransactionSuccessful();
                        }finally {
                            db.endTransaction();
                            updateExpenses();
                        }

                    }
                });
                alertDlg.setNegativeButton("削除しない",null);
                alertDlg.create().show();
                return true;
            }
        });
        expenseListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(activity,InputActivity.class);
                i.putExtra("dbID",(int)id);
                startActivity(i);
            }
        });

    }
}
