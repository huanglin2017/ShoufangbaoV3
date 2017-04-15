package com.kupangstudio.shoufangbao;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Custom;
import com.kupangstudio.shoufangbao.model.CustomFollow;
import com.kupangstudio.shoufangbao.model.FollowList;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.utils.StringUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * Created by Jsmi on 2015/11/26.
 * 来电悬浮窗
 */
public class PhoneReceiver extends BroadcastReceiver{
    private Context context;
    private WindowManager.LayoutParams wmParams;
    private WindowManager mWindowManager;// 创建悬浮窗口设置布局参数的对象
    View v;
    TextView phone_name;
    TextView phone_number;
    TextView phone_intent;
    TextView phone_statue;
    TextView phone_time;
    TextView phone_beizhu;
    ImageView phone_close;
    ImageView phone_line;
    User user;
    private float mTouchStartX;
    private float mTouchStartY;
    private float x;
    private float y;
    TelephonyManager phoneManager;
    private static boolean flag;
    private TextView name1;
    private TextView name2;
    private TextView name3;
    private TextView phone1;
    private TextView phone2;
    private TextView phone3;
    private RelativeLayout close1;
    private RelativeLayout close2;
    private RelativeLayout close3;

    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context = context;
        user = User.getInstance();
        if (user == null || user.userType == User.TYPE_DEFAULT_USER) {
            return;
        }


        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            // 监听拨打电话
            final String phoneNumber = intent
                    .getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            List<Custom> list = DataSupport.where("uid = ?", String.valueOf(user.uid)).find(Custom.class);
            for (int i = 0; i < list.size(); i++) {
                if (phoneNumber.contains(list.get(i).getTel())
                        || list.get(i).getTel().contains(phoneNumber)) {
                    final Custom c = list.get(i);
                    popPhone(phoneNumber);
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            popPhoneRemove();
                        }
                    }, 5000);

                }
            }
        } else {
            // 监听接听电话
            phoneManager = (TelephonyManager) context
                    .getSystemService(Service.TELEPHONY_SERVICE);
            phoneManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        }
    }

    /**
     * 监听手机来电状态
     */
    PhoneStateListener listener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, final String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:// 电话挂断状态
                    popPhoneRemove();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:// 电话接听状态
                    popPhoneRemove();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:// 电话铃响状态
                    if (!flag && !TextUtils.isEmpty(incomingNumber)) {
                        flag = true;
                        telRinging(incomingNumber);// 打开来电悬浮窗界面,传递来电号码
                    }
                    break;
            }
        }

    };

    /**
     * 打开来电悬浮窗界面
     *
     * @param number
     */
    private void telRinging(final String number) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                popPhone(number);
            }
        }, 2000);
    }

    private void updateViewPosition() {
        // 更新浮动窗口位置参数
        wmParams.x = (int) (x - mTouchStartX);
        wmParams.y = (int) (y - mTouchStartY);
        mWindowManager.updateViewLayout(v, wmParams);

    }

    /**
     * 悬浮窗的具体实现
     *
     * @param phone
     */
    private void popPhone(String phone) {
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;// 定义WindowManager.LayoutParams类型,TYPE_SYSTEM_ERROR为系统内部错误提示，显示于所有内容之上
        mWindowManager = (WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);// 系统服务，注意这里必须加getApplicationContext(),否则无法把悬浮窗显示在最上层
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; // 不能抢占聚焦点
        wmParams.flags = wmParams.flags
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        wmParams.flags = wmParams.flags
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; // 排版不受限制
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;//
        wmParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.x = 0;
        wmParams.y = (int) (5 * CommonUtils.getDensity(context) + 0.5f);
        boolean isShow = (boolean)SPUtils.get(context, Constants.WIDOW_FLOATING, true);

        List<Custom> list = DataSupport.where("uid = ?", String.valueOf(user.uid)).find(Custom.class);

        for (int i = 0; i < list.size(); i++) {
            if (phone.contains(list.get(i).getTel())
                    || list.get(i).getTel().contains(phone) && isShow) {
                final Custom c = list.get(i);
                addCustomFollow(context, c);
                if (v != null) {
                    return;
                }
                v = LayoutInflater.from(context).inflate(R.layout.pop_phone1, null, false);
                setViewContent1(c, v);
//				setViewContent(c, v);
                mWindowManager.addView(v, wmParams); // 创建View
                close1.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        popPhoneRemove();
                    }
                });
                close2.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        popPhoneRemove();
                    }
                });
                close3.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        popPhoneRemove();
                    }
                });
                v.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        x = event.getRawX();
                        y = event.getRawY();
                        if (!flag) {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    mTouchStartX = event.getX();
                                    mTouchStartY = event.getY();
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    updateViewPosition();
                                    break;
                                case MotionEvent.ACTION_UP:
                                    updateViewPosition();
                                    mTouchStartX = mTouchStartY = 0;
                                    break;
                            }
                        }
                        return true;
                    }
                });

                break;
            }
        }
    }

    private void addCustomFollow(final Context ctx, final Custom cus) {

        long time = System.currentTimeMillis() / 1000;
        FollowList followList = new FollowList();
        followList.setCtime(time);
        cus.setFollow(followList);
        cus.updateAll("cid = ? and uid = ?", String.valueOf(cus.getCid()), String.valueOf(User.getInstance().uid));

        cus.setDATEACTION(Custom.ACTION_UPDATE);
        EventBus.getDefault().post(cus);
        final CustomFollow customfollow = new CustomFollow();
        customfollow.setCid(cus.getCid());
        customfollow.setContent("电话沟通");
        customfollow.setCtime(time);
        customfollow.setType(Constants.TYPE_AUTO);

        getAddCustomFollowData(cus, "电话沟通", time);
    }

    private class MyAdapter extends BaseAdapter {
        private Context context;
        List<CustomFollow> list;

        private MyAdapter(List<CustomFollow> list, Context context) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                holder = new ViewHolder();
                view = LayoutInflater.from(context).inflate(R.layout.listview_item, viewGroup, false);
                holder.time = (TextView) view.findViewById(R.id.time);
                holder.content = (TextView) view.findViewById(R.id.content_genjin);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            CustomFollow customFollow= list.get(i);
            holder.time.setText("11111");
            holder.content.setText("1111111111");
            return view;
        }

        class ViewHolder {
            private TextView time;
            private TextView content;
        }
    }

    private void setViewContent1(Custom custom, View v) {
        //查询跟进数据
        List<CustomFollow> list = DataSupport.where("cid = ?", String.valueOf(custom.getCid())).find(CustomFollow.class);

        name1 = (TextView) v.findViewById(R.id.name1);
        name2 = (TextView) v.findViewById(R.id.name2);
        name3 = (TextView) v.findViewById(R.id.name3);
        phone1 = (TextView) v.findViewById(R.id.phone1);
        phone2 = (TextView) v.findViewById(R.id.phone2);
        phone3 = (TextView) v.findViewById(R.id.phone3);
        close1 = (RelativeLayout) v.findViewById(R.id.close1);
        close2 = (RelativeLayout) v.findViewById(R.id.close2);
        close3 = (RelativeLayout) v.findViewById(R.id.close3);
        //布局选择
        RelativeLayout popPhoneSelect1 = (RelativeLayout)v.findViewById(R.id.popphone_follow_1);
        RelativeLayout popPhoneSelect2 = (RelativeLayout)v.findViewById(R.id.popphone_follow_2);
        RelativeLayout popPhoneSelect3 = (RelativeLayout)v.findViewById(R.id.popphone_follow_3);

        //第一种
        TextView popPhoneFollowtime11 = (TextView)v.findViewById(R.id.pop_phone_follow_1_1_time);
        TextView popPhoneFollowmsg11 = (TextView)v.findViewById(R.id.pop_phone_follow_1_1_message);

        TextView popPhoneFollowtime12 = (TextView)v.findViewById(R.id.pop_phone_follow_1_2_time);
        TextView popPhoneFollowmsg12 = (TextView)v.findViewById(R.id.pop_phone_follow_1_2_message);

        TextView popPhoneFollowtime13 = (TextView)v.findViewById(R.id.pop_phone_follow_1_3_time);
        TextView popPhoneFollowmsg13 = (TextView)v.findViewById(R.id.pop_phone_follow_1_3_message);

        //第二种
        TextView popPhoneFollowtime21 = (TextView)v.findViewById(R.id.pop_phone_follow_2_1_time);
        TextView popPhoneFollowmsg21 = (TextView)v.findViewById(R.id.pop_phone_follow_2_1_message);

        TextView popPhoneFollowtime22 = (TextView)v.findViewById(R.id.pop_phone_follow_2_2_time);
        TextView popPhoneFollowmsg22 = (TextView)v.findViewById(R.id.pop_phone_follow_2_2_message);

        //第三种
        TextView popPhoneFollowtime31 = (TextView)v.findViewById(R.id.pop_phone_follow_3_1_time);
        TextView popPhoneFollowmsg31 = (TextView)v.findViewById(R.id.pop_phone_follow_3_1_message);

        name1.setText(custom.getName());
        name2.setText(custom.getName());
        name3.setText(custom.getName());
        phone1.setText(custom.getTel());
        phone2.setText(custom.getTel());
        phone3.setText(custom.getTel());

        CustomFollow customFollow = new CustomFollow();

        switch (list.size()){
            case 0:
                popPhoneSelect3.setVisibility(View.VISIBLE);
                popPhoneFollowtime31.setText("00/00");
                popPhoneFollowmsg31.setText("暂无跟进信息");
                break;
            case 1:
                popPhoneSelect3.setVisibility(View.VISIBLE);
                customFollow = list.get(0);
                popPhoneFollowtime31.setText(TimeUtils.getPhoneReceiverData(customFollow.getCtime() * 1000));
                popPhoneFollowmsg31.setText(customFollow.getContent());
                break;
            case 2:
                popPhoneSelect2.setVisibility(View.VISIBLE);
                customFollow = list.get(0);
                popPhoneFollowtime21.setText(TimeUtils.getPhoneReceiverData(customFollow.getCtime() * 1000));
                popPhoneFollowmsg21.setText(customFollow.getContent());
                customFollow = list.get(1);
                popPhoneFollowtime22.setText(TimeUtils.getPhoneReceiverData(customFollow.getCtime() * 1000));
                popPhoneFollowmsg22.setText(customFollow.getContent());
                break;
            default:
                popPhoneSelect1.setVisibility(View.VISIBLE);
                customFollow = list.get(0);
                popPhoneFollowtime11.setText(TimeUtils.getPhoneReceiverData(customFollow.getCtime() * 1000));
                popPhoneFollowmsg11.setText(customFollow.getContent());
                customFollow = list.get(1);
                popPhoneFollowtime12.setText(TimeUtils.getPhoneReceiverData(customFollow.getCtime() * 1000));
                popPhoneFollowmsg12.setText(customFollow.getContent());
                customFollow = list.get(2);
                popPhoneFollowtime13.setText(TimeUtils.getPhoneReceiverData(customFollow.getCtime() * 1000));
                popPhoneFollowmsg13.setText(customFollow.getContent());
                break;
        }
    }

    private void setViewContent(Custom custom, View v) {
        phone_beizhu = (TextView) v.findViewById(R.id.phone_beizhu);
        phone_intent = (TextView) v.findViewById(R.id.phone_intent);
        phone_name = (TextView) v.findViewById(R.id.phone_name);
        phone_number = (TextView) v.findViewById(R.id.phone_number);
        phone_statue = (TextView) v.findViewById(R.id.phone_state);
        phone_time = (TextView) v.findViewById(R.id.phone_time);
        phone_close = (ImageView) v.findViewById(R.id.phone_close);
        phone_line = (ImageView) v.findViewById(R.id.phone_line);
        phone_name.setText(custom.getName());
        phone_number.setText(custom.getTel());
        phone_intent.setText(Constants.WILLITEMS[custom.getWill()]);
        if (StringUtils.isEmpty(custom.getRemark())) {
            phone_beizhu.setVisibility(View.GONE);
            phone_line.setVisibility(View.GONE);
        } else {
            phone_beizhu.setText(custom.getRemark());
        }

    }

    /**
     * 移除悬浮窗
     */
    private void popPhoneRemove() {

        if (v != null) {
            mWindowManager.removeView(v);
        }
        v = null;// 必须加入此语句，否则会windowManager会找不到view
        flag = false;
    }


    private void getAddCustomFollowData(final Custom cus, final String content, final Long time) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "addFollow");
        map.put("module", Constants.MODULE_CUSTOM);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("content", content);
        map.put("type", String.valueOf(Constants.TYPE_AUTO));
        map.put("id", User.getInstance().salt);
        map.put("cid", String.valueOf(cus.getCid()));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(context, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                JSONObject object = new JSONObject(jsonObject.getJSONObject("content").toString());
                                //存跟进到数据库
                                CustomFollow follow = new CustomFollow();
                                follow.setCid(cus.getCid());
                                follow.setType(Constants.TYPE_AUTO);
                                follow.setCtime(time);
                                follow.setContent(content);
                                follow.setFid(object.getInt("fid"));
                                follow.save();

                                //更新客户信息
                                FollowList followList = new FollowList();
                                followList.setCtime(time);
                                followList.setCid(cus.getCid());
                                cus.setFollow(followList);
                                cus.updateAll("cid = ?", String.valueOf(cus.getCid()));
                                cus.setDATEACTION(Constants.CUSTOM_UPDATA);
                                EventBus.getDefault().post(cus);
                            }
                        } catch (JSONException e) {
                            return;
                        }
                    }
                });
    }
}
