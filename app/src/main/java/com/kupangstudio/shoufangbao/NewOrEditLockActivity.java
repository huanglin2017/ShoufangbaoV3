package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.LockPatternUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.widget.LockPatternView;

import java.util.List;

/**
 * Created by Jsmi on 2015/12/2.
 * 创建修改手势密码
 */
public class NewOrEditLockActivity extends BaseActivity{

    private TextView title;
    private LockPatternView lockPatternView;
    private LockPatternUtils lockPatternUtils;
    private boolean isFirst;
    private String option;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neworedit_lock);
        initView();
        Intent intent = getIntent();
        option = intent.getStringExtra("option");
        lockPatternUtils = new LockPatternUtils(NewOrEditLockActivity.this);
        SPUtils.put(NewOrEditLockActivity.this, Constants.IS_LOCK_SET, false);
        isFirst = true;
        if(option.equals("new")){
            CommonUtils.handleTitleBarRightGone(NewOrEditLockActivity.this, "创建手势密码");
        }else {
            CommonUtils.handleTitleBarRightGone(NewOrEditLockActivity.this, "修改手势密码");
        }

        lockPatternView.setOnPatternListener(new LockPatternView.OnPatternListener() {
            @Override
            public void onPatternStart() {

            }

            @Override
            public void onPatternCleared() {

            }

            @Override
            public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

            }

            @Override
            public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                if(pattern.size() < 4) {
                    //至少输入四个点
                    title.setText("至少连接4个点，请重试");
                    lockPatternView.clearPattern();
                    return;
                }

                if(isFirst){
                    lockPatternUtils.saveFirstLockPattern(pattern);
                    title.setText("请再次绘制解锁图案");
                    isFirst = false;
                }else {
                    int result = lockPatternUtils.checkPattern(pattern);
                    if(result == 1){
                        title.setText("解锁图案已经保存");
                        lockPatternUtils.clearLock();
                        lockPatternUtils.saveLockPattern(pattern);
                        SPUtils.put(NewOrEditLockActivity.this, Constants.IS_LOCK_OPEN, true);
                        if(option.equals("new")){
                            Toast.makeText(NewOrEditLockActivity.this, "手势密码成功设置", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(NewOrEditLockActivity.this, "手势密码成功修改", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                        //设置成功
                    }else if(result == 0){
                        title.setText("与上次输入不一致，请重试");
                        title.setTextColor(Color.RED);
                        //两次输入不一致
                    }else {
                        finish();
                        Toast.makeText(NewOrEditLockActivity.this, "锁屏暂时有点问题，请稍后重试", Toast.LENGTH_SHORT).show();
                        //锁屏暂时有点问题，稍后重试
                    }
                }
                lockPatternView.clearPattern();
            }
        });
    }

    private void initView() {
        title = (TextView)findViewById(R.id.lockpattern_title);
        lockPatternView = (LockPatternView)findViewById(R.id.lockpattern_lock);
    }
}
