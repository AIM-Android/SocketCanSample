package com.advantech.socketcan;

import android.util.Log;

public class StringUtil {
    private static final String TAG = "StringUtil";

    public static int HexToInt(String hex) {
        int value = 0;
        try {
            value = Integer.parseInt(hex);
        } catch (NumberFormatException exception) {
            Log.e(TAG, exception.getMessage() + hex + " > 2147483647");
            return -1;
        }
        return value;
    }

    public static int HexToInt(String hex, int radix) {
        int value = 0;
        try {
            value = Integer.parseInt(hex, radix);
        } catch (NumberFormatException exception) {
            Log.e(TAG, exception.getMessage() + hex + " > 2147483647");
            return -1;
        }
        return value;
    }
}
