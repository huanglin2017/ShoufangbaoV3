package com.kupangstudio.shoufangbao;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.utils.StringUtils;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.Call;

/**
 * Created by long1 on 15/12/1.
 * Copyright 15/12/1 android_xiaobai.
 */
public class CashActivity extends BaseActivity {

    private EditText etCard;
    private Button btnCash;
    private String cardNum;
    private TextView intro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "提现");
        etCard = (EditText) findViewById(R.id.etCard);
        btnCash = (Button) findViewById(R.id.cash_btn);
        intro = (TextView) findViewById(R.id.cash_intro);
        cardNum = (String) SPUtils.get(this, Constants.BANK_CARD_NUM, "");
        SpannableStringBuilder builder = new SpannableStringBuilder(intro.getText().toString());
        ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.parseColor("#c40202"));
        builder.setSpan(redSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        intro.setText(builder);
        btnCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AppDialog.Builder builder = new AppDialog.Builder(CashActivity.this, AppDialog.Builder.COMMONDIALOG);
                View view = LayoutInflater.from(CashActivity.this).inflate(R.layout.common_dialog_custom, null);
                builder.setContentView(view);
                builder.setMessage("确定提现？");
                builder.setPositiveButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (StringUtils.isEmpty(etCard.getText().toString())) {
                            Toast.makeText(CashActivity.this, "请输入银行卡号", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        cashMoney();
                    }
                });
                builder.setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.dismiss();
                    }
                });
                builder.create();
            }
        });
        etCard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    if (s.length() == 4) {
                        etCard.setText(s + " ");
                        etCard.setSelection(5);
                    }
                    if (s.length() == 9) {
                        etCard.setText(s + " ");
                        etCard.setSelection(10);
                    }
                    if (s.length() == 14) {
                        etCard.setText(s + " ");
                        etCard.setSelection(15);
                    }
                    if (s.length() == 19) {
                        etCard.setText(s + " ");
                        etCard.setSelection(20);
                    }
                } else if (count == 0) {
                    if (s.length() == 4) {
                        etCard.setText(s.subSequence(0, s.length() - 1));
                        etCard.setSelection(3);
                    }
                    if (s.length() == 9) {
                        etCard.setText(s.subSequence(0, s.length() - 1));
                        etCard.setSelection(8);
                    }
                    if (s.length() == 14) {
                        etCard.setText(s.subSequence(0, s.length() - 1));
                        etCard.setSelection(13);
                    }
                    if (s.length() == 19) {
                        etCard.setText(s.subSequence(0, s.length() - 1));
                        etCard.setSelection(18);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etCard.setText(cardNum);
    }

    /**
     * 提现
     */
    private void cashMoney() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "userWithdraw");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("bankcard", etCard.getText().toString());
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
                            String notice = jsonObject.getString("notice");
                            if(code > Result.RESULT_OK) {
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(CashActivity.this, notice, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(CashActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
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
