package com.kupangstudio.shoufangbao;

import android.os.Bundle;
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
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.Call;

/**
 * Created by long1 on 15/11/21.
 * Copyright 15/11/21 android_xiaobai.
 * 修改密码
 */
public class ModifyPasswordActivity extends BaseActivity {

    private EditText etPrePassWord;//原密码
    private EditText etNewPassWord;//新密码
    private EditText etNewAgain;//再次输入新密码
    private Button btnOk;//提交
    private String pass;
    private String newpass;
    private String repass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "修改密码");
        initview();
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pass = etPrePassWord.getText().toString();
                newpass = etNewPassWord.getText().toString();
                repass = etNewAgain.getText().toString();
                if (StringUtils.isEmpty(pass)) {
                    Toast.makeText(ModifyPasswordActivity.this, "原密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newpass.isEmpty()) {
                    Toast.makeText(ModifyPasswordActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                } else if (newpass.length() < 6) {
                    Toast.makeText(ModifyPasswordActivity.this, "请输入至少6位密码", Toast.LENGTH_SHORT).show();
                    return;
                } else if(!CommonUtils.isPassword(newpass)) {
                    Toast.makeText(ModifyPasswordActivity.this, "请输入数字与字母组合密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newpass.equals(repass)) {
                    Toast.makeText(ModifyPasswordActivity.this, "新密码前后两次不一致", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newpass.equals(pass)) {
                    Toast.makeText(ModifyPasswordActivity.this, "新密码与原密码一致，无需修改", Toast.LENGTH_SHORT).show();
                    return;
                }
                modifyPassWord();
            }
        });
    }

    private void initview() {
        etPrePassWord = (EditText) findViewById(R.id.repass_text);
        etNewPassWord = (EditText) findViewById(R.id.repass_newtext);
        etNewAgain = (EditText) findViewById(R.id.repass_retext);
        btnOk = (Button) findViewById(R.id.repass_affirm);
    }

    private void modifyPassWord() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "changePassword");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("oldPassword", pass);
        map.put("newPassword", newpass);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ModifyPasswordActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            Toast.makeText(ModifyPasswordActivity.this, notice, Toast.LENGTH_SHORT).show();
                            if(code > Result.RESULT_OK) {
                                finish();
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
