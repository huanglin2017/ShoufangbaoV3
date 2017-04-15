package com.kupangstudio.shoufangbao;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.AuthArea;
import com.kupangstudio.shoufangbao.model.AuthCity;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.AppUtils;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.FileUpload;
import com.kupangstudio.shoufangbao.utils.ImageUtils;
import com.kupangstudio.shoufangbao.utils.SDCardUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.utils.StringUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pickerview.OptionsPopupWindow;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.request.OkHttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by long1 on 15/11/24.
 * Copyright 15/11/24 android_xiaobai.
 * 认证
 */
public class AuthActivity extends BaseActivity {

    private static final String FILE_NAME_CARD = "authcard.jpg";
    private static final int TAKE_PHOTO_CARD = 1;
    private static final String FILE_NAME_IDCARD = "authidcard.jpg";
    private static final int TAKE_PHOTO_IDCARD = 2;
    private EditText etName;
    private TextView tvCity;
    private EditText etProject;
    private RelativeLayout cardLayout;
    private ImageView ivCard;
    private ImageView ivCardTag;
    private RelativeLayout idCardLayout;
    private ImageView ivIdCard;
    private ImageView ivIdCardTag;
    private Button btnVerify;
    private File authCardFile;
    private File authIdCardFile;
    private List<AuthCity> list;
    private ProgressDialog dialog;
    private ArrayList<ArrayList<String>> area = new ArrayList<>();
    private ArrayList<String> city = new ArrayList<>();
    private OptionsPopupWindow popupWindow;
    private int selectCityId;
    private int selectAreaId;
    private ProgressDialog dialogUp;
    private boolean isFromIntro;
    private long startTime;
    private long endTime;
    private ImageView left;
    private boolean isCardTake;
    private boolean isIdCardTake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        startTime = System.currentTimeMillis();
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "身份认证");
        isFromIntro = getIntent().getBooleanExtra("isFromIntro", false);
        initView();
        getAuthCityData();
        dialogUp = new ProgressDialog(this);
        dialogUp.setMessage("上传中，请稍后。。。");
        tvCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
            }
        });
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFromIntro) {
                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    finish();
                }
            }
        });
        popupWindow.setOnoptionsSelectListener(new OptionsPopupWindow.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                AuthCity authCity = list.get(options1);
                AuthArea authArea = authCity.getList().get(option2);
                selectCityId = authCity.getCityid();
                selectAreaId = authArea.getAreaid();
                tvCity.setText(authCity.getCity() + " " + authArea.getArea());
            }
        });
        cardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SDCardUtils.isSDCardEnable()) {
                    Toast.makeText(AuthActivity.this, "SD卡不可用，请稍后重试", Toast.LENGTH_SHORT).show();
                    return;
                }
                authCardFile = new File(Constants.IMAGE_PATH + File.separator + FILE_NAME_CARD);
                if(!authCardFile.getParentFile().exists()) {
                    authCardFile.getParentFile().mkdirs();
                }
                if(!authCardFile.exists()) {
                    try {
                        authCardFile.createNewFile();
                    } catch (IOException e) {
                        Toast.makeText(AuthActivity.this, "SD卡不可用，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                }
                if (AppUtils.checkPermission(AuthActivity.this, "android.permission.CAMERA")) {
                    SPUtils.put(AuthActivity.this, "isCardTake", false);
                    isCardTake = false;
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(authCardFile));
                    startActivityForResult(intent, TAKE_PHOTO_CARD);
                } else {
                    Toast.makeText(AuthActivity.this, "请您打开照相机权限", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        idCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SDCardUtils.isSDCardEnable()) {
                    Toast.makeText(AuthActivity.this, "SD卡不可用，请稍后重试", Toast.LENGTH_SHORT).show();
                    return;
                }
                authIdCardFile = new File(Constants.IMAGE_PATH + File.separator + FILE_NAME_IDCARD);
                if(!authIdCardFile.getParentFile().exists()) {
                    authIdCardFile.getParentFile().mkdirs();
                }
                if(!authIdCardFile.exists()) {
                    try {
                        authIdCardFile.createNewFile();
                    } catch (IOException e) {
                        Toast.makeText(AuthActivity.this, "SD卡不可用，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                }
                if (AppUtils.checkPermission(AuthActivity.this, "android.permission.CAMERA")) {
                    isIdCardTake = false;
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(authIdCardFile));
                    startActivityForResult(intent, TAKE_PHOTO_IDCARD);
                } else {
                    Toast.makeText(AuthActivity.this, "请您打开照相机权限", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.isEmpty(etName.getText().toString())) {
                    Toast.makeText(AuthActivity.this, "请输入姓名", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (StringUtils.isEmpty(tvCity.getText().toString())) {
                    Toast.makeText(AuthActivity.this, "请选择所属城市", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (StringUtils.isEmpty(etProject.getText().toString())) {
                    Toast.makeText(AuthActivity.this, "请输入项目门店", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ivCard.getDrawable() == null) {
                    Toast.makeText(AuthActivity.this, "请拍摄名片", Toast.LENGTH_SHORT).show();
                    return;
                }
                authVerify();
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isCardTake = savedInstanceState.getBoolean("isCardTake");
        isIdCardTake = savedInstanceState.getBoolean("isIdCardTake");
        if(isCardTake) {
            authCardFile = new File(Constants.IMAGE_PATH + File.separator + FILE_NAME_CARD);
            ImageLoader.getInstance().displayImage("file://" + authCardFile.getAbsolutePath(), ivCard);
            ivCardTag.setVisibility(View.VISIBLE);
        }
        if(isIdCardTake) {
            authIdCardFile = new File(Constants.IMAGE_PATH + File.separator + FILE_NAME_IDCARD);
            ImageLoader.getInstance().displayImage("file://" + authIdCardFile.getAbsolutePath(), ivIdCard);
            ivIdCardTag.setVisibility(View.VISIBLE);
        }
    }

    private void initView() {
        etName = (EditText) findViewById(R.id.et_auth_name);
        tvCity = (TextView) findViewById(R.id.et_auth_city);
        etProject = (EditText) findViewById(R.id.et_auth_project);
        cardLayout = (RelativeLayout) findViewById(R.id.auth_card_layout);
        ivCard = (ImageView) findViewById(R.id.auth_card_photo);
        ivCardTag = (ImageView) findViewById(R.id.auth_card_tag);
        idCardLayout = (RelativeLayout) findViewById(R.id.auth_idcard_layout);
        ivIdCard = (ImageView) findViewById(R.id.auth_idcard_photo);
        ivIdCardTag = (ImageView) findViewById(R.id.auth_idcard_tag);
        btnVerify = (Button) findViewById(R.id.btn_verify);
        popupWindow = new OptionsPopupWindow(AuthActivity.this);
        left = (ImageView) findViewById(R.id.navbar_image_left);
    }

    private void getAuthCityData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getVerifyCity");
        map.put("module", Constants.MODULE_DISTRICT);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new AuthCityCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog = new ProgressDialog(AuthActivity.this);
                        dialog.setMessage("正在拉取最新城市数据。。。");
                        dialog.show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(AuthActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        list = CommonUtils.getAuthCityJsonData(AuthActivity.this);
                    }

                    @Override
                    public void onResponse(Result<List<AuthCity>> response) {
                        if(response == null) {
                            list = CommonUtils.getAuthCityJsonData(AuthActivity.this);
                            Toast.makeText(AuthActivity.this, ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            list = response.getContent();
                        } else {
                            list = CommonUtils.getAuthCityJsonData(AuthActivity.this);
                            Toast.makeText(AuthActivity.this, response.getNotice(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onAfter() {
                        super.onAfter();
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        initCityData();
                    }
                });
    }

    private abstract class AuthCityCallback extends Callback<Result<List<AuthCity>>> {
        @Override
        public Result<List<AuthCity>> parseNetworkResponse(Response response) throws Exception {
            Result<List<AuthCity>> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json, new TypeToken<Result<List<AuthCity>>>() {}.getType());
            }catch (Exception e) {
                return  null;
            }
            return result;
        }
    }

    private void initCityData() {
        for (AuthCity authCity : list) {
            city.add(authCity.getCity());
            ArrayList<String> areaList = new ArrayList<>();
            for (AuthArea authArea : authCity.getList()) {
                areaList.add(authArea.getArea());
            }
            area.add(areaList);
        }
        popupWindow.setPicker(city, area, 0, 0, true);
        popupWindow.setSelectOptions(0, 0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isCardTake", isCardTake);
        outState.putBoolean("isIdCardTake", isIdCardTake);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        OkHttpUtils.getInstance().cancelTag("auth_pic");
    }

    private void authVerify() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "userAuthor");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("realname", etName.getText().toString());
        map.put("cityid", String.valueOf(selectCityId));
        map.put("areaid", String.valueOf(selectAreaId));
        map.put("project", etProject.getText().toString());
        HashMap<String, File> mapCard = new HashMap<>();
        if(isCardTake) {
            mapCard.put("card", authCardFile);
        }
        if(isIdCardTake){
            mapCard.put("IDcard", authIdCardFile);
        }
        new FileUpload(Constants.HOST_URL, map, mapCard, new MyListener(), dialogUp).execute("");
    }

    class MyListener implements FileUpload.UploadListener {
        @Override
        public void onUploadEnd(boolean success, String object) {
            if (success) {
                try {
                    JSONObject obj = new JSONObject(object.toString());
                    String message = obj.getString("notice");
                    Toast.makeText(AuthActivity.this, message, Toast.LENGTH_SHORT).show();
                    User user = User.getInstance();
                    user.verify = User.USER_DEAL;
                    User.saveUser(user, AuthActivity.this);
                    EventBus.getDefault().post(user);
                    if (isFromIntro) {
                        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        finish();
                    }
                } catch (JSONException e) {
                    Toast.makeText(AuthActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AuthActivity.this, "上传失败请稍后重试!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        endTime = System.currentTimeMillis();
        int duration = (int) (endTime - startTime);
        MobclickAgent.onEventValue(AuthActivity.this, "authentication", null, duration);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PHOTO_CARD:
                    authCardFile = new File(Constants.IMAGE_PATH + File.separator + FILE_NAME_CARD);
                    if(authCardFile == null) {
                        Toast.makeText(AuthActivity.this,"照片处理失败，请重试！",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ImageUtils.scaleSaveBmFile(authCardFile, AuthActivity.this, 500f, 500f);
                    ImageLoader.getInstance().displayImage("file://" + authCardFile.getAbsolutePath(), ivCard);
                    ivCardTag.setVisibility(View.VISIBLE);
                    isCardTake = true;
                    break;
                case TAKE_PHOTO_IDCARD:
                    authIdCardFile = new File(Constants.IMAGE_PATH + File.separator + FILE_NAME_IDCARD);
                    if(authIdCardFile == null) {
                        Toast.makeText(AuthActivity.this,"照片处理失败，请重试！",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ImageUtils.scaleSaveBmFile(authIdCardFile, AuthActivity.this, 500f, 500f);
                    ImageLoader.getInstance().displayImage("file://" + authIdCardFile.getAbsolutePath(), ivIdCard);
                    ivIdCardTag.setVisibility(View.VISIBLE);
                    isIdCardTake = true;
                    break;
                default:
                    break;
            }
        }
    }
}
