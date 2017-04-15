package com.kupangstudio.shoufangbao.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.model.BuildParameter;
import com.kupangstudio.shoufangbao.model.BuildProfile;
import com.kupangstudio.shoufangbao.widget.MyListView;

import java.util.List;

/**
 * Created by long1 on 16/3/2.
 * Copyright 16/3/2 android_xiaobai.
 * 楼盘参数
 */
public class BuildParameterFragment extends Fragment {

    private List<BuildParameter> profile;
    private HouseParameterAdapter adapter;
    private MyListView lv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_build_parameter, container, false);
        profile = (List<BuildParameter>) getArguments().getSerializable("buildParameter");
        lv = (MyListView) view.findViewById(R.id.parameter_lv);
        if(getActivity() != null) {
            adapter = new HouseParameterAdapter(profile, getActivity());
            lv.setAdapter(adapter);
        }
        return view;
    }

    /**
     * 楼盘参数
     */

    class HouseParameterAdapter extends BaseAdapter {

        LayoutInflater inflater;
        List<BuildParameter> list;

        public HouseParameterAdapter(List<BuildParameter> list, Context ctx) {
            this.list = list;
            inflater = LayoutInflater.from(ctx);
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
            BuildParameter hp = list.get(position);
            if (hp.getType() == 1) {
                convertView = inflater.inflate(R.layout.item_houseparameter,
                        parent, false);
                TextView name = (TextView) convertView.findViewById(R.id.itemparameter_name);
                TextView tv = (TextView) convertView
                        .findViewById(R.id.itemparameter_content);
                name.setText(hp.getName() + "：");
                tv.setText(hp.getContent());
            } else {
                convertView = inflater.inflate(R.layout.item_houseparameter_two, parent, false);
                TextView name = (TextView) convertView.findViewById(R.id.item_parametertwo_name);
                TextView content = (TextView) convertView.findViewById(R.id.item_parametertwo_content);
                name.setText(hp.getName() + "：");
                content.setText(hp.getContent());
            }

            return convertView;
        }

    }
}
