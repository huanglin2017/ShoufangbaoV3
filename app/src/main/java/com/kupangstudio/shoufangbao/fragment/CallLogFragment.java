package com.kupangstudio.shoufangbao.fragment;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.base.BaseFragment;
import com.kupangstudio.shoufangbao.base.ShoufangbaoApplication;
import com.kupangstudio.shoufangbao.model.CallLogBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CallLogFragment extends Fragment {

    ShoufangbaoApplication application;
    ListView mListView;
    AsyncQueryHandler asyncQuery;
    HomeDialAdapter adapter;

    private List<CallLogBean> list;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater
                .inflate(R.layout.fragment_calllog, container, false);
        mListView  = (ListView) view.findViewById(R.id.fragment_callog_listview);


        mListView.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int pos,
                                    long arg3) {

                CallLogBean bean = list.get(pos);

                Intent it = new Intent();
                it.putExtra("name", bean.getName());
                it.putExtra("num", bean.getNumber());
                getActivity().setResult(Activity.RESULT_OK, it);
                getActivity().finish();
            }

        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        init();
    }


    private void init(){
        Uri uri = CallLog.Calls.CONTENT_URI;

        String[] projection = {
                CallLog.Calls.DATE,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls._ID
        };
        // 查询的列
        asyncQuery = new MyAsyncQueryHandler(getActivity().getContentResolver());
        asyncQuery.startQuery(0, null, uri, projection, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
    }



    private class MyAsyncQueryHandler extends AsyncQueryHandler {

        public MyAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0) {
                list = new ArrayList<CallLogBean>();
                SimpleDateFormat sfd = new SimpleDateFormat("yyyy/MM/dd hh:mm");
                Date date;
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    date = new Date(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
//					String date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                    String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                    int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                    String cachedName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));//缓存的名称与电话号码，如果它的存在
                    int id = cursor.getInt(cursor.getColumnIndex(CallLog.Calls._ID));

                    CallLogBean clb = new CallLogBean();
                    clb.setId(id);
                    clb.setNumber(number);
                    clb.setName(cachedName);
                    if(null == cachedName || "".equals(cachedName)){
                        clb.setName(number);
                    }
                    clb.setType(type);
                    clb.setDate(sfd.format(date));
                    list.add(clb);
                }
                if (list.size() > 0) {
					/*for(int i=0;i<list.size();i++){
						for(int j=list.size()-1;j>i;j--){
							if(list.get(j).getName().equals(list.get(i).getName())){
								list.remove(j);
							}
						}
					}*/
                    setAdapter(list);
                }
            }
        }

    }


    private void setAdapter(List<CallLogBean> list) {
        adapter = new HomeDialAdapter(getActivity(), list);
        mListView.setAdapter(adapter);
    }

    public class HomeDialAdapter extends BaseAdapter {

        private Context ctx;
        private List<CallLogBean> list;
        private LayoutInflater inflater;

        public HomeDialAdapter(Context context, List<CallLogBean> list) {

            this.ctx = context;
            this.list = list;
            this.inflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return list.size();
        }

        public Object getItem(int position) {
            return list.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_calllog_list, parent, false);
                holder = new ViewHolder();
                holder.call_type = (ImageView) convertView.findViewById(R.id.call_type);
                holder.name = (TextView) convertView.findViewById(R.id.call_name);
                holder.number = (TextView) convertView.findViewById(R.id.call_number);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            CallLogBean clb = list.get(position);
            switch (clb.getType()) {
                case 1:
                    holder.call_type.setBackgroundResource(R.drawable.ic_calllog_incomming_normal);
                    break;
                case 2:
                    holder.call_type.setBackgroundResource(R.drawable.ic_calllog_outgoing_nomal);
                    break;
                case 3:
                    holder.call_type.setBackgroundResource(R.drawable.ic_calllog_missed_normal);
                    break;
            }
            holder.name.setText(clb.getName());
            holder.number.setText(clb.getNumber());
            holder.time.setText(clb.getDate());

            return convertView;
        }

        private class ViewHolder {
            ImageView call_type;
            TextView name;
            TextView number;
            TextView time;
        }


    }

}
