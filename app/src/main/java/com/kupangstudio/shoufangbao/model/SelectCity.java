package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

/**
 * Created by long on 15/11/9.
 * Copyright 2015 android_xiaobai.
 */
public class SelectCity extends DataSupport {
    private int cityId;
    private String name;
    private int level;
    private int upid;
    private int displayorder;
    private String initial;
    private int type;

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getUpid() {
        return upid;
    }

    public void setUpid(int upid) {
        this.upid = upid;
    }

    public int getDisplayorder() {
        return displayorder;
    }

    public void setDisplayorder(int displayorder) {
        this.displayorder = displayorder;
    }

    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
