package com.kupangstudio.shoufangbao.updateservice;

/**
 * Created by long on 15/8/10.
 * Copyright 2015 android_xiaobai.
 */

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateManager {

    private Context mContext;
    private boolean isDown;
    private int isForce;//是否强制升级0可选1强制
    private boolean isShowProgress;//首页自动检测还是用户手动检测
    private boolean isFinished;
    //提示语
    private String updateMsg;
    //返回的安装包url
    private String apkUrl;
    private MyBroadcast broadcast;
    Dialog dialog;
    /* 下载包安装路径 */
    private static final String savePath = Constants.CACHE_TMP_PATH;
    private static final String saveFileName = savePath + "update" + File.separator + "Shoufangbao.apk";
    public static final String ACTION_COMPLETE = "com.kupangstudio.shoufangbao.updatecomplete";
    public static final String ACTION_FAIL = "com.kupangstudio.shoufangbao.updatefail";
    Button ok;
    IntentFilter intentFilter;

    public UpdateManager() {

    }

    public UpdateManager(Context context, boolean isShowProgress) {
        this.mContext = context;
        this.isShowProgress = isShowProgress;
        broadcast = new MyBroadcast();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_COMPLETE);
    }

    //外部接口让主Activity调用
    public void checkUpdateInfo() {
        getUpdateData();
    }

    private void showNoticeDialog() {
        Builder builder = new Builder(mContext);
        dialog = builder.create();
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_update, null);
        dialog.show();
        dialog.getWindow().setContentView(view);
        if (isForce == 1) {
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            view.findViewById(R.id.update_cancel).setVisibility(View.GONE);
        }
        ok = (Button) view.findViewById(R.id.update_ok);
        ok.findViewById(R.id.update_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.registerReceiver(broadcast, intentFilter);
                if (isDown) {
                    Toast.makeText(mContext, "安装包下载中，请稍后", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isFinished) {
                    installApk();
                    return;
                }
                if(apkUrl == null || apkUrl.equals("")) {
                    Toast.makeText(mContext, "地址解析失败！请稍后重试", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isForce == 1) {
                    ok.setText("正在下载");
                } else {
                    dialog.dismiss();
                }
                try {
                    downLoadAPK();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        TextView cancel = (TextView) view.findViewById(R.id.update_cancel);
        cancel.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        cancel.getPaint().setAntiAlias(true);//抗锯齿
        cancel.findViewById(R.id.update_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        TextView content = (TextView) view.findViewById(R.id.update_content);
        content.setText(updateMsg);
    }

    private void downLoadAPK() throws IOException {
        isDown = true;
        File file = new File(saveFileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        Intent intent = new Intent(mContext, DownLoadService.class);
        intent.putExtra("url", apkUrl);
        intent.putExtra("path", file.getAbsolutePath());
        mContext.startService(intent);
    }


    class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (dialog == null) {
                return;
            }
            isDown = false;
            if (intent.getAction().equals(ACTION_COMPLETE)) {
                isFinished = true;
                ok.setText("下载完成");
            } else {
                isFinished = false;
                ok.setText("点击重试");
            }
        }
    }

    public void unRegReceiver() {
        if (mContext == null) {
            return;
        }
        mContext.unregisterReceiver(broadcast);
    }

    /**
     * 安装apk
     *
     * @param
     */
    public void installApk() {
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }

    /**
     * 从网络获取最新闪屏图片
     */
    private void getUpdateData() {
        final ProgressDialog progress;
        progress = new ProgressDialog(mContext);
        progress.setTitle("正在检测更新");
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getUpgrade");
        map.put("module", Constants.MODULE_UPGRADE);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new VersionCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        if (isShowProgress) {
                            progress.show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (isShowProgress) {
                            Toast.makeText(mContext, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onResponse(Result<Version> response) {
                        if (response == null) {
                            if(isShowProgress) {
                                Toast.makeText(mContext, ResultError.MESSAGE_ERROR,
                                        Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                        if (response.getCode() > 2000) {
                            Version version = response.getContent();
                            isForce = version.getUpgrade();
                            apkUrl = version.getPath();
                            updateMsg = version.getContent();
                            if (comparedWithCurrentPackage(version)) {
                                showNoticeDialog();
                            } else {
                                if (isShowProgress) {
                                    Toast.makeText(mContext, "已经是最新版本", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            if (isShowProgress) {
                                Toast.makeText(mContext, response.getNotice(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        if (progress != null && progress.isShowing()) {
                            progress.dismiss();
                        }
                    }
                });
    }

    private abstract class VersionCallback extends Callback<Result<Version>>{
        @Override
        public Result<Version> parseNetworkResponse(Response response) throws Exception {
            Result<Version> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<Version>>(){}.getType());
            }catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    StringBuffer toStringBuffer(InputStream is) throws IOException {
        if (null == is) return null;
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = null;
        while ((line = in.readLine()) != null) {
            buffer.append(line).append("\n");
        }
        is.close();
        return buffer;
    }

    boolean comparedWithCurrentPackage(Version version) {
        if (version == null) return false;
        int currentVersionCode = 0;
        try {
            PackageInfo pkg = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            currentVersionCode = pkg.versionCode;
        } catch (PackageManager.NameNotFoundException exp) {
            exp.printStackTrace();
        }
        return version.getVersioncode() > currentVersionCode;
    }

}

