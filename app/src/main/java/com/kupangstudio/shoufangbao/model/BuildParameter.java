package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by long on 15/11/11.
 * Copyright 2015 android_xiaobai.
 * 楼盘参数
 */
public class BuildParameter extends DataSupport implements Serializable {

    private int id;
    private BuildDetail buildDetail;
    private int type;
    private String name;
    private String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BuildDetail getBuildDetail() {
        return buildDetail;
    }

    public void setBuildDetail(BuildDetail buildDetail) {
        this.buildDetail = buildDetail;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
