package com.kupangstudio.shoufangbao.updateservice;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by long on 15/8/13.
 * Copyright 2015 android_xiaobai.
 */
public class DownFileThread extends Thread {
    public static final int DOWNLOAD_COMPLETE = 0;
    public static final int DOWNLOAD_FAIL = 1;
    Handler mHandler;//通知更新进度条handler;
    String urlStr;//下载地址
    File apkFile;//下载APK文件
    private boolean isFinished;//是否下载完成
    private boolean interupted;//是否停止下载

    public DownFileThread(Handler handler, String urlStr, String filePath) {
        this.mHandler = handler;
        this.urlStr = urlStr;
        apkFile = new File(filePath);
    }

    public File getApkFile() {
        if (isFinished) {
            return apkFile;
        } else {
            return null;
        }
    }

    public boolean isFinished() {
        return isFinished;
    }

    /**
     * 强行终止文件下载
     */
    public void interuptThread() {
        interupted = true;
    }

    @Override
    public void run() {
        super.run();
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            URL url = null;
            HttpURLConnection conn = null;
            InputStream is = null;
            try {
                url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(20000);
                is = conn.getInputStream();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(apkFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buffer = new byte[1024];
            int len;
            //获取文件总长度
            int length = conn.getContentLength();
            double rate = (double) 100 / length;  //最大进度转化为100
            int total = 0;
            int times = 0;//设置更新频率，频繁操作ＵＩ线程会导致系统奔溃
            try {
                while (interupted == false && ((len = bis.read(buffer)) != -1)) {
                    fos.write(buffer, 0, len);
                    // 获取已经读取长度
                    total += len;
                    int p = (int) (total * rate);
                    if (times >= 512 || p == 100) {/*
                    这是防止频繁地更新通知，而导致系统变慢甚至崩溃。非常重要。。。。。*/
                        times = 0;
                        Message msg = Message.obtain();
                        msg.what = p;
                        mHandler.sendMessage(msg);
                    }
                    times++;
                }
                fos.close();
                bis.close();
                is.close();
                if (total == length) {
                    isFinished = true;
                    mHandler.sendEmptyMessage(DOWNLOAD_COMPLETE);
                }
            } catch (IOException e) {
                mHandler.sendEmptyMessage(DOWNLOAD_FAIL);
                e.printStackTrace();
            }
        } else {
            mHandler.sendEmptyMessage(DOWNLOAD_FAIL);
        }
    }
}
