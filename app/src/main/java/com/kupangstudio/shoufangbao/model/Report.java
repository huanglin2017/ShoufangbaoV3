package com.kupangstudio.shoufangbao.model;

import com.kupangstudio.shoufangbao.utils.StringUtils;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by long1 on 15/11/14.
 * Copyright 15/11/14 android_xiaobai.
 */
public class Report extends DataSupport implements Serializable {
    public static final int REPORT = 1;//已报备
    public static final int ACCEPT = 2;//已确认
    public static final int DAIKAN = 3;//带看中
    public static final int PURCHASE = 4;//已认购
    public static final int DEAL = 5;//已成交
    public static final int COMMISSION = 6;//已结佣
    public static final int END = 1;//已结束
    public static final int REPORT_BUILD = 2;
    public static final int REPORT_CUSTOM = 1;
    public static final int PHONE_REPORT = 2;//电话报备
    public static final int ONLINE_REPORT = 1;//在线报备
    public static final int APPLY_OK = 1;
    public static final int ACTION_DAIKAN = 1;//发起带看
    public static final int ACTION_PURCHASE = 2;//发起认购
    public static final int ACTION_DEAL = 3;//发起成交

    private String number;
    private int rid;
    private String name;
    private String tel;
    private int isend;
    private int mold;
    private int id;
    private int type;
    private ReportBuild build;
    private List<ReportLog> reportLog;
    private int status;
    private ReportEndLog endLog;

    public ReportEndLog getEndLog() {
        return endLog;
    }

    public void setEndLog(ReportEndLog endLog) {
        this.endLog = endLog;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public int getIsend() {
        return isend;
    }

    public void setIsend(int isend) {
        this.isend = isend;
    }

    public int getMold() {
        return mold;
    }

    public void setMold(int mold) {
        this.mold = mold;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ReportBuild getBuild() {
        return build;
    }

    public void setBuild(ReportBuild build) {
        this.build = build;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<ReportLog> getReportLog() {
        return reportLog;
    }

    public void setReportLog(List<ReportLog> reportLog) {
        this.reportLog = reportLog;
    }
}
