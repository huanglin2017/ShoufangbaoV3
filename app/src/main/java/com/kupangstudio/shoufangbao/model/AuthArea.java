package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

/**
 * Created by long1 on 15/11/25.
 * Copyright 15/11/25 android_xiaobai.
 */
public class AuthArea extends DataSupport {
    private String area;
    private int areaid;
    private AuthCity authCity;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public int getAreaid() {
        return areaid;
    }

    public void setAreaid(int areaid) {
        this.areaid = areaid;
    }

    public AuthCity getAuthCity() {
        return authCity;
    }

    public void setAuthCity(AuthCity authCity) {
        this.authCity = authCity;
    }
}
