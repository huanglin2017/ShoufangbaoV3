package com.kupangstudio.shoufangbao.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.base.BaseFragment;

/**
 * Created by long on 15/11/9.
 * Copyright 2015 android_xiaobai.
 * 介绍页一
 */
public class IntroFragmentOne extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_one, container, false);
        return view;
    }
}
