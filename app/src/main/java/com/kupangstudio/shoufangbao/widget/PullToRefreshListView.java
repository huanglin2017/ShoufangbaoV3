package com.kupangstudio.shoufangbao.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.OverscrollHelper;
import com.handmark.pulltorefresh.library.PullToRefreshAdapterViewBase;
import com.handmark.pulltorefresh.library.internal.EmptyViewMethodAccessor;
/**
 * Created by Jsmi on 2015/11/9.
 * 下拉刷新
 */

public class PullToRefreshListView extends PullToRefreshAdapterViewBase<ListView>{

    private View view;

    public PullToRefreshListView(Context context) {
        super(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshListView(Context context, Mode mode) {
        super(context, mode);
    }

    public PullToRefreshListView(Context context, Mode mode, AnimationStyle style) {
        super(context, mode, style);
    }

    @Override
    public final Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    protected ListView createListView(Context context, AttributeSet attrs) {
        final ListView lv;
        if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
            lv = new InternalListViewSDK9(context, attrs);
        } else {
            lv = new InternalListView(context, attrs);
        }
        return lv;
    }

    @Override
    protected ListView createRefreshableView(Context context, AttributeSet attrs) {
        ListView lv = createListView(context, attrs);
        // Set it to this so it can be used in ListActivity/ListFragment
        lv.setId(android.R.id.list);
        return lv;
    }


    @TargetApi(9)
    final class InternalListViewSDK9 extends InternalListView {

        public InternalListViewSDK9(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX,
                                       int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

            final boolean returnValue = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
                    scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);

            // Does all of the hard work...
            OverscrollHelper.overScrollBy(PullToRefreshListView.this, deltaX, scrollX, deltaY, scrollY, isTouchEvent);

            return returnValue;
        }
    }

    protected class InternalListView extends ListView implements EmptyViewMethodAccessor {

        public InternalListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            /**
             * This is a bit hacky, but Samsung's ListView has got a bug in it
             * when using Header/Footer Views and the list is empty. This masks
             * the issue so that it doesn't cause an FC. See Issue #66.
             */
            try {
                super.dispatchDraw(canvas);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            /**
             * This is a bit hacky, but Samsung's ListView has got a bug in it
             * when using Header/Footer Views and the list is empty. This masks
             * the issue so that it doesn't cause an FC. See Issue #66.
             */
            try {
                return super.dispatchTouchEvent(ev);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void setAdapter(ListAdapter adapter) {
            // Add the Footer View at the last possible moment
            //		addFooterView(view, null, false);
            super.setAdapter(adapter);
        }

        @Override
        public void setEmptyView(View emptyView) {
            PullToRefreshListView.this.setEmptyView(emptyView);
        }

        @Override
        public void setEmptyViewInternal(View emptyView) {
            super.setEmptyView(emptyView);
        }

    }


    public void addFooterView(View view){
        this.view=view;
    }
}



