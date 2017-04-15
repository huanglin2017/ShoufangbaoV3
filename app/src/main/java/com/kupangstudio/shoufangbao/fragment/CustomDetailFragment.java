package com.kupangstudio.shoufangbao.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kupangstudio.shoufangbao.BuildDetailActivity;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Build;
import com.kupangstudio.shoufangbao.model.Custom;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Jsmi on 2015/11/4.
 * 客户详情页面
 */
@SuppressLint("ValidFragment")
public class CustomDetailFragment extends Fragment {
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Custom custom;
    private ListView mDetailListview;
    private ArrayList<Build> data;
    private BuildAdapter mDetailAdapter;
    private TextView mName;
    private TextView mSex;
    private TextView mPhone;
    private TextView mWill;
    private TextView mLayout;
    private TextView mSize;
    private TextView mPrice;
    private TextView mType;
    private TextView mRemark;
    private TextView title;
    private TextView empty;

    public CustomDetailFragment() {
    }

    public CustomDetailFragment(Custom custom) {
        this.custom = custom;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_custom_detail, container, false);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        initView(ret);
        data = new ArrayList<Build>();
        getBuildData();
        initData(custom);
        return ret;
    }

    private void initView(View view) {
        mDetailListview = (ListView) view.findViewById(R.id.custom_detail_build);
        mName = (TextView) view.findViewById(R.id.custom_detail_name);
        mSex = (TextView) view.findViewById(R.id.custom_detail_sex);
        mPhone = (TextView) view.findViewById(R.id.custom_detail_phone);
        mWill = (TextView) view.findViewById(R.id.custom_detail_will);
        mLayout = (TextView) view.findViewById(R.id.custom_detail_layout);
        mSize = (TextView) view.findViewById(R.id.custom_detail_size);
        mPrice = (TextView) view.findViewById(R.id.custom_detail_price);
        mType = (TextView) view.findViewById(R.id.custom_detail_type);
        mRemark = (TextView) view.findViewById(R.id.custom_detail_remark);
        title = (TextView) view.findViewById(R.id.title);
        mDetailListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), BuildDetailActivity.class);
                intent.putExtra("bid", data.get(position).getBid());
                startActivity(intent);
            }
        });
        Spanned text = Html.fromHtml("<font color=#be1a20><b>*</b></font>" + "  智能匹配楼盘列表");
        title.setText(text);
        empty = (TextView) view.findViewById(R.id.custom_detail_empty);
    }

    //客户详情
    class BuildAdapter extends BaseAdapter {

        List<Build> list;
        LayoutInflater inflater;

        public BuildAdapter(Context ctx, List<Build> list) {
            inflater = LayoutInflater.from(ctx);
            this.list = list;
        }

        @Override
        public int getCount() {
            int ret = 0;
            if (list != null && list.size() != 0) {
                ret = list.size();
            }
            return ret;
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

    /**
     * 获得楼盘列表数据
     */
    private void getBuildData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "buildList");
        map.put("module", Constants.MODULE_BUILD);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("currentCity", (String) SPUtils.get(getActivity(), Constants.CURRENTCITY, "北京"));
        map.put("cityid", custom.getCityid() + "");
        map.put("lat", (String) SPUtils.get(getActivity(), Constants.CURRENTLAT, "116.403875"));
        map.put("lng", (String) SPUtils.get(getActivity(), Constants.CURRENTLNG, "39.915168"));
        map.put("page", String.valueOf(1));
        map.put("totalPrice", String.valueOf(custom.getPrice()));
        map.put("areaid", String.valueOf(custom.getAreaid()));
        map.put("style", String.valueOf(custom.getType()));
        map.put("layout", String.valueOf(custom.getLayout()));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new BuildCallBack() {
                    @Override
                    public void onError(Call call, Exception e) {
                        empty.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onResponse(Result<ArrayList<Build>> response) {
                        if (response == null) {
                            if (getActivity() == null) {
                                return;
                            }
                            empty.setVisibility(View.VISIBLE);
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            empty.setVisibility(View.GONE);
                            data = response.getContent();
                        } else {
                            empty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        setDetailAdapter();
                    }
                });
    }

    private abstract class BuildCallBack extends Callback<Result<ArrayList<Build>>> {
        @Override
        public Result<ArrayList<Build>> parseNetworkResponse(Response response) throws Exception {
            String s = response.body().string();
            Result<ArrayList<Build>> list = null;
            try {
                list = new Gson().fromJson(s, new TypeToken<Result<ArrayList<Build>>>() {
                }.getType());
            } catch (Exception e) {
                return null;
            }
            return list;
        }
    }

    private void initData(Custom custom) {
        data.clear();
        mName.setText(custom.getName());
        if (custom.getGender() == 1) {
            mSex.setText("先生");
        } else {
            mSex.setText("女士");
        }
        mPhone.setText(custom.getTel());
        mWill.setText(Constants.WILLITEMS[custom.getWill()]);
        mLayout.setText(Constants.LAYOUTITEMS[custom.getLayout()]);
        mSize.setText(Constants.SIZEITEMS[custom.getSize()]);
        mPrice.setText(Constants.PRICEITEMS[custom.getPrice()]);
        mType.setText(Constants.TYPEITEMS[custom.getType()]);
        mRemark.setText(custom.getRemark());
        setDetailAdapter();
    }

    private void setDetailAdapter() {
        if(data.size() > 3) {
            ArrayList<Build> ls = new ArrayList<Build>();
            ls.add(data.get(0));
            ls.add(data.get(1));
            ls.add(data.get(2));
            data.clear();
            data.addAll(ls);
        }
        if (mDetailAdapter == null) {
            mDetailAdapter = new BuildAdapter(getActivity(), data);
            mDetailListview.setAdapter(mDetailAdapter);
        } else {
            mDetailAdapter.list = data;
            mDetailAdapter.notifyDataSetChanged();
        }
    }

    public void onEventMainThread(Custom event) {
        if (event != null) {
            initData(event);
            getBuildData();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(getActivity());
    }
}