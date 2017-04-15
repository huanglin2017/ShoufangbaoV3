package com.kupangstudio.shoufangbao;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Custom;
import com.kupangstudio.shoufangbao.model.FollowList;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.FileUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.pickerview.OptionsPopupWindow;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Response;

public class AddCustomActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout floatAnimLayout;//浮层
    private ImageView ivAnim;
    private LinearLayout animLayout;//整体布局动画
    private ImageView ivHideAnim;
    private ImageView ivBack;
    public static final int REQUESTCODE_CONTANTS = 1;
    private static final int REQUESTCODE_SELECTCITY = 2;

    private int typeIndex = 0;
    private int layoutIndex = 0;
    private int priceIndex = 0;
    private int sizeIndex = 0;
    private int strengthIndex = 0;
    private int sexIndex = 1;//默认男
    private int cityid = 1;
    private String areaName;
    private int areaid = 0;
    private String communityName;
    private int communityid = 0;

    private Custom mCustom;

    private EditText etName;//姓名
    private EditText etPhone;//手机号
    private EditText etReamrk;//备注
    private Button btnContact;//通讯录
    private Button btnSave;//保存
    private LinearLayout sexLayout;//性别
    private TextView tvSex;
    private LinearLayout typeLayout;//类型
    private TextView tvType;
    private LinearLayout layoutLayout;//户型
    private TextView tvLayout;
    private LinearLayout priceLayout;//价格
    private TextView tvPrice;
    private LinearLayout sizeLayout;//面积
    private TextView tvSize;
    private LinearLayout communityLayout;//商圈
    private TextView tvCommunity;
    private LinearLayout strengthLayout;//强度
    private TextView tvStrength;
    private long beginTime;
    private long endTime;
    private OptionsPopupWindow pwOptions;
    private ArrayList<String> mCitiesDatas;
    private ArrayList<String> mCitiesIdDatas;
    private ArrayList<ArrayList<String>> mAreasDatas;
    private ArrayList<ArrayList<String>> mAreasIdDatas;
    private String jsonStr;
    private int mPositionCity;
    private int mPositionArea;
    private LinearLayout cityLayout;
    private TextView tvCity;
    private String cityName;
    private ImageView ivMultiAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_custom);
        CommonUtils.addActivity(this);
        beginTime = System.currentTimeMillis();
        initView();
        initData();
        //监听确定选择按钮
        pwOptions.setOnoptionsSelectListener(new OptionsPopupWindow.OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                //返回的分别是三个级别的选中位置
                mPositionCity = options1;
                mPositionArea = option2;
                if (options1 == 0) {
                    if (mCitiesDatas.get(options1) == null) {
                        areaName = "";
                    } else {
                        areaName = mCitiesDatas.get(options1);
                    }
                    try {
                        communityName = mAreasDatas.get(options1).get(option2);
                    } catch (Exception e) {
                        communityName = "";
                    }
                    areaid = 0;
                    communityid = 0;
                } else {
                    if (mCitiesDatas.get(options1) == null) {
                        areaName = "";
                        areaid = 0;
                    } else {
                        areaName = mCitiesDatas.get(options1);
                        areaid = Integer.parseInt(mCitiesIdDatas.get(options1));
                    }
                    if (mAreasDatas.get(options1).get(option2) == null) {
                        communityName = "";
                        communityid = 0;
                    } else {
                        communityName = mAreasDatas.get(options1).get(option2);
                        communityid = Integer.parseInt(mAreasIdDatas.get(options1).get(option2));
                    }
                }
                String tx = areaName + communityName;
                tvCommunity.setText(tx);
            }
        });
        ivAnim.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        ivMultiAdd.setOnClickListener(this);
        ivHideAnim.setOnClickListener(this);
        btnSave.setOnClickListener(mAddListener);
        btnContact.setOnClickListener(contactListener);
        typeLayout.setOnClickListener(mOptionListener);
        layoutLayout.setOnClickListener(mOptionListener);
        priceLayout.setOnClickListener(mOptionListener);
        sizeLayout.setOnClickListener(mOptionListener);
        sexLayout.setOnClickListener(mOptionListener);
        communityLayout.setOnClickListener(mOptionListener);
        cityLayout.setOnClickListener(mOptionListener);
        strengthLayout.setOnClickListener(mOptionListener);
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
    protected void onStop() {
        super.onStop();

        endTime = System.currentTimeMillis();
        int duration = (int) ((endTime - beginTime) / 1000);
        Map<String, String> map_value = new HashMap<String, String>();
        MobclickAgent.onEventValue(this, "addcustome", map_value, duration);

    }

    private void initView() {
        ivBack = (ImageView) findViewById(R.id.iv_back);
        animLayout = (LinearLayout) findViewById(R.id.layout_anim);
        floatAnimLayout = (LinearLayout) findViewById(R.id.float_anim_layout);
        ivAnim = (ImageView) findViewById(R.id.iv_anim);
        ivHideAnim = (ImageView) findViewById(R.id.iv_hide_anim);
        etName = (EditText) findViewById(R.id.add_custom_name);
        btnContact = (Button) findViewById(R.id.add_custom_contact);
        etPhone = (EditText) findViewById(R.id.add_custom_phone);
        typeLayout = (LinearLayout) findViewById(R.id.add_custom_typelayout);
        tvType = (TextView) findViewById(R.id.add_custom_type);
        layoutLayout = (LinearLayout) findViewById(R.id.add_custom_layoutlayout);
        tvLayout = (TextView) findViewById(R.id.add_custom_layout);
        priceLayout = (LinearLayout) findViewById(R.id.add_custom_pricelayout);
        tvPrice = (TextView) findViewById(R.id.add_custom_price);
        sizeLayout = (LinearLayout) findViewById(R.id.add_custom_sizelayout);
        tvSize = (TextView) findViewById(R.id.add_custom_size);
        communityLayout = (LinearLayout) findViewById(R.id.add_custom_communitylayout);
        tvCommunity = (TextView) findViewById(R.id.add_custom_community);
        cityLayout = (LinearLayout) findViewById(R.id.add_custom_citylayout);
        tvCity = (TextView) findViewById(R.id.add_custom_city);
        strengthLayout = (LinearLayout) findViewById(R.id.add_custom_strengthlayout);
        tvStrength = (TextView) findViewById(R.id.add_custom_strength);
        etReamrk = (EditText) findViewById(R.id.add_custom_remark);
        btnSave = (Button) findViewById(R.id.add_custom_save);
        ivMultiAdd = (ImageView) findViewById(R.id.iv_multi_add);
        sexLayout = (LinearLayout) findViewById(R.id.add_custom_sexlayout);
        tvSex = (TextView) findViewById(R.id.add_custom_sex);
    }

    private void initData() {
        tvType.setText(Constants.TYPEITEMS[typeIndex]);
        tvLayout.setText(Constants.LAYOUTITEMS[layoutIndex]);
        tvPrice.setText(Constants.PRICEITEMS[priceIndex]);
        tvSize.setText(Constants.SIZEITEMS[sizeIndex]);
        tvStrength.setText(Constants.WILLITEMS[strengthIndex]);
        tvSex.setText("先生");
        tvCity.setText((String) SPUtils.get(AddCustomActivity.this, Constants.BUILD_CITY_NAME, "北京"));
        mCustom = new Custom();
        cityid = (int) SPUtils.get(AddCustomActivity.this, Constants.BUILD_CITY_ID, 1);
        cityName = (String) SPUtils.get(AddCustomActivity.this, Constants.BUILD_CITY_NAME, "北京");
        //选项选择器
        pwOptions = new OptionsPopupWindow(this);
        //初始化数据源
        mCitiesDatas = new ArrayList<String>();
        mCitiesIdDatas = new ArrayList<String>();
        mAreasDatas = new ArrayList<ArrayList<String>>();
        mAreasIdDatas = new ArrayList<ArrayList<String>>();
        jsonStr = initJsonData(this, cityid + ".txt");
        if (jsonStr == null) {
            File file = new File(Constants.CACHE_PATH + File.separator + "area", cityid + ".txt");
            if (!file.exists()) {
                getSelectAreaData(cityid);
            } else {
                jsonStr = FileUtils.readFileContentStr(file, this, cityid + ".txt");
            }
        }
        if (jsonStr != null && !jsonStr.equals("")) {
            initNetDistrictDatas();
            pwOptions.setPicker(mCitiesDatas, mAreasDatas, 0, 0, true);
            pwOptions.setSelectOptions(0, 0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_anim:
                openLayout();
                break;
            case R.id.iv_hide_anim:
                hideLayout();
                break;
            case R.id.iv_multi_add:
                Intent intent = new Intent(AddCustomActivity.this, MultiAddActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    View.OnClickListener mOptionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View view = LayoutInflater.from(AddCustomActivity.this).
                    inflate(R.layout.common_dialog_list, null);
            switch (v.getId()) {
                case R.id.add_custom_typelayout://类型
                    final AppDialog.Builder builder = new AppDialog.Builder(AddCustomActivity.this,
                            AppDialog.Builder.SINGLECHOICE, Constants.TYPEITEMS);
                    builder.setContentView(view).setItemClick(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            typeIndex = position;
                            tvType.setText(Constants.TYPEITEMS[typeIndex]);
                            builder.dismiss();
                        }
                    }).create();
                    break;
                case R.id.add_custom_layoutlayout://户型
                    final AppDialog.Builder builder1 = new AppDialog.Builder(AddCustomActivity.this,
                            AppDialog.Builder.SINGLECHOICE, Constants.LAYOUTITEMS);
                    builder1.setContentView(view).setItemClick(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            layoutIndex = position;
                            tvLayout.setText(Constants.LAYOUTITEMS[layoutIndex]);
                            builder1.dismiss();
                        }
                    }).create();
                    break;
                case R.id.add_custom_pricelayout://价格
                    final AppDialog.Builder builder2 = new AppDialog.Builder(AddCustomActivity.this,
                            AppDialog.Builder.SINGLECHOICE, Constants.PRICEITEMS);
                    builder2.setContentView(view).setItemClick(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            priceIndex = position;
                            tvPrice.setText(Constants.PRICEITEMS[priceIndex]);
                            builder2.dismiss();
                        }
                    }).create();
                    break;
                case R.id.add_custom_sizelayout://面积
                    final AppDialog.Builder builder3 = new AppDialog.Builder(AddCustomActivity.this,
                            AppDialog.Builder.SINGLECHOICE, Constants.SIZEITEMS);
                    builder3.setContentView(view).setItemClick(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            sizeIndex = position;
                            tvSize.setText(Constants.SIZEITEMS[sizeIndex]);
                            builder3.dismiss();
                        }
                    }).create();
                    break;
                case R.id.add_custom_strengthlayout://强度
                    final AppDialog.Builder builder4 = new AppDialog.Builder(AddCustomActivity.this,
                            AppDialog.Builder.SINGLECHOICE, Constants.WILLITEMS);
                    builder4.setContentView(view).setItemClick(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            strengthIndex = position;
                            tvStrength.setText(Constants.WILLITEMS[strengthIndex]);
                            builder4.dismiss();
                        }
                    }).create();
                    break;
                case R.id.add_custom_sexlayout://性别
                    final AppDialog.Builder builder5 = new AppDialog.Builder(AddCustomActivity.this,
                            AppDialog.Builder.SINGLECHOICE, Constants.GENDERITEMS);
                    builder5.setContentView(view).setItemClick(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if(position == 0) {
                                sexIndex = Custom.GENDER_MAN;
                            } else {
                                sexIndex = Custom.GENDER_WOMEN;
                            }
                            tvSex.setText(Constants.GENDERITEMS[position]);
                            builder5.dismiss();
                        }
                    }).create();
                    break;
                case R.id.add_custom_citylayout://城市
                    Intent intent = new Intent(AddCustomActivity.this, SelectAllcityActivity.class);
                    //String city = (String) SPUtils.get(AddOrEditCustomActivity.this, Constants.BUILD_CITY_NAME, "北京");
                    //int cityId = (int) SPUtils.get(AddOrEditCustomActivity.this, Constants.BUILD_CITY_ID, 1);
                    intent.putExtra("city", cityName);
                    intent.putExtra("cityid", cityid);
                    startActivityForResult(intent, REQUESTCODE_SELECTCITY);
                    break;
                case R.id.add_custom_communitylayout://商圈
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //强制隐藏键盘
                    if (jsonStr != null && !jsonStr.equals("")) {
                        pwOptions.setPicker(mCitiesDatas, mAreasDatas, mPositionCity, mPositionArea, true);
                        pwOptions.showAtLocation(communityLayout, Gravity.BOTTOM, 0, 0);
                    } else {
                        Toast.makeText(AddCustomActivity.this, "初始化信息失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private View.OnClickListener contactListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if ((Boolean) SPUtils.get(AddCustomActivity.this, Constants.CONTACT_TIP, true)) {
                View view = LayoutInflater.from(AddCustomActivity.this).
                        inflate(R.layout.common_dialog_custom, null);
                final AppDialog.Builder builder = new AppDialog.Builder(AddCustomActivity.this, AppDialog.Builder.COMMONDIALOG);
                builder.setContentView(view).setMessage("通过通讯录和通话记录可以快速填充姓名和手机号码")
                        .setPositiveButton("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SPUtils.put(AddCustomActivity.this, Constants.CONTACT_TIP, false);
                                Intent it = new Intent(AddCustomActivity.this, SelectContactsActivity.class);
                                startActivityForResult(it, REQUESTCODE_CONTANTS);
                                builder.dismiss();
                            }
                        })
                        .setNegativeButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                builder.dismiss();
                            }
                        }).create();
            } else {
                Intent it = new Intent(AddCustomActivity.this, SelectContactsActivity.class);
                startActivityForResult(it, REQUESTCODE_CONTANTS);
            }
        }
    };

    private View.OnClickListener mAddListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            String name = etName.getEditableText().toString();
            String phone = etPhone.getEditableText().toString();
            String remark = etReamrk.getEditableText().toString();
            if (CommonUtils.stringIsEmpty(name)) {
                Toast.makeText(getBaseContext(), "姓名不能为空", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            if (CommonUtils.stringIsEmpty(phone)) {
                Toast.makeText(getBaseContext(), "电话不能为空", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            if (CommonUtils.stringIsEmpty(remark)) {
                remark = "";
            }
            final User u = User.getInstance();
            List<Custom> tList = DataSupport.where("uid = ? and tel = ?", String.valueOf(u.uid), phone).find(Custom.class);
            if (tList != null && tList.size() > 0) {
                Toast.makeText(
                        getBaseContext(),
                        "号码 " + phone + " 在用户 " + tList.get(0).getName()
                                + " 使用， 电话号码不能重复", Toast.LENGTH_SHORT).show();
                return;
            }
            mCustom.setName(name);
            mCustom.setTel(phone);
            mCustom.setRemark(remark);
            mCustom.setAreaid(areaid);
            mCustom.setArea(areaName);
            mCustom.setDistrictid(communityid);
            mCustom.setCityid(cityid);
            mCustom.setCity(cityName);
            mCustom.setUid(u.uid);
            mCustom.setDistrict(communityName);
            mCustom.setGender(sexIndex);
            mCustom.setLayout(layoutIndex);
            mCustom.setSize(sizeIndex);
            mCustom.setPrice(priceIndex);
            mCustom.setType(typeIndex);
            mCustom.setWill(strengthIndex);
            mCustom.setDistrict(communityName);
            mCustom.setArea(areaName);
            mCustom.setStatus(0);
            // 设置数据，存储数据库
            mCustom.save();
            //0代表添加
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("isedit", "0");
            MobclickAgent.onEvent(AddCustomActivity.this, "addcustomclick", map);

            // uid为-1时也同步
            User user = User.getInstance();
            if (user.userType != User.TYPE_DEFAULT_USER) {
                getAddCustomData(mCustom);
            } else {
                FollowList follow = new FollowList();
                follow.setCtime(System.currentTimeMillis() / 1000);
                follow.save();
                mCustom.setFollow(follow);
                mCustom.setCtime(System.currentTimeMillis() / 1000);
                mCustom.DATEACTION = Constants.CUSTOM_ADD;
                mCustom.save();
                EventBus.getDefault().post(mCustom);
                Toast.makeText(AddCustomActivity.this, "本地添加成功", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

        }
    };

    private void hideLayout() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.hide_add_custom);
        animLayout.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animLayout.setVisibility(View.GONE);
                floatAnimLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void openLayout() {
        floatAnimLayout.setVisibility(View.GONE);
        animLayout.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.open_add_custom);
        animLayout.startAnimation(animation);
    }

    //添加客户
    private void getAddCustomData(Custom custom) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "addCustomer");
        map.put("module", Constants.MODULE_CUSTOM);
        map.put("uid", String.valueOf(custom.getUid()));
        map.put("tel", custom.getTel());
        map.put("name", custom.getName());
        map.put("gender", String.valueOf(custom.getGender()));
        map.put("size", String.valueOf(custom.getSize()));
        map.put("price", String.valueOf(custom.getPrice()));
        map.put("cityid", String.valueOf(custom.getCityid()));
        map.put("areaid", String.valueOf(custom.getAreaid()));
        map.put("districtid", String.valueOf(custom.getDistrictid()));
        map.put("remark", custom.getRemark());
        map.put("type", String.valueOf(custom.getType()));
        map.put("layout", String.valueOf(custom.getLayout()));
        map.put("will", String.valueOf(custom.getWill()));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new AddCustomCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(AddCustomActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Result<Custom> response) {
                        if (response == null) {
                            Toast.makeText(AddCustomActivity.this, ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(AddCustomActivity.this, response.getNotice(), Toast.LENGTH_SHORT).show();
                        if (response.getCode() > Result.RESULT_OK) {
                            mCustom.setCid(response.getContent().getCid());
                            mCustom.setDATEACTION(Constants.CUSTOM_ADD);
                            FollowList follow = new FollowList();
                            follow.setCtime(0L);
                            mCustom.setFollow(follow);
                            mCustom.setCtime(System.currentTimeMillis() / 1000);
                            mCustom.save();
                            CommonUtils.setTaskDone(AddCustomActivity.this, 20);
                            CommonUtils.setTaskDone(AddCustomActivity.this, 250);
                            EventBus.getDefault().post(mCustom);
                            finish();
                        }
                    }
                });
    }

    private abstract class AddCustomCallback extends Callback<Result<Custom>> {
        @Override
        public Result<Custom> parseNetworkResponse(Response response) throws Exception {
            String json = response.body().string();
            Result<Custom> result = null;
            try {
                result = new Gson().fromJson(json, new TypeToken<Result<Custom>>() {
                }.getType());
            } catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_CONTANTS) {
                Bundle b = data.getExtras(); // data为B中回传的Intent
                String name = b.getString("name");
                String num = b.getString("num");
                if (!CommonUtils.stringIsEmpty(name)) {
                    etName.setText(name);
                }
                if (!CommonUtils.stringIsEmpty(num)) {
                    if (num.startsWith("+86")) {
                        num = num.substring(3);
                    }
                    num = num.replace(" ", "");
                    num = num.replace(" ", "");
                    num = num.replace("-", "");
                    num = num.replace("(", "");
                    num = num.replace(")", "");
                    num = num.replace("_", "");
                    etPhone.setText(num);
                }
            } else if (requestCode == REQUESTCODE_SELECTCITY) {
                cityid = data.getIntExtra("cityid", 1);
                cityName = data.getStringExtra("city");
                if (data.getStringExtra("jsonStr") != null) {
                    jsonStr = data.getStringExtra("jsonStr");
                    initNetDistrictDatas();
                } else {
                    jsonStr = CommonUtils.initCityJsonData(AddCustomActivity.this, cityid + ".txt");
                    initNetDistrictDatas();
                }
                tvCommunity.setText("");
                areaName = "";
                areaid = 0;
                communityName = "";
                communityid = 0;
                tvCity.setText(cityName);
            }
        }

    }

    //取出数据
    public static String initJsonData(Context ctx, String name) {
        String str = null;
        try {
            StringBuffer sb = new StringBuffer();
            InputStream is = ctx.getAssets().open("area" + File.separator + name);
            int len = -1;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                sb.append(new String(buf, 0, len));
            }
            is.close();
            str = sb.toString();
        } catch (IOException e) {
            return str;
        }
        return str;
    }

    //解析服务器回传的商圈信息
    private void initNetDistrictDatas() {
        mPositionArea = 0;
        mPositionCity = 0;
        mAreasDatas.clear();
        mCitiesDatas.clear();
        mCitiesIdDatas.clear();
        mAreasIdDatas.clear();
        try {
            JSONObject jsonP = new JSONObject(jsonStr);
            JSONArray jsonC = null;
            try {
                jsonC = jsonP.getJSONArray("list");
            } catch (Exception e1) {
                Toast.makeText(AddCustomActivity.this, "数据格式错误", Toast.LENGTH_SHORT).show();
            }
            if (jsonC.length() == 0) {
                mCitiesDatas.add("");
                mCitiesIdDatas.add(0 + "");
                ArrayList<String> mAreasDatas0 = new ArrayList<String>();
                ArrayList<String> mAreasIdDatas0 = new ArrayList<String>();
                mAreasDatas0.add("");
                mAreasIdDatas0.add(0 + "");
                mAreasDatas.add(mAreasDatas0);
                mAreasIdDatas.add(mAreasIdDatas0);
            }
            for (int j = 0; j < jsonC.length(); j++) {
                JSONObject jsonCity = jsonC.getJSONObject(j);
                String city = jsonCity.getString("name");// 市名字
                int cid = jsonCity.getInt("areaId");// 市id
                mCitiesDatas.add(city);
                mCitiesIdDatas.add(cid + "");
                JSONArray jsonAreas = null;
                ArrayList<String> mAreasDatas1 = new ArrayList<String>();
                ArrayList<String> mAreasIdDatas1 = new ArrayList<String>();
                try {
                    if (jsonCity.isNull("list")) {
                        mAreasDatas1.add("");
                        mAreasIdDatas1.add(cid + "");
                        mAreasDatas.add(mAreasDatas1);
                        mAreasIdDatas.add(mAreasIdDatas1);
                    } else {
                        jsonAreas = jsonCity.getJSONArray("list");
                        for (int k = 0; k < jsonAreas.length(); k++) {
                            JSONObject jsonArea = jsonAreas.getJSONObject(k);
                            String area = jsonArea.getString("name");
                            int aid = jsonArea.getInt("districtId");
                            mAreasIdDatas1.add(aid + "");
                            mAreasDatas1.add(area);
                        }
                        mAreasIdDatas.add(mAreasIdDatas1);
                        mAreasDatas.add(mAreasDatas1);
                    }
                } catch (Exception e2) {
                    Toast.makeText(AddCustomActivity.this, "数据格式错误", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (JSONException e) {
            Toast.makeText(AddCustomActivity.this, "数据格式错误", Toast.LENGTH_SHORT).show();
        }
    }


    private void getSelectAreaData(final int cityid) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getOneCity");
        map.put("module", Constants.MODULE_DISTRICT);
        map.put("cityid", String.valueOf(cityid));
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(AddCustomActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            jsonStr = jsonObject.getJSONObject("content").toString();
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                File file = new File(Constants.CACHE_PATH + File.separator + "area", cityid + ".txt");
                                if (!file.getParentFile().exists()) {
                                    file.getParentFile().mkdirs();
                                }
                                if (!file.exists()) {
                                    try {
                                        file.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    FileOutputStream fos = new FileOutputStream(file);
                                    fos.write(jsonStr.getBytes());
                                    fos.flush();
                                    fos.close();
                                } catch (FileNotFoundException e) {
                                    Toast.makeText(AddCustomActivity.this, "未找到文件", Toast.LENGTH_SHORT).show();
                                    return;
                                } catch (IOException e) {
                                    Toast.makeText(AddCustomActivity.this, "存储异常", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {
                                jsonStr = "";
                                Toast.makeText(AddCustomActivity.this, "初始化城市内容失败，请稍候重试", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(AddCustomActivity.this, ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
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
