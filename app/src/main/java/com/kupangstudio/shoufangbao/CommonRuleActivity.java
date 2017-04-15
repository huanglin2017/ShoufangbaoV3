package com.kupangstudio.shoufangbao;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.ProgressBar;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Jsmi on 2015/11/24.
 * web通用界面
 */
public class CommonRuleActivity extends BaseActivity{
    private WebView webview;
    private String title;
    private int task;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_rule);
        CommonUtils.addActivity(this);
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        String url = intent.getStringExtra("url");
        task = intent.getIntExtra("task", 0);
        CommonUtils.handleTitleBarRightGone(this, title);
        initview(url);
        findViewById(R.id.navbar_image_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(webview.canGoBack()) {
                    webview.goBack();
                } else {
                    finish();
                }
            }
        });
    }
    @SuppressLint("SetJavaScriptEnabled")
    private void initview(String url) {
        webview = (WebView) findViewById(R.id.webview);

        pb = (ProgressBar)findViewById(R.id.common_rule_pb);
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
        CommonUtils.setTaskDone(CommonRuleActivity.this, 100);
    }

    @Override
    //设置回退
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            webview.goBack(); //goBack()表示返回WebView的上一页面
            return true;
        } else {
            finish();
        }
        return false;
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
            pb.setVisibility(View.GONE);
            if(title.equals("积分规则")) {
                CommonUtils.setTaskDone(CommonRuleActivity.this, 82);
            }else if(title.equals("玩玩乐")){
                CommonUtils.setTaskDone(CommonRuleActivity.this, 81);
            }else {
                CommonUtils.setTaskDone(CommonRuleActivity.this, 100);
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
