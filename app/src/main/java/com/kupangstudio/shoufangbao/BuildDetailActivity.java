package com.kupangstudio.shoufangbao;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kupangstudio.shoufangbao.adapter.BuildDetailCommissionAdapter;
import com.kupangstudio.shoufangbao.adapter.BuildDetailLayoutAdapter;
import com.kupangstudio.shoufangbao.base.BaseFragmentActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.fragment.BuildParameterFragment;
import com.kupangstudio.shoufangbao.fragment.BuildSalesFragment;
import com.kupangstudio.shoufangbao.model.BuildBase;
import com.kupangstudio.shoufangbao.model.BuildCommission;
import com.kupangstudio.shoufangbao.model.BuildDetail;
import com.kupangstudio.shoufangbao.model.BuildDetailNotice;
import com.kupangstudio.shoufangbao.model.BuildImage;
import com.kupangstudio.shoufangbao.model.BuildLayout;
import com.kupangstudio.shoufangbao.model.BuildParameter;
import com.kupangstudio.shoufangbao.model.BuildProfile;
import com.kupangstudio.shoufangbao.model.NewPacket;
import com.kupangstudio.shoufangbao.model.OpenPacket;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.AES;
import com.kupangstudio.shoufangbao.utils.AmnountUtils;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.DensityUtils;
import com.kupangstudio.shoufangbao.widget.MyScrollView;
import com.kupangstudio.shoufangbao.widget.PopMenu;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.system.text.ShortMessage;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by long on 15/11/11.
 * Copyright 2015 android_xiaobai.
 * 楼盘详情
 */
public class BuildDetailActivity extends BaseFragmentActivity implements RadioGroup.OnCheckedChangeListener {

    private DisplayImageOptions optionHead;
    private ImageLoader imageLoader;
    private int bid;
    private TextView tvTitle;
    private ImageView ivCollect;
    private ImageView ivShare;
    private ImageView ivBack;
    private ViewPager viewPager;
    private ViewPagerAdapter pagerAdapter;
    private LinearLayout layoutLocation;
    private TextView tvPageNum;
    private TextView tvBuildName;//楼盘名字
    private TextView tvBuildPrice;//楼盘价格
    private TextView tvBuildAddress;//楼盘地址
    private TextView tvCommissionNum;//大于等于3套显示佣金（数目）
    private TextView tvLayoutNum;//户型图数目大于等于4套
    private RecyclerView recyclerCommission;//佣金列表
    private BuildDetailCommissionAdapter commissionAdapter;
    private Button btnVerify;
    private RecyclerView recyclerLayout;//户型图列表
    private BuildDetailLayoutAdapter layoutAdapter;
    private RadioGroup radioGroup;
    private BuildSalesFragment salesFragment;//楼盘卖点
    private BuildParameterFragment parameterFragment;//楼盘参数
    private RelativeLayout visibleLayout;
    private RelativeLayout goneLayout;
    private RadioGroup radioGroupGone;
    private RadioButton radioSales;
    private RadioButton radioSalesGone;
    private RadioButton radioParameter;
    private RadioButton radioParameterGone;
    private ProgressDialog progressDialog;
    private BuildDetail detail;
    private LinearLayout phoneReport;//电话报备
    private LinearLayout lineReport;//在线报备
    //红包相关
    private ImageView ivLineReport;//在线报备红包图片
    private RelativeLayout layoutPacket;//带看和补贴红包
    private ImageView ivPacket;
    private long packetTime;
    private boolean isCounterDown;
    private Handler packetHandler;
    private TimerCount timerCount;
    private Dialog robDialog;
    private boolean isRobbed;
    private PopMenu popMenu;
    private String buildUrl;
    private String buildProfileUrl;
    private MyScrollView myScrollView;
    private TextView tvNotice;//公告

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bid = getIntent().getIntExtra("bid", 0);
        if (savedInstanceState != null) {
            bid = savedInstanceState.getInt("bid", 0);
        }
        setContentView(R.layout.activity_build_detail);
        ShareSDK.initSDK(this);
        CommonUtils.addActivity(this);
        EventBus.getDefault().register(this);
        initView();
        init();
        ivBack.setOnClickListener(onClickListener);
        ivCollect.setOnClickListener(onClickListener);
        ivShare.setOnClickListener(onClickListener);
        phoneReport.setOnClickListener(onClickListener);
        lineReport.setOnClickListener(onClickListener);
        layoutLocation.setOnClickListener(onClickListener);
        ivPacket.setOnClickListener(onClickListener);
        radioGroup.setOnCheckedChangeListener(this);
        radioGroupGone.setOnCheckedChangeListener(this);
        salesFragment = new BuildSalesFragment();
        parameterFragment = new BuildParameterFragment();
        getData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("bid", bid);
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

    private void initView() {
        tvTitle = (TextView) findViewById(R.id.detail_title);
        ivCollect = (ImageView) findViewById(R.id.detail_collect);
        ivShare = (ImageView) findViewById(R.id.detail_share);
        ivBack = (ImageView) findViewById(R.id.detail_back);
        ivShare = (ImageView) findViewById(R.id.detail_share);
        viewPager = (ViewPager) findViewById(R.id.detail_viewpager);
        tvPageNum = (TextView) findViewById(R.id.tv_page_num);
        tvBuildName = (TextView) findViewById(R.id.tv_build_name);
        tvBuildPrice = (TextView) findViewById(R.id.tv_build_price);
        tvBuildAddress = (TextView) findViewById(R.id.tv_build_address);
        tvCommissionNum = (TextView) findViewById(R.id.tv_build_commission_num);
        recyclerCommission = (RecyclerView) findViewById(R.id.recycle_view_commission);
        recyclerLayout = (RecyclerView) findViewById(R.id.recycle_view_layout);
        btnVerify = (Button) findViewById(R.id.btn_verify);
        tvLayoutNum = (TextView) findViewById(R.id.tv_build_layout_num);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group_build_detail);
        phoneReport = (LinearLayout) findViewById(R.id.layout_phone_report);
        lineReport = (LinearLayout) findViewById(R.id.layout_line_report);
        ivLineReport = (ImageView) findViewById(R.id.iv_line_report);
        layoutPacket = (RelativeLayout) findViewById(R.id.layout_packet);
        ivPacket = (ImageView) findViewById(R.id.iv_red_packet);
        layoutLocation = (LinearLayout) findViewById(R.id.layout_build_location);
        myScrollView = (MyScrollView) findViewById(R.id.detail_scroll);
        goneLayout = (RelativeLayout) findViewById(R.id.float_layout);
        radioGroupGone = (RadioGroup) findViewById(R.id.radio_group_build_detail_float);
        visibleLayout = (RelativeLayout) findViewById(R.id.visible_layout);
        radioSales = (RadioButton) findViewById(R.id.radio_sales);
        radioSalesGone = (RadioButton) findViewById(R.id.radio_sales_float);
        radioParameter = (RadioButton) findViewById(R.id.radio_parameter);
        radioParameterGone = (RadioButton) findViewById(R.id.radio_parameter_float);
        tvNotice = (TextView) findViewById(R.id.tv_notice);
        myScrollView.setmTopView(goneLayout);
    }

    private void init() {
        imageLoader = ImageLoader.getInstance();
        optionHead = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        int[] drawable = new int[]{R.drawable.build_share_broker, R.drawable.build_share_custom};
        popMenu = new PopMenu(BuildDetailActivity.this, drawable, "build");
        popMenu.addItems(new String[]{"给同行", "给客户"});
        //分享点击事件
        popMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = User.getInstance();
                switch (i) {
                    case 0:
                        if (user.userType != User.TYPE_NORMAL_USER) {
                            CommonUtils.showRegDialog(BuildDetailActivity.this, "请您先登录", 0);
                        } else {
                            showShare();
                        }
                        break;
                    case 1:
                        if (user.userType != User.TYPE_NORMAL_USER) {
                            CommonUtils.showRegDialog(BuildDetailActivity.this, "请您先登录", 0);
                        } else {
                            showProfileShare();
                        }
                        break;
                    default:
                        break;
                }
                popMenu.dismiss();
            }
        });
    }

    //给经纪人
    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        buildUrl = detail.getBase().getBrokerUrl() + "&sfb=1" + "&uid=" + User.getInstance().uid;
        final String content = "楼盘佣金：" + detail.getBase().getShareCommission() + "；\n楼盘均价：" + detail.getBase().getPrice()
                + "；\n楼盘优惠：" + detail.getBase().getShareSale() + "。";
        final String imageUrl = detail.getBuildPic().get(0).getUrl();
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                if (platform.getName().equals(ShortMessage.NAME)) {
                    paramsToShare.setText(buildUrl + content);
                }
            }
        });

        oks.setTitle(detail.getBase().getName());
        oks.setTitleUrl(buildUrl);
        oks.setText(content);
        oks.setImageUrl(imageUrl);
        oks.setUrl(buildUrl);
        oks.setSite(getString(R.string.app_name));
        oks.setDialogMode();
        oks.setSiteUrl(buildUrl);
        oks.show(this);
        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                addIntegrate(1);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {

            }

            @Override
            public void onCancel(Platform platform, int i) {

            }
        });
    }

    //给客户
    private void showProfileShare() {
        ShareSDK.initSDK(this);
        String title = detail.getBase().getName();
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        buildProfileUrl = detail.getBase().getCustomerUrl()+ "&sfb=1" + "&uid=" + User.getInstance().uid;
        final String content = "楼盘均价：" + detail.getBase().getSharePrice() + "；\n楼盘优惠：" + detail.getBase().getShareSale() + "。";
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                if (platform.getName().equals(ShortMessage.NAME)) {
                    paramsToShare.setText(buildProfileUrl + content);
                }
                if (platform.getName().equals(Wechat.NAME)) {
                    paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
                }
            }
        });
        oks.setTitle(title);
        oks.setTitleUrl(buildProfileUrl);
        oks.setText(content);
        oks.setUrl(buildProfileUrl);
        oks.setImageUrl(detail.getBuildPic().get(0).getUrl());
        oks.setSite(getString(R.string.app_name));
        oks.setDialogMode();
        oks.setSiteUrl(buildProfileUrl);
        oks.show(this);

        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                addIntegrate(1);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {

            }

            @Override
            public void onCancel(Platform platform, int i) {

            }
        });
    }

    /**
     * 添加积分
     */
    private void addIntegrate(int type) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "addIntegrate");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("type", String.valueOf(type));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {

                    }
                });
    }

    private void getData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "buildDetail");
        map.put("module", Constants.MODULE_BUILD);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("bid", String.valueOf(bid));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new BuildDetailCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        progressDialog = new ProgressDialog(BuildDetailActivity.this);
                        progressDialog.setMessage("加载中...");
                        progressDialog.show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(BuildDetailActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        queryDataBase();
                    }

                    @Override
                    public void onResponse(Result<BuildDetail> response) {
                        if (response == null) {
                            Toast.makeText(BuildDetailActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            queryDataBase();
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            detail = response.getContent();
                            DataSupport.deleteAll(BuildDetail.class, "bid = ?", String.valueOf(bid));
                            List<BuildParameter> parameters = detail.getBuildProfile();
                            List<BuildImage> images = detail.getBuildPic();
                            List<BuildLayout> layouts = detail.getLayout();
                            List<BuildCommission> buildCommissions = detail.getCommission();
                            BuildProfile profile = detail.getSell();
                            BuildDetailNotice notice = detail.getBuildNew();
                            BuildBase buildBase = detail.getBase();
                            buildBase.save();
                            profile.save();
                            if (notice != null) {
                                notice.save();
                            }
                            DataSupport.saveAll(parameters);
                            DataSupport.saveAll(images);
                            DataSupport.saveAll(layouts);
                            DataSupport.saveAll(buildCommissions);
                            detail.save();
                            initBuildDetail(detail);
                        } else {
                            queryDataBase();
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private abstract class BuildDetailCallback extends Callback<Result<BuildDetail>> {
        @Override
        public Result<BuildDetail> parseNetworkResponse(Response response) throws Exception {
            Result<BuildDetail> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<BuildDetail>>() {
                        }.getType());
            } catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    private void queryDataBase() {
        List<BuildDetail> list = DataSupport.where("bid = ?", String.valueOf(bid)).find(BuildDetail.class);
        if (list != null && list.size() > 0) {
            detail = list.get(0);
            List<BuildImage> buildImages = DataSupport.where("builddetail_id = ?", String.valueOf(detail.getId()))
                    .find(BuildImage.class);
            detail.setBuildPic(buildImages);
            List<BuildDetailNotice> noticeList = DataSupport.where("builddetail_id = ?", String.valueOf(detail.getId()))
                    .find(BuildDetailNotice.class);
            if (noticeList.size() >= 0) {
                BuildDetailNotice notice = noticeList.get(0);
                detail.setBuildNew(notice);
            }
            List<BuildParameter> buildParameters = DataSupport.where("builddetail_id = ?", String.valueOf(detail.getId()))
                    .find(BuildParameter.class);
            detail.setBuildProfile(buildParameters);
            List<BuildProfile> profiles = DataSupport.where("builddetail_id = ?", String.valueOf(detail.getId()))
                    .find(BuildProfile.class);
            if (profiles.size() >= 0) {
                BuildProfile profile = profiles.get(0);
                detail.setSell(profile);
            }
            List<BuildLayout> buildLayouts = DataSupport.where("builddetail_id = ?", String.valueOf(detail.getId()))
                    .find(BuildLayout.class);
            detail.setLayout(buildLayouts);
            List<BuildCommission> buildCommissions = DataSupport.where("builddetail_id = ?", String.valueOf(detail.getId()))
                    .find(BuildCommission.class);
            detail.setCommission(buildCommissions);
            List<BuildBase> buildBases = DataSupport.where("builddetail_id = ?", String.valueOf(detail.getId()))
                    .find(BuildBase.class);
            if (buildBases.size() >= 0) {
                BuildBase base = buildBases.get(0);
                detail.setBase(base);
            }
            initBuildDetail(detail);
        }
    }

    private void initBuildDetail(BuildDetail detail) {
        User user = User.getInstance();
        if (detail.getCollect() == BuildDetail.COLLECT) {
            ivCollect.setImageResource(R.drawable.common_collect);
        } else {
            ivCollect.setImageResource(R.drawable.common_uncollect);
        }
        BuildBase buildBase = detail.getBase();
        tvBuildName.setText(buildBase.getName());
        tvTitle.setText(buildBase.getName());
        tvBuildPrice.setText(buildBase.getPrice());
        tvBuildAddress.setText(buildBase.getAddress());
        //初始化红包
        int packet = detail.getBase().getPacket();
        switch (packet) {
            case com.kupangstudio.shoufangbao.model.Build.NEW_PACKET:
                layoutPacket.setVisibility(View.VISIBLE);
                ivPacket.setImageResource(R.drawable.build_detail_packet_report);
                final TranslateAnimation animation = new TranslateAnimation(0, 20, 0, 0);
                animation.setDuration(100);//设置动画持续时间
                animation.setRepeatCount(8);//设置重复次数
                animation.setRepeatMode(Animation.REVERSE);
                ivPacket.startAnimation(animation);
                break;
            case com.kupangstudio.shoufangbao.model.Build.DETAIL_DAIKAN_PACKET:
                layoutPacket.setVisibility(View.VISIBLE);
                ivPacket.setImageResource(R.drawable.build_butie);
                break;
            case com.kupangstudio.shoufangbao.model.Build.REPORT_PACKET:
                layoutPacket.setVisibility(View.GONE);
                ivLineReport.setImageResource(R.drawable.build_detail_packet_report);
                break;
            default:
                layoutPacket.setVisibility(View.GONE);
                break;
        }
        //初始化banner
        List<BuildImage> images = detail.getBuildPic();
        final List<BuildImage> effectImages = new ArrayList<BuildImage>();
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getType() == BuildDetail.EFFECT) {
                effectImages.add(images.get(i));
            }
        }
        pagerAdapter = new ViewPagerAdapter(effectImages, images);
        tvPageNum.setText("1/" + effectImages.size());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tvPageNum.setText(position + 1 + "/" + effectImages.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //基础配置
        //初始化佣金相关
        List<BuildCommission> commissions = detail.getCommission();
        if (commissions.size() >= 3) {
            tvCommissionNum.setText("（" + commissions.size() + "套）");
        }
        if (user.verify == User.USER_THROUGH) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(BuildDetailActivity.this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerCommission.setLayoutManager(layoutManager);
            commissionAdapter = new BuildDetailCommissionAdapter(
                    BuildDetailActivity.this, commissions
            );
            recyclerCommission.setAdapter(commissionAdapter);
        } else {
            List<BuildCommission> buildCommissions = new ArrayList<BuildCommission>();
            for (int i = 0; i < commissions.size(); i++) {
                BuildCommission b = new BuildCommission();
                b.setName(commissions.get(i).getName());
                b.setCommission("认证可见");
                buildCommissions.add(b);
            }
            LinearLayoutManager layoutManager = new LinearLayoutManager(BuildDetailActivity.this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerCommission.setLayoutManager(layoutManager);
            commissionAdapter = new BuildDetailCommissionAdapter(
                    BuildDetailActivity.this, buildCommissions
            );
            recyclerCommission.setAdapter(commissionAdapter);
        }
        //初始化户型图
        final List<BuildLayout> layouts = detail.getLayout();
        if (layouts.size() > 3) {
            tvLayoutNum.setText("户型（" + layouts.size() + "套）");
        }
        LinearLayoutManager manager = new LinearLayoutManager(BuildDetailActivity.this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerLayout.setLayoutManager(manager);
        layoutAdapter = new BuildDetailLayoutAdapter(
                BuildDetailActivity.this, layouts
        );
        layoutAdapter.setOnItemClickListener(new BuildDetailLayoutAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(BuildDetailActivity.this, LayoutPicDetailActivity.class);
                HashMap map = new HashMap<String, String>();
                map.put("type", "linereport");
                map.put("buildname", tvTitle.getText().toString());
                MobclickAgent.onEvent(BuildDetailActivity.this, "collect", map);
                intent.putExtra("layoutList", (Serializable) layouts);
                if (position == 0) {
                    intent.putExtra("position", 0);
                } else {
                    int count = 0;
                    for (int i = 0; i < position; i++) {
                        BuildLayout layout = layouts.get(i);
                        count += layout.getPics().size();
                    }
                    intent.putExtra("position", count);
                }
                intent.putExtra("layoutPos", position);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        recyclerLayout.setAdapter(layoutAdapter);
        //初始化楼盘卖点等
        Bundle bundle = new Bundle();
        bundle.putSerializable("buildProfile", detail.getSell());
        salesFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, salesFragment).commit();
        myScrollView.setGoneHeight(visibleLayout.getTop());
        BuildDetailNotice notice = detail.getBuildNew();
        if (notice.getTitle() != null && !notice.getTitle().equals("")) {
            tvNotice.setText(notice.getTitle());
            verticalRun(tvNotice);
        }
    }

    public void verticalRun(final View view) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, DensityUtils.dp2px(BuildDetailActivity.this, 30));
        animator.setTarget(view);
        animator.setDuration(1000).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setTranslationY((Float) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setVisibility(View.GONE);
                    }
                }, 3000);
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            HashMap<String, String> map;
            switch (v.getId()) {
                case R.id.detail_back:
                    finish();
                    break;
                case R.id.detail_collect://收藏
                    map = new HashMap<String, String>();
                    map.put("type", "linereport");
                    map.put("buildname", tvTitle.getText().toString());
                    MobclickAgent.onEvent(BuildDetailActivity.this, "collect", map);
                    if (detail == null) {
                        return;
                    }
                    if (!CommonUtils.isLogin()) {
                        CommonUtils.showRegDialog(BuildDetailActivity.this, "请您先登录", 0);
                        return;
                    }
                    collectBuild(detail.getCollect());
                    break;
                case R.id.detail_share://分享
                    if (detail == null) {
                        return;
                    }
                    map = new HashMap<String, String>();
                    map.put("type", "share");
                    map.put("buildname", tvTitle.getText().toString());
                    MobclickAgent.onEvent(BuildDetailActivity.this, "builddetailclick", map);
                    popMenu.showAsDropDown(v);
                    break;
                case R.id.btn_verify://认证后可见
                    intent = new Intent(BuildDetailActivity.this, AuthActivity.class);
                    startActivity(intent);
                    break;
                case R.id.layout_phone_report://电话报备
                    if (detail == null) {
                        return;
                    }
                    if (CommonUtils.isLogin()) {
                        map = new HashMap<>();
                        map.put("type", "phonereport");
                        map.put("buildname", tvTitle.getText().toString());
                        MobclickAgent.onEvent(BuildDetailActivity.this, "builddetailclick", map);
                        if (detail.getReporttel() == null || detail.getReporttel().equals("")) {
                            CommonUtils.callPhone("4000116929", BuildDetailActivity.this);
                        } else {
                            CommonUtils.callPhone(detail.getReporttel(), BuildDetailActivity.this);
                        }
                    } else {
                        CommonUtils.showRegDialog(BuildDetailActivity.this, "请您先登录", 0);
                    }
                    break;
                case R.id.layout_line_report://在线报备
                    if (detail == null) {
                        return;
                    }
                    if (!CommonUtils.isLogin()) {
                        CommonUtils.showRegDialog(BuildDetailActivity.this, "请您先登录", 0);
                        return;
                    }
                    map = new HashMap<String, String>();
                    map.put("type", "linereport");
                    map.put("buildname", tvTitle.getText().toString());
                    MobclickAgent.onEvent(BuildDetailActivity.this, "builddetailclick", map);
                    intent = new Intent(BuildDetailActivity.this, ReportCustomActivity.class);
                    intent.putExtra("hid", bid);
                    if (detail.getBase().getPacket() == com.kupangstudio.shoufangbao.model.Build.REPORT_PACKET) {
                        intent.putExtra("isPacket", true);
                    } else {
                        intent.putExtra("isPacket", false);
                    }
                    startActivity(intent);
                    break;
                case R.id.iv_red_packet:
                    if (detail == null) {
                        return;
                    }
                    ivPacket.setEnabled(false);
                    if (detail.getBase().getPacket() == com.kupangstudio.shoufangbao.model.Build.DETAIL_DAIKAN_PACKET) {
                        if (detail.getReward().equals("")) {
                            String rules = "1.售房宝客户端标识有“贴”的楼盘，表示有带看红包活动；" +
                                    "\\n2.经纪人带真实客户到带看红包活动案场看盘，即送现金红包；" +
                                    "\\n3.售房宝案场工作人员确认带看真实性后，现场赠送带看红包；" +
                                    "\\n4.活动最终解释权归售房宝所有。";
                            CommonUtils.showCommonDialogOk(BuildDetailActivity.this, rules, "带看规则");
                        }
                        CommonUtils.showCommonDialogOk(BuildDetailActivity.this, detail.getReward(), "带看规则");
                        ivPacket.setEnabled(true);
                    } else {
                        User user = User.getInstance();
                        if (user.userType == User.TYPE_NORMAL_USER) {
                            getPacketData();
                        } else {
                            CommonUtils.showRegDialog(BuildDetailActivity.this, "请您先登录", 0);
                            ivPacket.setEnabled(true);
                        }
                    }
                    break;
                case R.id.layout_build_location:
                    intent = new Intent(BuildDetailActivity.this, BuildDetailLocationActivity.class);
                    intent.putExtra("lat", detail.getBase().getLat());
                    intent.putExtra("lng", detail.getBase().getLng());
                    intent.putExtra("position", detail.getBase().getAddress());
                    intent.putExtra("name", detail.getBase().getName());
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        Bundle bundle;
        switch (checkedId) {
            case R.id.radio_sales:
                radioSalesGone.setChecked(true);
                myScrollView.scrollTo(0, visibleLayout.getTop());
                bundle = new Bundle();
                bundle.putSerializable("buildProfile", detail.getSell());
                salesFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, salesFragment).commit();
                break;
            case R.id.radio_parameter:
                radioParameterGone.setChecked(true);
                myScrollView.scrollTo(0, visibleLayout.getTop());
                bundle = new Bundle();
                bundle.putSerializable("buildParameter", (Serializable) detail.getBuildProfile());
                parameterFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, parameterFragment).commit();
                break;
            case R.id.radio_sales_float:
                radioSales.setChecked(true);
                bundle = new Bundle();
                bundle.putSerializable("buildProfile", detail.getSell());
                salesFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, salesFragment).commit();
                break;
            case R.id.radio_parameter_float:
                radioParameter.setChecked(true);
                bundle = new Bundle();
                bundle.putSerializable("buildParameter", (Serializable) detail.getBuildProfile());
                parameterFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, parameterFragment).commit();
                break;
            default:
                break;
        }
    }

    /**
     * banner
     */
    class ViewPagerAdapter extends PagerAdapter {

        List<BuildImage> images;
        List<BuildImage> intentList;

        public ViewPagerAdapter(List<BuildImage> images, List<BuildImage> intentList) {
            this.images = images;
            this.intentList = intentList;
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = LayoutInflater.from(BuildDetailActivity.this).inflate(R.layout.header_builddetail, null);
            ImageView image = (ImageView) view.findViewById(R.id.builddetail_headimage);
            imageLoader.displayImage(images.get(position).getUrl(), image, optionHead);
            container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("type", "bannerpic");
                    map.put("buildname", tvTitle.getText().toString());
                    MobclickAgent.onEvent(BuildDetailActivity.this, "builddetailclick", map);
                    Intent intent = new Intent(BuildDetailActivity.this, BuildImageActivity.class);
                    intent.putExtra("pList", (Serializable) intentList);
                    intent.putExtra("hb", detail);
                    startActivity(intent);
                }
            });
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    /**
     * 收藏
     *
     * @param state
     */
    private void collectBuild(final int state) {
        ivCollect.setClickable(false);
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "buildCollect");
        map.put("module", Constants.MODULE_BUILD);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("bid", String.valueOf(bid));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        ivCollect.setClickable(true);
                        Toast.makeText(BuildDetailActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if (code > Result.RESULT_OK) {
                                CommonUtils.setTaskDone(BuildDetailActivity.this, 6);
                                if (state == BuildDetail.COLLECT) {
                                    ivCollect.setImageResource(R.drawable.common_uncollect);
                                    detail.setCollect(BuildDetail.UNCOLLECT);
                                    detail.updateAll("bid = ?", String.valueOf(bid));
                                    EventBus.getDefault().post(detail);
                                } else {
                                    ivCollect.setImageResource(R.drawable.common_collect);
                                    detail.setCollect(BuildDetail.COLLECT);
                                    detail.updateAll("bid = ?", String.valueOf(bid));
                                }
                            }
                            Toast.makeText(BuildDetailActivity.this, notice, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(BuildDetailActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        ivCollect.setClickable(true);
                    }
                });
    }

    /**
     * 获得红包数据
     */
    private void getPacketData() {
        User user = User.getInstance();
        String secret = CommonUtils.getMD5(getSecret());
        String key = CommonUtils.getKey(user.salt, packetTime);
        AES aes = new AES(key);
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getBuildBrowser");
        map.put("module", Constants.MODULE_PACKET);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("bid", String.valueOf(bid));
        map.put("sfb", aes.encrypt(secret));
        map.put("ctime", String.valueOf(packetTime));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new GetPacketCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        ivPacket.setEnabled(true);
                        Toast.makeText(BuildDetailActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Result<NewPacket> response) {
                        if (response == null) {
                            Toast.makeText(BuildDetailActivity.this, ResultError.MESSAGE_ERROR
                                    , Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ivPacket.setEnabled(true);
                        if (response.getCode() > Result.RESULT_OK) {
                            NewPacket newPacket = response.getContent();
                            long startTime = newPacket.getStarttime();
                            long currentTimeMillis = System.currentTimeMillis() / 1000;
                            if (startTime - currentTimeMillis > 0) {
                                timerCount = new TimerCount(startTime * 1000, 1000);
                                countDownPcketDialog(newPacket);
                            } else {
                                qiangPacket(newPacket);
                            }
                        } else {
                            Toast.makeText(BuildDetailActivity.this, response.getNotice(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private abstract class GetPacketCallback extends Callback<Result<NewPacket>> {
        @Override
        public Result<NewPacket> parseNetworkResponse(Response response) throws Exception {
            Result<NewPacket> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<NewPacket>>() {
                        }.getType());
            } catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    /**
     * 红包计时器
     */
    private void countDownPcketDialog(final NewPacket newPacket) {
        final Dialog dialog = new Dialog(BuildDetailActivity.this, R.style.Dialog_Notitle);
        View view = LayoutInflater.from(BuildDetailActivity.this).inflate(R.layout.dialog_newpacket_daojishi, null);
        final TextView tvTimer = (TextView) view.findViewById(R.id.new_packet_jishi);
        final Button btn = (Button) view.findViewById(R.id.new_packet_guang);
        ImageView caiShen = (ImageView) view.findViewById(R.id.new_packet_caishen);
        ImageView close = (ImageView) view.findViewById(R.id.new_packet_close);
        AnimationDrawable animation = (AnimationDrawable) caiShen.getDrawable();
        animation.start();
        dialog.setContentView(view);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (isCounterDown) {
                    qiangPacket(newPacket);
                } else {
                    finish();
                }
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivPacket.setEnabled(true);
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setContentView(view);
        dialog.show();
        timerCount.start();
        packetHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        String time = (String) msg.obj;
                        tvTimer.setText(time);
                        break;
                    case 1:
                        tvTimer.setText("00:00:00");
                        btn.setText("立即参与");
                        break;
                }
            }
        };

    }

    /**
     * 抢红包
     */
    private void qiangPacket(final NewPacket newPacket) {
        robDialog = new Dialog(BuildDetailActivity.this, R.style.Dialog_Notitle);
        View view = LayoutInflater.from(BuildDetailActivity.this).inflate(R.layout.dialog_newpacket_qiang, null);
        final TextView content = (TextView) view.findViewById(R.id.new_packet_qiang_content);
        final Button btn = (Button) view.findViewById(R.id.new_packet_qiang_btn);
        ImageView caiShen = (ImageView) view.findViewById(R.id.new_packet_qiang_caishen);
        ImageView close = (ImageView) view.findViewById(R.id.new_packet_qiang_close);
        TextView title = (TextView) view.findViewById(R.id.new_packet_qiang_title);
        title.setText(newPacket.getTitle());
        content.setText(newPacket.getRemark());
        AnimationDrawable animation = (AnimationDrawable) caiShen.getDrawable();
        animation.start();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRobbed) {
                    return;
                }
                openPacket(newPacket);
                isRobbed = true;
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robDialog.dismiss();
            }
        });
        robDialog.setContentView(view);
        robDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        robDialog.show();
    }

    /**
     * 打开红包
     */

    private void openPacket(final NewPacket newPacket) {
        User user = User.getInstance();
        String secret = CommonUtils.getMD5(getOpenSecret());
        String key = CommonUtils.getKey(user.salt, packetTime);
        AES aes = new AES(key);
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "openPacket");
        map.put("module", Constants.MODULE_PACKET);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("pid", String.valueOf(newPacket.getPid()));
        map.put("style", "1");
        map.put("sfb", aes.encrypt(secret));
        map.put("ctime", String.valueOf(packetTime));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new OpenPacketCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        isRobbed = false;
                        OpenPacket openPacket = new OpenPacket();
                        sharePacket(openPacket, OpenPacket.PACKET_ERROR, newPacket);
                        if (robDialog != null && robDialog.isShowing()) {
                            robDialog.dismiss();
                        }
                    }

                    @Override
                    public void onResponse(Result<OpenPacket> response) {
                        if (response == null) {
                            Toast.makeText(BuildDetailActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        isRobbed = false;
                        if (robDialog != null && robDialog.isShowing()) {
                            robDialog.dismiss();
                        }
                        OpenPacket openPacket = response.getContent();
                        if (response.getCode() > Result.RESULT_OK) {
                            sharePacket(openPacket, OpenPacket.PACKET_OK, newPacket);
                        } else {
                            if (response.getCode() == 1146) {
                                sharePacket(openPacket, OpenPacket.PACKET_OVER, newPacket);
                            }
                            if (response.getCode() == 1147) {
                                sharePacket(openPacket, OpenPacket.PACKET_ALREADY, newPacket);
                            }
                            if (response.getCode() == 1149 || response.getCode() == 1150) {
                                sharePacket(openPacket, OpenPacket.PACKET_ERROR, newPacket);
                            }
                        }
                    }
                });
    }

    private abstract class OpenPacketCallback extends Callback<Result<OpenPacket>> {
        @Override
        public Result<OpenPacket> parseNetworkResponse(Response response) throws Exception {
            String json = response.body().string();
            Result<OpenPacket> result = null;
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<OpenPacket>>() {
                        }.getType());
            } catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    /**
     * 分享红包
     */

    private void sharePacket(final OpenPacket openPacket, final int state, final NewPacket newPacket) {
        final Dialog dialog = new Dialog(BuildDetailActivity.this, R.style.Dialog_Notitle);
        View view = LayoutInflater.from(BuildDetailActivity.this).inflate(R.layout.dialog_new_packet_share, null);
        TextView title = (TextView) view.findViewById(R.id.new_packet_share_title);
        final TextView content = (TextView) view.findViewById(R.id.new_packet_share_content);
        final Button btn = (Button) view.findViewById(R.id.new_packet_share_more);
        final ImageView caiShen = (ImageView) view.findViewById(R.id.new_packet_share_caishen);
        ImageView close = (ImageView) view.findViewById(R.id.new_packet_share_close);
        ImageView wx = (ImageView) view.findViewById(R.id.new_packet_share_wx);
        ImageView friend = (ImageView) view.findViewById(R.id.new_packet_share_friend);
        ImageView qq = (ImageView) view.findViewById(R.id.new_packet_share_qq);
        ImageView message = (ImageView) view.findViewById(R.id.new_packet_share_message);
        switch (state) {
            case OpenPacket.PACKET_OK:
                try {
                    title.setText(AmnountUtils.changeF2Y(openPacket.getMoney()) + "元");
                } catch (Exception e) {
                    title.setText("****");
                }
                content.setText(openPacket.getWishing());
                btn.setText("更多楼盘");
                break;
            case OpenPacket.PACKET_OVER://活动结束
                title.setText("活动结束");
                btn.setText("关闭");
                content.setText("活动结束");
                caiShen.setImageResource(R.drawable.caishen_tear_animation);
                break;
            case OpenPacket.PACKET_ALREADY://已经领过
                title.setText("已经领过");
                btn.setText("关闭");
                content.setText("已经领过");
                caiShen.setImageResource(R.drawable.caishen_tear_animation);
                break;
            case OpenPacket.PACKET_ERROR://领取失败
                title.setText("抱歉");
                btn.setText("重新加载");
                content.setText("客官，好像有点问题\n请稍后重试");
                caiShen.setImageResource(R.drawable.caishen_tear_animation);
                break;
        }
        AnimationDrawable animation = (AnimationDrawable) caiShen.getDrawable();
        animation.start();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (state) {
                    case OpenPacket.PACKET_OK:
                        dialog.dismiss();
                        break;
                    case OpenPacket.PACKET_OVER:
                        dialog.dismiss();
                        break;
                    case OpenPacket.PACKET_ALREADY:
                        dialog.dismiss();
                        break;
                    case OpenPacket.PACKET_ERROR:
                        openPacket(newPacket);
                        break;
                }
            }
        });
        wx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                shareWx();
            }
        });
        friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                shareWxFriend();
            }
        });
        qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                shareQq();
            }
        });
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                shareMessage();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setContentView(view);
        dialog.show();
    }

    private String getSecret() {
        packetTime = System.currentTimeMillis() / 1000;
        User user = User.getInstance();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("module", Constants.MODULE_PACKET);
        map.put("model", Build.MODEL);
        map.put("action", "getBuildBrowser");
        map.put("id", user.salt);
        map.put("bid", bid + "");
        map.put("uid", user.uid + "");
        map.put("sdk", Build.VERSION.SDK_INT + "");
        map.put("os", 1 + "");
        map.put("versionCode", String.valueOf(Constants.VERSION_CODE));
        map.put("imei", Constants.IMEI);
        map.put("ctime", String.valueOf(packetTime));
        return CommonUtils.getSecret(map);
    }

    private String getOpenSecret() {
        packetTime = System.currentTimeMillis() / 1000;
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
        map.put("ctime", String.valueOf(packetTime));
        return CommonUtils.getSecret(map);
    }

    class TimerCount extends CountDownTimer {

        public TimerCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            isCounterDown = true;
            packetHandler.sendEmptyMessage(1);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long t = millisUntilFinished / 1000;
            int h = new Long(t / 3600).intValue();
            int m = new Long(t % 3600 / 60).intValue();
            int s = new Long(t % 3600 % 60).intValue();
            String str = String.format("%02d", h) + ":" + String.format("%02d", m) + ":" + String.format("%02d", s);
            Message msg = Message.obtain();
            msg.what = 0;
            msg.obj = str;
            packetHandler.sendMessage(msg);
        }
    }

    private void shareWx() {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);
        sp.setText("【售房宝】新盘上线啦！我已经抢得红包。赶快来参加吧！");
        sp.setUrl("http://m.shoufangbao.net");
        sp.setTitleUrl("http://m.shoufangbao.net");
        sp.setImageUrl("http://www.shoufangbao.net/images/logo/qr.png");
        Platform wx = ShareSDK.getPlatform(Wechat.NAME);
        wx.share(sp);
    }

    private void shareWxFriend() {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);
        sp.setTitle("【售房宝】新盘上线啦！我已经抢得红包。赶快来参加吧！");
        sp.setUrl("http://m.shoufangbao.net");
        sp.setTitleUrl("http://m.shoufangbao.net");
        sp.setImageUrl("http://www.shoufangbao.net/images/logo/qr.png");
        Platform weibo = ShareSDK.getPlatform(WechatMoments.NAME);
        weibo.share(sp);
    }

    private void shareQq() {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setText("【售房宝】新盘上线啦！我已经抢得红包。赶快来参加吧！");
        sp.setUrl("http://m.shoufangbao.net");
        sp.setTitleUrl("http://m.shoufangbao.net");
        sp.setImageUrl("http://www.shoufangbao.net/images/logo/qr.png");
        Platform weibo = ShareSDK.getPlatform(QQ.NAME);
        weibo.share(sp);
    }

    private void shareMessage() {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setText("【售房宝】新盘上线啦！我已经抢得红包。赶快来参加吧！" + "http://m.shoufangbao.net");
        sp.setImageUrl("http://www.shoufangbao.net/images/logo/qr.png");
        Platform weibo = ShareSDK.getPlatform(ShortMessage.NAME);
        weibo.share(sp);
    }

    public void onEventMainThread(User event) {
        List<BuildCommission> commissions = detail.getCommission();
        if (commissions.size() >= 3) {
            tvCommissionNum.setText("（" + commissions.size() + "套)");
        }
        if (event.verify == User.USER_THROUGH) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(BuildDetailActivity.this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerCommission.setLayoutManager(layoutManager);
            commissionAdapter = new BuildDetailCommissionAdapter(
                    BuildDetailActivity.this, commissions
            );
            recyclerCommission.setAdapter(commissionAdapter);
        } else {
            recyclerCommission.setVisibility(View.GONE);
            btnVerify.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        EventBus.getDefault().unregister(this);
        CommonUtils.removeActivity(this);
    }
}
