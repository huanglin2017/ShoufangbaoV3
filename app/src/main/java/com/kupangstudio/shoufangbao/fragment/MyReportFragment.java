package com.kupangstudio.shoufangbao.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.ReportDetailActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Report;
import com.kupangstudio.shoufangbao.model.ReportBuild;
import com.kupangstudio.shoufangbao.model.ReportEndLog;
import com.kupangstudio.shoufangbao.model.ReportEvent;
import com.kupangstudio.shoufangbao.model.ReportLog;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.kupangstudio.shoufangbao.widget.PullToRefreshListView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.OkHttpRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by long1 on 16/1/21.
 * Copyright 16/1/21 android_xiaobai.
 */
public class MyReportFragment extends Fragment {
    private int position;
    private List<Report> reportList;
    private PullToRefreshListView refreshListView;
    private ListView lv;
    private RelativeLayout emptyLayout;
    private TextView emptyText;
    private Button emptyBtn;
    private boolean isFirst = true;
    private RelativeLayout loadingReport;
    private MyAdapter adapter;
    private int type = 1;
    private ProgressDialog dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_report, container, false);
        position = getArguments().getInt("position");
        initView(view);
        if (position == 0) {
            getMyReport(type);
        } else {
            initList(position);
        }
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ReportDetailActivity.class);
                intent.putExtra("report", reportList.get(position));
                intent.putExtra("flag", "myreport");
                startActivity(intent);
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Report report = reportList.get(position);
                final AppDialog.Builder builder = new AppDialog.Builder(getActivity(), AppDialog.Builder.COMMONDIALOG);
                View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.common_dialog_custom, null);
                builder.setContentView(dialogView);
                builder.setTitle("确定删除");
                builder.setPositiveButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.dismiss();
                        deleteReport(report.getRid());
                    }
                });
                builder.setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.dismiss();
                    }
                });
                builder.create();
                return true;
            }
        });
        refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                isFirst = false;
                if (position == 0) {
                    getMyReport(type);
                } else {
                    initList(position);
                }
            }
        });
        emptyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirst = true;
                if (emptyLayout.getVisibility() == View.VISIBLE) {
                    emptyLayout.setVisibility(View.GONE);
                }
                if (position == 0) {
                    getMyReport(type);
                } else {
                    initList(position);
                }
            }
        });
        return view;
    }

    private void initList(int position) {
        if (isFirst) {
            loadingReport.setVisibility(View.VISIBLE);
        }
        if (position == 7) {
            reportList = DataSupport.where("isend = ?", String.valueOf(Report.END)).find(Report.class);
        } else {
            reportList = DataSupport.where("status = ? and isend = ?", String.valueOf(position), "0").find(Report.class);
        }
        for (int i = 0; i < reportList.size(); i++) {
            Report report = reportList.get(i);
            List<ReportLog> reportLogs = DataSupport.where("report_id = ?", String.valueOf(report.getId())).find(ReportLog.class);
            List<ReportBuild> reportBuild = DataSupport.where("report_id = ?", String.valueOf(report.getId())).find(ReportBuild.class);
            if (reportBuild.size() > 0 && reportBuild != null) {
                report.setBuild(reportBuild.get(0));
            }
            List<ReportEndLog> endLog = DataSupport.where("report_id = ?", String.valueOf(report.getId())).find(ReportEndLog.class);
            if (endLog != null && endLog.size() > 0) {
                report.setEndLog(endLog.get(0));
            }
            report.setReportLog(reportLogs);
        }
        setAdapter();
        if (isFirst) {
            loadingReport.setVisibility(View.GONE);
        } else {
            refreshListView.onRefreshComplete();
        }
    }

    private void deleteReport(final int rid) {
        if (getActivity() == null) {
            return;
        }
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("删除中");
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "delReport");
        map.put("module", Constants.MODULE_CUSTOM);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("rid", String.valueOf(rid));
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
                        if (getActivity() == null) {
                            return;
                        }
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null) {
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                Report report = null;
                                DataSupport.deleteAll(Report.class, "rid = ?", String.valueOf(rid));
                                for (int j = 0; j < reportList.size(); j++) {
                                    if (reportList.get(j).getRid() == rid) {
                                        report = reportList.get(j);
                                        reportList.remove(j);
                                    }
                                }
                                EventBus.getDefault().post(report);
                                setAdapter();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
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

    private void initView(View view) {
        emptyLayout = (RelativeLayout) view.findViewById(R.id.emptyview);
        emptyText = (TextView) view.findViewById(R.id.emptyview_text);
        emptyBtn = (Button) view.findViewById(R.id.emptyview_btn);
        loadingReport = (RelativeLayout) view.findViewById(R.id.loading_report);
        refreshListView = (PullToRefreshListView) view.findViewById(R.id.refreshListView);
        lv = refreshListView.getRefreshableView();
        emptyText.setText("暂无报备，小宝的钱都发给别人了！");
    }

    public void onEventMainThread(Report report) {
        for (int j = 0; j < reportList.size(); j++) {
            if (reportList.get(j).getRid() == report.getRid()) {
                reportList.remove(j);
            }
        }
        setAdapter();
    }

    public void onEventMainThread(ReportEvent type) {
        if (type.getPosition() == 2) {
            initList(position);
        } else {
            this.type = type.getPosition();
            isFirst = true;
            if (emptyLayout.getVisibility() == View.VISIBLE) {
                emptyLayout.setVisibility(View.GONE);
            }
            if (position == 0) {
                getMyReport(this.type);
            }
        }
    }

    private void getMyReport(int type) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getReport");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("type", String.valueOf(type));
        map.put("status", "0");
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new MyReportCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        if (isFirst) {
                            loadingReport.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (getActivity() == null) {
                            return;
                        }
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        reportList = DataSupport.findAll(Report.class);
                        for (int i = 0; i < reportList.size(); i++) {
                            Report report = reportList.get(i);
                            List<ReportLog> reportLogs = DataSupport.where("report_id = ?", String.valueOf(report.getId())).find(ReportLog.class);
                            List<ReportBuild> reportBuild = DataSupport.where("report_id = ?", String.valueOf(report.getId())).find(ReportBuild.class);
                            if (reportBuild.size() > 0 && reportBuild != null) {
                                report.setBuild(reportBuild.get(0));
                            }
                            List<ReportEndLog> endLog = DataSupport.where("report_id = ?", String.valueOf(report.getId())).find(ReportEndLog.class);
                            if (endLog.size() > 0 && endLog != null) {
                                report.setEndLog(endLog.get(0));
                            }
                            report.setReportLog(reportLogs);
                        }
                        setAdapter();
                    }

                    @Override
                    public void onResponse(Result<List<Report>> response) {
                        if (getActivity() == null) {
                            return;
                        }
                        if (response == null) {
                            reportList = DataSupport.findAll(Report.class);
                            for (int i = 0; i < reportList.size(); i++) {
                                Report report = reportList.get(i);
                                List<ReportLog> reportLogs = DataSupport.where("report_id = ?", String.valueOf(report.getId())).find(ReportLog.class);
                                List<ReportBuild> reportBuild = DataSupport.where("report_id = ?", String.valueOf(report.getId())).find(ReportBuild.class);
                                if (reportBuild.size() > 0 && reportBuild != null) {
                                    report.setBuild(reportBuild.get(0));
                                }
                                List<ReportEndLog> endLog = DataSupport.where("report_id = ?", String.valueOf(report.getId())).find(ReportEndLog.class);
                                if (endLog.size() > 0 && endLog != null) {
                                    report.setEndLog(endLog.get(0));
                                }
                                report.setReportLog(reportLogs);
                            }
                            setAdapter();
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            ReportEvent switchEvent = new ReportEvent(2);
                            EventBus.getDefault().post(switchEvent);
                            emptyLayout.setVisibility(View.GONE);
                            reportList = response.getContent();
                            DataSupport.deleteAll(Report.class);
                            for (int i = 0; i < reportList.size(); i++) {
                                Report report = reportList.get(i);
                                report.getBuild().save();
                                report.getEndLog().save();
                                List<ReportLog> reportLog = report.getReportLog();
                                DataSupport.saveAll(reportLog);
                                report.save();
                            }
                        } else {
                            if (getActivity() == null) {
                                return;
                            }
                            Toast.makeText(getActivity(), response.getNotice(), Toast.LENGTH_SHORT).show();
                            if (response.getCode() != 1078) {
                                reportList = DataSupport.findAll(Report.class);
                                for (int i = 0; i < reportList.size(); i++) {
                                    Report report = reportList.get(i);
                                    List<ReportBuild> reportBuild = DataSupport.where("report_id = ?", String.valueOf(report.getId())).find(ReportBuild.class);
                                    if (reportBuild.size() > 0 && reportBuild != null) {
                                        report.setBuild(reportBuild.get(0));
                                    }
                                    List<ReportEndLog> endLog = DataSupport.where("report_id = ?", String.valueOf(report.getId())).find(ReportEndLog.class);
                                    if (endLog.size() > 0 && endLog != null) {
                                        report.setEndLog(endLog.get(0));
                                    }
                                    List<ReportLog> reportLogs = DataSupport.where("report_id = ?", String.valueOf(report.getId())).find(ReportLog.class);
                                    report.setReportLog(reportLogs);
                                }
                            }
                        }
                        setAdapter();
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        if (isFirst) {
                            loadingReport.setVisibility(View.GONE);
                            isFirst = false;
                        } else {
                            refreshListView.onRefreshComplete();
                        }
                        if (reportList == null || reportList.size() <= 0) {
                            emptyLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private abstract class MyReportCallback extends Callback<Result<List<Report>>> {
        @Override
        public Result<List<Report>> parseNetworkResponse(Response response) throws Exception {
            String json = response.body().string();
            Result<List<Report>> list = null;
            try {
                list = new Gson().fromJson(json,
                        new TypeToken<Result<List<Report>>>() {
                        }.getType());
            } catch (Exception e) {
                if (getActivity() == null) {
                    return null;
                }
                Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                return null;
            }
            return list;
        }
    }

    class MyAdapter extends BaseAdapter {
        LayoutInflater inflater;
        List<Report> list;

        public MyAdapter(Context context, List<Report> list) {
            inflater = LayoutInflater.from(context);
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_mybuildreport, parent, false);
                holder.item_type = (ImageView) convertView.findViewById(R.id.item_myreport_type);
                holder.item_content = (TextView) convertView.findViewById(R.id.item_myreport_content);
                holder.item_phone = (TextView) convertView.findViewById(R.id.item_myreport_phone);
                holder.item_time = (TextView) convertView.findViewById(R.id.item_myreport_time);
                holder.item_raw = (ImageView) convertView.findViewById(R.id.item_myreport_raw);
                holder.item_myreport_raw = (ImageView) convertView.findViewById(R.id.item_myreport_raw);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Report r = list.get(position);
            holder.item_myreport_raw.setVisibility(View.VISIBLE);
            if (r.getIsend() == Report.END) {
                holder.item_type.setImageResource(R.drawable.myreport_fail);
                holder.item_raw.setImageResource(R.drawable.common_rawright_red);
                holder.item_content.setTextColor(Color.parseColor("#f15353"));
                holder.item_phone.setTextColor(Color.parseColor("#f15353"));
                holder.item_time.setTextColor(Color.parseColor("#f15353"));
            } else {
                holder.item_content.setTextColor(Color.parseColor("#464646"));
                holder.item_phone.setTextColor(Color.parseColor("#464646"));
                holder.item_time.setTextColor(Color.parseColor("#464646"));
                holder.item_raw.setImageResource(R.drawable.common_rawright_black);
                switch (r.getMold()) {
                    case Report.PHONE_REPORT:
                        holder.item_type.setImageResource(R.drawable.myreport_phone);
                        break;
                    case Report.ONLINE_REPORT:
                        holder.item_type.setImageResource(R.drawable.myreport_online);
                        break;
                    default:
                        break;
                }
            }
            holder.item_content.setText("报备" + r.getName() + "给" + r.getBuild().getName());
            String phone = r.getTel();
            holder.item_phone.setText("手机号：" + phone);
            holder.item_time.setText(TimeUtils.getCustomFollowData(r.getReportLog().get(0).getCtime() * 1000));
            return convertView;
        }

        private class ViewHolder {
            ImageView item_type;
            TextView item_content;
            TextView item_phone;
            TextView item_time;
            ImageView item_raw;
            ImageView item_myreport_raw;
        }
    }

    private void setAdapter() {
        if (reportList == null || reportList.size() <= 0) {
            emptyLayout.setVisibility(View.VISIBLE);
        } else {
            emptyLayout.setVisibility(View.GONE);
        }
        if (reportList == null) {
            reportList = new ArrayList<Report>();
        }
        if (adapter == null) {
            if (getActivity() == null) {
                return;
            }
            adapter = new MyAdapter(getActivity(), reportList);
            lv.setAdapter(adapter);
        } else {
            adapter.list = reportList;
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
