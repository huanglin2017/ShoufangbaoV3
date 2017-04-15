package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by long1 on 15/12/14.
 * Copyright 15/12/14 android_xiaobai.
 */
public class ReportSafeActivity extends BaseActivity {

    private WebView webview;
    private String title;
    private int task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_safe);
        CommonUtils.addActivity(this);
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        String url = intent.getStringExtra("url");
        task = intent.getIntExtra("task", 0);
        CommonUtils.handleTitleBarRightGone(this, title);
        initview(url);
    }

    private void initview(String url) {
        webview = (WebView) findViewById(R.id.webview_safe);
        //设置WebView属性，能够执行Javascript脚本
        webview.getSettings().setJavaScriptEnabled(true);
        //设置可以支持缩放
        webview.getSettings().setSupportZoom(true);
        //设置出现缩放工具
        webview.getSettings().setBuiltInZoomControls(true);
        //扩大比例的缩放
        webview.getSettings().setUseWideViewPort(true);
        //自适应屏幕
        webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webview.getSettings().setLoadWithOverviewMode(true);
        //加载需要显示的网页
        webview.loadUrl(url);
        //设置Web视图
        webview.setWebViewClient(new HelloWebViewClient());
    }

    @JavascriptInterface
    public void setTask() {
        CommonUtils.setTaskDone(ReportSafeActivity.this, 100);
    }

    //Web视图
    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView webView, String s) {
            super.onPageFinished(webView, s);
            if (title.equals("积分规则")) {
                CommonUtils.setTaskDone(ReportSafeActivity.this, 82);
            } else if (title.equals("玩玩乐")) {
                CommonUtils.setTaskDone(ReportSafeActivity.this, 81);
            } else {
                CommonUtils.setTaskDone(ReportSafeActivity.this, 100);
            }
        }
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
