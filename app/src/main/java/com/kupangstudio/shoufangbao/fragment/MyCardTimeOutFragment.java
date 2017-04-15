package com.kupangstudio.shoufangbao.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Card;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.DensityUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.kupangstudio.shoufangbao.widget.PullToRefreshListView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by long1 on 16/3/16.
 * Copyright 16/3/16 android_xiaobai.
 * 已过期卡券
 */
public class MyCardTimeOutFragment extends Fragment {
    private PullToRefreshListView refreshListView;
    private RelativeLayout loadingLayout;
    private boolean isFirst = true;
    private ListView lv;
    private List<Card> list = new ArrayList<Card>();
    private MyAdapter adapter;
    private Button emptyButton;
    private TextView emptyText;
    private RelativeLayout emptyLayout;
    private ImageView emptyImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_card_timeout, container, false);
        initView(view);
        getData();
        emptyText.setText("抱歉！您还没有卡券");
        emptyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirst = true;
                getData();
            }
        });
        refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                getData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
        return view;
    }

    private void getData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getCoupon");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("type", "3");
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        if (isFirst) {
                            loadingLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (getActivity() == null) {
                            return;
                        }
                        emptyLayout.setVisibility(View.VISIBLE);
                        emptyImage.setImageResource(R.drawable.common_empty_nonet);
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null) {
                            return;
                        }
                        if (isFirst) {
                            loadingLayout.setVisibility(View.GONE);
                            isFirst = false;
                        }
                        refreshListView.onRefreshComplete();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if (code > Result.RESULT_OK) {
                                emptyLayout.setVisibility(View.GONE);
                                list.clear();
                                initCardList(jsonObject);
                                if (adapter == null) {
                                    adapter = new MyAdapter(getActivity(), list);
                                    lv.setAdapter(adapter);
                                } else {
                                    adapter.list = list;
                                    adapter.notifyDataSetChanged();
                                }
                            } else {
                                emptyLayout.setVisibility(View.VISIBLE);
                                emptyImage.setImageResource(R.drawable.empty_card);
                                Toast.makeText(getActivity(), notice, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            emptyLayout.setVisibility(View.VISIBLE);
                            emptyImage.setImageResource(R.drawable.empty_card);
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    private void initCardList(JSONObject jsonObject) {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("content");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                Card card = new Card();
                int style = object.getInt("style");
                if (style == 4) {//4卡券
                    card.setItemType(0);
                    card.setEndtime(object.getLong("endtime"));
                    card.setMoney(object.getString("money"));
                    card.setRemark(object.getString("remark"));
                    card.setStyle(style);
                    card.setType(object.getString("type"));
                } else {
                    card.setItemType(1);
                    card.setEndtime(object.getLong("endtime"));
                    card.setMoney(object.getString("money"));
                    card.setStyle(style);
                    card.setType(object.getString("type"));
                }
                list.add(card);
            }
        } catch (JSONException e) {
            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void initView(View view) {
        refreshListView = (PullToRefreshListView) view.findViewById(R.id.refreshListView);
        loadingLayout = (RelativeLayout) view.findViewById(R.id.loading_my_card_used);
        emptyButton = (Button) view.findViewById(R.id.emptyview_btn);
        emptyText = (TextView) view.findViewById(R.id.emptyview_text);
        emptyLayout = (RelativeLayout) view.findViewById(R.id.emptyview);
        emptyImage = (ImageView) view.findViewById(R.id.emptyview_img);
        lv = refreshListView.getRefreshableView();
        lv.setSelector(R.color.transparent);
        lv.setDividerHeight(DensityUtils.dp2px(getActivity(), 10));
    }

    class MyAdapter extends BaseAdapter {

        LayoutInflater inflater;
        List<Card> list;

        public MyAdapter(Context context, List<Card> list) {
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
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return list.get(position).getItemType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);
            ViewHolder1 holder1 = null;
            ViewHolder2 holder2 = null;
            if (convertView == null) {
                switch (type) {
                    case 0:
                        convertView = inflater.inflate(R.layout.item_my_card_grey_one, parent, false);
                        holder1 = new ViewHolder1();
                        holder1.tvMoney = (TextView) convertView.findViewById(R.id.tv_money);
                        holder1.tvType = (TextView) convertView.findViewById(R.id.tv_type);
                        holder1.tvEnd = (TextView) convertView.findViewById(R.id.tv_endtime);
                        holder1.tvRemark = (TextView) convertView.findViewById(R.id.tv_remark);
                        convertView.setTag(holder1);
                        break;
                    case 1:
                        convertView = inflater.inflate(R.layout.item_my_card_grey_two, parent, false);
                        holder2 = new ViewHolder2();
                        holder2.usedLayout = (LinearLayout) convertView.findViewById(R.id.layout_used);
                        holder2.tvEnd = (TextView) convertView.findViewById(R.id.tv_endtime);
                        convertView.setTag(holder2);
                        break;
                    default:
                        break;
                }
            } else {
                switch (type) {
                    case 0:
                        holder1 = (ViewHolder1) convertView.getTag();
                        break;
                    case 1:
                        holder2 = (ViewHolder2) convertView.getTag();
                        break;
                    default:
                        break;
                }
            }
            Card card = list.get(position);
            switch (type) {
                case 0:
                    holder1.tvMoney.setText(card.getMoney());
                    holder1.tvType.setText(card.getType());
                    holder1.tvRemark.setText(card.getRemark());
                    long time = card.getEndtime() * 1000;
                    holder1.tvEnd.setText("有效期至" + TimeUtils.getCustomReportData(time));
                    break;
                case 1:
                    int money = Integer.parseInt(card.getMoney());
                    if (money == 1) {
                        holder2.usedLayout.setBackgroundDrawable(getActivity().getResources()
                                .getDrawable(R.drawable.bg_money_one_grey_used));
                    } else if (money == 5) {
                        holder2.usedLayout.setBackgroundDrawable(getActivity().getResources()
                                .getDrawable(R.drawable.bg_money_five_grey_used));
                    } else if (money == 10) {
                        holder2.usedLayout.setBackgroundDrawable(getActivity().getResources()
                                .getDrawable(R.drawable.bg_money_ten_grey_used));
                    } else {
                        holder2.usedLayout.setBackgroundDrawable(getActivity().getResources()
                                .getDrawable(R.drawable.bg_money_hundred_grey_used));
                    }
                    holder2.tvEnd.setText("有效期至" + TimeUtils.
                            getCustomReportData(card.getEndtime() * 1000));
                    break;
                default:
                    break;
            }
            return convertView;
        }
    }

    static class ViewHolder1 {
        TextView tvMoney;
        TextView tvType;
        TextView tvRemark;
        TextView tvEnd;
    }

    static class ViewHolder2 {
        LinearLayout usedLayout;
        TextView tvEnd;
    }
}
