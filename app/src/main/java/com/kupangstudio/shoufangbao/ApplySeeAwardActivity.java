package com.kupangstudio.shoufangbao;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.AppUtils;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.FileUtils;
import com.kupangstudio.shoufangbao.utils.SDCardUtils;
import com.kupangstudio.shoufangbao.utils.StringUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by long1 on 15/11/25.
 * Copyright 15/11/25 android_xiaobai.
 * 申请带看奖励
 */
public class ApplySeeAwardActivity extends BaseActivity {

    private static final String FILE_NAME = "applyaward.jpg";
    private static final int TAKE_PHOTO = 1;
    private File file;
    private RelativeLayout photoLayout;
    private LinearLayout visibleLayout;
    private ImageView ivPhoto;
    private TextView tvPosition;
    private Button btnCommit;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private int rid;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtils.addActivity(this);
        setContentView(R.layout.activity_apply_see_award);
        CommonUtils.handleTitleBarRightGone(this, "申请领取奖励");
        initView();
        rid = getIntent().getIntExtra("rid", 0);
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();
        mLocationClient.start();
        photoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SDCardUtils.isSDCardEnable()) {
                    Toast.makeText(ApplySeeAwardActivity.this, "SD卡不可用，请稍后重试", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (AppUtils.checkPermission(ApplySeeAwardActivity.this, "android.permission.CAMERA")) {
                    startActivityForResult(new Intent("android.media.action.IMAGE_CAPTURE"), TAKE_PHOTO);
                } else {
                    Toast.makeText(ApplySeeAwardActivity.this, "请您打开照相机权限", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.isEmpty(tvPosition.getText().toString()) ||
                        tvPosition.getText().toString().equals("")) {
                    Toast.makeText(ApplySeeAwardActivity.this, "请等待定位结果", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ivPhoto.getDrawable() == null) {
                    Toast.makeText(ApplySeeAwardActivity.this, "请先进行拍照", Toast.LENGTH_SHORT).show();
                    return;
                }
                applyAward();
            }
        });
    }

    private void initView() {
        photoLayout = (RelativeLayout) findViewById(R.id.award_photo_layout);
        visibleLayout = (LinearLayout) findViewById(R.id.award_takephoto);
        ivPhoto = (ImageView) findViewById(R.id.award_photo);
        tvPosition = (TextView) findViewById(R.id.award_position);
        btnCommit = (Button) findViewById(R.id.award_commit);
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                tvPosition.setText("");
                return;
            }
            tvPosition.setText(location.getAddrStr());
        }
    }

    private void applyAward() {

        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "buildSee");
        map.put("module", Constants.MODULE_ADVERT);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("rid", String.valueOf(rid));
        map.put("address", tvPosition.getText().toString());
        OkHttpUtils.post().url(Constants.HOST_URL)
                .tag("apply_award")
                .params(map)
                .build()
                .execute(new StringCallback() {

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog = new ProgressDialog(ApplySeeAwardActivity.this);
                        dialog.setMessage("申请奖励中...");
                        dialog.show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ApplySeeAwardActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String notice = jsonObject.getString("notice");
                            Toast.makeText(ApplySeeAwardActivity.this, notice, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(ApplySeeAwardActivity.this, ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        OkHttpUtils.getInstance().cancelTag("apply_award");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                Bitmap bm = (Bitmap) data.getExtras().get("data");
                ivPhoto.setImageBitmap(bm);
                file = new File(Constants.IMAGE_PATH + File.separator + FILE_NAME);
                FileUtils.writeFile(file, 80, bm);
                visibleLayout.setVisibility(View.GONE);
            }
        }
    }

}
