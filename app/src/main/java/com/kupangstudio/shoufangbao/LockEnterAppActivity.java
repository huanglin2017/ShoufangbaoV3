package com.kupangstudio.shoufangbao;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.base.ShoufangbaoApplication;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.utils.LockPatternUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.widget.LockPatternView;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Jsmi on 2015/12/2.
 * 通过锁屏进入APP页面
 */
public class LockEnterAppActivity extends BaseActivity implements LockPatternView.OnPatternListener {
    private LockPatternView mLockPatternView;
    private CountDownTimer mCountdownTimer = null;
    private Handler mHandler = new Handler();
    private TextView mHeadTextView;
    private TextView mForgetPassword;
    private Animation mShakeAnim;
    private int count = 0;
    private LockPatternUtils lockPatternUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockenter_app);
        initView();
        setClickListener();
        lockPatternUtils = new LockPatternUtils(LockEnterAppActivity.this);
    }
    private void initView() {
        mLockPatternView = (LockPatternView) this.findViewById(R.id.gesturepwd_unlock_lockview);
        mLockPatternView.setTactileFeedbackEnabled(true);
        mHeadTextView = (TextView) findViewById(R.id.gesturepwd_unlock_text);
        mForgetPassword = (TextView) findViewById(R.id.gesturepwd_unlock_forget);
        mShakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake_x);
    }
    private void setClickListener() {
        mLockPatternView.setOnPatternListener(this);
        mForgetPassword.setOnClickListener(mForgetListener);
    }


    private View.OnClickListener mForgetListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder dg = new AlertDialog.Builder(LockEnterAppActivity.this);
            dg.setTitle("忘记手势密码，需要重新登录");
            dg.setPositiveButton("重新登录", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {

                    User.logout();
                    lockPatternUtils.clearLock();
                    SPUtils.put(LockEnterAppActivity.this, Constants.IS_LOCK_OPEN, false);
                    //退出登录的刷新个人中心和客户界面
                    User user = User.getInstance();
                    EventBus.getDefault().post(user);

                    Intent it = new Intent();
                    it.setClassName(LockEnterAppActivity.this, LoginActivity.class.getName());
                    it.putExtra("isneedquit", true);
                    startActivity(it);

                    //清除自己
                    finish();
                }
            });
            dg.setNegativeButton("取消", null);
            dg.show();
        }
    };

    @Override
    public void onPatternStart() {
        if(count >= 5){

        }
    }

    @Override
    public void onPatternCleared() {

    }

    @Override
    public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

    }

    @Override
    public void onPatternDetected(List<LockPatternView.Cell> pattern) {
        if(pattern == null){
            Toast.makeText(LockEnterAppActivity.this, "获取结果有误", Toast.LENGTH_SHORT).show();
            return;
        }
        if(pattern.size() < 4) {
            //至少输入四个点
            mHeadTextView.setText("至少连接4个点，请重试");
            mLockPatternView.clearPattern();
            return;
        }
        int result = lockPatternUtils.checkPattern(pattern);
        if(result == 1){
            mHeadTextView.setText("解锁成功");
            Intent intent = new Intent(LockEnterAppActivity.this, MainActivity.class);
            // 打开新的Activity
            startActivity(intent);
            finish();
        }else {
            count++;
            int num = 5 - count;
            mHeadTextView.setTextColor(Color.RED);
            mHeadTextView.startAnimation(mShakeAnim);
            if(count >= 5){
                mHandler.postDelayed(attemptLockout, 2000);
                mLockPatternView.clearPattern();
            }else {
                mHandler.postDelayed(mClearPatternRunnable, 2000);
                mHeadTextView.setText("密码输入错误,还可以输入" + num + "次");
            }
        }
    }

    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };

    Runnable attemptLockout = new Runnable() {

        @Override
        public void run() {
            mLockPatternView.clearPattern();
            mLockPatternView.setEnabled(false);
            mCountdownTimer = new CountDownTimer(60000L, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    int secondsRemaining = (int) (millisUntilFinished / 1000) - 1;
                    if (secondsRemaining > 0) {
                        mHeadTextView.setText(secondsRemaining + " 秒后重试");
                    } else {
                        mHeadTextView.setText("请绘制手势密码");
                        mHeadTextView.setTextColor(getResources().getColor(R.color.lockpattern_text));
                    }

                }

                @Override
                public void onFinish() {
                    mLockPatternView.setEnabled(true);
                    count = 0;
                }
            }.start();
        }
    };
}
