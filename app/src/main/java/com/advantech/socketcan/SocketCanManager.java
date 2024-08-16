package com.advantech.socketcan;

import android.content.Context;

import java.lang.ref.WeakReference;

public class SocketCanManager {
    private static volatile SocketCanManager instance;
    private WeakReference<SocketCan> socketCanRef;
    private WeakReference<Context> contextRef;
    private OnFrameDataReceivedListener onFrameDataReceivedListener;

    private SocketCanManager(Context context, OnFrameDataReceivedListener listener) {
        this.contextRef = new WeakReference<>(context.getApplicationContext());
        this.onFrameDataReceivedListener = listener;
        this.socketCanRef = new WeakReference<>(new SocketCan(this.contextRef.get(), this.onFrameDataReceivedListener));
    }

    public static SocketCanManager getInstance(Context context, OnFrameDataReceivedListener listener) {
        if (instance == null) {
            synchronized (SocketCanManager.class) {
                if (instance == null) {
                    instance = new SocketCanManager(context, listener);
                }
            }
        }
        return instance;
    }

    public SocketCan getSocketCan() {
        return socketCanRef.get();
    }

    public void destroy() {
        socketCanRef.clear();
        contextRef.clear();
        instance = null;
    }
}
