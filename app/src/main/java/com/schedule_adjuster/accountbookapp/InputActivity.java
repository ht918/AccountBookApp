package com.schedule_adjuster.accountbookapp;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;


public class InputActivity extends AppCompatActivity {

    private Spinner bigSpinner,smallSpinner;

    private DatePickerDialog.OnDateSetListener dateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        /* Spinnerの設定 */
        bigSpinner = (Spinner)findViewById(R.id.BigListSpinner);
        smallSpinner = (Spinner)findViewById(R.id.SmallListSpinner);

        ArrayAdapter<String> bigAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.BigList));
        bigAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final ArrayAdapter<String> foodAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.FoodList));
        foodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final ArrayAdapter<String> otherAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.OtherList));
        otherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final ArrayAdapter<String> mizuhoAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.MizuhoList));
        mizuhoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final ArrayAdapter<String> sumitomoAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.SumitomoList));
        sumitomoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        bigSpinner.setAdapter(bigAdapter);

        bigSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String)bigSpinner.getSelectedItem();
                if(item.equals(getResources().getStringArray(R.array.BigList)[0])){
                    smallSpinner.setAdapter(foodAdapter);
                }else if(item.equals(getResources().getStringArray(R.array.BigList)[1])){
                    smallSpinner.setAdapter(otherAdapter);
                }else if(item.equals(getResources().getStringArray(R.array.BigList)[2])){
                    smallSpinner.setAdapter(mizuhoAdapter);
                }else if(item.equals(getResources().getStringArray(R.array.BigList)[3])){
                    smallSpinner.setAdapter(sumitomoAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        /* DatePickerの設定 */
        final EditText dateText = (EditText)findViewById(R.id.DateInput);
        Calendar calendar = Calendar.getInstance();
        dateText.setText(calendar.get(Calendar.YEAR) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH));

        dateSetListener = new DatePickerDialog.OnDateSetListener(){

            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                dateText.setText(year+"/"+ (month + 1) +"/"+day);
            }
        };

        /* Intentからのデータの受け取り */
        Intent i = getIntent();
        int dbId = i.getIntExtra("dbID",-1);
        if(dbId >= 0){
            final AccountDbOpenHelper accountDbOpenHelper = new AccountDbOpenHelper(this);
            SQLiteDatabase db = accountDbOpenHelper.getReadableDatabase();
            Cursor cursor = db.query("expenses",null,"_id=?",new String[]{Integer.toString(dbId)},null,null,null,null);
            try{
                if(cursor.moveToNext()){
                    String detail = cursor.getString(cursor.getColumnIndex("detail"));
                    String amount = cursor.getString(cursor.getColumnIndex("amount"));
                    EditText detailEditText = (EditText)findViewById(R.id.DetailInput);
                    EditText amountEditText = (EditText)findViewById(R.id.AmountInput);
                    detailEditText.setText(detail);
                    amountEditText.setText(amount);
                    String item = cursor.getString(cursor.getColumnIndex("big"));
                    int bigPosition = bigAdapter.getPosition(item);
                    bigSpinner.setSelection(bigPosition);
                    int smallPosition = 0;
                    String smallItem = cursor.getString(cursor.getColumnIndex("small"));
                    Log.d("Item",item + " / " + smallItem + getResources().getStringArray(R.array.BigList)[1]);
                    if(item.equals(getResources().getStringArray(R.array.BigList)[0])){
                        smallPosition = foodAdapter.getPosition(smallItem);
                    }else if(item.equals(getResources().getStringArray(R.array.BigList)[1])){
                        smallPosition = otherAdapter.getPosition(smallItem);
                    }else if(item.equals(getResources().getStringArray(R.array.BigList)[2])){
                        smallPosition = mizuhoAdapter.getPosition(smallItem);
                    }else if(item.equals(getResources().getStringArray(R.array.BigList)[3])){
                        smallPosition = sumitomoAdapter.getPosition(smallItem);
                    }
                    Log.d("smallPosition",Integer.toString(smallPosition));
                    final int finalSmallPosition = smallPosition;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            smallSpinner.setSelection(finalSmallPosition);
                        }
                    },500);
                    dateText.setText(cursor.getString(cursor.getColumnIndex("date")));
                }
            } finally {
                cursor.close();
            }
        }

    }

    public void openDatePicker(View view){
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    public void inputExpenseData(View view){
        AccountDbOpenHelper accountDbOpenHelper = new AccountDbOpenHelper(this);
        SQLiteDatabase db = accountDbOpenHelper.getWritableDatabase();

        EditText detail = (EditText)findViewById(R.id.DetailInput);
        EditText amount = (EditText)findViewById(R.id.AmountInput);
        EditText date = (EditText)findViewById(R.id.DateInput);

        ContentValues values = new ContentValues();
        values.put("big",(String)bigSpinner.getSelectedItem());
        values.put("small",(String)smallSpinner.getSelectedItem());
        values.put("detail",detail.getText().toString());
        values.put("amount",Integer.valueOf(amount.getText().toString()));
        values.put("upload",0);
        values.put("date",date.getText().toString());

        long id = db.insert("expenses",null,values);

        detail.setText("");
        amount.setText("");
        Toast.makeText(this,"入力完了",Toast.LENGTH_SHORT).show();

        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }

}
