package com.kupangstudio.shoufangbao;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.updateservice.UpdateManager;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

/**
 * Created by long1 on 15/11/23.
 * Copyright 15/11/23 android_xiaobai.
 * 版本信息
 */
public class VersionActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "版本信息");
        findViewById(R.id.version_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> map = new HashMap<String, String>();
                MobclickAgent.onEvent(VersionActivity.this, "versionupdateclick", map);
                if (CommonUtils.isServiceWork(VersionActivity.this, "com.kupangstudio.shoufangbao.updateservice.DownLoadService")) {
                    Toast.makeText(VersionActivity.this, "新版本安装包下载中，请稍后", Toast.LENGTH_SHORT).show();
                    return;
                }
                UpdateManager updateManager = new UpdateManager(VersionActivity.this, true);
                updateManager.checkUpdateInfo();
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
