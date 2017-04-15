package com.kupangstudio.shoufangbao;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.utils.AppUtils;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.system.text.ShortMessage;
import cn.sharesdk.wechat.friends.Wechat;
import okhttp3.Call;

/**
 * Created by Jsmi on 2015/11/30.
 * 分享Banner
 */
public class ShareBannerActivity extends Activity {

    //handler监听分享成功的回调
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (integral != 0) {
                        //new AddIntegralAsyncTask().execute();
                    }
                    int position = (int) SPUtils.get(ShareBannerActivity.this, Constants.TASKPARAMETER, 0);
                    if (pos == position) {
                        CommonUtils.setTaskDone(ShareBannerActivity.this, 7);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private ProgressBar pb;
    private int integral;
    private int pos;
    private String title;
    private String url;
    private String shareUrl;
    private String content;
    private String imageUrl;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_banner);
        CommonUtils.addActivity(this);
        //接收传递数据
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        url = intent.getStringExtra("url");
        shareUrl = url;

        //初始化
        pb = (ProgressBar) findViewById(R.id.share_banner_pb);
        WebView webView = (WebView) findViewById(R.id.share_banner_web);
        ImageView right = (ImageView) findViewById(R.id.navbar_image_right);
        TextView titleText = (TextView) findViewById(R.id.navbar_title);
        ImageView left = (ImageView) findViewById(R.id.navbar_image_left);
        titleText.setText(title);

        //webview设置
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(this, "task");
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.setWebViewClient(new WebClient());
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.loadUrl(url);
        webView.addJavascriptInterface(new JS(), "android");

        //点击事件处理(分享)
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShare(content, title, imageUrl, url);
            }
        });
        //回退
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @JavascriptInterface
    public void setTask() {
        CommonUtils.setTaskDone(ShareBannerActivity.this, 100);
    }

    public class JS {
        @JavascriptInterface
        public void data(String js) {
            //Toast.makeText(ShareBannerActivity.this, "读取参数失败", Toast.LENGTH_SHORT).show();
            try {
                JSONObject jsonObject = new JSONObject(js);
                title = jsonObject.getString("title");
                content = jsonObject.getString("desc");
                imageUrl = jsonObject.getString("pic");
            } catch (JSONException e) {
                content = "售房宝又发福利，助力经纪人小伙伴一起飞，先到先得，莫错过";
                shareUrl = url;
                imageUrl = "http://www.shoufangbao.net/images/logo/qr.png";
            }
        }
    }

    //webview优化加载
    class WebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("tel:")) {
                CommonUtils.callPhone(url, ShareBannerActivity.this);
                return true;
            } else if (url.startsWith("sms:")) {
                String phone = url.substring(4, url.length());
                Uri uri = Uri.parse("smsto:" + phone);
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(intent);
            } else {
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            pb.setVisibility(View.GONE);
        }
    }

    //分享方法
    private void showShare(String contents, String shareTitle, String imgUrl, String shareUrls) {

        if(User.getInstance().userType != User.TYPE_NORMAL_USER){
            AppUtils.showRegAuthDialog(ShareBannerActivity.this, "请您先登录", 0);
            return;
        }

        if(shareUrls == null || shareUrls.equals("")){
            return;
        }

        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        if (imageUrl == null ||content == null || shareUrl == null || shareTitle == null ||
                content.equals("") || shareUrl.equals("") || shareTitle.equals("") || imageUrl.equals("")) {
            content = "售房宝又发福利，助力经纪人小伙伴一起飞，先到先得，莫错过";
            shareUrl = url;
            imgUrl = "http://www.shoufangbao.net/images/logo/qr.png";
        } else {
            content = contents;
            title = shareTitle;
            shareUrl = shareUrls;
        }
        shareUrl = shareUrl + "&uid=" + User.getInstance().uid;
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                if (platform.getName().equals(ShortMessage.NAME)) {
                    paramsToShare.setText(title + url + content);
                    paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
                }
                if (platform.getName().equals(Wechat.NAME)) {
                    paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
                }
            }
        });
        oks.setTitle(title);
        oks.setTitleUrl(shareUrl);
        oks.setText(content);
        oks.setUrl(shareUrl);
        oks.setImageUrl(imgUrl);
        oks.setSite(getString(R.string.app_name));
        oks.setDialogMode();
        oks.setSiteUrl(shareUrl);
        oks.show(this);
        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                handler.sendEmptyMessage(0);
                addIntegrate(3);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {

            }

            @Override
            public void onCancel(Platform platform, int i) {

            }
        });
    }

    /**
     * 添加积分
     */
    private void addIntegrate(int type) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "addIntegrate");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("type", String.valueOf(type));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
