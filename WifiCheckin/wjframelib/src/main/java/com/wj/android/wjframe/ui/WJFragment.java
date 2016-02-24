package com.wj.android.wjframe.ui;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.SoftReference;

/**
 * Created by wecan-mac on 15/11/30.
 */
public abstract class WJFragment extends Fragment implements View.OnClickListener {
    public static final int WHICH_MSG = 0X37211;
    protected View fragementRootView;
    private ThreadDataCallBack callBack;

    private WJFragmentHandle threadhandle=new WJFragmentHandle(this);

    private interface  ThreadDataCallBack{
        void onSuccess();
    }

    private static class WJFragmentHandle extends Handler {

        private final SoftReference<WJFragment> mOuterInstanse;

        WJFragmentHandle(WJFragment outer){
            mOuterInstanse =new SoftReference<WJFragment>(outer);
        }

        // 当线程中初始化的数据初始化完成后，调用回调方法
        @Override
        public void handleMessage(Message msg) {
            WJFragment wjFragment=mOuterInstanse.get();
            if(msg.what==WHICH_MSG && wjFragment!=null){
                wjFragment.callBack.onSuccess();
            }
        }
    }

    protected abstract View inflaterView(LayoutInflater inflater,ViewGroup container,Bundle bundle);

    /*
     *initialization widget, you should use look like parentView.findviewbyid(id);
     *
     * call method
     * */
    protected void initWidget(View parentView){

    }

    /**
     * initialization data
     */
    protected void initData(){

    }

    /*
    * 当通过changeFragment（）显示时会调用（类似onResume）
    * */
    public void onChange(){

    }

    /**
     * initialization data. And this method run in background thread, so you
     * shouldn't change ui<br>
     * on initializated, will call threadDataInited();
     */
    protected void initDataFromThread(){
        callBack=new ThreadDataCallBack() {
            @Override
            public void onSuccess() {
                threadDataInited();
            }
        };
    }

    /**
     * 如果调用了initDataFromThread()，则当数据初始化完成后将回调该方法。
     */
    protected void threadDataInited() {
    }


    /**
     * widget click method
     */
    protected void widgetClick(View v) {

    }

    @Override
    public void onClick(View v) {
        widgetClick(v);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragementRootView=inflaterView(inflater,container,savedInstanceState);

        initData();
        initWidget(fragementRootView);
        new Thread(new Runnable() {
            @Override
            public void run() {
                initDataFromThread();
                threadhandle.sendEmptyMessage(WHICH_MSG);
            }
        }).start();

        return fragementRootView;
    }
}
