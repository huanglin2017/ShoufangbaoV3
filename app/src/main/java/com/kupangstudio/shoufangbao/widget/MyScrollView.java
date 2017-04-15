package com.kupangstudio.shoufangbao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

/**
 * Created by long1 on 16/3/9.
 * Copyright 16/3/9 android_xiaobai.
 */
public class MyScrollView extends ScrollView{

    private RelativeLayout mTopView;
    private int height;

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setmTopView(RelativeLayout mTopView) {
        this.mTopView = mTopView;
    }


    public void setGoneHeight(int height) {
        this.height = height;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if(mTopView != null){
            if(t >= height) {
                mTopView.setVisibility(VISIBLE);
            } else {
                mTopView.setVisibility(GONE);
            }
        }
    }
}
