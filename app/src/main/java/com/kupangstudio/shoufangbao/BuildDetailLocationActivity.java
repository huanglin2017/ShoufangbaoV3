package com.kupangstudio.shoufangbao;

import android.os.Bundle;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.utils.CommonUtils;

/**
 * Created by long1 on 16/2/26.
 * Copyright 16/2/26 android_xiaobai.
 */
public class BuildDetailLocationActivity extends BaseActivity {

    MapView mMapView;
    BaiduMap mBaiduMap;
    String lat;
    String lng;
    String address;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtils.addActivity(this);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_build_detail_location);
        mMapView = (MapView) findViewById(R.id.map_build_detail);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        lat = getIntent().getStringExtra("lat");
        lng = getIntent().getStringExtra("lng");
        address = getIntent().getStringExtra("position");
        name = getIntent().getStringExtra("name");
        CommonUtils.handleTitleBarRightGone(this, name);
        LatLng point = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.build_detail_position);
//构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        MapStatus mapStatus = new MapStatus.Builder()
                .target(point)
                .zoom(18)
                .build();
        TextView textView = new TextView(this);
        textView.setText(name);
        InfoWindow mInfoWindow = new InfoWindow(textView, point, -47);
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
//显示InfoWindow
        mBaiduMap.setMapStatus(mapStatusUpdate);
        mBaiduMap.addOverlay(option);
        mBaiduMap.showInfoWindow(mInfoWindow);
//在地图上添加Marker，并显示
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
}
