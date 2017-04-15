package com.kupangstudio.shoufangbao.widget;

/**
 * Created by Jsmi on 2015/11/9.
 */

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.R;

import java.util.ArrayList;

public class PopMenu {
    private ArrayList<String> itemList;
    private String string;
    private Context context;
    private PopupWindow popupWindow;
    private ListView listView;
    private int[] drawable;
    private boolean isZero = false;

    @SuppressWarnings("deprecation")
    public PopMenu(Context context) {
        this.context = context;

        itemList = new ArrayList<String>();
        drawable = new int[]{};

        View view = LayoutInflater.from(context)
                .inflate(R.layout.common_popmenu, null);

        listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(new PopAdapter());
        listView.setFocusableInTouchMode(true);
        listView.setFocusable(true);

        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
    }

    @SuppressWarnings("deprecation")
    public PopMenu(Context context, int[] drawable, String string) {
        this.context = context;
        this.drawable = drawable;
        isZero = true;
        itemList = new ArrayList<String>();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.common_popmenu_actionbar, null);

        listView = (ListView) view.findViewById(R.id.listView_actionbar);
        listView.setAdapter(new PopAdapter1());
        listView.setFocusableInTouchMode(true);
        listView.setFocusable(true);
        if (drawable.length > 0) {
            popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            listView.setDividerHeight(0);
        }
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
    }


    @SuppressWarnings("deprecation")
    public PopMenu(Context context, int[] drawable) {
        this.context = context;
        this.drawable = drawable;
        isZero = true;

        itemList = new ArrayList<String>();


        View view = LayoutInflater.from(context)
                .inflate(R.layout.common_popmenu_actionbar, null);

        listView = (ListView) view.findViewById(R.id.listView_actionbar);
        listView.setAdapter(new PopAdapter());
        listView.setFocusableInTouchMode(true);
        listView.setFocusable(true);
        if (drawable.length > 0) {
            popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            listView.setDividerHeight(0);
        }
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        listView.setOnItemClickListener(listener);
    }

    public void addItems(String[] items) {
        for (String s : items)
            itemList.add(s);
    }

    public void addItem(String item) {
        itemList.add(item);
    }

    public void showAsDropDown(View parent) {
        if (isZero) {
            popupWindow.showAsDropDown(parent, 30, 0);
        } else {
            popupWindow.showAsDropDown(parent, 10, context.getResources()
                    .getDimensionPixelSize(R.dimen.popmenu_yoff));
        }

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.update();
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    private final class PopAdapter extends BaseAdapter {

        public PopAdapter() {
        }


        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (drawable.length > 0) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(
                            R.layout.item_popmenu_actionbar, null);
                    holder = new ViewHolder();
                    holder.groupItem = (TextView) convertView
                            .findViewById(R.id.textView_actionbar);
                    holder.groupImage = (ImageView) convertView
                            .findViewById(R.id.imageView_actionbar);

                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.groupImage.setVisibility(View.VISIBLE);
                holder.groupImage.setImageResource(drawable[position]);
                holder.groupItem.setText(itemList.get(position));
            } else {
                ViewHolderTwo holderTwo;
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(
                            R.layout.item_popmenu_actionbar_noimage, null);
                    holderTwo = new ViewHolderTwo();
                    holderTwo.groupItemTwo = (TextView) convertView.findViewById(R.id.textView);
                    convertView.setTag(holderTwo);
                } else {
                    holderTwo = (ViewHolderTwo) convertView.getTag();
                }
                holderTwo.groupItemTwo.setText(itemList.get(position));
            }

            return convertView;
        }

        private final class ViewHolder {
            TextView groupItem;
            ImageView groupImage;
        }

        private final class ViewHolderTwo {
            TextView groupItemTwo;
        }
    }


    private final class PopAdapter1 extends BaseAdapter {

        public PopAdapter1() {
        }


        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (drawable.length > 0) {
                ViewHolder1 holder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(
                            R.layout.item_popmenu_actionbar_image, null);
                    holder = new ViewHolder1();
                    holder.groupItem = (TextView) convertView
                            .findViewById(R.id.textView_actionbar);
                    holder.groupImage = (ImageView) convertView
                            .findViewById(R.id.imageView_actionbar);

                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder1) convertView.getTag();
                }

                holder.groupImage.setVisibility(View.VISIBLE);
                holder.groupImage.setImageResource(drawable[position]);
                holder.groupItem.setText(itemList.get(position));
            }
            return convertView;
        }

        private final class ViewHolder1 {
            TextView groupItem;
            ImageView groupImage;
        }
    }
}
