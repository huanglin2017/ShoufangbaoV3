package com.kupangstudio.shoufangbao.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.AuthActivity;
import com.kupangstudio.shoufangbao.LoginActivity;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 跟App相关的辅助类
 */
public class AppUtils {

    private AppUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");

    }

    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用程序版本名称信息
     * @param context
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用程序版本号
     * @param context
     * @return
     */
    public static int getAppVersionCode(Context context) {
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                PackageInfo pi;
                try {
                    pi = pm.getPackageInfo(context.getPackageName(), 0);
                    if (pi != null) {
                        return pi.versionCode;
                    }
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    /**
     * 安装APK
     * @param context
     * @param filePath file path of package
     * @return whether apk exist
     */
    public static boolean installAPK(Context context, String filePath) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        File file = new File(filePath);
        if (file == null || !file.exists() || !file.isFile() || file.length() <= 0) {
            return false;
        }

        i.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        return true;
    }

    /**
     * 权限检查
     * @param context
     * @param permission
     * @return
     */
    public static boolean checkPermission(Context context, String permission) {
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * 检测对应系统
     * @param propName
     * @return
     */
    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return line;
    }

    //登陆认证提示对话框
    public static void showRegAuthDialog(final Context ctx, String content, final int type) {
        final Dialog dialog = new Dialog(ctx, R.style.Dialog_Notitle);
        View view = LayoutInflater.from(ctx).inflate(R.layout.common_regauth_dialog, null);
        dialog.setContentView(view);
        Button cancel = (Button) view.findViewById(R.id.regauth_btncancel);
        Button ok = (Button) view.findViewById(R.id.regauth_btnok);
        if (type == 1) {
            ok.setText("去认证");
        }
        TextView tv = (TextView) view.findViewById(R.id.regauth_content);
        tv.setText(content);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                switch (type) {
                    case 0://登陆
                        Intent it = new Intent(ctx, LoginActivity.class);
                        ctx.startActivity(it);
                        break;
                    case 1://认证
                        //跳转认证界面
                        Intent it2 = new Intent(ctx, AuthActivity.class);
                        ctx.startActivity(it2);
                        break;
                }
            }
        });
        dialog.show();
    }

    //判断是否注册登录了
    public static boolean isRegAndLog() {
        User user = User.getInstance();
        if (user.userType != User.TYPE_NORMAL_USER) {
            return true;
        } else {
            return false;
        }
    }
}
