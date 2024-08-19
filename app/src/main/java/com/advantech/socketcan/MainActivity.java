package com.advantech.socketcan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ResultReceiver;
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

import androidx.annotation.Nullable;

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
    private ListView resultListView;

    private ResultAdapter adapter;
    private List<CanFrame> canFrames;

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
        canDataEdittext = findViewById(R.id.candata_edt);

        baudrateSpinner = findViewById(R.id.baudrate_sp);
        baudrateSpinner.setOnItemSelectedListener(this);

        isCallBackModeCbx = findViewById(R.id.callback_cbx);
        isCallBackModeCbx.setOnCheckedChangeListener(this);
        isExtendedCbx = findViewById(R.id.extended_cbx);
        isRemoteCbx = findViewById(R.id.remote_cbx);

        resultListView = findViewById(R.id.result_list_lv);
    }

    @Override
    protected void initData() {
        baudrateArray = getResources().getStringArray(R.array.can_speeds);
        adapter = new ResultAdapter(this);
        canFrames = new ArrayList<>();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        onFrameDataReceivedListener = isChecked ? listener : null;
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
        socketCan0 = null;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        if (manager != null) {
            manager.destroy();
            manager = null;
        }
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
        canFrames.add(canFrame);
        adapter.setDataList(canFrames);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultListView.setAdapter(adapter);
                resultListView.post(new Runnable() {
                    @Override
                    public void run() {
                        resultListView.setSelection(resultListView.getCount() - 1);
                    }
                });
            }
        });
    }

    private void send() {
        String canidStr = canIdEdittext.getText().toString().toLowerCase();
        if (TextUtils.isEmpty(canidStr)) {
            showToast("canid is null.");
            return;
        }
        if (!canidStr.matches("[0-9]+")) {
            showToast("canid format error.");
            return;
        }
        String dataStr = canDataEdittext.getText().toString();
        if (TextUtils.isEmpty(dataStr)) {
            showToast("can data is empty.");
            return;
        }

        if ((dataStr.length() > 8)) {
            showToast("can data length is more than 8.");
            return;
        }

        int canid = 0;
        try {
            canid = Integer.parseInt(canidStr);
        } catch (NumberFormatException e) {
            showToast(e.getMessage() + " > 2147483647");
            return;
        }
        if (!isExtendedCbx.isChecked() && canid > 2047) {
            showToast("can id is error.");
            return;
        }
        if (canid > 536870911) {
            showToast("can id is error.");
            return;
        }
        byte[] data = dataStr.getBytes(StandardCharsets.UTF_8);
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
}