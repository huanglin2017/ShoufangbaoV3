package com.kupangstudio.shoufangbao.model;

import java.util.ArrayList;

/**
 * Created by Jsmi on 2015/11/6.
 */
public class ReportCustom {
    private ArrayList<Long> list;
    private String reportnum;
    private String buildname;
    private int status;

    public ArrayList<Long> getList() {
        return list;
    }

    public void setList(ArrayList<Long> list) {
        this.list = list;
    }

    public String getReportnum() {
        return reportnum;
    }

    public void setReportnum(String reportnum) {
        this.reportnum = reportnum;
    }

    public String getBuildName() {
        return buildname;
    }

    public void setBuildName(String buildname) {
        this.buildname = buildname;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
