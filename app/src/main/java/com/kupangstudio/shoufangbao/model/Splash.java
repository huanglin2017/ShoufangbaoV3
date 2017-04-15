package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

/**
 * 闪屏模型类
 * Created by long on 15/11/4.
 * Copyright 2015 android_xiaobai.
 */
public class Splash extends DataSupport {

    private long starttime;
    private long endtime;
    private String pic;

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

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }
}
