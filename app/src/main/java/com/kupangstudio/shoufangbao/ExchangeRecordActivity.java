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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.ExchangeRecord;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.kupangstudio.shoufangbao.widget.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Jsmi on 2015/11/24.
 * 兑换记录
 */
public class ExchangeRecordActivity extends BaseActivity {
    private ImageView left;
    private TextView right;
    private PullToRefreshListView refreshListView;
    private ListView lv;
    private ArrayList<ExchangeRecord> list;
    private LinearLayout emptyLayout;//空状态
    private Button emptyBtn;
    private RelativeLayout loadingLayout;
    private MyAdapter adapter;
    private long startTime;
    private long endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange);
        CommonUtils.addActivity(this);
        startTime=System.currentTimeMillis();
        initView();
        setOnClickListener();
        getData();
    }

    private void initView() {
        left = (ImageView) findViewById(R.id.navbar_image_left);
        right = (TextView) findViewById(R.id.navbar_image_right);
        refreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_listview);
        emptyLayout = (LinearLayout) findViewById(R.id.empty_layout);
        emptyBtn = (Button) findViewById(R.id.empty_btn);
        loadingLayout = (RelativeLayout) findViewById(R.id.exchange_record_loading);
        refreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        list = new ArrayList<ExchangeRecord>();
        refreshListView.getLoadingLayoutProxy().setRefreshingLabel("正在刷新...");
        refreshListView.getLoadingLayoutProxy().setReleaseLabel("下拉刷新");
        refreshListView.getLoadingLayoutProxy().setPullLabel("释放立即刷新");
        lv = refreshListView.getRefreshableView();
        refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                list.clear();
                getData();
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ExchangeRecord record = list.get(i);
                int type = record.getType();
                if(type == ExchangeRecord.XUNI){
                    View dialogView = LayoutInflater.from(ExchangeRecordActivity.this).inflate(R.layout.common_dialog_onebtn, null);
                    final AppDialog.Builder builder = new AppDialog.Builder(ExchangeRecordActivity.this, AppDialog.Builder.COMMONDIALOG);
                    builder.setContentView(dialogView).
                            setTitle(record.getTip()).
                            setPositiveButton("知道了", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    builder.dismiss();
                                }
                            }).create();
                }
            }
        });

    }

    private void setOnClickListener() {
        left.setOnClickListener(listener);//返回键
        right.setOnClickListener(listener);//兑换规则
        emptyBtn.setOnClickListener(listener);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.navbar_image_left://返回键
                    finish();
                    break;
                case R.id.navbar_image_right://兑换规则
                    Intent intent = new Intent(ExchangeRecordActivity.this, CommonRuleActivity.class);
                    intent.putExtra("title", "积分规则");
                    intent.putExtra("url", Constants.INTEGRAL_RULE);
                    startActivity(intent);
                    break;
                case R.id.empty_btn://空状态按钮
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    private void getData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();;
        map.put("action", "getExchangeList");
        map.put("module", Constants.MODULE_MALL);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new ExchangeCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ExchangeRecordActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Result<ArrayList<ExchangeRecord>> response) {
                        if(response == null) {
                            Toast.makeText(ExchangeRecordActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        refreshListView.onRefreshComplete();
                        if(response.getCode() > Result.RESULT_OK){
                            list = response.getContent();
                            exchangeRecords(list);
                            if(list.size() <= 0 || list == null){
                                emptyLayout.setVisibility(View.VISIBLE);
                            }else {
                                DataSupport.deleteAll(ExchangeRecord.class);
                                DataSupport.saveAll(list);
                            }
                        }else {
                            list = (ArrayList<ExchangeRecord>)DataSupport.findAll(ExchangeRecord.class);
                            if(list.size() <= 0 || list == null){
                                emptyLayout.setVisibility(View.VISIBLE);
                            }
                        }
                        if (adapter == null) {
                            adapter = new MyAdapter(ExchangeRecordActivity.this, list);
                            lv.setAdapter(adapter);
                        } else {
                            adapter.list = list;
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        loadingLayout.setVisibility(View.GONE);
                    }
                });
    }

    private abstract class ExchangeCallback extends Callback<Result<ArrayList<ExchangeRecord>>>{
        @Override
        public Result<ArrayList<ExchangeRecord>> parseNetworkResponse(Response response) throws Exception {
            Result<ArrayList<ExchangeRecord>> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<ArrayList<ExchangeRecord>>>(){}.getType());
            }catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    class MyAdapter extends BaseAdapter {
        LayoutInflater inflater;
        ArrayList<ExchangeRecord> list;

        public MyAdapter(Context context, ArrayList<ExchangeRecord> list) {
            inflater = LayoutInflater.from(context);
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            ExchangeRecord exchangeRecord = list.get(i);
            if (view == null) {
                holder = new ViewHolder();
                view = inflater.inflate(R.layout.item_exchange_point, viewGroup, false);
                holder.goods = (TextView) view.findViewById(R.id.exchang_goods);
                holder.point = (TextView) view.findViewById(R.id.exchang_point);
                holder.time = (TextView) view.findViewById(R.id.exchang_time);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.goods.setText(exchangeRecord.getTitle());
            holder.point.setText(String.valueOf(exchangeRecord.getIntegrate()));
            holder.time.setText(TimeUtils.getCustomFollowData(exchangeRecord.getCtime() * 1000));
            return view;
        }

        class ViewHolder {
            TextView goods;
            TextView time;
            TextView point;
        }
    }


    //友盟监测
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
    protected void onStop() {
        super.onStop();
        endTime=System.currentTimeMillis();
        int duration =(int)((endTime-startTime)/1000);
        Map<String, String> map_value = new HashMap<String, String>();
        MobclickAgent.onEventValue(this, "myexchange", map_value, duration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    public static List<ExchangeRecord> exchangeRecords(List<ExchangeRecord> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size() - i; j++) {
                exchange(list, i, j);
            }
        }
        return list;
    }

    private static List<ExchangeRecord> exchange(List<ExchangeRecord> list, int i, int j) {
        if (list.get(i).getCtime() < list.get(j).getCtime()) {
            ExchangeRecord record = list.get(i);
            list.set(i, list.get(j));
            list.set(j, record);
        }
        return list;
    }
}
