package com.wj.android.wjframe.ui;

import android.view.View;

/**
 * WJFrameActivity接口协议，实现此接口可使用WJActivityManager堆栈
 * Created by wecan-mac on 15/11/30.
 */
public interface I_WJActivity {

    int DESTROY =0;
    int STOP=1;
    int PAUSE=2;
    int RESUME=3;

    /**
     * 设置root界面
     */
    void setRootView();

    /**
     * 初始化数据
     */
    void initData();

    /**
     * 在线程中初始化数据
     */
    void initDataFromThread();

    /**
     * 初始化控件
     */
    void initWidget();

    /**
     * 点击事件回调方法
     */
    void widgetClick(View v);

}
