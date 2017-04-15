package com.kupangstudio.shoufangbao.base;

import android.telephony.TelephonyManager;
import android.content.ContentValues;
import android.content.Context;

import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.utils.StringUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.kupangstudio.shoufangbao.model.Custom;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePalApplication;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


import cn.jpush.android.api.JPushInterface;
import okhttp3.Call;

/**
 * Created by long on 15/11/2.
 * Copyright 2015 android_xiaobai.
 */
public class ShoufangbaoApplication extends LitePalApplication {

    public static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        OkHttpUtils.getInstance().setConnectTimeout(10, TimeUnit.SECONDS);
        OkHttpUtils.getInstance().setReadTimeout(20, TimeUnit.SECONDS);
        OkHttpUtils.getInstance().setWriteTimeout(10, TimeUnit.SECONDS);
        OkHttpUtils.getInstance().setCertificates();
        TelephonyManager mTm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        Constants.IMEI = mTm.getDeviceId();
        //极光相关配置
        JPushInterface.init(this);
        //ImageLoader相关配置全局初始化一次就行
        File cacheDir = StorageUtils.getOwnCacheDirectory(this, "imageloader/Cache");
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this)
                .threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .diskCache(new UnlimitedDiscCache(cacheDir))
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(5 * 1024 * 1024)
                .diskCacheFileCount(100)
                .writeDebugLogs() // Remove for release app
                .build();
        ImageLoader.getInstance().init(config);
        init();
//        if (User.getInstance().userType == User.TYPE_NORMAL_USER && User.getInstance().cityId != 0) {
//            getTaskData();
//        }
    }

    private void init() {
        User user = User.getInstance();
        getAppConfig(user);
        if (user.userType == User.TYPE_DEFAULT_USER) {
            HashMap<String, String> map = CommonUtils.getUpHashMap();
            map.put("action", "userAnonymous");
            map.put("module", Constants.MODULE_USER);
            map.put("uid", String.valueOf(user.uid));
            map.put("id", user.salt);
            OkHttpUtils.post().url(Constants.HOST_URL)
                    .params(map)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {

                        }

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                int code = jsonObject.getInt("code");
                                if (code > Result.RESULT_OK) {
                                    String jsonStr = jsonObject.getJSONObject("content").toString();
                                    JSONObject json = new JSONObject(jsonStr);
                                    User user = User.parseUser(appContext, json, User.TYPE_TMP_USER);
                                    User.saveUser(user, appContext);
                                    ContentValues values = new ContentValues();
                                    values.put("uid", String.valueOf(User.getInstance().uid));
                                    DataSupport.updateAll(Custom.class, values, "uid = ?", "-1");
                                }
                            } catch (JSONException e) {
                                return;
                            }
                        }
                    });
        }
    }

    private void getAppConfig(User user) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getAppConfig");
        map.put("module", Constants.MODULE_CONFIG);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        SPUtils.put(appContext, Constants.ISMEET, 0);
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                String jsonStr = jsonObject.getJSONObject("content").toString();
                                JSONObject json = new JSONObject(jsonStr);
                                int navi = json.getInt("nav");
                                int tasknavi = json.getInt("taskpower");
                                String newsUrl = json.getString("newurl");
                                //String h5Tips = json.getString("H5tips");
                                String safeUrl = json.getString("reporturl");
                                JSONArray array = json.getJSONArray("navicon");
                                int meet = 0;
                                if (!json.isNull("meet")) {
                                    meet = json.getInt("meet");
                                }
                                SPUtils.put(appContext, Constants.ISMEET, meet);
                                SPUtils.put(appContext, Constants.NAVI_SWITCH, navi);
                                SPUtils.put(appContext, Constants.TASK, tasknavi);
                                SPUtils.put(appContext, Constants.NEWS_URL, newsUrl);
                                //SPUtils.put(appContext, Constants.H5_TIPS, h5Tips);
                                SPUtils.put(appContext, Constants.MAIN_ONE_URL, array.getString(0));
                                SPUtils.put(appContext, Constants.MAIN_TWO_URL, array.getString(1));
                                SPUtils.put(appContext, Constants.MAIN_THREE_URL, array.getString(2));
                                SPUtils.put(appContext, Constants.MAIN_FOUR_URL, array.getString(3));
                                //SPUtils.put(appContext, Constants.MAIN_FIVE_URL, array.getString(4));
                                SPUtils.put(appContext, Constants.REPORT_SAFE_URL, safeUrl);
                            }
                        } catch (JSONException e) {
                            SPUtils.put(appContext, Constants.ISMEET, 0);
                            return;
                        }
                    }
                });
    }

    private void getTaskData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getTodayTask");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        String preTime = null;
                        String localTime = TimeUtils.getCustomReportData(System.currentTimeMillis());
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getJSONObject("content").toString();
                            User user = User.getInstance();
                            preTime = (String) SPUtils.get(getApplicationContext(), user.uid + Constants.TASKTIME, "");
                            if (StringUtils.isEmpty(result)) {
                                //Toast.makeText(getApplicationContext(), "result为空", Toast.LENGTH_SHORT).show();
                                if (!localTime.equals(preTime)) {
                                    SPUtils.put(getApplicationContext(), Constants.TASKCONTENT, "首先，点击“客户”模块--点击右上角“添加客户”按钮--最后，填写相应的客户信息");
                                    SPUtils.put(getApplicationContext(), User.getInstance().uid + Constants.TASKTIME, localTime);
                                    SPUtils.put(getApplicationContext(), User.getInstance().uid + Constants.TASKNUM, 250);
                                    SPUtils.put(getApplicationContext(), Constants.TASKPARAMETER, 0);
                                    SPUtils.put(getApplicationContext(), Constants.TASKTITLE, "添加客户，体验小宝的客户管理功能");
                                    SPUtils.put(getApplicationContext(), Constants.TASKTIP, "做任务 拿签到奖励");
                                    SPUtils.put(getApplicationContext(), Constants.TASKTRUE, "做任务 拿奖励");
                                    SPUtils.put(getApplicationContext(), Constants.TASKFALSE, "给奖也不要");
                                    String key = User.getInstance().uid + "taskdone";
                                    SPUtils.put(getApplicationContext(), key, false);
                                }
                                return;
                            }
                            JSONObject obj = new JSONObject(result);
                            String taskTime = obj.getString("starttime");
                            String tip = obj.getString("tip");
                            String truebtn = obj.getString("truebtn");
                            String falsebtn = obj.getString("falsebtn");
                            int taskNum = obj.getInt("type");
                            String taskContent = "";
                            if (taskNum == 63) {
                                JSONArray jsonArray = obj.getJSONArray("remark");
                                SPUtils.put(getApplicationContext(), Constants.TASKCHOICEA, jsonArray.getString(0));
                                SPUtils.put(getApplicationContext(), Constants.TASKCHOICEB, jsonArray.getString(1));
                                SPUtils.put(getApplicationContext(), Constants.TASKCHOICEC, jsonArray.getString(2));
                                if (obj.isNull("sid")) {
                                    SPUtils.put(getApplicationContext(), Constants.TASKID, 0);
                                } else {
                                    SPUtils.put(getApplicationContext(), Constants.TASKID, obj.getInt("sid"));
                                }
                            } else {
                                taskContent = obj.getString("remark");
                            }

                            int taskParameter;
                            if (obj.isNull("ext")) {
                                taskParameter = 0;
                            } else {
                                taskParameter = obj.getInt("ext");
                            }
                            String taskTitle = obj.getString("title");
                            if (!taskTime.equals(preTime)) {
                                SPUtils.put(getApplicationContext(), Constants.TASKCONTENT, taskContent);
                                SPUtils.put(getApplicationContext(), User.getInstance().uid + Constants.TASKTIME, taskTime);
                                SPUtils.put(getApplicationContext(), User.getInstance().uid + Constants.TASKNUM, taskNum);
                                SPUtils.put(getApplicationContext(), Constants.TASKPARAMETER, taskParameter);
                                SPUtils.put(getApplicationContext(), Constants.TASKTITLE, taskTitle);
                                SPUtils.put(getApplicationContext(), Constants.TASKTIP, tip);
                                SPUtils.put(getApplicationContext(), Constants.TASKTRUE, truebtn);
                                SPUtils.put(getApplicationContext(), Constants.TASKFALSE, falsebtn);
                                String key = User.getInstance().uid + "taskdone";
                                SPUtils.put(getApplicationContext(), key, false);
                            }

                        } catch (JSONException e) {
                            if (!localTime.equals(preTime)) {
                                SPUtils.put(getApplicationContext(), Constants.TASKCONTENT, "首先，点击“客户”模块--点击右上角“添加客户”按钮--最后，填写相应的客户信息");
                                SPUtils.put(getApplicationContext(), User.getInstance().uid + Constants.TASKTIME, localTime);
                                SPUtils.put(getApplicationContext(), User.getInstance().uid + Constants.TASKNUM, 250);
                                SPUtils.put(getApplicationContext(), Constants.TASKPARAMETER, 0);
                                SPUtils.put(getApplicationContext(), Constants.TASKTITLE, "添加客户，体验小宝的客户管理功能");
                                SPUtils.put(getApplicationContext(), Constants.TASKTIP, "做任务 拿签到奖励");
                                SPUtils.put(getApplicationContext(), Constants.TASKTRUE, "做任务 拿奖励");
                                SPUtils.put(getApplicationContext(), Constants.TASKFALSE, "给奖也不要");
                                String key = User.getInstance().uid + "taskdone";
                                SPUtils.put(getApplicationContext(), key, false);
                            }
                        }
                    }
                });
    }
}
