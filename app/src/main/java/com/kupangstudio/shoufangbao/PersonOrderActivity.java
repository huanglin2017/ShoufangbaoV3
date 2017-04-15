package com.kupangstudio.shoufangbao;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.kupangstudio.shoufangbao.animation.ScaleAnimationHelper;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Build;
import com.kupangstudio.shoufangbao.model.PersonSet;
import com.kupangstudio.shoufangbao.model.Report;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.FileUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.kupangstudio.shoufangbao.widget.PullToRefreshListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pickerview.OptionsPopupWindow;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Jsmi on 2016/3/1.
 * 私人订制页面
 */
public class PersonOrderActivity extends Activity implements View.OnClickListener {

    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private ImageView personSet;
    private ImageView left;
    private TextView title;
    private PullToRefreshListView refreshListView;
    private ListView lv;
    private LinearLayout areaLl;
    private LinearLayout priceLl;
    private LinearLayout typeLl;
    private TextView areaTv;
    private TextView priceTv;
    private TextView typeTv;
    private Button submit;
    public RelativeLayout layout_parent;
    private int priceIndex = 0;
    private int typeIndex = 0;
    View layout;
    private ImageView leftSet;
    private OptionsPopupWindow pwOptions;
    private ArrayList<String> mCitiesDatas;
    private ArrayList<String> mCitiesIdDatas;
    private ArrayList<ArrayList<String>> mAreasDatas;
    private ArrayList<ArrayList<String>> mAreasIdDatas;
    private String jsonStr;
    private int mPositionCity;
    private int mPositionArea;
    private String areaName;
    private int areaid = 0;
    private String communityName;
    private int communityid = 0;
    private int cityid;
    private BuildAdapter adapter;
    private List<Build> buildList = new ArrayList<Build>();
    private PersonSet ps;
    private RelativeLayout empty;
    private RelativeLayout loading;
    private RelativeLayout loadingMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_order);
        CommonUtils.addActivity(this);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        initView();
        setClickListener();
        getPersonSetData();
        ps = new PersonSet();
        cityid = (int) SPUtils.get(PersonOrderActivity.this, Constants.BUILD_CITY_ID, 1);
        //选项选择器
        pwOptions = new OptionsPopupWindow(this);
        //初始化数据源
        mCitiesDatas = new ArrayList<String>();
        mCitiesIdDatas = new ArrayList<String>();
        mAreasDatas = new ArrayList<ArrayList<String>>();
        mAreasIdDatas = new ArrayList<ArrayList<String>>();
        jsonStr = initJsonData(this, cityid + ".txt");
        if (jsonStr == null) {
            File file = new File(Constants.CACHE_PATH + File.separator + "area", cityid + ".txt");
            if (!file.exists()) {
                getSelectAreaData(cityid);
            } else {
                jsonStr = FileUtils.readFileContentStr(file, this, cityid + ".txt");
            }
        }
        if (jsonStr != null && !jsonStr.equals("")) {
            initNetDistrictDatas();
            pwOptions.setPicker(mCitiesDatas, mAreasDatas, 0, 0, true);
            pwOptions.setSelectOptions(0, 0);
        }
        //监听确定选择按钮
        pwOptions.setOnoptionsSelectListener(new OptionsPopupWindow.OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                //返回的分别是三个级别的选中位置
                mPositionCity = options1;
                mPositionArea = option2;
                /*if (options1 == 0) {
                    if (mCitiesDatas.get(options1) == null) {
                        areaName = "";
                    } else {
                        areaName = mCitiesDatas.get(options1);
                    }
                    try {
                        communityName = mAreasDatas.get(options1).get(option2);
                    } catch (Exception e) {
                        communityName = "";
                    }
                    areaid = 0;
                    communityid = 0;
                } else {*/
                if (mCitiesDatas.get(options1) == null) {
                    areaName = "";
                    areaid = 0;
                } else {
                    areaName = mCitiesDatas.get(options1);
                    areaid = Integer.parseInt(mCitiesIdDatas.get(options1));
                }
                if (mAreasDatas.get(options1).get(option2) == null) {
                    communityName = "";
                    communityid = 0;
                } else {
                    communityName = mAreasDatas.get(options1).get(option2);
                    communityid = Integer.parseInt(mAreasIdDatas.get(options1).get(option2));
                }
                //}
                String tx = areaName;
                areaTv.setText(tx);
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PersonOrderActivity.this, BuildDetailActivity.class);
                intent.putExtra("bid", buildList.get(position).getBid());
                startActivity(intent);
            }
        });
        getBuildData();
    }

    private void initView() {
        layout_parent = (RelativeLayout) findViewById(R.id.person_order_parent);
        personSet = (ImageView) findViewById(R.id.navbar_image_right);
        left = (ImageView) findViewById(R.id.navbar_image_left);
        title = (TextView) findViewById(R.id.navbar_title);
        title.setText("私人订制");
        refreshListView = (PullToRefreshListView) findViewById(R.id.person_order_refresh);
        refreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
        loading = (RelativeLayout) findViewById(R.id.loading_personal);
        loadingMain = (RelativeLayout) findViewById(R.id.loading_personal_main);
        lv = refreshListView.getRefreshableView();
        empty = (RelativeLayout) findViewById(R.id.emptyview);
        TextView emptyviewText = (TextView) findViewById(R.id.emptyview_text);
        emptyviewText.setText("暂无更多房源，请换个条件试试吧!");
        Button emptybtn = (Button) findViewById(R.id.emptyview_btn);
        emptybtn.setVisibility(View.GONE);
    }

    private void setClickListener() {
        personSet.setOnClickListener(this);
        left.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        View view = LayoutInflater.from(PersonOrderActivity.this).
                inflate(R.layout.common_dialog_list, null);
        switch (v.getId()) {
            case R.id.navbar_image_left:
                finish();
                break;
            case R.id.navbar_image_right:
                personSet.setEnabled(false);
                displayPage();
                break;
            case R.id.person_set_area:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //强制隐藏键盘
                if (jsonStr != null && !jsonStr.equals("")) {
                    pwOptions.setPicker(mCitiesDatas, mPositionCity, mPositionArea);
                    pwOptions.showAtLocation(areaLl, Gravity.BOTTOM, 0, 0);
                } else {
                    Toast.makeText(PersonOrderActivity.this, "初始化信息失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.person_set_price:
                final AppDialog.Builder builder2 = new AppDialog.Builder(PersonOrderActivity.this,
                        AppDialog.Builder.SINGLECHOICE, Constants.PRICEITEMS);
                builder2.setContentView(view).setItemClick(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        priceIndex = position;
                        priceTv.setText(Constants.PRICEITEMS[priceIndex]);
                        builder2.dismiss();
                    }
                }).create();
                break;
            case R.id.person_set_type:
                final AppDialog.Builder builder = new AppDialog.Builder(PersonOrderActivity.this,
                        AppDialog.Builder.SINGLECHOICE, Constants.TYPEITEMS);
                builder.setContentView(view).setItemClick(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        typeIndex = position;
                        typeTv.setText(Constants.TYPEITEMS[typeIndex]);
                        builder.dismiss();
                    }
                }).create();
                break;
            case R.id.person_set_submit:
                if (areaid != 0) {
                    getPersonSumbitData();
                    personSet.setEnabled(true);
                    dismissPage();
                } else {
                    Toast.makeText(PersonOrderActivity.this, "请选择您的房源定制区域", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    public void displayPage() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        layout = inflater.inflate(R.layout.activity_person_set, null);
        layout.setId(Constants.KEY_LAYOUT_ID);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT);
        layout_parent.addView(layout, layoutParams);

        findDialogView();

        if (ps.getArea() != null) {
            areaTv.setText(ps.getArea());
            priceTv.setText(Constants.PRICEITEMS[ps.getPrice()]);
            typeTv.setText(Constants.TYPEITEMS[ps.getStyle()]);
            areaid = ps.getAreaid();
            priceIndex = ps.getPrice();
            typeIndex = ps.getStyle();
        }
        // 显示layout进行缩放动画效果
        //ScaleAnimationHelper scaleHelper = new ScaleAnimationHelper(this,Constants.KEY_FIRSTPAGE);
        //scaleHelper.ScaleOutAnimation(layout);
    }

    public void removeLayout() {
        layout_parent.removeView(layout_parent.findViewById(Constants.KEY_LAYOUT_ID));
    }

    public void findDialogView() {
        areaLl = (LinearLayout) layout.findViewById(R.id.person_set_area);
        priceLl = (LinearLayout) layout.findViewById(R.id.person_set_price);
        typeLl = (LinearLayout) layout.findViewById(R.id.person_set_type);
        areaTv = (TextView) layout.findViewById(R.id.person_set_area_txt);
        priceTv = (TextView) layout.findViewById(R.id.person_set_price_txt);
        typeTv = (TextView) layout.findViewById(R.id.person_set_type_txt);
        submit = (Button) layout.findViewById(R.id.person_set_submit);
        leftSet = (ImageView) layout.findViewById(R.id.navbar_image_left);
        TextView titleSet = (TextView) layout.findViewById(R.id.navbar_title);
        titleSet.setText("私人订制");
        areaLl.setOnClickListener(this);
        priceLl.setOnClickListener(this);
        typeLl.setOnClickListener(this);
        submit.setOnClickListener(this);
        leftSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((boolean) SPUtils.get(PersonOrderActivity.this, Constants.PERSONALORDER, true)) {
                    personSet.setEnabled(true);
                    dismissPage();
                    initSelect();
                } else {
                    finish();
                }

            }
        });
    }

    public void dismissPage() {
        ScaleAnimationHelper scaleHelper = new ScaleAnimationHelper(this,
                Constants.KEY_SECONDPAGE);
        scaleHelper.ScaleInAnimation(layout);
    }

    private void setAdapter() {
        if (adapter == null) {
            adapter = new BuildAdapter(PersonOrderActivity.this, buildList);
            lv.setAdapter(adapter);
        } else {
            adapter.list = buildList;
            adapter.notifyDataSetChanged();
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

    //获取私人定制信息
    private void getPersonSetData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getCustom");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new PersonSetCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(PersonOrderActivity.this, ResultError.MESSAGE_ERROR,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        loadingMain.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        loadingMain.setVisibility(View.GONE);
                    }

                    @Override
                    public void onResponse(Result<PersonSet> response) {
                        if (response == null) {
                            Toast.makeText(PersonOrderActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            ps = response.getContent();
                            SPUtils.put(PersonOrderActivity.this, Constants.PERSONALORDER, true);
                        } else {
                            SPUtils.put(PersonOrderActivity.this, Constants.PERSONALORDER, false);
                            personSet.setEnabled(false);
                            displayPage();
                        }
                    }
                });
    }

    private abstract class PersonSetCallback extends Callback<Result<PersonSet>> {
        @Override
        public Result<PersonSet> parseNetworkResponse(Response response) throws Exception {
            Result<PersonSet> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<PersonSet>>() {
                        }.getType());
            } catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    //提交私人订制数据
    private void getPersonSumbitData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "editSelect");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("cityid", cityid + "");
        map.put("areaid", areaid + "");
        map.put("style", String.valueOf(typeIndex));
        map.put("price", String.valueOf(priceIndex));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        initSelect();
                    }

                    @Override
                    public void onResponse(String response) {
                        initSelect();
                        try {
                            JSONObject object = new JSONObject(response);
                            String notice = object.getString("notice");
                            int code = object.getInt("code");
                            //Toast.makeText(PersonOrderActivity.this, notice, Toast.LENGTH_SHORT).show();
                            if (code > Result.RESULT_OK) {
                                buildList.clear();
                                getPersonSetData();
                                getBuildData();
                            }
                        } catch (JSONException e) {
                            return;
                        }
                    }
                });
    }

    /**
     * 获得楼盘列表数据
     */
    private void getBuildData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getCustom");
        map.put("module", Constants.MODULE_BUILD);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("currentCity", (String) SPUtils.get(PersonOrderActivity.this, Constants.CURRENTCITY, "北京"));
        map.put("lat", (String) SPUtils.get(PersonOrderActivity.this, Constants.CURRENTLAT, "39.915168"));
        map.put("lng", (String) SPUtils.get(PersonOrderActivity.this, Constants.CURRENTLNG, "116.403875"));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new BuildCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        loading.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        empty.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onResponse(Result<ArrayList<Build>> response) {
                        if (response == null) {
                            empty.setVisibility(View.VISIBLE);
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            empty.setVisibility(View.GONE);
                            buildList = response.getContent();
                        } else {
                            empty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        setAdapter();
                        loading.setVisibility(View.GONE);
                    }
                });
    }

    private abstract class BuildCallback extends Callback<Result<ArrayList<Build>>> {
        @Override
        public Result<ArrayList<Build>> parseNetworkResponse(Response response) throws Exception {
            Result<ArrayList<Build>> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<ArrayList<Build>>>() {
                        }.getType());
            } catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    //取出数据
    public static String initJsonData(Context ctx, String name) {
        String str = null;
        try {
            StringBuffer sb = new StringBuffer();
            InputStream is = ctx.getAssets().open("area" + File.separator + name);
            int len = -1;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                sb.append(new String(buf, 0, len));
            }
            is.close();
            str = sb.toString();
        } catch (IOException e) {
            return str;
        }
        return str;
    }

    //解析服务器回传的商圈信息
    private void initNetDistrictDatas() {
        mPositionArea = 0;
        mPositionCity = 0;
        mAreasDatas.clear();
        mCitiesDatas.clear();
        mCitiesIdDatas.clear();
        mAreasIdDatas.clear();
        try {
            JSONObject jsonP = new JSONObject(jsonStr);
            JSONArray jsonC = null;
            try {
                jsonC = jsonP.getJSONArray("list");
            } catch (Exception e1) {
                Toast.makeText(PersonOrderActivity.this, "数据格式错误", Toast.LENGTH_SHORT).show();
            }
            if (jsonC.length() == 0) {
                mCitiesDatas.add("");
                mCitiesIdDatas.add(0 + "");
                ArrayList<String> mAreasDatas0 = new ArrayList<String>();
                ArrayList<String> mAreasIdDatas0 = new ArrayList<String>();
                mAreasDatas0.add("");
                mAreasIdDatas0.add(0 + "");
                mAreasDatas.add(mAreasDatas0);
                mAreasIdDatas.add(mAreasIdDatas0);
            }
            for (int j = 0; j < jsonC.length(); j++) {
                JSONObject jsonCity = jsonC.getJSONObject(j);
                String city = jsonCity.getString("name");// 市名字
                int cid = jsonCity.getInt("areaId");// 市id
                mCitiesDatas.add(city);
                mCitiesIdDatas.add(cid + "");
                JSONArray jsonAreas = null;
                ArrayList<String> mAreasDatas1 = new ArrayList<String>();
                ArrayList<String> mAreasIdDatas1 = new ArrayList<String>();
                try {
                    if (jsonCity.isNull("list")) {
                        mAreasDatas1.add("");
                        mAreasIdDatas1.add(cid + "");
                        mAreasDatas.add(mAreasDatas1);
                        mAreasIdDatas.add(mAreasIdDatas1);
                    } else {
                        jsonAreas = jsonCity.getJSONArray("list");
                        for (int k = 0; k < jsonAreas.length(); k++) {
                            JSONObject jsonArea = jsonAreas.getJSONObject(k);
                            String area = jsonArea.getString("name");
                            int aid = jsonArea.getInt("districtId");
                            mAreasIdDatas1.add(aid + "");
                            mAreasDatas1.add(area);
                        }
                        mAreasIdDatas.add(mAreasIdDatas1);
                        mAreasDatas.add(mAreasDatas1);
                    }
                } catch (Exception e2) {
                    Toast.makeText(PersonOrderActivity.this, "数据格式错误", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (JSONException e) {
            Toast.makeText(PersonOrderActivity.this, "数据格式错误", Toast.LENGTH_SHORT).show();
        }
    }


    private void getSelectAreaData(final int cityid) {
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
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(PersonOrderActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                jsonStr = jsonObject.getJSONObject("content").toString();
                                File file = new File(Constants.CACHE_PATH + File.separator + "area", cityid + ".txt");
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
                                    fos.write(jsonStr.getBytes());
                                    fos.flush();
                                    fos.close();
                                } catch (FileNotFoundException e) {
                                    Toast.makeText(PersonOrderActivity.this, "未找到文件", Toast.LENGTH_SHORT).show();
                                    return;
                                } catch (IOException e) {
                                    Toast.makeText(PersonOrderActivity.this, "存储异常", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {
                                jsonStr = "";
                                Toast.makeText(PersonOrderActivity.this, "初始化城市内容失败，请稍候重试", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(PersonOrderActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    private void initSelect() {
        typeIndex = 0;
        priceIndex = 0;
        areaid = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
