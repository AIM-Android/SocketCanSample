package com.advantech.socketcan;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.advantech.socketcan.adapter.MaskResultAdapter;
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
    private boolean isExtended;

    private ListView listView;
    private MaskResultAdapter adapter;

    List<MaskBean> dataList;


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
        listView = findViewById(R.id.mask_result_list_lv);
    }

    @Override
    protected void initData() {
        isExtended = getIntent().getBooleanExtra("isExtended", false);
        SocketCanManager manager = SocketCanManager.getInstance(this, null);
        socketCan0 = manager.getSocketCan();
        adapter = new MaskResultAdapter(this);
        dataList = new ArrayList<>();

        filterId1Edittext.setHint(isExtended ? "0～1FFFFFFF" : "0-7FF");
        filterId2Edittext.setHint(isExtended ? "0～1FFFFFFF" : "0-7FF");
        filterIdEdittext.setHint(isExtended ? "0～1FFFFFFF" : "0-7FF");

        filterId1Edittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(isExtended ? 8 : 3)});
        filterId2Edittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(isExtended ? 8 : 3)});
        filterIdEdittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(isExtended ? 8 : 3)});

        Mask mask = socketCan0.getAllMaskFilter();
        if (mask == null) {
            return;
        }
        if (mask.isGroup1Valid()) {
            filterId1Edittext.setText(String.format("%X", mask.getFilterId1()));
            mask1Edittext.setText(Integer.toHexString(mask.getMask1()).toUpperCase());
        }
        if (mask.isGroup2Valid()) {
            filterId2Edittext.setText(String.format("%X", mask.getFilterId2()));
            mask2Edittext.setText(Integer.toHexString(mask.getMask2()).toUpperCase());
        }
        listView.setAdapter(adapter);
        update();
    }

    @Override
    public void onClick(View v) {
        if (R.id.set_tv == v.getId()) {
            setMaskFilter();
        } else if (R.id.get_tv == v.getId()) {
            getMaskFilter();
        } else if (R.id.remove_tv == v.getId()) {
            if (!TextUtils.isEmpty(filterIdEdittext.getText())) {
                int filterId = Integer.parseInt(filterIdEdittext.getText().toString(), 16);
                int result = socketCan0.removeMaskFilter(filterId);
                Log.d(TAG, "removeMaskFilter result : " + result);
                if (result >= 0) {
                    update();
                    showToast("remove OK");
                } else {
                    showToast("filterId is not exist.");
                }
            } else {
                showToast("filterId is null.");
            }
        } else if (R.id.reset_tv == v.getId()) {
            Log.d(TAG, "clearAndResetMaskFilter result : " + socketCan0.clearAndResetMaskFilter());
            dataList.clear();
            adapter.notifyDataSetChanged();
            showToast("reset OK");
        }
    }

    private void update() {
        Mask result = socketCan0.getAllMaskFilter();
        dataList.clear();
        if (result == null) {
            return;
        }
        if (result.isGroup1Valid()) {
            dataList.add(new MaskBean(result.getFilterId1(), result.getMask1()));
        }

        if (result.isGroup2Valid()) {
            dataList.add(new MaskBean(result.getFilterId2(), result.getMask2()));
        }
        adapter.setDataList(dataList);
        listView.setAdapter(adapter);
    }

    private void setMaskFilter() {
        Mask mask = new Mask();
        String filterId1Str = filterId1Edittext.getText().toString();
        String mask1Str = mask1Edittext.getText().toString();
        String filterId2Str = filterId2Edittext.getText().toString();
        String mask2Str = mask2Edittext.getText().toString();
        if (!TextUtils.isEmpty(filterId1Str) && !TextUtils.isEmpty(mask1Str)) {
            int filterId1 = StringUtil.HexToInt(filterId1Str, 16);
            int mask1 = StringUtil.HexToInt(mask1Str, 16);
            if (mask1 == -1) {
                showToast("input string " + mask1Str + " > 2147483647");
                return;
            }
            mask.setFilterId1(filterId1);
            mask.setMask1(mask1);
            if (!checkId(mask.getFilterId1())) {
                return;
            }
        }

        if (!TextUtils.isEmpty(filterId2Str) && !TextUtils.isEmpty(mask2Str)) {
            int filterId2 = StringUtil.HexToInt(filterId2Str, 16);
            int mask2 = StringUtil.HexToInt(mask2Str, 16);
            if (mask2 == -1) {
                showToast("input string " + mask2Str + " > 2147483647");
                return;
            }
            mask.setFilterId2(filterId2);
            mask.setMask2(mask2);
            if (!checkId(mask.getFilterId2())) {
                return;
            }
        }
        if (!mask.isGroup1Valid() && !mask.isGroup2Valid()) {
            return;
        }
        int result = socketCan0.setMaskFilter(mask);
        if (result == ErrorCodeEnum.ERR_INVALID_PARAMETER.getErrorCode()) {
            showToast("filterId1 is equal filterId2");
            return;
        } else if (result >= 0) {
            update();
            showToast("set OK");
        } else {
            showToast("set failed.");
        }
    }

    private boolean checkId(int filterId) {
        if (!isExtended && (filterId < 0 || filterId > 0x7FF)) {
            showToast("can id is error. out of range [0-0x7FF]");
            return false;
        }
        if (isExtended && (filterId < 0 || filterId > 0x1FFFFFFF)) {
            showToast("can id is error.out of range [0-0x1FFFFFFF]");
            return false;
        }
        return true;
    }

    private void getMaskFilter() {
        if (!TextUtils.isEmpty(filterIdEdittext.getText())) {
            int filterId = StringUtil.HexToInt(filterIdEdittext.getText().toString(), 16);
            if (filterId == -1) {
                showToast("input string " + filterId + " > 2147483647");
                return;
            }
            Mask result = socketCan0.getAllMaskFilter();
            if (result != null) {
                if (filterId == result.getFilterId1()) {
                    resultTextView.setText(String.format("%X", result.getMask1()));
                    showToast("get OK");
                } else if (filterId == result.getFilterId2()) {
                    resultTextView.setText(String.format("%X", result.getMask2()));
                    showToast("get OK");
                } else {
                    resultTextView.setText("");
                    showToast("get result is null");
                }
            } else {
                resultTextView.setText("");
                showToast("get result is null");
            }
        } else {
            showToast("filterId is null.");
        }
    }
}