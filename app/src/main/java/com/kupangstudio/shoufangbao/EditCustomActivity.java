package com.kupangstudio.shoufangbao;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Custom;
import com.kupangstudio.shoufangbao.model.CustomFollow;
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
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * Created by Jsmi on 2015/11/12.
 * 编辑客户界面
 */
public class EditCustomActivity extends BaseActivity {

    public static final int REQUESTCODE_CONTANTS = 1;
    private static final int REQUESTCODE_SELECTCITY = 2;

    private int sexIndex = Constants.GENDER_MAN;
    private int typeIndex = 0;
    private int layoutIndex = 0;
    private int priceIndex = 0;
    private int sizeIndex = 0;
    private int strengthIndex = 0;
    private int cityid = 1;
    private String areaName;
    private int areaid = 0;
    private String communityName;
    private int communityid = 0;

    private Custom mCustom;
    private Custom lastCustom;

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
    private TextView title;
    private ImageView back;
    private String content;
    //private BaseDialog dialog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtils.addActivity(this);
        setContentView(R.layout.activity_edit_custom);
        beginTime = System.currentTimeMillis();
        initView();
        btnContact.setVisibility(View.GONE);
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

    private void initData() {
        mCustom = (Custom) getIntent().getSerializableExtra("customdata");
        lastCustom = new Custom();
        setLastCustom(mCustom);
        if (mCustom.getCityid() == 0) {
            cityid = (int) SPUtils.get(EditCustomActivity.this, Constants.BUILD_CITY_ID, 1);
            areaid = 0;
            communityid = 0;
            areaName = "";
            cityName = (String) SPUtils.get(EditCustomActivity.this, Constants.BUILD_CITY_NAME, "北京");
            communityName = mCustom.getDistrict();
        } else {
            cityid = mCustom.getCityid();
            areaid = mCustom.getAreaid();
            communityid = mCustom.getDistrictid();
            areaName = mCustom.getArea();
            cityName = mCustom.getCity();
            communityName = mCustom.getDistrict();
        }
        sexIndex = mCustom.getGender();
        typeIndex = mCustom.getType();
        layoutIndex = mCustom.getLayout();
        priceIndex = mCustom.getPrice();
        sizeIndex = mCustom.getSize();
        strengthIndex = mCustom.getWill();

        title.setText("编辑客户");
        etName.setText(mCustom.getName());
        if (sexIndex == Constants.GENDER_MAN) {
            tvSex.setText("先生");
        } else {
            tvSex.setText("女士");
        }
        etPhone.setText(mCustom.getTel());
        tvCity.setText(mCustom.getCity());
        tvType.setText(Constants.TYPEITEMS[typeIndex]);
        tvLayout.setText(Constants.LAYOUTITEMS[layoutIndex]);
        tvPrice.setText(Constants.PRICEITEMS[priceIndex]);
        tvSize.setText(Constants.SIZEITEMS[sizeIndex]);
        tvStrength.setText(Constants.WILLITEMS[strengthIndex]);
        if (!CommonUtils.stringIsEmpty(mCustom.getRemark())) {
            etReamrk.setText(mCustom.getRemark());
        }
        tvCommunity.setText(communityName);
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

    private void initView() {
        etName = (EditText) findViewById(R.id.add_custom_name);
        btnContact = (Button) findViewById(R.id.add_custom_contact);
        etPhone = (EditText) findViewById(R.id.add_custom_phone);
        sexLayout = (LinearLayout) findViewById(R.id.add_custom_sexlayout);
        tvSex = (TextView) findViewById(R.id.add_custom_sex);
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
        title = (TextView) findViewById(R.id.add_custom_title);
        back = (ImageView) findViewById(R.id.add_custom_left);
        btnSave.setOnClickListener(mAddListener);
        btnContact.setOnClickListener(contactListener);
        sexLayout.setOnClickListener(mOptionListener);
        typeLayout.setOnClickListener(mOptionListener);
        layoutLayout.setOnClickListener(mOptionListener);
        priceLayout.setOnClickListener(mOptionListener);
        sizeLayout.setOnClickListener(mOptionListener);
        communityLayout.setOnClickListener(mOptionListener);
        cityLayout.setOnClickListener(mOptionListener);
        strengthLayout.setOnClickListener(mOptionListener);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setLastCustom(Custom mCustom) {
        lastCustom.setCid(mCustom.getCid());
        lastCustom.setUid(mCustom.getUid());
        lastCustom.setName(mCustom.getName());
        lastCustom.setGender(mCustom.getGender());
        lastCustom.setTel(mCustom.getTel());
        lastCustom.setCtime(mCustom.getCtime());
        lastCustom.setSize(mCustom.getSize());
        lastCustom.setWill(mCustom.getWill());
        lastCustom.setType(mCustom.getType());
        lastCustom.setPrice(mCustom.getPrice());
        lastCustom.setLayout(mCustom.getLayout());
        lastCustom.setCityid(mCustom.getCityid());
        lastCustom.setAreaid(mCustom.getAreaid());
        lastCustom.setDistrictid(mCustom.getDistrictid());
        lastCustom.setRemark(mCustom.getRemark());
        lastCustom.setSortLetters(mCustom.getSortLetters());
        lastCustom.setStatus(mCustom.getStatus());
        lastCustom.setFollow(mCustom.getFollow());
        lastCustom.setArea(mCustom.getArea());
        lastCustom.setCity(mCustom.getCity());
        lastCustom.setDistrict(mCustom.getDistrict());
    }

    View.OnClickListener mOptionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View view = LayoutInflater.from(EditCustomActivity.this).
                    inflate(R.layout.common_dialog_list, null);
            switch (v.getId()) {
                case R.id.add_custom_typelayout://类型
                    final AppDialog.Builder builder = new AppDialog.Builder(EditCustomActivity.this,
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
                    final AppDialog.Builder builder1 = new AppDialog.Builder(EditCustomActivity.this,
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
                    final AppDialog.Builder builder2 = new AppDialog.Builder(EditCustomActivity.this,
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
                    final AppDialog.Builder builder3 = new AppDialog.Builder(EditCustomActivity.this,
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
                    final AppDialog.Builder builder4 = new AppDialog.Builder(EditCustomActivity.this,
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
                    final AppDialog.Builder builder5 = new AppDialog.Builder(EditCustomActivity.this,
                            AppDialog.Builder.SINGLECHOICE, Constants.GENDERITEMS);
                    builder5.setContentView(view).setItemClick(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
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
                    Intent intent = new Intent(EditCustomActivity.this, SelectAllcityActivity.class);
                    //String city = (String) SPUtils.get(EditCustomActivity.this, Constants.BUILD_CITY_NAME, "北京");
                    //int cityId = (int) SPUtils.get(EditCustomActivity.this, Constants.BUILD_CITY_ID, 1);
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
                        Toast.makeText(EditCustomActivity.this, "初始化信息失败", Toast.LENGTH_SHORT).show();
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
            if ((Boolean) SPUtils.get(EditCustomActivity.this, Constants.CONTACT_TIP, true)) {
                View view = LayoutInflater.from(EditCustomActivity.this).
                        inflate(R.layout.common_dialog_custom, null);
                final AppDialog.Builder builder = new AppDialog.Builder(EditCustomActivity.this, AppDialog.Builder.COMMONDIALOG);
                builder.setContentView(view).setMessage("通过通讯录和通话记录可以快速填充姓名和手机号码")
                        .setPositiveButton("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SPUtils.put(EditCustomActivity.this, Constants.CONTACT_TIP, false);
                                Intent it = new Intent(EditCustomActivity.this, SelectContactsActivity.class);
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
                Intent it = new Intent(EditCustomActivity.this, SelectContactsActivity.class);
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
            StringBuffer sb = new StringBuffer();
            HashMap<String, String> map = CommonUtils.getUpHashMap();
            map.put("cid", String.valueOf(mCustom.getCid()));
            if (mCustom.getType() != (lastCustom.getType())) {
                map.put("type", mCustom.getType() + "");
                sb.append("类型修改为【").append(Constants.TYPEITEMS[mCustom.getType()]).append("】");
            }
            if (!mCustom.getName().equals(lastCustom.getName())) {
                map.put("name", mCustom.getName());
                sb.append("姓名修改为【").append(mCustom.getName()).append("】");
            }
            if (!mCustom.getTel().equals(lastCustom.getTel())) {
                map.put("tel", mCustom.getTel());
                sb.append("电话修改为【").append(mCustom.getTel())
                        .append("】");
            }

            if (mCustom.getSize() != (lastCustom.getSize())) {
                map.put("size", mCustom.getSize() + "");

                sb.append("面积修改为【")
                        .append(Constants.SIZEITEMS[mCustom.getSize()])
                        .append("】");
            }
            if (!mCustom.getPrice().equals(lastCustom.getPrice())) {
                map.put("price", mCustom.getPrice() + "");

                sb.append("价格修改为【")
                        .append(Constants.PRICEITEMS[mCustom.getPrice()])
                        .append("】");
            }
            if (mCustom.getGender() != (lastCustom.getGender())) {
                map.put("gender", mCustom.getGender() + "");

                sb.append("性别修改为【")
                        .append(Constants.GENDERITEMS[mCustom.getGender() - 1])
                        .append("】");
            }
            if (mCustom.getWill() != (lastCustom.getWill())) {
                map.put("will", mCustom.getWill() + "");
                sb.append("意向强度修改为【")
                        .append(Constants.WILLITEMS[mCustom.getWill()])
                        .append("】");
            }
            if (mCustom.getLayout() != (lastCustom.getLayout())) {
                map.put("layout", mCustom.getLayout() + "");

                sb.append("户型修改为【")
                        .append(Constants.LAYOUTITEMS[mCustom.getLayout()])
                        .append("】");
            }

            if (mCustom.getCity() != null && lastCustom.getCity() != null &&
                    !mCustom.getCity().equals(lastCustom.getCity())) {
                map.put("cityid", mCustom.getCityid() + "");
                sb.append("城市修改为【").append(mCustom.getCity())
                        .append("】");
            }

            if (mCustom.getArea() != null && lastCustom.getArea() != null &&
                    !mCustom.getArea().equals(lastCustom.getArea())) {
                map.put("areaid", mCustom.getAreaid() + "");
                sb.append("区域修改为【").append(mCustom.getArea())
                        .append("】");
            }

            if (mCustom.getDistrict() != null && lastCustom.getDistrict() != null &&
                    !mCustom.getDistrict().equals(lastCustom.getDistrict())) {
                map.put("areaid", mCustom.getAreaid() + "");
                map.put("districtid", mCustom.getDistrictid() + "");
                sb.append("商圈修改为【").append(mCustom.getDistrict())
                        .append("】");
            }
                /*if (!mCustom.getFollow().getCtime().equals(lastCustom.getFollow().getCtime())) {
                    map.put("follow", String.valueOf(mCustom.getFollow().getCtime()));
                }*/
            if (mCustom.getRemark() != null && lastCustom.getRemark() != null &&
                    !mCustom.getRemark().equals(lastCustom.getRemark())) {
                map.put("remark", mCustom.getRemark());
                sb.append("备注修改为【").append(mCustom.getRemark()).append("】");
            }
            if (map.size() < 1) {
                finish();
                return;
            }
            //更新数据库
            mCustom.updateAll("tel = ?", String.valueOf(mCustom.getTel()));

            User user = User.getInstance();
            content = sb.toString();
            //1代表编辑
            HashMap<String, String> map1 = new HashMap<String, String>();
            map1.put("isedit", "1");
            MobclickAgent.onEvent(EditCustomActivity.this, "addcustomclick", map1);
            if (user.userType != User.TYPE_DEFAULT_USER) {
                editCustomData(map, sb.toString());
            } else {
                Toast.makeText(EditCustomActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    };

    //编辑客户
    private void editCustomData(HashMap<String, String> cus, final String content) {
        cus.put("action", "editCustomer");
        cus.putAll(CommonUtils.getUpHashMap());
        cus.put("module", Constants.MODULE_CUSTOM);
        cus.put("uid", String.valueOf(User.getInstance().uid));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(cus)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(EditCustomActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            Toast.makeText(EditCustomActivity.this, notice, Toast.LENGTH_SHORT).show();
                            if (code > Result.RESULT_OK) {
                                mCustom.setDATEACTION(Constants.CUSTOM_UPDATA);
                                mCustom.updateAll("cid = ?", String.valueOf(mCustom.getCid()));
                                CustomFollow customFollow = new CustomFollow();
                                JSONObject object = jsonObject.getJSONObject("content");
                                customFollow.setFid(object.getInt("fid"));
                                customFollow.setContent(content);
                                customFollow.setCtime(System.currentTimeMillis() / 1000);
                                customFollow.setType(Constants.TYPE_EDIT_CUSTOM);
                                EventBus.getDefault().post(customFollow);
                                // update返回
                                Intent it = new Intent(EditCustomActivity.this, CustomDetailActivity.class);
                                it.putExtra("customdata", mCustom);
                                it.putExtra("editcontent", content);
                                setResult(Activity.RESULT_OK, it);
                                finish();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(EditCustomActivity.this, ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
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
                    jsonStr = CommonUtils.initCityJsonData(EditCustomActivity.this, cityid + ".txt");
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
                Toast.makeText(EditCustomActivity.this, "数据格式错误", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(EditCustomActivity.this, "数据格式错误", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (JSONException e) {
            Toast.makeText(EditCustomActivity.this, "数据格式错误", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(EditCustomActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(EditCustomActivity.this, "未找到文件", Toast.LENGTH_SHORT).show();
                                    return;
                                } catch (IOException e) {
                                    Toast.makeText(EditCustomActivity.this, "存储异常", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {
                                jsonStr = "";
                                Toast.makeText(EditCustomActivity.this, "初始化城市内容失败，请稍候重试", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(EditCustomActivity.this, ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
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
