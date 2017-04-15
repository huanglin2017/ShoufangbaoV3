package com.kupangstudio.shoufangbao;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Custom;
import com.kupangstudio.shoufangbao.model.FollowList;
import com.kupangstudio.shoufangbao.model.NewPacket;
import com.kupangstudio.shoufangbao.model.OpenPacket;
import com.kupangstudio.shoufangbao.model.Report;
import com.kupangstudio.shoufangbao.model.ReportResult;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.AES;
import com.kupangstudio.shoufangbao.utils.AmnountUtils;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.utils.StringUtils;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.system.text.ShortMessage;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class ReportCustomActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    public static final int REQUESTCODE_CONTANTS = 1;
    public static final int REQUESTCODE_CUSTOM = 2;
    private Button btnReportCustom;//客户
    private Button btnReportContact;//通讯录
    private EditText etName;//名字
    private RadioButton radioMan;//男
    private RadioButton radioWoman;//女
    private EditText etPhone;//手机号
    private EditText etMark;//备注
    private ImageView ivBack;//返回
    private Button reportBtn;//报备
    private LinearLayout layoutReportType;//意向类型
    private TextView tvReportType;
    private LinearLayout layoutReportLayout;//意向户型
    private TextView tvReportLayout;
    private LinearLayout layoutReportPrice;//意向价格
    private TextView tvReportPrice;
    private int hid;
    private int cid;
    private int selectType = 0;
    private int selectLayout = 0;
    private int selectPrice = 0;
    private int sex;
    private CheckBox yincangTel;
    private TextView tvYinCang;
    private CheckBox saveCk;
    private TextView saveTxt;
    private String phoneNumber = null;
    private Custom mCustom;
    private String name;
    private String phone;
    private String mark;
    private Button reportBtn1;
    private boolean isChoose;
    private long time;
    //private ReportPacket reportPacket;
    private long starttime;
    private long endtime;
    private ProgressDialog pb;
    private EditText etPhone1;
    private String phoneThree;
    private boolean isPacket;
    private String shareUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_custom);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "报备客户");
        starttime = System.currentTimeMillis();
        hid = getIntent().getIntExtra("hid", 0);
        isPacket = getIntent().getBooleanExtra("isPacket", false);
        shareUrl = "http://m.shoufangbao.net";
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("bid", hid + "");
        MobclickAgent.onEvent(this, "report", map);
        initView();
        setClickListener();
        saveTxt.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
        saveCk.setChecked(true);
        yincangTel.setOnCheckedChangeListener(this);
        saveCk.setOnCheckedChangeListener(this);
        tvReportType.setText(Constants.TYPEITEMS[0]);
        tvReportLayout.setText(Constants.LAYOUTITEMS[0]);
        tvReportPrice.setText(Constants.PRICEITEMS[0]);
        radioMan.setChecked(true);
        etPhone.setLongClickable(false);
        sex = Custom.GENDER_MAN;
        etPhone.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
        changePhoneNumberNew1(etPhone);
        changePhoneNumberNew2(etPhone1);
        User user = User.getInstance();
        if (isPacket) {
            if (user.verify != User.USER_THROUGH) {
                final Dialog dialog = new Dialog(ReportCustomActivity.this, R.style.Dialog_Notitle);
                View view = LayoutInflater.from(ReportCustomActivity.this).inflate(R.layout.item_report_packet_dialog, null);
                view.findViewById(R.id.common_positive_btn).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (User.getInstance().verify == User.USER_DEAL) {
                            Toast.makeText(ReportCustomActivity.this, "您的认证我们已经收到，请耐心等待", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent it = new Intent(ReportCustomActivity.this, AuthActivity.class);
                        startActivity(it);
                    }
                });
                view.findViewById(R.id.common_negative_btn).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.setContentView(view);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                dialog.show();
            }
        }
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
        endtime = System.currentTimeMillis();
        int duration = (int) (endtime - starttime); //开发者需要自己计算音乐播放时长
        Map<String, String> map_value = new HashMap<String, String>();
        map_value.put("bid", String.valueOf(hid));
        MobclickAgent.onEventValue(this, "reportdetail", map_value, duration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
        if (pb != null && pb.isShowing()) {
            pb.dismiss();
        }
    }

    private void setClickListener() {
        ivBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        layoutReportType.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items = Constants.TYPEITEMS;
                View view = LayoutInflater.from(ReportCustomActivity.this).inflate(R.layout.common_dialog_list, null);
                final AppDialog.Builder builder = new AppDialog.Builder(ReportCustomActivity.this, AppDialog.Builder.SINGLECHOICE, items);
                builder.setContentView(view);
                builder.setItemClick(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectType = position;
                        tvReportType.setText(Constants.TYPEITEMS[position]);
                        builder.dismiss();
                    }
                });
                builder.create();
            }
        });
        layoutReportLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items = new String[]{"不限", "一居室", "两居室", "三居室", "四居室", "五居室及以上"};
                View view = LayoutInflater.from(ReportCustomActivity.this).inflate(R.layout.common_dialog_list, null);
                final AppDialog.Builder builder = new AppDialog.Builder(ReportCustomActivity.this, AppDialog.Builder.SINGLECHOICE, items);
                builder.setContentView(view);
                builder.setItemClick(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectLayout = position;
                        tvReportLayout.setText(Constants.LAYOUTITEMS[position]);
                        builder.dismiss();
                    }
                });
                builder.create();
            }
        });
        layoutReportPrice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items = Constants.PRICEITEMS;
                View view = LayoutInflater.from(ReportCustomActivity.this).inflate(R.layout.common_dialog_list, null);
                final AppDialog.Builder builder = new AppDialog.Builder(ReportCustomActivity.this, AppDialog.Builder.SINGLECHOICE, items);
                builder.setContentView(view);
                builder.setItemClick(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectPrice = position;
                        tvReportPrice.setText(Constants.PRICEITEMS[position]);
                        builder.dismiss();
                    }
                });
                builder.create();
            }
        });
        btnReportCustom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("type", "custom");
                MobclickAgent.onEvent(ReportCustomActivity.this, "reportdetailclick", map);
                Intent it = new Intent(ReportCustomActivity.this, ReportSelectCustomActivity.class);
                startActivityForResult(it, REQUESTCODE_CUSTOM);
            }
        });
        btnReportContact.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("type", "contact");
                MobclickAgent.onEvent(ReportCustomActivity.this, "reportdetailclick", map);
                Intent it = new Intent(ReportCustomActivity.this,
                        SelectContactsActivity.class);
                startActivityForResult(it, REQUESTCODE_CONTANTS);
            }
        });
        reportBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                name = etName.getText().toString();
                phone = etPhone.getText().toString() + "****" + etPhone1.getText().toString();
                if (radioMan.isChecked()) {
                    sex = Custom.GENDER_MAN;
                } else {
                    sex = Custom.GENDER_WOMEN;
                }
                if (StringUtils.isEmpty(name)) {
                    Toast.makeText(ReportCustomActivity.this, "姓名不能为空",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (phone.equals("****")) {
                    Toast.makeText(ReportCustomActivity.this, "请输入手机号",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!CommonUtils.isTopMobileNO(phone.substring(0, 3) + phone.substring(7, phone.length()))) {
                    Toast.makeText(ReportCustomActivity.this, "手机号格式错误",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (StringUtils.isEmpty(etMark.getText().toString())) {
                    mark = "";
                } else {
                    mark = etMark.getText().toString();
                }

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("type", "submit");
                MobclickAgent.onEvent(ReportCustomActivity.this, "reportdetailclick", map);
                reportBuild(name, phone, String.valueOf(hid), String.valueOf(cid), mark);
            }
        });
        tvYinCang.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (yincangTel.isChecked()) {
                    yincangTel.setChecked(false);
                } else {
                    yincangTel.setChecked(true);
                }
            }
        });
    }

    private void initView() {
        ivBack = (ImageView) findViewById(R.id.navbar_image_left);
        etName = (EditText) findViewById(R.id.reportcustom_etname);
        btnReportCustom = (Button) findViewById(R.id.reportcustom_btn);
        btnReportContact = (Button) findViewById(R.id.reportcontact_btn);
        radioMan = (RadioButton) findViewById(R.id.reportcustom_man);
        radioWoman = (RadioButton) findViewById(R.id.reportcustom_woman);
        etPhone = (EditText) findViewById(R.id.reportcustom_phonenum);
        etPhone1 = (EditText) findViewById(R.id.reportcustom_phonenum_hou);
        layoutReportType = (LinearLayout) findViewById(R.id.reportcustom_type);
        tvReportType = (TextView) findViewById(R.id.reportcustom_type_tv);
        layoutReportLayout = (LinearLayout) findViewById(R.id.reportcustom_layout);
        tvReportLayout = (TextView) findViewById(R.id.reportcustom_layout_tv);
        layoutReportPrice = (LinearLayout) findViewById(R.id.reportcustom_price);
        tvReportPrice = (TextView) findViewById(R.id.reportcustom_price_tv);
        etMark = (EditText) findViewById(R.id.reportcustom_remark);
        reportBtn = (Button) findViewById(R.id.reportcustom_report);
        reportBtn1 = (Button) findViewById(R.id.reportcustom_report_1);
        yincangTel = (CheckBox) findViewById(R.id.reportcustom_yincang_tel);
        saveCk = (CheckBox) findViewById(R.id.reportcustom_save_ck);
        saveTxt = (TextView) findViewById(R.id.reportcustom_save_txt);
        tvYinCang = (TextView) findViewById(R.id.reportcustom_yincang_tv);
        saveTxt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(ReportCustomActivity.this, ReportSafeActivity.class);
                String url = (String) SPUtils.get(ReportCustomActivity.this, Constants.REPORT_SAFE_URL, "https://www.shoufangbao.com/index.php?r=appweb/report");
                it.putExtra("url", url);
                it.putExtra("title", "报备安全协议");
                startActivity(it);
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.reportcustom_save_ck:
                if (!b) {
                    reportBtn.setVisibility(View.GONE);
                    reportBtn1.setVisibility(View.VISIBLE);
                } else {
                    reportBtn.setVisibility(View.VISIBLE);
                    reportBtn1.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
    }

    private void reportBuild(final String name, final String tel, String bid, String cid, final String remark) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        final User user = User.getInstance();
        map.put("action", "addReport");
        map.put("module", Constants.MODULE_CUSTOM);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("tel", tel);
        map.put("name", name);
        map.put("price", String.valueOf(selectPrice));
        map.put("layout", String.valueOf(selectLayout));
        map.put("bid", bid);
        map.put("remark", remark);
        map.put("cid", cid);
        map.put("style", String.valueOf(Report.REPORT_BUILD));
        map.put("type", String.valueOf(selectType));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new ReportBuildCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        pb = new ProgressDialog(ReportCustomActivity.this);
                        pb.setMessage("报备中，请稍后...");
                        pb.show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ReportCustomActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        if (pb != null && pb.isShowing()) {
                            pb.dismiss();
                        }
                    }

                    @Override
                    public void onResponse(Result<ReportResult> response) {
                        if (response == null) {
                            Toast.makeText(ReportCustomActivity.this, ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(ReportCustomActivity.this, response.getNotice(), Toast.LENGTH_SHORT).show();
                        if (pb != null && pb.isShowing()) {
                            pb.dismiss();
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            int ext = (int) SPUtils.get(ReportCustomActivity.this, Constants.TASKPARAMETER, 250);
                            if (hid == ext) {
                                CommonUtils.setTaskDone(ReportCustomActivity.this, 5);
                            }
                            int cid = response.getContent().getCid();
                            if (cid > 0) {
                                List<Custom> list = DataSupport.where("cid = ?", String.valueOf(cid)).find(Custom.class);
                                if (list.size() > 0) {
                                    mCustom = new Custom();
                                    mCustom.setCid(cid);
                                    mCustom.setStatus(Report.REPORT);
                                    mCustom.setIsend(0);
                                    mCustom.setDATEACTION(Custom.ACTION_UPDATE);
                                } else {
                                    User user = User.getInstance();
                                    mCustom = new Custom();
                                    FollowList followList = new FollowList();
                                    followList.setCid(cid);
                                    followList.setCtime(System.currentTimeMillis());
                                    mCustom.setFollow(followList);
                                    mCustom.setUid(user.uid);
                                    mCustom.setSize(0);
                                    mCustom.setWill(0);
                                    mCustom.setType(selectType);
                                    mCustom.setPrice(selectPrice);
                                    mCustom.setLayout(selectLayout);
                                    mCustom.setGender(sex);
                                    mCustom.setCtime(System.currentTimeMillis());
                                    mCustom.setTel(etPhone.getText().toString() + "****" + etPhone1.getText().toString());
                                    mCustom.setName(name);
                                    mCustom.setRemark(remark);
                                    mCustom.setCityid((Integer) SPUtils.get(ReportCustomActivity.this, Constants.BUILD_CITY_ID, 1));
                                    mCustom.setCity((String) SPUtils.get(ReportCustomActivity.this, Constants.BUILD_CITY_NAME, "北京"));
                                    mCustom.setAreaid(0);
                                    mCustom.setArea("");
                                    mCustom.setDistrictid(0);
                                    mCustom.setDistrict("");
                                    mCustom.setStatus(Report.REPORT);
                                    mCustom.setIsend(0);
                                    mCustom.setDATEACTION(Custom.ACTION_ADD);
                                    mCustom.setCid(cid);
                                }
                                EventBus.getDefault().post(mCustom);
                            }
                            if (user.verify == User.USER_THROUGH && user.cityId > 0 && isPacket) {
                                getReportPacket();
                            } else {
                                finish();
                            }
                        }
                    }
                });
    }

    private abstract class ReportBuildCallback extends Callback<Result<ReportResult>>{
        @Override
        public Result<ReportResult> parseNetworkResponse(Response response) throws Exception {
            Result<ReportResult> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<ReportResult>>(){}.getType());
            }catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            isChoose = true;
            if (requestCode == REQUESTCODE_CONTANTS) {
                Bundle b = data.getExtras(); // data为B中回传的Intent
                String name = b.getString("name");
                String num = b.getString("num");
                cid = 0;
                if (num.startsWith("+86")) {
                    num = num.substring(3, num.length());
                }
                num = num.replace(" ", "");
                num = num.replace(" ", "");
                num = num.replace("-", "");
                num = num.replace("(", "");
                num = num.replace(")", "");
                num = num.replace("_", "");
                phoneNumber = num;
                etName.setText(name);
                etName.setSelection(name.length());
                if (CommonUtils.isMobileNO(phoneNumber)) {
                    etPhone.setText(phoneNumber.substring(0, 3));
                    etPhone1.setText(phoneNumber.substring(7, 11));
                } else {
                    etPhone.setText("");
                    etPhone1.setText("");
                }
            } else {
                Bundle b = data.getExtras(); // data为B中回传的Intent
                String name = b.getString("name");
                String num = b.getString("num");
                if (num.startsWith("+86")) {
                    num = num.substring(3, num.length());
                }
                int id = b.getInt("cid");
                if (b.getInt("sex") == Custom.GENDER_MAN) {
                    radioMan.setChecked(true);
                    sex = Custom.GENDER_MAN;
                } else {
                    radioWoman.setChecked(true);
                    sex = Custom.GENDER_WOMEN;
                }
                cid = id;
                num = num.replace(" ", "");
                num = num.replace(" ", "");
                num = num.replace("-", "");
                num = num.replace("(", "");
                num = num.replace(")", "");
                num = num.replace("_", "");
                phoneNumber = num;
                etName.setText(name);
                etName.setSelection(name.length());
                if (CommonUtils.isMobileNO(phoneNumber) || CommonUtils.isTopMobileNO(phoneNumber.substring(0, 3) + phoneNumber.substring(7, phoneNumber.length()))) {
                    etPhone.setText(phoneNumber.substring(0, 3));
                    etPhone1.setText(phoneNumber.substring(7, 11));
                } else {
                    etPhone.setText("");
                    etPhone1.setText("");
                }
            }
        }
    }

    /**
     * 请求报备红包
     */

    private void getReportPacket() {
        final ProgressDialog dialog = new ProgressDialog(ReportCustomActivity.this);
        dialog.setMessage("一个红包正在向你跑来。。。");
        User user = User.getInstance();
        String secret = CommonUtils.getMD5(getSecret());
        String key = CommonUtils.getKey(user.salt, time);
        AES aes = new AES(key);
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getBuildReport");
        map.put("module", Constants.MODULE_PACKET);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("bid", String.valueOf(hid));
        map.put("sfb", aes.encrypt(secret));
        map.put("ctime", String.valueOf(time));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new GetPacketCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog.show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ReportCustomActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onResponse(Result<NewPacket> response) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Toast.makeText(ReportCustomActivity.this, response.getNotice(), Toast.LENGTH_SHORT).show();
                        if (response.getCode() > Result.RESULT_OK) {
                            reportPacketDialog(response.getContent());
                        } else {
                            finish();
                        }
                    }
                });
    }

    private abstract class GetPacketCallback extends Callback<Result<NewPacket>>{
        @Override
        public Result<NewPacket> parseNetworkResponse(Response response) throws Exception {
            Result<NewPacket> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<NewPacket>>(){}.getType());
            }catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    /**
     * 红包dialog
     */

    private void reportPacketDialog(final NewPacket newPacket) {
        final Dialog dialog = new Dialog(ReportCustomActivity.this, R.style.Dialog_Notitle);
        View view = LayoutInflater.from(ReportCustomActivity.this).inflate(R.layout.dialog_report_packet_rob, null);
        TextView title = (TextView) view.findViewById(R.id.report_packet_title);
        TextView content = (TextView) view.findViewById(R.id.report_packet_content);
        ImageView btn = (ImageView) view.findViewById(R.id.report_packet_btn);
        title.setText(newPacket.getTitle());
        content.setText(newPacket.getRemark());
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                openPacket(newPacket.getPid());
            }
        });
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
    }

    /**
     * 打开红包
     */
    private void openPacket(final int pid) {
        final ProgressDialog openDialog = new ProgressDialog(ReportCustomActivity.this);
        openDialog.setMessage("正在打开，请稍后...");
        User user = User.getInstance();
        String secret = CommonUtils.getMD5(getOpenSecret());
        String key = CommonUtils.getKey(user.salt, time);
        AES aes = new AES(key);
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "openPacket");
        map.put("module", Constants.MODULE_PACKET);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("pid", String.valueOf(pid));
        map.put("sfb", aes.encrypt(secret));
        map.put("ctime", String.valueOf(time));
        map.put("style", "2");
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new OpenPacketCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        openDialog.show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (openDialog != null && openDialog.isShowing()) {
                            openDialog.dismiss();
                        }
                        showPacketError("哦～网络开小差了", "您的手机网络出状况了，请点击按钮重试", pid);
                    }

                    @Override
                    public void onResponse(Result<OpenPacket> response) {
                        if (response == null) {
                            Toast.makeText(ReportCustomActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (openDialog != null && openDialog.isShowing()) {
                            openDialog.dismiss();
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            showPacketSuccess(response.getContent());
                        } else {
                            switch (response.getCode()) {
                                case 1148://个人红包最大额度
                                    showPacketMax();
                                    break;
                                case 1155://红包总量
                                    showPacketCountDone();
                                    break;
                                case 1156://活动结束
                                    showPacketTimeDone();
                                    break;
                                default:
                                    Toast.makeText(ReportCustomActivity.this, response.getNotice(), Toast.LENGTH_SHORT).show();
                                    showPacketError("哦～网络开小差了", "您的手机网络出状况了，请点击按钮重试", pid);
                                    break;
                            }
                        }
                    }
                });
    }

    private abstract class OpenPacketCallback extends Callback<Result<OpenPacket>>{
        @Override
        public Result<OpenPacket> parseNetworkResponse(Response response) throws Exception {
            Result<OpenPacket> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<OpenPacket>>(){}.getType());
            }catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    /**
     * 打开成功
     *
     * @param openPacket
     */
    private void showPacketSuccess(OpenPacket openPacket) {
        final Dialog dialog = new Dialog(ReportCustomActivity.this, R.style.Dialog_Notitle);
        View view = LayoutInflater.from(ReportCustomActivity.this).inflate(R.layout.dialog_report_packet_success, null);
        TextView tvTitle = (TextView) view.findViewById(R.id.success_title);
        TextView content = (TextView) view.findViewById(R.id.success_content);
        TextView money = (TextView) view.findViewById(R.id.success_money);
        ImageView wx = (ImageView) view.findViewById(R.id.success_weixin);
        ImageView friend = (ImageView) view.findViewById(R.id.success_friend);
        ImageView qq = (ImageView) view.findViewById(R.id.success_qq);
        ImageView message = (ImageView) view.findViewById(R.id.success_message);
        final ImageView anim = (ImageView) view.findViewById(R.id.success_anim);
        tvTitle.setText(openPacket.getTitle());
        content.setText(openPacket.getWishing());
        try {
            money.setText(AmnountUtils.changeF2Y(openPacket.getMoney()));
        } catch (Exception e) {
            money.setText("**");
        }
        anim.setImageResource(R.drawable.report_anim_gift);
        final AnimationDrawable drawable = (AnimationDrawable) anim.getDrawable();
        int duration = 0;
        for (int i = 0; i < drawable.getNumberOfFrames(); i++) {
            duration += drawable.getDuration(i);
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                anim.setVisibility(View.GONE);
            }
        }, duration);
        drawable.start();
        wx.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shareWx();
            }
        });
        friend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shareWxFriend();
            }
        });
        qq.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shareQq();
            }
        });
        message.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shareMessage();
            }
        });
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
    }

    /**
     * 红包失败
     *
     * @param
     */
    private void showPacketError(String title, String message, final int pid) {
        final Dialog dialog = new Dialog(ReportCustomActivity.this, R.style.Dialog_Notitle);
        View view = LayoutInflater.from(ReportCustomActivity.this).inflate(R.layout.dialog_report_packet_rob, null);
        TextView tvTitle = (TextView) view.findViewById(R.id.report_packet_title);
        TextView content = (TextView) view.findViewById(R.id.report_packet_content);
        ImageView btn = (ImageView) view.findViewById(R.id.report_packet_btn);
        tvTitle.setText(title);
        content.setText(message);
        btn.setImageResource(R.drawable.report_hongbao_btnretry);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                openPacket(pid);
            }
        });
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.show();
    }


    private String getOpenSecret() {
        time = System.currentTimeMillis() / 1000;
        User user = User.getInstance();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("module", Constants.MODULE_PACKET);
        map.put("model", Build.MODEL);
        map.put("action", "openPacket");
        map.put("id", user.salt);
        map.put("uid", user.uid + "");
        map.put("sdk", Build.VERSION.SDK_INT + "");
        map.put("os", 1 + "");
        map.put("versionCode", String.valueOf(Constants.VERSION_CODE));
        map.put("imei", Constants.IMEI);
        map.put("ctime", String.valueOf(time));
        return CommonUtils.getSecret(map);
    }


    //个人红包达到最大额度 1148
    private void showPacketMax() {
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(ReportCustomActivity.this)
                .inflate(R.layout.report_hongbao_max, null);
        final Dialog dialog = new AlertDialog.Builder(ReportCustomActivity.this).create();
        dialog.show();
        dialog.getWindow().setContentView(layout);
        layout.findViewById(R.id.hongbao_max_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    //红包时间结束
    private void showPacketTimeDone() {
        RelativeLayout donelayout = (RelativeLayout) LayoutInflater.from(ReportCustomActivity.this)
                .inflate(R.layout.report_hongbao_timedone, null);
        final Dialog dialog = new AlertDialog.Builder(ReportCustomActivity.this).create();
        dialog.show();
        dialog.getWindow().setContentView(donelayout);
        donelayout.findViewById(R.id.hongbao_time_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    //红包总量完成
    private void showPacketCountDone() {
        RelativeLayout donelayout = (RelativeLayout) LayoutInflater.from(ReportCustomActivity.this)
                .inflate(R.layout.report_hongbao_countdone, null);
        final Dialog dialog = new AlertDialog.Builder(ReportCustomActivity.this).create();
        dialog.show();
        dialog.getWindow().setContentView(donelayout);
        donelayout.findViewById(R.id.hongbao_done_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * 加密
     *
     * @return
     */
    private String getSecret() {
        time = System.currentTimeMillis() / 1000;
        User user = User.getInstance();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("module", Constants.MODULE_PACKET);
        map.put("action", "getBuildReport");
        map.put("id", user.salt);
        map.put("bid", hid + "");
        map.put("uid", user.uid + "");
        map.put("model", Build.MODEL);
        map.put("sdk", Build.VERSION.SDK_INT + "");
        map.put("os", 1 + "");
        map.put("versionCode", String.valueOf(Constants.VERSION_CODE));
        map.put("imei", Constants.IMEI);
        map.put("ctime", String.valueOf(time));
        return CommonUtils.getSecret(map);
    }

    /*隐藏手机号*/
    private void changePhoneNumberNew1(final EditText editText) {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String s = editText.getText().toString();
                if (s.length() == 3) {
                    etPhone1.requestFocus();
                    etPhone1.setSelection(0);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                phoneThree = etPhone.getText().toString();
            }
        };
        etPhone.addTextChangedListener(textWatcher);
    }

    private void changePhoneNumberNew2(final EditText editText) {
        TextWatcher textWatcher = new TextWatcher() {

            private String s;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                s = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String s1 = editText.getText().toString();
                if (s.length() > s1.length() && s1.length() == 0) {
                    etPhone.requestFocus();
                    etPhone.setSelection(phoneThree.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        etPhone1.addTextChangedListener(textWatcher);
    }

    private void shareWx() {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);
        sp.setText("【售房宝】小伙伴们！售房宝推荐楼盘中报备客户得红包。赶快来参加吧！");
        sp.setUrl(shareUrl);
        sp.setTitleUrl(shareUrl);
        sp.setImageUrl("http://www.shoufangbao.net/images/logo/qr.png");
        Platform wx = ShareSDK.getPlatform(Wechat.NAME);
        wx.share(sp);
    }

    private void shareWxFriend() {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);
        sp.setTitle("【售房宝】小伙伴们！售房宝推荐楼盘中报备客户得红包。赶快来参加吧！");
        sp.setUrl(shareUrl);
        sp.setTitleUrl(shareUrl);
        sp.setImageUrl("http://www.shoufangbao.net/images/logo/qr.png");
        Platform weibo = ShareSDK.getPlatform(WechatMoments.NAME);
        weibo.share(sp);
    }

    private void shareQq() {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setText("【售房宝】小伙伴们！售房宝推荐楼盘中报备客户得红包。赶快来参加吧！");
        sp.setUrl(shareUrl);
        sp.setTitleUrl(shareUrl);
        sp.setImageUrl("http://www.shoufangbao.net/images/logo/qr.png");
        Platform weibo = ShareSDK.getPlatform(QQ.NAME);
        weibo.share(sp);
    }

    private void shareMessage() {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setText("【售房宝】小伙伴们！售房宝推荐楼盘中报备客户得红包。赶快来参加吧！" + shareUrl);
        sp.setImageUrl("http://www.shoufangbao.net/images/logo/qr.png");
        Platform weibo = ShareSDK.getPlatform(ShortMessage.NAME);
        weibo.share(sp);
    }

}
