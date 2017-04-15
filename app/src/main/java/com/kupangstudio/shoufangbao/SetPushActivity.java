package com.kupangstudio.shoufangbao;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by long1 on 15/11/20.
 * Copyright 15/11/20 android_xiaobai.
 * 极光推送设置
 */
public class SetPushActivity extends BaseActivity {

    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_push);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "推送通知");
        checkBox = (CheckBox) findViewById(R.id.push_checkbox);
        boolean isSelected = (boolean) SPUtils.get(this, Constants.SET_PUSH, true);
        if (isSelected) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SPUtils.put(SetPushActivity.this, Constants.SET_PUSH, true);
                    finish();
                } else {
                    SPUtils.put(SetPushActivity.this, Constants.SET_PUSH, false);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
