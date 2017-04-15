package com.kupangstudio.shoufangbao.updateservice;

/**
 * Created by long on 15/8/10.
 * Copyright 2015 android_xiaobai.
 */
public class Version {
    /**
     * 版本号 e.g: 13
     */
    private int versioncode;

    /**
     * 版本名 e.g: 1.0.9
     */
    private String title;

    /**
     * 此版本特性 e.g: Fixed bugs
     */
    private String content;

    /**
     * 是否强制升级
     */
    private int upgrade;

    /**
     * 此版本APK下载地址
     */
    private String path;

    public int getVersioncode() {
        return versioncode;
    }

    public void setVersioncode(int versioncode) {
        this.versioncode = versioncode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUpgrade() {
        return upgrade;
    }

    public void setUpgrade(int upgrade) {
        this.upgrade = upgrade;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
