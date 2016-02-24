package com.product.stateless.checkin.model;

import com.google.gson.Gson;

/**
 * Created by wecan on 2016/2/23.
 */
public class PostParam {

    /**
     * Result : 已经添加到本系统中
     * Status : ok
     * Params : 1111
     */

    private String Result;
    private String Status;
    private String Params;

    public static PostParam objectFromData(String str) {

        return new Gson().fromJson(str, PostParam.class);
    }

    public void setResult(String Result) {
        this.Result = Result;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }

    public void setParams(String Params) {
        this.Params = Params;
    }

    public String getResult() {
        return Result;
    }

    public String getStatus() {
        return Status;
    }

    public String getParams() {
        return Params;
    }
}

