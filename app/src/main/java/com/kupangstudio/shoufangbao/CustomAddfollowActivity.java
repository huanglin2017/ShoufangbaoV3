package com.kupangstudio.shoufangbao;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Custom;
import com.kupangstudio.shoufangbao.model.CustomFollow;
import com.kupangstudio.shoufangbao.model.FollowList;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Jsmi on 2015/11/5.
 * 添加客户跟进页面
 */
public class CustomAddfollowActivity extends BaseActivity implements View.OnClickListener {

    private EditText mEditText;
    private Button mButton;
    private Custom custom;
    private CustomFollow follow;
    private FollowList followList;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_addfollow);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "客户跟进");
        initView();
        setClickListener();
        Intent intent = getIntent();
        custom = (Custom) intent.getSerializableExtra("custom");
        follow = new CustomFollow();
    }

    private void initView() {
        mEditText = (EditText) findViewById(R.id.sug_text);
        mButton = (Button) findViewById(R.id.sug_commit);
    }

    private void setClickListener() {
        mButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String content = mEditText.getEditableText().toString();
        if (CommonUtils.stringIsEmpty(content)) {
            Toast.makeText(CustomAddfollowActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        followList = new FollowList();
        followList.setCid(custom.getCid());
        followList.setCtime(System.currentTimeMillis() / 1000);
        followList.updateAll("cid = ?", String.valueOf(custom.getCid()));

        // 沟通时间的更新 更新最后沟通时间
        custom.setFollow(followList);
        custom.setDATEACTION(Constants.CUSTOM_UPDATA);
        //EventBus.getDefault().post(custom);

        follow.setCid(custom.getCid());
        follow.setContent(content);
        follow.setType(Constants.TYPE_EDIT);
        follow.setCtime(System.currentTimeMillis() / 1000);
        getData();
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
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    private void getData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "addFollow");
        map.put("module", Constants.MODULE_CUSTOM);
        map.put("cid", String.valueOf(custom.getCid()));
        map.put("content", mEditText.getText().toString().trim());
        map.put("type", String.valueOf(Constants.TYPE_EDIT));
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog = new ProgressDialog(CustomAddfollowActivity.this);
                        CommonUtils.progressDialogShow(dialog, "添加中，请稍候...");
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(CustomAddfollowActivity.this, ResultError.MESSAGE_NULL,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                JSONObject object = jsonObject.getJSONObject("content");
                                follow.setFid(object.getInt("fid"));
                                Intent intent = new Intent(CustomAddfollowActivity.this, CustomDetailActivity.class);
                                intent.putExtra("follow", follow);
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                                CommonUtils.setTaskDone(CustomAddfollowActivity.this, 22);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(CustomAddfollowActivity.this, ResultError.MESSAGE_ERROR
                                    , Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        CommonUtils.progressDialogDismiss(dialog);
                    }
                });

    }
}
