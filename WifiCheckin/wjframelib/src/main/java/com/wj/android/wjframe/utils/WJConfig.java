package com.wj.android.wjframe.utils;

/**
 * Created by wecan-mac on 15/11/27.
 */
public final class WJConfig {

    public static final double VERSION =1.000;

    /**
     * 错误处理广播
     */
    public  static  final String RECEIVER_ERROR=WJConfig.class.getName()+"com.wj.android.frame.error";


    /**
     * 无网络警告广播
     */
    public static final String RECEIVER_NOT_NET_WARN = WJConfig.class.getName()
            + "com.wj.android.frame.notnet";

    /**
     * preference键值对
     */
    public static final String SETTING_FILE = "wjframe_preference";
    public static final String ONLY_WIFI = "only_wifi";
}
