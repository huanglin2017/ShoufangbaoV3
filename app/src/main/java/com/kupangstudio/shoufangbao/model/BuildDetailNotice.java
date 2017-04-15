package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by long1 on 15/11/13.
 * Copyright 15/11/13 android_xiaobai.
 */
public class BuildDetailNotice extends DataSupport implements Serializable {

    public static final int SCROLL = 1;
    public static final int NOTSCROLL = 0;
    public static final int TYPE_WEBVIEW = 1;
    public static final int TYPE_NORMAL = 0;
    private int nid;
    private String title;
    private String content;
    private String ctime;
    private int scroll;
    private int type;

    public int getNid() {
        return nid;
    }

    public void setNid(int nid) {
        this.nid = nid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public int getScroll() {
        return scroll;
    }

    public void setScroll(int scroll) {
        this.scroll = scroll;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
