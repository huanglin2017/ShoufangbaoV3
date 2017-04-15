package com.kupangstudio.shoufangbao.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.service.carrier.CarrierMessagingService;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.kupangstudio.shoufangbao.CommonRuleActivity;
import com.kupangstudio.shoufangbao.CommunityDetailActivity;
import com.kupangstudio.shoufangbao.CooperBrandActivity;
import com.kupangstudio.shoufangbao.MainActivity;
import com.kupangstudio.shoufangbao.MyInviteActivity;
import com.kupangstudio.shoufangbao.MyMessageActivity;
import com.kupangstudio.shoufangbao.PointMallActivity;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.SuggestActivity;
import com.kupangstudio.shoufangbao.XuanChuanPageActivity;
import com.kupangstudio.shoufangbao.base.BaseFragment;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.News;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.AppUtils;
import com.kupangstudio.shoufangbao.utils.CityComparator;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.FileUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.kupangstudio.shoufangbao.widget.PullToRefreshListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.OkHttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.system.text.ShortMessage;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by long on 15/11/9.
 * Copyright 2015 android_xiaobai.
 * 社区
 */
public class CommunityFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private ImageView moreLeft, moreRight;
    private TextView moreTitle;
    private ListView moreList;
    private View headView;
    private List<News> list;
    private List<Integer> data;
    private SocialMessageAdapter madapter;
    private int flag = 0;
    private List<ImageView> dates;
    private List<ImageView> datesline;
    private int id;
    private String url;
    private PullToRefreshListView pullmoreList;
    private int offset;
    private ImageView topimage;
    private TextView toptitle;
    private TextView signRules;
    private TextView signMoney;
    private String signrules;
    private RelativeLayout emptyView;
    private String shareUrl;
    private Button emptybtn;
    private RelativeLayout loading;
    private boolean isFirst = true;
    private View footer;
    private RelativeLayout pointmarket;
    //签到任务配置
    private RelativeLayout signTaskLayout;
    private TextView tvTip;
    private TextView tvContent;
    private TextView tvTitle;
    private Button btnCancel;
    private Button btnOk;
    private TaskReceiver receiver;
    private ImageView lineweek2;
    private ImageView lineweek3;
    private ImageView lineweek4;
    private ImageView lineweek5;
    private ImageView lineweek6;
    private ImageView lineweek7;
    private ImageView lineweek1;
    private ImageView week1;
    private ImageView week2;
    private ImageView week3;
    private ImageView week4;
    private ImageView week5;
    private ImageView week6;
    private ImageView week7;
    private TextView rules;
    private RelativeLayout signTaskRl;
    private RelativeLayout signTaskClose;
    private TextView signTaskContentA;
    private TextView signTaskContentB;
    private TextView signTaskContentC;
    private Button signTaskBtnA;
    private Button signTaskBtnB;
    private Button signTaskBtnC;
    private Button signTaskSubmit;
    private TextView signTaskTitle;
    private ImageView signTaskRightA;
    private ImageView signTaskRightB;
    private ImageView signTaskRightC;
    private TextView signTaskTodaytask;
    private int answer;
    private boolean selcetSub = true;
    private View fragmentView;
    /**
     * 标志位，标志已经初始化完成
     */
    private boolean isPrepared;
    /**
     * 是否已被加载过一次，第二次就不再去请求数据了
     */
    private boolean mHasLoadedOnce;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver = new TaskReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.kupangstudio.shoufangbao.taskdone");
        getActivity().registerReceiver(receiver, filter);
        EventBus.getDefault().register(this);
        ShareSDK.initSDK(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null == fragmentView) {
            User user = User.getInstance();
            shareUrl = "https://www.shoufangbao.com/index.php?r=appweb/meet&invite=" + user.uid;
            fragmentView = inflater.inflate(R.layout.fragment_community, container, false);
            initView(fragmentView);
            initData();
            setClickListener();
            signTaskLayout = (RelativeLayout) fragmentView.findViewById(R.id.sign_task_layout);
            signTaskLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            pullmoreList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
                @Override
                public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                    moreList.setEnabled(false);
                    String label = DateUtils.formatDateTime(
                            getActivity(),
                            System.currentTimeMillis(),
                            DateUtils.FORMAT_SHOW_TIME
                                    | DateUtils.FORMAT_SHOW_DATE
                                    | DateUtils.FORMAT_ABBREV_ALL);
                    // 显示最后更新的时间
                    refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                    offset = 0;
                    list.clear();
                    getNewsListData();
                    pullmoreList.setMode(PullToRefreshBase.Mode.BOTH);
                }

                @Override
                public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                    String label = DateUtils.formatDateTime(
                            getActivity(),
                            System.currentTimeMillis(),
                            DateUtils.FORMAT_SHOW_TIME
                                    | DateUtils.FORMAT_SHOW_DATE
                                    | DateUtils.FORMAT_ABBREV_ALL);
                    // 显示最后更新的时间
                    refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                    getNewsListData();
                }
            });
            isPrepared = true;
        }
        return fragmentView;
    }

    private void initData() {
        View tiao = footer.findViewById(R.id.common_footer_tiao);
        tiao.setVisibility(View.GONE);
        datesline = new LinkedList<ImageView>();

        datesline.add(lineweek1);
        datesline.add(lineweek2);
        datesline.add(lineweek3);
        datesline.add(lineweek4);
        datesline.add(lineweek5);
        datesline.add(lineweek6);
        datesline.add(lineweek7);
        Calendar calendar = Calendar.getInstance();
        int index = calendar.get(Calendar.DAY_OF_WEEK);
        if (index != 1) {
            datesline.get(index - 1).setImageResource(R.drawable.community_sign_waitsign);
        } else {
            datesline.get(6).setImageResource(R.drawable.community_sign_waitsign);
        }
        offset = 0;
        dates = new LinkedList<ImageView>();
        dates.add(week1);
        dates.add(week2);
        dates.add(week3);
        dates.add(week4);
        dates.add(week5);
        dates.add(week6);
        dates.add(week7);
        pullmoreList.setMode(PullToRefreshBase.Mode.BOTH);
        pullmoreList.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
        pullmoreList.getLoadingLayoutProxy(false, true).setReleaseLabel("松手加载");
        pullmoreList.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载");
        moreLeft.setVisibility(View.GONE);
        moreRight.setVisibility(View.GONE);
        moreTitle.setText("社区");
        list = new ArrayList<News>();
        moreList = pullmoreList.getRefreshableView();
        moreList.addHeaderView(headView, null, false);
    }

    private void initView(View view) {
        headView = LayoutInflater.from(getActivity()).inflate(R.layout.header_fragment_community, null);
        footer = LayoutInflater.from(getActivity()).inflate(R.layout.common_footer, null);

        emptyView = (RelativeLayout) view.findViewById(R.id.emptyview);
        loading = (RelativeLayout) view.findViewById(R.id.homepager_loading);
        emptybtn = (Button) view.findViewById(R.id.emptyview_btn);

        signRules = (TextView) headView.findViewById(R.id.social_sign_rule);
        signMoney = (TextView) headView.findViewById(R.id.social_sign_money);
        rules = (TextView) headView.findViewById(R.id.resiger_rules);

        lineweek2 = (ImageView) headView.findViewById(R.id.line_week1);
        lineweek3 = (ImageView) headView.findViewById(R.id.line_week2);
        lineweek4 = (ImageView) headView.findViewById(R.id.line_week3);
        lineweek5 = (ImageView) headView.findViewById(R.id.line_week4);
        lineweek6 = (ImageView) headView.findViewById(R.id.line_week5);
        lineweek7 = (ImageView) headView.findViewById(R.id.line_week6);
        lineweek1 = (ImageView) headView.findViewById(R.id.line_week7);

        week1 = (ImageView) headView.findViewById(R.id.week1);
        week2 = (ImageView) headView.findViewById(R.id.week2);
        week3 = (ImageView) headView.findViewById(R.id.week3);
        week4 = (ImageView) headView.findViewById(R.id.week4);
        week5 = (ImageView) headView.findViewById(R.id.week5);
        week6 = (ImageView) headView.findViewById(R.id.week6);
        week7 = (ImageView) headView.findViewById(R.id.week7);

        topimage = (ImageView) headView.findViewById(R.id.top_qiandao);
        toptitle = (TextView) headView.findViewById(R.id.social_top_qiandao_title);
        pointmarket = (RelativeLayout) headView.findViewById(R.id.point_market);//积分商城入口

        pullmoreList = (PullToRefreshListView) view.findViewById(R.id.social_list);

        moreLeft = (ImageView) view.findViewById(R.id.navbar_image_left);
        moreRight = (ImageView) view.findViewById(R.id.navbar_image_right);
        moreTitle = (TextView) view.findViewById(R.id.navbar_title);

        //任务布局初始化
        tvTip = (TextView) view.findViewById(R.id.sign_task_tip);
        TextPaint tp = tvTip.getPaint();
        tp.setFakeBoldText(true);

        //答题任务初始化
        signTaskRl = (RelativeLayout) view.findViewById(R.id.sign_task_rl);
        signTaskClose = (RelativeLayout) view.findViewById(R.id.sign_task_close);
        signTaskContentA = (TextView) view.findViewById(R.id.sign_task_contentA);
        signTaskContentB = (TextView) view.findViewById(R.id.sign_task_contentB);
        signTaskContentC = (TextView) view.findViewById(R.id.sign_task_contentC);
        signTaskBtnA = (Button) view.findViewById(R.id.sign_task_btnA);
        signTaskBtnB = (Button) view.findViewById(R.id.sign_task_btnB);
        signTaskBtnC = (Button) view.findViewById(R.id.sign_task_btnC);
        signTaskSubmit = (Button) view.findViewById(R.id.sign_task_submit);
        signTaskTitle = (TextView) view.findViewById(R.id.sign_task_titletoday);
        signTaskTodaytask = (TextView) view.findViewById(R.id.sign_task_todaytask);
        signTaskRightA = (ImageView) view.findViewById(R.id.sign_task_rightA);
        signTaskRightB = (ImageView) view.findViewById(R.id.sign_task_rightB);
        signTaskRightC = (ImageView) view.findViewById(R.id.sign_task_rightC);

        tvTitle = (TextView) view.findViewById(R.id.sign_task_title);
        tvContent = (TextView) view.findViewById(R.id.sign_task_content);
        btnCancel = (Button) view.findViewById(R.id.sign_task_btncancel);
        btnOk = (Button) view.findViewById(R.id.sign_task_btnok);
    }

    private void setClickListener() {
        emptybtn.setOnClickListener(this);
        rules.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        topimage.setOnClickListener(this);
        pointmarket.setOnClickListener(this);
        moreList.setOnItemClickListener(this);
        signTaskClose.setOnClickListener(this);
        signTaskBtnA.setOnClickListener(this);
        signTaskBtnB.setOnClickListener(this);
        signTaskBtnC.setOnClickListener(this);
        signTaskSubmit.setOnClickListener(this);
        signTaskRl.setOnClickListener(this);
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible || mHasLoadedOnce) {
            return;
        }
        getNewsListData();
        if (User.getInstance().userType == User.TYPE_NORMAL_USER) {
            getSignListData();
        }
        getCommunityConfiger();
    }

    class TaskReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Calendar calendar = Calendar.getInstance();
            int index = calendar.get(Calendar.DAY_OF_WEEK) - 2;
            if (index == -1) {
                index = 6;
            }
            dates.get(index).setImageResource(R.drawable.community_sign_red);
            topimage.setImageResource(R.drawable.community_sign_topalreadysign);
            toptitle.setText("已签到");
            getSignListData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fragmentView != null) {
            ((ViewGroup) fragmentView.getParent()).removeView(fragmentView);
        }
    }

    // user状态改变，刷新界面
    public void onEventMainThread(User event) {
        offset = 0;
        list.clear();
        if (User.getInstance().userType == User.TYPE_NORMAL_USER && User.getInstance().cityId > 0) {
            getSignListData();
        } else {
            topimage.setImageResource(R.drawable.community_sign_topwaitsign);
            toptitle.setText("签到");
        }
        getNewsListData();
        if (event.userType != User.TYPE_NORMAL_USER) {
            week1.setImageResource(R.drawable.community_week_monday);
            week2.setImageResource(R.drawable.community_week_tuesday);
            week3.setImageResource(R.drawable.community_week_wednesday);
            week4.setImageResource(R.drawable.community_week_thursday);
            week5.setImageResource(R.drawable.community_week_friday);
            week6.setImageResource(R.drawable.community_week_saturday);
            week7.setImageResource(R.drawable.community_week_sunday);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.resiger_rules://签到规则
                HashMap<String, String> map = new HashMap<String, String>();
                MobclickAgent.onEvent(getActivity(), "signruleclick", map);
                if (getActivity() != null) {
                    Intent it = new Intent(getActivity(), CommonRuleActivity.class);
                    it.putExtra("title", "帮助中心");
                    it.putExtra("url", Constants.HELPCENTER);
                    startActivity(it);
                }
                break;
            case R.id.emptyview_btn:
                emptyView.setVisibility(View.GONE);
                getNewsListData();
                break;
            case R.id.top_qiandao://签到
                User user = User.getInstance();
                HashMap<String, String> map1 = new HashMap<String, String>();
                MobclickAgent.onEvent(getActivity(), "signinclick", map1);
                topimage.setEnabled(false);
                if (User.getInstance().verify == User.USER_THROUGH) {
                    if (User.getInstance().cityId <= 0) {
                        topimage.setEnabled(true);
                        showSignDailog();
                    } else {
                        if (User.getInstance().style != User.NORMAL_BROKER) {
                            topimage.setEnabled(true);
                            CommonUtils.showCommonDialogOk(getActivity(), "亲，成功带看两次就可晋升为标准经纪人啦", "");
                            return;
                        }
                        int isRun = (int) SPUtils.get(getActivity(), Constants.TASK, 1);
                        if (isRun == 1) {//打开
                            if (SPUtils.get(getActivity(), User.getInstance().uid + Constants.TASKNUM, 250) !=null) {
                                String localTime = TimeUtils.getCustomReportData(System.currentTimeMillis());
                                String preTime = (String) SPUtils.get(getActivity(), User.getInstance().uid + Constants.TASKTIME, "");
                                if (!localTime.equals(preTime)) {
                                    SPUtils.put(getActivity(), Constants.TASKISANSWER, true);
                                }
                                boolean isCan = (boolean) SPUtils.get(getActivity(), Constants.TASKISANSWER, true);
                                if (isCan) {
                                    signTaskRl.setVisibility(View.VISIBLE);
                                    //文字加粗
                                    TextPaint tp = signTaskTodaytask.getPaint();
                                    tp.setFakeBoldText(true);
                                    signTaskContentA.setText((String) SPUtils.get(getActivity(), Constants.TASKCHOICEA, ""));
                                    signTaskContentB.setText((String) SPUtils.get(getActivity(), Constants.TASKCHOICEB, ""));
                                    signTaskContentC.setText((String) SPUtils.get(getActivity(), Constants.TASKCHOICEC, ""));
                                    signTaskTitle.setText((String) SPUtils.get(getActivity(), Constants.TASKTITLE, ""));
                                } else {
                                    Toast.makeText(getActivity(), "今日已经签到失败，请明日再来", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String preTime;
                                String localTime = TimeUtils.getCustomReportData(System.currentTimeMillis());
                                preTime = (String) SPUtils.get(getActivity(), User.getInstance().uid + Constants.TASKTIME, "");
                                if (!localTime.equals(preTime)) {

                                    SPUtils.put(getActivity(), Constants.TASKCONTENT, "首先，点击“客户”模块--点击右上角“添加客户”按钮--最后，填写相应的客户信息");
                                    SPUtils.put(getActivity(), User.getInstance().uid + Constants.TASKTIME, localTime);
                                    SPUtils.put(getActivity(), User.getInstance().uid + Constants.TASKNUM, 250);
                                    SPUtils.put(getActivity(), Constants.TASKPARAMETER, 0);
                                    SPUtils.put(getActivity(), Constants.TASKTITLE, "添加客户，体验小宝的客户管理功能");
                                    SPUtils.put(getActivity(), Constants.TASKTIP, "做任务 拿签到奖励");
                                    SPUtils.put(getActivity(), Constants.TASKTRUE, "做任务 拿奖励");
                                    SPUtils.put(getActivity(), Constants.TASKFALSE, "给奖也不要");
                                    String key = User.getInstance().uid + "taskdone";
                                    SPUtils.put(getActivity(), key, false);
                                }

                                switch ((int) SPUtils.get(getActivity(), User.getInstance().uid + Constants.TASKNUM, 250)) {
                                    case 1://合作品牌
                                        break;
                                    case 2://楼盘列表(筛选)
                                        break;
                                    case 3://楼盘详情 bid
                                        break;
                                    case 4://强制分享 bid
                                        break;
                                    case 5://在线报备 bid
                                        break;
                                    case 6://收藏
                                        break;
                                    case 7://活动页面分享
                                        break;
                                    case 20://添加客户
                                        break;
                                    case 21://联系客户
                                        break;
                                    case 22://添加跟进
                                        break;
                                    case 23://报备客户
                                        break;
                                    case 24://客户分析
                                        break;
                                    case 40://消息列表
                                        break;
                                    case 41://宣传页制作 type
                                        break;
                                    case 42://意见反馈
                                        break;
                                    case 43://分享邀请码
                                        break;
                                    case 60://社区文章阅读 mid
                                        break;
                                    case 61://社区文章分享 mid
                                        break;
                                    case 62://社区文章  点赞 mid
                                        break;
                                    case 80://猜猜乐
                                        break;
                                    case 81://参与游戏
                                        break;
                                    case 82://查看积分规则
                                        break;
                                    case 100://web任务
                                        break;
                                    case 250://默认任务
                                        break;
                                    default:
                                        String localTime1 = TimeUtils.getCustomReportData(System.currentTimeMillis());
                                        SPUtils.put(getActivity(), Constants.TASKCONTENT, "首先，点击“客户”模块--点击右上角“添加客户”按钮--最后，填写相应的客户信息");
                                        SPUtils.put(getActivity(), User.getInstance().uid + Constants.TASKTIME, localTime1);
                                        SPUtils.put(getActivity(), User.getInstance().uid + Constants.TASKNUM, 250);
                                        SPUtils.put(getActivity(), Constants.TASKPARAMETER, 0);
                                        SPUtils.put(getActivity(), Constants.TASKTITLE, "添加客户，体验小宝的客户管理功能");
                                        SPUtils.put(getActivity(), Constants.TASKTIP, "做任务 拿签到奖励");
                                        SPUtils.put(getActivity(), Constants.TASKTRUE, "做任务 拿奖励");
                                        SPUtils.put(getActivity(), Constants.TASKFALSE, "给奖也不要");
                                        String key = User.getInstance().uid + "taskdone";
                                        SPUtils.put(getActivity(), key, false);
                                        break;
                                }

                                if (CommonUtils.isTaskDone(getActivity())) {
                                    getSignData();
                                } else {
                                    signTaskLayout.setVisibility(View.VISIBLE);
                                    tvTip.setText((String) SPUtils.get(getActivity(), Constants.TASKTIP, ""));
                                    tvContent.setText((String) SPUtils.get(getActivity(), Constants.TASKCONTENT, ""));
                                    tvTitle.setText((String) SPUtils.get(getActivity(), Constants.TASKTITLE, ""));
                                    btnCancel.setText((String) SPUtils.get(getActivity(), Constants.TASKFALSE, ""));
                                    btnOk.setText((String) SPUtils.get(getActivity(), Constants.TASKTRUE, ""));
                                }
                            }
                        } else {
                            getSignData();
                        }
                    }
                } else {
                    topimage.setEnabled(true);
                    if (User.getInstance().verify == User.USER_DEAL) {
                        Toast.makeText(getActivity(), "您的认证已经提交，请耐心等待", Toast.LENGTH_SHORT).show();
                    } else {
                        if (User.getInstance().userType != User.TYPE_NORMAL_USER) {
                            AppUtils.showRegAuthDialog(getActivity(), "亲，仅支持活动区域注册认证后的标准经纪人签到", 0);
                        } else {
                            AppUtils.showRegAuthDialog(getActivity(), "亲，仅支持活动区域认证后的标准经纪人签到", 1);
                        }
                    }
                }
                break;
            case R.id.point_market://积分商城
                Intent intent = new Intent(getActivity(), PointMallActivity.class);
                startActivity(intent);
                break;
            case R.id.sign_task_btncancel://任务取消
                HashMap<String, String> map2 = new HashMap<String, String>();
                MobclickAgent.onEvent(getActivity(), "signincanclclick", map2);
                signTaskLayout.setVisibility(View.GONE);
                topimage.setEnabled(true);
                break;
            case R.id.sign_task_btnok://任务执行
                HashMap<String, String> map3 = new HashMap<String, String>();
                MobclickAgent.onEvent(getActivity(), "signinyesclick", map3);
                topimage.setEnabled(true);
                MainActivity act = (MainActivity) getActivity();
                final int taskNum = (int) SPUtils.get(getActivity(), User.getInstance().uid + Constants.TASKNUM, 250);
                signTaskLayout.setVisibility(View.GONE);
                Intent it;
                switch (taskNum) {
                    case 1://合作品牌
                        it = new Intent(getActivity(), CooperBrandActivity.class);
                        getActivity().startActivity(it);
                        break;
                    case 2://楼盘列表(筛选)
                        act.switchContent(MainActivity.POS_MAIN);
                        break;
                    case 3://楼盘详情 bid
                        act.switchContent(MainActivity.POS_MAIN);
                        break;
                    case 4://强制分享 bid
                        act.switchContent(MainActivity.POS_MAIN);
                        break;
                    case 5://在线报备 bid
                        act.switchContent(MainActivity.POS_MAIN);
                        break;
                    case 6://收藏
                        act.switchContent(MainActivity.POS_MAIN);
                        break;
                    case 7://活动页面分享
                        act.switchContent(MainActivity.POS_MAIN);
                        break;
                    case 20://添加客户
                        act.switchContent(MainActivity.POS_CUSTOM);
                        break;
                    case 21://联系客户
                        act.switchContent(MainActivity.POS_CUSTOM);
                        break;
                    case 22://添加跟进
                        act.switchContent(MainActivity.POS_CUSTOM);
                        break;
                    case 23://报备客户
                        act.switchContent(MainActivity.POS_CUSTOM);
                        break;
                    case 24://客户分析
                        act.switchContent(MainActivity.POS_CUSTOM);
                        break;
                    case 40://消息列表
                        it = new Intent(getActivity(), MyMessageActivity.class);
                        getActivity().startActivity(it);
                        break;
                    case 41://宣传页制作 type
                        it = new Intent(getActivity(), XuanChuanPageActivity.class);
                        getActivity().startActivity(it);
                        break;
                    case 42://意见反馈
                        it = new Intent(getActivity(), SuggestActivity.class);
                        getActivity().startActivity(it);
                        break;
                    case 43://分享邀请码
                        it = new Intent(getActivity(), MyInviteActivity.class);
                        getActivity().startActivity(it);
                        break;
                    case 60://社区文章阅读 mid
                        break;
                    case 61://社区文章分享 mid
                        break;
                    case 62://社区文章  点赞 mid
                        break;
                    case 80://猜猜乐
                        it = new Intent(getActivity(), PointMallActivity.class);
                        getActivity().startActivity(it);
                        break;
                    case 81://参与游戏
                        if (SPUtils.get(getActivity(), Constants.NAVIGAME, 0) == null && getActivity() != null) {
                            it = new Intent(getActivity(), CommonRuleActivity.class);
                            it.putExtra("title", "玩玩乐");
                            it.putExtra("task", 81);
                            it.putExtra("url", (String) SPUtils.get(getActivity(), Constants.GAMEURL, ""));
                            getActivity().startActivity(it);
                        } else {
                            Toast.makeText(getActivity(), "精彩游戏即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 82://查看积分规则
                        it = new Intent(getActivity(), CommonRuleActivity.class);
                        it.putExtra("title", "积分规则");
                        it.putExtra("task", 82);
                        it.putExtra("url", Constants.INTEGRAL_RULE);
                        getActivity().startActivity(it);
                        break;
                    case 100://web任务
                        it = new Intent(getActivity(), PointMallActivity.class);
                        getActivity().startActivity(it);
                        break;
                    case 250://默认任务
                        act.switchContent(MainActivity.POS_CUSTOM);
                        break;
                    default:
                        String localTime = TimeUtils.getCustomReportData(System.currentTimeMillis());
                        SPUtils.put(getActivity(), Constants.TASKCONTENT, "首先，点击“客户”模块--点击右上角“添加客户”按钮--最后，填写相应的客户信息");
                        SPUtils.put(getActivity(), User.getInstance().uid + Constants.TASKTIME, localTime);
                        SPUtils.put(getActivity(), User.getInstance().uid + Constants.TASKNUM, 250);
                        SPUtils.put(getActivity(), Constants.TASKPARAMETER, 0);
                        SPUtils.put(getActivity(), Constants.TASKTITLE, "添加客户，体验小宝的客户管理功能");
                        SPUtils.put(getActivity(), Constants.TASKTIP, "做任务 拿签到奖励");
                        SPUtils.put(getActivity(), Constants.TASKTRUE, "做任务 拿奖励");
                        SPUtils.put(getActivity(), Constants.TASKFALSE, "给奖也不要");
                        String key = User.getInstance().uid + "taskdone";
                        SPUtils.put(getActivity(), key, false);
                        act.switchContent(MainActivity.POS_CUSTOM);
                        break;
                }
                break;
            case R.id.sign_task_btnA://选择A
                signTaskBtnA.setSelected(true);
                signTaskBtnB.setSelected(false);
                signTaskBtnC.setSelected(false);
                answer = 1;
                break;
            case R.id.sign_task_btnB://选择B
                signTaskBtnA.setSelected(false);
                signTaskBtnB.setSelected(true);
                signTaskBtnC.setSelected(false);
                answer = 2;
                break;
            case R.id.sign_task_btnC://选择C
                signTaskBtnA.setSelected(false);
                signTaskBtnB.setSelected(false);
                signTaskBtnC.setSelected(true);
                answer = 3;
                break;
            case R.id.sign_task_submit:
                //上传答案
                if (selcetSub) {
                    if (answer == 0 && getActivity() != null) {
                        Toast.makeText(getActivity(), "请选择答案", Toast.LENGTH_SHORT).show();
                    } else {
                        getAnswerTaskData();
                    }
                } else {
                    getSignData();
                    signTaskRl.setVisibility(View.GONE);
                    topimage.setEnabled(true);
                }

                break;
            case R.id.sign_task_close://关闭任务答题窗口
                signTaskRl.setVisibility(View.GONE);
                topimage.setEnabled(true);
                break;
            case R.id.sign_task_rl://外面布局的点击事件
                break;
            default:
                break;
        }

    }

    private void showSignDailog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.common_dialog_onebtn, null);
        final AppDialog.Builder builder = new AppDialog.Builder(getActivity(), AppDialog.Builder.COMMONDIALOG);
        builder.setContentView(view).setTitle("亲，当前只支持活动区域经纪人签到。")
                .setPositiveButton("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.dismiss();
                    }
                }).create();
    }

    public void setWeek(List<Integer> data) {
        //获取系统周几,从周日开始
        Calendar calendar = Calendar.getInstance();
        int index = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (index == -1) {
            index = 6;
        }

        //判断
        for (int i = 0; i < 7; i++) {
            if (index != i) {
                if (data.get(i) == 0) {
                    dates.get(i).setImageResource(R.drawable.community_sign_grey);
                }
                if (data.get(i) == 1) {
                    dates.get(i).setImageResource(R.drawable.community_sign_red);
                }
                if (data.get(i) == 2) {
                    flag++;
                }
            } else {
                if (data.get(i) == 1) {
                    dates.get(i).setImageResource(R.drawable.community_sign_red);
                    topimage.setImageResource(R.drawable.community_sign_topalreadysign);
                    toptitle.setText("已签到");
                }
                if (data.get(i) == 2) {
                    flag++;
                }
            }
        }
    }

    //资讯条目点击事件
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        /*if (i == list.size() || i == list.size() - 1) {
            return;
        }*/
        if (i != 0) {
            News news = list.get(i - 1);
            id = news.getNid();
            url = news.getContent();
            int ext = (int) SPUtils.get(getActivity(), Constants.TASKPARAMETER, 250);
            if (id == ext) {
                CommonUtils.setTaskDone(getActivity(), 60);
            }
            String title = news.getTitle();
            String content = news.getDescribe();
            User user = User.getInstance();
            if (user.userType == User.TYPE_NORMAL_USER) {
                getNewsData(news.getNid());
                int type = news.getRead();
                TextView view1 = (TextView) view.findViewById(R.id.social_list_title_social);
                TextView view3 = (TextView) view.findViewById(R.id.social_list_content_social);
                TextView view4 = (TextView) view.findViewById(R.id.social_list_time);
                TextView view2 = (TextView) view.findViewById(R.id.social_list_chakan_num);
                String s = view2.getText().toString();
                int p = Integer.parseInt(s);
                String s1 = String.valueOf(p + 1);
                view2.setText(s1);
                Map<String, String> map = new HashMap<String, String>();
                map.put(String.valueOf(i - 1), s1);
                view2.setTag(map);
                if (type == 0) {
                    List<News> newsList = DataSupport.where("uid = ? and nid = ?", String.valueOf(User.getInstance().uid),
                            String.valueOf(news.getNid())).find(News.class);
                    News news1 = newsList.get(0);
                    news1.setRead(1);
                    news1.updateAll("uid = ? and nid = ?", String.valueOf(User.getInstance().uid),
                            String.valueOf(news.getNid()));
                    view1.setTextColor(getResources().getColor(R.color.unselect_tishi));
                    view3.setTextColor(getResources().getColor(R.color.unselect_tishi));
                    view4.setTextColor(getResources().getColor(R.color.unselect_tishi));
                    view1.setTag(news.getPic());
                }
            }
            Intent intent = new Intent(getActivity(), CommunityDetailActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("image", news.getPic());
            intent.putExtra("shareurl", url);
            intent.putExtra("title", title);
            intent.putExtra("content", content);
            intent.putExtra("mid", id);
            intent.putExtra("newsid", String.valueOf(id));
            startActivity(intent);
        }
    }

    //签到列表
    private void getSignListData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getMeet");
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
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String json = jsonObject.getJSONObject("content").toString();
                            FileUtils.saveJsonFile(json, getActivity(), "MyMeets.txt");
                            if (code > Result.RESULT_OK) {
                                JSONObject object = new JSONObject(json);
                                JSONArray jsonArray = object.getJSONArray("status");
                                List<Integer> data = new ArrayList<Integer>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    data.add(jsonArray.getInt(i));
                                }
                                setWeek(data);
                            } else {
                                if (User.getInstance().verify != User.TYPE_NORMAL_USER) {
                                    return;
                                }
                                File file = getActivity().getDir("MyMeets.txt", Context.MODE_PRIVATE);
                                String content;
                                if (!file.exists()) {
                                    Toast.makeText(getActivity(), "抱歉，签到信息加载错误", Toast.LENGTH_SHORT).show();
                                    return;
                                } else {
                                    content = FileUtils.readFileContentStr(file, getActivity(), "MyMeets.txt");
                                    if (content == null) {
                                        Toast.makeText(getActivity(), "抱歉，签到信息加载错误", Toast.LENGTH_SHORT).show();
                                        return;
                                    } else {
                                        JSONObject object = new JSONObject(content);
                                        JSONArray obj = object.getJSONArray("status");
                                        for (int i = 0; i < obj.length(); i++) {
                                            data.add(obj.getInt(i));
                                        }
                                        setWeek(data);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        mHasLoadedOnce = true;
                    }
                });
    }

    //回答今日任务的问题
    private void getAnswerTaskData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "answerTask");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        map.put("answer", String.valueOf(answer));
        map.put("sid", String.valueOf(SPUtils.get(getActivity(), Constants.TASKID, 0)));
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
                            JSONObject jsonObject1 = new JSONObject(response);
                            int code = jsonObject1.getInt("code");
                            String notice = jsonObject1.getString("notice");
                            String json = jsonObject1.getJSONObject("content").toString();
                            int key = 0;
                            JSONObject jsonObject = new JSONObject(json);
                            key = jsonObject.getInt("ext");
                            if (code > Result.RESULT_OK) {
                                if (key == 1) {
                                    signTaskRightA.setVisibility(View.VISIBLE);
                                } else if (key == 2) {
                                    signTaskRightB.setVisibility(View.VISIBLE);
                                } else {
                                    signTaskRightC.setVisibility(View.VISIBLE);
                                }
                                selcetSub = false;
                                signTaskSubmit.setText("关闭");
                                getSignData();
                                Toast.makeText(getActivity(), "今日有奖任务已完成，签到奖励进账啦", Toast.LENGTH_LONG).show();
                            } else if (code == 1132) {
                                if (key == 1) {
                                    signTaskRightA.setVisibility(View.VISIBLE);
                                } else if (key == 2) {
                                    signTaskRightB.setVisibility(View.VISIBLE);
                                } else {
                                    signTaskRightC.setVisibility(View.VISIBLE);
                                }
                                selcetSub = false;
                                signTaskSubmit.setText("关闭");
                            } else {
                                SPUtils.put(getActivity(), Constants.TASKISANSWER, false);
                                Toast.makeText(getActivity(), notice, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    //签到点击
    private void getSignData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "meet");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new MyStringCallBack());
    }

    private class MyStringCallBack extends StringCallback {

        @Override
        public void onError(Call call, Exception e) {
            Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResponse(String response) {
            try {
                JSONObject object = new JSONObject(response);
                int code = object.getInt("code");
                String notice = object.getString("message");
                topimage.setEnabled(true);
                View v = LayoutInflater.from(getActivity()).inflate(R.layout.common_diaog_signshare, null);
                if (code == 1029) {
                    Toast.makeText(getActivity(), "请您登录并认证", Toast.LENGTH_SHORT).show();
                } else {
                    if (code == 2015) {
                        //签到处理
                        topimage.setImageResource(R.drawable.community_sign_topalreadysign);
                        toptitle.setText("已签到");
                        //刷新签到列表
                        getSignListData();
                    } else if (code == 2014) {
                        //签到处理
                        topimage.setImageResource(R.drawable.community_sign_topalreadysign);
                        toptitle.setText("已签到");
                        //刷新签到列表
                        getSignListData();
                    } else {
                        Toast.makeText(getActivity(), notice, Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    //资讯列表
    private void getNewsListData() {
        if (isFirst) {
            loading.setVisibility(View.VISIBLE);
            isFirst = false;
            pullmoreList.setMode(PullToRefreshBase.Mode.DISABLED);
        }
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getNewsList");
        map.put("module", Constants.MODULE_NEWS);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        map.put("offset", String.valueOf(offset));
        map.put("cityid", String.valueOf(SPUtils.get(getActivity(), Constants.BUILD_CITY_ID, 1)));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new NewsCallBack() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        loading.setVisibility(View.GONE);
                        pullmoreList.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                        list.clear();
                        list = DataSupport.where("uid = ?", String.valueOf(User.getInstance().uid)).find(News.class);
                        if (madapter == null) {
                            madapter = new SocialMessageAdapter(getActivity(), list);
                            moreList.setAdapter(madapter);
                        } else {
                            madapter.lists = list;
                            madapter.notifyDataSetChanged();
                        }
                        if (list.size() == 0) {
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            emptyView.setVisibility(View.GONE);
                        }
                        moreList.setEnabled(true);
                        pullmoreList.onRefreshComplete();
                    }

                    @Override
                    public void onResponse(Result<List<News>> response) {
                        if (response == null) {
                            loading.setVisibility(View.GONE);
                            pullmoreList.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                            list.clear();
                            list = DataSupport.where("uid = ?", String.valueOf(User.getInstance().uid)).find(News.class);
                            if (madapter == null) {
                                madapter = new SocialMessageAdapter(getActivity(), list);
                                moreList.setAdapter(madapter);
                            } else {
                                madapter.lists = list;
                                madapter.notifyDataSetChanged();
                            }
                            if (list.size() == 0) {
                                emptyView.setVisibility(View.VISIBLE);
                            } else {
                                emptyView.setVisibility(View.GONE);
                            }
                            moreList.setEnabled(true);
                            pullmoreList.onRefreshComplete();
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        loading.setVisibility(View.GONE);
                        pullmoreList.onRefreshComplete();
                        pullmoreList.setMode(PullToRefreshBase.Mode.BOTH);
                        if (response.getCode() > Result.RESULT_OK) {
                            pullmoreList.setMode(PullToRefreshBase.Mode.BOTH);
                            List<News> data = new LinkedList<News>();
                            if (response.getContent() == null) {
                                pullmoreList.onRefreshComplete();
                                pullmoreList.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                                moreList.addFooterView(footer, null, false);
                                list.clear();
                                list = DataSupport.findAll(News.class);
                                if (madapter == null) {
                                    madapter = new SocialMessageAdapter(getActivity(), list);
                                    moreList.setAdapter(madapter);
                                } else {
                                    madapter.lists = list;
                                    madapter.notifyDataSetChanged();
                                }
                                return;
                            }
                            data = response.getContent();
                            list.addAll(data);
                            int size = data.size();
                            if (offset == 0) {
                                DataSupport.deleteAll(News.class);
                                DataSupport.saveAll(list);
                            } else {
                                if (data != null && size > 0) {
                                    DataSupport.saveAll(data);
                                }
                            }
                            if (size < 10) {
                                pullmoreList.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                                moreList.addFooterView(footer, null, false);
                            } else {
                                moreList.removeFooterView(footer);
                            }
                            offset = offset + 10;
                        } else if (response.getCode() == 1106) {
                            pullmoreList.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                            moreList.addFooterView(footer, null, false);
                        } else {
                            pullmoreList.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                            list = DataSupport.findAll(News.class);
                        }

                        if (madapter == null) {
                            madapter = new SocialMessageAdapter(getActivity(), list);
                            moreList.setAdapter(madapter);
                        } else {
                            madapter.lists = list;
                            madapter.notifyDataSetChanged();
                        }
                        if (list.size() == 0) {
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            emptyView.setVisibility(View.GONE);
                        }
                        moreList.setEnabled(true);
                        pullmoreList.onRefreshComplete();
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        mHasLoadedOnce = true;
                    }
                });
    }

    private abstract class NewsCallBack extends Callback<Result<List<News>>> {
        @Override
        public Result<List<News>> parseNetworkResponse(Response response) throws Exception {
            String string = response.body().string();
            Result<List<News>> list = null;
            try {
                list = new Gson().fromJson(string, new TypeToken<Result<List<News>>>() {
                }.getType());
            } catch (Exception e) {
                return null;
            }
            return list;
        }
    }

    //签到规则、游戏地址
    private void getCommunityConfiger() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getCommunityConfig");
        map.put("module", Constants.MODULE_CONFIG);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
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
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                String json = jsonObject.getJSONObject("content").toString();
                                JSONObject object = new JSONObject(json);
                                SPUtils.put(getActivity(), Constants.SIGNRULE, object.getString("meetexplain"));
                                SPUtils.put(getActivity(), Constants.SIGNMONEY, object.getString("meetamount"));
                                SPUtils.put(getActivity(), Constants.GAMEURL, object.getString("gameurl"));
                                SPUtils.put(getActivity(), Constants.NAVIGAME, object.getInt("gamepower"));
                                JSONArray array = object.getJSONArray("interagelevel");
                                FileUtils.saveJsonFile(array.toString(), getActivity(), "json");
                            }
                            //签到规则加载
                            signRules.setText((String) SPUtils.get(getActivity(), Constants.SIGNRULE,
                                    "每天签到可得1积分,签满一个自然周再送8积分"));
                            signMoney.setText((String) SPUtils.get(getActivity(), Constants.SIGNMONEY, "1元钱"));
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        mHasLoadedOnce = true;
                    }
                });
    }

    //资讯阅读
    private void getNewsData(int nid) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "readNews");
        map.put("module", Constants.MODULE_NEWS);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        map.put("nid", String.valueOf(nid));
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

    private void shareWx() {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);
        //sp.setTitle("【售房宝】");
        sp.setText("【售房宝】签到送大礼，日日有惊喜！你敢来，我就敢送！");
        sp.setUrl(shareUrl);
        sp.setTitleUrl(shareUrl);
        sp.setImageUrl("http://www.shoufangbao.net/images/logo/qr.png");
        Platform wx = ShareSDK.getPlatform(Wechat.NAME);
        wx.share(sp);
    }

    private void shareWxFriend() {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);
        sp.setTitle("【售房宝】签到送大礼，日日有惊喜！你敢来，我就敢送！");
        sp.setUrl(shareUrl);
        sp.setTitleUrl(shareUrl);
        sp.setImageUrl("http://www.shoufangbao.net/images/logo/qr.png");
        Platform weibo = ShareSDK.getPlatform(WechatMoments.NAME);
        weibo.share(sp);
    }

    private void shareQq() {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setText("【售房宝】签到送大礼，日日有惊喜！你敢来，我就敢送！");
        sp.setUrl(shareUrl);
        sp.setTitleUrl(shareUrl);
        sp.setImageUrl("http://www.shoufangbao.net/images/logo/qr.png");
        Platform weibo = ShareSDK.getPlatform(QQ.NAME);
        weibo.share(sp);
    }

    private void shareMessage() {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setText("【售房宝】签到送大礼，日日有惊喜！你敢来，我就敢送！" + shareUrl);
        sp.setImageUrl("http://www.shoufangbao.net/images/logo/qr.png");
        Platform weibo = ShareSDK.getPlatform(ShortMessage.NAME);
        weibo.share(sp);
    }

    public class SocialMessageAdapter extends BaseAdapter {

        private Context context;
        public List<News> lists;
        private List<Integer> data = new LinkedList<Integer>();

        public SocialMessageAdapter(Context context, List<News> lists) {
            this.context = context;
            this.lists = lists;
        }


        @Override
        public int getCount() {
            int ret = 0;
            if (context != null && list.size() > 0) {
                ret = lists.size();
            }
            return ret;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.empty_building_list)
                    .showImageForEmptyUri(R.drawable.empty_building_list)
                    .showImageOnFail(R.drawable.empty_building_list)
                    .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                    .build();
            View ret = null;
            if (view != null) {
                ret = view;
            } else {
                ret = LayoutInflater.from(context).inflate(R.layout.item_community_newslist, viewGroup, false);
            }
            ViewHolder holder = (ViewHolder) ret.getTag();
            if (holder == null) {
                holder = new ViewHolder();
                holder.touxiang = (ImageView) ret.findViewById(R.id.social_list_touxiang);
                holder.zhuangtai = (TextView) ret.findViewById(R.id.social_list_zhuangtai);
                holder.title_social = (TextView) ret.findViewById(R.id.social_list_title_social);
                holder.content_social = (TextView) ret.findViewById(R.id.social_list_content_social);
                holder.chakan = (ImageView) ret.findViewById(R.id.social_list_chakan);
                holder.chakan_num = (TextView) ret.findViewById(R.id.social_list_chakan_num);
                holder.social_time = (TextView) ret.findViewById(R.id.social_list_time);
                ret.setTag(holder);
            }
            News news = list.get(i);
            if (news.getLabel() == 1) {
                holder.zhuangtai.setVisibility(View.VISIBLE);
                holder.zhuangtai.setBackgroundColor(context.getResources().getColor(R.color.common_title_bar));
                holder.zhuangtai.setText("头条");
            } else if (news.getLabel() == 2) {
                holder.zhuangtai.setVisibility(View.VISIBLE);
                holder.zhuangtai.setBackgroundColor(context.getResources().getColor(R.color.common_green));
                holder.zhuangtai.setText("推荐");
            } else {
                holder.zhuangtai.setVisibility(View.GONE);
            }
            holder.title_social.setText(news.getTitle());
            holder.content_social.setText(news.getDescribe());

            if (holder.chakan_num.getTag() != null) {
                Map tag = (Map) holder.chakan_num.getTag();
                if (tag.get(String.valueOf(i)) != null) {
                    Object obj = tag.get(String.valueOf(i));
                    if (obj instanceof String) {
                        String txt = obj.toString();
                        holder.chakan_num.setText(txt);
                    }
                } else {
                    holder.chakan_num.setText(String.valueOf(news.getBrower()));
                }
            } else {
                holder.chakan_num.setText(String.valueOf(news.getBrower()));
            }

            holder.social_time.setText(TimeUtils.getCustomFollowData(news.getCtime() * 1000));
            int n = news.getNid();
            data.add(n);
            //设置头像
            imageLoader.displayImage(news.getPic(), holder.touxiang, options);
            //设置阅读状态
            if (news.getRead() != 0) {
                holder.title_social.setTextColor(context.getResources().getColor(R.color.unselect_tishi));
                holder.content_social.setTextColor(context.getResources().getColor(R.color.unselect_tishi));
                holder.social_time.setTextColor(context.getResources().getColor(R.color.unselect_tishi));
                holder.chakan_num.setTextColor(context.getResources().getColor(R.color.unselect_tishi));
            } else {
                holder.title_social.setTextColor(context.getResources().getColor(R.color.small_title));
                holder.content_social.setTextColor(context.getResources().getColor(R.color.content_text));
                holder.social_time.setTextColor(context.getResources().getColor(R.color.content_text));
                holder.chakan_num.setTextColor(context.getResources().getColor(R.color.content_text));
            }
            return ret;
        }

        private class ViewHolder {
            public ImageView touxiang;
            public TextView zhuangtai;
            public TextView title_social;
            public TextView content_social;
            public TextView social_time;
            public ImageView chakan;
            public TextView chakan_num;
        }
    }
}
