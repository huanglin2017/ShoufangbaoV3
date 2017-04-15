package com.kupangstudio.shoufangbao;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.model.Custom;
import com.kupangstudio.shoufangbao.model.FollowList;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by long1 on 15/11/24.
 * Copyright 15/11/24 android_xiaobai.
 */
public class ReportSelectCustomActivity extends BaseActivity {
    private ListView lv;
    private List<Custom> listdatas;
    private ImageView leftbutton, rightbutton;
    private TextView title;
    private MessageAdapter adapter;
    private RelativeLayout empty;
    private TextView emotyText;
    private Button emotyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportselectcustom);
        CommonUtils.addActivity(this);
        initView();
        setClickListener();
        User user = User.getInstance();
        listdatas = DataSupport.where("uid = ?", String.valueOf(user.uid)).find(Custom.class);
        for (int i = 0; i < listdatas.size(); i++) {
            List<FollowList> lists = DataSupport.where("custom_id = ?", String.valueOf(listdatas.get(i).getId())).find(FollowList.class);
            listdatas.get(i).setFollow(lists.get(0));
        }
        adapter = new MessageAdapter(ReportSelectCustomActivity.this,
                listdatas);
        lv.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    private void setClickListener() {
        leftbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Custom c = listdatas.get(position);
                Intent it = new Intent();
                it.putExtra("name", c.getName());
                it.putExtra("num", c.getTel());
                it.putExtra("cid", c.getCid());
                it.putExtra("uid", c.getUid());
                it.putExtra("sex", c.getGender());
                setResult(Activity.RESULT_OK, it);
                finish();
            }
        });
    }

    private void initView() {
        lv = (ListView) findViewById(R.id.list_report_selectcustom);
        leftbutton = (ImageView) findViewById(R.id.navbar_image_left);
        rightbutton = (ImageView) findViewById(R.id.navbar_image_right);
        title = (TextView) findViewById(R.id.navbar_title);
        rightbutton.setVisibility(View.GONE);
        empty = (RelativeLayout) findViewById(R.id.emptyview);
        emotyText = (TextView) findViewById(R.id.emptyview_text);
        emotyText.setText("您暂时还没有客户");
        emotyBtn = (Button) findViewById(R.id.emptyview_btn);
        emotyBtn.setVisibility(View.GONE);
        title.setText("选择客户");
        lv.setEmptyView(empty);
    }

    class ViewHolder {
        CheckBox cb;
        ImageView identity;
        TextView title;
        TextView state;
        TextView times;
    }

    class MessageAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<Custom> list;

        public MessageAdapter(Context ctx, List<Custom> list) {
            inflater = LayoutInflater.from(ctx);
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(
                        R.layout.item_list_message_custom, parent, false);
                holder.identity = (ImageView) convertView
                        .findViewById(R.id.item_message_identity);
                holder.title = (TextView) convertView
                        .findViewById(R.id.item_message_name);
                holder.state = (TextView) convertView
                        .findViewById(R.id.item_message_state);
                holder.times = (TextView) convertView
                        .findViewById(R.id.item_message_times);
                holder.cb = (CheckBox) convertView
                        .findViewById(R.id.item_message_check);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Custom c = list.get(position);
            holder.title.setText(c.getName());
            if (c.getFollow().getCtime() == null) {
                holder.times.setText("无沟通记录");
            } else {
                long offset = System.currentTimeMillis() / 1000
                        - c.getFollow().getCtime();

                if (offset <= 0) {
                    holder.times.setText("刚刚沟通过");
                } else {

                    int min = (int) (offset / 60);
                    if (min < 60) {
                        if (min < 5) {
                            holder.times.setText("刚刚沟通过");
                        } else {
                            holder.times.setText(min + "分钟前沟通");
                        }

                    } else {
                        int hour = (int) (min / (60));
                        if (hour < 24) {
                            holder.times.setText(hour + "小时前沟通");
                        } else {
                            int day = hour / 24;
                            if (day > 90) {
                                holder.times.setText("无沟通记录");
                            } else {
                                holder.times.setText(day + "天前沟通记录");
                            }
                        }
                    }

                }
            }
            return convertView;
        }

    }
}
