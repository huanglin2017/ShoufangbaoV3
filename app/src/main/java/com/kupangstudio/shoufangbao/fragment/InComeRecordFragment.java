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
public class InComeRecordFragment extends Fragment {

    private PullToRefreshListView refreshListView;
    private ListView lv;
    private RelativeLayout emptyLayout;
    private TextView emptyText;
    private Button emptyButton;
    private ImageView emptyImage;
    private List<InRecord> list;
    private MyAdapter adapter;
    private boolean isFirst = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income_record, container, false);
        initView(view);
        init();
        getInComeRecord();
        refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                isFirst = false;
                getInComeRecord();
            }
        });
        return view;
    }

    private void initView(View view) {
        refreshListView = (PullToRefreshListView) view.findViewById(R.id.income_record_list);
        emptyLayout = (RelativeLayout) view.findViewById(R.id.emptyview);
        emptyText = (TextView) view.findViewById(R.id.emptyview_text);
        emptyButton = (Button) view.findViewById(R.id.emptyview_btn);
        emptyImage = (ImageView) view.findViewById(R.id.emptyview_img);
        lv = refreshListView.getRefreshableView();
        lv.setEmptyView(emptyLayout);
    }

    private void init() {
        emptyText.setText("钱包空空的，赶紧去奋斗！");
        emptyButton.setVisibility(View.GONE);
        emptyImage.setImageResource(R.drawable.empty_nosign);
    }

    private void getInComeRecord() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getAmount");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new RecordCallBack() {
                    @Override
                    public void onError(Call call, Exception e) {
                        if (getActivity() == null) {
                            return;
                        }
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        list = DataSupport.findAll(InRecord.class);
                        setAdapter();
                    }

                    @Override
                    public void onResponse(Result<ArrayList<InRecord>> response) {
                        if (getActivity() == null) {
                            list = DataSupport.findAll(InRecord.class);
                            setAdapter();
                            return;
                        }
                        if (response == null) {
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            list = response.getContent();
                            DataSupport.deleteAll(InRecord.class);
                            DataSupport.saveAll(list);
                        } else {
                            if (response.getCode() == 1030) {
                                list = new ArrayList<InRecord>();
                            } else {
                                list = DataSupport.findAll(InRecord.class);
                            }
                            Toast.makeText(getActivity(), response.getNotice(), Toast.LENGTH_SHORT).show();
                        }
                        setAdapter();
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        if(!isFirst){
                            refreshListView.onRefreshComplete();
                        }
                    }
                });
    }

    private abstract class RecordCallBack extends Callback<Result<ArrayList<InRecord>>> {
        @Override
        public Result<ArrayList<InRecord>> parseNetworkResponse(Response response) throws Exception {
            String jsonObject = response.body().string();
            Result<ArrayList<InRecord>> list = null;
            try {
                list = new Gson().fromJson(jsonObject,
                        new TypeToken<Result<ArrayList<InRecord>>>(){}.getType());
            }catch (Exception e) {
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
        List<InRecord> list;

        public MyAdapter(Context context, List<InRecord> list) {
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
            InRecord inRecord = list.get(position);
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
                holder.statusview.setText("已提现");
            } else {
                holder.statusview.setText("未提现");
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
