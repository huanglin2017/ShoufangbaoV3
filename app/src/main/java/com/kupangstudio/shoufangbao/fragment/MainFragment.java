package com.kupangstudio.shoufangbao.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.bigkoo.convenientbanner.CBPageAdapter;
import com.bigkoo.convenientbanner.CBViewHolderCreator;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.kupangstudio.shoufangbao.AuthActivity;
import com.kupangstudio.shoufangbao.BuildDetailActivity;
import com.kupangstudio.shoufangbao.BuildSearchActivity;
import com.kupangstudio.shoufangbao.BuildSelectActivity;
import com.kupangstudio.shoufangbao.CooperBrandActivity;
import com.kupangstudio.shoufangbao.HelpCenterActivity;
import com.kupangstudio.shoufangbao.LoginActivity;
import com.kupangstudio.shoufangbao.MainActivity;
import com.kupangstudio.shoufangbao.MyInviteActivity;
import com.kupangstudio.shoufangbao.MyReportActivity;
import com.kupangstudio.shoufangbao.PersonOrderActivity;
import com.kupangstudio.shoufangbao.PointMallActivity;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.SelectHomeCityActivity;
import com.kupangstudio.shoufangbao.ShareBannerActivity;
import com.kupangstudio.shoufangbao.SuggestActivity;
import com.kupangstudio.shoufangbao.ToolsActivity;
import com.kupangstudio.shoufangbao.base.BaseFragment;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Banner;
import com.kupangstudio.shoufangbao.model.Build;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.AppUtils;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.widget.PullToRefreshListView;
import com.kupangstudio.shoufangbao.widget.shimmer.Shimmer;
import com.kupangstudio.shoufangbao.widget.shimmer.ShimmerTextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.litepal.crud.DataSupport;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by long on 15/11/9.
 * Copyright 2015 android_xiaobai.
 * 主页
 */
public class MainFragment extends BaseFragment {

    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private ConvenientBanner banner;
    private List<Banner> bannerList;
    private ArrayList<Build> buildList = new ArrayList<Build>();
    private ShimmerTextView shimmerTv;
    private ImageView shimmerImage;
    private Shimmer shimmer;
    private ListView lv;
    private LinearLayout signMoney;// 积分商城
    private LinearLayout myReport;//我的报备
    private LinearLayout calTool;//计算工具
    private LinearLayout cooperBrand;//私人定制
    private ImageView ivSignMoney;
    private ImageView ivMyReport;
    private ImageView ivCalTool;
    private ImageView ivCooperBrand;
    private int cityId;
    private String city;
    private int currentCityId;
    private BuildAdapter adapter;
    private boolean isBannerDone;
    private boolean isBuildDone;
    private View fragmentView;
    private DisplayImageOptions tabOneOption;//首页4个按钮
    private DisplayImageOptions tabTwoOption;//首页4个按钮
    private DisplayImageOptions tabThreeOption;//首页4个按钮
    private DisplayImageOptions tabFourOption;//首页4个按钮
    private StringBuffer bid;
    private RelativeLayout loading;
    private View headerView;
    /**
     * 标志位，标志已经初始化完成
     */
    private boolean isPrepared;
    /**
     * 是否已被加载过一次，第二次就不再去请求数据了
     */
    private boolean mHasLoadedOnce;
    private boolean isShow;
    private boolean isRefresh;
    private TextView searchTv;
    private LinearLayout tabLl;
    private Button btnArea;
    private Button btnType;
    private Button btnPrice;
    private Button btnMore;
    private int page = 1;
    private LocationClient mLocationClient;
    private MyLocationListener myListener;
    private LinearLayout header;
    private ImageView backTop;
    private PullToRefreshListView pullToRefreshListView;
    private int hFlag = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                if (hFlag != 1) {
                    hFlag = 2;
                    tabLl.setVisibility(View.VISIBLE);
                }
            } else {
                if (hFlag != 2) {
                    hFlag = 1;
                    tabLl.setVisibility(View.GONE);
                }
            }
        }
    };
    private LinearLayout selectCity;
    private View footerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        tabOneOption = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.header_main_sign_money)
                .showImageForEmptyUri(R.drawable.header_main_sign_money)
                .showImageOnFail(R.drawable.header_main_sign_money)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        tabTwoOption = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.header_main_my_report)
                .showImageForEmptyUri(R.drawable.header_main_my_report)
                .showImageOnFail(R.drawable.header_main_my_report)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        tabThreeOption = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.header_main_cal_tool)
                .showImageForEmptyUri(R.drawable.header_main_cal_tool)
                .showImageOnFail(R.drawable.header_main_cal_tool)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        tabFourOption = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.header_main_cooperation_brand)
                .showImageForEmptyUri(R.drawable.header_main_cooperation_brand)
                .showImageOnFail(R.drawable.header_main_cooperation_brand)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == fragmentView) {
            fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
            headerView = LayoutInflater.from(getActivity()).inflate(R.layout.header_fragment_main, null);
            footerView = LayoutInflater.from(getActivity()).inflate(R.layout.common_footer, null);
            footerView.findViewById(R.id.common_footer_tiao).setVisibility(View.GONE);
            TextView footerTitle = (TextView) footerView.findViewById(R.id.common_footer_text);
            footerTitle.setText("已经全部加载完毕");
            SDKInitializer.initialize(this.getActivity().getApplicationContext());
            initBaiduMap();
            initView(fragmentView);
            initHeaderView(headerView);
            init();
            setClickListener();

            pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
                @Override
                public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                    Message message = new Message();
                    message.what = 1;
                    mHandler.sendMessage(message);
                    page = 1;
                    lv.removeFooterView(footerView);
                    pullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
                    if (mHasLoadedOnce && mLocationClient != null && mLocationClient.isStarted()) {
                        mLocationClient.requestLocation();
                    }
                    buildList = new ArrayList<Build>();
                    getBuildData();
                    getBannerData();
                }

                @Override
                public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                    page++;
                    getBuildData();
                }
            });

            pullToRefreshListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == SCROLL_STATE_IDLE) {
                        if (!pullToRefreshListView.isRefreshing()) {
                            tabLl.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    int scrollY = getScrollY();
                    if (!mHasLoadedOnce) {
                        tabLl.setVisibility(View.VISIBLE);
                        tabLl.getBackground().mutate().setAlpha(0);
                        header.setVisibility(View.GONE);
                    } else {
                        float h = headerView.getHeight();
                        float tab = tabLl.getHeight();
                        float Y = h - 2 * tab;
                        float f = 0;
                        float x = headerView.getY();
                        if (firstVisibleItem == 0) {
                            if (x == 0) {
                                tabLl.getBackground().mutate().setAlpha(0);
                               /* int[] location = new int[2];
                                view.getChildAt(1).getLocationOnScreen(location);
                                int u = location[1];
                                if (location[1] - 623 < 0) {
                                    tabLl.setVisibility(View.VISIBLE);
                                } else {
                                    tabLl.setVisibility(View.GONE);
                                }*/
                            } else {
                                f = 1 - (Y - Math.abs(x)) / Y;
                                if (f <= 1) {
                                    tabLl.getBackground().mutate().setAlpha((int) (255 * f));
                                    tabLl.setVisibility(View.VISIBLE);
                                }
                            }
                            if (scrollY < Y) {
                                header.setVisibility(View.GONE);
                                backTop.setVisibility(View.GONE);
                            } else {
                                header.setVisibility(View.VISIBLE);
                                backTop.setVisibility(View.VISIBLE);
                                tabLl.getBackground().mutate().setAlpha(255);
                            }
                        } else {
                            header.setVisibility(View.VISIBLE);
                            backTop.setVisibility(View.VISIBLE);
                            tabLl.getBackground().mutate().setAlpha(255);
                        }
                    }
                }
            });

            pullToRefreshListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float hstratY = 0;
                    float hmoveY = 0;
                    float startY = 0;
                    float moveY = 0;
                    hFlag = 0;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            hstratY = headerView.getY();
                            startY = event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            hmoveY = headerView.getY();
                            moveY = event.getY();
                            if (moveY > startY) {
                                if (hstratY == 0 && hmoveY == 0) {
                                    tabLl.setVisibility(View.GONE);
                                } else {
                                    tabLl.setVisibility(View.VISIBLE);
                                }
                            } else {
                                tabLl.setVisibility(View.VISIBLE);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Message msg = new Message();
                                    msg.what = 2;
                                    mHandler.sendMessage(msg);
                                }
                            }, 1000);
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });
            isPrepared = true;
            lazyLoad();
        }
        return fragmentView;
    }

    private void initBaiduMap() {
        myListener = new MyLocationListener();
        LocationClientOption option = new LocationClientOption();
        mLocationClient = new LocationClient(this.getActivity().getApplicationContext());
        mLocationClient.registerLocationListener(myListener); //注册监听函数
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
        option.setIsNeedAddress(true);//返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
        mLocationClient.setLocOption(option);
        mLocationClient.start();

    }

    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                Toast.makeText(getActivity(), "定位失败，请检查网络和定位权限后重试！", Toast.LENGTH_SHORT).show();
                return;
            }
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            String address = location.getAddrStr();
            String currentCity = location.getCity();
            String[] strs = new String[0];
            strs = currentCity.split("市");
            currentCity = strs[0];
            SPUtils.put(getActivity(), Constants.CURRENTLAT, String.valueOf(lat));
            SPUtils.put(getActivity(), Constants.CURRENTLNG, String.valueOf(lng));
            SPUtils.put(getActivity(), Constants.CURRENTADDR, address);
            SPUtils.put(getActivity(), Constants.CURRENTCITY, String.valueOf(currentCity));
        }
    }

    private void initView(View view) {
        pullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.refreshListView);
        pullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        lv = pullToRefreshListView.getRefreshableView();
        selectCity = (LinearLayout) view.findViewById(R.id.main_select_city);
        shimmerTv = (ShimmerTextView) view.findViewById(R.id.tv_shimmer);
        shimmerImage = (ImageView) view.findViewById(R.id.iv_shimmer);
        searchTv = (TextView) view.findViewById(R.id.homepager_head_search);
        tabLl = (LinearLayout) view.findViewById(R.id.table_searche_two);
        loading = (RelativeLayout) view.findViewById(R.id.loading_build);
        header = (LinearLayout) view.findViewById(R.id.header_select);
        btnArea = (Button) view.findViewById(R.id.header_select_area);
        btnType = (Button) view.findViewById(R.id.header_select_type);
        btnPrice = (Button) view.findViewById(R.id.header_select_price);
        btnMore = (Button) view.findViewById(R.id.header_select_more);
        backTop = (ImageView) view.findViewById(R.id.main_back_top);
    }

    private void initHeaderView(View headerView) {
        banner = (ConvenientBanner) headerView.findViewById(R.id.banner_fragment_main);
        signMoney = (LinearLayout) headerView.findViewById(R.id.header_signMoney);
        myReport = (LinearLayout) headerView.findViewById(R.id.header_myReport);
        calTool = (LinearLayout) headerView.findViewById(R.id.header_calTool);
        cooperBrand = (LinearLayout) headerView.findViewById(R.id.header_brand);
        ivSignMoney = (ImageView) headerView.findViewById(R.id.header_iv_signMoney);
        ivMyReport = (ImageView) headerView.findViewById(R.id.header_iv_myReport);
        ivCalTool = (ImageView) headerView.findViewById(R.id.header_iv_calTool);
        ivCooperBrand = (ImageView) headerView.findViewById(R.id.header_iv_brand);
    }

    private void init() {
        currentCityId = (int) SPUtils.get(getActivity(), Constants.BUILD_CITY_ID, 1);
        shimmerTv.setText((String) SPUtils.get(getActivity(), Constants.BUILD_CITY_NAME, "北京"));
        boolean flag = (boolean) SPUtils.get(getActivity(), "is_shimmer", false);
        if (!flag) {
            shimmer = new Shimmer();
            shimmerTv.setReflectionColor(Color.parseColor("#ffb721"));
            shimmer.setRepeatCount(1);
            shimmer.start(shimmerTv);
            SPUtils.put(getActivity(), "is_shimmer", true);
        }
        if (((int) SPUtils.get(getActivity(), Constants.NAVI_SWITCH, 0)) == 1) {
            imageLoader.displayImage((String) SPUtils.get(getActivity(), Constants.MAIN_ONE_URL, ""), ivSignMoney, tabOneOption);
            imageLoader.displayImage((String) SPUtils.get(getActivity(), Constants.MAIN_TWO_URL, ""), ivMyReport, tabTwoOption);
            imageLoader.displayImage((String) SPUtils.get(getActivity(), Constants.MAIN_THREE_URL, ""), ivCalTool, tabThreeOption);
            imageLoader.displayImage((String) SPUtils.get(getActivity(), Constants.MAIN_FOUR_URL, ""), ivCooperBrand, tabFourOption);
        }
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isRefresh) {
                    return;
                }
                Build hb = buildList.get(position - 1);
                int total = hb.getBrower() + 1;
                hb.setBrower(total);
                buildList.set(position - 1, hb);
                Intent it = new Intent(getActivity(), BuildDetailActivity.class);
                it.putExtra("bid", hb.getBid());
                startActivity(it);
            }
        });
    }

    private void setClickListener() {
        signMoney.setOnClickListener(mClickListener);
        myReport.setOnClickListener(mClickListener);
        calTool.setOnClickListener(mClickListener);
        cooperBrand.setOnClickListener(mClickListener);
        shimmerTv.setOnClickListener(mClickListener);
        selectCity.setOnClickListener(mClickListener);
        shimmerImage.setOnClickListener(mClickListener);
        searchTv.setOnClickListener(mClickListener);
        loading.setOnClickListener(mClickListener);
        btnArea.setOnClickListener(mClickListener);
        btnType.setOnClickListener(mClickListener);
        btnPrice.setOnClickListener(mClickListener);
        btnMore.setOnClickListener(mClickListener);
        backTop.setOnClickListener(mClickListener);
    }

    /**
     * 获得banner数据
     */
    private void getBannerData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getBanner");
        map.put("module", Constants.MODULE_ADVERT);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("cityId", String.valueOf(currentCityId));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new BannerCallBack() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        bannerList = DataSupport.where("cityid = ?", String.valueOf(cityId)).find(Banner.class);
                        if (bannerList != null && bannerList.size() > 0) {
                            isShow = true;
                            banner.setPages(new CBViewHolderCreator<ImageHolderView>() {
                                @Override
                                public ImageHolderView createHolder() {
                                    return new ImageHolderView();
                                }
                            }, bannerList)
                                    .setPageIndicator(new int[]{R.drawable.banner_unselected, R.drawable.banner_selected});
                        } else {
                            isShow = false;
                            bannerList = new ArrayList<Banner>();
                            bannerList.add(new Banner());
                            bannerList.add(new Banner());
                            banner.setPages(new CBViewHolderCreator<ImageHolderView>() {
                                @Override
                                public ImageHolderView createHolder() {
                                    return new ImageHolderView();
                                }
                            }, bannerList)
                                    .setPageIndicator(new int[]{R.drawable.banner_unselected, R.drawable.banner_selected});
                        }
                    }

                    @Override
                    public void onResponse(Result<ArrayList<Banner>> response) {
                        if (response == null) {
                            bannerList = DataSupport.where("cityid = ?", String.valueOf(cityId)).find(Banner.class);
                            if (bannerList != null && bannerList.size() > 0) {
                                isShow = true;
                                banner.setPages(new CBViewHolderCreator<ImageHolderView>() {
                                    @Override
                                    public ImageHolderView createHolder() {
                                        return new ImageHolderView();
                                    }
                                }, bannerList)
                                        .setPageIndicator(new int[]{R.drawable.banner_unselected, R.drawable.banner_selected});
                            } else {
                                isShow = false;
                                bannerList = new ArrayList<Banner>();
                                bannerList.add(new Banner());
                                bannerList.add(new Banner());
                                banner.setPages(new CBViewHolderCreator<ImageHolderView>() {
                                    @Override
                                    public ImageHolderView createHolder() {
                                        return new ImageHolderView();
                                    }
                                }, bannerList)
                                        .setPageIndicator(new int[]{R.drawable.banner_unselected, R.drawable.banner_selected});
                            }
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            isShow = true;
                            DataSupport.deleteAll(Banner.class, "cityid = ?", String.valueOf(currentCityId));
                            bannerList = response.getContent();
                            DataSupport.saveAll(bannerList);
                            banner.setPages(new CBViewHolderCreator<ImageHolderView>() {
                                @Override
                                public ImageHolderView createHolder() {
                                    return new ImageHolderView();
                                }
                            }, bannerList)
                                    .setPageIndicator(new int[]{R.drawable.banner_unselected, R.drawable.banner_selected});
                        } else {
                            if (response.getCode() == 1046) {

                            } else {
                                bannerList = DataSupport.where("cityid = ?", String.valueOf(cityId)).find(Banner.class);
                            }
                            if (bannerList != null && bannerList.size() > 0) {
                                isShow = true;
                                banner.setPages(new CBViewHolderCreator<ImageHolderView>() {
                                    @Override
                                    public ImageHolderView createHolder() {
                                        return new ImageHolderView();
                                    }
                                }, bannerList)
                                        .setPageIndicator(new int[]{R.drawable.banner_unselected, R.drawable.banner_selected});
                            } else {
                                isShow = false;
                                bannerList = new ArrayList<Banner>();
                                bannerList.add(new Banner());
                                bannerList.add(new Banner());
                                banner.setPages(new CBViewHolderCreator<ImageHolderView>() {
                                    @Override
                                    public ImageHolderView createHolder() {
                                        return new ImageHolderView();
                                    }
                                }, bannerList)
                                        .setPageIndicator(new int[]{R.drawable.banner_unselected, R.drawable.banner_selected});
                            }
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        isBannerDone = true;
                        if (isBuildDone) {
                            loading.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private abstract class BannerCallBack extends Callback<Result<ArrayList<Banner>>> {
        @Override
        public Result<ArrayList<Banner>> parseNetworkResponse(Response response) throws Exception {
            String string = response.body().string();
            Result<ArrayList<Banner>> banner = null;
            try {
                banner = new Gson().fromJson(string,
                        new TypeToken<Result<ArrayList<Banner>>>() {
                        }.getType());
            } catch (Exception e) {
                return null;
            }
            return banner;
        }
    }

    /**
     * 获得楼盘列表数据
     */
    private void getBuildData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "buildList");
        map.put("module", Constants.MODULE_BUILD);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("currentCity", (String) SPUtils.get(getActivity(), Constants.CURRENTCITY, "北京"));
        map.put("cityid", String.valueOf(currentCityId));
        map.put("lat", (String) SPUtils.get(getActivity(), Constants.CURRENTLAT, "39.915168"));
        map.put("lng", (String) SPUtils.get(getActivity(), Constants.CURRENTLNG, "116.403875"));
        map.put("address", (String) SPUtils.get(getActivity(), Constants.CURRENTADDR, ""));
        map.put("page", String.valueOf(page));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new BuildCallBack() {

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        if (!mHasLoadedOnce) {
                            loading.setVisibility(View.VISIBLE);
                        }
                        isRefresh = true;
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        pullToRefreshListView.onRefreshComplete();
                        tabLl.setVisibility(View.VISIBLE);
                        if (getActivity() == null) {
                            return;
                        }
                        if (page == 1) {
                            buildList = (ArrayList<Build>) DataSupport.where("cityid = ?", String.valueOf(currentCityId)).find(Build.class);
                        } else {
                            page--;
                            Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        }
                        setBuildAdapter(buildList);
                    }

                    @Override
                    public void onResponse(Result<ArrayList<Build>> response) {
                        pullToRefreshListView.onRefreshComplete();
                        tabLl.setVisibility(View.VISIBLE);
                        if (getActivity() == null) {
                            return;
                        }
                        if (response != null) {
                            if (response.getCode() > Result.RESULT_OK) {
                                buildList.addAll(response.getContent());
                                DataSupport.deleteAll(Build.class);
                                DataSupport.saveAll(buildList);
                            } else {
                                if (page == 1) {
                                    buildList = (ArrayList<Build>) DataSupport.where("cityid = ?", String.valueOf(currentCityId)).find(Build.class);
                                } else {
                                    //Toast.makeText(getActivity(), "没有更多楼盘了", Toast.LENGTH_SHORT).show();
                                    if (lv.getFooterViewsCount() == 0) {
                                        lv.addFooterView(footerView);
                                        pullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                                    }
                                    page--;
                                }
                            }
                        } else {
                            buildList = (ArrayList<Build>) DataSupport.where("cityid = ?", String.valueOf(currentCityId)).find(Build.class);
                        }
                        setBuildAdapter(buildList);
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        bid = new StringBuffer();
                        for (int i = 0; i < buildList.size(); i++) {
                            bid.append(buildList.get(i).getBid());
                            if (i != buildList.size() - 1) {
                                bid.append(",");
                            }
                        }
                        isBuildDone = true;
                        if (isBannerDone) {
                            loading.setVisibility(View.GONE);
                        }
                        isRefresh = false;
                        mHasLoadedOnce = true;
                    }
                });
    }

    private abstract class BuildCallBack extends Callback<Result<ArrayList<Build>>> {
        @Override
        public Result<ArrayList<Build>> parseNetworkResponse(Response response) throws Exception {
            String string = response.body().string();
            Result<ArrayList<Build>> build = null;
            try {
                build = new Gson().fromJson(string,
                        new TypeToken<Result<ArrayList<Build>>>() {
                        }.getType());
            } catch (Exception e) {
                return null;
            }
            return build;
        }
    }

    private void setBuildAdapter(ArrayList<Build> builds) {
        if (adapter == null) {
            adapter = new BuildAdapter(getActivity(), builds);
            lv.setAdapter(adapter);
        } else {
            adapter.list = builds;
            adapter.notifyDataSetChanged();
        }
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent it;
            HashMap<String, String> map;
            User user = User.getInstance();
            switch (v.getId()) {
                case R.id.header_signMoney://积分商成
                    map = new HashMap<>();
                    map.put("type", "integralmall");
                    MobclickAgent.onEvent(getActivity(), "buttonclick", map);
                   /* if (user.verify == User.USER_THROUGH && user.cityId > 0) {
                        if (user.style == User.NORMAL_BROKER) {*/
                    Intent intent = new Intent(getActivity(), PointMallActivity.class);
                    startActivity(intent);
                        /*} else {
                            CommonUtils.showCommonDialogOk(getActivity(), "亲，成功带看两次就可晋升为标准经纪人啦", "");
                        }*/
                    /*} else {
                        if (user.verify == User.USER_DEAL) {
                            Toast.makeText(getActivity(), "您的认证已经提交，请耐心等待", Toast.LENGTH_SHORT).show();
                        } else {
                            if (user.userType != User.TYPE_NORMAL_USER) {
                                CommonUtils.showRegDialog(getActivity(), "亲，仅支持活动区域注册认证后的标准经纪人签到", 0);
                            } else {
                                CommonUtils.showRegDialog(getActivity(), "亲，仅支持活动区域认证后的标准经纪人签到", 1);
                            }
                        }
                    }*/
                    break;
                case R.id.header_myReport://我的报备
                    map = new HashMap<String, String>();
                    map.put("type", "myreport");
                    MobclickAgent.onEvent(getActivity(), "buttonclick", map);
                    if (user.userType != User.TYPE_NORMAL_USER) {
                        CommonUtils.showRegDialog(getActivity(), "请您先登录", 0);
                        return;
                    }
                    it = new Intent(getActivity(), MyReportActivity.class);
                    startActivity(it);
                    break;
                case R.id.header_calTool://计算工具
                    map = new HashMap<String, String>();
                    map.put("type", "caltool");
                    MobclickAgent.onEvent(getActivity(), "buttonclick", map);
                    it = new Intent(getActivity(), ToolsActivity.class);
                    startActivity(it);
                    break;
                case R.id.header_brand://私人定制
                    map = new HashMap<String, String>();
                    map.put("type", "personal");
                    MobclickAgent.onEvent(getActivity(), "buttonclick", map);
                    if (User.getInstance().userType == User.TYPE_NORMAL_USER) {
                        it = new Intent(getActivity(), PersonOrderActivity.class);
                        startActivity(it);
                    } else {
                        AppUtils.showRegAuthDialog(getActivity(), "请您先登录", 0);
                    }
                    break;
                case R.id.header_select_area:
                    selection(1, 0, 0, 0, 0);
                    //areaSelect.showAsDropDown(header, 0, 1);
                    break;
                case R.id.header_select_type:
                    selection(0, 0, 1, 0, 0);
                    //typeSelect.showAsDropDown(header, 0, 1);
                    break;
                case R.id.header_select_price:
                    selection(0, 1, 0, 0, 0);
                    //priceSelect.showAsDropDown(header, 0, 1);
                    break;
                case R.id.header_select_more:
                    selection(0, 0, 0, 1, 0);
                    //moreSelect.showAsDropDown(header, 0, 1);
                    break;
                case R.id.main_select_city:
                case R.id.iv_shimmer://城市
                case R.id.tv_shimmer://城市
                    it = new Intent(getActivity(), SelectHomeCityActivity.class);
                    startActivity(it);
                    break;
                case R.id.homepager_head_search:
                    it = new Intent(getActivity(), BuildSearchActivity.class);
                    it.putExtra("cityid", currentCityId);
                    startActivity(it);
                    break;
                case R.id.loading_build:
                    break;
                case R.id.main_back_top:
                    //pullUpScrollView.scrollTo(0, 0);
                    //pullToRefreshListView.scrollTo(0, 0);
                    //lv.setSelection(0);
                    lv.smoothScrollToPosition(0);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        banner.startTurning(3000);
        city = (String) SPUtils.get(getActivity(), Constants.BUILD_CITY_NAME, "北京");
        cityId = (int) SPUtils.get(getActivity(), Constants.BUILD_CITY_ID, 1);
        if (currentCityId != cityId) {
            isBannerDone = false;
            isBuildDone = false;
            bid = new StringBuffer();
            currentCityId = cityId;
            shimmerTv.setText(city);
            bannerList = new ArrayList<>();
            adapter = null;
            buildList.clear();
            page = 1;
            lv.removeFooterView(footerView);
            pullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
            getBannerData();
            getBuildData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        banner.stopTurning();
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible || mHasLoadedOnce) {
            return;
        }
        lv.addHeaderView(headerView, null, false);
        getBannerData();
        getBuildData();
        //lv.addFooterView(footerView, null, false);
    }

    private class ImageHolderView implements CBPageAdapter.Holder<Banner> {
        private ImageView imageView;

        @Override
        public View createView(Context context) {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return imageView;
        }

        @Override
        public void UpdateUI(Context context, final int position, final Banner data) {
            if (isShow) {
                imageView.setImageResource(R.drawable.empty_building_list);
                String bannerUrl = data.getPic();
                imageLoader.displayImage(CommonUtils.getBannerUrl(getActivity(), bannerUrl), imageView);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("pos", String.valueOf(position));
                        MobclickAgent.onEvent(getActivity(), "bannerclick", map);
                        switch (data.getType()) {
                            case Constants.TYPELOGIN://登录注册界面
                                if (User.getInstance().userType == User.TYPE_NORMAL_USER) {
                                    Toast.makeText(getActivity(), "您已经登录", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (getActivity() != null) {
                                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                                    startActivity(intent);
                                }
                                break;
                            case Constants.TYPEBUILD://房源列表
                                if (getActivity() != null) {
                                    if (buildList.size() == 0 || buildList == null) {
                                        Toast.makeText(getActivity(), "暂无房源列表", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Intent intent = new Intent(getActivity(), BuildSelectActivity.class);
                                        intent.putExtra("buildList", buildList);
                                        startActivity(intent);
                                    }
                                }
                                break;
                            case Constants.TYPECUSTOM://客户首页
                                if (getActivity() != null) {
                                    MainActivity context = (MainActivity) getActivity();
                                    context.switchContent(MainActivity.POS_CUSTOM);
                                }
                                break;
                            case Constants.TYPECENTER://个人中心首页
                                if (getActivity() != null) {
                                    MainActivity context = (MainActivity) getActivity();
                                    context.switchContent(MainActivity.POS_CENTER);
                                }
                                break;
                            case Constants.TYPEIDAUTH://身份认证
                                if (User.getInstance().userType == User.TYPE_DEFAULT_USER || User.getInstance().userType == User.TYPE_TMP_USER) {
                                    if (getActivity() != null) {
                                        AppUtils.showRegAuthDialog(getActivity(), "请您先登录", 0);
                                    }
                                    return;
                                }
                                if (User.getInstance().verify == User.USER_DEAL) {
                                    Toast.makeText(getActivity(), "您的认证我们已经收到，请耐心等待", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (User.getInstance().verify == User.USER_THROUGH) {
                                    Toast.makeText(getActivity(), "您已是售房宝认证用户，无需再次认证", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Intent it = new Intent(getActivity(), AuthActivity.class);
                                startActivity(it);
                                break;
                            case Constants.TYPEH5SHOW://宣传页制作
                            /*if (getActivity() != null) {
                                Intent intent = new Intent(getActivity(), H5ShowActivity.class);
                                startActivity(intent);
                            }*/
                                break;
                            case Constants.TYPESENDADVICE://意见反馈
                                if (getActivity() != null) {
                                    Intent intent = new Intent(getActivity(), SuggestActivity.class);
                                    startActivity(intent);
                                }
                                break;
                            case Constants.TYPEINENTFRIEND://邀请好友
                                if (CommonUtils.isLogin()) {
                                    if (getActivity() != null) {
                                        Intent intent = new Intent(getActivity(), MyInviteActivity.class);
                                        startActivity(intent);
                                    }
                                } else {
                                    CommonUtils.showRegDialog(getActivity(), "请您先登录", 0);
                                }
                                break;
                            case Constants.TYPESOCIAL://社区首页
                                if (getActivity() != null) {
                                    MainActivity context = (MainActivity) getActivity();
                                    context.switchContent(MainActivity.POS_MORE);
                                }
                                break;
                            case Constants.TYPEPOINTMALL://积分商城
                                if (getActivity() != null) {
                                    Intent intent = new Intent(getActivity(), PointMallActivity.class);
                                    startActivity(intent);
                                }
                                break;
                            case Constants.TYPEDEVELOP://品牌合作
                                if (getActivity() != null) {
                                    Intent intent = new Intent(getActivity(), CooperBrandActivity.class);
                                    intent.putExtra("cityid", User.getInstance().cityId);
                                    startActivity(intent);
                                }
                                break;
                            case Constants.TYPEINTEGRAL://添加积分
                                if (getActivity() != null) {
                                    Intent intent = new Intent(getActivity(), ShareBannerActivity.class);
                                    String url = String.valueOf(data.getExt());
                                    intent.putExtra("url", url);
                                    intent.putExtra("title", data.getTitle());
                                    startActivity(intent);
                                }
                                break;
                            case Constants.TYPEURL://单个URL
                                if (getActivity() != null) {
                                    Intent intent = new Intent(getActivity(), ShareBannerActivity.class);
                                    String url = String.valueOf(data.getExt());
                                    intent.putExtra("url", url);
                                    intent.putExtra("title", data.getTitle());
                                    startActivity(intent);
                                }
                                break;
                            case Constants.TYPEBUILDDETAIL://楼盘详情
                                String s = (String) data.getExt();
                                int build;
                                try {
                                    build = Integer.parseInt(s);
                                } catch (NumberFormatException e) {
                                    return;
                                }
                                Intent intent = new Intent(getActivity(), BuildDetailActivity.class);
                                intent.putExtra("bid", build);
                                startActivity(intent);
                                break;
                            default:
                                break;
                        }
                    }
                });
            } else {
                if (position == 0) {
                    imageView.setImageResource(R.drawable.local_banner_user);
                } else {
                    imageView.setImageResource(R.drawable.local_banner_help);
                }
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (position == 0) {
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getActivity(), HelpCenterActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        }
    }

    class BuildAdapter extends BaseAdapter {

        List<Build> list;
        LayoutInflater inflater;

        public BuildAdapter(Context ctx, List<Build> list) {
            inflater = LayoutInflater.from(ctx);
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            Build build = list.get(position);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_buildfragment_list, parent, false);
                holder.buildImage = (ImageView) convertView.findViewById(R.id.item_build_image);
                holder.browseNum = (TextView) convertView.findViewById(R.id.item_build_browse_num);
                holder.buildHongbao = (ImageView) convertView.findViewById(R.id.item_build_hongbao);
                holder.buildAngle = (ImageView) convertView.findViewById(R.id.item_build_angle);
                holder.district = (TextView) convertView.findViewById(R.id.item_build_district);
                holder.name = (TextView) convertView.findViewById(R.id.item_build_name);
                holder.price = (TextView) convertView.findViewById(R.id.item_build_price);
                holder.commission = (TextView) convertView.findViewById(R.id.item_build_commission);
                holder.distanceLl = (LinearLayout) convertView.findViewById(R.id.item_build_diatance);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.name.setText(build.getName());
            holder.district.setText("[" + build.getArea() + "]");
            holder.price.setText(build.getPrice());
            if (User.getInstance().verify == User.USER_THROUGH) {
                holder.commission.setText(build.getCommission());
            } else {
                holder.commission.setText("认证可见");
            }
            int distance = 0;
            if (build.getDistance() == null || build.getDistance().equals("") || build.getDistance().equals("0")
                    || Integer.parseInt(build.getDistance()) >= 100000) {
                holder.distanceLl.setVisibility(View.GONE);
            } else {
                holder.distanceLl.setVisibility(View.VISIBLE);
                distance = Integer.parseInt(build.getDistance());
            }
            DecimalFormat df2 = new DecimalFormat("#0.00");//这样为保持2位
            holder.browseNum.setText("距您" + df2.format(distance / 1000.0) + "km");
            switch (build.getLabel()) {
                case Build.NEW:
                    if (build.getUnique_business() == 1) {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.unique_o2o);
                    } else {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.build_angle_new);
                    }
                    break;
                case Build.JIAN:
                    if (build.getUnique_business() == 1) {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.unique_o2o);
                    } else {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.build_angle_jian);
                    }
                    break;
                case Build.RED_PACKETS:
                case Build.DAIKAN_PACKET:
                    holder.buildAngle.setVisibility(View.VISIBLE);
                    holder.buildAngle.setImageResource(R.drawable.build_angle_packet);
                    break;
                default:
                    if (build.getUnique_business() == 1) {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.unique_o2o);
                    } else {
                        holder.buildAngle.setVisibility(View.GONE);
                    }
                    break;
            }
            imageLoader.displayImage(build.getPic(), holder.buildImage, options);
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView buildImage;
        TextView browseNum;
        ImageView buildHongbao;
        ImageView buildAngle;
        TextView district;
        TextView name;
        TextView price;
        TextView commission;
        LinearLayout distanceLl;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.unRegisterLocationListener(myListener);
        mLocationClient.stop();
        EventBus.getDefault().unregister(getActivity());
    }

    public void onEventMainThread(User event) {
        if (event.verify == User.USER_THROUGH) {
            lv.setSelection(0);
            setBuildAdapter(buildList);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fragmentView != null) {
            ((ViewGroup) fragmentView.getParent()).removeView(fragmentView);
        }
    }

    private void selection(int area, int price, int type, int feature, int layout) {

        btnArea.setSelected(false);
        btnArea.setText("区域");
        btnArea.setTextColor(Color.parseColor("#323232"));

        btnType.setSelected(false);
        btnType.setText("类型");
        btnType.setTextColor(Color.parseColor("#323232"));

        btnPrice.setSelected(false);
        btnPrice.setText("价格");
        btnPrice.setTextColor(Color.parseColor("#323232"));

        Intent intent = new Intent(getActivity(), BuildSelectActivity.class);
        intent.putExtra("area", area);
        intent.putExtra("price", price);
        intent.putExtra("type", type);
        intent.putExtra("feature", feature);
        intent.putExtra("layout", layout);
        intent.putExtra("buildList", buildList);
        startActivity(intent);
    }

    //listview滚动距离
    public int getScrollY() {
        View c = lv.getChildAt(0);
        if (c == null) {
            return 0;
        }
        int firstVisiblePosition = lv.getFirstVisiblePosition();
        int top = c.getTop();
        return -top;
    }
}
