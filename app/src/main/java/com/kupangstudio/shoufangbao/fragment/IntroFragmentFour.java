package com.kupangstudio.shoufangbao.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kupangstudio.shoufangbao.MainActivity;
import com.kupangstudio.shoufangbao.MiuiPointActivity;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.SelectHomeCityActivity;
import com.kupangstudio.shoufangbao.base.BaseFragment;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;

/**
 * Created by long on 15/11/9.
 * Copyright 2015 android_xiaobai.
 * 介绍页四
 */
public class IntroFragmentFour extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_four, container, false);
        ImageView startBtn = (ImageView) view.findViewById(R.id.intro_start_btn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.checkOtherSystem(getActivity())) {//是否是小米系统
                    Intent it = new Intent(getActivity(), MiuiPointActivity.class);
                    startActivity(it);
                } else {
                    boolean setHomeCity = (boolean) SPUtils.get(getActivity(), Constants.HOME_SET_CITY, false);
                    if (setHomeCity) {//是否选择过首页城市
                        Intent it = new Intent(getActivity(), MainActivity.class);
                        startActivity(it);
                    } else {
                        Intent it = new Intent(getActivity(), SelectHomeCityActivity.class);
                        it.putExtra(Constants.IS_FROM_FIRST, true);
                        startActivity(it);
                    }
                }
            }
        });
        return view;
    }
}
