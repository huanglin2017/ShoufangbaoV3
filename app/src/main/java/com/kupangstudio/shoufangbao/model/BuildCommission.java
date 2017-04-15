package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by long1 on 16/3/3.
 * Copyright 16/3/3 android_xiaobai.
 */
public class BuildCommission extends DataSupport implements Serializable{
    private BuildDetail buildDetail;
    private String name;
    private String commission;

    public BuildDetail getBuildDetail() {
        return buildDetail;
    }

    public void setBuildDetail(BuildDetail buildDetail) {
        this.buildDetail = buildDetail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommission() {
        return commission;
    }

    public void setCommission(String commission) {
        this.commission = commission;
    }
}
