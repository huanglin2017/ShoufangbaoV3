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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Jsmi on 2015/11/4.
 * 注册页面
 */
public class RegActivity extends BaseActivity implements View.OnClickListener {

    private EditText mRegistPhone;
    private EditText mRegistSmsCode;
    private EditText mRegistPassword;
    private EditText mRegistInviteCode;
    private LinearLayout mRegistSmsLl;//验证码布局
    private Button mRegistGetSms;
    private Button mRegist;
    private int count = 0;
    private boolean flag = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                count--;
                if (count > 0) {
                    // 开始循环
                    sendEmptyMessageDelayed(1, 1000);
                }
                setTipButton(count);
            }
        }
    };
    private long startTime;
    private long endTime;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        startTime = System.currentTimeMillis();
        CommonUtils.addActivity(this);
        initView();
        setCliclkListener();
        CommonUtils.handleTitleBarRightGone(RegActivity.this, "注册");
        mRegistGetSms.setText("获取验证码");
    }

    private void initView() {
        mRegistPhone = (EditText) findViewById(R.id.regist_phone);
        mRegistSmsCode = (EditText) findViewById(R.id.regist_smscode);
        mRegistPassword = (EditText) findViewById(R.id.regist_password);
        mRegistInviteCode = (EditText) findViewById(R.id.regist_invitecode);
        mRegistSmsLl = (LinearLayout) findViewById(R.id.regist_linear_smscode);
        mRegistGetSms = (Button) findViewById(R.id.regist_getsms);
        mRegist = (Button) findViewById(R.id.regist_button);
    }

    private void setCliclkListener() {
        mRegistGetSms.setOnClickListener(this);
        mRegist.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.regist_getsms://获取验证码
                String phone1 = mRegistPhone.getText().toString().trim();

                if (phone1.isEmpty()) {
                    Toast.makeText(RegActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!CommonUtils.isMobileNO(phone1)) {
                    Toast.makeText(RegActivity.this, "手机号格式错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                mRegistGetSms.setText("获取中...");
                mRegistGetSms.setEnabled(false);
                if(!flag){
                    flag = true;
                    getSmsCodeData(phone1);
                }
                break;
            case R.id.regist_button://注册按钮
                String phone = mRegistPhone.getText().toString().trim();
                String password = mRegistPassword.getText().toString().trim();
                String smscode = mRegistSmsCode.getText().toString();

                if (phone.isEmpty()) {
                    Toast.makeText(RegActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 验证手机号码合法
                if (!CommonUtils.isMobileNO(phone)) {
                    Toast.makeText(RegActivity.this, "手机号格式错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(RegActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                } else if (password.length() < 6) {
                    Toast.makeText(RegActivity.this, "请输入至少6位密码", Toast.LENGTH_SHORT).show();
                    return;
                } else if(!CommonUtils.isPassword(password)) {
                    Toast.makeText(RegActivity.this, "请输入数字与字母组合密码", Toast.LENGTH_SHORT).show();
                    return;
                }


            if (smscode.isEmpty()) {
                    Toast.makeText(RegActivity.this, "请输入验证码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (smscode.length() != 6) {
                    Toast.makeText(RegActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                String inviteCode = mRegistInviteCode.getText().toString();
                if (TextUtils.isEmpty(inviteCode)) {
                    inviteCode = "";
                } else {
                    String str = mRegistInviteCode.getText().toString();
                    inviteCode = str;
                }
                // 关掉键盘
                InputMethodManager imm = (InputMethodManager) RegActivity.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                // 得到InputMethodManager的实例
                if (imm.isActive()) {
                    // 如果开启
                    imm.hideSoftInputFromWindow(mRegistPhone.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(mRegistSmsCode.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(mRegistPassword.getWindowToken(), 0);
                }
                getRegData(phone, password, smscode, inviteCode);
                break;
            default:
                break;
        }
    }

    private void getRegData(String phone, String password, String smscode, String inviteCode) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "register");
        map.put("module", Constants.MODULE_USER);
        map.put("mobile", phone);
        map.put("password", password);
        map.put("code", smscode);
        map.put("upid", inviteCode);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog = new ProgressDialog(RegActivity.this, R.style.Dialog_NoTitle_Transparennt);
                        dialog.setMessage("正在注册");
                        dialog.show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Toast.makeText(RegActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                String str = jsonObject.getJSONObject("content").toString();
                                User user = null;
                                JSONObject json = new JSONObject(str);
                                user = User.parseUser(RegActivity.this, json, User.TYPE_NORMAL_USER);
                                rHandler.sendMessage(rHandler.obtainMessage(MSG_SET_ALIAS,
                                        user.uid + ""));
                                User.saveUser(user, RegActivity.this);
                                EventBus.getDefault().post(user);
                                Intent it = new Intent(RegActivity.this, IntroAuthActivity.class);
                                startActivity(it);
                                finish();
                            }
                            Toast.makeText(RegActivity.this, jsonObject.getString("notice"), Toast.LENGTH_SHORT).show();
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(RegActivity.this, ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    private void getSmsCodeData(String phone) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getCode");
        map.put("module", Constants.MODULE_USER);
        map.put("mobile", phone);
        map.put("type", Constants.SMSCODE_REGISTER);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(RegActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if(code < Result.RESULT_OK) {
                                flag = false;
                                // 发送失败
                                mRegistGetSms.setEnabled(true);
                                mRegistGetSms.setText("获取验证码");
                            } else {
                                // 验证码 以发出 开始倒计时
                                count = 60;
                                setTipButton(count);
                                mHandler.sendEmptyMessageDelayed(1, 1000);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(RegActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        endTime = System.currentTimeMillis();
        int duration = (int) (endTime - startTime);
        MobclickAgent.onEventValue(RegActivity.this, "register",null,duration);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
        count = 0;
    }

    public void setTipButton(int count) {
        if (count <= 0) {
            mRegistGetSms.setText("获取验证码");
            flag = false;
            mRegistGetSms.setEnabled(true);
        } else {
            mRegistGetSms.setText("等待 " + count + " 秒");
            mRegistGetSms.setEnabled(false);
        }
    }

    private static final int MOST_TIME = 5;
    private int aliasRetrycount = 0;
    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            switch (code) {
                case 0:
                    // 建议这里往 SharePreference 里写一个成功设置的状态。成功设置一次后，以后不必再次设置了。
                    rHandler.sendEmptyMessage(MSG_ALIAS_SUCCESS);
                    break;
                case 6002:
                    // 延迟 60 秒来调用 Handler 设置别名
                    // 重试5次
                    aliasRetrycount++;
                    if (aliasRetrycount > MOST_TIME) {
                        return;
                    }
                    rHandler.sendMessageDelayed(rHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                    break;
            }
        }
    };

    private static final int MSG_SET_ALIAS = 1001;
    private static final int MSG_ALIAS_SUCCESS = 1002;
    private final Handler rHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    JPushInterface.setAliasAndTags(RegActivity.this, (String) msg.obj, null, mAliasCallback);
                    break;
                case MSG_ALIAS_SUCCESS:
                    break;
            }
        }
    };
}
