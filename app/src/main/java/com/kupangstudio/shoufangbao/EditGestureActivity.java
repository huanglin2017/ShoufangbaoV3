package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;

/**
 * Created by Jsmi on 2015/12/2.
 * 手势密码关闭以及修改界面
 */
public class EditGestureActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private CheckBox isOpenGesture;
    private LinearLayout rePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_gesture);
        initView();
        setClickListener();
        isOpenGesture.setChecked(true);
        CommonUtils.handleTitleBarRightGone(EditGestureActivity.this, "手势密码");

    }

    private void initView() {
        isOpenGesture = (CheckBox)findViewById(R.id.lock_checkbox_pass);
        rePassword = (LinearLayout)findViewById(R.id.lock_repassword);
    }

    private void setClickListener() {
        rePassword.setOnClickListener(this);
        isOpenGesture.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(EditGestureActivity.this, NewOrEditLockActivity.class);
        intent.putExtra("option", "repass");
        startActivity(intent);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
            SPUtils.put(EditGestureActivity.this, Constants.IS_LOCK_OPEN, true);
            rePassword.setVisibility(View.VISIBLE);
        }else {
            SPUtils.put(EditGestureActivity.this, Constants.IS_LOCK_OPEN, false);
            rePassword.setVisibility(View.GONE);
        }
    }
}
