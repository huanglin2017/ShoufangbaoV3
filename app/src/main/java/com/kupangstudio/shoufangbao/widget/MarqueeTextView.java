package com.kupangstudio.shoufangbao.widget;

/**
 * Created by Administrator on 2015/9/17.
 */
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.TextView;

/**
 * Created by long on 15/9/16.
 * Copyright 2015 android_xiaobai.
 */


public class MarqueeTextView extends TextView {
    /** 是否停止滚动 */
    private boolean mStopMarquee;
    private String mText;
    private float mCoordinateX;
    private float mTextWidth;
    private float windowWith;
    private float mTextHeight;


    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void setText(String text) {
        this.mText = text;
        mTextWidth = getPaint().measureText(mText);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Rect mBounds = new Rect();
        getPaint().getTextBounds(mText,0,mText.length(),mBounds);
        mTextHeight = mBounds.height();
        windowWith = displayMetrics.widthPixels;
        if (mHandler.hasMessages(0))
            mHandler.removeMessages(0);
        mHandler.sendEmptyMessageDelayed(0, 2000);

    }


    @SuppressLint("NewApi")
    @Override
    protected void onAttachedToWindow() {
        mStopMarquee = false;
        if (!(mText == null || mText.isEmpty()))
            mHandler.sendEmptyMessageDelayed(0, 2000);
        super.onAttachedToWindow();
    }

    public void stopMarquee(boolean mStopMarquee){
        this.mStopMarquee = mStopMarquee;
    }

    @Override
    protected void onDetachedFromWindow() {
        mStopMarquee = true;
        if (mHandler.hasMessages(0))
            mHandler.removeMessages(0);
        super.onDetachedFromWindow();
    }



    @SuppressLint("NewApi")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!(mText == null || mText.isEmpty()))
            canvas.drawText(mText, mCoordinateX, getHeight()/2+mTextHeight/2, getPaint());
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if(!mStopMarquee){
                        if(mCoordinateX < 0 && Math.abs(mCoordinateX) > mTextWidth){
                            mCoordinateX = windowWith;
                        }else{
                            mCoordinateX -= 3;
                        }
                        invalidate();
                        sendEmptyMessageDelayed(0,30);
                    }
                    break;

            }
        }
    };
}
