package com.kupangstudio.shoufangbao;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.model.ContactBean;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.PinyinCompare;
import com.kupangstudio.shoufangbao.widget.SideBar;
import com.kupangstudio.shoufangbao.widget.SideBar.OnTouchingLetterChangedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectContactsActivity extends Activity implements SectionIndexer {

    private LinearLayout titleLayout;
    private TextView title;
    private SideBar sideBar;
    private TextView dialog;
    private ArrayList<ContactBean> list;
    ArrayList<ContactBean> filterCustomList;
    private AsyncQueryHandler asyncQuery;
    private Map<Integer, ContactBean> contactIdMap = null;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinCompare pinyinComparator;
    private boolean isfirst = true;
    private boolean isshow = false;// titleLayout
    /**
     * 上次第一个可见元素，用于滚动时记录标识。
     */
    private int lastFirstVisibleItem = -1;
    // 触摸事件
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    // 26个字母
    public static String[] b = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};
    private ListView mListView;
    private ContactAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);
        CommonUtils.handleTitleBarRightGone(this, "选择联系人");
        initView();

        // 实例化汉字转拼音类
        pinyinComparator = new PinyinCompare();
        sideBar.setAlphaColor(SelectContactsActivity.this.getResources().getColor(R.color.common_select));
        sideBar.setTextView(dialog, SelectContactsActivity.this);

        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                if (list != null) {
                    int position = getPositionForSection(s.charAt(0));
                    if (position != -1) {
                        mListView.setSelection(position);
                    }
                }
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL
                        && mListView.isEnabled() && isshow) {
                    titleLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (list == null) {
                    return;
                }
                int section = getSectionForPosition(firstVisibleItem);
                int nextSection = getSectionForPosition(firstVisibleItem + 1);
                int nextSecPosition = getPositionForSection(nextSection);
                if (firstVisibleItem != lastFirstVisibleItem) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleLayout
                            .getLayoutParams();
                    params.topMargin = 0;
                    titleLayout.setLayoutParams(params);
                    int index = getPositionForSection(section);
                    if (index >= 0 && index < list.size()) {
                        title.setText(list.get(index).getSortKey());
                    }
                }
                if (nextSecPosition == firstVisibleItem + 1) {
                    View childView = view.getChildAt(0);
                    if (childView != null) {
                        int titleHeight = titleLayout.getHeight();
                        int bottom = childView.getBottom();
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleLayout
                                .getLayoutParams();
                        if (bottom < titleHeight) {
                            float pushedDistance = bottom - titleHeight;
                            params.topMargin = (int) pushedDistance;
                            titleLayout.setLayoutParams(params);
                        } else {
                            if (params.topMargin != 0) {
                                params.topMargin = 0;
                                titleLayout.setLayoutParams(params);
                            }
                        }
                    }
                }
                lastFirstVisibleItem = firstVisibleItem;
            }
        });


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> view, View itemview, int pos, long arg3) {
                ContactBean bean = list.get(pos);
                Intent it = new Intent();
                it.putExtra("name", bean.getDisplayName());
                it.putExtra("num", bean.getPhoneNum());
                SelectContactsActivity.this.setResult(Activity.RESULT_OK, it);
                SelectContactsActivity.this.finish();
            }
        });
        init();
    }

    private void initView (){
        mListView = (ListView) findViewById(R.id.custom_list);
        //设置客户列表
        mListView.setFooterDividersEnabled(false);
        mListView.setHeaderDividersEnabled(false);
        titleLayout = (LinearLayout) findViewById(R.id.title_layout);
        title = (TextView) findViewById(R.id.title_layout_catalog);
        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    @Override
    public int getPositionForSection(int section) {
        for (int i = 0; i < list.size(); i++) {
            String sortStr = list.get(i).getSortKey();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    @Override
    public int getSectionForPosition(int position) {

        if (list != null && list.size() > position) {
            return list.get(position).getSortKey().charAt(0);
        }
        return 0;
    }


    private void init() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
        String[] projection = {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.DATA1,
                "sort_key",
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
        };
        // 查询的列
        asyncQuery = new MyAsyncQueryHandler(SelectContactsActivity.this.getContentResolver());
        asyncQuery.startQuery(0, null, uri, projection, null, null,
                "sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
    }

    /**
     * 数据库异步查询类AsyncQueryHandler
     *
     * @author administrator
     */
    private class MyAsyncQueryHandler extends AsyncQueryHandler {

        public MyAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        /**
         * 查询结束的回调函数
         */
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0) {

                contactIdMap = new HashMap<Integer, ContactBean>();

                list = new ArrayList<ContactBean>();
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    String name = cursor.getString(1);
                    String number = cursor.getString(2);
                    String sortKey = getAlpha(cursor.getString(3));
                    int contactId = cursor.getInt(4);
                    Long photoId = cursor.getLong(5);
                    String lookUpKey = cursor.getString(6);

                    if (!contactIdMap.containsKey(contactId)) {
                        ContactBean cb = new ContactBean();
                        cb.setDisplayName(name);
                        cb.setPhoneNum(number);
                        cb.setSortKey(sortKey);
                        cb.setContactId(contactId);
                        cb.setPhotoId(photoId);
                        cb.setLookUpKey(lookUpKey);
                        list.add(cb);
                        contactIdMap.put(contactId, cb);
                    }
                }
                if (list.size() > 0) {
                    setAdapter();
                }
            }
        }
    }

    class ContactAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private ArrayList<ContactBean> data;

        public ContactAdapter(Context ctx, ArrayList<ContactBean> data) {
            inflater = LayoutInflater.from(ctx);
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int pos) {
            return data.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        @Override
        public View getView(int pos, View contentView, ViewGroup parent) {
            ViewHolder holder = null;
            if (contentView != null) {
                holder = (ViewHolder) contentView.getTag();
            } else {
                holder = new ViewHolder();
                contentView = inflater.inflate(R.layout.item_select_contact, parent, false);
                holder.name = (TextView) contentView.findViewById(R.id.contact_name);
                holder.phone = (TextView) contentView.findViewById(R.id.contact_number);
                holder.letterTag = (TextView) contentView.findViewById(R.id.catalog);
                contentView.setTag(holder);
            }
            ContactBean c = data.get(pos);
            holder.name.setText(c.getDisplayName());
            holder.phone.setText(c.getPhoneNum());
            String firstLetter = c.getSortKey().toUpperCase();
            if (pos == 0) {
                if (data.size() > 0) {
                    holder.letterTag.setVisibility(View.VISIBLE);
                    holder.letterTag.setText(firstLetter);
                } else {
                    holder.letterTag.setVisibility(View.GONE);
                }
            } else {
                if (data.size() > 0) {
                    String firstLetterPre = data.get(pos - 1).getSortKey().toUpperCase();
                    if (firstLetter.equals(firstLetterPre)) {
                        holder.letterTag.setVisibility(View.GONE);
                    } else {
                        holder.letterTag.setVisibility(View.VISIBLE);
                        holder.letterTag.setText(firstLetter);
                    }
                } else {
                    holder.letterTag.setVisibility(View.GONE);
                }
            }
            if (data.size() > 0) {
                titleLayout.setVisibility(View.VISIBLE);
            } else {
                titleLayout.setVisibility(View.GONE);
            }

            return contentView;
        }

        class ViewHolder {
            TextView name;
            TextView phone;
            TextView letterTag;
        }
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new ContactAdapter(SelectContactsActivity.this, list);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.data = list;
            mAdapter.notifyDataSetChanged();
        }
        int index = mListView.getFirstVisiblePosition();
        if (index >= 0 && index < list.size()) {
            title.setText(list.get(index).getSortKey());
        }
    }


    /**
     * @param str
     * @return
     */
    private String getAlpha(String str) {
        if (CommonUtils.getFirstSpell(str).length() == 0) {
            return "#";
        } else {
            String pinyin = CommonUtils.getFirstSpell(str);
            String sortString = pinyin.substring(0, 1).toUpperCase();
            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                return sortString.toUpperCase();
            } else {
                return "#";
            }
        }
    }
}
