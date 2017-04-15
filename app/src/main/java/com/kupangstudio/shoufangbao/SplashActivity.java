package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Splash;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.FileUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.utils.StringUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.litepal.crud.DataSupport;

import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Response;


/**
 * 闪屏程序入口activity
 */

public class SplashActivity extends BaseActivity {

    private static final int DELAYED_MILLSECONDS = 3000;
    private static final String START_FIRST = "start_first" + Constants.VERSION_NAME;
    private static final int REQ_CREATE_PATTERN = 1;//创建手势解锁
    private static final int REQ_ENTER_PATTERN = 2;//验证手势解锁
    private static final int DELAY_END = 1;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private ImageView ivSplash;
    private String newsid;
    private String bid;
    private String show;
    private LocationClient mLocationClient;
    private MyLocationListener myListener;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DELAY_END:
                    User user = User.getInstance();
                    if (user.userType == User.TYPE_NORMAL_USER) {
                        boolean isOpenFIrst = (boolean) SPUtils.get(SplashActivity.this, Constants.IS_LOCK_SET, true);
                        if (!isOpenFIrst) {//是否第一次启动
                            boolean isOpenLock = (boolean) SPUtils.get(SplashActivity.this, Constants.IS_LOCK_OPEN, false);
                            if (isOpenLock) {// 是否打开手势解锁
                                Intent it = new Intent(SplashActivity.this, LockEnterAppActivity.class);
                                startActivity(it);
                            } else {
                                Intent it = new Intent(SplashActivity.this, MainActivity.class);
                                startActivity(it);
                            }
                        } else {
                            isFirstEnter();
                        }
                    } else {
                        isFirstEnter();
                    }
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * app第一次启动的一各种判断
     */
    private void isFirstEnter() {
        boolean firstStart = (boolean) SPUtils.get(SplashActivity.this, START_FIRST, true);
        if (firstStart) {//是否第一次启动
            FileUtils.deleteFile(Constants.CACHE_PATH);
            SPUtils.put(SplashActivity.this, START_FIRST, false);
            Intent it = new Intent(SplashActivity.this, IntroActivity.class);
            startActivity(it);
        } else {

            boolean setHomeCity = (boolean) SPUtils.get(SplashActivity.this, Constants.HOME_SET_CITY, false);
            if (setHomeCity) {//是否选择过首页城市
                Intent it = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(it);
            } else {
                Intent it = new Intent(SplashActivity.this, SelectHomeCityActivity.class);
                it.putExtra(Constants.IS_FROM_FIRST, true);
                startActivity(it);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        CommonUtils.addActivity(this);
        initView();
        init();
        SDKInitializer.initialize(this.getApplicationContext());
        initBaiduMap();
        Splash s = DataSupport.findFirst(Splash.class);
        mHandler.sendEmptyMessageDelayed(DELAY_END, DELAYED_MILLSECONDS);
        if (s != null && !StringUtils.isEmpty(s.getPic())) {
            imageLoader.displayImage(CommonUtils.getSplashUrl(SplashActivity.this, s.getPic()), ivSplash, options);
        }

        //接收跳转
        Intent intent1 = getIntent();
        String action = intent1.getAction();
        if(Intent.ACTION_VIEW.equals(action)){
            Uri data = intent1.getData();
            if(data != null){
                show = data.getQueryParameter("show");
                if (show.equals("buildDetail")) {
                    bid = data.getQueryParameter("bid");
                    if(bid == null) {
                        bid = "";
                    }
                    newsid = "";
                }else if(show.equals("socialMessage")){
                    newsid = data.getQueryParameter("newsid");
                    if(newsid == null) {
                        newsid = "";
                    }
                    bid = "";
                }else{
                    show = "";
                }
            }
        }else{
            show = "";
            bid = "";
            newsid = "";
        }
        SPUtils.put(SplashActivity.this, Constants.PARM_SHOW, show);
        SPUtils.put(SplashActivity.this, Constants.PARM_BID, bid);
        SPUtils.put(SplashActivity.this, Constants.PARM_NEWSID, newsid);
        getData();
    }

    private void initBaiduMap() {
        myListener = new MyLocationListener();
        LocationClientOption option = new LocationClientOption();
        mLocationClient = new LocationClient(this.getApplicationContext());
        mLocationClient.registerLocationListener(myListener); //注册监听函数
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
        option.setIsNeedAddress(true);//返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.requestLocation();
        }
    }

    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                Toast.makeText(SplashActivity.this, "定位失败，请检查网络和定位权限后重试！", Toast.LENGTH_SHORT).show();
                return;
            }
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            String address = location.getAddrStr();
            String currentCity = location.getCity();
            String[] strs = new String[0];
            strs = currentCity.split("市");
            currentCity = strs[0];
            SPUtils.put(SplashActivity.this, Constants.CURRENTLAT, String.valueOf(lat));
            SPUtils.put(SplashActivity.this, Constants.CURRENTLNG, String.valueOf(lng));
            SPUtils.put(SplashActivity.this, Constants.CURRENTADDR, address);
            SPUtils.put(SplashActivity.this, Constants.CURRENTCITY, String.valueOf(currentCity));
            //Toast.makeText(SplashActivity.this, "纬度："+lat +"经度："+ lng + address, Toast.LENGTH_LONG).show();
        }
    }

    private void initView() {
        ivSplash = (ImageView) findViewById(R.id.iv_splash);
    }

    private void init() {
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.splash_bg)
                .showImageOnFail(R.drawable.splash_bg).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).build();
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void onBackPressed() {
        //屏蔽back返回键
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.unRegisterLocationListener(myListener);
        mLocationClient.stop();
        CommonUtils.removeActivity(this);
    }

    /**
     * 从网络获取最新闪屏图片
     */
    private void getData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getScreen");
        map.put("module", Constants.MODULE_ADVERT);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        int cityId = (int) SPUtils.get(SplashActivity.this, Constants.BUILD_CITY_ID,1);
        map.put("cityId", String.valueOf(cityId));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new SplashCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(Result<Splash> response) {
                        if (response.getCode() > Result.RESULT_OK) {
                            DataSupport.deleteAll(Splash.class);
                            Splash splash = response.getContent();
                            splash.save();
                            imageLoader.displayImage(CommonUtils.getSplashUrl(SplashActivity.this, splash.getPic()), ivSplash, options);
                        }
                    }
                });
    }

    private abstract class SplashCallback extends Callback<Result<Splash>> {
        @Override
        public Result<Splash> parseNetworkResponse(Response response) throws Exception {
            Result<Splash> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<Splash>>(){}.getType());
            }catch (Exception e) {
                return null;
            }
            return result;
        }
    }
}
