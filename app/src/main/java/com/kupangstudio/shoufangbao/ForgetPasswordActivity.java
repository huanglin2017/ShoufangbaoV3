package com.kupangstudio.shoufangbao;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import okhttp3.Call;


public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener {

    private EditText phoneText;
    private EditText smsCodeText;
    private EditText passwdText;
    private EditText rePasswdText;
    private boolean checkState;
    private Button submitButton;
    private Button smsButton;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "忘记密码");
        initView();
        setClickListener();
        initData();
    }

    private void initData() {
        smsButton.setText("获取验证码");
        smsCodeText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6) {
                    checkState = true;
                    return;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initView() {
        smsButton = (Button) this.findViewById(R.id.forget_getsms);
        submitButton = (Button) this.findViewById(R.id.forget_button);
        phoneText = (EditText) this.findViewById(R.id.forget_phone);
        smsCodeText = (EditText) this.findViewById(R.id.forget_smscode);
        passwdText = (EditText) this.findViewById(R.id.forget_password);
        rePasswdText = (EditText) this.findViewById(R.id.forget_repassword);
    }

    private void setClickListener() {
        smsButton.setOnClickListener(this);
        submitButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forget_getsms://获取验证码
                String phone = phoneText.getText().toString();

                if (phone.isEmpty()) {
                    Toast.makeText(ForgetPasswordActivity.this, "请输入手机号",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!CommonUtils.isMobileNO(phone)) {
                    Toast.makeText(ForgetPasswordActivity.this, "手机号格式错误",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                smsButton.setText("获取中...");
                smsButton.setEnabled(false);

                getSmsCodeData(phone);
                break;
            case R.id.forget_button://提交
                String phone1 = phoneText.getText().toString();

                if (phone1.isEmpty()) {
                    Toast.makeText(ForgetPasswordActivity.this, "请输入手机号",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                //验证手机号码合法
                if (!CommonUtils.isMobileNO(phone1)) {
                    Toast.makeText(ForgetPasswordActivity.this, "手机号格式错误",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String code = smsCodeText.getText().toString();
                if (code.isEmpty()) {
                    Toast.makeText(ForgetPasswordActivity.this, "验证码不能为空", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (!checkState) {
                    Toast.makeText(ForgetPasswordActivity.this, "验证码错误",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                final String pwd = passwdText.getText().toString();
                if (pwd.isEmpty()) {
                    Toast.makeText(ForgetPasswordActivity.this, "请输入密码",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (pwd.length() < 6) {
                    Toast.makeText(ForgetPasswordActivity.this, "密码长度至少6位",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String repwd = rePasswdText.getText().toString();
                if (repwd.isEmpty() || !repwd.equals(pwd)) {
                    Toast.makeText(ForgetPasswordActivity.this, "前后两次密码不一致",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                //关掉键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //得到InputMethodManager的实例
                if (imm.isActive()) {
                    //如果开启
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                }
                getForwordPasswordData(phone1, pwd, code);
                break;
            default:
                break;
        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
        count = 0;
    }

    private void getSmsCodeData(String phone) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getCode");
        map.put("module", Constants.MODULE_USER);
        map.put("mobile", phone);
        map.put("type", Constants.SMSCODE_CHANGE_PASSWORD);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ForgetPasswordActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if (code < Result.RESULT_OK) {
                                //发送失败
                                smsButton.setEnabled(true);
                                smsButton.setText("获取验证码");
                            } else {
                                //验证码 以发出 开始倒计时
                                count = 60;
                                setTipButton(count);
                                mHandler.sendEmptyMessageDelayed(1, 1000);
                            }
                            Toast.makeText(ForgetPasswordActivity.this, notice, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(ForgetPasswordActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    private void getForwordPasswordData(String phone, String password, String code) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "forgetPassword");
        map.put("module", Constants.MODULE_USER);
        map.put("phone", phone);
        map.put("password", password);
        map.put("code", code);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ForgetPasswordActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if(code > Result.RESULT_OK) {
                                finish();
                            }
                            Toast.makeText(ForgetPasswordActivity.this, notice,
                                    Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(ForgetPasswordActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });

    }

    private MyHandler mHandler = new MyHandler(this);

    static class MyHandler extends Handler {
        WeakReference<ForgetPasswordActivity> mActivity;

        MyHandler(ForgetPasswordActivity activity) {
            mActivity = new WeakReference<ForgetPasswordActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ForgetPasswordActivity ac = mActivity.get();
            if (ac == null) {
                return;
            }

            if (msg.what == 1) {
                ac.count--;
                if (ac.count > 0) {
                    //开始循环
                    sendEmptyMessageDelayed(1, 1000);
                }
                ac.setTipButton(ac.count);
            }
        }
    }

    private void setTipButton(int count) {
        if (count <= 0) {
            smsButton.setText("获取验证码");
            smsButton.setEnabled(true);
        } else {
            smsButton.setText("等待 " + count + " 秒");
            smsButton.setEnabled(false);
        }
    }
}
