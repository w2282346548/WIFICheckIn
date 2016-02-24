package com.wj.android.wjframe.ui;

/**
 * 侵入式View的调用工具类
 * Created by wecan-mac on 15/11/30.
 */
public class ViewInject {
    public ViewInject(){

    }

    private static class ClassHolder{
        private static final ViewInject instance=new ViewInject();
    }

    public static ViewInject create(){
        return ClassHolder.instance;
    }


    public static void toast(String msg){
        try {
        }
        catch (Exception e){

        }
    }

}

