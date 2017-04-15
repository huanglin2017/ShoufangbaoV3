package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * 小米系统的人性化提示
 * Created by long on 15/11/6.
 * Copyright 2015 android_xiaobai.
 */
public class MiuiPointActivity extends BaseActivity {

    private TextView tvTitle;
    private ImageView ivBack;
    private Button btnSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miuipoint);
        CommonUtils.addActivity(this);
        initView();
        setClickListener();
        tvTitle.setText("开启来电提醒助手");
    }

    private void setClickListener() {
        btnSet.setOnClickListener(clickListener);
        ivBack.setOnClickListener(clickListener);
    }

    private void initView() {
        btnSet = (Button) findViewById(R.id.miui_button_set);
        tvTitle = (TextView) findViewById(R.id.navbar_title);
        ivBack = (ImageView) findViewById(R.id.navbar_image_left);
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

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.miui_button_set:
                    Intent intent = new Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + "com.kupangstudio.shoufangbao"));
                    startActivity(intent);
                    break;
                case R.id.navbar_image_left:
                    boolean setHomeCity = (boolean) SPUtils.get(MiuiPointActivity.this, Constants.HOME_SET_CITY, false);
                    if (setHomeCity) {//是否选择过首页城市
                        Intent it = new Intent(MiuiPointActivity.this, MainActivity.class);
                        startActivity(it);
                    } else {
                        Intent it = new Intent(MiuiPointActivity.this, SelectHomeCityActivity.class);
                        it.putExtra(Constants.IS_FROM_FIRST, true);
                        startActivity(it);
                    }
                    finish();
                    break;
                default:
                    break;
            }
        }
    };
}
