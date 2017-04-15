package com.kupangstudio.shoufangbao.base;

import android.app.Activity;
import android.os.Bundle;
/**
 * app基类activity
 * Created by long on 15/11/2.
 * Copyright 2015 android_xiaobai.
 */
public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
