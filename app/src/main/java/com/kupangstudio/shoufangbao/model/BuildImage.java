package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.List;

/**
 * Created by long on 15/11/11.
 * Copyright 2015 android_xiaobai.
 * 楼盘图片信息
 */
public class BuildImage extends DataSupport implements Serializable {

    private String url;
    private int type;
    private BuildDetail buildDetail;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public BuildDetail getBuildDetail() {
        return buildDetail;
    }

    public void setBuildDetail(BuildDetail buildDetail) {
        this.buildDetail = buildDetail;
    }
}
