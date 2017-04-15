package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseFragmentActivity;
import com.kupangstudio.shoufangbao.fragment.CustomDetailFragment;
import com.kupangstudio.shoufangbao.fragment.CustomFollowFragment;
import com.kupangstudio.shoufangbao.fragment.CustomReportFragment;
import com.kupangstudio.shoufangbao.model.Custom;
import com.kupangstudio.shoufangbao.utils.CommonUtils;

import de.greenrobot.event.EventBus;

/**
 * Created by Jsmi on 2015/11/4.
 * 客户详情
 */
public class CustomDetailActivity extends BaseFragmentActivity implements View.OnClickListener {
    private TextView mCustomDetailTitle;// titlebar
    private ImageView mCustomDetailLeft;
    private TextView mCustomDetailRight;// titlebar
    private Button mDetail;
    private Button mFollow;
    private Button mReport;
    private Fragment mFollowFragment;
    private Fragment mDetailFragment;
    private Fragment mReportFragment;
    private Fragment mContent;
    private Custom cus;
    private boolean Flag = false;
    private int RESULT_EDIT_CUSTOMER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_detail);
        CommonUtils.addActivity(this);
        initView();
        setClickListener();
        mCustomDetailTitle.setText("客户详情");
        Intent intent = getIntent();
        cus = (Custom)intent.getSerializableExtra("custom");
        mFollowFragment = new CustomFollowFragment(cus);
        mDetailFragment = new CustomDetailFragment(cus);
        mReportFragment = new CustomReportFragment(cus);
        mContent = mFollowFragment;
        setSelect(true, false, false, false);
        FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
        tran.add(R.id.custom_detail_fl, mContent).commit();
    }

    private void initView() {
        mCustomDetailLeft = (ImageView) findViewById(R.id.customdetail_image_left);
        mCustomDetailRight = (TextView) findViewById(R.id.customdetail_image_right);
        mCustomDetailTitle = (TextView) findViewById(R.id.customdetail_title);
        mDetail = (Button) findViewById(R.id.custom_detail_detail);
        mFollow = (Button) findViewById(R.id.custom_detail_follow);
        mReport = (Button) findViewById(R.id.custom_detail_report);
    }

    private void setClickListener() {
        mCustomDetailRight.setOnClickListener(this);
        mCustomDetailLeft.setOnClickListener(this);
        mDetail.setOnClickListener(this);
        mFollow.setOnClickListener(this);
        mReport.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.customdetail_image_left://回退键
                finish();
                break;

            case R.id.customdetail_image_right://跳转编辑信息页面
                if(!Flag){
                    Intent intent = new Intent(CustomDetailActivity.this, EditCustomActivity.class);
                    intent.putExtra("editmode", true);
                    intent.putExtra("customdata", cus);
                    startActivityForResult(intent, RESULT_EDIT_CUSTOMER);
                }else{
                    Toast.makeText(CustomDetailActivity.this, "数据加载中，请稍候", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.custom_detail_detail://选择详情页面
                setSelect(false, true, false, true);
                mContent = CommonUtils.switchFragmentContent(mContent, mContent, mDetailFragment,
                        R.id.custom_detail_fl, CustomDetailActivity.this);
                break;

            case R.id.custom_detail_follow://选择跟进页面
                setSelect(true, false, false, false);
                mContent = CommonUtils.switchFragmentContent(mContent, mContent, mFollowFragment,
                        R.id.custom_detail_fl, CustomDetailActivity.this);
                break;

            case R.id.custom_detail_report://选择报备页面
                setSelect(false, false, true, false);
                mContent = CommonUtils.switchFragmentContent(mContent, mContent, mReportFragment,
                        R.id.custom_detail_fl, CustomDetailActivity.this);
                break;

            default:
                break;
        }
    }

    private void setSelect(boolean follow, boolean detail, boolean report, boolean isEdit) {
        mFollow.setSelected(follow);
        mDetail.setSelected(detail);
        mReport.setSelected(report);
        if(isEdit){
            mCustomDetailRight.setVisibility(View.VISIBLE);
        }else{
            mCustomDetailRight.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(mFollow.isSelected()){
            mFollowFragment.onActivityResult(requestCode, resultCode, data);
        }
        if(mDetail.isSelected()){
            if(resultCode == RESULT_OK){
                if(requestCode == RESULT_EDIT_CUSTOMER){
                    cus = (Custom)data.getSerializableExtra("customdata");
                    EventBus.getDefault().post(cus);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
