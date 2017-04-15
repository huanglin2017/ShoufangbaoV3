package com.kupangstudio.shoufangbao.utils;

import android.content.Context;
import com.umeng.analytics.MobclickAgent;
import java.util.HashMap;

/**
 * 友盟统计工具类
 * Created by long on 15/11/4.
 * Copyright 2015 android_xiaobai.
 */
public class UmengUtils {
    private UmengUtils() {
         /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 统计发生次数
     *
     * @param context
     * @param eventId
     */
    public static void onEvent(Context context, String eventId) {
        MobclickAgent.onEvent(context, eventId);
    }

    /**
     * 统计统计点击行为各属性被触发的次数（如购买以及购买的商品类型数量放在map集合里）
     *
     * @param context
     * @param eventId
     * @param map
     */
    public static void onEventAction(Context context, String eventId, HashMap<String, String> map) {
        MobclickAgent.onEvent(context, eventId, map);
    }

    /**
     * 统计时长
     *
     * @param context
     * @param eventId
     * @param map
     * @param duration
     */
    public static void onEventDuration(Context context, String eventId, HashMap<String, String> map
            , long duration) {
       // MobclickAgent.onEventDuration(context, eventId, map, duration);
    }
}
