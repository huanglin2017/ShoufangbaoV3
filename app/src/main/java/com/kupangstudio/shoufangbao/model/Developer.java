package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by long1 on 15/11/16.
 * Copyright 15/11/16 android_xiaobai.
 * 开发商
 */
public class Developer extends DataSupport {
    private int id;
    private String name;
    private String pic;
    private String intro;
    private int cityid;
    private List<DeveloperBuild> build;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public int getCityid() {
        return cityid;
    }

    public void setCityid(int cityid) {
        this.cityid = cityid;
    }

    public List<DeveloperBuild> getBuild() {
        return build;
    }

    public void setBuild(List<DeveloperBuild> build) {
        this.build = build;
    }

    public List<DeveloperBuild> getDeveloperBuilds() {
        return DataSupport.where("developer_id = ?", String.valueOf(id)).find(DeveloperBuild.class);
    }

}
