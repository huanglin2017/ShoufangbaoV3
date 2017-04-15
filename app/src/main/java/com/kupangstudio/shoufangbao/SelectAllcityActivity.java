package com.kupangstudio.shoufangbao;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.CenterCity;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.FileUtils;
import com.kupangstudio.shoufangbao.widget.SideBar;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Jsmi on 2015/11/18.
 * 资料编辑选择城市
 */
public class SelectAllcityActivity extends BaseActivity implements SectionIndexer {

    private ListView lv_city;// 城市列表
    private SideBar sideBar;// 右侧索引
    private TextView dialog;
    private ArrayList<CenterCity> listCity;
    private CityTask cityTask;
    private CityAdapter adapter;
    private boolean isDone;
    private TextView hint;
    private String city;
    private int cityid;
    private int[] area = new int[]{1, 2, 9, 22, 107, 120, 121, 162, 166, 175, 223, 228, 229, 235, 240,
    258, 275, 289, 291, 385, 438};
    private boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_allcity);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(SelectAllcityActivity.this, "选择城市");
        city = getIntent().getStringExtra("city");
        cityid = getIntent().getIntExtra("cityid", 1);
        lv_city = (ListView) findViewById(R.id.centercity_list);
        sideBar = (SideBar) findViewById(R.id.centercity_sidebar);
        dialog = (TextView) findViewById(R.id.centercity_dialog);
        hint = (TextView) findViewById(R.id.centercity_hint);
        hint.setVisibility(View.GONE);
        sideBar.setTextView(dialog, SelectAllcityActivity.this);
        sideBar.setAlphaColor(getResources().getColor(R.color.common_title_bar));
        View view = LayoutInflater.from(this).inflate(R.layout.header_select_city, null);
        TextView currentCity = (TextView) view.findViewById(R.id.current_city);
        if (CommonUtils.stringIsEmpty(city)) {
            city = "北京";
        }
        currentCity.setText(city);
        lv_city.addHeaderView(view);
        lv_city.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position > 0) {
                    CenterCity c = listCity.get(position - 1);
                    for (int i = 0; i < area.length; i++) {
                        if (area[i] == c.getCid()) {
                            flag = true;
                            Intent intent = new Intent();
                            intent.putExtra("city", c.getName());
                            intent.putExtra("cityid", c.getCid());
                            intent.putExtra("flag", true);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                            break;
                        }
                    }
                    if (!flag) {
                        getData(c.getName(), c.getCid());
                    }
                } else {
                    for (int i = 0; i < area.length; i++) {
                        if (area[i] == cityid) {
                            flag = true;
                            Intent intent = new Intent();
                            intent.putExtra("city", city);
                            intent.putExtra("cityid", cityid);
                            intent.putExtra("flag", true);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                            break;
                        }
                    }
                    if (!flag) {
                        getData(city, cityid);
                    }
                }
            }
        });
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                if (!isDone) {
                    return;
                }
                int position = getPositionForSection(s.charAt(0));
                lv_city.setSelection(position);
            }
        });
        cityTask = new CityTask();
        cityTask.execute(new String[]{});
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    @Override
    public int getPositionForSection(int section) {
        for (int i = 0; i < listCity.size(); i++) {
            String sortStr = listCity.get(i).getSortLetter();
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
            return listCity.get(position).getSortLetter().charAt(0);
        }

        return 0;

    }

    @Override
    public Object[] getSections() {
        return null;
    }

    class CityTask extends
            AsyncTask<String, Integer, ArrayList<CenterCity>> {
        ProgressDialog pb;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb = new ProgressDialog(SelectAllcityActivity.this);
            pb.setMessage("请稍候");
            pb.setCancelable(true);
            pb.setCanceledOnTouchOutside(false);
            pb.show();
        }

        @Override
        protected ArrayList<CenterCity> doInBackground(
                String... params) {
            ArrayList<CenterCity> list = FileUtils.readCenterCity(SelectAllcityActivity.this);
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<CenterCity> result) {
            super.onPostExecute(result);
            if (pb != null && pb.isShowing()) {
                pb.dismiss();
                pb = null;
            }
            isDone = true;
            listCity = result;
            if (adapter == null) {
                adapter = new CityAdapter(getApplicationContext(), listCity);
                lv_city.setAdapter(adapter);
            } else {
                adapter.list = listCity;
                adapter.notifyDataSetChanged();
            }
        }
    }

    class ViewHolder {
        TextView letter;
        TextView city;
        View line;
    }

    class CityAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private ArrayList<CenterCity> list;

        public CityAdapter(Context context, ArrayList<CenterCity> list) {
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
                convertView = inflater.inflate(R.layout.item_select_allcity, parent,
                        false);
                holder.letter = (TextView) convertView.findViewById(R.id.alpha);
                holder.city = (TextView) convertView
                        .findViewById(R.id.city_name);
                holder.line = convertView.findViewById(R.id.city_line);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            CenterCity c = list.get(position);
            holder.city.setText(c.getName());
            String firstletter = c.getSortLetter().toUpperCase();
            if (position == 0) {
                holder.letter.setVisibility(View.VISIBLE);
                holder.letter.setText(firstletter);
                holder.line.setVisibility(View.GONE);
            } else {
                String firstletterpre = list.get(position - 1).getSortLetter()
                        .toUpperCase();
                if (firstletter.equals(firstletterpre)) {
                    holder.letter.setVisibility(View.GONE);
                    holder.line.setVisibility(View.VISIBLE);
                } else {
                    holder.letter.setVisibility(View.VISIBLE);
                    holder.letter.setText(firstletter);
                    holder.line.setVisibility(View.GONE);
                }
            }
            return convertView;
        }

    }

    private void getData(final String name, final int cityid) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getOneCity");
        map.put("module", Constants.MODULE_DISTRICT);
        map.put("cityid", String.valueOf(cityid));
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    private ProgressDialog dialog;

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog = new ProgressDialog(SelectAllcityActivity.this);
                        dialog.setMessage("请稍候...");
                        dialog.show();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(SelectAllcityActivity.this, ResultError.MESSAGE_NULL,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                File file = new File(Constants.CACHE_PATH + File.separator + "area",
                                        cityid + ".txt");
                                if (!file.getParentFile().exists()) {
                                    file.getParentFile().mkdirs();
                                }
                                if (!file.exists()) {
                                    try {
                                        file.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    FileOutputStream fos = new FileOutputStream(file);
                                    fos.write(jsonObject.getJSONObject("content").toString().getBytes());
                                    fos.flush();
                                    fos.close();
                                } catch (FileNotFoundException e) {
                                    Toast.makeText(SelectAllcityActivity.this, "未找到文件",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                } catch (IOException e) {
                                    Toast.makeText(SelectAllcityActivity.this, "存储异常",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Intent intent = new Intent();
                                intent.putExtra("city", name);
                                intent.putExtra("cityid", cityid);
                                intent.putExtra("flag", true);
                                intent.putExtra("jsonStr", jsonObject.getJSONObject("content").toString());
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            } else {
                                Intent intent = new Intent();
                                intent.putExtra("city", name);
                                intent.putExtra("cityid", cityid);
                                intent.putExtra("flag", false);
                                setResult(Activity.RESULT_OK, intent);
                                Toast.makeText(SelectAllcityActivity.this, "初始化城市内容失败，请稍候重试",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(SelectAllcityActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        dialog.dismiss();
                    }
                });
    }
}
