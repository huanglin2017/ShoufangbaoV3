package com.kupangstudio.shoufangbao.receiver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.kupangstudio.shoufangbao.MessageTextDetailActivity;
import com.kupangstudio.shoufangbao.MessageWebDetailActivity;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.base.ShoufangbaoApplication;
import com.kupangstudio.shoufangbao.model.Message;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;

import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;
import de.greenrobot.event.EventBus;

/**
 * Created by long on 15/11/3.
 * Copyright 2015 android_xiaobai.
 */
public class MessageReceiver extends BroadcastReceiver {

    private NotificationManager nm;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (nm == null) {
            nm = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }
        Bundle bundle = intent.getExtras();
        if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
                .getAction())) {
            openMessage(context, bundle);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void openMessage(Context context, Bundle bundle) {

        User user = User.getInstance();
        Message msg = null;
        if (user == null || user.userType == User.TYPE_DEFAULT_USER) {
            // 用户为空不处理
            return;
        }
        String content = null, title = null;
        String jsonString = bundle.getString(JPushInterface.EXTRA_EXTRA);
        try {
            JSONObject obj = new JSONObject(jsonString);

            int mid = obj.getInt("mid");
            int type = obj.getInt("type");
            int style = obj.getInt("style");
            title = obj.getString("title");
            content = obj.getString("content");
            msg = new Message();
            msg.setMid(mid);
            msg.setTitle(title);
            msg.setType(type);
            msg.setContent(content);
            msg.setCtime(System.currentTimeMillis()/1000);
            msg.setStyle(style);
            msg.setUid(user.uid);
            msg.setStatus(Message.STATE_UNREAD);// 标记为未读

        } catch (Exception e) {
            return;
        }

        // 消息为空
        if (msg == null) {
            return;
        }
        msg.save();
        EventBus.getDefault().post(msg);
        // 检测是否接收推送信息开关
        boolean flag = (boolean) SPUtils.get(context, Constants.SET_PUSH, true);
        if (!flag) {
            return;
        }

        Intent intent;
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (msg.getType() == Message.ACTION_WEB) {
            intent = new Intent(context, MessageWebDetailActivity.class);
            intent.putExtra("isFromNotify", true);
            intent.putExtra("msgobject", msg);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            content = "点击查看详情";
        } else {
            intent = new Intent(context, MessageTextDetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("isFromNotify", true);
            intent.putExtra("msgobject", msg);
        }

        PendingIntent pi = PendingIntent.getActivity(context, msg.getMid(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // version 16
        Notification nf = null;
        try {
            nf = new NotificationCompat.Builder(context).setContentTitle(title)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentText(content)
                    .setSmallIcon(R.drawable.ic_launcher).setContentIntent(pi)
                    .setAutoCancel(true).build();
        } catch (Exception ex) {
            nf = new NotificationCompat.Builder(context).setContentTitle(title)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentText(content)
                    .setSmallIcon(R.drawable.ic_launcher).setContentIntent(pi)
                    .setAutoCancel(true).getNotification();
        }

        if (nf != null) {
            nm.notify(msg.getMid(), nf);
        }

    }

}
