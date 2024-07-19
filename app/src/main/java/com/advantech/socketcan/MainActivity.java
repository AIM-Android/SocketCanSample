package com.advantech.socketcan;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CanTestTool";

    private static final String CAN_PORT = "can0";
    private static final String CAN_SPEED = "persist.sys.can_speed";

    private TextView receiveTextView;
    private Button openButton, sendButton, clearButton;
    private Spinner canSpeedSpinner;
    private EditText canidEdittext, candataEdittext;
    private CheckBox checkBox;

    private String mCanX;
    private String[] mCanSpeedArray;
    private String mSpeed;

    private SocketCan socketCan;
    private RecvCanDataThread thread;
    private OnFrameDataReceivedListener mListener;

    private boolean isCanOpen;
    private int sendCount = 0;
    private int recvCount = 0;
    private TextView sendCountTv, recvCountTv;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initStringArray();
        mCanX = CAN_PORT;
        initView();
    }

    private OnFrameDataReceivedListener listener = new OnFrameDataReceivedListener() {
        @Override
        public void onDataReceived(CanFrame canFrame) {
            update(canFrame);
        }
    };

    private void initStringArray() {
        mCanSpeedArray = getResources().getStringArray(R.array.can_speeds);
    }

    private void initView() {
        scrollView = findViewById(R.id.recv_scrollView);
        receiveTextView = findViewById(R.id.receive_tv);
        openButton = findViewById(R.id.can_open_btn);
        sendButton = findViewById(R.id.can_send_btn);
        clearButton = findViewById(R.id.clear_btn);

        openButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);

        canSpeedSpinner = findViewById(R.id.can_speed_sp);
        canSpeedSpinner.setSelection(0);
        canSpeedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSpeed = mCanSpeedArray[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        canidEdittext = findViewById(R.id.can_id_edt);
        candataEdittext = findViewById(R.id.can_data_edt);

        checkBox = findViewById(R.id.checkbox_btn);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "checkbox");
                mListener = isChecked ? listener : null;
                sendCount = 0;
                recvCount = 0;
            }
        });

        sendCountTv = findViewById(R.id.send_count_tv);
        recvCountTv = findViewById(R.id.recv_count_tv);
    }

    @Override
    public void onClick(View v) {
        if (R.id.can_open_btn == v.getId()) {
            if ("open".equals(openButton.getText())) {
                openCan();
            } else {
                closeCan();
            }
        } else if (R.id.can_send_btn == v.getId()) {
            // send
            sendCanData();
        } else if (R.id.clear_btn == v.getId()) {
            receiveTextView.setText("");
        }
    }

    private void closeCan() {
        Log.d(TAG, "SocketCan : " + socketCan.toString());
        isCanOpen = false;
        canidEdittext.setEnabled(true);
        checkBox.setEnabled(true);
        canSpeedSpinner.setEnabled(true);
        openButton.setText("open");
        receiveTextView.setText("");
        sendCount = 0;
        recvCount = 0;
        sendCountTv.setText(getString(R.string.hint__1_100));
        recvCountTv.setText(getString(R.string.hint__1_100));
        if (socketCan != null) {
            Log.d(TAG, "close : " + socketCan.close());
            socketCan = null;
        }
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    private void openCan() {
        if (TextUtils.isEmpty(canidEdittext.getText())) {
            ToastUtil.show(this, "can id is empty.", Gravity.CENTER, 0);
            return;
        }
        if (!canidEdittext.getText().toString().toUpperCase().matches("[0-9]+")) {
            ToastUtil.show(this, "CAN ID FORMAT ERROR.", Gravity.CENTER, 0);
            return;
        }
        int canid = Integer.parseInt(canidEdittext.getText().toString().toUpperCase());
        if (canid > 2048 || canid < 0) {
            ToastUtil.show(this, "ID FORMAT ERROR.", Gravity.CENTER, 0);
            return;
        }
        if (socketCan == null) {
            socketCan = new SocketCan(this, mListener);
        }
        int reault = socketCan.open(0, mSpeed);
        if (reault == -1) {
            ToastUtil.show(this, "open " + mCanX + " fail.", Gravity.CENTER, 0);
        } else {
            canidEdittext.setEnabled(false);
            checkBox.setEnabled(false);
            canSpeedSpinner.setEnabled(false);
            isCanOpen = true;
            openButton.setText("close");
            ToastUtil.show(this, "open " + mCanX + " succ.", Gravity.CENTER, 0);
            if (mListener == null) {
                startReceiveThread();
            }
        }
    }

    private void sendCanData() {
        if (TextUtils.isEmpty(canidEdittext.getText())) {
            ToastUtil.show(this, "can id is empty.", Gravity.CENTER, 0);
            return;
        }
        if (TextUtils.isEmpty(candataEdittext.getText())) {
            ToastUtil.show(this, "can data is empty.", Gravity.CENTER, 0);
            return;
        }

        if (!candataEdittext.getText().toString().toUpperCase().matches("[0-9a-fA-F]+")) {
            ToastUtil.show(this, "DATA FORMAT ERROR.", Gravity.CENTER, 0);
            return;
        }
        int canid = StringUtils.HexToInt(canidEdittext.getText().toString());
        if (isCanOpen) {
            byte[] arr = candataEdittext.getText().toString().getBytes(StandardCharsets.UTF_8);
            int result = socketCan.send(new CanFrame(canid, arr, arr.length));
            if (result > 0) {
                sendCount++;
                sendCountTv.setText(sendCount + "/100");
                ToastUtil.show(this, "can data send succ.", Gravity.CENTER, 0);
            } else {
                ToastUtil.show(this, "can data send fail.", Gravity.CENTER, 0);
            }
        } else {
            ToastUtil.show(this, "please open can.", Gravity.CENTER, 0);
        }
    }

    private void startReceiveThread() {
        if (thread == null) {
            thread = new RecvCanDataThread();
        }
        if (!thread.isAlive()) {
            thread.start();
        }
    }

    private class RecvCanDataThread extends Thread {
        @Override
        public void run() {
            while (isCanOpen && mListener == null) {
                Log.d(TAG, "RecvCanDataThread is running.");
                onDataReceived(socketCan.recv());
            }
        }
    }

    private void onDataReceived(CanFrame canFrame) {
        update(canFrame);
    }

    private void update(CanFrame canFrame) {
        if (canFrame == null) {
            return;
        }
        String data = new String(canFrame.getData(), StandardCharsets.UTF_8);
        if (data.length() <= 0) {
            return;
        }
        recvCount++;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recvCountTv.setText(recvCount + "/100");
                receiveTextView.append(data + "\n");
                receiveTextView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                }, 100);
            }
        });
    }
}