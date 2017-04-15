package com.kupangstudio.shoufangbao.updateservice;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.kupangstudio.shoufangbao.R;

/**
 * Created by long on 15/8/13.
 * Copyright 2015 android_xiaobai.
 */
public class DownLoadService extends Service {
    private final static int DOWNLOAD_COMPLETE = 0;
    private final static int DOWNLOAD_FAIL = 1;
    //自定义通知栏类
    DownLoadNotification myNotification;
    String urlStr;
    String filePathString; //下载文件绝对路径(包括文件名)
    //通知栏跳转Intent
    private Intent updateIntent = null;
    private PendingIntent updatePendingIntent = null;
    DownFileThread downFileThread;  //自定义文件下载线程
    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_COMPLETE:
                    Intent intent = new Intent(UpdateManager.ACTION_COMPLETE);
                    DownLoadService.this.sendBroadcast(intent);
                    myNotification.removeNotification();
                    UpdateManager updateManager = new UpdateManager();
                    //updateManager.installApk();
                    //点击安装PendingIntent
                    Uri uri = Uri.fromFile(downFileThread.getApkFile());
                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
                    installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    DownLoadService.this.startActivity(installIntent);
                    updateManager.unRegReceiver();
                    stopSelf();
                    break;
                case DOWNLOAD_FAIL:
                    Intent it = new Intent(UpdateManager.ACTION_FAIL);
                    DownLoadService.this.sendBroadcast(it);
                    myNotification.changeNotificationText("文件下载失败！");
                    stopSelf();
                    break;
                default:  //下载中
                    myNotification.changeProgressStatus(msg.what);
            }
        }
    };

    public DownLoadService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (downFileThread != null)
            downFileThread.interuptThread();
        stopSelf();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        urlStr = intent.getStringExtra("url");
        filePathString = intent.getStringExtra("path");
        updateIntent = new Intent(DownLoadService.this, intent.getClass());
        PendingIntent updatePendingIntent = PendingIntent.getActivity(this, 0, updateIntent, 0);
        myNotification = new DownLoadNotification(this, updatePendingIntent, 1);
        myNotification.showCustomizeNotification(R.drawable.ic_launcher, "售房宝更新", R.layout.update_notification);
        //开启一个新的线程下载，如果使用Service同步下载，会导致ANR问题，Service本身也会阻塞
        downFileThread = new DownFileThread(updateHandler, urlStr, filePathString);
        new Thread(downFileThread).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
