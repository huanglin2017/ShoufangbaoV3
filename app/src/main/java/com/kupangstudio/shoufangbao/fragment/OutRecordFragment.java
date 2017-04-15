package com.kupangstudio.shoufangbao.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.InRecord;
import com.kupangstudio.shoufangbao.model.OutRecord;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.AmnountUtils;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by long1 on 15/11/24.
 * Copyright 15/11/24 android_xiaobai.
 */
public class OutRecordFragment extends Fragment {

    private PullToRefreshListView refreshListView;
    private RelativeLayout emptyLayout;
    private TextView emptyText;
    private ImageView emptyImage;
    private Button emptyButton;
    private ListView lv;
    private boolean isFirst = true;
    private List<OutRecord> list;
    private MyAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_out_record, container, false);
        initView(view);
        init();
        lv.setEmptyView(emptyLayout);
        getOutRecord();
        refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                isFirst = false;
                getOutRecord();
            }
        });
        return view;
    }

    private void initView(View view) {
        emptyLayout = (RelativeLayout) view.findViewById(R.id.emptyview);
        emptyText = (TextView) view.findViewById(R.id.emptyview_text);
        emptyImage = (ImageView) view.findViewById(R.id.emptyview_img);
        emptyButton = (Button) view.findViewById(R.id.emptyview_btn);
        refreshListView = (PullToRefreshListView) view.findViewById(R.id.out_record_list);
    }

    private void init() {
        emptyButton.setVisibility(View.GONE);
        emptyImage.setImageResource(R.drawable.empty_nosign);
        emptyText.setText("您还没有提现记录哦！");
        lv = refreshListView.getRefreshableView();
    }

    private void getOutRecord() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getAccount");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new OutRecordCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        if (getActivity() == null) {
                            return;
                        }
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        list = DataSupport.findAll(OutRecord.class);
                        setAdapter();
                    }

                    @Override
                    public void onResponse(Result<ArrayList<OutRecord>> response) {
                        if (getActivity() == null) {
                            return;
                        }
                        if(response == null) {
                            list = DataSupport.findAll(OutRecord.class);
                            setAdapter();
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            list = response.getContent();
                            DataSupport.deleteAll(OutRecord.class);
                            DataSupport.saveAll(list);
                        } else {
                            if (response.getCode() == 1032) {
                                list = new ArrayList<OutRecord>();
                            } else {
                                list = DataSupport.findAll(OutRecord.class);
                            }
                            Toast.makeText(getActivity(), response.getNotice(), Toast.LENGTH_SHORT).show();
                        }
                        setAdapter();
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        if (!isFirst) {
                            refreshListView.onRefreshComplete();
                        }
                    }
                });
    }

    private abstract class OutRecordCallback extends Callback<Result<ArrayList<OutRecord>>> {
        @Override
        public Result<ArrayList<OutRecord>> parseNetworkResponse(Response response) throws Exception {
            Result<ArrayList<OutRecord>> list = null;
            String json = response.body().string();
            try {
                list = new Gson().fromJson(json,
                        new TypeToken<Result<ArrayList<OutRecord>>>() {
                        }.getType());
            } catch (Exception e) {
                return null;
            }
            return list;
        }
    }

    private void setAdapter() {
        if (adapter == null) {
            adapter = new MyAdapter(getActivity(), list);
            lv.setAdapter(adapter);
        } else {
            adapter.list = list;
            adapter.notifyDataSetChanged();
        }
    }

    class MyAdapter extends BaseAdapter {

        LayoutInflater inflater;
        List<OutRecord> list;

        public MyAdapter(Context context, List<OutRecord> list) {
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
            OutRecord inRecord = list.get(position);
            if (convertView == null) {

                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_commission, parent, false);
                holder.timeview = (TextView) convertView
                        .findViewById(R.id.item_commissiontime);
                holder.typeview = (TextView) convertView
                        .findViewById(R.id.item_commissiontype);
                holder.moneyview = (TextView) convertView
                        .findViewById(R.id.item_commissionmoney);
                holder.statusview = (TextView) convertView
                        .findViewById(R.id.item_commissionstatus);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.timeview.setText(TimeUtils.getCustomReportData(inRecord.getCtime() * 1000));
            holder.typeview.setText(inRecord.getRemark());
            try {
                holder.moneyview.setText("+" + AmnountUtils.changeF2Y(inRecord.getAmount()) + "元");
            } catch (Exception e) {
                holder.moneyview.setText("");
            }
            if (inRecord.getStatus() == InRecord.STATUS_DONE) {
                holder.statusview.setText("已汇出");
            } else if (inRecord.getStatus() == InRecord.STATUS_UNCASH) {
                holder.statusview.setText("处理中");
            } else {
                holder.statusview.setText("提现失败");
            }
            return convertView;
        }
    }

    static class ViewHolder {
        TextView timeview;
        TextView typeview;
        TextView moneyview;
        TextView statusview;
    }

}
