package com.kupangstudio.shoufangbao.model;

import com.kupangstudio.shoufangbao.utils.StringUtils;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by Jsmi on 2015/11/10.
 * 社区新闻
 */
public class News extends DataSupport implements Serializable {
    public static final int ALREADY = 1;

    private int nid;
    private int brower;
    private String title;
    private Long ctime;
    private String content;
    private String describe;
    private int read;
    private int uid;
    private int cityid;
    private String pic;
    private int label;

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public int getNid() {
        return nid;
    }

    public void setNid(int nid) {
        this.nid = nid;
    }

    public int getBrower() {
        return brower;
    }

    public void setBrower(int brower) {
        this.brower = brower;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCtime() {
        return ctime;
    }

    public void setCtime(Long ctime) {
        this.ctime = ctime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getCityid() {
        return cityid;
    }

    public void setCityid(int cityid) {
        this.cityid = cityid;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }
}
