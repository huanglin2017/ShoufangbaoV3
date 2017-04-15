package com.kupangstudio.shoufangbao.network;

/**
 * Created by long on 15/11/2.
 * Copyright 2015 android_xiaobai.
 */
public class Result<T> {
    private int code;
    private String notice;
    private T content;
    public static final int RESULT_OK = 2000;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}
