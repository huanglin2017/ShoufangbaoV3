package com.kupangstudio.shoufangbao.animation;

import android.app.Activity;
import android.view.animation.Animation;

import com.kupangstudio.shoufangbao.PersonOrderActivity;
import com.kupangstudio.shoufangbao.base.Constants;

public class DisplayNextView implements Animation.AnimationListener {

    Object obj;

    // 动画监听器的构造函数
    Activity ac;
    int order;

    public DisplayNextView(Activity ac, int order) {
        this.ac = ac;
        this.order = order;
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onAnimationEnd(Animation animation) {
        doSomethingOnEnd(order);
    }

    public void onAnimationRepeat(Animation animation) {
    }

    private final class SwapViews implements Runnable {
        public void run() {
            switch (order) {
                case Constants.KEY_SECONDPAGE:
                    ((PersonOrderActivity) ac).removeLayout();
                    break;
            }
        }
    }

    public void doSomethingOnEnd(int _order) {
        switch (_order) {

            case Constants.KEY_SECONDPAGE:
                ((PersonOrderActivity) ac).layout_parent.post(new SwapViews());
                break;
        }
    }
}
