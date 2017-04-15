package com.kupangstudio.shoufangbao;

import android.os.Bundle;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by long1 on 15/12/1.
 * Copyright 15/12/1 android_xiaobai.
 */
public class WeiXinChargesActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weixin_charges);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "售房宝");
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
