package com.kupangstudio.shoufangbao;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Message;
import com.kupangstudio.shoufangbao.model.Report;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.OkHttpRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.HashMap;

import okhttp3.Call;

/**
 * Created by long1 on 15/11/24.
 * Copyright 15/11/24 android_xiaobai.
 */
public class MessageWebDetailActivity extends BaseActivity {

    Message msg;
    boolean isFromNotify;
    private String url;
    private WebView webView;
    private ImageView ivLeft;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_webdetail);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "消息详情");
        progressBar = (ProgressBar) findViewById(R.id.msg_web_pb);
        ivLeft = (ImageView) findViewById(R.id.navbar_image_left);
        webView = (WebView) findViewById(R.id.my_message_web);
        isFromNotify = getIntent().getBooleanExtra("isFromNotify", false);
        msg = (Message) getIntent().getSerializableExtra("msgobject");
        url = msg.getContent();
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        ivLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    if (isFromNotify) {
                        Intent it = new Intent();
                        it.setClassName(MessageWebDetailActivity.this,
                                MainActivity.class.getName());
                        startActivity(it);
                        finish();
                    } else {
                        finish();
                    }
                }
            }
        });
        webView.loadUrl(url);
        webView.setWebViewClient(new MessageWebClient());
        readMessage();
    }

    class MessageWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String s) {
            if (url.startsWith("tel:")) {
                CommonUtils.callPhone(url, MessageWebDetailActivity.this);
                return true;
            } else if (url.startsWith("sms:")) {
                String phone = url.substring(4, url.length());
                Uri uri = Uri.parse("smsto:" + phone);
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(intent);
            } else {
                webView.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView webView, String s) {
            super.onPageFinished(webView, s);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
            super.onPageStarted(webView, s, bitmap);
            progressBar.setVisibility(View.VISIBLE);
        }
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

    private void readMessage() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "readMessage");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("mid", String.valueOf(msg.getMid()));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                if (msg.getStatus() == Message.STATE_UNREAD) {
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("status", String.valueOf(Message.STATE_READ));
                                    DataSupport.updateAll(Message.class, contentValues, "mid = ?", String.valueOf(msg.getMid()));
                                }
                            }
                        } catch (JSONException e) {
                            return;
                        }
                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
