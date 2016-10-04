package com.schedule_adjuster.accountbookapp;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class AccountDbAdapter extends ArrayAdapter<String>{

    private ArrayList<Integer> dbIds;
    private ArrayList<String> texts;

    public AccountDbAdapter(Context context,int textViewResourceId,ArrayList<String> arrayList,ArrayList<Integer> dbIds){
        super(context,textViewResourceId,arrayList);
        this.dbIds = dbIds;
        this.texts = arrayList;
    }

    @Override
    public long getItemId(int position){
        return (long)dbIds.get(position);
    }

    public String getItemString(int position){
        return texts.get(position);
    }

}
