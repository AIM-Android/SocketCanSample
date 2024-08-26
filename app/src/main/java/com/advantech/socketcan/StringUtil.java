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

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] byteArray) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];

        for ( int j = 0; j < byteArray.length; j++ ) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2]     = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String byteArrayToHexString(byte[] byteArray, int convertLength) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];

        for ( int j = 0; j < byteArray.length && j < convertLength; j++ ) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2]     = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars).trim();
    }
}
