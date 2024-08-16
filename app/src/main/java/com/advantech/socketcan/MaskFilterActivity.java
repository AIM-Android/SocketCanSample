package com.advantech.socketcan;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.advantech.socketcan.baseui.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaskFilterActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MaskFilterActivity";

    private Button setButton, getButton, removeButton, resetButton;
    private EditText filterId1Edittext, mask1Edittext, filterId2Edittext, mask2Edittext, filterIdEdittext;
    private TextView resultTextView;

    private SocketCan socketCan0;
    private Map<Integer, Mask> maskMap;
    private boolean isExtended;


    @Override
    protected int getLayoutResID() {
        return R.layout.activity_mask_filter;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setButton = findViewById(R.id.set_tv);
        setButton.setOnClickListener(this);
        getButton = findViewById(R.id.get_tv);
        getButton.setOnClickListener(this);
        removeButton = findViewById(R.id.remove_tv);
        removeButton.setOnClickListener(this);
        resetButton = findViewById(R.id.reset_tv);
        resetButton.setOnClickListener(this);


        filterId1Edittext = findViewById(R.id.filterId1_edt);
        mask1Edittext = findViewById(R.id.mask1_edt);
        filterId2Edittext = findViewById(R.id.filterId2_edt);
        mask2Edittext = findViewById(R.id.mask2_edt);
        filterIdEdittext = findViewById(R.id.filterId_edt);

        resultTextView = findViewById(R.id.mask_result_tv);
    }

    @Override
    protected void initData() {
        maskMap = new HashMap<>();
        SocketCanManager manager = SocketCanManager.getInstance(this, null);
        socketCan0 = manager.getSocketCan();

        Mask mask = socketCan0.getAllMaskFilter();
        if (mask == null) {
            return;
        }
        if (mask.getFilterId1() != -1) {
            filterId1Edittext.setText(String.valueOf(mask.getFilterId1()));
            mask1Edittext.setText(Integer.toHexString(mask.getMask1()).toUpperCase());
        }
        if (mask.getFilterId2() != -1) {
            filterId2Edittext.setText(String.valueOf(mask.getFilterId2()));
            mask2Edittext.setText(Integer.toHexString(mask.getMask2()).toUpperCase());
        }

//        Bundle bundle = getIntent().getExtras();
//        isExtended = bundle.getBoolean("isExtended");
//        maskMap = (Map<Integer, Mask>) bundle.getSerializable("maskMap");
//        for (Map.Entry<Integer, Mask> entry : maskMap.entrySet()) {
//            Mask mask = (Mask) entry.getValue();
//            if (mask.isGroup1Valid()) {
//                filterId1Edittext.setText(String.valueOf(mask.getFilterId1()));
//                mask1Edittext.setText(Integer.toHexString(mask.getMask1()).toUpperCase());
//            }
//
//            if (mask.isGroup2Valid()) {
//                filterId2Edittext.setText(String.valueOf(mask.getFilterId2()));
//                mask2Edittext.setText(Integer.toHexString(mask.getMask2()).toUpperCase());
//            }
//        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.set_tv == v.getId()) {
            Mask mask = new Mask();
            String filterId1Str = filterId1Edittext.getText().toString().toLowerCase();
            String mask1Str = mask1Edittext.getText().toString().toLowerCase();
            String filterId2Str = filterId2Edittext.getText().toString().toLowerCase();
            String mask2Str = mask2Edittext.getText().toString().toLowerCase();
            if (!TextUtils.isEmpty(filterId1Str) && !TextUtils.isEmpty(mask1Str)) {
                int filterId1 = Integer.parseInt(filterId1Str);
                int mask1 = Integer.parseInt(mask1Str, 16);
                mask.setFilterId1(filterId1);
                mask.setMask1(mask1);
            }

            if (!TextUtils.isEmpty(filterId2Str) && !TextUtils.isEmpty(mask2Str)) {
                int filterId2 = Integer.parseInt(filterId2Str);
                int mask2 = Integer.parseInt(mask2Str, 16);
                mask.setFilterId2(filterId2);
                mask.setMask2(mask2);
            }
            socketCan0.setMaskFilter(mask);
        } else if (R.id.get_tv == v.getId()) {
//            if (!TextUtils.isEmpty(filterIdEdittext.getText())) {
//                int filterId = Integer.parseInt(filterIdEdittext.getText().toString());
//                Mask result = socketCan0.getMaskFilter(filterId);
//                if (result != null) {
//                    resultTextView.setText(result.toString());
//                }
//            }
        } else if (R.id.remove_tv == v.getId()) {
            if (!TextUtils.isEmpty(filterIdEdittext.getText())) {
                int filterId = Integer.parseInt(filterIdEdittext.getText().toString());
                socketCan0.removeMaskFilter(filterId);
                updateResult();
            }
        } else if (R.id.reset_tv == v.getId()) {
            socketCan0.clearAndResetMaskFilter();
//            updateResult();
        }
    }

    private void updateResult() {}
}