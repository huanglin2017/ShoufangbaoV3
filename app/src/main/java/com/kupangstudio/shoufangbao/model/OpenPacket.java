package com.kupangstudio.shoufangbao.model;

/**
 * Created by long1 on 15/11/30.
 * Copyright 15/11/30 android_xiaobai.
 */
public class OpenPacket {

    public static final int PACKET_ERROR = 4;//未知原因
    public static final int PACKET_OK = 1;//成功
    public static final int PACKET_OVER = 2;//活动结束
    public static final int PACKET_ALREADY = 3;//已经领过

    private String title;
    private String wishing;
    private int money;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWishing() {
        return wishing;
    }

    public void setWishing(String wishing) {
        this.wishing = wishing;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }
}
