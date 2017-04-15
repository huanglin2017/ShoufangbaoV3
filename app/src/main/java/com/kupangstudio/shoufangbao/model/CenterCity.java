package com.kupangstudio.shoufangbao.model;

/**
 * Created by Jsmi on 2015/11/18.
 */
public class CenterCity {
    private int cid;
    private int uid;
    private String name;
    private String sortLetter;
    private int upid;
    private int level;

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSortLetter() {
        return sortLetter;
    }

    public void setSortLetter(String sortLetter) {
        this.sortLetter = sortLetter;
    }

    public int getUpid() {
        return upid;
    }

    public void setUpid(int upid) {
        this.upid = upid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
