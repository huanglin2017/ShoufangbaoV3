package com.kupangstudio.shoufangbao;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.BaseFragmentActivity;
import com.kupangstudio.shoufangbao.base.ShoufangbaoApplication;
import com.kupangstudio.shoufangbao.fragment.PointInComeFragment;
import com.kupangstudio.shoufangbao.fragment.PointOutofFragment;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jsmi on 2015/11/25.
 * 积分记录
 */
public class MyPointsActivity extends BaseFragmentActivity{
    private Button comein;//进账记录
    private Button goout;//出账记录
    private PointInComeFragment inComeFragment;//进账记录
    private PointOutofFragment outOfFragment;//出账记录
    private Fragment mContent;
    private TextView point;
    private long beginTime;
    private long endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypoints);
        CommonUtils.addActivity(this);
        beginTime = System.currentTimeMillis();
        CommonUtils.handleTitleBarRightGone(this, "我的积分");
        initview();
        setClickListener();
        //进入页面2个按钮初始化
        comein.setSelected(true);
        goout.setSelected(false);
        inComeFragment = new PointInComeFragment();
        outOfFragment = new PointOutofFragment();
        mContent = inComeFragment;
        getSupportFragmentManager().beginTransaction().add(R.id.mypoint_frame, inComeFragment).commitAllowingStateLoss();
    }

    private void initview() {
        comein = (Button) findViewById(R.id.come_in_record);
        goout = (Button) findViewById(R.id.go_out_record);
        point = (TextView) findViewById(R.id.point);
    }

    private void setClickListener() {
        comein.setOnClickListener(listener);
        goout.setOnClickListener(listener);
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
        point.setText(User.getInstance().interage+"");
    }

    @Override
    protected void onStop() {
        super.onStop();
        endTime = System.currentTimeMillis();
        int duration = (int)((endTime-beginTime)/1000);
        Map<String, String> map_value = new HashMap<String, String>();
        MobclickAgent.onEventValue(this, "myjinfen", map_value, duration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.come_in_record://进账记录
                    comein.setSelected(true);
                    goout.setSelected(false);
                    switchContent(mContent,inComeFragment);
                    break;
                case R.id.go_out_record://出账记录
                    comein.setSelected(false);
                    goout.setSelected(true);
                    switchContent(mContent,outOfFragment);
                    break;
            }
        }
    };

    private void switchContent(Fragment from, Fragment to) {
        if (mContent != to) {
            mContent = to;
            FragmentTransaction tran = getSupportFragmentManager()
                    .beginTransaction();
            if (!to.isAdded()) {
                tran.hide(from).add(R.id.mypoint_frame, to);
            } else {
                tran.hide(from).show(to);
            }
            tran.commitAllowingStateLoss();
        }
    }
}
