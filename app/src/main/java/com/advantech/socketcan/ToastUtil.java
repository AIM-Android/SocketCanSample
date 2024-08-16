package com.advantech.socketcan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtil {

    private static final int SHOW_TOP = Gravity.TOP;
    private static final int SHOW_CENTER = Gravity.CENTER;
    private static final int SHOW_START = Gravity.START;
    private static final int SHOW_END = Gravity.END;
    private static final int SHOW_BOTTOM = Gravity.BOTTOM;

    private static final int SHOW_SHORT = Toast.LENGTH_SHORT;
    private static final int SHOW_LONG = Toast.LENGTH_LONG;

    private static Toast toast;
    /**
     * description  显示带位置的Toast
     * param    mContext    context
     * param    text        显示的内容
     * param    resourceId  资源ID
     * param    position    显示位置
     * param    duration    显示时间
     * return
     */
    public static void show(Context mContext, String text, int position, int duration) {
        if (toast != null)
        {
            toast.cancel();
        }
        toast = new Toast(mContext);
        View toastView = LayoutInflater.from(mContext).inflate(R.layout.toast_text_message, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView tvToastMsg = toastView.findViewById(R.id.tv_toast_text_msg);
        //要提示的文本
        tvToastMsg.setText(text);
        //位置
        switch (position)
        {
            case SHOW_TOP:
                toast.setGravity(SHOW_TOP, 0, 0);
                break;

            case SHOW_START:
                toast.setGravity(SHOW_START, 0, 0);
                break;

            case SHOW_END:
                toast.setGravity(SHOW_END, 0, 0);
                break;

            case SHOW_CENTER:
                toast.setGravity(SHOW_CENTER, 0, 0);
                break;

            case SHOW_BOTTOM:
                toast.setGravity(SHOW_BOTTOM, 0, 0);
                break;
            default:
                toast.setGravity(SHOW_BOTTOM, 0, 80);
                break;
        }
        //设置短暂提示
        toast.setDuration(duration);
        //把定义好的View布局设置到Toast里面
        toast.setView(toastView);
        toast.show();
    }

    public static void cancel() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
