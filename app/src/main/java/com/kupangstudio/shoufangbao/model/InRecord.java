package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

/**
 * Created by long1 on 15/11/24.
 * Copyright 15/11/24 android_xiaobai.
 * 进账记录
 */
public class InRecord extends DataSupport{

    public static final int STATUS_UNCASH = 0;
    public static final int STATUS_DONE = 1;
    public static final int STATUS_UNSUCCESS = 2;
    private int uid;
    private int amount;
    private String remark;
    private long ctime;
    private int status;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
