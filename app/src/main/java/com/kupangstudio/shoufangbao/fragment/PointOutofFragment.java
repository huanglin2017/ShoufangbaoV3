package com.kupangstudio.shoufangbao.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.PointRecord;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.kupangstudio.shoufangbao.widget.PullToRefreshListView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.request.OkHttpRequest;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Jsmi on 2015/11/25.
 * 积分出账记录
 */
public class PointOutofFragment extends Fragment {
    private PullToRefreshListView refreshListView;
    private ListView lv;
    private MyAdapter adapter;
    private LinearLayout emptyLayout;
    private Button emptyBtn;
    private ArrayList<PointRecord> list = new ArrayList<PointRecord>();
    private RelativeLayout loadingLayout;
    private int page = 1;
    private View footerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_point_outof, container, false);
        initView(view);
        setClickListener();
        getData();
        return view;
    }

    private void setClickListener() {
        emptyBtn.setOnClickListener(listener);
    }

    private void initView(View view) {
        footerView = LayoutInflater.from(getActivity()).inflate(R.layout.common_footer, null);
        View tiao = footerView.findViewById(R.id.common_footer_tiao);
        tiao.setVisibility(View.GONE);
        refreshListView = (PullToRefreshListView) view.findViewById(R.id.pull_refresh_listview);
        emptyLayout = (LinearLayout) view.findViewById(R.id.empty_layout);
        emptyBtn = (Button) view.findViewById(R.id.empty_btn);
        loadingLayout = (RelativeLayout) view.findViewById(R.id.point_out_loading);
        refreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        refreshListView.getLoadingLayoutProxy().setRefreshingLabel("正在刷新...");
        refreshListView.getLoadingLayoutProxy().setReleaseLabel("释放立即刷新");
        refreshListView.getLoadingLayoutProxy().setPullLabel("下拉刷新");
        lv = refreshListView.getRefreshableView();
        refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                refreshListView.setMode(PullToRefreshBase.Mode.BOTH);
                lv.removeFooterView(footerView);
                list.clear();
                page = 0;
                getData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                page++;
                getData();
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.empty_btn:
                    getActivity().finish();
                    break;
                default:
                    break;
            }
        }
    };

    class MyAdapter extends BaseAdapter {

        ArrayList<PointRecord> list;
        LayoutInflater inflater;

        public MyAdapter(Context context, ArrayList<PointRecord> list) {
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
            ViewHolder holder;
            PointRecord integralRecord = list.get(position);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_mypoint, parent, false);
                holder.itemTitle = (TextView)convertView.findViewById(R.id.item_mypoint_title);
                holder.itemPoint = (TextView)convertView.findViewById(R.id.item_mypoint_point);
                holder.itemTime = (TextView)convertView.findViewById(R.id.item_mypoint_time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.itemTitle.setText(integralRecord.getRemark());
            holder.itemPoint.setText("-" + integralRecord.getIntegrate());
            holder.itemTime.setText(TimeUtils.getCustomFollowData(integralRecord.getCtime() * 1000));
            return convertView;
        }
    }

    static class ViewHolder {
        TextView itemTitle;
        TextView itemPoint;
        TextView itemTime;
    }

    private void getData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getIntegrate");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        map.put("type", "2");
        map.put("offset", String.valueOf(page));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new PointOutCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        loadingLayout.setVisibility(View.GONE);
                        refreshListView.onRefreshComplete();
                        emptyLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        lv.addFooterView(footerView);
                        list = (ArrayList<PointRecord>) DataSupport.where("uid = ? and type = ?",
                                String.valueOf(User.getInstance().uid), "2").order("ctime desc").find(PointRecord.class);
                        if (adapter == null) {
                            adapter = new MyAdapter(getActivity(), list);
                            lv.setAdapter(adapter);
                        } else {
                            adapter.list = list;
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onResponse(Result<ArrayList<PointRecord>> response) {
                        if (getActivity() == null) {
                            return;
                        }
                        if (response == null) {
                            loadingLayout.setVisibility(View.GONE);
                            refreshListView.onRefreshComplete();
                            emptyLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                            lv.addFooterView(footerView);
                            list = (ArrayList<PointRecord>) DataSupport.where("uid = ? and type = ?",
                                    String.valueOf(User.getInstance().uid), "2").order("ctime desc").find(PointRecord.class);
                            if (adapter == null) {
                                adapter = new MyAdapter(getActivity(), list);
                                lv.setAdapter(adapter);
                            } else {
                                adapter.list = list;
                                adapter.notifyDataSetChanged();
                            }
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        loadingLayout.setVisibility(View.GONE);
                        emptyLayout.setVisibility(View.GONE);
                        refreshListView.onRefreshComplete();
                        User user = User.getInstance();
                        if (response.getCode() > Result.RESULT_OK) {
                            if (response.getContent() == null) {
                                lv.addFooterView(footerView);
                                refreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                            } else {
                                list.addAll(response.getContent());
                            }
                            if (response.getContent().size() < 20) {
                                refreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                                lv.addFooterView(footerView);
                            }
                            if (adapter == null) {
                                adapter = new MyAdapter(getActivity(), list);
                                lv.setAdapter(adapter);
                            } else {
                                adapter.list = list;
                                adapter.notifyDataSetChanged();
                            }
                            if (page == 1) {
                                DataSupport.deleteAll(PointRecord.class, "uid = ? and type = ?",
                                        String.valueOf(user.uid), "2");
                                CommonUtils.addType(list, 2);
                                DataSupport.saveAll(list);
                            } else {
                                List<PointRecord> data = response.getContent();
                                CommonUtils.addType(data, 2);
                                DataSupport.saveAll(data);
                            }
                        } else {
                            refreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                            if (response.getCode() == 1034 && page > 1) {
                                page--;
                                Toast.makeText(getActivity(), "暂无更多", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), response.getNotice(), Toast.LENGTH_SHORT).show();

                                list = (ArrayList<PointRecord>) DataSupport.where("uid = ? and type = ?",
                                        String.valueOf(user.uid), "2").order("ctime desc").find(PointRecord.class);
                                if (adapter == null) {
                                    adapter = new MyAdapter(getActivity(), list);
                                    lv.setAdapter(adapter);
                                } else {
                                    adapter.list = list;
                                    adapter.notifyDataSetChanged();
                                }
                            }
                            emptyLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private abstract class PointOutCallback extends Callback<Result<ArrayList<PointRecord>>> {
        @Override
        public Result<ArrayList<PointRecord>> parseNetworkResponse(Response response) throws Exception {
            String json = response.body().string();
            Result<ArrayList<PointRecord>> listResult = null;
            try {
                listResult = new Gson().fromJson(json,
                        new TypeToken<Result<ArrayList<PointRecord>>>(){}.getType());
            }catch (Exception e) {
                return null;
            }
            return listResult;
        }
    }

}
