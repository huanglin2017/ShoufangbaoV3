package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
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
 * 来电悬浮窗设置
 */
public class SetFloatWindowActivity extends BaseActivity {

    private CheckBox checkBox;
    private Button btnSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_float_window);
        initView();
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "来电悬浮窗");
        boolean isSelect = (boolean) SPUtils.get(this, Constants.WIDOW_FLOATING, true);
        if (isSelect) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:"
                        + "com.kupangstudio.shoufangbao"));
                startActivity(intent);
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SPUtils.put(SetFloatWindowActivity.this, Constants.WIDOW_FLOATING, true);
                    finish();
                } else {
                    SPUtils.put(SetFloatWindowActivity.this, Constants.WIDOW_FLOATING, false);
                }
            }
        });
    }

    private void initView() {
        checkBox = (CheckBox) findViewById(R.id.float_window_checkbox);
        btnSet = (Button) findViewById(R.id.btn_set_float);
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
