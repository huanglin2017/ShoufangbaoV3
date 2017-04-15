package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Jsmeli on 2016/3/15.
 * 新社区Model
 */
public class Scene extends DataSupport implements Serializable{
    public final static int ISRECOMMEND = 1;
    private int type;
    private String face;
    private String realname;
    private int is_recommend;
    private int create_type;
    private int consult_type;
    private String content;
    private List<ScenePic> picture;
    private String url;
    private long ctime;
    private int id;
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public int getCreate_type() {
        return create_type;
    }

    public void setCreate_type(int create_type) {
        this.create_type = create_type;
    }

    public int getConsult_type() {
        return consult_type;
    }

    public void setConsult_type(int consult_type) {
        this.consult_type = consult_type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<ScenePic> getPicture() {
        return picture;
    }

    public void setPicture(List<ScenePic> picture) {
        this.picture = picture;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getIs_recommend() {
        return is_recommend;
    }

    public void setIs_recommend(int is_recommend) {
        this.is_recommend = is_recommend;
    }
}
