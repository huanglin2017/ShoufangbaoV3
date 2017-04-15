package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;

import com.kupangstudio.shoufangbao.base.BaseFragmentActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.fragment.IntroFragmentFour;
import com.kupangstudio.shoufangbao.fragment.IntroFragmentOne;
import com.kupangstudio.shoufangbao.fragment.IntroFragmentThree;
import com.kupangstudio.shoufangbao.fragment.IntroFragmentTwo;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

/**
 * App首次启动引导页面
 * Created by long on 15/11/5.
 * Copyright 2015 android_xiaobai.
 */
public class IntroActivity extends BaseFragmentActivity {

    private IntroFragmentOne introFragmentOne;
    private IntroFragmentTwo introFragmentTwo;
    private IntroFragmentThree introFragmentThree;
    private IntroFragmentFour introFragmentFour;
    private ArrayList<Fragment> fList;
    private FragmentPagerAdapter adapter;
    private ViewPager pager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        CommonUtils.addActivity(this);
        pager = (ViewPager) findViewById(R.id.pager);
        init();
    }

    private void init() {
        fList = new ArrayList<Fragment>();
        introFragmentOne = new IntroFragmentOne();
        introFragmentTwo = new IntroFragmentTwo();
        introFragmentThree = new IntroFragmentThree();
        introFragmentFour = new IntroFragmentFour();
        fList.add(introFragmentOne);
        fList.add(introFragmentTwo);
        fList.add(introFragmentThree);
        fList.add(introFragmentFour);
        adapter = new MyAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
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

    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            return fList.get(pos);
        }

        @Override
        public int getCount() {
            return fList.size();
        }

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (CommonUtils.checkOtherSystem(IntroActivity.this)) {//是否是小米系统
                Intent it = new Intent(IntroActivity.this, MiuiPointActivity.class);
                startActivity(it);
            } else {
                boolean setHomeCity = (boolean) SPUtils.get(IntroActivity.this, Constants.HOME_SET_CITY, false);
                if (setHomeCity) {//是否选择过首页城市
                    Intent it = new Intent(IntroActivity.this, MainActivity.class);
                    startActivity(it);
                    finish();
                } else {
                    Intent it = new Intent(IntroActivity.this, SelectHomeCityActivity.class);
                    it.putExtra(Constants.IS_FROM_FIRST, true);
                    startActivity(it);
                    finish();
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
