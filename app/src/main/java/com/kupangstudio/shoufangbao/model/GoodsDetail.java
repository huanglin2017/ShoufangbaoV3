package com.kupangstudio.shoufangbao.model;

import com.kupangstudio.shoufangbao.utils.StringUtils;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Jsmi on 2015/11/24.
 * 积分商城商品详情
 */
public class GoodsDetail extends DataSupport implements Serializable{
    private List<String> pic;
    private String title;
    private int integrate;
    private String worth;
    private int change;
    private String city;
    private int stock;
    private String format;
    private int ground;
    private int freight;
    private int wid;
    private int type;

    public List<String> getPic() {
        return pic;
    }

    public void setPic(List<String> pic) {
        this.pic = pic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIntegrate() {
        return integrate;
    }

    public void setIntegrate(int integrate) {
        this.integrate = integrate;
    }

    public String getWorth() {
        return worth;
    }

    public void setWorth(String worth) {
        this.worth = worth;
    }

    public int getChange() {
        return change;
    }

    public void setChange(int change) {
        this.change = change;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getGround() {
        return ground;
    }

    public void setGround(int ground) {
        this.ground = ground;
    }

    public int getFreight() {
        return freight;
    }

    public void setFreight(int freight) {
        this.freight = freight;
    }

    public int getWid() {
        return wid;
    }

    public void setWid(int wid) {
        this.wid = wid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
