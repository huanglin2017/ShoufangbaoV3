package com.kupangstudio.shoufangbao;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.SelectCity;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CityComparator;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.widget.SideBar;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by long on 15/11/6.
 * Copyright 2015 android_xiaobai.
 * 首页的选择城市界面
 */
public class SelectHomeCityActivity extends BaseActivity implements SectionIndexer {

    private boolean isFromFirst;
    private ListView lvCity;//城市列表
    private SideBar sideBar;//右侧索引
    private TextView dialog;//选择的字母对话框
    private String city;
    private int cityId;
    private ImageView ivBack;
    private List<SelectCity> listCity;
    private TextView tvCurrentCity;//当前城市
    private boolean isDone;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        CommonUtils.addActivity(this);
        isFromFirst = getIntent().getBooleanExtra(Constants.IS_FROM_FIRST, false);
        city = (String) SPUtils.get(this, Constants.BUILD_CITY_NAME, "北京");
        cityId = (int) SPUtils.get(this, Constants.BUILD_CITY_ID, 1);
        View headerView = LayoutInflater.from(this).inflate(R.layout.header_select_city, null);
        initView();
        initHeaderView(headerView);
        setClickListener();
        getData();
        sideBar.setTextView(dialog, this);
        sideBar.setAlphaColor(getResources().getColor(R.color.common_red));
        tvCurrentCity.setText(city);
        lvCity.addHeaderView(headerView);
    }


    private void initView() {
        sideBar = (SideBar) findViewById(R.id.select_city_sidebar);
        lvCity = (ListView) findViewById(R.id.list);
        dialog = (TextView) findViewById(R.id.select_city_dialog);
        ivBack = (ImageView) findViewById(R.id.select_city_back);
    }

    private void initHeaderView(View headerView) {
        tvCurrentCity = (TextView) headerView.findViewById(R.id.current_city);
    }

    private void setClickListener() {
        ivBack.setOnClickListener(clickListener);
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                if (!isDone) {
                    return;
                }
                int position = getPositionForSection(s.charAt(0));
                lvCity.setSelection(position);
            }
        });
        lvCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    SelectCity c = listCity.get(position - 1);
                    city = c.getName();
                    cityId = c.getCityId();
                }
                SPUtils.put(SelectHomeCityActivity.this, Constants.BUILD_CITY_NAME, city);
                SPUtils.put(SelectHomeCityActivity.this, Constants.BUILD_CITY_ID, cityId);
                SPUtils.put(SelectHomeCityActivity.this, Constants.HOME_SET_CITY, true);
                if (isFromFirst) {
                    Intent it = new Intent(SelectHomeCityActivity.this, MainActivity.class);
                    startActivity(it);
                    finish();
                } else {
                    finish();
                }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    private void getData() {
        final ProgressDialog pd = new ProgressDialog(SelectHomeCityActivity.this);
        pd.setMessage("请稍候");
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getDredgeCity");
        map.put("module", Constants.MODULE_DISTRICT);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new MyCallBack() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        pd.show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(SelectHomeCityActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        listCity = DataSupport.findAll(SelectCity.class);
                        if (listCity == null || listCity.size() <= 0) {
                            SelectCity c = new SelectCity();
                            c.setCityId(1);
                            c.setName("北京");
                            c.setInitial("B");
                            SelectCity c1 = new SelectCity();
                            c1.setCityId(2);
                            c1.setName("天津");
                            c1.setInitial("T");
                            listCity.add(c);
                            listCity.add(c1);
                        }
                        CityComparator comparator = new CityComparator();
                        Collections.sort(listCity, comparator);
                        isDone = true;
                        if (adapter == null) {
                            adapter = new MyAdapter(SelectHomeCityActivity.this, listCity);
                            lvCity.setAdapter(adapter);
                        } else {
                            adapter.list = listCity;
                            adapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onResponse(Result<ArrayList<SelectCity>> response) {
                        if(response == null){
                            Toast.makeText(SelectHomeCityActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            listCity = DataSupport.findAll(SelectCity.class);
                            if (listCity == null || listCity.size() <= 0) {
                                SelectCity c = new SelectCity();
                                c.setCityId(1);
                                c.setName("北京");
                                c.setInitial("B");
                                SelectCity c1 = new SelectCity();
                                c1.setCityId(2);
                                c1.setName("天津");
                                c1.setInitial("T");
                                listCity.add(c);
                                listCity.add(c1);
                            }
                            CityComparator comparator = new CityComparator();
                            Collections.sort(listCity, comparator);
                            isDone = true;
                            if (adapter == null) {
                                adapter = new MyAdapter(SelectHomeCityActivity.this, listCity);
                                lvCity.setAdapter(adapter);
                            } else {
                                adapter.list = listCity;
                                adapter.notifyDataSetChanged();
                            }
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            listCity = response.getContent();
                            DataSupport.deleteAll(SelectCity.class);
                            DataSupport.saveAll(listCity);
                        } else {
                            listCity = DataSupport.findAll(SelectCity.class);
                        }
                        if (listCity == null || listCity.size() <= 0) {
                            SelectCity c = new SelectCity();
                            c.setCityId(1);
                            c.setName("北京");
                            c.setInitial("B");
                            SelectCity c1 = new SelectCity();
                            c1.setCityId(2);
                            c1.setName("天津");
                            c1.setInitial("T");
                            listCity.add(c);
                            listCity.add(c1);
                        }
                        CityComparator comparator = new CityComparator();
                        Collections.sort(listCity, comparator);
                        isDone = true;
                        if (adapter == null) {
                            adapter = new MyAdapter(SelectHomeCityActivity.this, listCity);
                            lvCity.setAdapter(adapter);
                        } else {
                            adapter.list = listCity;
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onAfter() {
                        super.onAfter();
                        pd.dismiss();
                    }
                });
    }

    private abstract class MyCallBack extends Callback<Result<ArrayList<SelectCity>>> {
        @Override
        public Result<ArrayList<SelectCity>> parseNetworkResponse(Response response) throws Exception {
            Result<ArrayList<SelectCity>> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<ArrayList<SelectCity>>>(){}.getType());
            }catch (Exception e){
                return null;
            }
            return result;
        }
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.select_city_back:
                    SPUtils.put(SelectHomeCityActivity.this, Constants.HOME_SET_CITY, true);
                    if (isFromFirst) {
                        Intent it = new Intent(SelectHomeCityActivity.this, MainActivity.class);
                        startActivity(it);
                    } else {
                        finish();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    class MyAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<SelectCity> list;

        public MyAdapter(Context context, List<SelectCity> list) {
            inflater = LayoutInflater.from(context);
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_select_city, parent,
                        false);
                holder.letter = (TextView) convertView.findViewById(R.id.alpha);
                holder.city = (TextView) convertView
                        .findViewById(R.id.city_name);
                holder.line = convertView.findViewById(R.id.city_line);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            SelectCity city = list.get(position);
            holder.city.setText(city.getName());
            String firstLetter = city.getInitial().toUpperCase();
            if (position == 0) {
                holder.letter.setVisibility(View.VISIBLE);
                holder.letter.setText(firstLetter);
                holder.line.setVisibility(View.GONE);
            } else {
                String firstLetterPre = list.get(position - 1).getInitial()
                        .toUpperCase();
                if (firstLetter.equals(firstLetterPre)) {
                    holder.letter.setVisibility(View.GONE);
                    holder.line.setVisibility(View.VISIBLE);
                } else {
                    holder.letter.setVisibility(View.VISIBLE);
                    holder.letter.setText(firstLetter);
                    holder.line.setVisibility(View.GONE);
                }
            }
            return convertView;
        }
    }

    static class ViewHolder {
        TextView letter;
        TextView city;
        View line;
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    @Override
    public int getPositionForSection(int section) {
        for (int i = 0; i < listCity.size(); i++) {
            String sortStr = listCity.get(i).getInitial();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    @Override
    public int getSectionForPosition(int position) {

        if (listCity != null && listCity.size() > position) {
            return listCity.get(position).getInitial().charAt(0);
        }

        return 0;

    }

    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            SPUtils.put(SelectHomeCityActivity.this, Constants.HOME_SET_CITY, true);
            if (isFromFirst) {
                Intent it = new Intent(SelectHomeCityActivity.this, MainActivity.class);
                startActivity(it);
            } else {
                finish();
            }
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }
}
