package com.advantech.socketcan;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;

/**
 * socketcan-android sample
 * can interface can0
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CanTestTool";

    private static final String CAN_PORT = "can0";

    private TextView receiveTextView;
    private Button openButton, sendButton, clearButton;
    private Spinner canSpeedSpinner;
    private EditText canidEdittext, candataEdittext;;

    /**
     * can0
     */
    private String mCanX;

    /**
     * 100000 250000
     */
    private String[] mCanSpeedArray;

    /**
     * 帧结构体与 struct can_frame 对应
     */
    private CanFrame mCanFrame;

    /**
     * 数据接收线程
     */
    private RecvCanDataThread thread;

    /**
     * 标记 can0 状态
     */
    private boolean isCanOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initStringArray();
        // default can interface can0
        mCanX = CAN_PORT;
        initView();

        mCanFrame = new CanFrame();
    }

    private void initStringArray() {
        mCanSpeedArray = getResources().getStringArray(R.array.can_speeds);
    }

    private void initView() {
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
                // config can0
                configCan(mCanSpeedArray[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        canidEdittext = findViewById(R.id.can_id_edt);
        candataEdittext = findViewById(R.id.can_data_edt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCan();
    }

    @Override
    public void onClick(View v) {
        if (R.id.can_open_btn == v.getId()) {
            if ("open".equals(openButton.getText())) {
                // open can0
                openCan();
            } else {
                // close can0
                closeCan();
            }
        } else if (R.id.can_send_btn == v.getId()) {
            // send frame to can0
            sendCanData();
        } else if (R.id.clear_btn == v.getId()) {
            receiveTextView.setText("");
        }
    }

    private void closeCan() {
        isCanOpen = false;
        openButton.setText("open");
        SocketCan.closeCan();
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    private void configCan(String speed) {
        // native configCan
        SocketCan.configCan(this, speed);
    }

    private void openCan() {
        if (SocketCan.openCan(mCanX) > 0) {
            isCanOpen = true;
            ToastUtil.show(this, "open " + mCanX + " succ.", Gravity.CENTER, 0);
            startReceiveThread();
        } else {
            ToastUtil.show(this, "open " + mCanX + " fail.", Gravity.CENTER, 0);
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

        int canid = StringUtils.HexToInt(canidEdittext.getText().toString());
        if (isCanOpen) {
            byte[] arr = candataEdittext.getText().toString().getBytes(StandardCharsets.UTF_8);
            boolean isSucc = SocketCan.send(canid, mCanX, arr) > 0;
            if (isSucc) {
                ToastUtil.show(this, "can data send succ.", Gravity.CENTER, 0);
            } else {
                ToastUtil.show(this, "can data send fail.", Gravity.CENTER, 0);
            }
        } else {
            ToastUtil.show(this, "plese open can.", Gravity.CENTER, 0);
        }
    }

    private void startReceiveThread() {
        if (TextUtils.isEmpty(canidEdittext.getText())) {
            ToastUtil.show(this, "can id is null.", Gravity.CENTER, 0);
            return;
        }
        openButton.setText("close");
        mCanFrame.id = StringUtils.HexToInt(canidEdittext.getText().toString());
        Log.d(TAG, "startsss");
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
            while (isCanOpen) {
                Log.d(TAG, "RecvCanDataThread is running.");
                onDataReceived(SocketCan.recv(mCanFrame, mCanX));
            }
        }
    }

    private void onDataReceived(CanFrame frame) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String data = new String(frame.data, StandardCharsets.UTF_8);
                Log.d(TAG, "data : " + data.length());
                if (data.length() <= 0) {
                    return;
                }
                receiveTextView.append(data + "\n");
            }
        });
    }
}