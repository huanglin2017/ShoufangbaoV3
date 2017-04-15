package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by long1 on 16/3/3.
 * Copyright 16/3/3 android_xiaobai.
 */
public class BuildBase extends DataSupport implements Serializable {

    private String name;
    private String price;
    private String lat;
    private String lng;
    private String address;
    private int packet;
    private String brokerUrl;
    private String customerUrl;
    private String shareCommission;
    private String sharePrice;
    private String shareSale;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPacket() {
        return packet;
    }

    public void setPacket(int packet) {
        this.packet = packet;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getCustomerUrl() {
        return customerUrl;
    }

    public void setCustomerUrl(String customerUrl) {
        this.customerUrl = customerUrl;
    }

    public String getShareCommission() {
        return shareCommission;
    }

    public void setShareCommission(String shareCommission) {
        this.shareCommission = shareCommission;
    }

    public String getSharePrice() {
        return sharePrice;
    }

    public void setSharePrice(String sharePrice) {
        this.sharePrice = sharePrice;
    }

    public String getShareSale() {
        return shareSale;
    }

    public void setShareSale(String shareSale) {
        this.shareSale = shareSale;
    }
}
