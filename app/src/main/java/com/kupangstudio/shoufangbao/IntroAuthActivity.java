package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by long1 on 15/12/2.
 * Copyright 15/12/2 android_xiaobai.
 */
public class IntroAuthActivity extends BaseActivity{

    private ImageView guideCancel;
    private ImageView guideOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_auth);
        CommonUtils.addActivity(this);
        guideCancel = (ImageView) findViewById(R.id.guideauth_cancel);
        guideOk = (ImageView) findViewById(R.id.guideauth_ok);
        guideCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(IntroAuthActivity.this, MainActivity.class);
                startActivity(it);
                finish();
            }
        });
        guideOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(IntroAuthActivity.this, AuthActivity.class);
                it.putExtra("isFromIntro", true);
                startActivity(it);
                finish();
            }
        });
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
