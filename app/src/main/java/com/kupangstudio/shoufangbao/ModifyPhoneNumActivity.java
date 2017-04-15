package com.kupangstudio.shoufangbao;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Report;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.AppUtils;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.FileUpload;
import com.kupangstudio.shoufangbao.utils.FileUtils;
import com.kupangstudio.shoufangbao.utils.SDCardUtils;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.OkHttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * Created by long1 on 15/11/23.
 * Copyright 15/11/23 android_xiaobai.
 * 修改手机号
 */
public class ModifyPhoneNumActivity extends BaseActivity {

    private static final String FILE_NAME = "modifyphone.jpg";
    private static final int TAKE_PHOTO = 1;
    private File file;
    private EditText etPrePhone;//原手机号
    private EditText etNewPhone;//新手机号
    private EditText etCode;//验证码
    private String prePhone;
    private String newPhone;
    private String code;
    private Button btnGetCode;//验证码
    private ImageView ivUpLoad;//上传的提示
    private ImageView ivUploadCard;//要上传的名片
    private Button btnCommit;//提交
    private TextView tvUpCard;
    private RelativeLayout layoutUp;
    private int count = 0;
    private boolean flag = false;
    private ProgressDialog dialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                count--;
                if (count > 0) {
                    // 开始循环
                    sendEmptyMessageDelayed(1, 1000);
                }
                setTipButton(count);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_phone_num);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "修改手机号");
        initView();
        setClickListener();
        init();
    }

    private void initView() {
        etPrePhone = (EditText) findViewById(R.id.et_pre_phone);
        etNewPhone = (EditText) findViewById(R.id.et_new_phone);
        etCode = (EditText) findViewById(R.id.et_code);
        btnGetCode = (Button) findViewById(R.id.btn_get_code);
        ivUpLoad = (ImageView) findViewById(R.id.iv_shangchuang);
        ivUploadCard = (ImageView) findViewById(R.id.iv_modify_card);
        btnCommit = (Button) findViewById(R.id.btn_modify_phone_commit);
        tvUpCard = (TextView) findViewById(R.id.tv_up_card);
        layoutUp = (RelativeLayout) findViewById(R.id.layout_upload);
    }

    private void setClickListener() {
        btnGetCode.setOnClickListener(mClickListener);
        btnCommit.setOnClickListener(mClickListener);
        ivUpLoad.setOnClickListener(mClickListener);
        layoutUp.setOnClickListener(mClickListener);
    }

    private void init() {
        Bitmap bm = BitmapFactory.decodeFile(Constants.IMAGE_PATH + File.separator + FILE_NAME);
        if (bm != null) {
            ivUploadCard.setImageBitmap(bm);
            ivUpLoad.setVisibility(View.GONE);
            tvUpCard.setVisibility(View.GONE);
        }
        if(User.getInstance().userType == User.TYPE_NORMAL_USER) {
            etPrePhone.setText(User.getInstance().mobile);
        }
        dialog = new ProgressDialog(ModifyPhoneNumActivity.this);
        dialog.setMessage("请稍后...");
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_get_code:
                    prePhone = etPrePhone.getText().toString();
                    newPhone = etNewPhone.getText().toString();
                    if (prePhone.equals(newPhone)) {
                        Toast.makeText(ModifyPhoneNumActivity.this, "手机号码一致，无需更改。", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(!prePhone.equals(User.getInstance().mobile)){
                        Toast.makeText(ModifyPhoneNumActivity.this, "原手机号码输入有误", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!CommonUtils.isMobileNO(prePhone)) {
                        Toast.makeText(ModifyPhoneNumActivity.this, "原手机号格式错误", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!CommonUtils.isMobileNO(newPhone)) {
                        Toast.makeText(ModifyPhoneNumActivity.this, "新手机号格式错误", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!flag) {
                        flag = true;
                        getSmsCodeData(newPhone);
                    }
                    break;
                case R.id.layout_upload:
                case R.id.iv_shangchuang:
                    tvUpCard.setVisibility(View.GONE);
                    if (!SDCardUtils.isSDCardEnable()) {
                        Toast.makeText(ModifyPhoneNumActivity.this, "SD卡不可用，请稍后重试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (AppUtils.checkPermission(ModifyPhoneNumActivity.this, "android.permission.CAMERA")) {
                        startActivityForResult(new Intent("android.media.action.IMAGE_CAPTURE"), TAKE_PHOTO);
                    } else {
                        Toast.makeText(ModifyPhoneNumActivity.this, "请您打开照相机权限", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;
                case R.id.btn_modify_phone_commit:
                    prePhone = etPrePhone.getText().toString();
                    newPhone = etNewPhone.getText().toString();
                    code = etCode.getText().toString();
                    if(!prePhone.equals(User.getInstance().mobile)){
                        Toast.makeText(ModifyPhoneNumActivity.this, "原手机号码输入有误", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (prePhone.equals(newPhone)) {
                        Toast.makeText(ModifyPhoneNumActivity.this, "手机号码一致，无需更改。", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        if (prePhone.isEmpty() || newPhone.isEmpty()) {
                            Toast.makeText(ModifyPhoneNumActivity.this, "请输入手机号", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        // 验证手机号码合法
                        if (!CommonUtils.isMobileNO(prePhone) && !CommonUtils.isMobileNO(newPhone)) {
                            Toast.makeText(ModifyPhoneNumActivity.this, "手机号格式错误", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                    }
                    if (code.isEmpty()) {
                        Toast.makeText(ModifyPhoneNumActivity.this, "请输入验证码", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    // 关掉键盘
                    InputMethodManager imm = (InputMethodManager) ModifyPhoneNumActivity.this
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    // 得到InputMethodManager的实例
                    if (imm.isActive()) {
                        // 如果开启
                        imm.hideSoftInputFromWindow(etPrePhone.getWindowToken(), 0);
                        imm.hideSoftInputFromWindow(etCode.getWindowToken(), 0);
                        imm.hideSoftInputFromWindow(etNewPhone.getWindowToken(), 0);
                    }
                    upLoadPic();
                    break;
                default:
                    break;
            }
        }
    };

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
        count = 0;
        OkHttpUtils.getInstance().cancelTag("modify_phone");
    }

    private void upLoadPic() {
        User user = User.getInstance();
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "changeMobile");
        map.put("module", Constants.MODULE_USER);
        map.put("oldMobile", prePhone);
        map.put("newMobile", newPhone);
        map.put("code", code);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        HashMap<String, File> mapCard = new HashMap<>();
        mapCard.put("card", file);
        new FileUpload(Constants.HOST_URL, map, mapCard, new MyListener(), dialog).execute("");
//        new OkHttpRequest.Builder().url(Constants.HOST_URL)
//                .params(map)
//                .tag("modify_phone")
//                .files(new Pair<String, File>("card", file))
//                .post(new ResultCallback<Result<Object>>() {
//
//                    @Override
//                    public void onBefore(Request request) {
//                        super.onBefore(request);
//
//                    }
//
//                    @Override
//                    public void onError(Request request, Exception e) {
//                        Toast.makeText(ModifyPhoneNumActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onResponse(Result<Object> response) {
//                        Toast.makeText(ModifyPhoneNumActivity.this, response.getNotice(), Toast.LENGTH_SHORT).show();
//                        if (response.getCode() < Result.RESULT_OK) {
//                        } else {
//                            finish();
//                        }
//                    }
//                });

    }

    class MyListener implements FileUpload.UploadListener {
        @Override
        public void onUploadEnd(boolean success, String object) {
            if (success) {
                try {
                    JSONObject obj = new JSONObject(object.toString());
                    String message = obj.getString("notice");
                    Toast.makeText(ModifyPhoneNumActivity.this, message, Toast.LENGTH_SHORT).show();
                    User user = User.getInstance();
                    user.verify = User.USER_DEAL;
                    User.saveUser(user, ModifyPhoneNumActivity.this);
                    EventBus.getDefault().post(user);
                    finish();
                } catch (JSONException e) {
                    Toast.makeText(ModifyPhoneNumActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ModifyPhoneNumActivity.this, "上传失败请稍后重试!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 验证码
     *
     * @param phone
     */
    private void getSmsCodeData(String phone) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getCode");
        map.put("module", Constants.MODULE_USER);
        map.put("mobile", phone);
        map.put("type", Constants.SMSCODE_REPHONE);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ModifyPhoneNumActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            Toast.makeText(ModifyPhoneNumActivity.this, notice, Toast.LENGTH_SHORT).show();
                            if(code < Result.RESULT_OK) {
                                flag = false;
                                // 发送失败
                                btnGetCode.setEnabled(true);
                                btnGetCode.setText("获取验证码");
                            } else {
                                // 验证码 以发出 开始倒计时
                                count = 60;
                                setTipButton(count);
                                mHandler.sendEmptyMessageDelayed(1, 1000);
                            }
                        } catch (JSONException e) {
                            return;
                        }
                    }
                });
    }

    public void setTipButton(int count) {
        if (count <= 0) {
            btnGetCode.setText("获取验证码");
            flag = false;
            btnGetCode.setEnabled(true);
        } else {
            btnGetCode.setText("等待 " + count + " 秒");
            btnGetCode.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                if(data == null) {
                    return;
                }
                Bitmap bm = (Bitmap) data.getExtras().get("data");
                ivUploadCard.setImageBitmap(bm);
                ivUpLoad.setVisibility(View.GONE);
                file = new File(Constants.IMAGE_PATH + File.separator + FILE_NAME);
                FileUtils.writeFile(file, 100, bm);
            }
        }
    }
}
