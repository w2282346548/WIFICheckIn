package com.wj.android.wjframe.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.wj.android.wjframe.utils.WJLoger;

import java.lang.ref.SoftReference;

/**
 * Created by wecan-mac on 15/12/3.
 */
public abstract class WJActivity extends AppCompatActivity implements View.OnClickListener,I_WJActivity,I_BroadcastReg,I_SkipActivity{

    public  static  final int WHICH_MSG=0x37210;

    public Activity aty;

    protected WJFragment currentWJFragment;

    private ThreadDataCallBack callBack;

    private WJActivityHandle threadHandle=new WJActivityHandle(this);

    /**
     * Activity状态
     */
    public int activityState = DESTROY;

    private  interface ThreadDataCallBack{
        void onSuccess();
    }

    private class WJActivityHandle extends Handler {

        private final SoftReference<WJActivity> mOuterInstance;

        public WJActivityHandle(WJActivity outer){
            mOuterInstance=new SoftReference<WJActivity>(outer);
        }

        @Override
        public void handleMessage(Message msg) {

            WJActivity activity=mOuterInstance.get();
            if(msg.what==WHICH_MSG&&activity !=null){
                activity.callBack.onSuccess();
            }
        }
    }


    /**
     * 如果调用了initDataFromThread()，则当数据初始化完成后将回调该方法。
     */
    protected void threadDataInited() {
    }

    /**
     * 在线程中初始化数据，注意不能在这里执行UI操作
     */
    @Override
    public void initDataFromThread() {
        callBack = new ThreadDataCallBack() {
            @Override
            public void onSuccess() {
                threadDataInited();
            }
        };
    }

    @Override
    public void initData() {

    }

    @Override
    public void initWidget() {

    }


    // 仅仅是为了代码整洁点
    private void initializer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initDataFromThread();
                threadHandle.sendEmptyMessage(WHICH_MSG);
            }
        }).start();
        initData();
        initWidget();
    }

    @Override
    public void widgetClick(View v) {

    }

    @Override
    public void onClick(View v) {
        widgetClick(v);
    }

    @Override
    public void registerBroadcast() {
    }

    @Override
    public void unRegisterBroadcast() {
    }

    /***************************************************************************
     * print Activity callback methods
     ***************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        aty=this;
        WJActivityStack.create().addActivity(this);
        WJLoger.state(this.getClass().getName(), "-------onCreate ");
        super.onCreate(savedInstanceState);

        setRootView();

        initializer();
        registerBroadcast();
    }

    @Override
    protected void onStart() {
        super.onStart();
        WJLoger.state(this.getClass().getName(), "---------onStart ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityState = RESUME;
        WJLoger.state(this.getClass().getName(), "---------onResume ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityState = PAUSE;
        WJLoger.state(this.getClass().getName(), "---------onPause ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        activityState = STOP;
        WJLoger.state(this.getClass().getName(), "---------onStop ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        WJLoger.state(this.getClass().getName(), "---------onRestart ");
    }


    @Override
    protected void onDestroy() {
        unRegisterBroadcast();
        activityState = DESTROY;
        WJLoger.state(this.getClass().getName(), "---------onDestroy ");
        super.onDestroy();
        WJActivityStack.create().finishActivity(this);
        currentWJFragment = null;
        callBack = null;
        threadHandle = null;
        aty = null;
    }

    /**
     * skip to @param(cls)，and call @param(aty's) finish() method
     */
    @Override
    public void skipActivity(Activity aty, Class<?> cls) {
        showActivity(aty, cls);
        aty.finish();
    }

    /**
     * skip to @param(cls)，and call @param(aty's) finish() method
     */
    @Override
    public void skipActivity(Activity aty, Intent it) {
        showActivity(aty, it);
        aty.finish();
    }

    /**
     * skip to @param(cls)，and call @param(aty's) finish() method
     */
    @Override
    public void skipActivity(Activity aty, Class<?> cls, Bundle extras) {
        showActivity(aty, cls, extras);
        aty.finish();
    }

    /**
     * show to @param(cls)，but can't finish activity
     */
    @Override
    public void showActivity(Activity aty, Class<?> cls) {
        Intent intent = new Intent();
        intent.setClass(aty, cls);
        aty.startActivity(intent);
    }

    /**
     * show to @param(cls)，but can't finish activity
     */
    @Override
    public void showActivity(Activity aty, Intent it) {
        aty.startActivity(it);
    }

    /**
     * show to @param(cls)，but can't finish activity
     */
    @Override
    public void showActivity(Activity aty, Class<?> cls, Bundle extras) {
        Intent intent = new Intent();
        intent.putExtras(extras);
        intent.setClass(aty, cls);
        aty.startActivity(intent);
    }

    /**
     * 用Fragment替换视图
     *
     * @param resView        将要被替换掉的视图
     * @param targetFragment 用来替换的Fragment
     */
    public void changeFragment(int resView, WJFragment targetFragment) {
        if (targetFragment.equals(currentWJFragment)) {
            return;
        }
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        if (!targetFragment.isAdded()) {
            transaction.add(resView, targetFragment, targetFragment.getClass()
                    .getName());
        }
        if (targetFragment.isHidden()) {
            transaction.show(targetFragment);
            targetFragment.onChange();
        }
        if (currentWJFragment != null && currentWJFragment.isVisible()) {
            transaction.hide(currentWJFragment);
        }
        currentWJFragment = targetFragment;
        transaction.commit();
    }

    /**
//     * 用Fragment替换视图
//     *
//     * @param resView        将要被替换掉的视图
//     * @param targetFragment 用来替换的Fragment
//     */
//    public void changeFragment(int resView, SupportFragment targetFragment) {
//        if (targetFragment.equals(currentSupportFragment)) {
//            return;
//        }
//        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager()
//                .beginTransaction();
//        if (!targetFragment.isAdded()) {
//            transaction.add(resView, targetFragment, targetFragment.getClass()
//                    .getName());
//        }
//        if (targetFragment.isHidden()) {
//            transaction.show(targetFragment);
//            targetFragment.onChange();
//        }
//        if (currentSupportFragment != null
//                && currentSupportFragment.isVisible()) {
//            transaction.hide(currentSupportFragment);
//        }
//        currentSupportFragment = targetFragment;
//        transaction.commit();
//    }

}
