package com.advantech.socketcan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.advantech.socketcan.MaskBean;
import com.advantech.socketcan.R;

import java.util.List;

public class MaskResultAdapter extends BaseAdapter {

    private final Context context;
    private List<MaskBean> dataList;

    public void setDataList(List<MaskBean> dataList) {
        this.dataList = dataList;
    }

    public MaskResultAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList == null ? null : dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_mask_result, null);
            holder = new ViewHolder();
            holder.filterIdTv = convertView.findViewById(R.id.filter_id_tv);
            holder.maskTv = convertView.findViewById(R.id.mask_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final MaskBean bean = dataList.get(position);
        if (bean != null) {
            holder.filterIdTv.setText(String.format("%X", bean.getFilterId()));
            holder.maskTv.setText(String.format("%X", bean.getMask()));
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView filterIdTv;
        TextView maskTv;
    }
}
