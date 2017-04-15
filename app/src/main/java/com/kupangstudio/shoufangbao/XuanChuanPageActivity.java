package com.kupangstudio.shoufangbao;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;

import com.kupangstudio.shoufangbao.base.BaseFragmentActivity;
import com.kupangstudio.shoufangbao.fragment.MinePageFragment;
import com.kupangstudio.shoufangbao.fragment.ModelPageFragment;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by long1 on 15/11/23.
 * Copyright 15/11/23 android_xiaobai.
 * 宣传页制作
 */
public class XuanChuanPageActivity extends BaseFragmentActivity {

    private Button btnModel;//模板
    private Button btnMine;//我的
    private ModelPageFragment modelFragment;
    private MinePageFragment mineFragment;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xuanchuan_page);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "制作列表");
        initView();
        setClickListener();
        init();
    }

    private void initView() {
        btnModel = (Button) findViewById(R.id.btn_model);
        btnMine = (Button) findViewById(R.id.btn_mine);
    }

    private void setClickListener() {
        btnModel.setOnClickListener(mClickListener);
        btnMine.setOnClickListener(mClickListener);
    }

    private void init() {
        modelFragment = new ModelPageFragment();
        mineFragment = new MinePageFragment();
        currentFragment = modelFragment;
        getSupportFragmentManager().beginTransaction().add(R.id.layout_xuanchuan, currentFragment).commitAllowingStateLoss();
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_model:
                    btnModel.setSelected(true);
                    btnMine.setSelected(false);
                    selectFragment(currentFragment, modelFragment);
                    break;
                case R.id.btn_mine:
                    btnModel.setSelected(false);
                    btnMine.setSelected(true);
                    selectFragment(currentFragment, mineFragment);
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
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    private void selectFragment(Fragment a, Fragment b) {
        if (currentFragment != b) {
            currentFragment = b;
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (!b.isAdded()) {
                ft.hide(a).add(R.id.layout_xuanchuan, b);
            } else {
                ft.hide(a).show(b);
            }
            ft.commitAllowingStateLoss();
        }
    }
}
