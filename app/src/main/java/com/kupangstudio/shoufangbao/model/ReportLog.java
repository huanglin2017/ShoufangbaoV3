package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by long1 on 15/11/25.
 * Copyright 15/11/25 android_xiaobai.
 */
public class ReportLog extends DataSupport implements Serializable{

    private Report report;
    private int status;
    private long ctime;
    private String remark;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
