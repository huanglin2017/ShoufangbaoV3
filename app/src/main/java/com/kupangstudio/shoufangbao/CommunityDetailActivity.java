package com.kupangstudio.shoufangbao;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.OkHttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

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
import cn.sharesdk.wechat.friends.Wechat;
import okhttp3.Call;

/**
 * Created by Jsmi on 2015/11/23.
 * 新闻详情
 */

public class CommunityDetailActivity extends BaseActivity {
    private String url;
    private String title;
    private String content;
    private ProgressBar pb;
    private Handler mHandler;
    private int mid;
    private long startTime;
    private String shareUrl;
    private String imageUrl;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_detail);
        CommonUtils.addActivity(this);
        startTime = System.currentTimeMillis();
        //initImagePath();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        //任务
                        int ext = (int) SPUtils.get(CommunityDetailActivity.this, Constants.TASKPARAMETER, 250);
                        if (mid == ext) {
                            CommonUtils.setTaskDone(CommunityDetailActivity.this, 61);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        /*mid = intent.getIntExtra("mid", 0);
        String newsid = intent.getStringExtra("newsid");
        if (url == null) {
            url = (String) SPUtils.get(CommunityDetailActivity.this, Constants.NEWS_URL,
                    "https://www.shoufangbao.com/index.php?r=new/index&nid=") + newsid;
            mid = Integer.parseInt(newsid);
        }
        if (!url.startsWith("https://")) {
            url = "https://www.shoufangbao.com/index.php?r=new/index&nid=" + newsid;
            mid = Integer.parseInt(newsid);
        }*/

        title = intent.getStringExtra("title");
        content = intent.getStringExtra("content");
        if (imageUrl == null || imageUrl.equals("")) {
            imageUrl = intent.getStringExtra("image");
        }
        shareUrl = url + "&sfb=1" + "&uid=" + User.getInstance().uid;

        TextView t = (TextView) findViewById(R.id.navbar_title);
        t.setText("资讯详情");
        ImageView leftbutton = (ImageView) findViewById(R.id.navbar_image_left);
        ImageView share = (ImageView) findViewById(R.id.navbar_image_right);
        pb = (ProgressBar) findViewById(R.id.social_progress);
        share.setImageResource(R.drawable.share_build_detail);
        share.setVisibility(View.VISIBLE);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("newsname", title);
                MobclickAgent.onEvent(CommunityDetailActivity.this, "newsdetailshareclick", map);
                showShare();
            }
        });
        leftbutton.setImageResource(R.drawable.common_titlebar_left);
        leftbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final WebView message = (WebView) findViewById(R.id.social_message);
        message.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final WebView.HitTestResult result = message.getHitTestResult();
                int type = result.getType();
                if (type == WebView.HitTestResult.IMAGE_TYPE ||
                        type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CommunityDetailActivity.this);
                    builder.setMessage("保存图片")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String imageUrl = result.getExtra();
                                    saveImage(imageUrl, dialog);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
                return false;
            }
        });
        WebSettings webSettings = message.getSettings();
        webSettings.setJavaScriptEnabled(true);
        message.setWebViewClient(new WebClient());
        message.loadUrl(url);
        message.addJavascriptInterface(new JS(), "android");
    }

    private void saveImage(final String url, final DialogInterface dialog) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = ImageLoader.getInstance().loadImageSync(url);
                if(bitmap == null) {
                    Toast.makeText(CommunityDetailActivity.this, "图片保存失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                CommonUtils.saveImageToGallery(CommunityDetailActivity.this, bitmap);
                dialog.dismiss();
            }
        }).start();
    }

    public class JS {
        @JavascriptInterface
        public void data(String js) {
            try {
                JSONObject jsonObject = new JSONObject(js);
                title = jsonObject.getString("title");
                content = jsonObject.getString("desc");
                imageUrl = jsonObject.getString("pic");
            } catch (JSONException e) {
                title = "售房宝好文推荐，错过等三年";
                content = "听君一席话，胜读十本书，看小宝讲述咱经纪人自己的事情";
                shareUrl = url + "&sfb=1";
                imageUrl = "http://www.shoufangbao.net/images/logo/qr.png";
            }
        }
    }

    class WebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
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

    private void showShare() {
        if (shareUrl == null || shareUrl.equals("")) {
            return;
        }
        if (title == null || content == null || imageUrl == null ||
                title.equals("") || content.equals("") || imageUrl.equals("")) {
            title = "售房宝好文推荐，错过等三年";
            content = "听君一席话，胜读十本书，看小宝讲述咱经纪人自己的事情";
            shareUrl = url + "&sfb=1";
            imageUrl = "http://www.shoufangbao.net/images/logo/qr.png";
        }
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                if (platform.getName().equals(ShortMessage.NAME)) {
                    paramsToShare.setText(title + shareUrl + content);
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
        oks.setImageUrl(imageUrl);
        oks.setSite(getString(R.string.app_name));
        oks.setDialogMode();
        oks.setSiteUrl(shareUrl);
        oks.show(this);
        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                mHandler.sendEmptyMessage(0);
                addIntegrate(2);
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


    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        long endTime = System.currentTimeMillis();
        int duration = (int) (endTime - startTime); //开发者需要自己计算音乐播放时长
        Map<String, String> map_value = new HashMap<String, String>();
        MobclickAgent.onEventValue(this, "newsdetail", map_value, duration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    private void initImagePath() {
        String imagePath;
        try {
            String cachePath = Constants.CACHE_PATH;
            imagePath = cachePath + "myicon.jpg";
            File file = new File(imagePath);
            if (!file.exists()) {
                file.createNewFile();
                Bitmap pic = BitmapFactory.decodeResource(getResources(), R.drawable.myshare);
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
