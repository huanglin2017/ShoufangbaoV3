package com.kupangstudio.shoufangbao;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseFragmentActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.fragment.InComeRecordFragment;
import com.kupangstudio.shoufangbao.fragment.OutRecordFragment;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.AmnountUtils;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.Call;

/**
 * Created by long1 on 15/11/24.
 * Copyright 15/11/24 android_xiaobai.
 * 我的钱包
 */
public class MyWalletActivity extends BaseFragmentActivity {

    private TextView tvUnpay;//未提现
    private TextView tvPay;//已提现
    private Button btnCommit;//提现
    private RelativeLayout authLayout;//未认证
    private LinearLayout unAuthLayout;//已认证
    private TextView tvAuth;//认证
    private TextView inComeRecord;//进账记录
    private TextView outRecord;//提现记录
    private Fragment currentFragment;
    private InComeRecordFragment inComeRecordFragment;
    private OutRecordFragment outRecordFragment;
    public static final int REQUEST_CASH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mywallet);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "我的钱包");
        initView();
        init();
        setClickListener();
        getUserInfo();
        inComeRecordFragment = new InComeRecordFragment();
        outRecordFragment = new OutRecordFragment();
        currentFragment = inComeRecordFragment;
        getSupportFragmentManager().beginTransaction().add(R.id.frame_my_wallet, currentFragment).commitAllowingStateLoss();
        inComeRecord.setSelected(true);
        outRecord.setSelected(false);
        selectFragment(currentFragment, currentFragment);
    }

    private void initView() {
        tvUnpay = (TextView) findViewById(R.id.commission_moneyunpay);
        tvPay = (TextView) findViewById(R.id.commission_moneypayed);
        btnCommit = (Button) findViewById(R.id.commission_submit);
        authLayout = (RelativeLayout) findViewById(R.id.commission_headlayout);
        unAuthLayout = (LinearLayout) findViewById(R.id.commision_renzheng);
        tvAuth = (TextView) findViewById(R.id.commision_renzheng_btn);
        inComeRecord = (TextView) findViewById(R.id.commission_buttonleft);
        outRecord = (TextView) findViewById(R.id.commission_buttonright);
    }

    private void init() {
        User user = User.getInstance();
        if (user.verify == User.USER_THROUGH) {
            authLayout.setVisibility(View.VISIBLE);
            unAuthLayout.setVisibility(View.GONE);
        } else {
            authLayout.setVisibility(View.GONE);
            unAuthLayout.setVisibility(View.VISIBLE);
        }
        if (user.ammount == 0) {
            tvPay.setText("已提现：" + "0元");
            tvUnpay.setText("未提现：" + "0元");
        } else {
            try {
                tvPay.setText("已提现：" + AmnountUtils.changeF2Y(user.account) + "元");
                tvUnpay.setText("未提现：" + AmnountUtils.changeF2Y((user.ammount - user.account)) + "元");
            } catch (Exception e) {
                Toast.makeText(MyWalletActivity.this, "金额有误！", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setClickListener() {
        btnCommit.setOnClickListener(mClickListener);
        inComeRecord.setOnClickListener(mClickListener);
        outRecord.setOnClickListener(mClickListener);
        tvAuth.setOnClickListener(mClickListener);
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            HashMap<String, String> map;
            switch (v.getId()) {
                case R.id.commission_buttonleft:
                    map = new HashMap<String, String>();
                    map.put("type", "cashin");
                    MobclickAgent.onEvent(MyWalletActivity.this, "mycommissionclick", map);
                    inComeRecord.setSelected(true);
                    outRecord.setSelected(false);
                    selectFragment(currentFragment, inComeRecordFragment);
                    break;
                case R.id.commission_buttonright:
                    map = new HashMap<String, String>();
                    map.put("type", "cashout");
                    MobclickAgent.onEvent(MyWalletActivity.this, "mycommissionclick", map);
                    inComeRecord.setSelected(false);
                    outRecord.setSelected(true);
                    selectFragment(currentFragment, outRecordFragment);
                    break;
                case R.id.commission_submit:
                    map = new HashMap<String, String>();
                    MobclickAgent.onEvent(MyWalletActivity.this, "mycommissioncashclick", map);
//                    if (User.getInstance().style != User.NORMAL_BROKER) {
//                        CommonUtils.showCommonDialogOk(MyWalletActivity.this, "亲，成功带看两次就可晋升为标准经纪人进行提现啦", "");
//                        return;
//                    }
                    final Dialog dialog = new Dialog(MyWalletActivity.this, R.style.Dialog_Notitle);
                    final String money = tvUnpay.getText().toString();
                    if (Double.parseDouble(money.substring(4, money.length() - 1)) >= 10) {
                        View view = LayoutInflater.from(MyWalletActivity.this).inflate(R.layout.money_dialog, null);
                        Button weixin = (Button) view.findViewById(R.id.moneydialog_btn_weixin);
                        Button card = (Button) view.findViewById(R.id.moneydialog_btn_ka);
                        dialog.setContentView(view);
                        dialog.show();
                        weixin.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("type", "weixin");
                                MobclickAgent.onEvent(MyWalletActivity.this, "mycommissioncashtypeclick", map);
                                Intent intent = new Intent(MyWalletActivity.this, WeiXinChargesActivity.class);
                                startActivity(intent);
                            }
                        });
                        card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("type", "bank");
                                MobclickAgent.onEvent(MyWalletActivity.this, "mycommissioncashtypeclick", map);
                                if (Double.parseDouble(money.substring(4, money.length()-1)) < 100) {
                                    final Dialog dialog = new Dialog(MyWalletActivity.this, R.style.Dialog_Notitle);
                                    View view1 = LayoutInflater.from(MyWalletActivity.this).inflate(R.layout.money_dialog_notice_small, null);
                                    Button btn = (Button) view1.findViewById(R.id.moneydialog_btn);
                                    dialog.setContentView(view1);
                                    dialog.show();
                                    btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });
                                } else {
                                    dialog.dismiss();
                                    Intent it = new Intent(MyWalletActivity.this, CashActivity.class);
                                    startActivityForResult(it, REQUEST_CASH);;
                                }
                            }
                        });
                    } else {
                        Toast.makeText(MyWalletActivity.this,"亲，满10元后再提现哟！",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.commision_renzheng_btn:
                    if (User.getInstance().verify == User.USER_DEAL) {
                        Toast.makeText(MyWalletActivity.this, "您的认证已经提交，请耐心等待", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent it = new Intent(MyWalletActivity.this, AuthActivity.class);
                    startActivity(it);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            getUserInfo();
        }
    }

    private void selectFragment(Fragment a, Fragment b) {
        if (currentFragment != b) {
            currentFragment = b;
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (!b.isAdded()) {
                ft.hide(a).add(R.id.frame_my_wallet, b);
            } else {
                ft.hide(a).show(b);
            }
            ft.commitAllowingStateLoss();
        }
    }

    private void getUserInfo() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "userInfo");
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
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                String str = jsonObject.getJSONObject("content").toString();
                                User preUser = User.getInstance();
                                User user = null;
                                JSONObject json = new JSONObject(str);
                                user = User.parseUser(MyWalletActivity.this, json, preUser.userType);
                                User.saveUser(user, MyWalletActivity.this);
                                try {
                                    tvPay.setText("已提现：" + AmnountUtils.changeF2Y(user.account) + "元");
                                    tvUnpay.setText("未提现：" + AmnountUtils.changeF2Y((user.ammount - user.account)) + "元");
                                } catch (Exception e) {
                                    Toast.makeText(MyWalletActivity.this, "金额转换错误", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(MyWalletActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

}
