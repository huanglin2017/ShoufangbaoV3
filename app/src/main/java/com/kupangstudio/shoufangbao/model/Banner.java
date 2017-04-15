package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

/**
 * Created by long on 15/11/10.
 * Copyright 2015 android_xiaobai.
 * Banner
 */
public class Banner extends DataSupport{
    private int cityid;
    private String title;
    private int displayorder;
    private String pic;
    private long starttime;
    private long endtime;
    private int type;
    private Object ext;

    public int getCityid() {
        return cityid;
    }

    public void setCityid(int cityid) {
        this.cityid = cityid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDisplayorder() {
        return displayorder;
    }

    public void setDisplayorder(int displayorder) {
        this.displayorder = displayorder;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getExt() {
        return ext;
    }

    public void setExt(Object ext) {
        this.ext = ext;
    }
}
