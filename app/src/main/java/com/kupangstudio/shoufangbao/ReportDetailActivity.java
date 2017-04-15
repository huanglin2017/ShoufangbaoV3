package com.kupangstudio.shoufangbao;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Report;
import com.kupangstudio.shoufangbao.model.ReportLog;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by long1 on 15/11/25.
 * Copyright 15/11/25 android_xiaobai.
 */
public class ReportDetailActivity extends BaseActivity {

    private TextView tvName;
    private TextView tvPhone;
    private ImageView ivTag;
    private TextView tvBuildName;
    private TextView tvReportNum;
    private ImageView stateOneImg;
    private TextView stateOneName;
    private TextView stateOneTime;
    private ImageView stateTwoImg;
    private TextView stateTwoName;
    private TextView stateTwoTime;
    private View stateTwoLine;
    private ImageView stateThreeImg;
    private TextView stateThreeName;
    private TextView stateThreeTime;
    private View stateThreeLine;
    private ImageView stateFourImg;
    private TextView stateFourName;
    private TextView stateFourTime;
    private View stateFourLine;
    private ImageView stateFiveImg;
    private TextView stateFiveName;
    private TextView stateFiveTime;
    private View stateFiveLine;
    private ImageView stateSixImg;
    private TextView stateSixName;
    private TextView stateSixTime;
    private View stateSixLine;
    private TextView tvReason;
    private RelativeLayout layoutLaunch;
    private RelativeLayout layoutApply;
    private Report report;
    private TextView tvHotLine;
    private String reason;
    private TextView tvAction;
    private int action;
    private String flag;
    private TextView remarkReport;
    private TextView remarkJieshou;
    private TextView remarkSee;
    private TextView remarkBuy;
    private TextView remarkExchange;
    private String remark = "备注：";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "报备进程");
        initView();
        report = (Report) getIntent().getSerializableExtra("report");
        flag = getIntent().getStringExtra("flag");
        setCustomGone();
        tvName.setText(report.getName());
        tvPhone.setText(report.getTel());
        tvBuildName.setText(report.getBuild().getName());
        tvReportNum.setText("报备号：" + report.getNumber());
        if (report.getIsend() == Report.END) {
            ivTag.setVisibility(View.VISIBLE);
            ivTag.setImageResource(R.drawable.myreport_fail);
        } else {
            switch (report.getMold()) {
                case Report.PHONE_REPORT:
                    ivTag.setImageResource(R.drawable.myreport_phone);
                    break;
                case Report.ONLINE_REPORT:
                    ivTag.setImageResource(R.drawable.myreport_online);
                    break;
                default:
                    break;
            }
        }
        final List<ReportLog> reportLogs = report.getReportLog();
        for (int i = 0; i < reportLogs.size(); i++) {
            if(reportLogs.get(i).getStatus() == 0) {
                reason = reportLogs.get(i).getRemark();
            }
        }
        setState(report.getReportLog().size());
        layoutLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (report.getIsend() == Report.END) {
                    Toast.makeText(ReportDetailActivity.this, "报备流程已关闭，请重新报备", Toast.LENGTH_SHORT).show();
                } else {
                    if(report.getReportLog().size() >= Report.DEAL) {
                        Toast.makeText(ReportDetailActivity.this, "已成交，无需再次发起", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    switch (action) {
                        case Report.ACTION_DAIKAN:
                            startSee();
                            break;
                        case Report.ACTION_PURCHASE:
                            startSubscribe();
                            break;
                        case Report.ACTION_DEAL:
                            startTurnover();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        layoutApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (report.getIsend() == Report.END) {
                    Toast.makeText(ReportDetailActivity.this, "报备流程已关闭，请重新报备", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (report.getType() == Report.APPLY_OK) {
                    Intent intent = new Intent(ReportDetailActivity.this, ApplySeeAwardActivity.class);
                    intent.putExtra("rid", report.getRid());
                    startActivity(intent);
                } else {
                    Toast.makeText(ReportDetailActivity.this, "亲，该楼盘当前还没有带看奖励呦~~", Toast.LENGTH_SHORT).show();
                }
            }
        });
        tvHotLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = (String) SPUtils.get(ReportDetailActivity.this, Constants.HOT_LINE, "4000116929");
                CommonUtils.callPhone(phone, ReportDetailActivity.this);
            }
        });
    }

    private void initView() {
        tvName = (TextView) findViewById(R.id.detail_name);
        tvPhone = (TextView) findViewById(R.id.detail_phone);
        ivTag = (ImageView) findViewById(R.id.detail_tag);
        tvBuildName = (TextView) findViewById(R.id.detail_build_name);
        tvReportNum = (TextView) findViewById(R.id.detail_num);
        stateOneImg = (ImageView) findViewById(R.id.detail_state_one_img);
        stateOneName = (TextView) findViewById(R.id.detail_state_one_name);
        stateOneTime = (TextView) findViewById(R.id.detail_state_one_time);
        stateTwoImg = (ImageView) findViewById(R.id.detail_state_two_img);
        stateTwoName = (TextView) findViewById(R.id.detail_state_two_name);
        stateTwoTime = (TextView) findViewById(R.id.detail_state_two_time);
        stateTwoLine = findViewById(R.id.detail_state_two_line);
        stateThreeImg = (ImageView) findViewById(R.id.detail_state_three_img);
        stateThreeName = (TextView) findViewById(R.id.detail_state_three_name);
        stateThreeTime = (TextView) findViewById(R.id.detail_state_three_time);
        stateThreeLine = findViewById(R.id.detail_state_three_line);
        stateFourImg = (ImageView) findViewById(R.id.detail_state_four_img);
        stateFourName = (TextView) findViewById(R.id.detail_state_four_name);
        stateFourTime = (TextView) findViewById(R.id.detail_state_four_time);
        stateFourLine = findViewById(R.id.detail_state_four_line);
        stateFiveImg = (ImageView) findViewById(R.id.detail_state_five_img);
        stateFiveName = (TextView) findViewById(R.id.detail_state_five_name);
        stateFiveTime = (TextView) findViewById(R.id.detail_state_five_time);
        stateFiveLine = findViewById(R.id.detail_state_five_line);
        stateSixImg = (ImageView) findViewById(R.id.detail_state_six_img);
        stateSixName = (TextView) findViewById(R.id.detail_state_six_name);
        stateSixTime = (TextView) findViewById(R.id.detail_state_six_time);
        stateSixLine = findViewById(R.id.detail_state_six_line);
        layoutLaunch = (RelativeLayout) findViewById(R.id.detail_launch);
        layoutApply = (RelativeLayout) findViewById(R.id.detail_apply);
        tvHotLine = (TextView) findViewById(R.id.report_hotline);
        tvReason = (TextView) findViewById(R.id.report_end_reason);
        tvAction = (TextView) findViewById(R.id.button_action);
        remarkReport = (TextView) findViewById(R.id.report_remark);
        remarkJieshou = (TextView) findViewById(R.id.jieshou_remark);
        remarkSee = (TextView) findViewById(R.id.see_remark);
        remarkBuy = (TextView) findViewById(R.id.buy_remark);
        remarkExchange = (TextView) findViewById(R.id.exchange_remark);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    private void startSee() {
        final ProgressDialog dialog = new ProgressDialog(ReportDetailActivity.this);
        dialog.setMessage("带看发起中...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "startSee");
        map.put("module", Constants.MODULE_BUILD);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("rid", String.valueOf(report.getRid()));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog.show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ReportDetailActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String notice = jsonObject.getString("notice");
                            Toast.makeText(ReportDetailActivity.this, notice, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(ReportDetailActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                });
    }


    private void startSubscribe() {
        final ProgressDialog dialog = new ProgressDialog(ReportDetailActivity.this);
        dialog.setMessage("认购发起中...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "startSubscribe");
        map.put("module", Constants.MODULE_BUILD);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("rid", String.valueOf(report.getRid()));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog.show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ReportDetailActivity.this, ResultError.MESSAGE_NULL,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String notice = jsonObject.getString("notice");
                            Toast.makeText(ReportDetailActivity.this, notice,
                                    Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(ReportDetailActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                });
    }

    private void startTurnover() {
        final ProgressDialog dialog = new ProgressDialog(ReportDetailActivity.this);
        dialog.setMessage("成交发起中...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "startTurnover");
        map.put("module", Constants.MODULE_BUILD);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("rid", String.valueOf(report.getRid()));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog.show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ReportDetailActivity.this, ResultError.MESSAGE_NULL,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String notice = jsonObject.getString("notice");
                            Toast.makeText(ReportDetailActivity.this, notice,
                                    Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(ReportDetailActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                });
    }


    private void setState(int state) {
        ReportLog reportOne;
        ReportLog reportTwo;
        ReportLog reportThree;
        ReportLog reportFour;
        ReportLog reportFive;
        ReportLog reportSix;
        switch (state) {
            case Report.REPORT:
                reportOne = report.getReportLog().get(0);
                stateOneImg.setImageResource(R.drawable.report_progress_done);
                stateOneName.setSelected(true);
                stateOneTime.setSelected(true);
                stateOneTime.setTextColor(Color.parseColor("#f15353"));
                stateOneTime.setText(TimeUtils.getCustomFollowData(reportOne.getCtime() * 1000));
                if (report.getIsend() == Report.END) {
                    stateTwoName.setText("已结束");
                    stateTwoLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                    stateTwoImg.setImageResource(R.drawable.report_progress_done);
                    stateTwoName.setSelected(true);
                    stateTwoTime.setSelected(true);
                    stateTwoTime.setTextColor(Color.parseColor("#f15353"));
                    stateTwoTime.setText(TimeUtils.getCustomFollowData(report.getEndLog().getCtime() * 1000));
                    if (report.getEndLog().getRemark() == null || report.getEndLog().getRemark().
                            equals("") || report.getEndLog().getRemark().equals(" ")) {
                        remarkJieshou.setText("");
                    }else {
                        remarkJieshou.setText(remark + report.getEndLog().getRemark());
                    }
                }
                if (reportOne.getRemark() == null || reportOne.getRemark().equals("") || reportOne.getRemark().equals(" ")) {
                    remarkReport.setText("");
                }else {
                    remarkReport.setText(remark + reportOne.getRemark());
                }
                action = Report.ACTION_DAIKAN;
                break;
            case Report.ACCEPT:
                reportOne = report.getReportLog().get(0);
                stateOneImg.setImageResource(R.drawable.report_progress_done);
                stateOneName.setSelected(true);
                stateOneTime.setSelected(true);
                stateOneTime.setText(TimeUtils.getCustomFollowData(reportOne.getCtime() * 1000));
                reportTwo = report.getReportLog().get(1);
                stateTwoImg.setImageResource(R.drawable.report_progress_done);
                stateTwoName.setSelected(true);
                stateTwoTime.setSelected(true);
                stateTwoTime.setText(TimeUtils.getCustomFollowData(reportTwo.getCtime() * 1000));
                stateTwoLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                if (report.getIsend() == Report.END) {
                    stateThreeName.setText("已结束");
                    stateThreeImg.setImageResource(R.drawable.report_progress_done);
                    stateThreeName.setSelected(true);
                    stateThreeLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                    stateThreeTime.setSelected(true);
                    stateThreeTime.setTextColor(Color.parseColor("#f15353"));
                    stateThreeTime.setText(TimeUtils.getCustomFollowData(report.getEndLog().getCtime() * 1000));
                    if (report.getEndLog().getRemark() == null || report.getEndLog().getRemark().
                            equals("") || report.getEndLog().getRemark().equals(" ")) {
                        remarkSee.setText("");
                    }else {
                        remarkSee.setText(remark + report.getEndLog().getRemark());
                    }
                }
                if (reportOne.getRemark() == null || reportOne.getRemark().equals("") || reportOne.getRemark().equals(" ")) {
                    remarkReport.setText("");
                }else {
                    remarkReport.setText(remark + reportOne.getRemark());
                }

                if (reportTwo.getRemark() == null || reportTwo.getRemark().equals("") || reportTwo.getRemark().equals(" ")) {
                    remarkJieshou.setText("");
                } else {
                    remarkJieshou.setText(remark + reportTwo.getRemark());
                }
                action = Report.ACTION_DAIKAN;
                break;
            case Report.DAIKAN:
                reportOne = report.getReportLog().get(0);
                stateOneImg.setImageResource(R.drawable.report_progress_done);
                stateOneName.setSelected(true);
                stateOneTime.setSelected(true);
                stateOneTime.setText(TimeUtils.getCustomFollowData(reportOne.getCtime() * 1000));
                reportTwo = report.getReportLog().get(1);
                stateTwoImg.setImageResource(R.drawable.report_progress_done);
                stateTwoName.setSelected(true);
                stateTwoTime.setSelected(true);
                stateTwoTime.setText(TimeUtils.getCustomFollowData(reportTwo.getCtime() * 1000));
                stateTwoLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                reportThree = report.getReportLog().get(2);
                stateThreeImg.setImageResource(R.drawable.report_progress_done);
                stateThreeName.setSelected(true);
                stateThreeTime.setSelected(true);
                stateThreeTime.setText(TimeUtils.getCustomFollowData(reportThree.getCtime() * 1000));
                stateThreeLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                if (report.getIsend() == Report.END) {
                    stateFourName.setText("已结束");
                    stateFourImg.setImageResource(R.drawable.report_progress_done);
                    stateFourName.setSelected(true);
                    stateFourLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                    stateFourTime.setSelected(true);
                    stateFourTime.setTextColor(Color.parseColor("#f15353"));
                    stateFourTime.setText(TimeUtils.getCustomFollowData(report.getEndLog().getCtime() * 1000));
                    if (report.getEndLog().getRemark() == null || report.getEndLog().getRemark().
                            equals("") || report.getEndLog().getRemark().equals(" ")) {
                        remarkBuy.setText("");
                    }else {
                        remarkBuy.setText(remark + report.getEndLog().getRemark());
                    }
                }
                if (reportOne.getRemark() == null || reportOne.getRemark().equals("") || reportOne.getRemark().equals(" ")) {
                    remarkReport.setText("");
                }else {
                    remarkReport.setText(remark + reportOne.getRemark());
                }

                if (reportTwo.getRemark() == null || reportTwo.getRemark().equals("") || reportTwo.getRemark().equals(" ")) {
                    remarkJieshou.setText("");
                } else {
                    remarkJieshou.setText(remark + reportTwo.getRemark());
                }

                if (reportThree.getRemark() == null || reportThree.getRemark().equals("") || reportThree.getRemark().equals(" ")) {
                    remarkSee.setText("");
                } else {
                    remarkSee.setText(remark + reportThree.getRemark());
                }
                action = Report.ACTION_PURCHASE;
                tvAction.setText("发起认购");
                break;
            case Report.PURCHASE:
                reportOne = report.getReportLog().get(0);
                stateOneImg.setImageResource(R.drawable.report_progress_done);
                stateOneName.setSelected(true);
                stateOneTime.setSelected(true);
                stateOneTime.setText(TimeUtils.getCustomFollowData(reportOne.getCtime() * 1000));
                reportTwo = report.getReportLog().get(1);
                stateTwoImg.setImageResource(R.drawable.report_progress_done);
                stateTwoName.setSelected(true);
                stateTwoTime.setSelected(true);
                stateTwoTime.setText(TimeUtils.getCustomFollowData(reportTwo.getCtime() * 1000));
                stateTwoLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                reportThree = report.getReportLog().get(2);
                stateThreeImg.setImageResource(R.drawable.report_progress_done);
                stateThreeName.setSelected(true);
                stateThreeTime.setSelected(true);
                stateThreeTime.setText(TimeUtils.getCustomFollowData(reportThree.getCtime() * 1000));
                stateThreeLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                reportFour = report.getReportLog().get(3);
                stateFourImg.setImageResource(R.drawable.report_progress_done);
                stateFourName.setSelected(true);
                stateFourTime.setSelected(true);
                stateFourTime.setText(TimeUtils.getCustomFollowData(reportFour.getCtime() * 1000));
                stateFourLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                if (report.getIsend() == Report.END) {
                    stateFiveName.setText("已结束");
                    stateFiveImg.setImageResource(R.drawable.report_progress_done);
                    stateFiveName.setSelected(true);
                    stateFiveLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                    stateFiveTime.setSelected(true);
                    stateFiveTime.setTextColor(Color.parseColor("#f15353"));
                    stateFiveTime.setText(TimeUtils.getCustomFollowData(report.getEndLog().getCtime() * 1000));
                    if (report.getEndLog().getRemark() == null || report.getEndLog().getRemark().
                            equals("") || report.getEndLog().getRemark().equals(" ")) {
                        remarkExchange.setText("");
                    }else {
                        remarkExchange.setText(remark + report.getEndLog().getRemark());
                    }
                }

                if (reportOne.getRemark() == null || reportOne.getRemark().equals("") || reportOne.getRemark().equals(" ")) {
                    remarkReport.setText("");
                }else {
                    remarkReport.setText(remark + reportOne.getRemark());
                }

                if (reportTwo.getRemark() == null || reportTwo.getRemark().equals("") || reportTwo.getRemark().equals(" ")) {
                    remarkJieshou.setText("");
                } else {
                    remarkJieshou.setText(remark + reportTwo.getRemark());
                }

                if (reportThree.getRemark() == null || reportThree.getRemark().equals("") || reportThree.getRemark().equals(" ")) {
                    remarkSee.setText("");
                } else {
                    remarkSee.setText(remark + reportThree.getRemark());
                }

                if (reportFour.getRemark() == null || reportFour.getRemark().equals("") || reportFour.getRemark().equals(" ")) {
                    remarkBuy.setText("");
                } else {
                    remarkBuy.setText(remark + reportFour.getRemark());
                }
                action = Report.ACTION_DEAL;
                tvAction.setText("发起成交");
                break;
            case Report.DEAL:
                reportOne = report.getReportLog().get(0);
                stateOneImg.setImageResource(R.drawable.report_progress_done);
                stateOneName.setSelected(true);
                stateOneTime.setSelected(true);
                stateOneTime.setText(TimeUtils.getCustomFollowData(reportOne.getCtime() * 1000));
                reportTwo = report.getReportLog().get(1);
                stateTwoImg.setImageResource(R.drawable.report_progress_done);
                stateTwoName.setSelected(true);
                stateTwoTime.setSelected(true);
                stateTwoTime.setText(TimeUtils.getCustomFollowData(reportTwo.getCtime() * 1000));
                stateTwoLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                reportThree = report.getReportLog().get(2);
                stateThreeImg.setImageResource(R.drawable.report_progress_done);
                stateThreeName.setSelected(true);
                stateThreeTime.setSelected(true);
                stateThreeTime.setText(TimeUtils.getCustomFollowData(reportThree.getCtime() * 1000));
                stateThreeLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                reportFour = report.getReportLog().get(3);
                stateFourImg.setImageResource(R.drawable.report_progress_done);
                stateFourName.setSelected(true);
                stateFourTime.setSelected(true);
                stateFourTime.setText(TimeUtils.getCustomFollowData(reportFour.getCtime() * 1000));
                stateFourLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                reportFive = report.getReportLog().get(4);
                stateFiveImg.setImageResource(R.drawable.report_progress_done);
                stateFiveName.setSelected(true);
                stateFiveTime.setSelected(true);
                stateFiveTime.setText(TimeUtils.getCustomFollowData(reportFive.getCtime() * 1000));
                stateFiveLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                if (report.getIsend() == Report.END) {
                    stateSixName.setText("已结束");
                    stateSixImg.setImageResource(R.drawable.report_progress_done);
                    stateSixName.setSelected(true);
                    stateSixLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                    stateSixTime.setSelected(true);
                    stateSixTime.setTextColor(Color.parseColor("#f15353"));
                    stateSixTime.setText(TimeUtils.getCustomFollowData(report.getEndLog().getCtime() * 1000));
                    if (report.getEndLog().getRemark() == null || report.getEndLog().getRemark().
                            equals("") || report.getEndLog().getRemark().equals(" ")) {
                        tvReason.setText("");
                    }else {
                        tvReason.setText(remark + report.getEndLog().getRemark());
                    }
                }
                if (reportOne.getRemark() == null || reportOne.getRemark().equals("") || reportOne.getRemark().equals(" ")) {
                    remarkReport.setText("");
                }else {
                    remarkReport.setText(remark + reportOne.getRemark());
                }

                if (reportTwo.getRemark() == null || reportTwo.getRemark().equals("") || reportTwo.getRemark().equals(" ")) {
                    remarkJieshou.setText("");
                } else {
                    remarkJieshou.setText(remark + reportTwo.getRemark());
                }

                if (reportThree.getRemark() == null || reportThree.getRemark().equals("") || reportThree.getRemark().equals(" ")) {
                    remarkSee.setText("");
                } else {
                    remarkSee.setText(remark + reportThree.getRemark());
                }

                if (reportFour.getRemark() == null || reportFour.getRemark().equals("") || reportFour.getRemark().equals(" ")) {
                    remarkBuy.setText("");
                } else {
                    remarkBuy.setText(remark + reportFour.getRemark());
                }

                if (reportFive.getRemark() == null || reportFive.getRemark().equals("") || reportFive.getRemark().equals(" ")) {
                    remarkExchange.setText("");
                } else {
                    remarkExchange.setText(remark + reportFive.getRemark());
                }

                action = Report.ACTION_DEAL;
                tvAction.setText("发起成交");
                break;
            case Report.COMMISSION:
                reportOne = report.getReportLog().get(0);
                stateOneImg.setImageResource(R.drawable.report_progress_done);
                stateOneName.setSelected(true);
                stateOneTime.setSelected(true);
                stateOneTime.setText(TimeUtils.getCustomFollowData(reportOne.getCtime() * 1000));
                reportTwo = report.getReportLog().get(1);
                stateTwoImg.setImageResource(R.drawable.report_progress_done);
                stateTwoName.setSelected(true);
                stateTwoTime.setSelected(true);
                stateTwoTime.setText(TimeUtils.getCustomFollowData(reportTwo.getCtime() * 1000));
                stateTwoLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                reportThree = report.getReportLog().get(2);
                stateThreeImg.setImageResource(R.drawable.report_progress_done);
                stateThreeName.setSelected(true);
                stateThreeTime.setSelected(true);
                stateThreeTime.setText(TimeUtils.getCustomFollowData(reportThree.getCtime() * 1000));
                stateThreeLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                reportFour = report.getReportLog().get(3);
                stateFourImg.setImageResource(R.drawable.report_progress_done);
                stateFourName.setSelected(true);
                stateFourTime.setSelected(true);
                stateFourTime.setText(TimeUtils.getCustomFollowData(reportFour.getCtime() * 1000));
                stateFourLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                reportFive = report.getReportLog().get(4);
                stateFiveImg.setImageResource(R.drawable.report_progress_done);
                stateFiveName.setSelected(true);
                stateFiveTime.setSelected(true);
                stateFiveTime.setText(TimeUtils.getCustomFollowData(reportFive.getCtime() * 1000));
                stateFiveLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                reportSix = report.getReportLog().get(5);
                stateSixImg.setImageResource(R.drawable.report_progress_done);
                stateSixName.setSelected(true);
                stateSixTime.setSelected(true);
                stateSixTime.setText(TimeUtils.getCustomFollowData(reportSix.getCtime() * 1000));
                stateSixLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                /*if (report.getIsend() == Report.END) {
                    stateSixName.setText("已结束");
                    stateSixTime.setText(TimeUtils.getCustomFollowData(report.getEndLog().getCtime() * 1000));
                    if (report.getEndLog().getRemark() == null || report.getEndLog().getRemark().
                            equals("") || report.getEndLog().getRemark().equals(" ")) {
                        tvReason.setText("");
                    }else {
                        tvReason.setText(remark + reportOne.getRemark());
                    }
                }*/
                if (reportOne.getRemark() == null || reportOne.getRemark().equals("") || reportOne.getRemark().equals(" ")) {
                    remarkReport.setText("");
                }else {
                    remarkReport.setText(remark + reportOne.getRemark());
                }

                if (reportTwo.getRemark() == null || reportTwo.getRemark().equals("") || reportTwo.getRemark().equals(" ")) {
                    remarkJieshou.setText("");
                } else {
                    remarkJieshou.setText(remark + reportTwo.getRemark());
                }

                if (reportThree.getRemark() == null || reportThree.getRemark().equals("") || reportThree.getRemark().equals(" ")) {
                    remarkSee.setText("");
                } else {
                    remarkSee.setText(remark + reportThree.getRemark());
                }

                if (reportFour.getRemark() == null || reportFour.getRemark().equals("") || reportFour.getRemark().equals(" ")) {
                    remarkBuy.setText("");
                } else {
                    remarkBuy.setText(remark + reportFour.getRemark());
                }

                if (reportFive.getRemark() == null || reportFive.getRemark().equals("") || reportFive.getRemark().equals(" ")) {
                    remarkExchange.setText("");
                } else {
                    remarkExchange.setText(remark + reportFive.getRemark());
                }

                if (reportSix.getRemark() == null || reportSix.getRemark().equals("") || reportSix.getRemark().equals(" ")) {
                    tvReason.setText("");
                } else {
                    tvReason.setText(remark + reportSix.getRemark());
                }
                action = Report.ACTION_DEAL;
                tvAction.setText("发起成交");
                break;
            default:
                if(report.getReportLog() == null || report.getReportLog().size() == 0) {
                    return;
                }
                reportOne = report.getReportLog().get(0);
                stateOneImg.setImageResource(R.drawable.report_progress_done);
                stateOneName.setSelected(true);
                stateOneTime.setSelected(true);
                stateOneTime.setText(TimeUtils.getCustomFollowData(reportOne.getCtime() * 1000));
                reportTwo = report.getReportLog().get(1);
                stateTwoImg.setImageResource(R.drawable.report_progress_done);
                stateTwoName.setSelected(true);
                stateTwoTime.setSelected(true);
                stateTwoTime.setText(TimeUtils.getCustomFollowData(reportTwo.getCtime() * 1000));
                stateTwoLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                reportThree = report.getReportLog().get(2);
                stateThreeImg.setImageResource(R.drawable.report_progress_done);
                stateThreeName.setSelected(true);
                stateThreeTime.setSelected(true);
                stateThreeTime.setText(TimeUtils.getCustomFollowData(reportThree.getCtime() * 1000));
                stateThreeLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                reportFour = report.getReportLog().get(3);
                stateFourImg.setImageResource(R.drawable.report_progress_done);
                stateFourName.setSelected(true);
                stateFourTime.setSelected(true);
                stateFourTime.setText(TimeUtils.getCustomFollowData(reportFour.getCtime() * 1000));
                stateFourLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                reportFive = report.getReportLog().get(4);
                stateFiveImg.setImageResource(R.drawable.report_progress_done);
                stateFiveName.setSelected(true);
                stateFiveTime.setSelected(true);
                stateFiveTime.setText(TimeUtils.getCustomFollowData(reportFive.getCtime() * 1000));
                stateFiveLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                reportSix = report.getReportLog().get(5);
                stateSixImg.setImageResource(R.drawable.report_progress_done);
                stateSixName.setSelected(true);
                stateSixTime.setSelected(true);
                stateSixTime.setText(TimeUtils.getCustomFollowData(reportSix.getCtime() * 1000));
                stateSixLine.setBackgroundColor(getResources().getColor(R.color.common_red));
                /*if (report.getIsend() == Report.END) {
                    stateSixName.setText("已结束");
                    stateSixTime.setText(TimeUtils.getCustomFollowData(report.getEndLog().getCtime() * 1000));
                    if (report.getEndLog().getRemark() == null || report.getEndLog().getRemark().
                            equals("") || report.getEndLog().getRemark().equals(" ")) {
                        tvReason.setText("");
                    }else {
                        tvReason.setText(remark + reportOne.getRemark());
                    }
                }*/
                if (reportOne.getRemark() == null || reportOne.getRemark().equals("") || reportOne.getRemark().equals(" ")) {
                    remarkReport.setText("");
                }else {
                    remarkReport.setText(remark + reportOne.getRemark());
                }

                if (reportTwo.getRemark() == null || reportTwo.getRemark().equals("") || reportTwo.getRemark().equals(" ")) {
                    remarkJieshou.setText("");
                } else {
                    remarkJieshou.setText(remark + reportTwo.getRemark());
                }

                if (reportThree.getRemark() == null || reportThree.getRemark().equals("") || reportThree.getRemark().equals(" ")) {
                    remarkSee.setText("");
                } else {
                    remarkSee.setText(remark + reportThree.getRemark());
                }

                if (reportFour.getRemark() == null || reportFour.getRemark().equals("") || reportFour.getRemark().equals(" ")) {
                    remarkBuy.setText("");
                } else {
                    remarkBuy.setText(remark + reportFour.getRemark());
                }

                if (reportFive.getRemark() == null || reportFive.getRemark().equals("") || reportFive.getRemark().equals(" ")) {
                    remarkExchange.setText("");
                } else {
                    remarkExchange.setText(remark + reportFive.getRemark());
                }

                if (reportSix.getRemark() == null || reportSix.getRemark().equals("") || reportSix.getRemark().equals(" ")) {
                    tvReason.setText("");
                } else {
                    tvReason.setText(remark + reportSix.getRemark());
                }
                action = Report.ACTION_DEAL;
                tvAction.setText("发起成交");
                break;
        }
    }

    private void setCustomGone() {
        if(flag.equals("custom")) {
            LinearLayout ll = (LinearLayout) findViewById(R.id.custom_gone_ll);
            LinearLayout lll = (LinearLayout) findViewById(R.id.custom_gone_lll);
            ImageView phone = (ImageView) findViewById(R.id.custom_gone_phone);
            View v = findViewById(R.id.custom_gone_v);
            ll.setVisibility(View.GONE);
            lll.setVisibility(View.GONE);
            phone.setVisibility(View.GONE);
            v.setVisibility(View.GONE);
            ivTag.setVisibility(View.GONE);
            tvHotLine.setVisibility(View.GONE);
        }
    }
}
