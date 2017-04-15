package com.kupangstudio.shoufangbao;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.mob.tools.utils.UIHandler;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;
import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Jsmi on 2015/12/1.
 * 绑定微信
 */
public class BindWeChatActivity extends BaseActivity implements PlatformActionListener, Handler.Callback {
    private Button btnweixin;
    private String face;
    private String openid;
    private RelativeLayout rtbangding;
    private RelativeLayout rtjiebang;
    private TextView tvweixin;
    private boolean flag = false;
    private RelativeLayout bdcontent;
    private RelativeLayout jbcontent;
    private TextView tvcontent;
    private String flag1;
    private String nickname;
    private static final int MSG_USERID_FOUND = 1;
    private static final int MSG_LOGIN = 2;
    private static final int MSG_AUTH_CANCEL = 3;
    private static final int MSG_AUTH_ERROR = 4;
    private static final int MSG_AUTH_COMPLETE = 5;
    private long startTime;
    private long endTime;
    private ProgressDialog dialog;
    private ProgressDialog dialog1;


    /*  程序步奏：
  1.点击微信绑定按钮之后,把按钮设置为解除绑定，绑定视图隐藏，显示解除绑定的视图,
  请求服务器完成微信绑定的操作。
  2.点击解除绑定按钮之后，请求服务器完成解除微信绑定的操作。Activity关闭。
  */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_wechat);
        CommonUtils.addActivity(this);
        startTime = System.currentTimeMillis();
        CommonUtils.handleTitleBarRightGone(this, "微信绑定");
        ShareSDK.initSDK(this);
        Intent intent = getIntent();
        flag1 = intent.getStringExtra("bind");

        nickname = intent.getStringExtra("name");
        openid = intent.getStringExtra("openid");
        face = intent.getStringExtra("header");

        //初始化所有需要用到的控件
        rtbangding = (RelativeLayout) findViewById(R.id.rt_bangding);//绑定微信的视图
        bdcontent = (RelativeLayout) findViewById(R.id.rt_bdcontent);//绑定微信时的文字
        jbcontent = (RelativeLayout) findViewById(R.id.rt_jbcontent);//解除绑定的文字
        rtjiebang = (RelativeLayout) findViewById(R.id.rt_jiebang);//解除绑定微信的视图
        tvweixin = (TextView) findViewById(R.id.tv_weixin);//解除绑定微信号视图里要显示的微信号
        btnweixin = (Button) findViewById(R.id.btn_bangding);//按钮
        tvcontent = (TextView) findViewById(R.id.tv_content);
        if (flag1.equals("BIND")) {
            flag = true;
            btnweixin.setText("解除绑定");
            rtbangding.setVisibility(View.GONE);
            bdcontent.setVisibility(View.GONE);
            jbcontent.setVisibility(View.VISIBLE);
            rtjiebang.setVisibility(View.VISIBLE);
            tvcontent.setText(nickname);
        }
        btnweixin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag) {
                    //解除绑定的操作
                    getUnBindData();
                } else {
                    dialog1 = new ProgressDialog(BindWeChatActivity.this);
                    CommonUtils.progressDialogShow(dialog1, "正在绑定中...");
                    //移除微信授权
                    Platform platform = ShareSDK.getPlatform(Wechat.NAME);
                    platform.removeAccount();
                    authorize(ShareSDK.getPlatform(Wechat.NAME));
                }
            }
        });

    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        endTime = System.currentTimeMillis();
        int duration = (int) ((endTime - startTime) / 1000); //开发者需要自己计算音乐播放时长
        Map<String, String> map_value = new HashMap<String, String>();
        MobclickAgent.onEventValue(this, "weixinbind", map_value, duration);
    }

    private void authorize(Platform plat) {
        if (plat.isValid()) {
            String userId = plat.getDb().getUserId();
            if (!TextUtils.isEmpty(userId)) {
                User user = User.getInstance();
                user.uniodId = plat.getDb().get("unionid");
                user.wxName = plat.getDb().getUserName();
                user.wxFace = plat.getDb().getUserIcon();
                User.saveUser(user, BindWeChatActivity.this);
                UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
                login(plat.getName(), userId, null);
                return;
            }
        }
        plat.setPlatformActionListener(this);
        plat.SSOSetting(true);
        plat.removeAccount();
        plat.showUser(null);
    }

    private void login(String plat, String userId, HashMap<String, Object> userInfo) {
        Message msg = new Message();
        msg.what = MSG_LOGIN;
        msg.obj = plat;
        UIHandler.sendMessage(msg, this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_USERID_FOUND: {
                CommonUtils.progressDialogDismiss(dialog1);
                Toast.makeText(BindWeChatActivity.this, R.string.userid_found, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_LOGIN: {
                String text = getString(R.string.logining, msg.obj);
                Toast.makeText(BindWeChatActivity.this, text, Toast.LENGTH_SHORT).show();
                getBindData();
            }
            break;
            case MSG_AUTH_CANCEL: {
                CommonUtils.progressDialogDismiss(dialog1);
                Toast.makeText(BindWeChatActivity.this, R.string.auth_cancel, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_AUTH_ERROR: {
                CommonUtils.progressDialogDismiss(dialog1);
                Toast.makeText(BindWeChatActivity.this, R.string.auth_error, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_AUTH_COMPLETE: {
                CommonUtils.progressDialogDismiss(dialog1);
                Toast.makeText(BindWeChatActivity.this, R.string.auth_complete, Toast.LENGTH_SHORT).show();
            }
            break;
        }
        return false;
    }

    @Override
    public void onCancel(Platform platform, int action) {
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_CANCEL, this);
        }
    }

    @Override
    public void onError(Platform platform, int action, Throwable t) {
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_ERROR, this);
        }
        t.printStackTrace();
    }

    @Override
    public void onComplete(Platform platform, int action, HashMap<String, Object> hashMap) {
        //解析部分用户资料字段
        openid = platform.getDb().get("unionid");
        nickname = platform.getDb().getUserName();
        face = platform.getDb().getUserIcon();
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_COMPLETE, this);
            login(platform.getName(), platform.getDb().getUserId(), hashMap);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
        ShareSDK.stopSDK(BindWeChatActivity.this);
        CommonUtils.closeDialog(dialog);
    }

    //微信解除绑定操作
    private void getUnBindData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "unBindwx");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog = new ProgressDialog(BindWeChatActivity.this);
                        CommonUtils.progressDialogShow(dialog, "正在解绑中...");
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(BindWeChatActivity.this, ResultError.MESSAGE_NULL,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if (code > Result.RESULT_OK) {
                                flag = false;
                                User user = User.getInstance();
                                user.uniodId = "";
                                user.wxName = "";
                                user.wxFace = "";
                                User.saveUser(user, BindWeChatActivity.this);
                                //移除微信授权
                                Platform platform = ShareSDK.getPlatform(Wechat.NAME);
                                platform.removeAccount();
                                btnweixin.setText("立即绑定");
                                rtbangding.setVisibility(View.VISIBLE);
                                bdcontent.setVisibility(View.VISIBLE);
                                jbcontent.setVisibility(View.GONE);
                                rtjiebang.setVisibility(View.GONE);
                                EventBus.getDefault().post(user);
                            } else {
                                Toast.makeText(BindWeChatActivity.this, notice,
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(BindWeChatActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        CommonUtils.progressDialogDismiss(dialog);
                    }
                });
    }

    //微信绑定的异步操作
    private void getBindData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "wxBind");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        map.put("wxname", nickname);
        map.put("wxface", face);
        map.put("UnionID", openid);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(BindWeChatActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if (code > Result.RESULT_OK) {
                                flag = true;
                                btnweixin.setText("解除绑定");
                                rtbangding.setVisibility(View.GONE);
                                bdcontent.setVisibility(View.GONE);
                                jbcontent.setVisibility(View.VISIBLE);
                                rtjiebang.setVisibility(View.VISIBLE);
                                tvcontent.setText(nickname);
                                User user = User.getInstance();
                                if (user.uniodId == null || user.uniodId.equals("")) {
                                    user.uniodId = openid;
                                    user.wxName = nickname;
                                    user.wxFace = face;
                                    User.saveUser(user, BindWeChatActivity.this);
                                }
                                EventBus.getDefault().post(user);
                            }
                            Toast.makeText(BindWeChatActivity.this, notice, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(BindWeChatActivity.this, ResultError.MESSAGE_ERROR
                                    , Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        CommonUtils.progressDialogDismiss(dialog1);
                    }
                });
    }
}
