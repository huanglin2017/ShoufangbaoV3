package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

/**
 * Created by long1 on 16/3/7.
 * Copyright 16/3/7 android_xiaobai.
 * 个人中心关于售房宝
 */
public class AboutSfbActivity extends BaseActivity {

    private LinearLayout layoutHelp;//帮助中心
    private LinearLayout layoutReport;//安全报备协议
    private LinearLayout layoutAbout;//关于售房宝
    private LinearLayout layoutVersion;//版本信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_sfb);
        CommonUtils.handleTitleBarRightGone(this, "关于售房宝");
        CommonUtils.addActivity(this);
        layoutHelp = (LinearLayout) findViewById(R.id.layout_help);
        layoutReport = (LinearLayout) findViewById(R.id.layout_report);
        layoutAbout = (LinearLayout) findViewById(R.id.layout_about);
        layoutVersion = (LinearLayout) findViewById(R.id.layout_version);
        layoutHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(AboutSfbActivity.this, CommonRuleActivity.class);
                it.putExtra("title", "帮助中心");
                it.putExtra("url", Constants.HELPCENTER);
                startActivity(it);
            }
        });
        layoutReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(AboutSfbActivity.this, ReportSafeActivity.class);
                String url = (String) SPUtils.get(AboutSfbActivity.this, Constants.REPORT_SAFE_URL, "https://www.shoufangbao.com/index.php?r=appweb/report");
                it.putExtra("url", url);
                it.putExtra("title", "报备安全协议");
                startActivity(it);
            }
        });
        layoutAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap map = new HashMap<String, String>();
                map.put("type", "aboutsfbclick");
                MobclickAgent.onEvent(AboutSfbActivity.this, "purchase", map);
                Intent it = new Intent(AboutSfbActivity.this, AboutActivity.class);
                startActivity(it);
            }
        });
        layoutVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap map = new HashMap<String, String>();
                MobclickAgent.onEvent(AboutSfbActivity.this, "versionclick", map);
                Intent it = new Intent(AboutSfbActivity.this, VersionActivity.class);
                startActivity(it);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
