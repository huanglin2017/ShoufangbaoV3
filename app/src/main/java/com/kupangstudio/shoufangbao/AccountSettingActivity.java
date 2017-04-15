package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseFragmentActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.LockPatternUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

import cn.jpush.android.api.JPushInterface;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;
import de.greenrobot.event.EventBus;

/**
 * Created by long1 on 15/11/18.
 * Copyright 15/11/18 android_xiaobai.
 * 帐户设置
 */
public class AccountSettingActivity extends BaseFragmentActivity {

    //手势密码
    private RelativeLayout layoutLockPattern;
    private TextView tvLockPattern;
    //来电悬浮窗
    private LinearLayout layoutFloatWindow;
    private TextView tvFloatWindow;
    //推送开关
    private LinearLayout layoutPush;
    private TextView tvPush;
    //修改密码
    private LinearLayout layoutPassword;
    //修改手机号
    private LinearLayout layoutPhoneNum;
    //合作账号
    private LinearLayout layoutAccount;
    private Button btnLogOut;
    private static final int REQ_CREATE_PATTERN = 1;//创建手势解锁
    private static final int REQ_ENTER_PATTERN = 2;//验证手势解锁
    private boolean isLockOpen;
    private boolean isFloatOpen;
    private boolean isPushOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ShareSDK.initSDK(this);
        setContentView(R.layout.activity_account_setting);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "账号设置");
        initView();
        setClickListener();
        init();
    }

    private void initView() {
        layoutLockPattern = (RelativeLayout) findViewById(R.id.set_lockpattern);
        tvLockPattern = (TextView) findViewById(R.id.set_lockpattern_tip);
        layoutFloatWindow = (LinearLayout) findViewById(R.id.set_float_window);
        tvFloatWindow = (TextView) findViewById(R.id.set_float_window_tip);
        layoutPush = (LinearLayout) findViewById(R.id.set_push);
        tvPush = (TextView) findViewById(R.id.set_push_tip);
        layoutPassword = (LinearLayout) findViewById(R.id.set_repassword);
        layoutPhoneNum = (LinearLayout) findViewById(R.id.set_phone_number);
        layoutAccount = (LinearLayout) findViewById(R.id.account_cooper);
        btnLogOut = (Button) findViewById(R.id.set_logout);
    }

    private void setClickListener() {
        layoutLockPattern.setOnClickListener(mClickListener);
        layoutFloatWindow.setOnClickListener(mClickListener);
        layoutPush.setOnClickListener(mClickListener);
        layoutPassword.setOnClickListener(mClickListener);
        layoutPhoneNum.setOnClickListener(mClickListener);
        layoutAccount.setOnClickListener(mClickListener);
        btnLogOut.setOnClickListener(mClickListener);
    }

    private void init() {
        isLockOpen = (boolean) SPUtils.get(AccountSettingActivity.this, Constants.IS_LOCK_OPEN, false);
        if (isLockOpen) {
            tvLockPattern.setText("开启");
        } else {
            tvLockPattern.setText("关闭");
        }
        isFloatOpen = (boolean) SPUtils.get(AccountSettingActivity.this, Constants.WIDOW_FLOATING, true);
        if (isFloatOpen) {
            tvFloatWindow.setText("开启");
        } else {
            tvFloatWindow.setText("关闭");
        }
        isPushOpen = (boolean) SPUtils.get(AccountSettingActivity.this, Constants.SET_PUSH, true);
        if (isPushOpen) {
            tvPush.setText("开启");
        } else {
            tvPush.setText("关闭");
        }
        if (CommonUtils.isLogin()) {
            btnLogOut.setVisibility(View.VISIBLE);
        } else {
            btnLogOut.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        isLockOpen = (boolean) SPUtils.get(AccountSettingActivity.this, Constants.IS_LOCK_OPEN, false);
        if (isLockOpen) {
            tvLockPattern.setText("开启");
        } else {
            tvLockPattern.setText("关闭");
        }
        isFloatOpen = (boolean) SPUtils.get(AccountSettingActivity.this, Constants.WIDOW_FLOATING, true);
        if (isFloatOpen) {
            tvFloatWindow.setText("开启");
        } else {
            tvFloatWindow.setText("关闭");
        }
        isPushOpen = (boolean) SPUtils.get(AccountSettingActivity.this, Constants.SET_PUSH, true);
        if (isPushOpen) {
            tvPush.setText("开启");
        } else {
            tvPush.setText("关闭");
        }
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            HashMap<String, String> map;
            User user = User.getInstance();
            switch (v.getId()) {
                case R.id.set_lockpattern://手势密码
                    if (isLockOpen) {
                        map = new HashMap<>();
                        map.put("type", "patternlock");
                        MobclickAgent.onEvent(AccountSettingActivity.this, "usersetoptionclick ", map);
                        intent = new Intent(AccountSettingActivity.this, EditGestureActivity.class);
                        startActivity(intent);
                    } else {
                        intent = new Intent(AccountSettingActivity.this, NewOrEditLockActivity.class);
                        intent.putExtra("option", "new");
                        startActivity(intent);
                    }
                    break;
                case R.id.set_float_window://悬浮窗
                    map = new HashMap<String, String>();
                    map.put("type", "floatwindow");
                    MobclickAgent.onEvent(AccountSettingActivity.this, "usersetoptionclick ", map);
                    intent = new Intent(AccountSettingActivity.this, SetFloatWindowActivity.class);
                    startActivity(intent);
                    break;
                case R.id.set_push://推送
                    map = new HashMap<String, String>();
                    map.put("type", "pushset");
                    MobclickAgent.onEvent(AccountSettingActivity.this, "usersetoptionclick ", map);
                    intent = new Intent(AccountSettingActivity.this, SetPushActivity.class);
                    startActivity(intent);
                    break;
                case R.id.set_repassword://修改密码
                    map = new HashMap<String, String>();
                    map.put("type", "resetpassword");
                    MobclickAgent.onEvent(AccountSettingActivity.this, "usersetoptionclick ", map);
                    if (CommonUtils.isLogin()) {
                        intent = new Intent(AccountSettingActivity.this, ModifyPasswordActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(AccountSettingActivity.this, "尚未注册，请先注册", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.set_phone_number://修改手机号
                    map = new HashMap<String, String>();
                    map.put("type", "resetphone");
                    MobclickAgent.onEvent(AccountSettingActivity.this, "usersetoptionclick ", map);
                    if (!CommonUtils.isLogin()) {
                        Toast.makeText(AccountSettingActivity.this, "尚未注册,请先注册", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent it = new Intent(AccountSettingActivity.this, ModifyPhoneNumActivity.class);
                        startActivity(it);
                    }
                    break;
                case R.id.account_cooper:
                    HashMap<String, String> map8 = new HashMap<String, String>();
                    MobclickAgent.onEvent(AccountSettingActivity.this, "weixinbindclick  ", map8);

                    if (CommonUtils.isLogin()) {
                        if (CommonUtils.isWXAppInstalled()) {
                            intent = new Intent(AccountSettingActivity.this, BindWeChatActivity.class);
                            if (User.getInstance().uniodId == null || User.getInstance().uniodId.equals("")) {
                                intent.putExtra("bind", "UNBIND");
                            } else {
                                intent.putExtra("bind", "BIND");
                                intent.putExtra("openid", User.getInstance().uniodId);
                                intent.putExtra("name", User.getInstance().wxName);
                                intent.putExtra("header", User.getInstance().wxFace);
                            }
                            startActivity(intent);
                        } else {
                            Toast.makeText(AccountSettingActivity.this, "未检测到微信客户端，请稍后重试！", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        CommonUtils.showRegDialog(AccountSettingActivity.this, "请您先登录", 0);
                    }
                    break;
                case R.id.set_logout://退出登录
                    User.logout(AccountSettingActivity.this);
                    //初始化微信授权，极光推送
                    ShareSDK.initSDK(AccountSettingActivity.this);
                    Platform platform = ShareSDK.getPlatform(Wechat.NAME);
                    platform.removeAccount();
                    //初始化解锁
                    LockPatternUtils lockPatternUtils = new LockPatternUtils(AccountSettingActivity.this);
                    lockPatternUtils.clearLock();
                    SPUtils.put(AccountSettingActivity.this, Constants.IS_LOCK_OPEN, false);
                    SPUtils.put(AccountSettingActivity.this, Constants.ISMEET, 0);
                    EventBus.getDefault().post(User.getInstance());
                    intent = new Intent(AccountSettingActivity.this, LoginActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 手势解锁的处理
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CREATE_PATTERN:
                tvLockPattern.setText("开启");
                SPUtils.put(AccountSettingActivity.this, Constants.IS_LOCK_OPEN, true);
                break;
            case REQ_ENTER_PATTERN:
                switch (resultCode) {
                    case RESULT_OK:

                        break;
                    case RESULT_CANCELED:

                        break;

                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
