package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * 客户跟进
 * Created by Jsmi on 2015/11/5.
 */
public class CustomFollow extends DataSupport implements Serializable{
    private int fid;
    private int cid;
    private String content;
    private Long ctime;
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Long getCtime() {
        return ctime;
    }

    public void setCtime(Long ctime) {
        this.ctime = ctime;
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
