package com.kupangstudio.shoufangbao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * Created by long1 on 15/11/30.
 * Copyright 15/11/30 android_xiaobai.
 */
public class PopListView extends ListView {

    public PopListView(Context context) {
        super(context);
    }

    public PopListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PopListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(!isInEditMode()) {
            int maxWidth = measureWidthByChilds() + getPaddingLeft() + getPaddingRight();
            super.onMeasure(MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public int measureWidthByChilds() {
        int maxWidth = 0;
        View view = null;
        for (int i = 0; i < getAdapter().getCount(); i++) {
            view = getAdapter().getView(i, view, this);
            view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            if (view.getMeasuredWidth() > maxWidth) {
                maxWidth = view.getMeasuredWidth();
            }
        }
        return maxWidth;
    }
}
