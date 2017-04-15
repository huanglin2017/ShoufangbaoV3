package com.kupangstudio.shoufangbao.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kupangstudio.shoufangbao.AboutSfbActivity;
import com.kupangstudio.shoufangbao.AccountSettingActivity;
import com.kupangstudio.shoufangbao.AuthActivity;
import com.kupangstudio.shoufangbao.LoginActivity;
import com.kupangstudio.shoufangbao.MyCardActivity;
import com.kupangstudio.shoufangbao.MyCollectActivity;
import com.kupangstudio.shoufangbao.MyInviteActivity;
import com.kupangstudio.shoufangbao.MyMessageActivity;
import com.kupangstudio.shoufangbao.MyReportActivity;
import com.kupangstudio.shoufangbao.MyWalletActivity;
import com.kupangstudio.shoufangbao.PointMallActivity;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.SuggestActivity;
import com.kupangstudio.shoufangbao.base.BaseFragment;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.AppUtils;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.FileUpload;
import com.kupangstudio.shoufangbao.utils.FileUtils;
import com.kupangstudio.shoufangbao.utils.ImageUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.widget.MyListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.zbar.lib.CaptureActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.OkHttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;

/**
 * Created by long on 15/11/9.
 * Copyright 2015 android_xiaobai.
 * 个人中心
 */
public class PersonalCenterFragment extends BaseFragment {

    private static final int RESULTCODE_PHOTO = 101;
    private static final int RESULTCODE_CAPTURE = 102;
    private static final int RESULTCODE_CROP = 103;
    private AlertDialog dialog;
    private File sdcardTempFile;
    private int crop = 300;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private View fragmentView;
    private RelativeLayout unRegister;//未登录注册
    private TextView tvLoginReg;
    private RelativeLayout walletLayout;
    private RelativeLayout messageLayout;
    private RelativeLayout reportLayout;
    private RelativeLayout inviteLayout;
    private RelativeLayout unAuth;//登录未认证
    private CircleImageView headerUnAuth;
    private TextView tvGoAuth;
    private TextView unAuthName;
    private TextView unAuthPhone;
    private RelativeLayout authView;//已经认证
    private CircleImageView headerAuth;
    private TextView authName;
    private TextView authPhone;
    private MyListView lv;
    private int[] iconArray;
    private String[] itemArray;
    private ItemAdapter itemAdapter;
    private String inviteUrl;
    private String hotline;
    private ScrollView scrollView;
    private ProgressDialog upDialog;
    private TextView messageNum;
    /**
     * 标志位，标志已经初始化完成
     */
    private boolean isPrepared;
    /**
     * 是否已被加载过一次，第二次就不再去请求数据了
     */
    private boolean mHasLoadedOnce;
    private RelativeLayout loading;
    private boolean isPersonDone;
    private boolean isInfoDone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.center_head_default)
                .showImageForEmptyUri(R.drawable.center_head_default)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        iconArray = new int[]{R.drawable.item_mine_set, R.drawable.item_mine_wallet,
                R.drawable.item_mine_card, R.drawable.item_mine_invite,
                R.drawable.item_mine_hotline, R.drawable.item_mine_suggest,
                R.drawable.item_mine_about};
        itemArray = new String[]{"账号设置", "我的钱包", "我的卡券", "邀请好友", "客服热线",
                "意见反馈", "关于我们"};
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == fragmentView) {
            fragmentView = inflater.inflate(R.layout.fragment_personalcenter, container, false);
            CommonUtils.handleFragmentTitleBarRightImg(getActivity(), fragmentView, "个人中心", R.drawable.scan_enter, mClickListener);
            ImageView ivLeft = (ImageView) fragmentView.findViewById(R.id.navbar_image_left);
            ivLeft.setVisibility(View.GONE);
            initView(fragmentView);
            initUnReg(fragmentView);
            initUnAuth(fragmentView);
            initAuth(fragmentView);
            setClickListener();
            upDialog = new ProgressDialog(getActivity());
            upDialog.setMessage("头像上传中");
            isPrepared = true;
        }
        return fragmentView;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        sdcardTempFile = new File(Constants.IMAGE_PATH + File.separator,
                "header.jpg");
        if (!sdcardTempFile.exists()) {
            try {
                sdcardTempFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        List<com.kupangstudio.shoufangbao.model.Message> list = DataSupport.where("uid = ?", String.valueOf(User.getInstance().uid)).find(com.kupangstudio.shoufangbao.model.Message.class);
        int unRead = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getStatus() == com.kupangstudio.shoufangbao.model.Message.STATE_UNREAD) {
                unRead += 1;
            }
        }
        if (unRead > 0) {
            messageNum.setVisibility(View.VISIBLE);
            messageNum.setText(String.valueOf(unRead));
        } else {
            messageNum.setVisibility(View.GONE);
        }
    }

    private void initView(View view) {
        scrollView = (ScrollView) view.findViewById(R.id.fragment_center_scroll);
        lv = (MyListView) view.findViewById(R.id.fragment_mine_list);
        loading = (RelativeLayout) view.findViewById(R.id.loading_center);
    }

    /**
     * 未登录注册
     */
    private void initUnReg(View view) {
        unRegister = (RelativeLayout) view.findViewById(R.id.header_center_unReg);
        tvLoginReg = (TextView) view.findViewById(R.id.header_center_unreg_login);
        walletLayout = (RelativeLayout) view.findViewById(R.id.header_center_unreg_wallet);
        messageLayout = (RelativeLayout) view.findViewById(R.id.header_center_unreg_message);
        reportLayout = (RelativeLayout) view.findViewById(R.id.header_center_unreg_report);
        inviteLayout = (RelativeLayout) view.findViewById(R.id.header_center_unreg_invite);
        messageNum = (TextView) view.findViewById(R.id.header_center_message_circle);
    }

    /**
     * 未认证
     */
    private void initUnAuth(View view) {
        unAuth = (RelativeLayout) view.findViewById(R.id.header_center_unauth);
        headerUnAuth = (CircleImageView) view.findViewById(R.id.header_center_unauth_head);
        tvGoAuth = (TextView) view.findViewById(R.id.header_center_unauth_verify);
        unAuthName = (TextView) view.findViewById(R.id.header_center_unauth_name);
        unAuthPhone = (TextView) view.findViewById(R.id.header_center_unauth_phone);
    }

    /**
     * 已认证
     */
    private void initAuth(View view) {
        authView = (RelativeLayout) view.findViewById(R.id.header_center_auth);
        headerAuth = (CircleImageView) view.findViewById(R.id.header_center_auth_head);
        authName = (TextView) view.findViewById(R.id.header_center_auth_name);
        authPhone = (TextView) view.findViewById(R.id.header_center_auth_phone);
    }

    private void getPersonConfig() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getPersonConfig");
        map.put("module", Constants.MODULE_CONFIG);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            JSONObject obj = object.getJSONObject("content");
                            hotline = obj.getString("hotline");
                            inviteUrl = obj.getString("inviteurl");
                            SPUtils.put(getActivity(), Constants.HOT_LINE, hotline);
                            SPUtils.put(getActivity(), Constants.INVITE_URL, inviteUrl);
                            if (!obj.isNull("gameurl")) {
                                SPUtils.put(getActivity(), Constants.GAMEURL, obj.getString("gameurl"));
                            }
                            if (!obj.isNull("gamepower")) {
                                SPUtils.put(getActivity(), Constants.NAVIGAME, obj.getInt("gamepower"));
                            } else {
                                SPUtils.put(getActivity(), Constants.NAVIGAME, 0);
                            }
                            if(!obj.isNull("interagelevel")) {
                                JSONArray array = obj.getJSONArray("interagelevel");
                                FileUtils.saveJsonFile(array.toString(), getActivity(), "json");
                            }
                            if (!obj.isNull("activeUrl")) {
                                SPUtils.put(getActivity(), Constants.ACTIVITURL, obj.getString("activeUrl"));
                            }
                            if (!obj.isNull("activePower")) {
                                SPUtils.put(getActivity(), Constants.NAVIACTIVITY, obj.getInt("activePower"));
                            } else {
                                SPUtils.put(getActivity(), Constants.NAVIACTIVITY, 0);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        isPersonDone = true;
                        mHasLoadedOnce = true;
                        init();
                        if (isInfoDone) {
                            loading.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void getUserInfo() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "userInfo");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        reFreshHeaderView(User.getInstance());
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                String str = jsonObject.getJSONObject("content").toString();
                                User preUser = User.getInstance();
                                User user = null;
                                try {
                                    JSONObject json = new JSONObject(str);
                                    user = User.parseUser(getActivity(), json, preUser.userType);
                                    User.saveUser(user, getActivity());
                                } catch (JSONException e) {
                                    return;
                                }
                                reFreshHeaderView(user);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        isInfoDone = true;
                        if (isPersonDone) {
                            loading.setVisibility(View.GONE);
                        }
                    }
                });

    }

    private void init() {
        reFreshHeaderView(User.getInstance());
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it;
                HashMap<String, String> map;
                switch (position) {
                    case 0://账号设置
                        map = new HashMap<String, String>();
                        MobclickAgent.onEvent(getActivity(), "usersetclick ", map);
                        if (CommonUtils.isLogin()) {
                            it = new Intent(getActivity(), AccountSettingActivity.class);
                            startActivity(it);
                        } else {
                            CommonUtils.showRegDialog(getActivity(), "请您先登录", 0);
                        }
                        break;
                    case 1://我的钱包
                        map = new HashMap<String, String>();
                        MobclickAgent.onEvent(getActivity(), "mywalletclick ", map);
                        if (CommonUtils.isLogin()) {
                            it = new Intent(getActivity(), MyWalletActivity.class);
                            startActivity(it);
                        } else {
                            CommonUtils.showRegDialog(getActivity(), "请您先登录", 0);
                        }
                        break;
                    case 2://我的卡券
                        map = new HashMap<String, String>();
                        MobclickAgent.onEvent(getActivity(), "mycardclick ", map);
                        if (CommonUtils.isLogin()) {
                            it = new Intent(getActivity(), MyCardActivity.class);
                            startActivity(it);
                        } else {
                            CommonUtils.showRegDialog(getActivity(), "请您先登录", 0);
                        }
                        break;
                    case 3://邀请好友
                        map = new HashMap<String, String>();
                        MobclickAgent.onEvent(getActivity(), "invitefriendclick ", map);
                        if (CommonUtils.isLogin()) {
                            it = new Intent(getActivity(), MyInviteActivity.class);
                            startActivity(it);
                        } else {
                            CommonUtils.showRegDialog(getActivity(), "请您先登录", 0);
                        }
                        break;
                    case 4://客服热线
                        map = new HashMap<String, String>();
                        MobclickAgent.onEvent(getActivity(), "kefuphoneclick", map);
                        if (hotline == null || hotline.equals("")) {
                            CommonUtils.callPhone("4000116929", getActivity());
                        } else {
                            CommonUtils.callPhone(hotline, getActivity());
                        }
                        break;
                    case 5://意见反馈
                        it = new Intent(getActivity(), SuggestActivity.class);
                        startActivity(it);
                        break;
                    case 6://关于售房宝
                        it = new Intent(getActivity(), AboutSfbActivity.class);
                        startActivity(it);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void setClickListener() {
        //未登录注册
        tvLoginReg.setOnClickListener(mClickListener);
        walletLayout.setOnClickListener(mClickListener);
        messageLayout.setOnClickListener(mClickListener);
        reportLayout.setOnClickListener(mClickListener);
        inviteLayout.setOnClickListener(mClickListener);
        //未认证
        headerUnAuth.setOnClickListener(mClickListener);
        tvGoAuth.setOnClickListener(mClickListener);
        //已认证
        headerAuth.setOnClickListener(mClickListener);
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent it;
            HashMap<String, String> map;
            switch (v.getId()) {
                case R.id.navbar_image_right:
                    if (User.getInstance().userType == User.TYPE_NORMAL_USER) {
                        Intent intent = new Intent(getActivity(), CaptureActivity.class);
                        startActivity(intent);
                    } else {
                        AppUtils.showRegAuthDialog(getActivity(), "请您先登录", 0);
                    }
                    break;
                case R.id.header_center_unreg_login://登录注册
                    it = new Intent(getActivity(), LoginActivity.class);
                    startActivity(it);
                    break;
                case R.id.header_center_unauth_verify://去认证
                    if (User.getInstance().verify == User.USER_DEAL) {
                        Toast.makeText(getActivity(), "您的认证我们已经收到，请耐心等待", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (User.getInstance().verify == User.USER_THROUGH) {
                        Toast.makeText(getActivity(), "您已认证成功，无需再次认证", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    it = new Intent(getActivity(), AuthActivity.class);
                    startActivity(it);
                    break;
                case R.id.header_center_unreg_wallet://积分商城
                    map = new HashMap<String, String>();
                    MobclickAgent.onEvent(getActivity(), "personalintegralmall ", map);
                    it = new Intent(getActivity(), PointMallActivity.class);
                    startActivity(it);
                    break;
                case R.id.header_center_unreg_message://我的消息
                    map = new HashMap<String, String>();
                    MobclickAgent.onEvent(getActivity(), "mymessageclick ", map);
                    if (CommonUtils.isLogin()) {
                        it = new Intent(getActivity(), MyMessageActivity.class);
                        startActivity(it);
                    } else {
                        CommonUtils.showRegDialog(getActivity(), "请您先登录", 0);
                    }
                    break;
                case R.id.header_center_unreg_report://报备进程
                    map = new HashMap<String, String>();
                    MobclickAgent.onEvent(getActivity(), "myreportclick ", map);
                    if (CommonUtils.isLogin()) {
                        it = new Intent(getActivity(), MyReportActivity.class);
                        startActivity(it);
                    } else {
                        CommonUtils.showRegDialog(getActivity(), "请您先登录", 0);
                    }
                    break;
                case R.id.header_center_unreg_invite://我的邀请
                    if (CommonUtils.isLogin()) {
                        it = new Intent(getActivity(), MyCollectActivity.class);
                        startActivity(it);
                    } else {
                        CommonUtils.showRegDialog(getActivity(), "请您先登录", 0);
                    }
                    break;
                case R.id.header_center_unauth_head://头像
                case R.id.header_center_auth_head:
                    modifyHeader();
                    break;
                default:
                    break;
            }
        }
    };

    public void onEventMainThread(User user) {
        reFreshHeaderView(user);
    }

    //消息推送
    public void onEventMainThread(com.kupangstudio.shoufangbao.model.Message message) {
        if (message.getStyle() == 2) {
            User user = User.getInstance();
            user.message = user.message + 1;
            user.verify = User.USER_THROUGH;
            User.saveUser(user, getActivity());
            reFreshHeaderView(user);
        }
    }

    private void reFreshHeaderView(User user) {
        if (user.message > 0) {
            messageNum.setVisibility(View.VISIBLE);
            messageNum.setText(String.valueOf(user.message));
        } else {
            messageNum.setVisibility(View.GONE);
        }
        if (user.userType == User.TYPE_NORMAL_USER) {
            if (user.verify == User.USER_THROUGH) {
                unRegister.setVisibility(View.GONE);
                unAuth.setVisibility(View.GONE);
                authView.setVisibility(View.VISIBLE);
                initAuthState(user);
            } else {
                unRegister.setVisibility(View.GONE);
                authView.setVisibility(View.GONE);
                unAuth.setVisibility(View.VISIBLE);
                initUnAuthState(user);
            }
        } else {
            unRegister.setVisibility(View.VISIBLE);
            unAuth.setVisibility(View.GONE);
            authView.setVisibility(View.GONE);
        }
        itemAdapter = new ItemAdapter(getActivity(), itemArray);
        lv.setAdapter(itemAdapter);
        scrollView.smoothScrollTo(0, 0);
        lv.setFocusable(false);
    }

    //初始化认证未通过以及未认证状态
    private void initUnAuthState(User user) {
        imageLoader.displayImage(user.face, headerUnAuth, options);
        unAuthName.setText(user.realName);
        unAuthPhone.setText(user.mobile);
        if (user.verify == User.USER_UN_THROUGH) {
            tvGoAuth.setText("未通过");
        } else if (user.verify == User.USER_DEAL) {
            tvGoAuth.setText("处理中");
        } else {
            tvGoAuth.setText("去认证");
        }
    }

    //初始化已认证状态
    private void initAuthState(User user) {
        imageLoader.displayImage(user.face, headerAuth, options);
        authName.setText(user.realName);
        authPhone.setText(user.mobile);
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible || mHasLoadedOnce) {
            return;
        }
        loading.setVisibility(View.VISIBLE);
        getPersonConfig();
        if (User.getInstance().userType == User.TYPE_NORMAL_USER) {
            getUserInfo();
        } else {
            loading.setVisibility(View.GONE);
            init();
        }
    }

    class ItemAdapter extends BaseAdapter {

        LayoutInflater inflater;
        String[] items;

        public ItemAdapter(Context ctx, String[] items) {
            inflater = LayoutInflater.from(ctx);
            this.items = items;
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
            convertView = inflater.inflate(R.layout.item_fragment_center, parent, false);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
            TextView textView = (TextView) convertView.findViewById(R.id.content);
            imageView.setImageResource(iconArray[position]);
            textView.setText(itemArray[position]);
            return convertView;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        OkHttpUtils.getInstance().cancelTag("modify_head");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ViewGroup) fragmentView).removeView(fragmentView);
    }

    private void modifyHeader() {
        sdcardTempFile = new File(Constants.IMAGE_PATH + File.separator,
                "header.jpg");
        if (!sdcardTempFile.exists()) {
            try {
                sdcardTempFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (dialog == null) {
            dialog = new AlertDialog.Builder(getActivity()).setItems(
                    new String[]{"相机", "相册"},
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            if (which == 0) {
                                if (AppUtils.checkPermission(getActivity(), "android.permission.CAMERA")) {
                                    // 选择拍照
                                    Intent cameraintent = new Intent(
                                            MediaStore.ACTION_IMAGE_CAPTURE);
                                    // 指定调用相机拍照后照片的储存路径
                                    cameraintent.putExtra(
                                            MediaStore.EXTRA_OUTPUT,
                                            Uri.fromFile(sdcardTempFile));
                                    startActivityForResult(cameraintent,
                                            RESULTCODE_CAPTURE);
                                } else {
                                    Toast.makeText(getActivity(), "请您打开照相机权限", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                            } else {
                                Intent intent = new Intent(
                                        "android.intent.action.PICK");
                                intent.setDataAndType(
                                        MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                                        "image/*");
                                intent.putExtra("output",
                                        Uri.fromFile(sdcardTempFile));
                                intent.putExtra("crop", "true");
                                intent.putExtra("aspectX", 1);// 裁剪框比例
                                intent.putExtra("aspectY", 1);
                                intent.putExtra("outputX", crop);// 输出图片大小
                                intent.putExtra("outputY", crop);
                                startActivityForResult(intent,
                                        RESULTCODE_PHOTO);
                            }
                        }
                    }).create();
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case RESULTCODE_PHOTO://相机
                    Bitmap bmp = BitmapFactory.decodeFile(sdcardTempFile
                            .getAbsolutePath());
                    headerUnAuth.setImageBitmap(bmp);
                    headerAuth.setImageBitmap(bmp);
                    // 本地保存，上传图片
                    handleImage(sdcardTempFile);
                    break;
                case RESULTCODE_CAPTURE:
                    Uri uri = Uri.fromFile(sdcardTempFile);
                    if (uri == null) {
                        Toast.makeText(getActivity(), "内存不足请稍后重试！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startPhotoZoom(Uri.fromFile(sdcardTempFile));
                    break;
                case RESULTCODE_CROP://相册
                    if (data != null) {
                        bmp = data.getParcelableExtra("data");
                        headerUnAuth.setImageBitmap(bmp);
                        headerAuth.setImageBitmap(bmp);
                        // 本地保存，上传图片
                        handleImage(sdcardTempFile);
                    }
                    break;
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 1) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "头像上传成功", Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "头像上传失败", Toast.LENGTH_SHORT)
                            .show();
                }
            }

        }
    };

    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, RESULTCODE_CROP);

    }

    public void handleImage(File file) {
        ImageUtils.scaleSaveFile(file, getActivity(), 200f, 200f);
        User user = User.getInstance();
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "editUser");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        HashMap<String, File> mapCard = new HashMap<>();
        mapCard.put("face", file);
        new FileUpload(Constants.HOST_URL, map, mapCard, new MyListener(), upDialog).execute("");
    }

    class MyListener implements FileUpload.UploadListener {
        @Override
        public void onUploadEnd(boolean success, String object) {
            Message msg = Message.obtain();
            if (success) {
                try {
                    JSONObject obj = new JSONObject(object.toString());
                    int code = obj.getInt("code");
                    if (code > Result.RESULT_OK) {
                        msg.what = 1;
                    } else {
                        msg.what = 0;
                    }
                } catch (JSONException e) {
                    msg.what = 0;
                }
            } else {
                msg.what = 0;
            }
            mHandler.sendMessage(msg);
        }
    }

}
