package com.kupangstudio.shoufangbao;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jsmi on 2015/11/23.
 */
public class HelpCenterActivity extends BaseActivity {

    private long beginTime;
    private ScrollView scrollView;
    Handler mHandler = new Handler();
    private TextView signTitle;
    private TextView signRule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);
        CommonUtils.addActivity(this);
        int flag = getIntent().getIntExtra("isJump", 0);
        beginTime = System.currentTimeMillis();
        initView();
        CommonUtils.handleTitleBarRightGone(this, "帮助中心");
        scrollView = (ScrollView) findViewById(R.id.scroll);
        if (flag == 1) {
            mHandler.postDelayed(mRunnable, 200);
            signTitle.setTextColor(getResources().getColor(R.color.common_red));
            signRule.setTextColor(getResources().getColor(R.color.common_red));
        }
    }

    private void initView(){
        signTitle = (TextView)findViewById(R.id.help_sign_title);
        signRule = (TextView)findViewById(R.id.help_sign_rule);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            scrollView.scrollTo(0, 700);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        long endTime = System.currentTimeMillis();
        int duration = (int) ((endTime - beginTime) / 1000);
        Map<String, String> map_value = new HashMap<String, String>();
        MobclickAgent.onEventValue(this, "helpcenter", map_value, duration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
