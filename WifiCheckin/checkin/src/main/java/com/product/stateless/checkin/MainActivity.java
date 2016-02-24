package com.product.stateless.checkin;

import android.content.DialogInterface;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;


import com.product.stateless.checkin.config.Config;
import com.product.stateless.checkin.model.PostParam;
import com.wj.android.wjframe.WJHttp;
import com.wj.android.wjframe.http.HttpCallBack;
import com.wj.android.wjframe.http.HttpParams;
import com.wj.android.wjframe.utils.PhoneUtils;
import com.wj.android.wjframe.utils.PreferenceHelper;
import com.wj.android.wjframe.utils.SystemTool;
import com.wj.android.wjframe.utils.WJLoger;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button checkIn;
    private Button checkOut;
    private Button checkSetting;
    private boolean isCheckIn;
    private boolean isFirstUse;
    private RelativeLayout rllRootView;

    private Handler hanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    checkSetting.setEnabled(false);
                    PreferenceHelper.write(MainActivity.this, Config.SETTING_FILE, Config.IS_FIRST_USE, false);
                    checkIn.setEnabled(true);
                    PostParam param=PostParam.objectFromData(msg.obj.toString());
                    WJLoger.debug("username:"+param.getParams());
                    PreferenceHelper.write(MainActivity.this, Config.SETTING_FILE, Config.PHONEIMEI, "");
                    PreferenceHelper.write(MainActivity.this,Config.SETTING_FILE,Config.USERREALNAME,param.getParams());
                    MainActivity.this.setTitle(param.getParams() + "   已经注册");
                    break;
                case 10:
                    Snackbar.make(rllRootView, "操作失败哦，请联系管理员:"+msg.obj , Snackbar.LENGTH_LONG).show();
                    break;
            }
        }
    };
    private String phoneIMEI;
    private String userRealName;
    private String realName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        initConfig();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

    }

    private void initConfig() {//初始化配置信息

        isCheckIn = PreferenceHelper.readBoolean(this, Config.SETTING_FILE, Config.IS_CHECKIN, false);
        WJLoger.debug("isCheckIn:"+isCheckIn);
        if (isCheckIn) {
            checkIn.setEnabled(false);
            checkOut.setEnabled(true);
        } else {
            checkIn.setEnabled(true);
            checkOut.setEnabled(false);
        }
        isFirstUse = PreferenceHelper.readBoolean(this, Config.SETTING_FILE, Config.IS_FIRST_USE, true);
        if (isFirstUse) {
            checkOut.setEnabled(false);
            checkIn.setEnabled(false);
            checkSetting.setEnabled(true);
        } else {
            checkOut.setEnabled(true);
            checkIn.setEnabled(true);
            checkSetting.setEnabled(false);
        }
        phoneIMEI = PreferenceHelper.readString(this, Config.SETTING_FILE, Config.PHONEIMEI, "");
        if(TextUtils.isEmpty(phoneIMEI)){
            phoneIMEI= SystemTool.getPhoneIMEI(this);
            PreferenceHelper.write(this,Config.SETTING_FILE,Config.PHONEIMEI,phoneIMEI);
        }

        userRealName = PreferenceHelper.readString(this, Config.SETTING_FILE, Config.USERREALNAME, "");
        if (TextUtils.isEmpty(userRealName)){
            this.setTitle("暂时没有注册");
        }else {
                this.setTitle(userRealName+"   已经注册");
        }
    }

    private void initView() {
        rllRootView = (RelativeLayout) findViewById(R.id.rll_rootView);
        checkIn = (Button) findViewById(R.id.btn_CheckIn);
        checkOut = (Button) findViewById(R.id.btn_CheckOut);
        checkSetting = (Button) findViewById(R.id.btn_setting);
        checkIn.setOnClickListener(this);
        checkOut.setOnClickListener(this);
        checkSetting.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_setting:
                BtnSetting(v);
                break;
            case R.id.btn_CheckIn:
                BtnCheckIn(v);
                break;
            case R.id.btn_CheckOut:
                BtnCheckOut(v);
                break;

        }
    }

    private void BtnCheckOut(View v) {
        if (!SystemTool.isWiFi(v.getContext())) {
            Snackbar.make(v, "请连接wifi签到", Snackbar.LENGTH_LONG).show();
        } else {
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                WifiInfo info = wifiManager.getConnectionInfo();
                String netName = info.getSSID(); //获取被连接网络的名称
                String netMac = info.getBSSID(); //获取被连接网络的mac地址
                String localMacAddress = info.getMacAddress();
                Snackbar.make(v, "netMac:"+netMac, Snackbar.LENGTH_LONG).show();
                WJLoger.debug("netmac"+netMac);
                WJHttp http=new WJHttp();
                HttpParams params=new HttpParams();
                params.put("type","3");
                params.put("catmac",netMac);
                params.put("realname", userRealName);
                params.put("phoneimei", phoneIMEI);
                http.post(Config.SERVERURL, params, new HttpCallBack() {
                    @Override
                    public void onSuccess(String t) {
                        super.onSuccess(t);
                        if (t.equalsIgnoreCase("ok")) {
                            Snackbar.make(rllRootView, "签退成功！", Snackbar.LENGTH_LONG).show();
                            PreferenceHelper.write(MainActivity.this, Config.SETTING_FILE, Config.IS_CHECKIN, false);
                            MainActivity.this.setTitle(userRealName+"  今日已经签退");
                        } else if (t.equalsIgnoreCase("err")) {
                            Snackbar.make(rllRootView, "签退失败！", Snackbar.LENGTH_LONG).show();
                            PreferenceHelper.write(MainActivity.this, Config.SETTING_FILE, Config.IS_CHECKIN, true);
                        }else if (t.equalsIgnoreCase("errmac")) {
                            Snackbar.make(rllRootView, "请到公司在签退,签退失败！", Snackbar.LENGTH_LONG).show();
                            PreferenceHelper.write(MainActivity.this, Config.SETTING_FILE, Config.IS_CHECKIN, false);
                        }
                    }

                    @Override
                    public void onFailure(int errorNo, String strMsg) {
                        super.onFailure(errorNo, strMsg);
                        Snackbar.make(rllRootView, "签退失败！", Snackbar.LENGTH_LONG).show();
                        PreferenceHelper.write(MainActivity.this, Config.SETTING_FILE, Config.IS_CHECKIN, false);
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                    }
                });
            }
        checkOut.setEnabled(false);
        checkIn.setEnabled(true);
    }}

    private void BtnCheckIn(View v) {
        if (!SystemTool.isWiFi(v.getContext())) {
            Snackbar.make(v, "请连接wifi签到", Snackbar.LENGTH_LONG).show();
        } else {
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                WifiInfo info = wifiManager.getConnectionInfo();
                String netName = info.getSSID(); //获取被连接网络的名称
                String netMac = info.getBSSID(); //获取被连接网络的mac地址
                String localMacAddress = info.getMacAddress();
                WJLoger.debug("netmac"+netMac);
                Snackbar.make(v, "netMac:"+netMac, Snackbar.LENGTH_LONG).show();
                WJHttp http=new WJHttp();
                HttpParams params=new HttpParams();
                params.put("type","2");
                params.put("catmac",netMac);
                params.put("realname", userRealName);
                params.put("phoneimei", phoneIMEI);
                http.post(Config.SERVERURL, params, new HttpCallBack() {
                    @Override
                    public void onSuccess(String t) {
                        super.onSuccess(t);
                        if (t.equalsIgnoreCase("ok")) {
                            Snackbar.make(rllRootView, "签到成功！", Snackbar.LENGTH_LONG).show();
                            PreferenceHelper.write(MainActivity.this, Config.SETTING_FILE, Config.IS_CHECKIN, true);
                            MainActivity.this.setTitle(userRealName+"  今日已经签到");
                        } else if (t.equalsIgnoreCase("err")) {
                            Snackbar.make(rllRootView, "签到失败！", Snackbar.LENGTH_LONG).show();
                            PreferenceHelper.write(MainActivity.this, Config.SETTING_FILE, Config.IS_CHECKIN, false);
                        }
                        else if (t.equalsIgnoreCase("errmac")) {
                            Snackbar.make(rllRootView, "请到公司在签到,签到失败！", Snackbar.LENGTH_LONG).show();
                            PreferenceHelper.write(MainActivity.this, Config.SETTING_FILE, Config.IS_CHECKIN, false);
                        }
                    }

                    @Override
                    public void onFailure(int errorNo, String strMsg) {
                        super.onFailure(errorNo, strMsg);
                        Snackbar.make(rllRootView, "签到失败！", Snackbar.LENGTH_LONG).show();
                        PreferenceHelper.write(MainActivity.this, Config.SETTING_FILE, Config.IS_CHECKIN, false);
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                    }
                });
        }

            checkIn.setEnabled(false);
            checkOut.setEnabled(true);
        }
    }


    private void BtnSetting(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("第一次配置");
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_setting, null);
        final TextInputLayout textInputLayout = (TextInputLayout) view.findViewById(R.id.til_realname);
        final EditText editText = textInputLayout.getEditText();
        textInputLayout.setHint("RealName");
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() > 4) {
//                    textInputLayout.setError("RealName error");
//                    textInputLayout.setErrorEnabled(true);
//                } else {
//                    textInputLayout.setErrorEnabled(false)
//                    ;
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        builder.setView(view);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                realName = editText.getText().toString();

                WJHttp wjHttp=new WJHttp();
                HttpParams params=new HttpParams();
                params.put("type","1");
                params.put("realname", realName);
                params.put("phoneimei", phoneIMEI);

                wjHttp.post(Config.SERVERURL, params, new HttpCallBack() {
                    @Override
                    public void onPreStart() {
                        super.onPreStart();
                    }

                    @Override
                    public void onSuccess(String t) {
                        super.onSuccess(t);
                        Message message=hanlder.obtainMessage();
                        message.what=1;
                        message.obj=t;
                        hanlder.sendMessage(message);
                    }

                    @Override
                    public void onFailure(int errorNo, String strMsg) {
                        super.onFailure(errorNo, strMsg);
                        Message message=hanlder.obtainMessage();
                        message.what=10;
                        message.obj=strMsg;
                        hanlder.sendMessage(message);
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                    }
                });

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();

    }


}
