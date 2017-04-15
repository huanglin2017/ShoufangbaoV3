package com.kupangstudio.shoufangbao;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.StringUtils;
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

/**
 * Created by Jsmi on 2015/12/1.
 * 绑定手机号
 */
public class BindPhoneActivity extends BaseActivity implements View.OnClickListener {
    private ImageView right;
    private Button bindPhonenum;
    private EditText mPhone;
    private Button huoqu;
    private boolean checkState;
    private boolean checkState1;
    public int count = 0;
    private String smsCode;
    private String name;
    private String openid;
    private String header;
    private String phoneNum;
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
    private EditText smsCodeText;
    private EditText yaoqingCodeText;
    private int checkId;
    private boolean flag = false;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_phone);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "绑定手机");
        Intent intent = getIntent();
        name = User.getInstance().wxName;
        openid = User.getInstance().uniodId;
        header = User.getInstance().wxFace;

        right = (ImageView) findViewById(R.id.navbar_image_right);
        right.setVisibility(View.GONE);
        bindPhonenum = (Button) findViewById(R.id.btn_bind_phoneNum);
        mPhone = (EditText) findViewById(R.id.et_phone);
        smsCodeText = (EditText) findViewById(R.id.bind_phonenum_text);
        yaoqingCodeText = (EditText) findViewById(R.id.bind_phonenum_text_1);
        yaoqingCodeText.setVisibility(View.GONE);
        huoqu = (Button) findViewById(R.id.btn_huoquNum);
        huoqu.setOnClickListener(this);
        bindPhonenum.setOnClickListener(this);
        smsCodeText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_bind_phoneNum:
                String smscode = smsCodeText.getText().toString();
                if (smscode.isEmpty()) {
                    Toast.makeText(BindPhoneActivity.this, "请输入验证码", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (!checkState) {
                    Toast.makeText(BindPhoneActivity.this, "验证码错误", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                String yaoqingCode = "";
                if (checkId == 0) {
                    yaoqingCode = yaoqingCodeText.getText().toString();
                }
                bindPhonenum.setEnabled(false);
                getWXRegisterData(smscode, yaoqingCode);
                break;
            case R.id.btn_huoquNum:
                String phone = mPhone.getText().toString().trim();
                if (StringUtils.isEmpty(phone)) {
                    Toast.makeText(BindPhoneActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!CommonUtils.isMobileNO(phone)) {
                    Toast.makeText(BindPhoneActivity.this, "手机号格式不对", Toast.LENGTH_SHORT).show();
                    return;
                }
                phoneNum = phone;
                if (!flag) {
                    flag = true;
                    getSmsCodeData(phoneNum);
                }
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

    private void getWXRegisterData(String smscode, String yaoqingCode) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "wxRegister");
        map.put("module", Constants.MODULE_USER);
        map.put("wxname", name);
        map.put("wxface", header);
        map.put("UnionID", openid);
        map.put("code", smscode);
        map.put("mobile", phoneNum);
        map.put("upid", yaoqingCode);
        map.put("uid", String.valueOf(User.getInstance().uid));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(BindPhoneActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if (code > Result.RESULT_OK) {
                                Toast.makeText(BindPhoneActivity.this, notice, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(BindPhoneActivity.this, MainActivity.class);
                                User user = User.getInstance();
                                user.uniodId = openid;
                                user.wxName = name;
                                user.wxFace = header;
                                User.saveUser(user, BindPhoneActivity.this);
                                EventBus.getDefault().post(user);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(BindPhoneActivity.this, ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(BindPhoneActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if(code < Result.RESULT_OK) {
                                Toast.makeText(BindPhoneActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                                // 发送失败
                                flag = false;
                                huoqu.setEnabled(true);
                                huoqu.setText("获取验证码");
                            } else {
                                Toast.makeText(BindPhoneActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                                // 验证码 以发出 开始倒计时
                                if (code == 2083) {
                                    checkId = 0;
                                    yaoqingCodeText.setVisibility(View.VISIBLE);
                                }
                                count = 60;
                                setTipButton(count);
                                mHandler.sendEmptyMessageDelayed(1, 1000);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(BindPhoneActivity.this, ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    public void setTipButton(int count) {
        if (count <= 0) {
            flag = false;
            huoqu.setText("获取验证码");
            huoqu.setEnabled(true);
        } else {
            huoqu.setText(count + " 秒");
            huoqu.setEnabled(false);

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
                    JPushInterface.setAliasAndTags(BindPhoneActivity.this, (String) msg.obj, null, mAliasCallback);
                    break;
                case MSG_ALIAS_SUCCESS:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
        CommonUtils.closeDialog(dialog);
    }

}
