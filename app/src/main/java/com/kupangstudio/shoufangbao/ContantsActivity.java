package com.kupangstudio.shoufangbao;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.kupangstudio.shoufangbao.base.BaseFragmentActivity;
import com.kupangstudio.shoufangbao.fragment.CallLogFragment;
import com.kupangstudio.shoufangbao.fragment.ContantsChoseFragment;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * Created by Jsmi on 2015/11/13.
 * 选择联系人界面
 */
public class ContantsActivity extends BaseFragmentActivity {
    private static final String[] CONTENT = new String[] {"通话记录", "联系人" };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contants);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "选择联系人");

        FragmentPagerAdapter adapter = new MyAdapter(getSupportFragmentManager());

        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(adapter);

        TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        indicator.setFooterColor(getResources().getColor(R.color.common_select));
        indicator.setViewPager(pager);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    return new CallLogFragment();
                case 1:
                    return new ContantsChoseFragment();
            }

            return new CallLogFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length].toUpperCase();
        }


        @Override
        public int getCount() {
            return CONTENT.length;
        }
    }

}
