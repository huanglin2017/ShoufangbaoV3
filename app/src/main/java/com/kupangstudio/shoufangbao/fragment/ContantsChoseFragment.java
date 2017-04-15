package com.kupangstudio.shoufangbao.fragment;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.base.BaseFragment;
import com.kupangstudio.shoufangbao.model.ContactBean;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.widget.QuickAlphabeticBar;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by Jsmi on 2015/11/13.
 * 联系人修改
 */
public class ContantsChoseFragment extends Fragment {
    private ListView mListView;
    private List<ContactBean> list;
    private AsyncQueryHandler asyncQuery;
    private QuickAlphabeticBar alpha;
    private ContactHomeAdapter adapter;

    private Map<Integer, ContactBean> contactIdMap = null;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contantschose, container, false);

        mListView = (ListView) view.findViewById(R.id.contantschose_listview);
        alpha = (QuickAlphabeticBar) view.findViewById(R.id.fast_scroller);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int pos,
                                    long arg3) {
                ContactBean bean = list.get(pos);
                Intent it = new Intent();
                it.putExtra("name", bean.getDisplayName());
                it.putExtra("num", bean.getPhoneNum());
                getActivity().setResult(Activity.RESULT_OK, it);
                getActivity().finish();
            }

        });

        return view;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
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
        asyncQuery = new MyAsyncQueryHandler(getActivity().getContentResolver());
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
                    String sortKey = cursor.getString(3);
                    int contactId = cursor.getInt(4);
                    Long photoId = cursor.getLong(5);
                    String lookUpKey = cursor.getString(6);

                    if (contactIdMap.containsKey(contactId)) {

                    } else {

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
                    setAdapter(list);
                }
            }
        }

    }


    private void setAdapter(List<ContactBean> list) {
        adapter = new ContactHomeAdapter(getActivity(), list, alpha);
        mListView.setAdapter(adapter);
        alpha.init(getActivity());
        alpha.setListView(mListView);
        alpha.setHight(alpha.getHeight());
        alpha.setVisibility(View.VISIBLE);

    }


    public class ContactHomeAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<ContactBean> list;
        private HashMap<String, Integer> alphaIndexer;
        private String[] sections;
        private Context ctx;

        public ContactHomeAdapter(Context context, List<ContactBean> list,
                                  QuickAlphabeticBar alpha) {

            this.ctx = context;
            this.inflater = LayoutInflater.from(context);
            this.list = list;
            this.alphaIndexer = new HashMap<String, Integer>();
            this.sections = new String[list.size()];

            for (int i = 0; i < list.size(); i++) {
                String name = getAlpha(list.get(i).getSortKey());
                if (!alphaIndexer.containsKey(name)) {
                    alphaIndexer.put(name, i);
                }
            }

            Set<String> sectionLetters = alphaIndexer.keySet();
            ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
            Collections.sort(sectionList);
            sections = new String[sectionList.size()];
            sectionList.toArray(sections);

            alpha.setAlphaIndexer(alphaIndexer);
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

        public void remove(int position) {
            list.remove(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_contact_list,
                        null);
                holder = new ViewHolder();
                holder.alpha = (TextView) convertView.findViewById(R.id.alpha);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.number = (TextView) convertView
                        .findViewById(R.id.number);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ContactBean cb = list.get(position);
            String name = cb.getDisplayName();
            String number = cb.getPhoneNum();
            holder.name.setText(name);
            holder.number.setText(number);
            // sortKey
            String currentStr = getAlpha(cb.getSortKey());
            // sortKey
            String previewStr = (position - 1) >= 0 ? getAlpha(list.get(
                    position - 1).getSortKey()) : " ";

            if (!previewStr.equals(currentStr)) {
                holder.alpha.setVisibility(View.VISIBLE);
                holder.alpha.setText(currentStr);
            } else {
                holder.alpha.setVisibility(View.GONE);
            }
            return convertView;
        }

        private class ViewHolder {
            TextView alpha;
            TextView name;
            TextView number;
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

}
