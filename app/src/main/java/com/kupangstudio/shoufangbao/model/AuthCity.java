package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by long1 on 15/11/25.
 * Copyright 15/11/25 android_xiaobai.
 */
public class AuthCity extends DataSupport {
    private int cityid;
    private String city;
    private List<AuthArea> list;

    public int getCityid() {
        return cityid;
    }

    public void setCityid(int cityid) {
        this.cityid = cityid;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<AuthArea> getList() {
        return list;
    }

    public void setList(List<AuthArea> list) {
        this.list = list;
    }
}
