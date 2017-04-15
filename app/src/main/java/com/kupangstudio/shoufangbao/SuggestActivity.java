package com.kupangstudio.shoufangbao;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.StringUtils;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by long1 on 15/11/21.
 * Copyright 15/11/21 android_xiaobai.
 * 意见反馈
 */
public class SuggestActivity extends BaseActivity {

    private EditText etContent;
    private EditText etPhone;
    private Button btnSubmit;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest);
        CommonUtils.handleTitleBarRightGone(this, "意见反馈");
        CommonUtils.addActivity(this);
        initView();
        if (CommonUtils.isLogin() && !StringUtils.isEmpty(User.getInstance().mobile)) {
            etPhone.setText(User.getInstance().mobile);
        }
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> map5 = new HashMap<String, String>();
                MobclickAgent.onEvent(SuggestActivity.this, "sugbuttonclick", map5);
                String commitContent = etContent.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                if (StringUtils.isEmpty(phone)) {
                    Toast.makeText(SuggestActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!CommonUtils.isMobileNO(phone)) {
                    Toast.makeText(SuggestActivity.this, "手机号格式不合法", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(commitContent)) {
                    Toast.makeText(SuggestActivity.this, "提交内容不能为空", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                sendSuggest(phone, commitContent);
            }
        });
    }

    private void initView() {
        etContent = (EditText) findViewById(R.id.sug_text);
        etPhone = (EditText) findViewById(R.id.sug_phone);
        btnSubmit = (Button) findViewById(R.id.sug_commit);
    }

    private void sendSuggest(String mobile, String content) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "userFeedback");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("mobile", mobile);
        map.put("content", content);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        progress = new ProgressDialog(SuggestActivity.this);
                        progress.setTitle("请稍后");
                        progress.show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(SuggestActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            Toast.makeText(SuggestActivity.this, notice, Toast.LENGTH_SHORT).show();
                            if (code > Result.RESULT_OK) {
                                CommonUtils.setTaskDone(SuggestActivity.this, 42);
                                finish();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(SuggestActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    @Override
                    public void onAfter() {
                        super.onAfter();
                        if (progress != null && progress.isShowing()) {
                            progress.dismiss();
                        }
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
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }
}
