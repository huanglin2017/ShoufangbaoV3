package com.kupangstudio.shoufangbao.fragment;

import android.app.ProgressDialog;
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
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.kupangstudio.shoufangbao.widget.PullToRefreshListView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by long1 on 16/3/16.
 * Copyright 16/3/16 android_xiaobai.
 * 可使用卡券
 */
public class MyCardUseFragment extends Fragment {

    private PullToRefreshListView refreshListView;
    private ListView lv;
    private boolean isFirst = true;
    private List<Card> list = new ArrayList<Card>();
    private MyAdapter adapter;
    private RelativeLayout loadingLayout;
    private ProgressDialog dialog;
    private Button emptyButton;
    private TextView emptyText;
    private RelativeLayout emptyLayout;
    private ImageView emptyImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_card_use, container, false);
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
        map.put("type", "1");
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
                        emptyImage.setImageResource(R.drawable.common_empty_nonet);
                        emptyLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        if (isFirst) {
                            loadingLayout.setVisibility(View.GONE);
                            isFirst = false;
                        }
                        refreshListView.onRefreshComplete();
                        if (getActivity() == null) {
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if (code > Result.RESULT_OK) {
                                emptyLayout.setVisibility(View.GONE);
                                list.clear();
                                initCardList(jsonObject.getJSONArray("content"));
                                if (adapter == null) {
                                    adapter = new MyAdapter(list, getActivity());
                                    lv.setAdapter(adapter);
                                } else {
                                    adapter.list = list;
                                    adapter.notifyDataSetChanged();
                                }
                            } else {
                                emptyImage.setImageResource(R.drawable.empty_card);
                                emptyLayout.setVisibility(View.VISIBLE);
                                Toast.makeText(getActivity(), notice, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            emptyImage.setImageResource(R.drawable.empty_card);
                            emptyLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    private void initCardList(JSONArray content) {
        for (int i = 0; i < content.length(); i++) {
            try {
                JSONObject jsonObject = content.getJSONObject(i);
                Card card = new Card();
                int style = jsonObject.getInt("style");
                if (style == 4) {//4卡券3拼图
                    card.setItemType(0);//自定义字段鱼getItemViewType配合0普通 1：2张拼图 2：4
                    card.setMoney(jsonObject.getString("money"));
                    card.setStyle(style);
                    card.setType(jsonObject.getString("type"));
                    card.setRemark(jsonObject.getString("remark"));
                    card.setEndtime(jsonObject.getLong("endtime"));
                } else {
                    int money = 0;
                    try {
                        money = Integer.parseInt(jsonObject.getString("money"));
                    } catch (Exception e) {
                        money = (int) Double.parseDouble(jsonObject.getString("money"));
                    }
                    if (money == 1 || money == 5) {
                        card.setItemType(1);
                    } else {
                        card.setItemType(2);
                    }
                    card.setMoney(money + "");
                    card.setStyle(style);
                    card.setType(jsonObject.getString("type"));
                    card.setEndtime(jsonObject.getLong("endtime"));
                    card.setRemark(jsonObject.getJSONObject("remark").toString());
                }
                list.add(card);
            } catch (JSONException e) {
                Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void initView(View view) {
        refreshListView = (PullToRefreshListView) view.findViewById(R.id.refreshListView);
        lv = refreshListView.getRefreshableView();
        emptyButton = (Button) view.findViewById(R.id.emptyview_btn);
        emptyText = (TextView) view.findViewById(R.id.emptyview_text);
        emptyLayout = (RelativeLayout) view.findViewById(R.id.emptyview);
        emptyImage = (ImageView) view.findViewById(R.id.emptyview_img);
        lv.setSelector(R.color.transparent);
        lv.setDividerHeight(DensityUtils.dp2px(getActivity(), 10));
        loadingLayout = (RelativeLayout) view.findViewById(R.id.loading_my_card);
    }

    class MyAdapter extends BaseAdapter {

        LayoutInflater inflater;
        List<Card> list;

        public MyAdapter(List<Card> list, Context context) {
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
            return 3;
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
            ViewHolder3 holder3 = null;
            if (convertView == null) {
                switch (type) {
                    case 0:
                        convertView = inflater.inflate(R.layout.item_my_card_type_one, parent, false);
                        holder1 = new ViewHolder1();
                        holder1.tvMoney = (TextView) convertView.findViewById(R.id.tv_money);
                        holder1.tvType = (TextView) convertView.findViewById(R.id.tv_type);
                        holder1.tvEnd = (TextView) convertView.findViewById(R.id.tv_endtime);
                        holder1.tvRemark = (TextView) convertView.findViewById(R.id.tv_remark);
                        convertView.setTag(holder1);
                        break;
                    case 1:
                        convertView = inflater.inflate(R.layout.item_my_card_two_piece, parent, false);
                        holder2 = new ViewHolder2();
                        holder2.layoutTwo = (RelativeLayout) convertView.findViewById(R.id.layout_two_piece);
                        holder2.ivExchange = (ImageView) convertView.findViewById(R.id.iv_exchange);
                        holder2.ivOne = (ImageView) convertView.findViewById(R.id.iv_one);
                        holder2.tvOneNum = (TextView) convertView.findViewById(R.id.tv_one_num);
                        holder2.ivTwo = (ImageView) convertView.findViewById(R.id.iv_two);
                        holder2.tvTwoNum = (TextView) convertView.findViewById(R.id.tv_two_num);
                        holder2.tvEnd = (TextView) convertView.findViewById(R.id.tv_endtime);
                        convertView.setTag(holder2);
                        break;
                    case 2:
                        convertView = inflater.inflate(R.layout.item_my_card_four_piece, parent, false);
                        holder3 = new ViewHolder3();
                        holder3.layoutFour = (RelativeLayout) convertView.findViewById(R.id.layout_four_piece);
                        holder3.ivExchange = (ImageView) convertView.findViewById(R.id.iv_exchange);
                        holder3.ivOne = (ImageView) convertView.findViewById(R.id.iv_one);
                        holder3.tvOneNum = (TextView) convertView.findViewById(R.id.tv_one_num);
                        holder3.ivTwo = (ImageView) convertView.findViewById(R.id.iv_two);
                        holder3.tvTwoNum = (TextView) convertView.findViewById(R.id.tv_two_num);
                        holder3.ivThree = (ImageView) convertView.findViewById(R.id.iv_three);
                        holder3.tvThreeNum = (TextView) convertView.findViewById(R.id.tv_three_num);
                        holder3.ivFour = (ImageView) convertView.findViewById(R.id.iv_four);
                        holder3.tvFourNum = (TextView) convertView.findViewById(R.id.tv_four_num);
                        holder3.tvEnd = (TextView) convertView.findViewById(R.id.tv_endtime);
                        convertView.setTag(holder3);
                        break;
                    default:
                        break;
                }
            } else {
                switch (type) {
                    case 0://普通
                        holder1 = (ViewHolder1) convertView.getTag();
                        break;
                    case 1://2张拼图
                        holder2 = (ViewHolder2) convertView.getTag();
                        break;
                    case 2://4张拼图
                        holder3 = (ViewHolder3) convertView.getTag();
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
                    if (getActivity() == null) {
                        break;
                    }
                    final int moneyOne = Integer.parseInt(card.getMoney());
                    if (moneyOne == 1) {
                        holder2.layoutTwo.setBackgroundDrawable(getActivity().getResources()
                                .getDrawable(R.drawable.bg_money_one_grey));
                    } else {
                        holder2.layoutTwo.setBackgroundDrawable(getActivity().getResources()
                                .getDrawable(R.drawable.bg_money_five_grey));
                    }
                    holder2.tvEnd.setText("有效期至" + TimeUtils.getCustomReportData(card.getEndtime() * 1000));
                    try {
                        JSONObject object = new JSONObject(card.getRemark());
                        Iterator iterator = object.keys();
                        int x = 0, y = 0;
                        holder2.tvOneNum.setVisibility(View.GONE);
                        holder2.tvTwoNum.setVisibility(View.GONE);
                        holder2.ivOne.setVisibility(View.GONE);
                        holder2.ivTwo.setVisibility(View.GONE);
                        while (iterator.hasNext()) {
                            String key = (String) iterator.next();
                            int value = object.getInt(key);
                            int num = Integer.parseInt(key);
                            switch (num) {
                                case 1:
                                    holder2.ivOne.setVisibility(View.VISIBLE);
                                    holder2.tvOneNum.setVisibility(View.VISIBLE);
                                    holder2.tvOneNum.setText(value + "");
                                    x = value;
                                    if (moneyOne == 1) {//1元
                                        holder2.ivOne.setImageResource(R.drawable.money_one_one);
                                    } else {//5元
                                        holder2.ivOne.setImageResource(R.drawable.money_five_one);
                                    }
                                    break;
                                case 2:
                                    holder2.ivTwo.setVisibility(View.VISIBLE);
                                    holder2.tvTwoNum.setVisibility(View.VISIBLE);
                                    holder2.tvTwoNum.setText(value + "");
                                    y = value;
                                    holder2.ivTwo.setImageResource(R.drawable.money_one_two);
                                    break;
                                default:
                                    holder2.tvOneNum.setVisibility(View.GONE);
                                    holder2.tvTwoNum.setVisibility(View.GONE);
                                    holder2.ivOne.setVisibility(View.GONE);
                                    holder2.ivTwo.setVisibility(View.GONE);
                                    break;
                            }
                        }
                        holder2.ivExchange.setImageResource(R.drawable.btn_my_card_exchange);
                        final int x1 = x;
                        final int y1 = y;
                        holder2.ivExchange.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (x1 > 0 && y1 > 0) {
                                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.common_dialog_custom, null);
                                    final AppDialog.Builder builder = new AppDialog.Builder(getActivity(), AppDialog.Builder.COMMONDIALOG);
                                    builder.setContentView(view).setTitle("确定兑换？")
                                            .setPositiveButton("确定", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    exchangeAward(moneyOne);
                                                    builder.dismiss();
                                                }
                                            })
                                            .setNegativeButton("取消", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    builder.dismiss();
                                                }
                                            }).create();
                                } else {
                                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.common_dialog_custom, null);
                                    final AppDialog.Builder builder = new AppDialog.Builder(getActivity(), AppDialog.Builder.COMMONDIALOG);
                                    builder.setContentView(view).setTitle("凑齐拼图卡券后才可兑换现金")
                                            .setPositiveButton("确定", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    builder.dismiss();
                                                }
                                            })
                                            .setNegativeButton("取消", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    builder.dismiss();
                                                }
                                            }).create();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        break;
                    }
                    break;
                case 2:
                    if (getActivity() == null) {
                        break;
                    }
                    final int moneyTwo = Integer.parseInt(card.getMoney());
                    if (moneyTwo == 10) {
                        holder3.layoutFour.setBackgroundDrawable(getActivity().getResources()
                                .getDrawable(R.drawable.bg_money_ten_grey));
                    } else {
                        holder3.layoutFour.setBackgroundDrawable(getActivity().getResources()
                                .getDrawable(R.drawable.bg_money_hundred_grey));
                    }
                    holder3.tvEnd.setText("有效期至" + TimeUtils.getCustomReportData(card.getEndtime() * 1000));
                    try {
                        JSONObject object = new JSONObject(card.getRemark());
                        Iterator iterator = object.keys();
                        int a = 0, b = 0, c = 0, d = 0;
                        holder3.tvOneNum.setVisibility(View.GONE);
                        holder3.tvTwoNum.setVisibility(View.GONE);
                        holder3.tvThreeNum.setVisibility(View.GONE);
                        holder3.tvFourNum.setVisibility(View.GONE);
                        holder3.ivOne.setVisibility(View.GONE);
                        holder3.ivTwo.setVisibility(View.GONE);
                        holder3.ivThree.setVisibility(View.GONE);
                        holder3.ivFour.setVisibility(View.GONE);
                        while (iterator.hasNext()) {
                            String key = (String) iterator.next();
                            int value = object.getInt(key);
                            int num = Integer.parseInt(key);
                            switch (num) {
                                case 1:
                                    holder3.ivOne.setVisibility(View.VISIBLE);
                                    holder3.tvOneNum.setVisibility(View.VISIBLE);
                                    holder3.tvOneNum.setText(value + "");
                                    a = value;
                                    holder3.ivOne.setImageResource(R.drawable.money_ten_one);
                                    break;
                                case 2:
                                    holder3.ivTwo.setVisibility(View.VISIBLE);
                                    holder3.tvTwoNum.setVisibility(View.VISIBLE);
                                    holder3.tvTwoNum.setText(value + "");
                                    b = value;
                                    if (moneyTwo == 10) {//10元
                                        holder3.ivTwo.setImageResource(R.drawable.money_ten_two);
                                    } else {//100元
                                        holder3.ivTwo.setImageResource(R.drawable.money_hundred_two);
                                    }
                                    break;
                                case 3:
                                    holder3.ivThree.setVisibility(View.VISIBLE);
                                    holder3.tvThreeNum.setVisibility(View.VISIBLE);
                                    holder3.tvThreeNum.setText(value + "");
                                    c = value;
                                    holder3.ivThree.setImageResource(R.drawable.money_ten_three);
                                    break;
                                case 4:
                                    holder3.ivFour.setVisibility(View.VISIBLE);
                                    holder3.tvFourNum.setVisibility(View.VISIBLE);
                                    holder3.tvFourNum.setText(value + "");
                                    d = value;
                                    holder3.ivFour.setImageResource(R.drawable.money_ten_four);
                                    break;
                                default:
                                    holder3.tvOneNum.setVisibility(View.GONE);
                                    holder3.tvTwoNum.setVisibility(View.GONE);
                                    holder3.tvThreeNum.setVisibility(View.GONE);
                                    holder3.tvFourNum.setVisibility(View.GONE);
                                    holder3.ivOne.setVisibility(View.GONE);
                                    holder3.ivTwo.setVisibility(View.GONE);
                                    holder3.ivThree.setVisibility(View.GONE);
                                    holder3.ivFour.setVisibility(View.GONE);
                                    break;
                            }

                        }
                        holder3.ivExchange.setImageResource(R.drawable.btn_my_card_exchange);
                        final int a1 = a;
                        final int b1 = b;
                        final int c1 = c;
                        final int d1 = d;
                        holder3.ivExchange.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (a1 > 0 && b1 > 0 && c1 > 0 && d1 > 0) {
                                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.common_dialog_custom, null);
                                    final AppDialog.Builder builder = new AppDialog.Builder(getActivity(), AppDialog.Builder.COMMONDIALOG);
                                    builder.setContentView(view).setTitle("确定兑换？")
                                            .setPositiveButton("确定", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    exchangeAward(moneyTwo);
                                                    builder.dismiss();
                                                }
                                            })
                                            .setNegativeButton("取消", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    builder.dismiss();
                                                }
                                            }).create();
                                } else {
                                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.common_dialog_custom, null);
                                    final AppDialog.Builder builder = new AppDialog.Builder(getActivity(), AppDialog.Builder.COMMONDIALOG);
                                    builder.setContentView(view).setTitle("凑齐拼图卡券后才可兑换现金")
                                            .setPositiveButton("确定", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    builder.dismiss();
                                                }
                                            })
                                            .setNegativeButton("取消", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    builder.dismiss();
                                                }
                                            }).create();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        break;
                    }
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
        RelativeLayout layoutTwo;
        ImageView ivOne;
        TextView tvOneNum;
        ImageView ivTwo;
        TextView tvTwoNum;
        ImageView ivExchange;
        TextView tvEnd;
    }

    static class ViewHolder3 {
        RelativeLayout layoutFour;
        ImageView ivOne;
        TextView tvOneNum;
        ImageView ivTwo;
        TextView tvTwoNum;
        ImageView ivThree;
        TextView tvThreeNum;
        ImageView ivFour;
        TextView tvFourNum;
        ImageView ivExchange;
        TextView tvEnd;
    }

    //兑换

    private void exchangeAward(int price) {

        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "convertCoupon");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("money", String.valueOf(price));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build().execute(new StringCallback() {
            @Override
            public void onBefore(Request request) {
                super.onBefore(request);
                if (getActivity() == null) {
                    return;
                }
                dialog = new ProgressDialog(getActivity());
                dialog.setMessage("兑换中");
                dialog.show();
            }

            @Override
            public void onError(Call call, Exception e) {
                if (getActivity() == null) {
                    return;
                }
                Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                if (getActivity() == null) {
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.getInt("code");
                    String notice = jsonObject.getString("notice");
                    Toast.makeText(getActivity(), notice, Toast.LENGTH_SHORT).show();
                    if (code > Result.RESULT_OK) {
                        isFirst = true;
                        getData();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onAfter() {
                super.onAfter();
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
