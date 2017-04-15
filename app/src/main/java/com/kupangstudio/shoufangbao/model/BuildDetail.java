package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.List;

/**
 * Created by long on 15/11/11.
 * Copyright 2015 android_xiaobai.
 * 楼盘详情
 */
public class BuildDetail extends DataSupport implements Serializable {

    public static final int EFFECT = 1;//效果图
    public static final int SHIJING = 2;//实景图
    public static final int GUIHUA = 3;//规划图
    public static final int HOUSEMODEL = 4;//户型图
    public static final int COLLECT = 1;
    public static final int UNCOLLECT = 0;
    public static final int NOTONSALE = 0;//代售
    public static final int ONSALE = 1;//在售
    public static final int RENT = 2;//出租
    private int id;
    private int bid;
    private int collect;
    private String reporttel;
    private String reward;
    private List<BuildImage> buildPic;//图片
    private BuildDetailNotice buildNew;//通告
    private List<BuildParameter> buildProfile;//楼盘参数
    private BuildProfile sell;//楼盘卖点、目标客户
    private List<BuildLayout> layout;//户型图
    private List<BuildCommission> commission;//佣金
    private BuildBase base;//基础数据

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReporttel() {
        return reporttel;
    }

    public void setReporttel(String reporttel) {
        this.reporttel = reporttel;
    }

    public List<BuildImage> getBuildPic() {
        return buildPic;
    }

    public void setBuildPic(List<BuildImage> buildPic) {
        this.buildPic = buildPic;
    }

    public BuildDetailNotice getBuildNew() {
        return buildNew;
    }

    public void setBuildNew(BuildDetailNotice buildNew) {
        this.buildNew = buildNew;
    }

    public List<BuildParameter> getBuildProfile() {
        return buildProfile;
    }

    public void setBuildProfile(List<BuildParameter> buildProfile) {
        this.buildProfile = buildProfile;
    }

    public BuildProfile getSell() {
        return sell;
    }

    public void setSell(BuildProfile sell) {
        this.sell = sell;
    }

    public List<BuildLayout> getLayout() {
        return layout;
    }

    public void setLayout(List<BuildLayout> layout) {
        this.layout = layout;
    }

    public List<BuildCommission> getCommission() {
        return commission;
    }

    public void setCommission(List<BuildCommission> commission) {
        this.commission = commission;
    }

    public BuildBase getBase() {
        return base;
    }

    public void setBase(BuildBase base) {
        this.base = base;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public int getCollect() {
        return collect;
    }

    public void setCollect(int collect) {
        this.collect = collect;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }
}
