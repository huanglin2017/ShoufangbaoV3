package com.kupangstudio.shoufangbao;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.system.text.ShortMessage;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * Created by Jsmi on 2015/11/30.
 * 我的邀请
 */
public class MyInviteActivity extends BaseActivity {
    private WebView web;
    private ProgressBar pb;
    private TextView title;
    private ImageView left;
    private TextView number;
    private Button btnShare;
    private String url;
    private String inviteUrl;
    private long beginTime;
    private long endTime;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinvite);
        CommonUtils.addActivity(this);
        beginTime = System.currentTimeMillis();
        initImagePath();
        final User user = User.getInstance();
        inviteUrl = "https://www.shoufangbao.com/index.php?r=appweb/invite&uid=" + user.uid;
        url = (String) SPUtils.get(MyInviteActivity.this, Constants.INVITE_URL, "https://www.shoufangbao.com/index.php?r=user/invite&uid=") + user.uid;
        if (!url.startsWith("https://")) {
            url = "https://www.shoufangbao.com/index.php?r=user/invite&uid=" + user.uid;
        }
        web = (WebView) findViewById(R.id.myinvite_web);
        pb = (ProgressBar) findViewById(R.id.myinvite_pb);
        title = (TextView) findViewById(R.id.myinvite_title);
        left = (ImageView) findViewById(R.id.myinvite_left);
        title.setText("我的邀请");
        left.setImageResource(R.drawable.common_titlebar_left);
        web.setWebViewClient(new WebClient());
        WebSettings webSettings = web.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        web.addJavascriptInterface(new JS(), "android");
        web.loadUrl(url);
        left.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @JavascriptInterface
    public void setTask() {
        CommonUtils.setTaskDone(MyInviteActivity.this, 100);
    }

    public class JS {
        @JavascriptInterface
        public void jsWebViewCallAppShare(String title, String desc, String pic, String url) {
            //Toast.makeText(MyInviteActivity.this, title + desc + pic + url, Toast.LENGTH_LONG).show();
            showShare(title, desc, pic, url + User.getInstance().uid);
        }
    }


    private void showShare(String titles, String contents, String imgUrls, String shareUrls) {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        final String content = contents;
        final String title = titles;
        final String imgUrl = imgUrls;
        final String shareUrl = shareUrls;

        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                if (platform.getName().equals(ShortMessage.NAME)) {
                    paramsToShare.setText(shareUrl + content);
                }
                if (platform.getName().equals(WechatMoments.NAME)) {
                    paramsToShare.setTitle(content);
                }
            }
        });
        oks.setTitle(title);
        oks.setTitleUrl(shareUrl);
        oks.setText(content);

        oks.setImageUrl(imgUrl);
        oks.setUrl(shareUrl);
        oks.setSite(getString(R.string.app_name));
        oks.setDialogMode();
        oks.setSiteUrl(shareUrl);
        oks.show(this);

        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                CommonUtils.setTaskDone(MyInviteActivity.this, 43);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {

            }

            @Override
            public void onCancel(Platform platform, int i) {

            }
        });
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
    protected void onStop() {
        super.onStop();
        endTime = System.currentTimeMillis();
        int duration = (int) ((endTime - beginTime) / 1000);
        Map<String, String> map_value = new HashMap<String, String>();
        MobclickAgent.onEventValue(this, "myinvite", map_value, duration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    class WebClient extends WebViewClient {
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

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private void initImagePath() {
        try {
            String cachePath = Constants.CACHE_PATH;
            imagePath = cachePath + "myinvite.jpg";
            File file = new File(imagePath);
            if (!file.exists()) {
                file.createNewFile();
                Bitmap pic = BitmapFactory.decodeResource(getResources(), R.drawable.myinvite);
                FileOutputStream fos = new FileOutputStream(file);
                pic.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            imagePath = null;
        }
    }
}
