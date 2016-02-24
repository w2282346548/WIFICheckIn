package com.wj.android.wjframe.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by wecan on 2016/2/23.
 */
public class PhoneUtils {


    /**
     * 返回手机的IMEI码
     */
    public static String getPhoneNum(Context cxt){
        TelephonyManager telephonyManager= (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getLine1Number();
    }
}
