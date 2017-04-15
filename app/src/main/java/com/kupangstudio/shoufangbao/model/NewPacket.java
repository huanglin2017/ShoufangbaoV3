package com.kupangstudio.shoufangbao.model;

import java.io.Serializable;

/**
 * Created by long1 on 15/11/30.
 * Copyright 15/11/30 android_xiaobai.
 */
public class NewPacket implements Serializable {
    private String title;
    private int bid;
    private String remark;
    private long starttime;
    private long endtime;
    private int pid;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getStarttime() {
        return starttime;
    }

    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    public long getEndtime() {
        return endtime;
    }

    public void setEndtime(long endtime) {
        this.endtime = endtime;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }
}
