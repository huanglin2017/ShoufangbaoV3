package com.kupangstudio.shoufangbao;

import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.service.carrier.CarrierMessagingService;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.ContactBean;
import com.kupangstudio.shoufangbao.model.Custom;
import com.kupangstudio.shoufangbao.model.FollowList;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.kupangstudio.shoufangbao.widget.SideBar;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.OkHttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * Created by Jsmi on 2015/11/9.
 * 批量添加
 */
public class MultiAddActivity extends BaseActivity implements SectionIndexer, OnClickListener {

    private ListView mListView;
    private AsyncQueryHandler asyncQuery;
    private SideBar sideBar;
    private ContactHomeAdapter adapter;
    private ArrayList<ContactBean> list;
    private ArrayList<ContactBean> multiList;
    private Map<Integer, ContactBean> contactIdMap = null;
    private Button btnAll, btnCancel;
    private boolean isCancel = false;
    private TextView title;
    private TextView right;
    private ImageView left;
    private TextView dialog;
    private long beginTime;
    private long endTime;
    private View footer;
    private ArrayList<Custom> addlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_add);
        CommonUtils.addActivity(this);
        initView();
        initData();
        setClickListener();

        //字母监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                if (list != null) {
                    int position = getPositionForSection(s.charAt(0));
                    if (position != -1) {
                        mListView.setSelection(position);
                    }
                }
            }
        });

        //列表点击
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (list.size() == position) {
                    return;
                }
                ViewHolder holder = (ViewHolder) view.getTag();
                holder.cb.toggle();
                adapter.isSelected.put(position, holder.cb.isChecked());
                if (holder.cb.isChecked()) {
                    multiList.add(list.get(position));
                } else {
                    multiList.remove(list.get(position));
                }

            }
        });
    }

    private void initData() {
        beginTime = System.currentTimeMillis();
        list = new ArrayList<ContactBean>();
        footer = LayoutInflater.from(this).inflate(R.layout.common_footer, null);
        title.setText("批量添加");
        multiList = new ArrayList<ContactBean>();
        sideBar.setAlphaColor(this.getResources().getColor(R.color.common_title_bar));
        sideBar.setTextView(dialog, this);

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
        String[] projection = {ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY};
        // 查询的列
        asyncQuery = new MyAsyncQueryHandler(this.getContentResolver());
        asyncQuery.startQuery(0, null, uri, projection, null, null,
                "sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
        mListView.addFooterView(footer);
    }

    private void initView() {
        left = (ImageView) findViewById(R.id.multi_add_left);
        title = (TextView) findViewById(R.id.multi_add_title);
        right = (TextView) findViewById(R.id.multi_add_right);
        btnAll = (Button) findViewById(R.id.selectAll);
        btnCancel = (Button) findViewById(R.id.selectCancel);
        mListView = (ListView) findViewById(R.id.multichose_listview);
        sideBar = (SideBar) findViewById(R.id.multi_scroller);
        dialog = (TextView) findViewById(R.id.multi_dialog);
    }

    private void setClickListener() {
        left.setOnClickListener(this);
        right.setOnClickListener(this);
        btnAll.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.multi_add_left://返回，回退
                finish();
                break;
            case R.id.multi_add_right://确定
                if (multiList.size() <= 0) {
                    Toast.makeText(MultiAddActivity.this, "请先选择客户",
                            Toast.LENGTH_SHORT).show();
                } else {
                    User u = User.getInstance();
                    if (u.userType == User.TYPE_DEFAULT_USER) {

                        handleDefaultUserTask();
                        return;
                    }
                    getData();
                    HashMap<String, String> map = new HashMap<String, String>();
                    MobclickAgent.onEvent(MultiAddActivity.this, "multiaddcustomclick", map);

                }

                break;
            case R.id.selectAll://全选
                if (list.size() == 0) {
                    return;
                }
                for (int i = 0; i < list.size(); i++) {
                    adapter.isSelected.put(i, true);
                    multiList.add(list.get(i));
                }
                adapter.notifyDataSetChanged();
                break;
            case R.id.selectCancel://取消
                if (list.size() == 0) {
                    return;
                }
                for (int i = 0; i < list.size(); i++) {
                    if (adapter.isSelected.get(i)) {
                        adapter.isSelected.put(i, false);
                        multiList.remove(list.get(i));
                    }
                }
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        endTime = System.currentTimeMillis();
        int duration = (int) ((endTime - beginTime) / 1000);
        Map<String, String> map_value = new HashMap<String, String>();
        MobclickAgent.onEventValue(this, "multiaddcustom", map_value, duration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
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
                list.clear();
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    String name = cursor.getString(1);
                    String number = cursor.getString(2);
                    String sortKey = getAlpha(cursor.getString(3));
                    int contactId = cursor.getInt(4);
                    Long photoId = cursor.getLong(5);
                    String lookUpKey = cursor.getString(6);

                    if (contactIdMap.containsKey(contactId)) {

                    } else {
                        ContactBean cb = new ContactBean();
                        cb.setDisplayName(name);
                        if (number.startsWith("+86")) {
                            number = number.substring(3, number.length());
                        }
                        number = number.trim();
                        number = number.replace("　", "");
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
        adapter = new ContactHomeAdapter(MultiAddActivity.this, list);
        mListView.setAdapter(adapter);
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


    public class ContactHomeAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<ContactBean> list;
        private HashMap<Integer, Boolean> isSelected;

        public ContactHomeAdapter(Context context, List<ContactBean> list) {

            this.inflater = LayoutInflater.from(context);
            this.list = list;
            initData();
        }

        private void initData() {
            isSelected = new HashMap<Integer, Boolean>();
            for (int i = 0; i < list.size(); i++) {
                isSelected.put(i, false);
            }

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
                convertView = inflater.inflate(R.layout.item_mutil_addcustom, null);
                holder = new ViewHolder();
                holder.alpha = (TextView) convertView
                        .findViewById(R.id.multialpha);
                holder.name = (TextView) convertView
                        .findViewById(R.id.multiname);
                holder.cb = (CheckBox) convertView
                        .findViewById(R.id.multicheck);
                holder.number = (TextView) convertView
                        .findViewById(R.id.multinumber);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ContactBean cb = list.get(position);
            String name = cb.getDisplayName();
            String number = cb.getPhoneNum();
            holder.name.setText(name);
            holder.number.setText(number);
            holder.cb.setChecked(isSelected.get(position));
            String firstLetter = String.valueOf(cb.getSortKey().toUpperCase().charAt(0));
            if (position == 0) {
                holder.alpha.setVisibility(View.VISIBLE);
                holder.alpha.setText(firstLetter);
            } else {
                String firstLetterPre = String.valueOf(list.get(position - 1).getSortKey()
                        .toUpperCase().charAt(0));
                if (firstLetter.equals(firstLetterPre)) {
                    holder.alpha.setVisibility(View.GONE);
                } else {
                    holder.alpha.setVisibility(View.VISIBLE);
                    holder.alpha.setText(firstLetter);
                }
            }
            return convertView;
        }

    }

    private class ViewHolder {
        TextView alpha;
        TextView name;
        TextView number;
        CheckBox cb;
    }

    private void getData() {
        final ProgressDialog pDialog = new ProgressDialog(MultiAddActivity.this);
        pDialog.setTitle("请稍等");
        pDialog.setMessage("正在添加客户,请您耐心等待...");
        pDialog.show();
        // 组织数据
        final JSONArray jsonarray = new JSONArray();
        JSONObject cus = null;
        for (int i = 0; i < multiList.size(); i++) {
            cus = new JSONObject();
            try {
                cus.put("tel", multiList.get(i).getPhoneNum());
                cus.put("name", multiList.get(i).getDisplayName());
                jsonarray.put(cus);
            } catch (JSONException e) {
                Toast.makeText(MultiAddActivity.this, "数据格式错误", Toast.LENGTH_SHORT).show();
            }
        }
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "addCustomers");
        map.put("module", Constants.MODULE_CUSTOM);
        map.put("content", jsonarray.toString());
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        if (pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        Toast.makeText(MultiAddActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        if (pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if(code > Result.RESULT_OK) {
                                String result = jsonObject.getJSONObject("content").toString();
                                parseMutilAddCustom(result);
                                EventBus.getDefault().post(addlist);
                            } else {
                                Toast.makeText(MultiAddActivity.this, notice,
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(MultiAddActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    private void parseMutilAddCustom(String result) {
        HashMap<String, ArrayList<Custom>> map = new HashMap<String, ArrayList<Custom>>();
        addlist = new ArrayList<Custom>();
        List<HashMap<String, String>> repeatlist = new ArrayList<HashMap<String, String>>();
        List<HashMap<String, String>> faillist = new ArrayList<HashMap<String, String>>();
        try {
            JSONObject cobj = new JSONObject(result);
            long ctime = 0;
            if (!cobj.isNull("ctime")) {
                ctime = cobj.getLong("ctime");
            }
            JSONArray addArray = cobj.getJSONArray("success");
            JSONArray repeatArray = cobj.getJSONArray("repeat");
            JSONArray failArray = cobj.getJSONArray("fail");
            User user = User.getInstance();
            if (addArray.length() > 0) {
                for (int i = 0; i < addArray.length(); i++) {
                    JSONObject object = addArray.getJSONObject(i);
                    Custom c = new Custom();
                    FollowList followList = new FollowList();
                    followList.setCtime(System.currentTimeMillis() / 1000);
                    c.setName(object.getString("name"));
                    c.setTel(object.getString("tel"));
                    c.setCid(object.getInt("cid"));
                    c.setRemark("");
                    c.setGender(Constants.GENDER_MAN);
                    c.setCtime(ctime);
                    c.setUid(user.uid);
                    c.setLayout(0);
                    c.setSize(0);
                    c.setPrice(0);
                    c.setType(0);
                    c.setWill(0);
                    c.setCity((String) SPUtils.get(MultiAddActivity.this, Constants.BUILD_CITY_NAME, "北京"));
                    c.setArea("");
                    c.setDistrict("");
                    c.setCityid((int) SPUtils.get(MultiAddActivity.this, Constants.BUILD_CITY_ID, 1));
                    c.setAreaid(0);
                    c.setDistrictid(0);
                    c.setStatus(0);
                    c.setFollow(followList);
                    addlist.add(c);
                }
                map.put("success", addlist);
            }
            if (repeatArray.length() > 0) {
                for (int j = 0; j < repeatArray.length(); j++) {
                    JSONObject object = repeatArray.getJSONObject(j);
                    HashMap<String, String> map1 = new HashMap<String, String>();
                    map1.put("content", object.getString("name"));
                    repeatlist.add(map1);
                }
            }
            if (failArray.length() > 0) {
                for (int k = 0; k < failArray.length(); k++) {
                    JSONObject object = failArray.getJSONObject(k);
                    HashMap<String, String> map2 = new HashMap<String, String>();
                    map2.put("content", object.getString("name"));
                    faillist.add(map2);
                }
            }

            //展示添加的结果信息
            View dialog = LayoutInflater.from(MultiAddActivity.this).inflate(R.layout.common_dialog_multiadd, null);
            if (addlist.size() > 0) {
                final AppDialog.Builder builder1 = new AppDialog.Builder(MultiAddActivity.this, AppDialog.Builder.SINGLE_OK,
                        repeatlist, faillist);
                builder1.setContentView(dialog)
                        .setPositiveButton("OK", new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                builder1.dismiss();
                                MultiAddActivity.this.finish();
                            }
                        })
                        .setSuccessNum(addlist.size())
                        .create();
            } else {
                final AppDialog.Builder builder2 = new AppDialog.Builder(MultiAddActivity.this, AppDialog.Builder.SINGLE_OK,
                        repeatlist, faillist);
                builder2.setContentView(dialog)
                        .setPositiveButton("OK", new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                builder2.dismiss();
                                MultiAddActivity.this.finish();
                            }
                        })
                        .setSuccessGone()
                        .create();
            }
        } catch (JSONException e) {
            Toast.makeText(MultiAddActivity.this, "数据格式错误", Toast.LENGTH_SHORT).show();
        }
    }

    boolean flag = false;

    private void handleDefaultUserTask() {

        if (flag) {
            Toast.makeText(MultiAddActivity.this, "正在添加，请稍候", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread() {

            public void run() {
                flag = true;
                User u = User.getInstance();
                Calendar calendar = Calendar.getInstance();
                //组织数据
                final ArrayList<Custom> cuslist = new ArrayList<Custom>();
                int size = multiList.size();
                for (int i = 0; i < size; i++) {
                    Custom c = new Custom();
                    c.setName(multiList.get(i).getDisplayName());
                    c.setTel(multiList.get(i).getPhoneNum());
                    c.setCid(Constants.INVALIDE_ID);
                    c.setRemark("");
                    c.setGender(Constants.GENDER_MAN);
                    c.setUid(u.uid);
                    long time = new Date().getTime() / 1000;
                    c.setCtime(calendar.getTimeInMillis() / 1000);
                    c.setLayout(0);
                    c.setSize(0);
                    c.setPrice(0);
                    c.setType(0);
                    c.setWill(0);
                    c.setCity(u.city);
                    c.setArea(u.area);
                    c.setCityid(u.cityId);
                    c.setAreaid(u.areaId);
                    c.setDistrictid(u.districtId);
                    c.setDistrict(u.district);
                    cuslist.add(c);
                }
                DataSupport.saveAll(cuslist);
                flag = false;
                MultiAddActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        EventBus.getDefault().post(cuslist);
                        finish();
                    }
                });
            }
        }.start();
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
