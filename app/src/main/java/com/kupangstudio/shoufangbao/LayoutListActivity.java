package com.kupangstudio.shoufangbao;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.model.BuildDetail;
import com.kupangstudio.shoufangbao.model.BuildImage;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by long1 on 15/11/13.
 * Copyright 15/11/13 android_xiaobai.
 * 户型图列表
 */
public class LayoutListActivity extends BaseActivity {

    private ArrayList<BuildImage> list;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private ListView lv;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_list);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "户型图");
        list = (ArrayList)getIntent().getSerializableExtra("layoutList");
        initView();
        init();
    }

    private void initView() {
        lv = (ListView) findViewById(R.id.lv_layout_list);
    }

    private void init() {
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).build();
        adapter = new MyAdapter(LayoutListActivity.this, list);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent(LayoutListActivity.this, LayoutPicDetailActivity.class);
                it.putExtra("layoutList", (ArrayList) list);
                it.putExtra("position", position);
                startActivity(it);
            }
        });
    }

    private class MyAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<BuildImage> list;

        private MyAdapter(Context context, ArrayList<BuildImage> list) {
            inflater = LayoutInflater.from(context);
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder holder;
            BuildImage h = list.get(i);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_build_detail_layout, viewGroup, false);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            imageLoader.displayImage(h.getUrl(), holder.imageView, options);
            return convertView;
        }

    }

    private class ViewHolder {
        public ImageView imageView;
        public TextView title;
        public TextView content;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
