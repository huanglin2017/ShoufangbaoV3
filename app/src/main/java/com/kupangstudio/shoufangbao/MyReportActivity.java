package com.kupangstudio.shoufangbao;

import android.graphics.Paint;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.BaseFragmentActivity;
import com.kupangstudio.shoufangbao.fragment.MyReportFragment;
import com.kupangstudio.shoufangbao.model.ReportEvent;
import com.kupangstudio.shoufangbao.model.SwitchEvent;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by long1 on 15/11/25.
 * Copyright 15/11/25 android_xiaobai.
 * 我的报备
 */
public class MyReportActivity extends BaseFragmentActivity implements RadioGroup.OnCheckedChangeListener {

    private RadioGroup radioGroup;
    private Fragment fragmentAll;
    private Fragment fragmentReport;
    private Fragment fragmentAccept;
    private Fragment fragmentSeeing;
    private Fragment fragmentPurchase;
    private Fragment fragmentDeal;
    private Fragment fragmentCommission;
    private Fragment fragmentEnd;
    private Fragment mFragment;
    private int checkPosition;
    private RadioButton[] mRadioButton;
    private int currentPosition;
    private RadioButton radioAll;
    private RadioButton radioReport;
    private RadioButton radioAccept;
    private RadioButton radioSeeing;
    private RadioButton radioPurchase;
    private RadioButton radioDeal;
    private RadioButton radioCommission;
    private RadioButton radioEnd;
    private LinearLayout layoutAll;
    private TextView tvAll;
    private HorizontalScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myreport);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "我的报备");
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        radioAll = (RadioButton) findViewById(R.id.radio_all);
        radioReport = (RadioButton) findViewById(R.id.radio_report);
        radioAccept = (RadioButton) findViewById(R.id.radio_accept);
        radioSeeing = (RadioButton) findViewById(R.id.radio_seeing);
        radioPurchase = (RadioButton) findViewById(R.id.radio_purchase);
        radioDeal = (RadioButton) findViewById(R.id.radio_deal);
        radioCommission = (RadioButton) findViewById(R.id.radio_commission);
        radioEnd = (RadioButton) findViewById(R.id.radio_end);
        layoutAll = (LinearLayout) findViewById(R.id.layoutAll);
        scrollView = (HorizontalScrollView) findViewById(R.id.scrollView);
        tvAll = (TextView) findViewById(R.id.tvAll);
        tvAll.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        tvAll.getPaint().setAntiAlias(true);//抗锯齿
        mRadioButton = new RadioButton[8];
        mRadioButton[0] = radioAll;
        mRadioButton[1] = radioReport;
        mRadioButton[2] = radioAccept;
        mRadioButton[3] = radioSeeing;
        mRadioButton[4] = radioPurchase;
        mRadioButton[5] = radioDeal;
        mRadioButton[6] = radioCommission;
        mRadioButton[7] = radioEnd;
        radioGroup.setOnCheckedChangeListener(this);
        currentPosition = checkPosition = 0;
        if (fragmentAll == null) {
            mFragment = fragmentAll = new MyReportFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", checkPosition);
            fragmentAll.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, fragmentAll, "TAG0").commit();
        }

        tvAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioAll.setChecked(true);
                layoutAll.setVisibility(View.GONE);
                EventBus.getDefault().post(new ReportEvent(0));
                mRadioButton[0].setText("全部");
                scrollView.scrollTo(0, 0);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
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
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle;
        int width = radioAll.getWidth();
        switch (checkedId) {
            case R.id.radio_all:
                checkPosition = 0;
                fragmentAll = manager.findFragmentByTag("TAG0");
                if (fragmentAll == null) {
                    fragmentAll = new MyReportFragment();
                    bundle = new Bundle();
                    bundle.putInt("position", checkPosition);
                    fragmentAll.setArguments(bundle);
                }
                switchContent(fragmentAll, checkPosition, width);
                break;
            case R.id.radio_report:
                checkPosition = 1;
                fragmentReport = manager.findFragmentByTag("TAG1");
                if (fragmentReport == null) {
                    fragmentReport = new MyReportFragment();
                    bundle = new Bundle();
                    bundle.putInt("position", checkPosition);
                    fragmentReport.setArguments(bundle);
                }
                switchContent(fragmentReport, checkPosition, width);
                break;
            case R.id.radio_accept:
                checkPosition = 2;
                fragmentAccept = manager.findFragmentByTag("TAG2");
                if (fragmentAccept == null) {
                    fragmentAccept = new MyReportFragment();
                    bundle = new Bundle();
                    bundle.putInt("position", checkPosition);
                    fragmentAccept.setArguments(bundle);
                }
                switchContent(fragmentAccept, checkPosition, width);
                break;
            case R.id.radio_seeing:
                checkPosition = 3;
                fragmentSeeing = manager.findFragmentByTag("TAG3");
                if (fragmentSeeing == null) {
                    fragmentSeeing = new MyReportFragment();
                    bundle = new Bundle();
                    bundle.putInt("position", checkPosition);
                    fragmentSeeing.setArguments(bundle);
                }
                switchContent(fragmentSeeing, checkPosition, width);
                break;
            case R.id.radio_purchase:
                checkPosition = 4;
                fragmentPurchase = manager.findFragmentByTag("TAG4");
                if (fragmentPurchase == null) {
                    fragmentPurchase = new MyReportFragment();
                    bundle = new Bundle();
                    bundle.putInt("position", checkPosition);
                    fragmentPurchase.setArguments(bundle);
                }
                switchContent(fragmentPurchase, checkPosition, width);
                break;
            case R.id.radio_deal:
                checkPosition = 5;
                fragmentDeal = manager.findFragmentByTag("TAG5");
                if (fragmentDeal == null) {
                    fragmentDeal = new MyReportFragment();
                    bundle = new Bundle();
                    bundle.putInt("position", checkPosition);
                    fragmentDeal.setArguments(bundle);
                }
                switchContent(fragmentDeal, checkPosition, width);
                break;
            case R.id.radio_commission:
                checkPosition = 6;
                fragmentCommission = manager.findFragmentByTag("TAG6");
                if (fragmentCommission == null) {
                    fragmentCommission = new MyReportFragment();
                    bundle = new Bundle();
                    bundle.putInt("position", checkPosition);
                    fragmentCommission.setArguments(bundle);
                }
                switchContent(fragmentCommission, checkPosition, width);
                break;
            case R.id.radio_end:
                checkPosition = 7;
                fragmentEnd = manager.findFragmentByTag("TAG7");
                if (fragmentEnd == null) {
                    fragmentEnd = new MyReportFragment();
                    bundle = new Bundle();
                    bundle.putInt("position", checkPosition);
                    fragmentEnd.setArguments(bundle);
                }
                switchContent(fragmentEnd, checkPosition, width);
                break;
            default:
                break;
        }
        changeTextSize(checkPosition);
    }

    private void changeTextSize(int checkPosition) {
        if (currentPosition != checkPosition) {
            mRadioButton[currentPosition].setTextSize(12);
            mRadioButton[checkPosition].setTextSize(14);
            currentPosition = checkPosition;
        }
    }

    private void switchContent(Fragment fragment, int position, int width) {
        scrollView.smoothScrollTo(position * width, 0);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mFragment != fragment) {
            if (fragment.isAdded()) {
                transaction.hide(mFragment).show(fragment).commit();
            } else {
                switch (position) {
                    case 0:
                        transaction.hide(mFragment).add(R.id.fragment, fragment, "TAG0").commit();
                        break;
                    case 1:
                        transaction.hide(mFragment).add(R.id.fragment, fragment, "TAG1").commit();
                        break;
                    case 2:
                        transaction.hide(mFragment).add(R.id.fragment, fragment, "TAG2").commit();
                        break;
                    case 3:
                        transaction.hide(mFragment).add(R.id.fragment, fragment, "TAG3").commit();
                        break;
                    case 4:
                        transaction.hide(mFragment).add(R.id.fragment, fragment, "TAG4").commit();
                        break;
                    case 5:
                        transaction.hide(mFragment).add(R.id.fragment, fragment, "TAG5").commit();
                        break;
                    case 6:
                        transaction.hide(mFragment).add(R.id.fragment, fragment, "TAG6").commit();
                        break;
                    case 7:
                        transaction.hide(mFragment).add(R.id.fragment, fragment, "TAG7").commit();
                        break;
                    default:
                        break;
                }
            }
            mFragment = fragment;
        }
    }

}
