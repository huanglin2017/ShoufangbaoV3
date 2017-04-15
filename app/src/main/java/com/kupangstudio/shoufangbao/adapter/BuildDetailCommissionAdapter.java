package com.kupangstudio.shoufangbao.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.model.BuildCommission;
import com.kupangstudio.shoufangbao.utils.DensityUtils;
import com.kupangstudio.shoufangbao.utils.ScreenUtils;

import java.util.List;


/**
 * Created by long1 on 16/3/2.
 * Copyright 16/3/2 android_xiaobai.
 * 楼盘详情佣金adapter
 */
public class BuildDetailCommissionAdapter extends RecyclerView.Adapter<BuildDetailCommissionAdapter.MyViewHolder> {

    private Context context;
    private List<BuildCommission> commissions;

    public BuildDetailCommissionAdapter(Context context, List<BuildCommission> commissions) {
        this.context = context;
        this.commissions = commissions;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_build_detail_commission
                , parent, false);
        MyViewHolder holder = new MyViewHolder(view, viewType);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        BuildCommission commission = commissions.get(position);
        holder.tvPrice.setText(commission.getCommission());
        holder.tvType.setText(commission.getName());
    }

    @Override
    public int getItemCount() {
        return commissions.size();
    }

//    @Override
//    public int getItemViewType(int position) {
//        if (commissions.size() == 1) {
//            return 0;
//        } else {
//            return 1;
//        }
//    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvPrice;
        TextView tvType;
        LinearLayout layout;

        public MyViewHolder(View itemView, int type) {
            super(itemView);
            tvPrice = (TextView) itemView.findViewById(R.id.tv_price);
            tvType = (TextView) itemView.findViewById(R.id.tv_type);
            layout = (LinearLayout) itemView.findViewById(R.id.layout);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
            int width = (ScreenUtils.getScreenWidth(context) - DensityUtils.dp2px(context, 10) * 3) / 2;
            params.width = width;
//            if (type == 0) {
//                int margin = (ScreenUtils.getScreenWidth(context) - width) / 2;
//                params.setMargins(margin, 0, margin, 0);
//            }
            layout.setLayoutParams(params);
        }
    }
}
