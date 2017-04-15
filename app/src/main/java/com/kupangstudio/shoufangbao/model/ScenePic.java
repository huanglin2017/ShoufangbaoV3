package com.kupangstudio.shoufangbao.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by Jsmeli on 2016/3/18.
 * 图片数组
 */
public class ScenePic extends DataSupport implements Serializable{
    private int position;
    private String url;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
