package com.asus.myapplication.ui.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.asus.myapplication.util.HRetrofitNetHelper;

public abstract class BaseActivity extends AppCompatActivity{
    public HRetrofitNetHelper retrofitNetHelper;
    public LayoutInflater mInflater;
    public ProgressDialog mDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        mInflater = LayoutInflater.from(this);
        setContentView(mInflater.inflate(layoutResID,null));
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        retrofitNetHelper = HRetrofitNetHelper.getInstance(BaseActivity.this);
        mDialog = new ProgressDialog(BaseActivity.this);
    }
}

