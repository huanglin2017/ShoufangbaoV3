package com.kupangstudio.shoufangbao.animation;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import com.kupangstudio.shoufangbao.PersonOrderActivity;

public class ScaleAnimationHelper {
    Context con;
    int order;

    public ScaleAnimationHelper(Context con, int order) {
        this.con = con;
        this.order = order;
    }

    DisplayNextView listener;
    ScaleAnimation myAnimation_Scale;
    //放大的类,不需要设置监听器
    public void ScaleOutAnimation(View view) {
        myAnimation_Scale = new ScaleAnimation(0.1f, 1.0f, 0.1f, 1f,
                Animation.RELATIVE_TO_SELF, 0.9f, Animation.RELATIVE_TO_SELF,
                0.1f);
        myAnimation_Scale.setInterpolator(new AccelerateInterpolator());
        AnimationSet aa = new AnimationSet(true);
        aa.addAnimation(myAnimation_Scale);
        aa.setDuration(500);

        view.startAnimation(aa);
    }

    public void ScaleInAnimation(View view) {
        listener = new DisplayNextView((PersonOrderActivity) con, order);
        myAnimation_Scale = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.92f, Animation.RELATIVE_TO_SELF,
                0.04f);
        myAnimation_Scale.setInterpolator(new AccelerateInterpolator());
        //缩小Layout的类,在动画结束需要从父View移除它
        myAnimation_Scale.setAnimationListener(listener);
        AnimationSet aa = new AnimationSet(true);
        aa.addAnimation(myAnimation_Scale);
        aa.setDuration(500);

        view.startAnimation(aa);
    }
}

