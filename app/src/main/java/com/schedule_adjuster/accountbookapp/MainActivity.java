package com.schedule_adjuster.accountbookapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.RESTUtility;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.WebOAuth2Session;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    final static private String APP_KEY = "wambvzsih5yvgox";
    final static private String APP_SECRET = "4gm2tjj3htbkw1u";
    private DropboxAPI<AndroidAuthSession> mDBApi;

    // Preference に保存するためのKEY
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!hasLoadAndroidAuthSession()) {
            AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
            AndroidAuthSession session = new AndroidAuthSession(appKeys);
            mDBApi = new DropboxAPI<AndroidAuthSession>(session);
            mDBApi.getSession().startOAuth2Authentication(this);
        }else{
            mDBApi = new DropboxAPI<>(loadAndroidAuthSession());
        }
        try {

            String tmpCsvPath = Environment.getExternalStorageDirectory().getPath() + "/tmp.csv";
            File inputFile = new File(tmpCsvPath);
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String dropboxData = reader.readLine();
            Log.d("Filesize",Long.toString(inputFile.length()));
            Log.d("dropboxData",dropboxData);
            if(!StringUtils.isEmpty(dropboxData)) {
                String[] amountStrings = dropboxData.split(",");
                int walletAmount, mizuhoAmount, sumitomoAmount;
                walletAmount = Integer.parseInt(amountStrings[0]);
                mizuhoAmount = Integer.parseInt(amountStrings[1]);
                sumitomoAmount = Integer.parseInt(amountStrings[2]);
                SharedPreferences preferences = getSharedPreferences("AMOUNT_DATA", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("wallet", walletAmount);
                editor.putInt("mizuho", mizuhoAmount);
                editor.putInt("sumitomo", sumitomoAmount);
                editor.commit();
                TextView walletAmountText = (TextView) findViewById(R.id.walletAmount);
                walletAmountText.setText(amountStrings[0] + "円");
                TextView mizuhoAmountText = (TextView) findViewById(R.id.mizuhoAmount);
                mizuhoAmountText.setText(amountStrings[1] + "円");
                TextView sumitomoAmountText = (TextView) findViewById(R.id.sumitomoAmount);
                sumitomoAmountText.setText(amountStrings[2] + "円");
            }
        }catch(IOException ioe){
            Log.e("ioe",ioe.toString());
        }
    }

    public void openInputWindow(View view){
        Intent i = new Intent(this,InputActivity.class);
        startActivity(i);
    }

    public void openAccountBook(View view){
        Intent i = new Intent(this,BookActivity.class);
        startActivity(i);
    }

    public void uploadAccountBook(View view){
        String tmpCsvPath = Environment.getExternalStorageDirectory().getPath() + "/tmp.csv";
        final File file = new File(tmpCsvPath);
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "SJIS"));
            AccountDbOpenHelper accountDbOpenHelper = new AccountDbOpenHelper(this);
            SQLiteDatabase db = accountDbOpenHelper.getReadableDatabase();
            Cursor c = db.query("expenses",new String[] {"big","small","detail","amount","date","upload","_id"},null,null,null,null,null);
            boolean mov = c.moveToFirst();
            while(mov){
                if(c.getInt(5) == 0) {
                    bw.write(c.getString(0) + "," + c.getString(1) + "," + c.getString(2) + "," + String.valueOf(c.getInt(3)) + "," + c.getString(4));
                    bw.newLine();
                    ContentValues cv = new ContentValues();
                    cv.put("upload",1);
                    db.update("expenses",cv,"_id = " + String.valueOf(c.getInt(6)),null);
                }else{
                }
                mov = c.moveToNext();
            }
            bw.flush();
            bw.close();
            c.close();
            db.close();
            final FileInputStream inputStream = new FileInputStream(file);
            final Handler handler = new Handler();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        DropboxAPI.Entry response = mDBApi.putFileOverwrite("/account-book.csv", inputStream, file.length(), null);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"アップロード完了",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }catch(DropboxException dbe){
                        Log.e("DropboxException",dbe.toString());
                    }
                }
            }).start();
        }catch(IOException ioe){
            Log.e("IOException",ioe.toString());
        }
        Log.d("Upload","upload finished");
    }

    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                storeKeys(accessToken);
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
        String tmpCsvPath = Environment.getExternalStorageDirectory().getPath() + "/tmp.csv";
        final File file = new File(tmpCsvPath);
        try {
            final FileOutputStream os = new FileOutputStream(file, false);
            final Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        if(RESTUtility.parseDate(mDBApi.metadata("account-book.csv",1,null,true,null).modified).compareTo(new Date(file.lastModified())) > 0){
                            mDBApi.getFile("/account-book.csv",null,os,null);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"更新完了",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }catch(DropboxException dbe) {
                        Log.e("DropboxException", dbe.toString());
                    }
                }
            }).start();
        }catch(IOException ioe){
            Log.e("IOE",ioe.toString());
        }

    }

    private AndroidAuthSession loadAndroidAuthSession() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, MODE_PRIVATE);
        String token = prefs.getString(ACCESS_KEY_NAME, null);
        if(token != null){
            AppKeyPair appKeyPair = new AppKeyPair(APP_KEY,APP_SECRET);
            return new AndroidAuthSession(appKeyPair,token);
        }else{
            return null;
        }
    }

    public boolean hasLoadAndroidAuthSession(){
        return loadAndroidAuthSession() != null;
    }
    /**
     * Preference に Key を保存する
     */
    private void storeKeys(String key) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME,MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.commit();
    }
    /**
     * Preference の Key を削除する
     */
    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
}
