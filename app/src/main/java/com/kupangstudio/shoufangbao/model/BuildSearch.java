package com.kupangstudio.shoufangbao.model;

import java.util.List;

/**
 * Created by long1 on 16/3/4.
 * Copyright 16/3/4 android_xiaobai.
 */
public class BuildSearch {
    private List<ActSearch> active;
    private List<HistorySearch> record;
    private List<HotSearch> hot;

    public List<ActSearch> getActive() {
        return active;
    }

    public void setActive(List<ActSearch> active) {
        this.active = active;
    }

    public List<HistorySearch> getRecord() {
        return record;
    }

    public void setRecord(List<HistorySearch> record) {
        this.record = record;
    }

    public List<HotSearch> getHot() {
        return hot;
    }

    public void setHot(List<HotSearch> hot) {
        this.hot = hot;
    }
}
