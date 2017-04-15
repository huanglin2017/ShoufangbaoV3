package com.kupangstudio.shoufangbao.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.ReportDetailActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Build;
import com.kupangstudio.shoufangbao.model.Custom;
import com.kupangstudio.shoufangbao.model.Report;
import com.kupangstudio.shoufangbao.model.ReportBuild;
import com.kupangstudio.shoufangbao.model.ReportEndLog;
import com.kupangstudio.shoufangbao.model.ReportLog;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.AppUtils;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Jsmi on 2015/11/4.
 * 客户报备页面
 */
@SuppressLint("ValidFragment")
public class CustomReportFragment extends Fragment implements View.OnClickListener {
    private Custom custom;
    private LinearLayout addReport;
    private ListView reportList;
    private ArrayList<Report> list;
    private ReportAdapter mAdapter;
    private RelativeLayout emptyview;
    private Button emptyBtn;
    private RelativeLayout progressDialog;
    private String buildNames;
    private TextView emptyText;
    private ImageView emptyImage;
    private String remarks = "备注：";

    public CustomReportFragment() {
    }

    public CustomReportFragment(Custom custom) {
        this.custom = custom;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_custom_report, container, false);
        init(ret);
        setClickListener();
        list = new ArrayList<Report>();
        getData();
        reportList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Report report = list.get(position);
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), ReportDetailActivity.class);
                    intent.putExtra("report", report);
                    intent.putExtra("flag", "custom");
                    startActivity(intent);
                }
            }
        });

        return ret;
    }

    private void init(View view) {
        addReport = (LinearLayout) view.findViewById(R.id.custom_report_add_rp);
        reportList = (ListView) view.findViewById(R.id.custom_report_listview);
        emptyview = (RelativeLayout) view.findViewById(R.id.emptyview);
        emptyBtn = (Button) view.findViewById(R.id.emptyview_btn);
        emptyText = (TextView) view.findViewById(R.id.emptyview_text);
        emptyText.setText("该客户暂无报备，请添加");
        emptyImage = (ImageView) view.findViewById(R.id.emptyview_img);
        emptyImage.setImageResource(R.drawable.common_empty_nodata);
        progressDialog = (RelativeLayout) view.findViewById(R.id.common_progress);
    }

    private void setClickListener() {
        addReport.setOnClickListener(this);
        emptyBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.empty_btn:
                getData();
                break;
            case R.id.custom_report_add_rp:
                if (User.getInstance().userType != User.TYPE_NORMAL_USER) {
                    AppUtils.showRegAuthDialog(getActivity(), "请您先登录", 0);
                    return;
                }
                //添加报备
                List<Build> builds = DataSupport.findAll(Build.class);
                List<String> buildName = new ArrayList<String>();
                final List<String> buildBid = new ArrayList<String>();
                for (Build build : builds) {
                    buildName.add(build.getName());
                    buildBid.add(String.valueOf(build.getBid()));
                }
                final String[] strings = new String[buildName.size()];
                int size = buildName.size();
                for (int i = 0; i < size; i++) {
                    strings[i] = buildName.get(i);
                }
                final AppDialog.Builder builder = new AppDialog.Builder(getActivity(), AppDialog.Builder.SINGLECHOICE, strings);
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.common_dialog_list, null);
                builder.setContentView(view).setContentView(view).setItemClick(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (CommonUtils.isLogin()) {
                            String bid = buildBid.get(position);
                            buildNames = strings[position];
                            getAddReportData(bid);
                            builder.dismiss();
                        }
                    }
                }).create();

                break;
            default:
                break;
        }

    }

    class ReportAdapter extends BaseAdapter {

        private Context context;
        private List<Report> data;

        public ReportAdapter(Context context, List<Report> data) {
            this.context = context;
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_custom_repot, viewGroup, false);
                holder = new ViewHolder();
                holder.isend = (ImageView) convertView.findViewById(R.id.custome_report_isend);
                holder.buildName = (TextView) convertView.findViewById(R.id.custom_report_buildname);
                holder.reportNum = (TextView) convertView.findViewById(R.id.custom_report_number);
                holder.status = (TextView) convertView.findViewById(R.id.custom_report_status);
                holder.time = (TextView) convertView.findViewById(R.id.custom_report_time);
                holder.remark = (TextView) convertView.findViewById(R.id.custom_report_remark);
                holder.line1 = convertView.findViewById(R.id.custom_line_1);
                holder.line2 = convertView.findViewById(R.id.custom_line_2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Report report = data.get(i);
            if (report.getIsend() == Report.END) {
                holder.isend.setVisibility(View.VISIBLE);
            } else {
                holder.isend.setVisibility(View.GONE);
            }
            holder.buildName.setText(report.getBuild().getName());
            holder.reportNum.setText(report.getNumber());
            if (report.getIsend() == Report.END) {
                holder.status.setText("已结束");
                holder.time.setText(TimeUtils.getCustomFollowData(report.getEndLog().getCtime() * 1000));
                if (report.getEndLog().getRemark() != null) {
                    holder.remark.setText(remarks + report.getEndLog().getRemark());
                    if (report.getEndLog().getRemark().length() <= 18) {
                        holder.line1.setVisibility(View.GONE);
                        holder.line2.setVisibility(View.VISIBLE);
                        holder.remark.setVisibility(View.VISIBLE);
                    } else {
                        holder.line1.setVisibility(View.VISIBLE);
                        holder.line2.setVisibility(View.GONE);
                        holder.remark.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                switch (report.getReportLog().size()) {
                    case 1:
                        holder.status.setText("已报备");
                        holder.time.setText(TimeUtils.getCustomFollowData(report.getReportLog().get(0).getCtime() * 1000));
                        if (report.getReportLog().get(0).getRemark() == null ||
                                report.getReportLog().get(0).getRemark().equals("") ||
                                report.getReportLog().get(0).getRemark().equals(" ")) {
                            holder.line1.setVisibility(View.GONE);
                            holder.line2.setVisibility(View.GONE);
                            holder.remark.setVisibility(View.GONE);
                        } else {
                            holder.remark.setText(remarks + report.getReportLog().get(0).getRemark());
                            if (report.getReportLog().get(0).getRemark().length() <= 18) {
                                holder.line1.setVisibility(View.GONE);
                                holder.line2.setVisibility(View.VISIBLE);
                                holder.remark.setVisibility(View.VISIBLE);
                            } else {
                                holder.line1.setVisibility(View.VISIBLE);
                                holder.line2.setVisibility(View.GONE);
                                holder.remark.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                    case 2:
                        holder.status.setText("已确认");
                        holder.time.setText(TimeUtils.getCustomFollowData(report.getReportLog().get(1).getCtime() * 1000));
                        if (report.getReportLog().get(1).getRemark() == null ||
                                report.getReportLog().get(1).getRemark().equals("") ||
                                report.getReportLog().get(1).getRemark().equals(" ")) {
                            holder.line1.setVisibility(View.GONE);
                            holder.line2.setVisibility(View.GONE);
                            holder.remark.setVisibility(View.GONE);
                        } else {
                            holder.remark.setText(remarks + report.getReportLog().get(1).getRemark());
                            if (report.getReportLog().get(1).getRemark().length() <= 18) {
                                holder.line1.setVisibility(View.GONE);
                                holder.line2.setVisibility(View.VISIBLE);
                                holder.remark.setVisibility(View.VISIBLE);
                            } else {
                                holder.line1.setVisibility(View.VISIBLE);
                                holder.line2.setVisibility(View.GONE);
                                holder.remark.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                    case 3:
                        holder.status.setText("已带看");
                        holder.time.setText(TimeUtils.getCustomFollowData(report.getReportLog().get(2).getCtime() * 1000));
                        if (report.getReportLog().get(2).getRemark() == null ||
                                report.getReportLog().get(2).getRemark().equals("")) {
                            holder.line1.setVisibility(View.GONE);
                            holder.line2.setVisibility(View.GONE);
                            holder.remark.setVisibility(View.GONE);
                        } else {
                            holder.remark.setText(remarks + report.getReportLog().get(2).getRemark());
                            if (report.getReportLog().get(2).getRemark().length() <= 18) {
                                holder.line1.setVisibility(View.GONE);
                                holder.line2.setVisibility(View.VISIBLE);
                                holder.remark.setVisibility(View.VISIBLE);
                            } else {
                                holder.line1.setVisibility(View.VISIBLE);
                                holder.line2.setVisibility(View.GONE);
                                holder.remark.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                    case 4:
                        holder.status.setText("已认购");
                        holder.time.setText(TimeUtils.getCustomFollowData(report.getReportLog().get(3).getCtime() * 1000));
                        if (report.getReportLog().get(3).getRemark() == null ||
                                report.getReportLog().get(3).getRemark().equals("")) {
                            holder.line1.setVisibility(View.GONE);
                            holder.line2.setVisibility(View.GONE);
                            holder.remark.setVisibility(View.GONE);
                        } else {
                            holder.remark.setText(remarks + report.getReportLog().get(3).getRemark());
                            if (report.getReportLog().get(3).getRemark().length() <= 18) {
                                holder.line1.setVisibility(View.GONE);
                                holder.line2.setVisibility(View.VISIBLE);
                                holder.remark.setVisibility(View.VISIBLE);
                            } else {
                                holder.line1.setVisibility(View.VISIBLE);
                                holder.line2.setVisibility(View.GONE);
                                holder.remark.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                    case 5:
                        holder.status.setText("已成交");
                        holder.time.setText(TimeUtils.getCustomFollowData(report.getReportLog().get(4).getCtime() * 1000));
                        if (report.getReportLog().get(4).getRemark() == null ||
                                report.getReportLog().get(4).getRemark().equals("")) {
                            holder.line1.setVisibility(View.GONE);
                            holder.line2.setVisibility(View.GONE);
                            holder.remark.setVisibility(View.GONE);
                        } else {
                            holder.remark.setText(remarks + report.getReportLog().get(4).getRemark());
                            if (report.getReportLog().get(4).getRemark().length() <= 18) {
                                holder.line1.setVisibility(View.GONE);
                                holder.line2.setVisibility(View.VISIBLE);
                                holder.remark.setVisibility(View.VISIBLE);
                            } else {
                                holder.line1.setVisibility(View.VISIBLE);
                                holder.line2.setVisibility(View.GONE);
                                holder.remark.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                    case 6:
                        holder.status.setText("已结佣");
                        holder.time.setText(TimeUtils.getCustomFollowData(report.getReportLog().get(5).getCtime() * 1000));
                        if (report.getReportLog().get(5).getRemark() == null ||
                                report.getReportLog().get(5).getRemark().equals("")) {
                            holder.line1.setVisibility(View.GONE);
                            holder.line2.setVisibility(View.GONE);
                            holder.remark.setVisibility(View.GONE);
                        } else {
                            holder.remark.setText(remarks + report.getReportLog().get(5).getRemark());
                            if (report.getReportLog().get(5).getRemark().length() <= 18) {
                                holder.line1.setVisibility(View.GONE);
                                holder.line2.setVisibility(View.VISIBLE);
                                holder.remark.setVisibility(View.VISIBLE);
                            } else {
                                holder.line1.setVisibility(View.VISIBLE);
                                holder.line2.setVisibility(View.GONE);
                                holder.remark.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            return convertView;
        }

        private class ViewHolder {
            public TextView buildName;
            public TextView reportNum;
            public TextView status;
            public TextView time;
            public TextView remark;
            public ImageView isend;
            public View line1;
            public View line2;
        }
    }

    //报备列表
    private void getData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getReport");
        map.put("module", Constants.MODULE_CUSTOM);
        map.put("uid", String.valueOf(custom.getUid()));
        map.put("cid", String.valueOf(custom.getCid()));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        progressDialog.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if (code > Result.RESULT_OK) {
                                String json = jsonObject.getJSONArray("content").toString();
                                parseReportJson(json);
                                if (list.size() > 0) {
                                    emptyview.setVisibility(View.GONE);
                                } else {
                                    emptyview.setVisibility(View.VISIBLE);
                                }
                                notifyAdapter();
                            } else {
                                emptyview.setVisibility(View.VISIBLE);
                                Toast.makeText(getActivity(), notice, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        progressDialog.setVisibility(View.GONE);
                    }
                });
    }

    //添加报备
    private void getAddReportData(String bid) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "addReport");
        map.put("module", Constants.MODULE_CUSTOM);
        map.put("uid", String.valueOf(custom.getUid()));
        map.put("cid", String.valueOf(custom.getCid()));
        map.put("id", User.getInstance().salt);
        map.put("tel", custom.getTel());
        map.put("name", custom.getName());
        map.put("price", String.valueOf(custom.getPrice()));
        map.put("layout", String.valueOf(custom.getLayout()));
        map.put("bid", bid);
        map.put("remark", custom.getRemark() + "");
        map.put("style", String.valueOf(Constants.REPORT_CUSTOM));
        map.put("type", String.valueOf(custom.getType()));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {

                    private ProgressDialog dialog;

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog = new ProgressDialog(getActivity());
                        CommonUtils.progressDialogShow(dialog, "正在报备中...");
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null) {
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if (code > Result.RESULT_OK) {
                                CommonUtils.setTaskDone(getActivity(), 23);
                                //插入报备
                                String json = jsonObject.getJSONObject("content").toString();
                                Report r = new Report();
                                ReportBuild reportBuild = new ReportBuild();
                                reportBuild.setName(buildNames);
                                r.setBuild(reportBuild);
                                r.setNumber(parseAddReportJson(json));
                                ArrayList<ReportLog> cList = new ArrayList<ReportLog>();
                                ReportLog reportLog = new ReportLog();
                                reportLog.setStatus(1);
                                reportLog.setCtime(System.currentTimeMillis() / 1000);
                                cList.add(reportLog);
                                r.setReportLog(cList);
                                list.add(0, r);
                                emptyview.setVisibility(View.GONE);
                                notifyAdapter();
                                custom.setStatus(1);
                                custom.setIsend(0);
                                custom.setDATEACTION(Constants.CUSTOM_UPDATA);
                                EventBus.getDefault().post(custom);
                            } else {
                                Toast.makeText(getActivity(), notice, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        CommonUtils.progressDialogDismiss(dialog);
                    }
                });
    }


    //刷新Adapter
    private void notifyAdapter() {
        if (mAdapter == null) {
            mAdapter = new ReportAdapter(getActivity(), list);
            reportList.setAdapter(mAdapter);
        } else {
            mAdapter.data = list;
            mAdapter.notifyDataSetChanged();
        }
    }

    //解析添加报备
    private String parseAddReportJson(String json) {
        String reportNum = "";
        try {
            JSONObject object = new JSONObject(json);
            reportNum = object.getString("number");
        } catch (JSONException e) {
            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
            return null;
        }
        return reportNum;
    }

    //解析报备列表
    private void parseReportJson(String json) {
        try {
            JSONArray array = new JSONArray(json);
            int size = array.length();
            for (int i = 0; i < size; i++) {
                JSONObject object = array.getJSONObject(i);
                Report report = new Report();
                report.setRid(object.getInt("rid"));
                JSONObject obj = object.getJSONObject("build");
                ReportBuild reportBuild = new ReportBuild();
                reportBuild.setName(obj.getString("name"));
                report.setBuild(reportBuild);
                report.setNumber(object.getString("number"));
                report.setIsend(object.getInt("isend"));
                JSONObject obj1 = object.getJSONObject("endLog");
                ReportEndLog endLog = new ReportEndLog();
                if (!obj1.isNull("ctime")) {
                    endLog.setCtime(obj1.getLong("ctime"));
                }
                if (!obj1.isNull("remark")) {
                    endLog.setRemark(obj1.getString("remark"));
                }
                report.setEndLog(endLog);
                JSONArray jsonArray = object.getJSONArray("reportLog");
                List<ReportLog> logList = new ArrayList<ReportLog>();
                for (int j = 0; j < jsonArray.length(); j++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(j);
                    ReportLog reportLog = new ReportLog();
                    reportLog.setStatus(jsonObject.getInt("status"));
                    reportLog.setCtime(jsonObject.getLong("ctime"));
                    reportLog.setRemark(jsonObject.getString("remark"));
                    logList.add(reportLog);
                }
                report.setReportLog(logList);
                list.add(report);
            }
        } catch (JSONException e) {
            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
