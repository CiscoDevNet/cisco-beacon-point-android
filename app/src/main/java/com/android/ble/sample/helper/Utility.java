package com.android.ble.sample.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

/**
 * Created by Entappiainc on 30-08-2016.
 */
public class Utility {

    public static boolean isEmpty(Object data) {
        return data == null;
    }

    /**
     * Check String value is empty or not and return a empty string or null based on  isNull parameter
     */
    public static String emptyOrNullString(String value, boolean isNull) {
        if (TextUtils.isEmpty(value) || value.equalsIgnoreCase("null")) {
            if (isNull)
                value = null;
            else
                value = "";
        }
        return value;
    }

    /**
     * Check String value is empty or not and return a empty string
     */

    public static String emptyOrNullString(String value) {
        return emptyOrNullString(value, false);
    }


    public static boolean isEmptyString(String value) {
        return (TextUtils.isEmpty(value) || value.equalsIgnoreCase("null"));
    }

    /**
     * Check Internet is on or off
     */

    public static boolean isNetworkAvailable(Context context) {
        boolean isNet = false;
        if(context!=null) {
            ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connec != null) {
                NetworkInfo result = connec.getActiveNetworkInfo();
                if (result != null && result.isConnectedOrConnecting()) {
                    isNet = true;
                }
            }
        }
        return isNet;
    }
}
