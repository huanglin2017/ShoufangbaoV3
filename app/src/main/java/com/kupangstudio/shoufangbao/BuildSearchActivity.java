package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.ActSearch;
import com.kupangstudio.shoufangbao.model.BuildSearch;
import com.kupangstudio.shoufangbao.model.HistorySearch;
import com.kupangstudio.shoufangbao.model.HotSearch;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.widget.MyListView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

import static com.kupangstudio.shoufangbao.R.id.tv_empty;

/**
 * 楼盘搜索界面
 */
public class BuildSearchActivity extends BaseActivity {
    //热门搜索
    private LinearLayout layoutHot;
    private RecyclerView recyclerViewHot;
    private HotAdapter hotAdapter;
    private List<HotSearch> hotList;
    private int cityid;
    //历史搜索
    private MyListView lvHistory;
    private LinearLayout layoutHistory;
    private List<HistorySearch> historyList;
    private MyAdapter historyAdapter;
    //活动
    private LinearLayout layoutAct;
    private MyListView lvAct;
    private List<ActSearch> actList;
    private ActAdapter actAdapter;
    private TextView tvCancel;//取消
    private EditText etSearch;
    private MyListView lvSearch;
    private SearchAdapter searchAdapter;
    private List<HistorySearch> searchList;
    private LinearLayout layoutAll;
    private TextView tvEmpty;
    private ImageView ivDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_search);
        CommonUtils.addActivity(this);
        cityid = getIntent().getIntExtra("cityid", 1);
        initView();
        ivDelete.setOnClickListener(onClickListener);
        tvCancel.setOnClickListener(onClickListener);
        lvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistorySearch search = searchList.get(position);
                addSearchHistory(search.getBid(), search.getName());
                HistorySearch historySearch = new HistorySearch();
                historySearch.setName(search.getName());
                historySearch.setBid(search.getBid());
                boolean isNeedAdd = true;
                int pos = 0;
                for (int i = 0; i < historyList.size(); i++) {
                    if (historyList.get(i).getName().equals(historySearch.getName())) {
                        isNeedAdd = false;
                        pos = i;
                        break;
                    }
                }
                if (isNeedAdd) {
                    if (historyList.size() == 6) {
                        historyList.remove(5);
                    }
                    historyList.add(0, historySearch);
                } else {
                    if (pos != 0) {
                        HistorySearch search1 = historyList.get(pos);
                        historyList.remove(pos);
                        historyList.add(0, search1);
                    }
                }
                if (historyList != null && historyList.size() > 0) {
                    layoutHistory.setVisibility(View.VISIBLE);
                    historyAdapter = new MyAdapter(historyList);
                    lvHistory.setAdapter(historyAdapter);
                } else {
                    layoutHistory.setVisibility(View.GONE);
                }
                Intent intent = new Intent(BuildSearchActivity.this, BuildDetailActivity.class);
                intent.putExtra("bid", search.getBid());
                startActivity(intent);
            }
        });
        lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistorySearch search = historyList.get(position);
                Intent intent = new Intent(BuildSearchActivity.this, BuildDetailActivity.class);
                intent.putExtra("bid", search.getBid());
                startActivity(intent);
            }
        });
        lvAct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ActSearch actSearch = actList.get(position);
                Intent intent = new Intent(BuildSearchActivity.this, ShareBannerActivity.class);
                intent.putExtra("title", actSearch.getName());
                intent.putExtra("url", actSearch.getUrl());
                startActivity(intent);
            }
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    ivDelete.setVisibility(View.GONE);
                    lvSearch.setVisibility(View.GONE);
                    layoutAll.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                } else {
                    ivDelete.setVisibility(View.VISIBLE);
                    if (searchList != null && searchList.size() > 0) {
                        searchList.clear();
                        if (searchAdapter == null) {
                            searchAdapter = new SearchAdapter(searchList);
                            lvSearch.setAdapter(searchAdapter);
                        } else {
                            searchAdapter.list = searchList;
                            searchAdapter.notifyDataSetChanged();
                        }
                    }
                    layoutAll.setVisibility(View.GONE);
                    searchData();
                }
            }
        });
        getData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        etSearch.setText("");
    }

    private void addSearchHistory(int bid, String name) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "addSelect");
        map.put("module", Constants.MODULE_BUILD);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("bid", String.valueOf(bid));
        map.put("name", name);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {

                    }
                });
    }

    //搜索
    private void searchData() {
        tvEmpty.setVisibility(View.GONE);
        final String s = etSearch.getText().toString();
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "searchBuild");
        map.put("module", Constants.MODULE_BUILD);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("cityid", String.valueOf(cityid));
        map.put("name", s);
        String search = etSearch.getText().toString();
        if (!search.equals(s)) {
            return;
        }
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new HistoryCallBack() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(Result<List<HistorySearch>> response) {
                        if (response == null) {
                            return;
                        }
                        String search = etSearch.getText().toString();
                        if (!search.equals(s)) {
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            tvEmpty.setVisibility(View.GONE);
                            lvSearch.setVisibility(View.VISIBLE);
                            searchList = response.getContent();
                            if (searchAdapter == null) {
                                searchAdapter = new SearchAdapter(searchList);
                                lvSearch.setAdapter(searchAdapter);
                            } else {
                                searchAdapter.list = searchList;
                                searchAdapter.notifyDataSetChanged();
                            }
                        } else {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private abstract class HistoryCallBack extends Callback<Result<List<HistorySearch>>> {
        @Override
        public Result<List<HistorySearch>> parseNetworkResponse(Response response) throws Exception {
            Result<List<HistorySearch>> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<List<HistorySearch>>>() {
                        }.getType());
            } catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    private void initView() {
        recyclerViewHot = (RecyclerView) findViewById(R.id.recycle_build_search_hot);
        layoutHot = (LinearLayout) findViewById(R.id.layout_hot);
        layoutHistory = (LinearLayout) findViewById(R.id.layout_history);
        lvHistory = (MyListView) findViewById(R.id.lv_search_history);
        layoutAct = (LinearLayout) findViewById(R.id.layout_active);
        tvCancel = (TextView) findViewById(R.id.tv_cancel);
        lvAct = (MyListView) findViewById(R.id.lv_act);
        etSearch = (EditText) findViewById(R.id.search_build);
        lvSearch = (MyListView) findViewById(R.id.lv_search);
        layoutAll = (LinearLayout) findViewById(R.id.layout_all_search);
        tvEmpty = (TextView) findViewById(tv_empty);
        ivDelete = (ImageView) findViewById(R.id.iv_delete);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_cancel:
                    finish();
                    break;
                case R.id.iv_delete:
                    etSearch.setText("");
                    break;
                default:
                    break;
            }
        }
    };

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    //热门搜索数据绑定
    class HotAdapter extends RecyclerView.Adapter<HotAdapter.MyViewHolder> {

        private OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(BuildSearchActivity.this)
                    .inflate(R.layout.item_build_search_hot, parent, false);
            MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            holder.tv.setText(hotList.get(position).getName());
            if (onItemClickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getLayoutPosition();
                        onItemClickListener.onItemClick(holder.itemView, position);
                    }
                });
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int position = holder.getLayoutPosition();
                        onItemClickListener.onItemLongClick(holder.itemView, position);
                        return true;
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return hotList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tv;

            public MyViewHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(R.id.tv_hot);
            }
        }

    }

    //获取搜索
    private void getData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "beforeSearch");
        map.put("module", Constants.MODULE_BUILD);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("cityid", String.valueOf(cityid));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new HotCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        layoutHot.setVisibility(View.GONE);
                        layoutHistory.setVisibility(View.GONE);
                        layoutAct.setVisibility(View.GONE);
                    }

                    @Override
                    public void onResponse(Result<BuildSearch> response) {
                        if (response == null) {
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            layoutHot.setVisibility(View.VISIBLE);
                            hotList = response.getContent().getHot();
                            hotAdapter = new HotAdapter();
                            LinearLayoutManager manager = new LinearLayoutManager(BuildSearchActivity.this);
                            manager.setOrientation(LinearLayoutManager.HORIZONTAL);
                            recyclerViewHot.setLayoutManager(manager);
                            recyclerViewHot.setAdapter(hotAdapter);
                            hotAdapter.setOnItemClickListener(new OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    HotSearch hot = hotList.get(position);
                                    addSearchHistory(hot.getBid(), hot.getName());
                                    HistorySearch historySearch = new HistorySearch();
                                    historySearch.setName(hot.getName());
                                    historySearch.setBid(hot.getBid());
                                    boolean isNeedAdd = true;
                                    int pos = 0;
                                    for (int i = 0; i < historyList.size(); i++) {
                                        if (historyList.get(i).getName().equals(historySearch.getName())) {
                                            isNeedAdd = false;
                                            pos = i;
                                            break;
                                        }
                                    }
                                    if (isNeedAdd) {
                                        if (historyList.size() == 6) {
                                            historyList.remove(5);
                                        }
                                        historyList.add(0, historySearch);
                                    } else {
                                        if (pos != 0) {
                                            HistorySearch search1 = historyList.get(pos);
                                            historyList.remove(pos);
                                            historyList.add(0, search1);
                                        }
                                    }
                                    if (historyList != null && historyList.size() > 0) {
                                        layoutHistory.setVisibility(View.VISIBLE);
                                        historyAdapter = new MyAdapter(historyList);
                                        lvHistory.setAdapter(historyAdapter);
                                    } else {
                                        layoutHistory.setVisibility(View.GONE);
                                    }
                                    Intent intent = new Intent(BuildSearchActivity.this, BuildDetailActivity.class);
                                    intent.putExtra("bid", hot.getBid());
                                    startActivity(intent);
                                }

                                @Override
                                public void onItemLongClick(View view, int position) {

                                }
                            });
                            historyList = response.getContent().getRecord();
                            if (historyList != null && historyList.size() > 0) {
                                layoutHistory.setVisibility(View.VISIBLE);
                                historyAdapter = new MyAdapter(historyList);
                                lvHistory.setAdapter(historyAdapter);
                            } else {
                                layoutHistory.setVisibility(View.GONE);
                            }
                            actList = response.getContent().getActive();
                            if (actList != null && actList.size() > 0) {
                                layoutAct.setVisibility(View.VISIBLE);
                                actAdapter = new ActAdapter(actList);
                                lvAct.setAdapter(actAdapter);
                            } else {
                                layoutAct.setVisibility(View.GONE);
                            }
                        } else {
                            layoutHot.setVisibility(View.GONE);
                            layoutHistory.setVisibility(View.GONE);
                            layoutAct.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private abstract class HotCallback extends Callback<Result<BuildSearch>> {
        @Override
        public Result<BuildSearch> parseNetworkResponse(Response response) throws Exception {
            Result<BuildSearch> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<BuildSearch>>() {
                        }.getType());
            } catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    //历史搜索
    class MyAdapter extends BaseAdapter {

        List<HistorySearch> list;

        public MyAdapter(List<HistorySearch> list) {
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
            convertView = LayoutInflater.from(BuildSearchActivity.this).inflate(
                    R.layout.item_build_search_history
                    , parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.tv_history);
            HistorySearch historySearch = list.get(position);
            textView.setText(historySearch.getName());
            return convertView;
        }
    }

    //活动
    class ActAdapter extends BaseAdapter {
        List<ActSearch> list;

        public ActAdapter(List<ActSearch> list) {
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(BuildSearchActivity.this).inflate(
                    R.layout.item_build_search_history
                    , parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.tv_history);
            ActSearch actSearch = list.get(position);
            textView.setText(actSearch.getName());
            textView.setTextColor(getResources().getColor(R.color.common_select));
            return convertView;
        }
    }

    //搜索结果
    class SearchAdapter extends BaseAdapter {

        List<HistorySearch> list;

        public SearchAdapter(List<HistorySearch> list) {
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
            convertView = LayoutInflater.from(BuildSearchActivity.this).inflate(
                    R.layout.item_build_search_history
                    , parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.tv_history);
            HistorySearch historySearch = list.get(position);
            textView.setText(historySearch.getName());
            return convertView;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

}
