package com.kupangstudio.shoufangbao.utils;

import android.app.Activity;
import android.content.Context;

import com.umeng.analytics.MobclickAgent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by long1 on 15/12/2.
 * Copyright 15/12/2 android_xiaobai.
 */
public class ExitUtils {
    private List<Activity> activityList = new LinkedList<Activity>();
    private static ExitUtils exitUtils;

    private ExitUtils() {

    }
    public static ExitUtils getInstance() {
        if (exitUtils == null) {
            exitUtils = new ExitUtils();
        }
        return exitUtils;
    }

    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void removeActivity(Activity activity) {
        activityList.remove(activity);
    }

    public void exit(Context context) {
        for (Activity activity : activityList) {
            activity.finish();
        }
        MobclickAgent.onKillProcess(context);
        System.exit(0);
    }

}
