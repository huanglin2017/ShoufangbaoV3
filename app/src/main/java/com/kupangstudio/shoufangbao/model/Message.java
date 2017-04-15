package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by long1 on 15/11/24.
 * Copyright 15/11/24 android_xiaobai.
 * 我的消息
 */
public class Message extends DataSupport implements Serializable {

    public static final int TYPE_SYSTEM = 1; //系统通知
    public static final int TYPE_ACTIVITY = 2; //活动通知
    public static final int TYPE_HOUSE = 3; //楼盘信息
    public static final int TYPE_WELCOME = 4; //欢迎信息
    public static final int TYPE_SUMMARY = 5; //总结信息
    public static final int TYPE_BATCH = 6; //报备信息

    public static final int ACTION_TEXT = 1;//文本类型
    public static final int ACTION_WEB = 2;//网页类型

    public static final int STATE_UNREAD = 0;
    public static final int STATE_READ = 1;
    private int mid;
    private String title;
    private String content;
    private int status;
    private int type;
    private int style;
    private long ctime;
    private int uid;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }
}
