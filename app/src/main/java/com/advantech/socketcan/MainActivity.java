package com.advantech.socketcan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.advantech.socketcan.adapter.ResultAdapter;
import com.advantech.socketcan.baseui.BaseActivity;
import com.advantech.socketcan.CanFrame;
import com.advantech.socketcan.Mask;
import com.advantech.socketcan.OnFrameDataReceivedListener;
import com.advantech.socketcan.SocketCan;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "SocketCanSample";

    private Button openCanButton, sendDataButton, setMaskFilterButton, clearResultButton;
    private EditText canIdEdittext, canDataEdittext;
    private CheckBox isCallBackModeCbx, isExtendedCbx, isRemoteCbx;
    private Spinner baudrateSpinner;
    private RecyclerView mRecyclerView;

    private ResultAdapter adapter;
    private List<CanFrame> canFrames = new ArrayList<>();;

    private boolean isOpened;

    private String[] baudrateArray;

    private SocketCan socketCan0;

    private String baudrate;

    private ReadingThread thread;

    private OnFrameDataReceivedListener onFrameDataReceivedListener;

    private SocketCanManager manager;

    private OnFrameDataReceivedListener listener = new OnFrameDataReceivedListener() {
        @Override
        public void onDataReceived(CanFrame canFrame) {
            updateResult(canFrame);
        }
    };

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        openCanButton = findViewById(R.id.open_btn);
        openCanButton.setOnClickListener(this);
        sendDataButton = findViewById(R.id.send_btn);
        sendDataButton.setOnClickListener(this);
        setMaskFilterButton = findViewById(R.id.setmask_btn);
        setMaskFilterButton.setOnClickListener(this);
        clearResultButton = findViewById(R.id.clear_btn);
        clearResultButton.setOnClickListener(this);

        canIdEdittext = findViewById(R.id.canid_edt);
        canIdEdittext.setHint("0-7FF");
        canIdEdittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(3)});
        canDataEdittext = findViewById(R.id.candata_edt);

        baudrateSpinner = findViewById(R.id.baudrate_sp);
        baudrateSpinner.setOnItemSelectedListener(this);

        isCallBackModeCbx = findViewById(R.id.callback_cbx);
        isCallBackModeCbx.setOnCheckedChangeListener(this);
        isExtendedCbx = findViewById(R.id.extended_cbx);
        isExtendedCbx.setOnCheckedChangeListener(this);
        isRemoteCbx = findViewById(R.id.remote_cbx);
        isRemoteCbx.setOnCheckedChangeListener(this);

        mRecyclerView = findViewById(R.id.result_list_lv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new ResultAdapter(this, canFrames);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        baudrateArray = getResources().getStringArray(R.array.can_speeds);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (R.id.callback_cbx == buttonView.getId()) {
            onFrameDataReceivedListener = isChecked ? listener : null;
        } else if (R.id.extended_cbx == buttonView.getId()) {
            canIdEdittext.setHint(isChecked ? "0～1FFFFFFF" : "0-7FF");
            canIdEdittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(isChecked ? 8 : 3)});
        } else if (R.id.remote_cbx == buttonView.getId()) {
            canDataEdittext.setEnabled(!isChecked);
        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.open_btn == v.getId()) {
            if ("openCan".equals(openCanButton.getText())) {
                open();
            } else {
                close();
            }
        } else if (R.id.send_btn == v.getId()) {
            send();
        } else if (R.id.setmask_btn == v.getId()) {
            if (isOpened) {
                setFilter();
            } else {
                showToast("please open can.");
            }
        } else if (R.id.clear_btn == v.getId()) {
            canFrames.clear();
            adapter.notifyDataSetChanged();
        }
    }

    private void setFilter() {
        Intent intent = new Intent(MainActivity.this, MaskFilterActivity.class);
        intent.putExtra("isExtended", isExtendedCbx.isChecked());
        startActivity(intent);
    }

    private void open() {
        manager = SocketCanManager.getInstance(this, onFrameDataReceivedListener);
//        socketCan0 = new SocketCan(this, onFrameDataReceivedListener);
        socketCan0 = manager.getSocketCan();
        int result = socketCan0.open(0, baudrate);
        if (result == -1) {
            showToast("failed open can0.");
        } else {
            isOpened = true;
            isCallBackModeCbx.setEnabled(false);
            baudrateSpinner.setEnabled(false);
            openCanButton.setText("closeCan");
            showToast("open can0 success.");
            if (onFrameDataReceivedListener == null) {
                if (thread == null) {
                    thread = new ReadingThread();
                }
                if (!thread.isAlive()) {
                    thread.start();
                }
            }
        }
    }

    private void close() {
        if (socketCan0 == null) {
            return;
        }
        socketCan0.close();
        isOpened = false;
        isCallBackModeCbx.setEnabled(true);
        baudrateSpinner.setEnabled(true);
        openCanButton.setText("openCan");

        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        socketCan0 = null;
        if (manager != null) {
            manager.destroy();
            manager = null;
        }

        canFrames.clear();
        adapter.notifyDataSetChanged();
    }

    private class ReadingThread extends Thread {
        @Override
        public void run() {
            while (isOpened && onFrameDataReceivedListener == null) {
                updateResult(socketCan0.recv());
            }
        }
    }

    private void updateResult(CanFrame canFrame) {
        if (canFrame == null) {
            return;
        }
        Log.d(TAG, "updateResult : " + canFrame.toString());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                canFrames.add(canFrame);
                adapter.notifyItemInserted(canFrames.size() - 1);
                mRecyclerView.scrollToPosition(canFrames.size() - 1);
            }
        });
    }

    private void send() {
        String canidStr = canIdEdittext.getText().toString().toLowerCase();
        if (TextUtils.isEmpty(canidStr)) {
            showToast("canid is null.");
            return;
        }
        if (!canidStr.matches("[0-9a-fA-F]+")) {
            showToast("canid format error.");
            return;
        }
        String dataStr = canDataEdittext.getText().toString();
        if (TextUtils.isEmpty(dataStr) && !isRemoteCbx.isChecked()) {
            showToast("can data is empty.");
            return;
        }

        if ((dataStr.length() % 2 != 0)) {
            showToast("can data The data must be even.");
            return;
        }

        int canid = 0;
        try {
            canid = Integer.parseInt(canidStr, 16);
        } catch (NumberFormatException e) {
            showToast("input string " + canidStr + " > 2147483647");
            return;
        }
        if (!isExtendedCbx.isChecked() && canid > 0x7FF) {
            showToast("can id is error. out of range [0-0x7FF]");
            return;
        }
        if (isExtendedCbx.isChecked() && canid > 0x1FFFFFFF) {
            showToast("can id is error.out of range [0-0x1FFFFFFF]");
            return;
        }
        if (canid > 536870911) {
            showToast("can id is error.");
            return;
        }
//        byte[] data = dataStr.getBytes(StandardCharsets.UTF_8);
        byte[] data = StringUtil.hexStringToByteArray(dataStr);
        CanFrame canFrame = new CanFrame(canid, data, data.length, isExtendedCbx.isChecked(), isRemoteCbx.isChecked());
        if (isOpened) {
            int result = socketCan0.send(canFrame);
            if (result > 0) {
                showToast("send can data success.");
            } else {
                showToast("send can data fail.");
            }
        } else {
            showToast("please open can.");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        baudrate = baudrateArray[position];
        Log.d(TAG, "can baudrate : " + baudrate);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close();
    }
}