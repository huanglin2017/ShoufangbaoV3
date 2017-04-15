package com.kupangstudio.shoufangbao.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.model.BuildProfile;

/**
 * Created by long1 on 16/3/2.
 * Copyright 16/3/2 android_xiaobai.
 * 楼盘卖点
 */
public class BuildSalesFragment extends Fragment {

    private TextView tvSales;//楼盘卖点
    private TextView tvCustoms;//目标客户
    private TextView tvTech;//拓客技巧
    private TextView tvSupport;//售房宝支持
    private BuildProfile sell;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_build_sales, container, false);
        initView(view);
        sell = (BuildProfile) getArguments().getSerializable("buildProfile");
        tvSales.setText(sell.getSell());
        tvCustoms.setText(sell.getTarget());
        tvTech.setText(sell.getSkill());
        tvSupport.setText(sell.getSupport());
        return view;
    }

    private void initView(View view) {
        tvSales = (TextView) view.findViewById(R.id.tv_build_sales);
        tvCustoms = (TextView) view.findViewById(R.id.tv_goal_custom);
        tvTech = (TextView) view.findViewById(R.id.tv_tuoke_tech);
        tvSupport = (TextView) view.findViewById(R.id.tv_sfb_support);
    }
}
