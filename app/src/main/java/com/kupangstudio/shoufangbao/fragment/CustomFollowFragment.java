package com.kupangstudio.shoufangbao.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kupangstudio.shoufangbao.CustomAddfollowActivity;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Custom;
import com.kupangstudio.shoufangbao.model.CustomFollow;
import com.kupangstudio.shoufangbao.model.FollowList;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.kupangstudio.shoufangbao.utils.UmengUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.OkHttpRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Jsmi on 2015/11/4.
 * 客户跟进页面
 */
@SuppressLint("ValidFragment")
public class CustomFollowFragment extends Fragment implements View.OnClickListener {
    private Custom custom;
    private TextView mFollowName;
    private TextView mFollowTel;
    private ImageView mFollowCall;
    private LinearLayout mFollowAddfollow;
    private ListView mFollowTimeLine;
    private ArrayList<CustomFollow> mCustomFollowList;
    private CustomFollowAdapter mAdapter;
    private int REQUEST_FOLLOW = 2;
    private RelativeLayout progressDialog;

    public CustomFollowFragment() {
    }

    public CustomFollowFragment(Custom custom) {
        this.custom = custom;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_custom_follow, container, false);
        initView(ret);
        setClickListener();
        mFollowName.setText(custom.getName());
        mFollowTel.setText(custom.getTel());
        mCustomFollowList = new ArrayList<CustomFollow>();
        getData(custom);
        return ret;
    }

    private void initView(View view) {
        mFollowName = (TextView) view.findViewById(R.id.follow_name);
        mFollowTel = (TextView) view.findViewById(R.id.follow_tel);
        mFollowCall = (ImageView) view.findViewById(R.id.follow_call);
        mFollowAddfollow = (LinearLayout) view.findViewById(R.id.follow_addfollow);
        mFollowTimeLine = (ListView) view.findViewById(R.id.follow_listview);
        progressDialog = (RelativeLayout) view.findViewById(R.id.common_progress);
    }

    private void setClickListener() {
        mFollowCall.setOnClickListener(this);
        mFollowAddfollow.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.follow_addfollow://添加跟进
                if (custom == null || custom.getCid() <= 0) {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), "客户数据暂未同步成功，无法添加跟进信息",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("type", "addfollow");
                UmengUtils.onEventAction(getActivity(), "customdetailclick", map);
                Intent intent = new Intent(getActivity(), CustomAddfollowActivity.class);
                intent.putExtra("custom", custom);
                startActivityForResult(intent, REQUEST_FOLLOW);
                break;
            case R.id.follow_call://拨打电话
                for (int i = 0; i < custom.getTel().length(); i++) {
                    if (custom.getTel().substring(i, i + 1).equals("*")) {
                        Toast.makeText(getActivity(), "号码不全，请更新客户信息后联系", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                try {
                    Uri uri = Uri.parse("tel:" + custom.getTel());
                    Intent intent1 = new Intent(Intent.ACTION_DIAL, uri);
                    getActivity().startActivity(intent1);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), "抱歉，该设备暂无打电话功能。", Toast.LENGTH_SHORT).show();
                }

                getAddCustomFollowData(custom, "电话沟通", System.currentTimeMillis() / 1000);
                break;
            default:
                break;
        }
    }


    //跟进页
    class CustomFollowAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ArrayList<CustomFollow> list;

        public CustomFollowAdapter(ArrayList<CustomFollow> infolist, Context context) {
            inflater = LayoutInflater.from(context);
            this.list = sortList(infolist);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View ret = null;
            if (convertView == null) {
                ret = inflater.inflate(R.layout.item_custom_follow, parent, false);
            } else {
                ret = convertView;
            }
            ViewHolder holder = (ViewHolder) ret.getTag();
            if (holder == null) {
                holder = new ViewHolder();
                holder.timelineTime = (TextView) ret.findViewById(R.id.custom_follow_time);
                holder.timelineImg = (ImageView) ret.findViewById(R.id.custom_follow_zhuangtai);
                holder.timelineType = (TextView) ret.findViewById(R.id.custom_follow_type);
                holder.timelineLine = ret.findViewById(R.id.custom_follow_line);
                ret.setTag(holder);
            }
            CustomFollow customFollow = list.get(position);
            String data = TimeUtils.getCustomFollowData(customFollow.getCtime() * 1000);
            StringBuffer datas;
            datas = new StringBuffer();
            datas.append(data);
            int j = data.length();
            for (int i = 0; i < j; i++) {
                if (datas.substring(i, i + 1).equals(" ")) {
                    datas.replace(i, i + 1, "\n");
                }
            }
            holder.timelineTime.setText(datas.toString());
            holder.timelineType.setText(customFollow.getContent());

            if (position == 0) {
                holder.timelineImg.setImageResource(R.drawable.report_progress_done);
                if (getActivity() != null) {
                    holder.timelineTime.setTextColor(getResources().getColor(R.color.common_red));
                    holder.timelineType.setTextColor(getResources().getColor(R.color.common_red));
                }
            } else {
                holder.timelineImg.setImageResource(R.drawable.report_progress_undone);
                if (getActivity() != null) {
                    holder.timelineTime.setTextColor(getResources().getColor(R.color.content_text));
                    holder.timelineType.setTextColor(getResources().getColor(R.color.content_text));
                }
            }
            if (position == list.size() - 1) {
                holder.timelineLine.setVisibility(View.GONE);
            } else {
                holder.timelineLine.setVisibility(View.VISIBLE);
            }
            return ret;
        }

        class ViewHolder {
            public TextView timelineTime;
            public ImageView timelineImg;
            public TextView timelineType;
            public View timelineLine;
        }
    }

    //对集合进行排序
    private ArrayList<CustomFollow> sortList(ArrayList<CustomFollow> list) {
        int n = list.size();
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < n - i; j++) {
                if (list.get(j - 1).getCtime() < list.get(j)
                        .getCtime()) {
                    CustomFollow cf = list.get(j - 1);
                    list.set(j - 1, list.get(j));
                    list.set(j, cf);
                }
            }
        }
        return list;
    }

    // 把添加客户放在底部
    private void formatInfoList(boolean isAdd) {
        if (isAdd) {
            CustomFollow cf = new CustomFollow();
            cf.setCtime(custom.getCtime());
            cf.setContent("添加客户");
            mCustomFollowList.add(cf);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_FOLLOW) {
                if (data != null) {
                    CustomFollow cf = (CustomFollow) data.getSerializableExtra("follow");
                    // 添加
                    mCustomFollowList.add(0, cf);
                    formatInfoList(false);
                    if (getActivity() != null) {
                        if (mAdapter == null) {
                            mAdapter = new CustomFollowAdapter(mCustomFollowList, getActivity());
                            mFollowTimeLine.setAdapter(mAdapter);
                        } else {
                            mAdapter.list = mCustomFollowList;
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    }

    private void getData(final Custom cus) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getFollow");
        map.put("module", Constants.MODULE_CUSTOM);
        map.put("cid", String.valueOf(cus.getCid()));
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new GetFollowCallBack() {

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        progressDialog.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        mCustomFollowList = (ArrayList<CustomFollow>) DataSupport.where("cid = ?",
                                String.valueOf(custom.getCid())).find(CustomFollow.class);
                        notifyAdapter();
                    }

                    @Override
                    public void onResponse(Result<ArrayList<CustomFollow>> response) {
                        if (response.getCode() > Result.RESULT_OK) {
                            mCustomFollowList = response.getContent();
                            DataSupport.deleteAll(CustomFollow.class);
                            DataSupport.saveAll(mCustomFollowList);

                        } else {
                            mCustomFollowList = (ArrayList<CustomFollow>) DataSupport.where("cid = ?",
                                    String.valueOf(custom.getCid())).find(CustomFollow.class);
                            if (getActivity() != null) {
                                Toast.makeText(getActivity(), response.getNotice(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        notifyAdapter();
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        progressDialog.setVisibility(View.GONE);
                    }
                });
    }

    private abstract class GetFollowCallBack extends Callback<Result<ArrayList<CustomFollow>>> {
        @Override
        public Result<ArrayList<CustomFollow>> parseNetworkResponse(Response response) throws Exception {
            String string = response.body().string();
            Result<ArrayList<CustomFollow>> list = null;
            try {
                list = new Gson().fromJson(string, new TypeToken<Result<ArrayList<CustomFollow>>>() {
                }.getType());
            } catch (Exception e) {
                return null;
            }
            return list;
        }
    }

    /**
     * 添加跟进
     *
     * @param cus     对应客户
     * @param content 跟进内容
     */
    private void getAddCustomFollowData(final Custom cus, final String content, final Long time) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "addFollow");
        map.put("module", Constants.MODULE_CUSTOM);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("content", content);
        map.put("type", String.valueOf(Constants.TYPE_AUTO));
        map.put("id", User.getInstance().salt);
        map.put("cid", String.valueOf(cus.getCid()));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if(code > Result.RESULT_OK) {
                                JSONObject object = jsonObject.getJSONObject("content");
                                //存跟进到数据库
                                CustomFollow follow = new CustomFollow();
                                follow.setCid(cus.getCid());
                                follow.setType(Constants.TYPE_AUTO);
                                follow.setCtime(time);
                                follow.setContent(content);
                                follow.setFid(object.getInt("fid"));
                                follow.save();
                                mCustomFollowList.add(0, follow);
                                notifyAdapter();

                                //更新客户信息
                                FollowList followList = new FollowList();
                                followList.setCtime(time);
                                followList.setCid(cus.getCid());
                                cus.setFollow(followList);
                                cus.updateAll("cid = ?", String.valueOf(cus.getCid()));
                                cus.setDATEACTION(Constants.CUSTOM_UPDATA);
                                EventBus.getDefault().post(cus);
                            }
                        } catch (JSONException e) {
                            return;
                        }
                    }
                });
    }

    public void onEventMainThread(CustomFollow event) {
        if (event != null) {
            mCustomFollowList.add(0, event);
            notifyAdapter();
        }
    }

    private void notifyAdapter() {
        if (getActivity() != null) {
            if (mAdapter == null) {
                mAdapter = new CustomFollowAdapter(mCustomFollowList, getActivity());
                mFollowTimeLine.setAdapter(mAdapter);
            } else {
                mAdapter.list = mCustomFollowList;
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}