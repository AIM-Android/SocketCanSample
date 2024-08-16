package com.advantech.socketcan.baseui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.advantech.socketcan.ToastUtil;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract int getLayoutResID();

    protected abstract void initView(Bundle savedInstanceState);

    protected abstract void initData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResID());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initView(savedInstanceState);

        initData();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            cancelToast();
            finish();
        }
        return true;
    }

    protected void showToast(String message) {
        ToastUtil.show(this, message, Gravity.CENTER, Toast.LENGTH_SHORT);
    }

    protected void cancelToast() {
        ToastUtil.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelToast();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancelToast();
        finish();
    }
}
