package com.kupangstudio.shoufangbao.network;

/**
 * Created by long on 15/11/9.
 * Copyright 2015 android_xiaobai.
 */
public class ResultError {
    public int code;
    public String notice;
    // 请求结果为空
    public static final int RESULT_NULL = -1;
    // 请求结果数据格式错误
    public static final int RESULT_DATA_FORMAT_ERROR = -2;
    // 请求返回错误
    public static final int RESULT_ERROR = -3;
    public static final String MESSAGE_NULL = "网络异常，请检查网络后重试";
    public static final String MESSAGE_ERROR = "数据格式错误，请稍候重试";
}
