package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseFragment;
import com.kupangstudio.shoufangbao.base.BaseFragmentActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.fragment.CommunityFragment;
import com.kupangstudio.shoufangbao.fragment.CustomFragment;
import com.kupangstudio.shoufangbao.fragment.MainFragment;
import com.kupangstudio.shoufangbao.fragment.PersonalCenterFragment;
import com.kupangstudio.shoufangbao.fragment.SceneFragment;
import com.kupangstudio.shoufangbao.model.SwitchEvent;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.updateservice.UpdateManager;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.ExitUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import cn.jpush.android.api.TagAliasCallback;
import de.greenrobot.event.EventBus;

/**
 * 主页activity四个模块对应四个Fragment
 * Created by long on 15/11/6.
 * Copyright 2015 android_xiaobai.
 */
public class MainActivity extends BaseFragmentActivity {

    public static final int POS_MAIN = 0;// 首页
    public static final int POS_CUSTOM = 1;// 客户
    public static final int POS_CENTER = 3;// 我的
    public static final int POS_MORE = 2;// v圈
    private ViewPager viewPager;
    private Button[] mTabs;
    private Button btnHome;
    private Button btnCustom;
    private Button btnCenter;
    private Button btnCommunity;
    private MainFragment mainFragment;
    private CustomFragment customFragment;
    private PersonalCenterFragment centerFragment;
    private SceneFragment communityFragment;
    private PagerAdapter adapter;
    private ArrayList<BaseFragment> fragmentList;
    private int currentPos;
    private int index;
    private static final int MSG_SET_ALIAS = 1001;
    private int aliasRetrycount = 0;
    private static final int MOST_TIME = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CommonUtils.addActivity(this);
        EventBus.getDefault().register(this);
        if (savedInstanceState != null) {
            currentPos = savedInstanceState.getInt("currentPos");
        }
        initView();
        init();
        UpdateManager updateManager = new UpdateManager(this, false);
        updateManager.checkUpdateInfo();
        for (int i = 0; i < mTabs.length; i++) {
            if (i == currentPos) {
                mTabs[i].setSelected(true);
            } else {
                mTabs[i].setSelected(false);
            }
        }
        setClickListener();

        String show = (String) SPUtils.get(MainActivity.this, Constants.PARM_SHOW, "");
        if (show != null && !show.equals("")) {
            if (show.equals("buildDetail")) {
                String bid = (String) SPUtils.get(MainActivity.this, Constants.PARM_BID, "");
                if (bid != null && !bid.equals("")) {
                    //跳到楼盘详情
                    Intent intent = new Intent(MainActivity.this, BuildDetailActivity.class);
                    intent.putExtra("bid", Integer.parseInt(bid));
                    startActivity(intent);
                }
            } else if (show.equals("socialMessage")) {
                /*String newsid = (String) SPUtils.get(MainActivity.this, Constants.PARM_NEWSID, "");
                if (newsid != null && !newsid.equals("")) {
                    //跳到资讯详情
                    Intent intent = new Intent(MainActivity.this, CommunityDetailActivity.class);
                    intent.putExtra("url", (String) SPUtils.get(MainActivity.this, Constants.NEWS_URL,
                            "https://www.shoufangbao.com/index.php?r=new/index&nid=") +
                            (String) SPUtils.get(MainActivity.this, Constants.PARM_NEWSID, ""));
                    intent.putExtra("newsid", newsid);
                    intent.putExtra("mid", Integer.parseInt(newsid));
                    startActivity(intent);
                }*/
                switchContent(POS_MORE);
            }
        }

        mTabs[currentPos].setSelected(true);
        adapter = new PagerAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setCurrentItem(currentPos);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                index = position;
                if (currentPos != index) {
                    mTabs[currentPos].setSelected(false);
                    mTabs[index].setSelected(true);
                    currentPos = index;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        User user = User.getInstance();
        if (user.userType == User.TYPE_NORMAL_USER) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS,
                    user.uid + ""));
        }
    }

    private void initView() {
        btnHome = (Button) findViewById(R.id.btn_home);
        btnCustom = (Button) findViewById(R.id.btn_custom);
        btnCenter = (Button) findViewById(R.id.btn_center);
        btnCommunity = (Button) findViewById(R.id.btn_community);
        viewPager = (ViewPager) findViewById(R.id.main_viewpager);
    }

    private void init() {
        mTabs = new Button[4];
        mainFragment = new MainFragment();
        customFragment = new CustomFragment();
        centerFragment = new PersonalCenterFragment();
        communityFragment = new SceneFragment();
        mTabs[0] = btnHome;
        mTabs[1] = btnCustom;
        mTabs[2] = btnCommunity;
        mTabs[3] = btnCenter;
        fragmentList = new ArrayList<>();
        fragmentList.add(mainFragment);
        fragmentList.add(customFragment);
        fragmentList.add(communityFragment);
        fragmentList.add(centerFragment);
    }

    private void setClickListener() {
        btnHome.setOnClickListener(mClickListener);
        btnCustom.setOnClickListener(mClickListener);
        btnCenter.setOnClickListener(mClickListener);
        btnCommunity.setOnClickListener(mClickListener);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    //JPushInterface.setAlias(MainActivity.this, (String) msg.obj, mAliasCallback);
                    break;
                default:
                    break;
            }
        }
    };

    private TagAliasCallback mAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            switch (code) {
                case 0:
                    // 建议这里往 SharePreference 里写一个成功设置的状态。成功设置一次后，以后不必再次设置了。
                    break;
                case 6002:
                    // 延迟 60 秒来调用 Handler 设置别名
                    // 重试5次
                    aliasRetrycount++;
                    if (aliasRetrycount > MOST_TIME) {
                        return;
                    }
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentPos", currentPos);
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            HashMap<String, String> map;
            switch (v.getId()) {
                case R.id.btn_home:
                    map = new HashMap<>();
                    map.put("tabbarclick", "home");
                    MobclickAgent.onEvent(MainActivity.this, "buttonclick", map);
                    index = POS_MAIN;
                    break;
                case R.id.btn_custom://客户
                    map = new HashMap<>();
                    map.put("tabbarclick", "custom");
                    MobclickAgent.onEvent(MainActivity.this, "buttonclick", map);
                    index = POS_CUSTOM;
                    break;
                case R.id.btn_center://我的
                    map = new HashMap<>();
                    map.put("tabbarclick", "personalcenter");
                    MobclickAgent.onEvent(MainActivity.this, "buttonclick", map);
                    index = POS_CENTER;
                    break;
                case R.id.btn_community://v圈子
                    map = new HashMap<>();
                    map.put("tabbarclick", "vcircle");
                    MobclickAgent.onEvent(MainActivity.this, "buttonclick", map);
                    index = POS_MORE;
                    break;
                default:
                    break;
            }
            viewPager.setCurrentItem(index);
            if (currentPos != index) {
                mTabs[currentPos].setSelected(false);
                mTabs[index].setSelected(true);
                currentPos = index;
            }
        }
    };

    public void switchContent(int pos) {
        index = pos;
        viewPager.setCurrentItem(index);
        if (currentPos != index) {
            mTabs[currentPos].setSelected(false);
            mTabs[index].setSelected(true);
            currentPos = index;
        }
    }

    public void onEventMainThread(SwitchEvent event) {
        switchContent(event.getPosition());
    }

    class PagerAdapter extends FragmentPagerAdapter {

        ArrayList<BaseFragment> list;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public PagerAdapter(FragmentManager fm, ArrayList<BaseFragment> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }

    public long backtime = 0;

    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            long t = System.currentTimeMillis();
            if (t - backtime > 3000) {
                Toast.makeText(this, "确定离开？", Toast.LENGTH_SHORT).show();
                backtime = t;
                return true;
            }
            ExitUtils.getInstance().exit(MainActivity.this);
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
        EventBus.getDefault().unregister(this);
    }
}
