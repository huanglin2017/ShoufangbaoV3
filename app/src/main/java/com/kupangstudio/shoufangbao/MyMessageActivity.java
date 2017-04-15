package com.kupangstudio.shoufangbao;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.carrier.CarrierMessagingService;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Message;
import com.kupangstudio.shoufangbao.model.Report;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.OkHttpRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by long1 on 15/11/24.
 * Copyright 15/11/24 android_xiaobai.
 * 我的消息
 */
public class MyMessageActivity extends BaseActivity {

    private LinearLayout deleteLayout;
    private PullToRefreshListView refreshListView;
    private ListView listView;
    private Button btnCancel;
    private Button btnDelete;
    private RelativeLayout emptyView;
    private TextView emptyText;
    private ImageView emptyImage;
    private Button emptyButton;
    private List<Message> messageList;
    private List<Message> deleteList = new ArrayList<>();
    private boolean isFirst = true;
    private StringBuffer mid;
    private MessageAdapter adapter;
    private boolean isDelete;
    private TextView tvRight;
    private boolean isDeleting;
    private int currentMid;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_message);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightText(this, "我的消息", "编辑", mClickListener);
        CommonUtils.setTaskDone(MyMessageActivity.this, 40);
        initView();
        init();
        setClickListener();
        getMessage();
    }

    private void initView() {
        deleteLayout = (LinearLayout) findViewById(R.id.delete_layout);
        refreshListView = (PullToRefreshListView) findViewById(R.id.my_message_list);
        listView = refreshListView.getRefreshableView();
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnDelete = (Button) findViewById(R.id.btn_ok);
        emptyView = (RelativeLayout) findViewById(R.id.emptyview);
        emptyText = (TextView) findViewById(R.id.emptyview_text);
        emptyImage = (ImageView) findViewById(R.id.emptyview_img);
        emptyButton = (Button) findViewById(R.id.emptyview_btn);
        tvRight = (TextView) findViewById(R.id.navbar_text_right);
    }

    private void init() {
        emptyText.setText("暂无消息,邮差小宝奔走中！");
        emptyButton.setVisibility(View.GONE);
    }

    private void setClickListener() {
        btnCancel.setOnClickListener(mClickListener);
        btnDelete.setOnClickListener(mClickListener);
        refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                isFirst = false;
                getMessage();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message message = messageList.get(position - 1);
                if (isDelete) {
                    MessageAdapter.ViewHolder holder = (MessageAdapter.ViewHolder) view.getTag();
                    holder.cb.toggle();
                    adapter.isSelected.put(position, holder.cb.isChecked());
                    if (holder.cb.isChecked()) {
                        deleteList.add(message);
                    } else {
                        deleteList.remove(message);
                    }
                } else {
                    currentMid = message.getMid();
                    if (message.getStatus() == Message.STATE_UNREAD) {
                        message.setStatus(Message.STATE_READ);
                        readMessage();
                        User user = User.getInstance();
                        user.message = user.message - 1;
                        User.saveUser(user, MyMessageActivity.this);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("status", String.valueOf(Message.STATE_READ));
                        DataSupport.updateAll(Message.class, contentValues, "mid = ? and uid = ?", String.valueOf(currentMid), String.valueOf(User.getInstance().uid));
                    }
                    TextView message_content = (TextView) view.findViewById(R.id.message_content);
                    TextView message_time = (TextView) view.findViewById(R.id.message_time);
                    ImageView message_enter = (ImageView) view.findViewById(R.id.message_enter_img);
                    message_content.setTextColor(getResources().getColor(R.color.content_text));
                    message_time.setTextColor(getResources().getColor(R.color.content_text));
                    message_enter.setImageResource(R.drawable.common_rawright_black);
                    adapter.notifyDataSetChanged();
                    if (message.getType() == Message.TYPE_BATCH) {
                        Intent intent = new Intent(MyMessageActivity.this, MyMessageActivity.class);
                        startActivity(intent);
                    }
                    if (message.getType() == Message.ACTION_WEB) {
                        Intent it = new Intent();
                        it.setClassName(MyMessageActivity.this, MessageWebDetailActivity.class.getName());
                        it.putExtra("isFromNotify", false);
                        it.putExtra("msgobject", message);
                        startActivity(it);
                    } else {
                        Intent it = new Intent();
                        it.setClassName(MyMessageActivity.this, MessageTextDetailActivity.class.getName());
                        it.putExtra("isFromNotify", false);
                        it.putExtra("msgobject", message);
                        startActivity(it);
                    }
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Message message = messageList.get(position - 1);
                final AppDialog.Builder builder = new AppDialog.Builder(MyMessageActivity.this, AppDialog.Builder.COMMONDIALOG);
                View dialogView = LayoutInflater.from(MyMessageActivity.this).inflate(R.layout.common_dialog_custom, null);
                builder.setContentView(dialogView);
                builder.setTitle("确定删除");
                builder.setPositiveButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mid = new StringBuffer();
                        deleteList.add(message);
                        mid.append(String.valueOf(message.getMid()));
                        deleteMessage();
                        builder.dismiss();
                    }
                });
                builder.setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.dismiss();
                    }
                });
                builder.create();
                return true;
            }
        });
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.navbar_text_right:
                    if (isDelete) {
                        for (int i = 0; i < messageList.size(); i++) {
                            adapter.isSelected.put(i, true);
                            deleteList.add(messageList.get(i));
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        tvRight.setText("全选");
                        deleteLayout.setVisibility(View.VISIBLE);
                        isDelete = true;
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case R.id.btn_cancel:
                    boolean isBack = true;
                    for (int j = 0; j < adapter.isSelected.size(); j++) {
                        if (adapter.isSelected.get(j)) {
                            isBack = false;
                        }
                    }
                    if (isBack) {
                        isDelete = false;
                        tvRight.setText("编辑");
                        deleteLayout.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    } else {
                        for (int i = 0; i < messageList.size(); i++) {
                            adapter.isSelected.put(i, false);
                        }
                        deleteList.clear();
                        tvRight.setText("全选");
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case R.id.btn_ok:
                    if (isDeleting) {
                        Toast.makeText(MyMessageActivity.this, "正在删除中...", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mid = new StringBuffer();
                    if (deleteList == null || deleteList.size() < 1) {
                        Toast.makeText(MyMessageActivity.this, "请选择删除条目!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (int i = 0; i < deleteList.size(); i++) {
                        Message msg = deleteList.get(i);
                        mid.append(msg.getMid() + "");
                        if (i != deleteList.size() - 1) {
                            mid.append(",");
                        }
                    }
                    deleteMessage();
                    break;
                default:
                    break;
            }
        }
    };

    private void readMessage() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "readMessage");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("mid", String.valueOf(currentMid));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {

                    }
                });
    }

    private void deleteMessage() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        final User user = User.getInstance();
        map.put("action", "delMessage");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("mid", mid.toString());
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    private ProgressDialog pb;

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        isDeleting = true;
                        pb = new ProgressDialog(MyMessageActivity.this);
                        CommonUtils.progressDialogShow(pb, "删除中...");
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(MyMessageActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        mid = null;
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if (code > Result.RESULT_OK) {
                                deleteLayout.setVisibility(View.GONE);
                                for (int i = 0; i < deleteList.size(); i++) {
                                    Message message = deleteList.get(i);
                                    DataSupport.deleteAll(Message.class, "mid = ? and uid = ?", String.valueOf(message.getMid()), String.valueOf(user.uid));
                                    for (int j = 0; j < messageList.size(); j++) {
                                        if (messageList.get(j).getMid() == message.getMid()) {
                                            messageList.remove(j);
                                        }
                                    }
                                }
                                deleteList = new ArrayList<Message>();
                                tvRight.setText("编辑");
                                adapter.initSelect();
                                isDelete = false;
                                setAdapter();
                            }
                            Toast.makeText(MyMessageActivity.this, notice, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(MyMessageActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        isDeleting = false;
                        CommonUtils.progressDialogDismiss(pb);
                    }
                });
    }

    class MessageAdapter extends BaseAdapter {

        LayoutInflater inflater;
        List<Message> list;
        private HashMap<Integer, Boolean> isSelected;

        public MessageAdapter(Context context, List<Message> list) {
            inflater = LayoutInflater.from(context);
            this.list = list;
            initSelect();
        }

        public void initSelect() {
            isSelected = new HashMap<Integer, Boolean>();
            for (int i = 0; i < messageList.size(); i++) {
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            Message message = list.get(position);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_my_message, parent, false);
                holder.message_content = (TextView) convertView.findViewById(R.id.message_content);
                holder.message_time = (TextView) convertView.findViewById(R.id.message_time);
                holder.message_enter = (ImageView) convertView.findViewById(R.id.message_enter_img);
                holder.cb = (CheckBox) convertView.findViewById(R.id.checkbox);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (isDelete) {
                holder.cb.setVisibility(View.VISIBLE);
                holder.message_enter.setVisibility(View.GONE);
            } else {
                holder.cb.setVisibility(View.GONE);
                holder.message_enter.setVisibility(View.VISIBLE);
            }
            holder.cb.setChecked(isSelected.get(position));
            if (message.getStatus() == Message.STATE_READ) {
                holder.message_content.setText(message.getTitle());
                holder.message_time.setText(TimeUtils.getCustomFollowData(message.getCtime() * 1000));
                holder.message_enter.setImageResource(R.drawable.common_rawright_black);
                holder.message_content.setTextColor(getResources().getColor(R.color.small_title));
                holder.message_time.setTextColor(getResources().getColor(R.color.small_title));
            } else {
                holder.message_content.setText(message.getTitle());
                holder.message_time.setText(TimeUtils.getCustomFollowData(message.getCtime() * 1000));
                holder.message_enter.setImageResource(R.drawable.common_rawright_red);
                holder.message_content.setTextColor(getResources().getColor(R.color.common_red));
                holder.message_time.setTextColor(getResources().getColor(R.color.common_red));
            }
            return convertView;
        }

        private class ViewHolder {
            public TextView message_content;
            public TextView message_time;
            public ImageView message_enter;
            public CheckBox cb;
        }
    }


    private void getMessage() {
        dialog = new ProgressDialog(MyMessageActivity.this);
        dialog.setMessage("请求中");
        dialog.show();
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getMessage");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new MessageCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(MyMessageActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        messageList = DataSupport.findAll(Message.class);
                        setAdapter();
                    }

                    @Override
                    public void onResponse(Result<List<Message>> response) {
                        if (response == null) {
                            Toast.makeText(MyMessageActivity.this, ResultError.MESSAGE_ERROR
                            ,Toast.LENGTH_SHORT).show();
                            messageList = DataSupport.findAll(Message.class);
                            setAdapter();
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            emptyView.setVisibility(View.GONE);
                            DataSupport.deleteAll(Message.class);
                            messageList = response.getContent();
                            for (int i = 0; i < messageList.size(); i++) {
                                messageList.get(i).setUid(User.getInstance().uid);
                            }
                            DataSupport.saveAll(messageList);
                        } else {
                            Toast.makeText(MyMessageActivity.this, response.getNotice(), Toast.LENGTH_SHORT).show();
                            if (response.getCode() == 1098) {
                                messageList = new ArrayList<>();
                            } else {
                                messageList = DataSupport.findAll(Message.class);
                            }
                        }
                        setAdapter();
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        if (!isFirst) {
                            refreshListView.onRefreshComplete();
                        }
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                });
    }

    private abstract class MessageCallback extends Callback<Result<List<Message>>> {
        @Override
        public Result<List<Message>> parseNetworkResponse(Response response) throws Exception {
            Result<List<Message>> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<List<Message>>>(){}.getType());
            }catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    private void setAdapter() {
        if (messageList == null || messageList.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
        }
        if (adapter == null) {
            adapter = new MessageAdapter(MyMessageActivity.this, messageList);
            listView.setAdapter(adapter);
        } else {
            adapter.list = messageList;
            adapter.notifyDataSetChanged();
        }
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
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        CommonUtils.removeActivity(this);
    }
}
