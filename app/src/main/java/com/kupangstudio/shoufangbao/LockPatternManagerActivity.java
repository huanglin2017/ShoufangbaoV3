package com.kupangstudio.shoufangbao;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by long1 on 15/11/23.
 * Copyright 15/11/23 android_xiaobai.
 */
public class LockPatternManagerActivity extends BaseActivity {

    private LinearLayout modifyLock;//修改手势密码
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockpattern_manager);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "手势密码");
        initView();
        boolean flag = (boolean) SPUtils.get(LockPatternManagerActivity.this, Constants.IS_LOCK_OPEN, false);
        if(flag) {
            checkBox.setChecked(true);
            modifyLock.setVisibility(View.VISIBLE);
        } else {
            checkBox.setChecked(false);
            modifyLock.setVisibility(View.GONE);
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    modifyLock.setVisibility(View.VISIBLE);
                    SPUtils.put(LockPatternManagerActivity.this, Constants.IS_LOCK_OPEN, true);
                } else {
                    modifyLock.setVisibility(View.GONE);
                    SPUtils.put(LockPatternManagerActivity.this, Constants.IS_LOCK_OPEN, false);
                }
            }
        });
    }

    private void initView() {
        modifyLock = (LinearLayout) findViewById(R.id.lock_repassword);
        checkBox = (CheckBox) findViewById(R.id.lock_checkbox_pass);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
