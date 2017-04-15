package com.kupangstudio.shoufangbao;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Message;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.HashMap;

import okhttp3.Call;

/**
 * Created by long1 on 15/11/24.
 * Copyright 15/11/24 android_xiaobai.
 */
public class MessageTextDetailActivity extends BaseActivity {

    Message msg;
    boolean isFromNotify;
    private TextView title;
    private ImageView leftButton;
    private TextView msgTitle;
    private TextView msgTime;
    private TextView msgContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_text_detail);
        CommonUtils.addActivity(this);
        title = (TextView) this.findViewById(R.id.navbar_title);
        leftButton = (ImageView) this.findViewById(R.id.navbar_image_left);
        ImageView rightButton = (ImageView) this
                .findViewById(R.id.navbar_image_right);
        rightButton.setVisibility(View.GONE);
        msgTitle = (TextView) this.findViewById(R.id.actdetail_name);
        msgTime = (TextView) this.findViewById(R.id.actdetail_time);
        msgContent = (TextView) this.findViewById(R.id.actdetail_content);
        isFromNotify = getIntent().getBooleanExtra("isFromNotify", false);
        msg = (Message) getIntent().getSerializableExtra("msgobject");
        title.setText("消息详情");
        leftButton.setOnClickListener(mClickListener);
        msgTitle.setText(msg.getTitle());
        msgTime.setText(TimeUtils.getCustomReportData(msg.getCtime() * 1000));
        msgContent.setText("        " + msg.getContent());
        readMessage();
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

    private View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (isFromNotify) {
                Intent it = new Intent();
                it.setClassName(MessageTextDetailActivity.this,
                        MainActivity.class.getName());
                startActivity(it);
                finish();
            } else {
                finish();
            }
        }

    };

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
