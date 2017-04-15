package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.List;

/**
 * Created by long1 on 16/3/3.
 * Copyright 16/3/3 android_xiaobai.
 * 楼盘户型
 */
public class BuildLayout extends DataSupport implements Serializable{
    private BuildDetail buildDetail;
    private String price;
    private String remark;
    private String title;
    private String discount;
    private String size;
    private String url;
    private String layout;
    private List<String> pics;

    public BuildDetail getBuildDetail() {
        return buildDetail;
    }

    public void setBuildDetail(BuildDetail buildDetail) {
        this.buildDetail = buildDetail;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getPics() {
        return pics;
    }

    public void setPics(List<String> pics) {
        this.pics = pics;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }
}
