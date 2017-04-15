package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.model.BuildLayout;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by long1 on 15/11/13.
 * Copyright 15/11/13 android_xiaobai.
 * 户型图详情
 */
public class LayoutPicDetailActivity extends BaseActivity {

    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private ViewPager pager;
    private ArrayList<BuildLayout> list;
    private ArrayList<BuildLayout> layouts = new ArrayList<BuildLayout>();
    private ArrayList<String> url = new ArrayList<>();
    private static final String CURRENT_POSITION = "CURRENT_POSITION";
    private MyAdapter adapter;
    private TextView tvLayout;
    private TextView tvSize;
    private TextView tvPrice;
    private TextView tvYouhui;
    private TextView tvIntroduction;
    private int currentPos;
    private int layoutPos;
    private LinearLayout layoutIntro;
    private TextView tvNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_pic_detail);
        CommonUtils.addActivity(this);
        CommonUtils.addActivity(this);
        initView();
        init();
    }

    private void init() {
        list = (ArrayList<BuildLayout>) getIntent().getSerializableExtra("layoutList");
        currentPos = getIntent().getIntExtra("position", 0);
        layoutPos = getIntent().getIntExtra("layoutPos", 0);
        for (int i = 0; i < list.size(); i++) {
            BuildLayout buildLayout = list.get(i);
            List<String> list = buildLayout.getPics();
            for (int j = 0; j < list.size(); j++) {
                url.add(list.get(j));
                layouts.add(buildLayout);
            }
        }
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).build();
        CommonUtils.handleTitleBarRightGone(LayoutPicDetailActivity.this, "户型图");
        BuildLayout buildLayout = layouts.get(currentPos);
        tvLayout.setText(buildLayout.getLayout());
        tvPrice.setText(buildLayout.getPrice());
        tvSize.setText("（"+buildLayout.getSize()+"）");
        tvYouhui.setText(buildLayout.getDiscount());
        if (buildLayout.getRemark() == null || buildLayout.getRemark().equals("")) {
            layoutIntro.setVisibility(View.GONE);
        } else {
            tvIntroduction.setText(buildLayout.getRemark());
        }
        if (adapter == null) {
            adapter = new MyAdapter(url);
            pager.setAdapter(adapter);
            pager.setCurrentItem(currentPos);
            tvNum.setText(currentPos + 1 + "/" + url.size());
        } else {
            adapter.url = url;
            adapter.notifyDataSetChanged();
        }
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                BuildLayout buildLayout = layouts.get(position);
                tvLayout.setText(buildLayout.getTitle());
                tvPrice.setText(buildLayout.getPrice());
                tvSize.setText("（" + buildLayout.getSize() + "）");
                tvYouhui.setText(buildLayout.getDiscount());
                tvIntroduction.setText(buildLayout.getRemark());
                tvNum.setText(position + 1 + "/" + url.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initView() {
        pager = (ViewPager) findViewById(R.id.pic_detail_pager);
        tvLayout = (TextView) findViewById(R.id.tv_layout);
        tvSize = (TextView) findViewById(R.id.tv_size);
        tvPrice = (TextView) findViewById(R.id.tv_price);
        tvYouhui = (TextView) findViewById(R.id.tv_youhui);
        tvIntroduction = (TextView) findViewById(R.id.tv_introduction);
        layoutIntro = (LinearLayout) findViewById(R.id.layout_intro);
        tvNum = (TextView) findViewById(R.id.layout_num);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_POSITION, pager.getCurrentItem());
    }

    class MyAdapter extends PagerAdapter {

        ArrayList<String> url;

        public MyAdapter(ArrayList<String> url) {
            this.url = url;
        }

        @Override
        public int getCount() {
            return url.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            imageLoader.displayImage(url.get(position), photoView, options);
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    Intent intent = new Intent(LayoutPicDetailActivity.this,
                            BigLayoutPicActivity.class);
                    intent.putExtra("currentPos", position);
                    intent.putExtra("buildLayout", layouts);
                    intent.putExtra("url",url);
                    startActivity(intent);
                }
            });
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
