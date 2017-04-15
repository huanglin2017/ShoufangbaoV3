package com.kupangstudio.shoufangbao;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Developer;
import com.kupangstudio.shoufangbao.model.DeveloperBuild;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.DensityUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.request.OkHttpRequest;

import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by long1 on 15/11/16.
 * Copyright 15/11/16 android_xiaobai.
 * 合作品牌
 */
public class CooperBrandActivity extends BaseActivity {

    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private RelativeLayout emptyView;
    private Button emptyBtn;
    private TextView emptyText;
    private PullToRefreshListView refreshListView;
    private ListView mListView;
    private int cityId;
    private List<Developer> list;
    private ItemAdapter itemAdapter;
    private MyAdapter adapter;
    private RelativeLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooper_brand);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightText(this, "合作品牌", "合作说明", mClickListener);
        CommonUtils.setTaskDone(CooperBrandActivity.this, 1);
        initView();
        init();
        setClickListener();
        getData();
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

    private void initView() {
        refreshListView = (PullToRefreshListView) findViewById(R.id.cooper_brand_refresh);
        emptyView = (RelativeLayout) findViewById(R.id.emptyview);
        emptyBtn = (Button) findViewById(R.id.emptyview_btn);
        emptyText = (TextView) findViewById(R.id.emptyview_text);
        loadingLayout = (RelativeLayout) findViewById(R.id.loading_cooper);
    }

    private void init() {
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        cityId = (int) SPUtils.get(CooperBrandActivity.this, Constants.BUILD_CITY_ID, 1);
        refreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
        mListView = refreshListView.getRefreshableView();
        mListView.setDividerHeight(DensityUtils.dp2px(CooperBrandActivity.this, 10));
        emptyText.setText("合作商都去找地方盖房子给你们住了。");
    }

    private void setClickListener() {
        emptyBtn.setOnClickListener(mClickListener);
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.navbar_text_right:
                    Intent intent = new Intent(CooperBrandActivity.this, CommonRuleActivity.class);
                    intent.putExtra("title", "合作说明");
                    intent.putExtra("url", "https://www.shoufangbao.com/index.php?r=appweb/cooperate");
                    startActivity(intent);
                    break;
                case R.id.emptyview_btn:
                    getData();
                    break;
                default:
                    break;
            }
        }
    };

    private void getData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getBuildDevelop");
        map.put("module", Constants.MODULE_BUILD);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("cityId", String.valueOf(cityId));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new DeveloperCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        loadingLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(CooperBrandActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        List<Developer> list = DataSupport.where("cityid = ?", String.valueOf(cityId)).find(Developer.class);
                        for (Developer developer:list) {
                            List<DeveloperBuild> build = DataSupport.where("developer_id = ?", String.valueOf(developer.getId())).find(DeveloperBuild.class);
                            if(build != null && build.size() > 0) {
                                developer.setBuild(build);
                            }
                        }
                        setAdapter(list);
                    }

                    @Override
                    public void onResponse(Result<List<Developer>> response) {
                        if(response == null) {
                            Toast.makeText(CooperBrandActivity.this, ResultError.MESSAGE_ERROR
                            ,Toast.LENGTH_SHORT).show();
                            List<Developer> list = DataSupport.where("cityid = ?", String.valueOf(cityId)).find(Developer.class);
                            for (Developer developer:list) {
                                List<DeveloperBuild> build = DataSupport.where("developer_id = ?", String.valueOf(developer.getId())).find(DeveloperBuild.class);
                                if(build != null && build.size() > 0) {
                                    developer.setBuild(build);
                                }
                            }
                            setAdapter(list);
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            emptyView.setVisibility(View.GONE);
                            list = response.getContent();
                            for (Developer developer:list) {
                                List<DeveloperBuild> builds = developer.getDeveloperBuilds();
                                DataSupport.saveAll(builds);
                                developer.save();
                            }
                            setAdapter(list);
                        } else {
                            Toast.makeText(CooperBrandActivity.this, response.getNotice(), Toast.LENGTH_SHORT).show();
                            List<Developer> list = DataSupport.where("cityid = ?", String.valueOf(cityId)).find(Developer.class);
                            for (Developer developer:list) {
                                List<DeveloperBuild> build = DataSupport.where("developer_id = ?", String.valueOf(developer.getId())).find(DeveloperBuild.class);
                                if(build != null && build.size() > 0) {
                                    developer.setBuild(build);
                                }
                            }
                            setAdapter(list);
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        loadingLayout.setVisibility(View.GONE);
                    }
                });
    }

    private abstract class DeveloperCallback extends Callback<Result<List<Developer>>> {
        @Override
        public Result<List<Developer>> parseNetworkResponse(Response response) throws Exception {
            Result<List<Developer>> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<List<Developer>>>(){}.getType());
            }catch (Exception e){
                return null;
            }
            return result;
        }
    }

    private void setAdapter(List<Developer> list) {
        if (list == null || list.size() <= 0) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            if (adapter == null) {
                adapter = new MyAdapter(CooperBrandActivity.this, list);
                mListView.setAdapter(adapter);
            } else {
                adapter.list = list;
                adapter.notifyDataSetChanged();
            }
        }
    }

    class MyAdapter extends BaseAdapter {

        List<Developer> list;
        LayoutInflater inflater;

        public MyAdapter(Context ctx, List<Developer> list) {
            this.list = list;
            inflater = LayoutInflater.from(ctx);
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
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_cooper_brand, parent, false);
                holder.ivLogo = (ImageView) convertView.findViewById(R.id.item_developer_logo);
                holder.tvTitle = (TextView) convertView.findViewById(R.id.item_developer_title);
                holder.tvContent = (TextView) convertView.findViewById(R.id.item_developer_content);
                holder.itemLv = (ListView) convertView.findViewById(R.id.item_developer_list);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Developer developer = list.get(position);
            imageLoader.displayImage(developer.getPic(), holder.ivLogo, options);
            holder.tvTitle.setText(developer.getName());
            holder.tvContent.setText(developer.getIntro());
            final List<DeveloperBuild> developerBuilds = developer.getBuild();
            itemAdapter = new ItemAdapter(CooperBrandActivity.this, developerBuilds);
            holder.itemLv.setAdapter(itemAdapter);
            holder.itemLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int bid = developerBuilds.get(position).getBid();
                    Intent it = new Intent(CooperBrandActivity.this, BuildDetailActivity.class);
                    it.putExtra("bid", bid);
                    startActivity(it);
                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView ivLogo;
        TextView tvTitle;
        TextView tvContent;
        ListView itemLv;
    }

    class ItemAdapter extends BaseAdapter {

        LayoutInflater inflater;
        List<DeveloperBuild> list;

        public ItemAdapter(Context ctx, List<DeveloperBuild> list) {
            inflater = LayoutInflater.from(ctx);
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
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_item_cooper_brand, parent, false);
                holder.itemContent = (TextView) convertView.findViewById(R.id.item_item_developer_title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            DeveloperBuild buildList = list.get(position);
            holder.itemContent.setText("[" + buildList.getAreaname() + "]" + " " + buildList.getName());
            return convertView;
        }

        class ViewHolder {
            TextView itemContent;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
