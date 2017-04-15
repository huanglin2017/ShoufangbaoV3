package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by long on 15/11/10.
 * Copyright 2015 android_xiaobai.
 * 楼盘列表
 */
public class Build extends DataSupport implements Serializable {

    public static final int NEW = 1;//新
    public static final int HOT = 2;//热
    public static final int JIAN = 3;//荐
    public static final int RED_PACKETS = 4;//红包
    public static final int DAIKAN_PACKET = 5;//带看福利
    public static final int DETAIL_DAIKAN_PACKET = 3;//详情页带看福利
    public static final int NONE_PACKET =0;//无红包
    public static final int NEW_PACKET = 1;//新盘红包
    public static final int REPORT_PACKET = 2;//报备红包

    private int unique_business;
    private int cityid;
    private String name;
    private int areaid;
    private int districtid;
    private String tag;
    private String price;
    private String commission;
    private int brower;
    private int style;
    private int layout;
    private long ctime;
    private int bid;
    private String city;
    private String area;
    private String district;
    private String pic;
    private int label;
    private int displayorder;
    private int packet;
    private String distance;
    private String lat;
    private String lng;

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public int getUnique_business() {
        return unique_business;
    }

    public void setUnique_business(int unique_business) {
        this.unique_business = unique_business;
    }

    public int getCityid() {
        return cityid;
    }

    public void setCityid(int cityid) {
        this.cityid = cityid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAreaid() {
        return areaid;
    }

    public void setAreaid(int areaid) {
        this.areaid = areaid;
    }

    public int getDistrictid() {
        return districtid;
    }

    public void setDistrictid(int districtid) {
        this.districtid = districtid;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCommission() {
        return commission;
    }

    public void setCommission(String commission) {
        this.commission = commission;
    }

    public int getBrower() {
        return brower;
    }

    public void setBrower(int brower) {
        this.brower = brower;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public int getDisplayorder() {
        return displayorder;
    }

    public void setDisplayorder(int displayorder) {
        this.displayorder = displayorder;
    }

    public int getPacket() {
        return packet;
    }

    public void setPacket(int packet) {
        this.packet = packet;
    }
}
