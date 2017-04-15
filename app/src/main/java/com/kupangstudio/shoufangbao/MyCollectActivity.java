package com.kupangstudio.shoufangbao;

import android.app.ProgressDialog;
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
import android.widget.LinearLayout;
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
import com.kupangstudio.shoufangbao.model.Build;
import com.kupangstudio.shoufangbao.model.BuildDetail;
import com.kupangstudio.shoufangbao.model.SwitchEvent;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by long1 on 15/11/21.
 * Copyright 15/11/21 android_xiaobai.
 * 我的收藏
 */
public class MyCollectActivity extends BaseActivity {

    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private PullToRefreshListView refreshListView;
    private ListView lv;
    private ArrayList<Build> builds;
    private ProgressDialog dialog;
    private BuildAdapter adapter;
    private RelativeLayout emptyLayout;
    private Button emptyBtn;
    private TextView emptyText;
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collect);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "我的收藏");
        initView();
        init();
        getCollectData();
        EventBus.getDefault().register(this);
    }

    private void initView() {
        refreshListView = (PullToRefreshListView) findViewById(R.id.my_collect_list);
        emptyLayout = (RelativeLayout) findViewById(R.id.emptyview);
        emptyBtn = (Button) findViewById(R.id.emptyview_btn);
        emptyText = (TextView) findViewById(R.id.emptyview_text);
    }

    private void init() {
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        lv = refreshListView.getRefreshableView();
        emptyBtn.setText("去收藏");
        emptyText.setText("收藏个好盘，像挑选另一半一样重要！");
        emptyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new SwitchEvent(0));
                Intent it = new Intent(MyCollectActivity.this, MainActivity.class);
                startActivity(it);
                finish();
            }
        });
        lv.setEmptyView(emptyLayout);
        refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                isFirst = false;
                getCollectData();
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Build hb = builds.get(position - 1);
                int total = hb.getBrower() + 1;
                TextView num = (TextView) view.findViewById(R.id.item_build_browse_num);
                num.setText(total + "");
                hb.setBrower(total);
                builds.set(position - 1, hb);
                Intent it = new Intent(MyCollectActivity.this, BuildDetailActivity.class);
                it.putExtra("bid", hb.getBid());
                startActivity(it);
            }
        });
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

    public void onEventMainThread(BuildDetail detail) {
        for (int i = 0; i < builds.size(); i++) {
            if (builds.get(i).getBid() == detail.getBid()) {
                builds.remove(i);
            }
        }
        if (adapter == null) {
            adapter = new BuildAdapter(MyCollectActivity.this, builds);
            lv.setAdapter(adapter);
        } else {
            adapter.list = builds;
            adapter.notifyDataSetChanged();
        }
    }

    private void getCollectData() {
        dialog = new ProgressDialog(MyCollectActivity.this);
        dialog.setMessage("请稍后...");
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getCollect");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new BuildCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        if (isFirst) {
                            dialog.show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(MyCollectActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Result<ArrayList<Build>> response) {
                        if(response == null) {
                            Toast.makeText(MyCollectActivity.this, ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            builds = response.getContent();
                            if(builds == null) {
                                builds = new ArrayList<Build>();
                            }
                            if (adapter == null) {
                                adapter = new BuildAdapter(MyCollectActivity.this, builds);
                                lv.setAdapter(adapter);
                            } else {
                                adapter.list = builds;
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(MyCollectActivity.this, response.getNotice(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        if (isFirst && dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        if (!isFirst) {
                            refreshListView.onRefreshComplete();
                        }
                    }
                });
    }

    private abstract class BuildCallback extends Callback<Result<ArrayList<Build>>>{
        @Override
        public Result<ArrayList<Build>> parseNetworkResponse(Response response) throws Exception {
            Result<ArrayList<Build>> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<ArrayList<Build>>>(){}.getType());
            }catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    class BuildAdapter extends BaseAdapter {

        List<Build> list;
        LayoutInflater inflater;

        public BuildAdapter(Context ctx, List<Build> list) {
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
            ViewHolder holder = null;
            Build build = list.get(position);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_buildfragment_list, parent, false);
                holder.buildImage = (ImageView) convertView.findViewById(R.id.item_build_image);
                holder.browseNum = (TextView) convertView.findViewById(R.id.item_build_browse_num);
                holder.buildHongbao = (ImageView) convertView.findViewById(R.id.item_build_hongbao);
                holder.buildAngle = (ImageView) convertView.findViewById(R.id.item_build_angle);
                holder.district = (TextView) convertView.findViewById(R.id.item_build_district);
                holder.name = (TextView) convertView.findViewById(R.id.item_build_name);
                holder.price = (TextView) convertView.findViewById(R.id.item_build_price);
                holder.commission = (TextView) convertView.findViewById(R.id.item_build_commission);
                holder.distanceLl = (LinearLayout) convertView.findViewById(R.id.item_build_diatance);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.name.setText(build.getName());
            holder.district.setText("[" + build.getArea() + "]");
            holder.price.setText(build.getPrice());
            if (User.getInstance().verify == User.USER_THROUGH) {
                holder.commission.setText(build.getCommission());
            } else {
                holder.commission.setText("认证可见");
            }
            int distance = 0;
            if (build.getDistance() == null || build.getDistance().equals("") || build.getDistance().equals("0")
                    || Integer.parseInt(build.getDistance()) >= 100000) {
                holder.distanceLl.setVisibility(View.GONE);
            } else {
                holder.distanceLl.setVisibility(View.VISIBLE);
                distance = Integer.parseInt(build.getDistance());
            }
            DecimalFormat df2 = new DecimalFormat("#0.00");//这样为保持2位
            holder.browseNum.setText("距您" + df2.format(distance / 1000.0) + "km");
            switch (build.getLabel()) {
                case Build.NEW:
                    if (build.getUnique_business() == 1) {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.unique_o2o);
                    } else {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.build_angle_new);
                    }
                    break;
                case Build.JIAN:
                    if (build.getUnique_business() == 1) {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.unique_o2o);
                    } else {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.build_angle_jian);
                    }
                    break;
                case Build.RED_PACKETS:
                case Build.DAIKAN_PACKET:
                    holder.buildAngle.setVisibility(View.VISIBLE);
                    holder.buildAngle.setImageResource(R.drawable.build_angle_packet);
                    break;
                default:
                    if (build.getUnique_business() == 1) {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.unique_o2o);
                    } else {
                        holder.buildAngle.setVisibility(View.GONE);
                    }
                    break;
            }

            imageLoader.displayImage(build.getPic(), holder.buildImage, options);
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView buildImage;
        TextView browseNum;
        ImageView buildHongbao;
        ImageView buildAngle;
        TextView district;
        TextView name;
        TextView price;
        TextView commission;
        LinearLayout distanceLl;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
        EventBus.getDefault().unregister(this);
    }
}
