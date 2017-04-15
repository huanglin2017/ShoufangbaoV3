package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by Jsmi on 2015/11/10.
 * 结束状态
 */
public class ReportEndLog extends DataSupport implements Serializable{
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
