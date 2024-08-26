package com.advantech.socketcan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.advantech.socketcan.R;
import com.advantech.socketcan.CanFrame;
import com.advantech.socketcan.StringUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ResultAdapter extends BaseAdapter {

    private final Context context;
    private List<CanFrame> dataList;

    public void setDataList(List<CanFrame> dataList) {
        this.dataList = dataList;
    }

    public ResultAdapter(Context context) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_result, null);
            holder = new ViewHolder();
            holder.idTv = convertView.findViewById(R.id.canid_tv);
            holder.lenTv = convertView.findViewById(R.id.can_len_tv);
            holder.dataTv = convertView.findViewById(R.id.can_data_tv);
            holder.extendedTv = convertView.findViewById(R.id.extended_tv);
            holder.remoteTv = convertView.findViewById(R.id.remote_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final CanFrame bean = dataList.get(position);
        if (bean != null) {
            holder.idTv.setText(String.format("%X", bean.getCanId()));
            holder.lenTv.setText(String.valueOf(bean.getCanDlc()));
            String data = StringUtil.byteArrayToHexString(bean.getData())
                    .replaceAll("(.{2})", "$1 ")
                    .replaceAll(" $", "");
            holder.dataTv.setText(data);
            holder.extendedTv.setText(String.valueOf(bean.isExtended()));
            holder.remoteTv.setText(String.valueOf(bean.isRemote()));
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView idTv;
        TextView lenTv;
        TextView dataTv;
        TextView extendedTv;
        TextView remoteTv;
    }
}
