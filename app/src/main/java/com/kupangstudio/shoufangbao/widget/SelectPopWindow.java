package com.kupangstudio.shoufangbao.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.utils.CommonUtils;

import java.net.InterfaceAddress;

/**
 * Created by Jsmi on 2015/11/6.
 * 选择类型弹窗
 */
public class SelectPopWindow extends PopupWindow {
    public static final int SELECT_BUILD = 0;//楼盘
    public static final int SELECT_CUSTOM = 1;//客户
    private ListView lv;
    private boolean flag;
    private MyAdapter adapter;
    private int selectPos;
    private View view;

    public SelectPopWindow(Context context, int type, String[] items) {
        init(context, type, items);
    }

    private void init(Context ctx, int type, String[] items) {
        switch (type) {
            case SELECT_BUILD:
                flag = true;
                view = LayoutInflater.from(ctx).inflate(R.layout.selectpopwindow_list, null);
                lv = (ListView) view.findViewById(R.id.lv_select_popwindow);
                adapter = new MyAdapter(ctx, items);
                lv.setAdapter(adapter);
                setContentView(view);
                break;
            case SELECT_CUSTOM:
                flag = true;
                view = LayoutInflater.from(ctx).inflate(R.layout.item_popowindow_custom, null);
                lv = (ListView) view.findViewById(R.id.lv_select_popwindow);
                adapter = new MyAdapter(ctx, items);
                lv.setAdapter(adapter);
                setContentView(view);
                break;
            default:
                break;
        }
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isShowing()) {
                    dismiss();
                }
                return false;
            }
        });
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ColorDrawable drawable = new ColorDrawable(0x4c000000);
        setBackgroundDrawable(drawable);
        setOutsideTouchable(true);
        if (Build.VERSION.SDK_INT < 19) {
            setFocusable(true);
        }
        //setTouchable(true);
        setAnimationStyle(R.style.PopupWindowAnimation);
    }

    class MyAdapter extends BaseAdapter {
        String[] items;
        LayoutInflater inflater;

        public MyAdapter(Context ctx, String[] items) {
            this.items = items;
            inflater = LayoutInflater.from(ctx);
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_popwindow, parent, false);
                holder = new ViewHolder();
                holder.tv = (TextView) convertView.findViewById(R.id.item_popwindow_tv);
                holder.layout = (LinearLayout) convertView.findViewById(R.id.item_popwindow_layout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv.setText(items[position]);
            if (!flag) {
                if (position == selectPos) {
                    holder.tv.setTextColor(Color.parseColor("#be1a20"));
                    holder.layout.setBackgroundColor(Color.parseColor("#f5f5f5"));
                } else {
                    holder.tv.setTextColor(Color.parseColor("#5a5a5a"));
                    holder.layout.setBackgroundColor(Color.parseColor("#ffffff"));
                }
            }
            return convertView;
        }
    }

    static class ViewHolder {
        TextView tv;
        LinearLayout layout;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        lv.setOnItemClickListener(listener);
    }

    public void setTouchListener(View.OnTouchListener listener) {
        view.setOnTouchListener(listener);
    }

    public void setSelection(int pos) {
        this.selectPos = pos;
        flag = false;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
