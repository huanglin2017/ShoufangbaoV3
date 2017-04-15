package com.kupangstudio.shoufangbao;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.model.BuildLayout;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

import static com.kupangstudio.shoufangbao.R.id.tv_title;

/**
 * Created by long1 on 16/3/15.
 * Copyright 16/3/15 android_xiaobai.
 * 户型图大图页面
 */
public class BigLayoutPicActivity extends BaseActivity implements View.OnClickListener {

    private TextView tvNum;
    private ViewPager viewPager;
    private ImageView ivBack;
    private TextView tvTitle;
    private int currentPos;
    private ArrayList<BuildLayout> list;
    private ArrayList<String> url;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_layout);
        CommonUtils.addActivity(this);
        initView();
        currentPos = getIntent().getIntExtra("currentPos", 0);
        list = (ArrayList<BuildLayout>) getIntent().getSerializableExtra("buildLayout");
        url = getIntent().getStringArrayListExtra("url");
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).build();
        ivBack.setOnClickListener(this);
        adapter = new MyAdapter(url);
        tvTitle.setText(list.get(currentPos).getTitle());
        tvNum.setText(currentPos + 1 + "/" + url.size());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPos);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                BuildLayout buildLayout = list.get(position);
                tvTitle.setText(buildLayout.getTitle());
                tvNum.setText(position + 1 + "/" + url.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initView() {
        tvNum = (TextView) findViewById(R.id.tv_num);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        tvTitle = (TextView) findViewById(tv_title);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
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
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
