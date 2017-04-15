package com.kupangstudio.shoufangbao;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.model.BuildDetail;
import com.kupangstudio.shoufangbao.model.BuildImage;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by long1 on 15/11/14.
 * Copyright 15/11/14 android_xiaobai.
 */
public class BuildImageActivity extends BaseActivity {

    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private static final String STATE_POSITION = "STATE_POSITION";
    private TextView tv;
    private ArrayList<BuildImage> list;
    private ArrayList<String> imagePicses;
    private ArrayList<String> eList;
    private ArrayList<String> sList;
    private ArrayList<String> hList;
    private ArrayList<String> gList;
    private ArrayList<String> pList;
    private MyAdapter adapter;
    private ViewPager pager;
    private TextView tvEffect;
    private TextView tvShijing;
    private TextView tvGuihua;
    private TextView tvHuxing;
    private TextView tvPeitao;
    private boolean isEffect = true;
    private boolean isShijing = true;
    private boolean isGuihua = true;
    private boolean isHuxing = true;
    private boolean isPeitao = true;
    private int effect;
    private int shijing;
    private int guihua;
    private int huxing;
    private int peitao;
    private LinearLayout save;//保存
    private LinearLayout share;//分享
    private PhotoView photoView;
    private BuildDetail hb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_image);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "图片预览");
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).build();
        list = (ArrayList<BuildImage>) getIntent().getSerializableExtra("pList");
        hb = (BuildDetail) getIntent().getSerializableExtra("hb");
        imagePicses = new ArrayList<>();
        eList = new ArrayList<>();//效果图
        sList = new ArrayList<>();//实景图
        hList = new ArrayList<>();//户型图
        gList = new ArrayList<>();//规划图
        pList = new ArrayList<>();//配套图
        for (int i = 0; i < list.size(); i++) {
            BuildImage h = list.get(i);
            if (h.getType() == BuildDetail.EFFECT) {
                eList.add(h.getUrl());
            } else if (h.getType() == BuildDetail.SHIJING) {
                sList.add(h.getUrl());
            } else if (h.getType() == BuildDetail.GUIHUA) {
                gList.add(h.getUrl());
            } else if (h.getType() == BuildDetail.HOUSEMODEL) {
                hList.add(h.getUrl());
            } else {
                pList.add(h.getUrl());
            }
        }
        if (eList.size() > 0) {
            for (String s : eList) {
                imagePicses.add(s);
            }
        }
        if (sList.size() > 0) {
            for (String s : sList) {
                imagePicses.add(s);
            }
        }
        if (gList.size() > 0) {
            for (String s : gList) {
                imagePicses.add(s);
            }
        }
        if (hList.size() > 0) {
            for (String s : hList) {
                imagePicses.add(s);
            }
        }
        if (pList.size() > 0) {
            for (String s : pList) {
                imagePicses.add(s);
            }
        }
        initView();
        if (adapter == null) {
            adapter = new MyAdapter(imagePicses);
            pager.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        pager.setCurrentItem(0);
        tv.setText(1 + "/" + imagePicses.size());
        tvEffect.setOnClickListener(listener);
        tvShijing.setOnClickListener(listener);
        tvGuihua.setOnClickListener(listener);
        tvHuxing.setOnClickListener(listener);
        tvPeitao.setOnClickListener(listener);
        save.setOnClickListener(listener);
        share.setOnClickListener(listener);
        effect = eList.size();
        shijing = sList.size();
        guihua = gList.size();
        huxing = hList.size();
        peitao = pList.size();
        if (effect == 0) {
            tvEffect.setTextColor(Color.parseColor("#cccccc"));
            isEffect = false;
            if (shijing != 0) {
                tvShijing.setTextColor(Color.parseColor("#be1a20"));
            } else if (guihua != 0) {
                tvGuihua.setTextColor(Color.parseColor("#be1a20"));
            } else if (huxing != 0) {
                tvHuxing.setTextColor(Color.parseColor("#be1a20"));
            } else if (peitao != 0) {
                tvPeitao.setTextColor(Color.parseColor("#be1a20"));
            }
        } else {
            tvEffect.setTextColor(Color.parseColor("#be1a20"));
        }
        if (shijing == 0) {
            tvShijing.setTextColor(Color.parseColor("#cccccc"));
            isShijing = false;
        }
        if (guihua == 0) {
            tvGuihua.setTextColor(Color.parseColor("#cccccc"));
            isGuihua = false;
        }
        if (huxing == 0) {
            tvHuxing.setTextColor(Color.parseColor("#cccccc"));
            isHuxing = false;
        }
        if (peitao == 0) {
            tvPeitao.setTextColor(Color.parseColor("#cccccc"));
            isPeitao = false;
        }
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tv.setText(position + 1 + "/" + imagePicses.size());
                if (isEffect) {
                    if (position == 0 || position == effect - 1) {
                        setEffect();
                    }
                    if (isShijing) {
                        if (position == effect || position == effect + shijing - 1) {
                            setShijing();
                        }
                    }
                    if (isGuihua) {
                        if (position == effect + shijing || position == effect + shijing + guihua - 1) {
                            setGuihua();
                        }
                    }
                    if (isHuxing) {
                        if (position == effect + shijing + guihua || position == effect + shijing + guihua + huxing - 1) {
                            setHuxing();
                        }
                    }
                    if (isPeitao) {
                        if (position == effect + shijing + guihua + huxing || position == list.size() - 1) {
                            setPeitao();
                        }
                    }
                } else {
                    if (position == 0 || position == shijing - 1) {
                        setShijing();
                    }
                    if (isGuihua) {
                        if (position == effect + shijing || position == effect + shijing + guihua - 1) {
                            setGuihua();
                        }
                    }
                    if (isHuxing) {
                        if (position == effect + shijing + guihua || position == effect + shijing + guihua + huxing - 1) {
                            setHuxing();
                        }
                    }
                    if (isPeitao) {
                        if (position == effect + shijing + guihua + huxing || position == list.size() - 1) {
                            setPeitao();
                        }
                    }
                }

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buildimage_effecttv:
                    if (!isEffect) {
                        return;
                    }
                    pager.setCurrentItem(0);
                    setEffect();
                    break;
                case R.id.buildimage_shijingtv:
                    if (!isShijing) {
                        return;
                    }
                    pager.setCurrentItem(effect);
                    setShijing();
                    break;
                case R.id.buildimage_guihuatv:
                    if (!isGuihua) {
                        return;
                    }
                    pager.setCurrentItem(effect + shijing);
                    setGuihua();
                    break;
                case R.id.buildimage_huxingtv:
                    if (!isHuxing) {
                        return;
                    }
                    pager.setCurrentItem(effect + shijing + guihua);
                    setHuxing();
                    break;
                case R.id.buildimage_peitaotv:
                    if (!isPeitao) {
                        return;
                    }
                    pager.setCurrentItem(effect + shijing + guihua + huxing);
                    setPeitao();
                    break;
                case R.id.buildimage_save:
                    BitmapDrawable drawable = (BitmapDrawable) photoView.getDrawable();
                    Bitmap image = drawable.getBitmap();
                    CommonUtils.saveImageToGallery(BuildImageActivity.this, image);
                    break;
                case R.id.buildimage_share:
                    User user = User.getInstance();
                    if (user.userType != User.TYPE_NORMAL_USER) {
                        CommonUtils.showRegDialog(BuildImageActivity.this, "请您先登录", 0);
                    } else {
                        showShare();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        oks.setTitle("楼盘图片");
        oks.setText("【售房宝】");
        oks.setImageUrl(hb.getBuildPic().get(0).getUrl());
        oks.setTitleUrl(hb.getBuildPic().get(0).getUrl());
        oks.setSite(getString(R.string.app_name));
        oks.setDialogMode();
        oks.show(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_POSITION, pager.getCurrentItem());
    }

    private void initView() {
        tv = (TextView) findViewById(R.id.buildimage_num);
        pager = (ViewPager) findViewById(R.id.buildimage_pager);
        tvEffect = (TextView) findViewById(R.id.buildimage_effecttv);
        tvShijing = (TextView) findViewById(R.id.buildimage_shijingtv);
        tvGuihua = (TextView) findViewById(R.id.buildimage_guihuatv);
        tvHuxing = (TextView) findViewById(R.id.buildimage_huxingtv);
        tvPeitao = (TextView) findViewById(R.id.buildimage_peitaotv);
        save = (LinearLayout) findViewById(R.id.buildimage_save);
        share = (LinearLayout) findViewById(R.id.buildimage_share);
    }

    private void setEffect() {
        tvEffect.setTextColor(Color.parseColor("#be1a20"));
        if (isShijing) {
            tvShijing.setTextColor(Color.parseColor("#323232"));
        } else {
            tvShijing.setTextColor(Color.parseColor("#cccccc"));
        }
        if (isGuihua) {
            tvGuihua.setTextColor(Color.parseColor("#323232"));
        } else {
            tvGuihua.setTextColor(Color.parseColor("#cccccc"));
        }
        if (isHuxing) {
            tvHuxing.setTextColor(Color.parseColor("#323232"));
        } else {
            tvHuxing.setTextColor(Color.parseColor("#cccccc"));
        }
        if (isPeitao) {
            tvPeitao.setTextColor(Color.parseColor("#323232"));
        } else {
            tvPeitao.setTextColor(Color.parseColor("#cccccc"));
        }
    }

    private void setShijing() {
        if (isEffect) {
            tvEffect.setTextColor(Color.parseColor("#323232"));
        } else {
            tvEffect.setTextColor(Color.parseColor("#cccccc"));
        }
        tvShijing.setTextColor(Color.parseColor("#be1a20"));
        if (isGuihua) {
            tvGuihua.setTextColor(Color.parseColor("#323232"));
        } else {
            tvGuihua.setTextColor(Color.parseColor("#cccccc"));
        }
        if (isHuxing) {
            tvHuxing.setTextColor(Color.parseColor("#323232"));
        } else {
            tvHuxing.setTextColor(Color.parseColor("#cccccc"));
        }
        if (isPeitao) {
            tvPeitao.setTextColor(Color.parseColor("#323232"));
        } else {
            tvPeitao.setTextColor(Color.parseColor("#cccccc"));
        }
    }

    private void setGuihua() {
        if (isEffect) {
            tvEffect.setTextColor(Color.parseColor("#323232"));
        } else {
            tvEffect.setTextColor(Color.parseColor("#cccccc"));
        }
        if (isShijing) {
            tvShijing.setTextColor(Color.parseColor("#323232"));
        } else {
            tvShijing.setTextColor(Color.parseColor("#cccccc"));
        }
        tvGuihua.setTextColor(Color.parseColor("#be1a20"));
        if (isHuxing) {
            tvHuxing.setTextColor(Color.parseColor("#323232"));
        } else {
            tvHuxing.setTextColor(Color.parseColor("#cccccc"));
        }
        if (isPeitao) {
            tvPeitao.setTextColor(Color.parseColor("#323232"));
        } else {
            tvPeitao.setTextColor(Color.parseColor("#cccccc"));
        }
    }

    private void setHuxing() {
        if (isEffect) {
            tvEffect.setTextColor(Color.parseColor("#323232"));
        } else {
            tvEffect.setTextColor(Color.parseColor("#cccccc"));
        }
        if (isShijing) {
            tvShijing.setTextColor(Color.parseColor("#323232"));
        } else {
            tvShijing.setTextColor(Color.parseColor("#cccccc"));
        }
        if (isGuihua) {
            tvGuihua.setTextColor(Color.parseColor("#323232"));
        } else {
            tvGuihua.setTextColor(Color.parseColor("#cccccc"));
        }
        tvHuxing.setTextColor(Color.parseColor("#be1a20"));
        if (isPeitao) {
            tvPeitao.setTextColor(Color.parseColor("#323232"));
        } else {
            tvPeitao.setTextColor(Color.parseColor("#cccccc"));
        }
    }

    private void setPeitao() {
        if (isEffect) {
            tvEffect.setTextColor(Color.parseColor("#323232"));
        } else {
            tvEffect.setTextColor(Color.parseColor("#cccccc"));
        }
        if (isShijing) {
            tvShijing.setTextColor(Color.parseColor("#323232"));
        } else {
            tvShijing.setTextColor(Color.parseColor("#cccccc"));
        }
        if (isGuihua) {
            tvGuihua.setTextColor(Color.parseColor("#323232"));
        } else {
            tvGuihua.setTextColor(Color.parseColor("#cccccc"));
        }
        if (isHuxing) {
            tvHuxing.setTextColor(Color.parseColor("#323232"));
        } else {
            tvHuxing.setTextColor(Color.parseColor("#cccccc"));
        }
        tvPeitao.setTextColor(Color.parseColor("#be1a20"));
    }

    class MyAdapter extends PagerAdapter {

        ArrayList<String> list;

        public MyAdapter(ArrayList<String> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            photoView = new PhotoView(container.getContext());
            imageLoader.displayImage(list.get(position), photoView, options);
            container.addView(photoView, RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0.equals(arg1);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
