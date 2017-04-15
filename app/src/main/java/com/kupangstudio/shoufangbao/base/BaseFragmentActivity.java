package com.kupangstudio.shoufangbao.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.kupangstudio.shoufangbao.MainActivity;

/**
 * app基类FragmentActivity
 * Created by long on 15/11/2.
 * Copyright 2015 android_xiaobai.
 */
public class BaseFragmentActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
