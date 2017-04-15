package com.kupangstudio.shoufangbao;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.utils.StringUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.mob.tools.utils.UIHandler;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler.Callback;

import java.util.HashMap;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;
import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Jsmi on 2015/11/4.
 * 登录页面
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener, Callback, PlatformActionListener {

    private Button mLogin;
    private TextView mForget;
    private TextView mRegist;
    private ImageView mLoginWechat;
    private LinearLayout mLoginWechatll;//微信登录布局
    private EditText mLoginPassword;//密码输入框
    private EditText mLoginPhone;//手机号输入框
    private static final int MSG_USERID_FOUND = 1;
    private static final int MSG_LOGIN = 2;
    private static final int MSG_AUTH_CANCEL = 3;
    private static final int MSG_AUTH_ERROR = 4;
    private static final int MSG_AUTH_COMPLETE = 5;
    private static final int MSG_SET_ALIAS = 1001;
    private static final int MOST_TIME = 5;
    private int aliasRetrycount = 0;
    private ProgressDialog dialog;
    private ProgressDialog dialog1;
    private boolean isNeedQuite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        CommonUtils.addActivity(this);
        ShareSDK.initSDK(this);
        initView();
        setClickListener();
        isNeedQuite = getIntent().getBooleanExtra("isneedquit", false);
    }

    private void initView() {
        mLoginPhone = (EditText) findViewById(R.id.login_phone);
        mLoginPassword = (EditText) findViewById(R.id.login_password);
        mLogin = (Button) findViewById(R.id.login_button);
        mForget = (TextView) findViewById(R.id.login_forget);
        mRegist = (TextView) findViewById(R.id.login_regist);
        mLoginWechat = (ImageView) findViewById(R.id.login_weixin);
        mLoginWechatll = (LinearLayout) findViewById(R.id.login_weixin_layout);
    }

    private void setClickListener() {
        mLogin.setOnClickListener(this);
        mForget.setOnClickListener(this);
        mRegist.setOnClickListener(this);
        mLoginWechat.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button://登录按钮
                String name = mLoginPhone.getText().toString();
                String pwd = mLoginPassword.getText().toString();

                if (CommonUtils.stringIsEmpty(name)) {
                    Toast.makeText(LoginActivity.this, "请输入手机号码", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (CommonUtils.stringIsEmpty(pwd)) {
                    Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!CommonUtils.isMobileNO(name)) {
                    Toast.makeText(LoginActivity.this, "手机号格式错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 关掉键盘
                InputMethodManager imm = (InputMethodManager) LoginActivity.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                // 得到InputMethodManager的实例
                if (imm.isActive()) {
                    // 如果开启
                    imm.hideSoftInputFromWindow(mLoginPhone.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(mLoginPassword.getWindowToken(), 0);
                }
                getLoginData(name, pwd);
                break;
            case R.id.login_forget://忘记密码
                Intent intent1 = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                startActivity(intent1);
                break;
            case R.id.login_regist://立即注册
                Intent intent = new Intent(LoginActivity.this, RegActivity.class);
                startActivity(intent);
                break;
            case R.id.login_weixin://微信登陆
                //移除微信授权
                if (CommonUtils.isWXAppInstalled()) {
                    authorize(ShareSDK.getPlatform(Wechat.NAME));
                } else {
                    Toast.makeText(LoginActivity.this, "未检测到微信客户端，请稍后重试！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void getLoginData(String phone, String password) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "login");
        map.put("module", Constants.MODULE_USER);
        map.put("mobile", phone);
        map.put("password", password);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog = new ProgressDialog(LoginActivity.this, R.style.Dialog_NoTitle_Transparennt);
                        dialog.setMessage("正在登录");
                        dialog.show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Toast.makeText(LoginActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            User user = null;
                            if (code > Result.RESULT_OK) {
                                JSONObject json = jsonObject.getJSONObject("content");
                                user = User.parseUser(LoginActivity.this, json, User.TYPE_NORMAL_USER);
                                mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS,
                                        user.uid + ""));
                                User.saveUser(user, LoginActivity.this);
                                EventBus.getDefault().post(user);
                                getTaskData();
                            } else {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                Toast.makeText(LoginActivity.this, notice, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
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
                                SPUtils.put(LoginActivity.this, Constants.TASKCHOICEA, jsonArray.getString(0));
                                SPUtils.put(LoginActivity.this, Constants.TASKCHOICEB, jsonArray.getString(1));
                                SPUtils.put(LoginActivity.this, Constants.TASKCHOICEC, jsonArray.getString(2));
                                if (obj.isNull("sid")) {
                                    SPUtils.put(LoginActivity.this, Constants.TASKID, 0);
                                } else {
                                    SPUtils.put(LoginActivity.this, Constants.TASKID, obj.getInt("sid"));
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
                                SPUtils.put(LoginActivity.this, Constants.TASKCONTENT, taskContent);
                                SPUtils.put(LoginActivity.this, User.getInstance().uid + Constants.TASKTIME, taskTime);
                                SPUtils.put(LoginActivity.this, User.getInstance().uid + Constants.TASKNUM, taskNum);
                                SPUtils.put(LoginActivity.this, Constants.TASKPARAMETER, taskParameter);
                                SPUtils.put(LoginActivity.this, Constants.TASKTITLE, taskTitle);
                                SPUtils.put(LoginActivity.this, Constants.TASKTIP, tip);
                                SPUtils.put(LoginActivity.this, Constants.TASKTRUE, truebtn);
                                SPUtils.put(LoginActivity.this, Constants.TASKFALSE, falsebtn);
                                String key = User.getInstance().uid + "taskdone";
                                SPUtils.put(LoginActivity.this, key, false);
                            }
                        } catch (JSONException e) {
                            if (!localTime.equals(preTime)) {
                                SPUtils.put(LoginActivity.this, Constants.TASKCONTENT, "首先，点击“客户”模块--点击右上角“添加客户”按钮--最后，填写相应的客户信息");
                                SPUtils.put(LoginActivity.this, User.getInstance().uid + Constants.TASKTIME, localTime);
                                SPUtils.put(LoginActivity.this, User.getInstance().uid + Constants.TASKNUM, 250);
                                SPUtils.put(LoginActivity.this, Constants.TASKPARAMETER, 0);
                                SPUtils.put(LoginActivity.this, Constants.TASKTITLE, "添加客户，体验小宝的客户管理功能");
                                SPUtils.put(LoginActivity.this, Constants.TASKTIP, "做任务 拿签到奖励");
                                SPUtils.put(LoginActivity.this, Constants.TASKTRUE, "做任务 拿奖励");
                                SPUtils.put(LoginActivity.this, Constants.TASKFALSE, "给奖也不要");
                                String key = User.getInstance().uid + "taskdone";
                                SPUtils.put(LoginActivity.this, key, false);
                            }
                        }
                        JumpMain();
                        finish();
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        if(dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    JPushInterface.setAlias(LoginActivity.this, (String) msg.obj, mAliasCallback);
                    break;
                default:
                    break;
            }
        }
    };

    private TagAliasCallback mAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            switch (code) {
                case 0:
                    // 建议这里往 SharePreference 里写一个成功设置的状态。成功设置一次后，以后不必再次设置了。
                    break;
                case 6002:
                    // 延迟 60 秒来调用 Handler 设置别名
                    // 重试5次
                    aliasRetrycount++;
                    if (aliasRetrycount > MOST_TIME) {
                        return;
                    }
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                    break;
                default:
                    break;
            }

        }
    };

    private void getWXLoginData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "wxlogin");
        map.put("wxname", User.getInstance().wxName);
        map.put("wxface", User.getInstance().wxFace);
        map.put("UnionID", User.getInstance().uniodId);
        map.put("module", Constants.MODULE_USER);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog1 = new ProgressDialog(LoginActivity.this);
                        CommonUtils.progressDialogShow(dialog1, "正在登录中...");
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(LoginActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                User user = null;
                                JSONObject json = jsonObject.getJSONObject("content");
                                user = User.parseUser(LoginActivity.this, json, User.TYPE_NORMAL_USER);
                                mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS,
                                        user.uid + ""));
                                User.saveUser(user, LoginActivity.this);
                                EventBus.getDefault().post(user);
                                JumpMain();
                                finish();
                            } else {
                                Intent intent = new Intent(LoginActivity.this, BindPhoneActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        CommonUtils.progressDialogDismiss(dialog1);
                    }
                });
    }

    private void authorize(Platform plat) {
        if (plat.isValid()) {
            String userId = plat.getDb().getUserId();
            if (!TextUtils.isEmpty(userId)) {
                UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
                //缓存部分用户资料字段
                User user = User.getInstance();
                user.uniodId = plat.getDb().get("unionid");
                user.wxName = plat.getDb().getUserName();
                user.wxFace = plat.getDb().getUserIcon();
                User.saveUser(user, LoginActivity.this);
                login(plat.getName(), userId, null);
            }
        }
        plat.setPlatformActionListener(this);
        plat.SSOSetting(true);
        plat.removeAccount();
        plat.showUser(null);
    }

    private void login(String plat, String userId, HashMap<String, Object> userInfo) {
        Message msg = new Message();
        msg.what = MSG_LOGIN;
        msg.obj = plat;
        UIHandler.sendMessage(msg, this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_USERID_FOUND: {
                Toast.makeText(LoginActivity.this, R.string.userid_found, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_LOGIN: {
                String text = getString(R.string.logining, msg.obj);
                Toast.makeText(LoginActivity.this, text, Toast.LENGTH_SHORT).show();
                getWXLoginData();
            }
            break;
            case MSG_AUTH_CANCEL: {
                Toast.makeText(LoginActivity.this, R.string.auth_cancel, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_AUTH_ERROR: {
                Toast.makeText(LoginActivity.this, R.string.auth_error, Toast.LENGTH_SHORT).show();

            }
            break;
            case MSG_AUTH_COMPLETE: {
                Toast.makeText(LoginActivity.this, R.string.auth_complete, Toast.LENGTH_SHORT).show();
            }
            break;
        }
        return false;
    }

    @Override
    public void onCancel(Platform platform, int action) {
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_CANCEL, this);
        }
    }

    @Override
    public void onError(Platform platform, int action, Throwable t) {
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_ERROR, this);
        }
        t.printStackTrace();
    }

    @Override
    public void onComplete(Platform platform, int action, HashMap<String, Object> hashMap) {
        //缓存部分用户资料字段
        User user = User.getInstance();
        user.uniodId = platform.getDb().get("unionid");
        user.wxName = platform.getDb().getUserName();
        user.wxFace = platform.getDb().getUserIcon();
        User.saveUser(user, LoginActivity.this);

        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_COMPLETE, this);
            login(platform.getName(), platform.getDb().getUserId(), hashMap);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
        ShareSDK.stopSDK();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if (dialog1 != null && dialog1.isShowing()) {
            dialog1.dismiss();
        }
    }

    private void JumpMain() {
        if (isNeedQuite) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}
