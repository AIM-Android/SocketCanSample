package com.advantech.socketcan.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.advantech.socketcan.R;
import com.advantech.socketcan.CanFrame;
import com.advantech.socketcan.StringUtil;

import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private static final String TAG = ResultAdapter.class.getSimpleName();

    private final Context context;
    private List<CanFrame> dataList;

    public ResultAdapter(Context context, List<CanFrame> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CanFrame bean = dataList.get(position);
        Log.d(TAG, "bean : " + bean.toString());
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
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView idTv;
        TextView lenTv;
        TextView dataTv;
        TextView extendedTv;
        TextView remoteTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            idTv = itemView.findViewById(R.id.canid_tv);
            lenTv = itemView.findViewById(R.id.can_len_tv);
            dataTv = itemView.findViewById(R.id.can_data_tv);
            extendedTv = itemView.findViewById(R.id.extended_tv);
            remoteTv = itemView.findViewById(R.id.remote_tv);
        }
    }
}
