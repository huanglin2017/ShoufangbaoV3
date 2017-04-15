package com.kupangstudio.shoufangbao.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ListView;
import android.widget.ScrollView;

/**
 * Created by Administrator on 2016/3/7.
 */
public class ScrollListView extends ListView implements Pullable{
    public ScrollListView(Context context) {
        super(context);
    }

    public ScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public boolean canPullDown()
    {
        if (getScrollY() == 0)
            return true;
        else
            return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE>>2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    @Override
    public boolean canPullUp()
    {
        if (getScrollY() >= (getChildAt(0).getHeight() - getMeasuredHeight()))
            return true;
        else
            return false;
    }

    // Edge-effects don't mix well with the translucent action bar in Android 2.X
   /* private boolean mDisableEdgeEffects = true;

    *//**
     * @author Cyril Mottier
     *//*
    public interface OnScrollChangedListener {
        void onScrollChanged(ListView who, int l, int t, int oldl, int oldt);
    }

    private OnScrollChangedListener mOnScrollChangedListener;

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedListener != null) {
            mOnScrollChangedListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mOnScrollChangedListener = listener;
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        if (mDisableEdgeEffects && Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return 0.0f;
        }
        return super.getTopFadingEdgeStrength();
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        if (mDisableEdgeEffects && Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return 0.0f;
        }
        return super.getBottomFadingEdgeStrength();
    }*/
}
