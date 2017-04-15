package com.kupangstudio.shoufangbao.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kupangstudio.shoufangbao.AuthActivity;
import com.kupangstudio.shoufangbao.LoginActivity;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.AuthCity;
import com.kupangstudio.shoufangbao.model.PointRecord;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.OkHttpRequest;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;
import okhttp3.Call;

/**
 * App的一些界面辅助常用工具
 */
public class CommonUtils {
    /**
     * isMobie
     * 手机号是否合法验证
     *
     * @param mobiles 手机号
     * @return 返回值
     */
    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern
                .compile("^((13[0-9])|(15[0-9])|(18[0-9])|(14[0-9])|(17[0-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 验证手机号是否是数字与字母的组合
     *
     * @param mobiles 手机号
     * @return 返回值
     */
    public static boolean isPassword(String mobiles) {
        Pattern p = Pattern
                .compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }


    /**
     * 获取推广邀请码，凑齐6位
     *
     * @param uid
     * @return
     */
    public static String getInviteNum(int uid) {

        String str = String.valueOf(uid);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 6 - str.length(); i++) {
            sb.append("0");
        }
        sb.append(str);
        return sb.toString();
    }

    /**
     * 打电话
     *
     * @param callNum
     * @param ctx
     * @return
     */
    public static boolean callPhone(String callNum, Context ctx) {
        for (int i = 0; i < callNum.length(); i++) {
            if (callNum.substring(i, i + 1).equals("*")) {
                Toast.makeText(ctx, "号码不全，请更新客户信息后联系", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        try {
            Uri uri = Uri.parse("tel:" + callNum);
            Intent intent = new Intent(Intent.ACTION_DIAL, uri);
            ctx.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ctx, "抱歉，拨打电话失败！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 发短信
     *
     * @param number
     * @param ctx
     * @return
     */
    public static boolean sendSms(String number, Context ctx) {
        for (int i = 0; i < number.length(); i++) {
            if (number.substring(i, i + 1).equals("*")) {
                Toast.makeText(ctx, "号码不全，请更新客户信息后联系", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        Uri uri = Uri.parse("sms:" + number);
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        ctx.startActivity(it);
        return true;
    }
    /*
    * 检测并且补全url
    */

    /**
     * 保存图片到相册
     *
     * @param context
     * @param bmp
     */
    public static void saveImageToGallery(final Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Constants.IMAGE_PATH, "Shoufangbao");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        final File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "抱歉，保存失败！", Toast.LENGTH_SHORT).show();
            Looper.loop();
            return;
        } catch (IOException e) {
            Toast.makeText(context, "抱歉，保存失败！", Toast.LENGTH_SHORT).show();
            Looper.loop();
            return;
        }
        SingleMediaScanner singleMediaScanner = new SingleMediaScanner(context, file);
    }

    /**
     * 判断当前Activity是否在前台运行
     */
    public static boolean isActivityRunning(Context mContext, String activityClassName) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
        if (info != null && info.size() > 0) {
            for (int i = 0; i < info.size(); i++) {
                ComponentName component = info.get(i).topActivity;
                if (activityClassName.equals(component.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName 是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    /**
     * 标题栏右边消失
     *
     * @param ctx   实例化上下文
     * @param title 标题
     */
    public static void handleTitleBarRightGone(final Activity ctx, String title) {
        TextView tv = (TextView) ctx.findViewById(R.id.navbar_title);
        tv.setText(title);
        ImageView leftbutton = (ImageView) ctx.findViewById(R.id.navbar_image_left);
        leftbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // 返回钮
                ctx.finish();
            }
        });
    }

    /**
     * 标题栏右边是图片的activity
     *
     * @param ctx      实例化上下文
     * @param title    标题
     * @param rightId  图片id
     * @param listener 点击事件
     */
    public static void handleTitleBarRightImg(final Activity ctx, String title, int rightId, View.OnClickListener listener) {
        TextView tv = (TextView) ctx.findViewById(R.id.navbar_title);
        tv.setText(title);
        ImageView leftbutton = (ImageView) ctx.findViewById(R.id.navbar_image_left);
        ImageView rightbutton = (ImageView) ctx.findViewById(R.id.navbar_image_right);
        rightbutton.setVisibility(View.VISIBLE);
        rightbutton.setImageResource(rightId);
        rightbutton.setOnClickListener(listener);
        leftbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.finish();
            }
        });
    }


    /**
     * 标题栏右边是图片的fragment
     *
     * @param ctx      实例化上下文
     * @param title    标题
     * @param rightId  图片id
     * @param listener 点击事件
     */
    public static void handleFragmentTitleBarRightImg(final Activity ctx, final View view, String title, int rightId, View.OnClickListener listener) {
        TextView tv = (TextView) view.findViewById(R.id.navbar_title);
        tv.setText(title);
        ImageView leftbutton = (ImageView) view.findViewById(R.id.navbar_image_left);
        ImageView rightbutton = (ImageView) view.findViewById(R.id.navbar_image_right);
        rightbutton.setVisibility(View.VISIBLE);
        rightbutton.setImageResource(rightId);
        rightbutton.setOnClickListener(listener);
        leftbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.finish();
            }
        });
    }


    /**
     * 标题栏右边是文字的
     *
     * @param ctx      实例化上下文
     * @param title    标题
     * @param right    右边文字
     * @param listener 点击事件
     */
    public static void handleTitleBarRightText(final Activity ctx, String title, String right, View.OnClickListener listener) {
        TextView tv = (TextView) ctx.findViewById(R.id.navbar_title);
        tv.setText(title);
        ImageView leftbutton = (ImageView) ctx.findViewById(R.id.navbar_image_left);
        TextView righttext = (TextView) ctx.findViewById(R.id.navbar_text_right);
        righttext.setVisibility(View.VISIBLE);
        righttext.setText(right);
        righttext.setOnClickListener(listener);
        leftbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.finish();
            }
        });
    }

    /**
     * 闪屏区分分辨率
     *
     * @param url
     * @return
     */
    public static String getSplashUrl(Context context, String url) {
        float density = DensityUtils.getDensity(context);
        String str = url;
        if (density > 2.0) {
            if (str.endsWith("jpg")) {
                str = str.replace(".jpg", "_1080_1670.jpg");
            } else {
                str = str.replace(".png", "_1080_1670.png");
            }
        } else {
            if (str.endsWith("jpg")) {
                str = str.replace(".jpg", "_640_800.jpg");
            } else {
                str = str.replace(".png", "_640_800.png");
            }
        }
        return str;
    }

    /**
     * banner区分分辨率
     *
     * @param context
     * @param url
     * @return
     */
    public static String getBannerUrl(Context context, String url) {
        float density = DensityUtils.getDensity(context);
        String str = url;
        if (density > 2.5) {
            if (str.endsWith("jpg")) {
                str = str.replace(".jpg", "_1080_480.jpg");
            } else {
                str = str.replace(".png", "_1080_480.png");
            }
        } else {
            if (str.endsWith("jpg")) {
                str = str.replace(".jpg", "_640_320.jpg");
            } else {
                str = str.replace(".png", "_640_320.png");
            }
        }
        return str;
    }

    /**
     * 检测其它系统信息
     *
     * @param context
     * @return
     */
    public static boolean checkOtherSystem(Context context) {
        boolean isFirst = (boolean) SPUtils.get(context, Constants.MIUI_FIRST, true);
        if (!AppUtils.getSystemProperty("ro.miui.ui.version.name").equals(null)) {
            String phoneInfo = AppUtils.getSystemProperty("ro.miui.ui.version.name");
            if (isFirst && phoneInfo.length() > 0) {
                SPUtils.put(context, Constants.MIUI_FIRST, false);
                return true;
            }
        }
        return false;
    }

    /**
     * 检测其它系统信息
     *
     * @param context
     * @return
     */
    public static boolean checkMiuiSystem(Context context) {
        if (!AppUtils.getSystemProperty("ro.miui.ui.version.name").equals(null)) {
            String phoneInfo = AppUtils.getSystemProperty("ro.miui.ui.version.name");
            if (phoneInfo.length() > 0) {
                SPUtils.put(context, Constants.MIUI_FIRST, false);
                return true;
            }
        }
        return false;
    }

    /**
     * 多个标题栏切换Fragment通用方法
     *
     * @param mContent 用于置换页面的第三个参数
     * @param from     当前Fragment
     * @param to       切换后的Fragment
     * @param FrameId  FrameLayout的Id
     * @param ctx      Activity的上下文
     */
    public static Fragment switchFragmentContent(Fragment mContent, Fragment from, Fragment to, int FrameId, FragmentActivity ctx) {
        if (mContent != to) {
            mContent = to;
            FragmentTransaction tran = ctx.getSupportFragmentManager().beginTransaction();
            if (!to.isAdded()) {
                tran.hide(from).add(FrameId, to);
            } else {
                tran.hide(from).show(to);
            }
            tran.commitAllowingStateLoss();
        }
        return mContent;
    }

    /**
     * 判断字符串为空
     *
     * @param str
     * @return
     */
    public static boolean stringIsEmpty(String str) {
        if (str == null || str.trim().length() <= 0) {
            return true;
        }
        return false;
    }

    public static boolean isPassWordIllegal(String pwd) {
        Pattern p = Pattern.compile("/(?!^\\d+$)(?!^[a-zA-Z]+$)[0-9a-zA-Z]{6,12}/");
        Matcher m = p.matcher(pwd);
        return m.matches();
    }


    /**
     * 将字符串中的中文转化为拼音,其他字符不变
     * 花花大神->huahuadashen
     *
     * @param inputString
     * @return
     */
    public static String getPingYin(String inputString) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        char[] input = inputString.trim().toCharArray();
        String output = "";

        try {
            for (char curchar : input) {
                if (java.lang.Character.toString(curchar).matches(
                        "[\\u4E00-\\u9FA5]+")) {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(
                            curchar, format);
                    output += temp[0];
                } else
                    output += java.lang.Character.toString(curchar);
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return output;
    }

    /**
     * 汉字转换为汉语拼音首字母，英文字符不变
     * 花花大神->hhds
     *
     * @param chinese 汉字
     * @return 拼音
     */
    public static String getFirstSpell(String chinese) {
        StringBuffer pybf = new StringBuffer();
        char[] arr = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (char curchar : arr) {
            if (curchar > 128) {
                try {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(curchar, defaultFormat);
                    if (temp != null) {
                        pybf.append(temp[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pybf.append(curchar);
            }
        }
        return pybf.toString().replaceAll("\\W", "").trim();
    }

    /**
     * 选择城市初始化json数据
     */

    public static String initCityJsonData(Context ctx, String name) {

        String str = null;
        try {
            StringBuffer sb = new StringBuffer();
            InputStream is = ctx.getAssets().open("area" + File.separator + name);
            int len = -1;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                sb.append(new String(buf, 0, len));
            }
            is.close();
            str = sb.toString();

        } catch (IOException e) {
            Toast.makeText(ctx, "本地存储异常", Toast.LENGTH_SHORT).show();
        }
        return str;
    }

    /**
     * 认证城市数据
     */

    public static List<AuthCity> getAuthCityJsonData(Context ctx) {
        List<AuthCity> list = null;
        String str = null;
        try {
            StringBuffer sb = new StringBuffer();
            InputStream is = ctx.getAssets().open("authcity.txt");
            int len = -1;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                sb.append(new String(buf, 0, len));
            }
            is.close();
            str = sb.toString();
            Gson mGson = new Gson();
            Result<List<AuthCity>> result = mGson.fromJson(str, new TypeToken<Result<List<AuthCity>>>() {
            }.getType());
            list = result.getContent();
        } catch (IOException e) {
            Toast.makeText(ctx, "本地存储异常", Toast.LENGTH_SHORT).show();
        }
        return list;
    }

    /**
     * 读取对应城市的json
     *
     * @param file
     * @param ctx
     * @param fileName
     * @return
     */
    public static String readCityJsonData(File file, Context ctx, String fileName) {
        String str = null;
        FileInputStream fis = null;
        try {
            StringBuffer sb = new StringBuffer();
            fis = new FileInputStream(file);
            byte[] b = new byte[1024];
            int len = -1;
            while ((len = fis.read(b)) != -1) {
                sb.append(new String(b, 0, len));
            }
            str = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                Toast.makeText(ctx, "本地存储异常", Toast.LENGTH_SHORT).show();
            }
        }

        return str;
    }

    /**
     * 隐藏键盘
     *
     * @param context
     */
    public static void delKeyBoard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * @param uid
     * @return
     */
    public static String getUserUid(int uid) {
        String uidStr = String.valueOf(uid);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 6 - uidStr.length(); i++) {
            sb.append("0");
        }
        sb.append(uidStr);
        return sb.toString();
    }

    public static boolean isLogin() {
        User user = User.getInstance();
        if (user.userType == User.TYPE_NORMAL_USER) {
            return true;
        } else {
            return false;
        }
    }

    //判断七位（前三后四）是不是手机号
    public static boolean isTopMobileNO(String mobiles) {
        Pattern p = Pattern
                .compile("^((13[0-9])|(15[0-9])|(18[0-9])|(14[0-9])|(17[0-9]))\\d{4}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    //获取屏幕分辨率
    public static float getDensity(Context context) {
        float SCALE = 0f;
        if (SCALE <= 0) {
            SCALE = context.getResources().getDisplayMetrics().density;
        }
        return SCALE;
    }

    /**
     * 登录注册
     *
     * @param context
     * @param message
     * @param type
     */
    public static void showRegDialog(final Context context, String message, final int type) {
        final AppDialog.Builder builder = new AppDialog.Builder(context, AppDialog.Builder.COMMONDIALOG);
        View view = LayoutInflater.from(context).inflate(R.layout.common_dialog_custom, null);
        builder.setContentView(view);
        builder.setMessage(message);
        builder.setPositiveButton("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
                Intent intent;
                switch (type) {
                    case 0:
                        intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(context, AuthActivity.class);
                        context.startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });
        builder.setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
            }
        });
        builder.create();
    }

    /**
     * @param context
     * @param message
     */
    public static void showCommonDialogOk(final Context context, String message, String title) {
        final AppDialog.Builder builder = new AppDialog.Builder(context, AppDialog.Builder.COMMON_OK);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_common_ok, null);
        builder.setContentView(view);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
            }
        });
        builder.create();
    }

    //检查任务是否完成
    public static boolean isTaskDone(Context ctx) {
        String key = User.getInstance().uid + "taskdone";
        return (boolean) SPUtils.get(ctx, key, false);
    }


    public static void setTaskDone(final Context ctx, int taskNum) {
//        int isRun = (int) SPUtils.get(ctx, Constants.TASK, 0);
//        if (isRun == 1) {
//            //如果默认任务不点击签到就出问题了
//            if (User.getInstance().userType != User.TYPE_NORMAL_USER || User.getInstance().verify
//                    != User.USER_THROUGH || User.getInstance().cityId == 0) {
//                return;
//            }
//
//            int currentNum = (int) SPUtils.get(ctx, User.getInstance().uid + Constants.TASKNUM, 250);
//            boolean isDone = false;
//            final Handler handler = new Handler() {
//                @Override
//                public void handleMessage(Message msg) {
//                    super.handleMessage(msg);
//                    switch (msg.what) {
//                        case 0:
//                            Toast.makeText(ctx, "今日有奖任务已完成，签到奖励进账啦", Toast.LENGTH_LONG).show();
//                            Intent it = new Intent();
//                            it.setAction("com.kupangstudio.shoufangbao.taskdone");
//                            ctx.sendBroadcast(it);
//                            String key = User.getInstance().uid + "taskdone";
//                            SPUtils.put(ctx, key, true);
//                            break;
//                        case 1:
//                            String key1 = User.getInstance().uid + "taskdone";
//                            SPUtils.put(ctx, key1, true);
//                            Toast.makeText(ctx, "今日有奖任务已完成，快去社区签到领奖啦", Toast.LENGTH_LONG).show();
//                            break;
//                        default:
//                            break;
//                    }
//                }
//            };
//            if (taskNum == currentNum) {
//                isDone = true;
//            }
//            if (isDone && !(boolean) SPUtils.get(ctx, User.getInstance().uid + "taskdone", false)) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        HashMap<String, String> map = getUpHashMap();
//                        map.put("action", "meet");
//                        map.put("module", Constants.MODULE_USER);
//                        map.put("uid", String.valueOf(User.getInstance().uid));
//                        map.put("id", User.getInstance().salt);
//                        OkHttpUtils.post().url(Constants.HOST_URL)
//                                .params(map)
//                                .build()
//                                .execute(new StringCallback() {
//                                    @Override
//                                    public void onError(Call call, Exception e) {
//                                        Toast.makeText(ctx, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
//                                    }
//
//                                    @Override
//                                    public void onResponse(String response) {
//                                        try {
//                                            JSONObject jsonObject = new JSONObject(response);
//                                            int code = jsonObject.getInt("code");
//                                            if(code > Result.RESULT_OK) {
//                                                handler.sendEmptyMessage(0);
//                                            } else {
//                                                handler.sendEmptyMessage(1);
//                                            }
//                                        } catch (JSONException e) {
//                                            return;
//                                        }
//                                    }
//                                });
//                    }
//                }).start();
//            }
//        }
    }

    /**
     * 获得统一上传参数
     *
     * @return
     */
    public static HashMap getUpHashMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("model", Build.MODEL);
        map.put("sdk", String.valueOf(Build.VERSION.SDK_INT));
        map.put("os", "1");
        map.put("versionCode", Constants.VERSION_CODE);
        map.put("imei", Constants.IMEI);
        return map;
    }

    /**
     * 网络类的对话框显示
     *
     * @param dialog
     * @param title
     */
    public static void progressDialogShow(ProgressDialog dialog, String title) {
        dialog.setMessage(title);
        dialog.show();
    }

    /**
     * 网络类的对话框消失
     *
     * @param dialog
     */
    public static void progressDialogDismiss(ProgressDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    //判断dialog的取消
    public static void closeDialog(ProgressDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    //md5
    public static String getMD5(String val) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(val.getBytes());
            byte[] m = md5.digest();// 加密
            return HexEncode(m);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return val;
    }

    public static String HexEncode(byte[] toencode) {
        StringBuilder sb = new StringBuilder(toencode.length * 2);
        for (byte b : toencode) {
            sb.append(Integer.toHexString((b & 0xf0) >>> 4));
            sb.append(Integer.toHexString(b & 0x0f));
        }
        return sb.toString();
    }

    public static String getSecret(HashMap<String, String> map) {
        StringBuffer secret = new StringBuffer();
        Object[] key = map.keySet().toArray();
        Arrays.sort(key);
        for (int i = 0; i < key.length; i++) {
            secret.append(key[i]);
            secret.append(getTwokey());
            secret.append(map.get(key[i]));
            if (i != key.length - 1) {
                secret.append("&");
            }
        }
        return secret.toString();
    }

    public static String getKey(String idkey, long time) {
        String str = null;
        Random random = new Random();
        int a = random.nextInt(10);
        if (a > 5) {
            int b = random.nextInt(10);
            a = a * b + (a - b) * (a + b + 1);
            str = str + a + "";
            b = b + a - (b * a) * (b * a + 1);
            a = a * b + (a - b) * (a + b + 1);
            str = str + a + "";
            b = b + a - (b * a) * (b * a + 1);
            str = str + a + "-" + b;
            a = (int) Math.sqrt(a);
            str = str + a + "" + "&8&*7&^&" + b + "";
        } else {
            int b = random.nextInt(10);
            b = a * b + (a - b) * (a + b + 1);
            str = str + a + "";
            b = b + a - (b * a) * (b * a + 1);
            a = a * b + (a - b) * (a + b + 1);
            str = str + a + "";
            b = b + a - (b * a) * (b * a + 1);
            str = str + a + "-" + b;
            a = (int) Math.sqrt(a);
            str = str + a + "" + "&8&*7&^&" + b + "";
        }
        return getMD5(idkey + String.valueOf(time).substring(2, 8)).substring(8, 24);
    }

    public static String getTwokey() {
        Random random = new Random();
        int a = random.nextInt(10);
        String str = null;
        if (a > 5) {
            int b = random.nextInt(10);
            a = a * b + (a - b) * (a + b + 1);
            str = str + a + "";
            b = b + a - (b * a) * (b * a + 1);
            str = str + a + "-" + b;
            a = (int) Math.sqrt(a);
            str = str + a + "" + "&8&*7&^&" + b + "";
        } else {
            int b = random.nextInt(10);
            b = a * b + (a - b) * (a + b + 1);
            str = str + a + "";
            b = b + a - (b * a) * (b * a + 1);
            str = str + a + "-" + b;
            a = (int) Math.sqrt(a);
            str = str + a + "" + "&8&*7&^&" + b + "";
        }
        str = "=";
        return str;
    }

    //积分进出帐记录类型添加
    public static List<PointRecord> addType(List<PointRecord> list, int type) {
        for (int i = 0; i < list.size(); i++) {
            PointRecord pointRecord = new PointRecord();
            pointRecord = list.get(i);
            pointRecord.setType(type);
            list.set(i, pointRecord);
        }
        return list;
    }

    //判断微信是否安装
    public static boolean isWXAppInstalled() {
        boolean b;
        Platform Tencent = ShareSDK.getPlatform(Wechat.NAME);
        b = Tencent.isClientValid();
        return b;
    }

    public static void addActivity(Activity activity) {
        ExitUtils.getInstance().addActivity(activity);
    }

    public static void removeActivity(Activity activity) {
        ExitUtils.getInstance().removeActivity(activity);
    }


    public static void setintent(Intent intent, String show, String bid, String newsid) {
        intent.putExtra("show", show);
        if (show.equals("buildDetail")) {
            intent.putExtra("bid", bid);
        } else if (show.equals("socialMessage")) {
            intent.putExtra("newsid", newsid);
        }
    }
}
