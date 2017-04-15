package com.kupangstudio.shoufangbao;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.convenientbanner.CBPageAdapter;
import com.bigkoo.convenientbanner.CBViewHolderCreator;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.GoodsDetail;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.AppUtils;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.viewpagerindicator.CirclePageIndicator;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.system.text.ShortMessage;
import cn.sharesdk.wechat.moments.WechatMoments;
import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Jsmi on 2015/11/24.
 * 商品详情界面
 */
public class IntegralDetailActivity extends BaseActivity implements View.OnClickListener {

    private ConvenientBanner pager;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;
    private List<String> urlList;
    private TextView title;
    private TextView right;
    private TextView goodsTitle;
    private TextView goodsFreight;
    private TextView goodsValue;
    private TextView goodsNum;
    private TextView goodsCity;
    private TextView goodsStock;
    private TextView goodsStandard;
    private TextView goodsBuy;
    private ImageView goodsShare;
    private Button goodsExchange;
    private Button goodsExchangeFinish;
    private CirclePageIndicator indicator;
    private boolean isShow = false;
    private ImageView left;
    private GoodsDetail goodsDetail;
    private boolean goodsType = true;
    private int integral;
    private long beginTime;
    private long endTime;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_integral_detail);
        CommonUtils.addActivity(this);
        beginTime = System.currentTimeMillis();
        init();
        setClickListener();
        initData();
        initPager();
    }

    private void init() {
        pager = (ConvenientBanner) findViewById(R.id.integral_scroll_pager);
        indicator = (CirclePageIndicator) findViewById(R.id.integral_indicator);
        title = (TextView) findViewById(R.id.navbar_title);
        right = (TextView) findViewById(R.id.navbar_image_right);
        left = (ImageView) findViewById(R.id.navbar_image_left);
        goodsTitle = (TextView) findViewById(R.id.integral_goods_title);
        goodsFreight = (TextView) findViewById(R.id.integral_goods_freight);
        goodsValue = (TextView) findViewById(R.id.integral_goods_value);
        goodsNum = (TextView) findViewById(R.id.integral_goods_num);
        goodsCity = (TextView) findViewById(R.id.integral_goods_city);
        goodsStock = (TextView) findViewById(R.id.integral_goods_stock);
        goodsStandard = (TextView) findViewById(R.id.integral_goods_standard);
        goodsBuy = (TextView) findViewById(R.id.integral_goods_buy);
        goodsShare = (ImageView) findViewById(R.id.integral_goods_share);
        goodsExchange = (Button) findViewById(R.id.integral_goods_exchange);
        goodsExchangeFinish = (Button) findViewById(R.id.integral_goods_exchange_finish);
    }

    private void setClickListener() {
        right.setOnClickListener(this);
        goodsShare.setOnClickListener(this);
        goodsExchange.setOnClickListener(this);
        left.setOnClickListener(this);
    }

    private void initData() {
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        Intent intent = getIntent();
        goodsDetail = (GoodsDetail) intent.getSerializableExtra("goodsdetail");
        integral = User.getInstance().interage;
        title.setText("商品详情");
        if (goodsDetail != null) {
            isShow = true;
            urlList = goodsDetail.getPic();
            goodsTitle.setText(goodsDetail.getTitle());
            goodsCity.setText(goodsDetail.getCity());
            goodsBuy.setText(goodsDetail.getIntegrate() + "积分");
            goodsFreight.setText(goodsDetail.getFreight() + "积分");
            goodsStandard.setText(goodsDetail.getFormat());
            goodsNum.setText("已兑换" + goodsDetail.getChange());
            goodsType = goodsDetail.getType() == 2;
            String stock = String.valueOf(goodsDetail.getStock());
            this.goodsStock.setText(stock);
            if (stock.equals("0")) {
                goodsExchange.setVisibility(View.GONE);
                goodsExchangeFinish.setVisibility(View.VISIBLE);
            } else {
                goodsExchange.setVisibility(View.VISIBLE);
                goodsExchangeFinish.setVisibility(View.GONE);
            }
            goodsValue.setText("¥" + goodsDetail.getWorth() + "元");
            goodsValue.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            urlList = new ArrayList<String>();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.navbar_image_right://兑换规则
                Intent intent = new Intent(IntegralDetailActivity.this, CommonRuleActivity.class);
                intent.putExtra("title", "积分规则");
                intent.putExtra("url", Constants.INTEGRAL_RULE);
                startActivity(intent);
                break;
            case R.id.integral_goods_exchange://立即兑换
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("type", "detailclick");
                MobclickAgent.onEvent(IntegralDetailActivity.this, "goodsclick", map);
                User user = User.getInstance();
                if (user.verify == User.USER_THROUGH) {
                    if (user.cityId <= 0) {
                        showSignDailog();
                    } else {
                        if(User.getInstance().style != User.NORMAL_BROKER) {
                            Toast.makeText(IntegralDetailActivity.this, "亲，成功带看两次就可晋升为标准经纪人参加活动啦！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (user.cityId == (int)SPUtils.get(IntegralDetailActivity.this, Constants.BUILD_CITY_ID, 1)) {
                            if (integral < goodsDetail.getIntegrate()) {
                                Toast.makeText(IntegralDetailActivity.this, "您的积分不足！", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (goodsDetail.getStock() == 0) {
                                Toast.makeText(IntegralDetailActivity.this, "没有库存", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (goodsType) {
                                //实物兑换
                                intent = new Intent(IntegralDetailActivity.this, GoodsChangeActivity.class);
                                intent.putExtra("goodsdetail", goodsDetail);
                                intent.putExtra("integral", integral);
                                startActivity(intent);
                                IntegralDetailActivity.this.finish();
                            } else {
                                //虚拟兑换
                                getExchangVirtualData(goodsDetail.getWid());
                            }
                        } else {
                            Toast.makeText(IntegralDetailActivity.this, "亲，当前您只能兑换认证城市的商品", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (user.verify == User.USER_DEAL) {
                        Toast.makeText(IntegralDetailActivity.this, "认证通过才能兑换，请耐心等待！", Toast.LENGTH_SHORT).show();
                    } else {
                        if (user.userType != User.TYPE_NORMAL_USER) {
                            AppUtils.showRegAuthDialog(IntegralDetailActivity.this, "亲，仅支持活动区域注册认证后的标准经纪人兑换", 0);
                        } else {
                            AppUtils.showRegAuthDialog(IntegralDetailActivity.this, "亲，仅支持活动区域认证后的标准经纪人兑换", 1);
                        }
                    }
                }
                break;
            case R.id.integral_goods_share://分享商品信息
                HashMap<String, String> map1 = new HashMap<String, String>();
                map1.put("type", "shareclick");
                MobclickAgent.onEvent(IntegralDetailActivity.this, "goodsclick", map1);
                showShare("商品名称：" + goodsDetail.getTitle(), "亲，售房宝积分兑换商品正在火热进行中，抓紧时间赚取积分兑换礼包吧！",
                        "http://www.shoufangbao.net/images/logo/qr.png",
                        "http://m.shoufangbao.net");
                break;
            case R.id.navbar_image_left:
                finish();
                break;
            default:
                break;
        }
    }

    //商品信息分享
    private void showShare(String shareTitle, String shareContent, String imgUrl,
                           final String shareUrl) {
        ShareSDK.initSDK(IntegralDetailActivity.this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        final String content = shareContent;
        final String title = shareTitle;
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                if (platform.getName().equals(ShortMessage.NAME)) {
                    paramsToShare.setText(shareUrl + content);
                }
                if (platform.getName().equals(WechatMoments.NAME)) {
                    paramsToShare.setTitle(content);
                }
            }
        });
        oks.setTitle(title);
        oks.setTitleUrl(shareUrl);
        oks.setText(content);
        oks.setImageUrl(imgUrl);
        oks.setUrl(shareUrl);
        oks.setSite(getString(R.string.app_name));
        oks.setDialogMode();
        oks.setSiteUrl(shareUrl);
        oks.show(IntegralDetailActivity.this);
    }


    private void initPager() {
        pager.setPages(new CBViewHolderCreator<ImageHolderView>() {
            @Override
            public ImageHolderView createHolder() {
                return new ImageHolderView();
            }
        }, urlList).setPageIndicator(new int[]{R.drawable.banner_unselected, R.drawable.banner_selected});
    }

    private class ImageHolderView implements CBPageAdapter.Holder<String> {
        private ImageView imageView;

        @Override
        public View createView(Context context) {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return imageView;
        }

        @Override
        public void UpdateUI(Context context, final int position, String data) {
            imageView.setImageResource(R.drawable.empty_building_list);
            imageLoader.displayImage(data, imageView);
        }
    }

    //兑换虚拟商品
    private void getExchangVirtualData(int wid) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "exchangeDummy");
        map.put("module", Constants.MODULE_MALL);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        map.put("wid", String.valueOf(wid));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog = new ProgressDialog(IntegralDetailActivity.this);
                        CommonUtils.progressDialogShow(dialog, "正在兑换中");
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(IntegralDetailActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if(code > Result.RESULT_OK) {
                                JSONObject content = jsonObject.getJSONObject("content");
                                String tip = content.getString("tip");
                                EventBus.getDefault().post(goodsDetail);
                                goodsStock.setText(String.valueOf(goodsDetail.getStock() - 1));
                                EventBus.getDefault().post(User.getInstance());
                                View dialogView = LayoutInflater.from(IntegralDetailActivity.this).inflate(R.layout.common_dialog_onebtn, null);
                                final AppDialog.Builder builder = new AppDialog.Builder(IntegralDetailActivity.this, AppDialog.Builder.COMMONDIALOG);
                                builder.setContentView(dialogView).
                                        setTitle(tip).
                                        setPositiveButton("知道了", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                builder.dismiss();
                                            }
                                        }).create();
                            }
                            Toast.makeText(IntegralDetailActivity.this, notice, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(IntegralDetailActivity.this, ResultError.MESSAGE_ERROR
                            ,Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    @Override
                    public void onAfter() {
                        super.onAfter();
                        CommonUtils.progressDialogDismiss(dialog);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        pager.startTurning(5000);
        if (User.getInstance().userType == User.TYPE_NORMAL_USER) {
            integral = User.getInstance().interage;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        pager.stopTurning();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        endTime = System.currentTimeMillis();
        int duration = (int) ((endTime - beginTime) / 1000);
        Map<String, String> map_value = new HashMap<String, String>();
        MobclickAgent.onEventValue(this, "goodsdetail", map_value, duration);
    }

    private void showSignDailog() {
        View view = LayoutInflater.from(IntegralDetailActivity.this).inflate(R.layout.common_dialog_onebtn, null);
        final AppDialog.Builder builder = new AppDialog.Builder(IntegralDetailActivity.this, AppDialog.Builder.COMMONDIALOG);
        builder.setContentView(view)
                .setTitle("亲，当前只支持活动区域经纪人兑换。")
                .setPositiveButton("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.dismiss();
                    }
                }).create();
    }
}
