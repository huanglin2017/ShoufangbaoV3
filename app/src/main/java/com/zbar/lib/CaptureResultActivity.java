package com.zbar.lib;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.base.Constants;
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
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;


/**
 * Created by Jsmi on 2015/11/12.
 * 扫描结果页面
 */

public class CaptureResultActivity extends BaseActivity {

    private String qrcode;
    private ProgressBar pb;
    private LinearLayout layout;
    private Button btn;
    private TextView btnCancle;
    private String operation;
    private int id;
    private String action;
    private int count = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    count++;
                    break;
                default:
                    break;
            }
        }
    };
    private TextView tishi;
    private Button btnRescan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captureresult);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "扫描结果");

        //获取上传信息以及扫描结果
        operation = getIntent().getStringExtra("operation");
        action = getIntent().getStringExtra("action");
        id = getIntent().getIntExtra("id", 0);
        qrcode = getIntent().getStringExtra("qrcode");

        pb = (ProgressBar) findViewById(R.id.pb_capture_result);
        layout = (LinearLayout) findViewById(R.id.capture_layout);
        btn = (Button) findViewById(R.id.capture_btn);
        btnRescan = (Button) findViewById(R.id.capture_btn_rescan);
        tishi = (TextView) findViewById(R.id.capture_text_tishi);
        btnCancle = (TextView) findViewById(R.id.capture_btn_cancle);
        btnRescan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CaptureResultActivity.this, CaptureActivity.class);
                intent.putExtra("operation", operation);
                intent.putExtra("action", action);
                intent.putExtra("id", id);
                startActivity(intent);
                finish();
            }
        });

        btnCancle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count >= 30) {
                    setTishi();
                } else {
                    finish();
                }
            }
        });

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        final Timer timer = new Timer();
        timer.schedule(timerTask, 1000, 1000);

        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count <= 30) {
                    layout.setVisibility(View.GONE);
                    pb.setVisibility(View.VISIBLE);
                    if (qrcode.startsWith("sfb_")) {
                        getData();
                    } else {
                        timer.cancel();
                    }
                } else {
                    setTishi();
                }
            }
        });
    }

    private void setTishi() {
        tishi.setText("有效期失效，敬请重新扫描");
        tishi.setTextColor(getResources().getColor(R.color.common_select));
        btn.setVisibility(View.GONE);
        btnRescan.setVisibility(View.VISIBLE);
        btnCancle.setVisibility(View.GONE);
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
    }


    //扫码登录
    private void getData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "qrLogin");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        map.put("code", qrcode);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        pb.setVisibility(View.GONE);
                        layout.setVisibility(View.VISIBLE);
                        Toast.makeText(CaptureResultActivity.this, ResultError.MESSAGE_NULL,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            Toast.makeText(CaptureResultActivity.this, notice, Toast.LENGTH_SHORT).show();
                            if (code > Result.RESULT_OK) {
                                CaptureResultActivity.this.finish();
                            } else {
                                pb.setVisibility(View.GONE);
                                layout.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            return;
                        }
                    }
                });
    }
}
