package com.kupangstudio.shoufangbao;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;

import com.kupangstudio.shoufangbao.base.BaseFragmentActivity;
import com.kupangstudio.shoufangbao.fragment.MyCardTimeOutFragment;
import com.kupangstudio.shoufangbao.fragment.MyCardUseFragment;
import com.kupangstudio.shoufangbao.fragment.MyCardUsedFragment;
import com.kupangstudio.shoufangbao.utils.CommonUtils;

/**
 * Created by long1 on 16/3/16.
 * Copyright 16/3/16 android_xiaobai.
 * 我的卡券
 */
public class MyCardActivity extends BaseFragmentActivity implements RadioGroup.OnCheckedChangeListener{

    private RadioGroup radioGroup;
    private MyCardUseFragment useFragment;//可使用
    private MyCardUsedFragment usedFragment;//已使用
    private MyCardTimeOutFragment timeOutFragment;//已过期
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_card);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "我的卡券");
        initView();
        useFragment = new MyCardUseFragment();
        usedFragment = new MyCardUsedFragment();
        timeOutFragment = new MyCardTimeOutFragment();
        currentFragment = useFragment;
        getSupportFragmentManager().beginTransaction().
                add(R.id.frame_my_card, useFragment).commit();
        radioGroup.setOnCheckedChangeListener(this);
    }

    private void initView() {
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.radio_use://可使用
                switchContent(currentFragment, useFragment);
                break;
            case R.id.radio_used://已使用
                switchContent(currentFragment, usedFragment);
                break;
            case R.id.radio_timeout://已过期
                switchContent(currentFragment, timeOutFragment);
                break;
            default:
                break;
        }
    }

    private void switchContent(Fragment from, Fragment to) {
        if(currentFragment != to) {
            currentFragment = to;
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if(!to.isAdded()) {
                ft.hide(from).add(R.id.frame_my_card, to);
            } else {
                ft.hide(from).show(to);
            }
            ft.commit();
        }
    }

}
