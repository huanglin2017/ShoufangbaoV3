package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * 客户的Modul
 * Created by Jsmi on 2015/11/4.
 */
public class Custom extends DataSupport implements Serializable{


    public static final int GENDER_MAN = 1;
    public static final int GENDER_WOMEN = 2;
    private int cid;//客户唯一标识
    private int uid;//该经纪人的客户
    private int id;//关联字段
    private int size;
    private int will;//意向强度
    private int type;//意向类型
    private int price;
    private int layout;//意向户型
    private int gender;//性别
    private Long ctime;//创建时间
    private String tel;//客户电话
    private String name;//客户姓名
    private String remark;//备注
    private int cityid;//城市id
    private String city;//城市
    private int areaid;//区域id
    private String area;//区域
    private int districtid;//商圈id
    private int isend;//交易结束
    private String district;//商圈
    private int status;//报备状态
    private FollowList follow;//最后沟通时间
    public int DATEACTION;
    public static final int ACTION_ADD = 0;
    public static final int ACTION_UPDATE = 1;

    public int getDATEACTION() {
        return DATEACTION;
    }

    public void setDATEACTION(int DATEACTION) {
        this.DATEACTION = DATEACTION;
    }

    public int getIsend() {
        return isend;
    }

    public void setIsend(int isend) {
        this.isend = isend;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getCtime() {
        return ctime;
    }

    public void setCtime(Long ctime) {
        this.ctime = ctime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public FollowList getFollow() {
        return follow;
    }

    public void setFollow(FollowList follow) {
        this.follow = follow;
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

    public void setWill(int will) {
        this.will = will;
    }

    public int getCityid() {
        return cityid;
    }

    public void setCityid(int cityid) {
        this.cityid = cityid;
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

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getSortLetters() {
        return SortLetters;
    }

    public void setSortLetters(String sortLetters) {
        SortLetters = sortLetters;
    }

    private String SortLetters;  //显示数据拼音的首字母

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getWill() {
        return will;
    }

    public void setWill(Integer will) {
        this.will = will;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }
}
