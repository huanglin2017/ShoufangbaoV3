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

import java.util.ArrayList;

/**
 * Created by Jsmi on 2015/2/29.
 * 选择类型弹窗
 */
public class MoreSelectPopWindow extends PopupWindow {
    public static final int SELECT_BUILD = 1;//客户
    private ListView lv;
    private boolean flag;
    private boolean flag1;
    private boolean firsFlag;
    private MyAdapter adapter;
    private ChildAdapter mAdapter;
    private int selectPos;
    private int selectPos2;
    private int selectChildPos;
    private String title;
    private String title2;
    private ListView lv1;
    private int selectPos3;
    private View view;

    public MoreSelectPopWindow(Context context, int type, String[] items, String[] items1) {
        init(context, type, items, items1);
    }

    private void init(Context ctx, int type, String[] items, String[] items1) {
        switch (type) {
            case SELECT_BUILD:
                flag = true;
                view = LayoutInflater.from(ctx).inflate(R.layout.item_popwindow_two, null);
                lv = (ListView) view.findViewById(R.id.lv_select_popwindow);
                lv1 = (ListView) view.findViewById(R.id.lv_select_popwindow_1);
                adapter = new MyAdapter(ctx, items);
                mAdapter = new ChildAdapter(ctx, items1);
                lv.setAdapter(adapter);
                lv1.setAdapter(mAdapter);
                setContentView(view);
                break;
            default:
                break;
        }

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
            ViewHoder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_popweindow_nocolor, parent, false);
                holder = new ViewHoder();
                holder.tv = (TextView) convertView.findViewById(R.id.item_popwindow_tv);
                holder.layout = (LinearLayout) convertView.findViewById(R.id.item_popwindow_layout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHoder) convertView.getTag();
            }
            holder.tv.setText(items[position]);
            if (!flag) {
                if (position == selectPos) {
                    holder.tv.setTextColor(Color.parseColor("#be1a20"));
                    //holder.layout.setBackgroundColor(Color.parseColor("#f5f5f5"));
                    if (title != null && !title.equals("")) {
                        holder.tv.setText(title);
                    }
                } else {
                    if (position == selectPos2) {
                        holder.tv.setTextColor(Color.parseColor("#be1a20"));
                        //holder.layout.setBackgroundColor(Color.parseColor("#f5f5f5"));
                        if (title2 != null && !title2.equals("")) {
                            holder.tv.setText(title2);
                        }
                    } else {
                        holder.tv.setTextColor(Color.parseColor("#5a5a5a"));
                        //holder.layout.setBackgroundColor(Color.parseColor("#ffffff"));
                    }
                }
                if (firsFlag) {
                    if (position == 0) {
                        holder.layout.setBackgroundColor(Color.parseColor("#f5f5f5"));
                    } else {
                        holder.layout.setBackgroundColor(Color.parseColor("#ffffff"));
                    }
                } else {
                    if (position == selectPos3) {
                        holder.layout.setBackgroundColor(Color.parseColor("#f5f5f5"));
                    } else {
                        holder.layout.setBackgroundColor(Color.parseColor("#ffffff"));
                    }
                }
            }
            return convertView;
        }
    }

    class ChildAdapter extends BaseAdapter {
        String[] items;
        LayoutInflater inflater;

        public ChildAdapter(Context ctx, String[] items) {
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
            ViewHoder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_popwindow, parent, false);
                holder = new ViewHoder();
                holder.tv = (TextView) convertView.findViewById(R.id.item_popwindow_tv);
                holder.layout = (LinearLayout) convertView.findViewById(R.id.item_popwindow_layout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHoder) convertView.getTag();
            }
            holder.tv.setText(items[position]);
            if (!flag1) {
                if (position == selectChildPos) {
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

    static class ViewHoder {
        TextView tv;
        LinearLayout layout;
    }

    public void setFirstSelect() {
        firsFlag = true;
        adapter.notifyDataSetChanged();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        lv.setOnItemClickListener(listener);
    }

    public void setOnItemClickListenerChild(AdapterView.OnItemClickListener listener) {
        lv1.setOnItemClickListener(listener);
    }

    public void setTouchListener(View.OnTouchListener listener) {
        view.setOnTouchListener(listener);
    }

    public void setSelectionChild(int pos) {
        this.selectChildPos = pos;
        flag1 = false;
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void setSelectionParent(int pos, int pos2, String title, String title2) {
        this.title = title;
        this.title2 = title2;
        flag = false;
        if (adapter != null) {
            selectPos = pos;
            selectPos2 = pos2;
            adapter.notifyDataSetChanged();
        }
    }

    public void setSelection(int posChild, int pos, int pos2, ArrayList<String[]> mList, int selectFeture, int selectLayout) {
        flag = false;
        flag1 = false;
        firsFlag = false;
        if (adapter != null) {
            selectPos = pos;
            selectPos2 = pos2;
            selectPos3 = posChild;
            if (selectFeture != 0) {
                this.title = mList.get(0)[selectFeture];
            } else {
                this.title = "特色";
            }
            if (selectLayout != 0) {
                this.title2 = mList.get(1)[selectLayout];
            } else {
                this.title2 = "户型";
            }
            adapter.notifyDataSetChanged();
        }
        if (posChild == 0) {
            this.selectChildPos = selectFeture;
            if (mAdapter != null) {
                mAdapter.items = mList.get(0);
                mAdapter.notifyDataSetChanged();
            }
        } else {
            this.selectChildPos = selectLayout;
            if (mAdapter != null) {
                mAdapter.items = mList.get(1);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
