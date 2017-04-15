package com.kupangstudio.shoufangbao;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Answer;
import com.kupangstudio.shoufangbao.model.GoodsDetail;
import com.kupangstudio.shoufangbao.model.GuessHappy;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.AppUtils;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.kupangstudio.shoufangbao.widget.PullToRefreshListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;


public class PointMallActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private PullToRefreshListView refreshListView;
    private ListView mlistView;
    private RelativeLayout pointIntegral;//我的等级
    private RelativeLayout pointDuihuan;//兑换记录
    //private RelativeLayout pointCaiCaiLe;//猜猜乐
    //未登录
    private RelativeLayout unRegLayout;
    private LinearLayout loginRegLayout;//去登陆注册
    private TextView unRegRule;//兑换规则
    //登录
    private RelativeLayout normalLayout;
    private LinearLayout pointLayout;//查看积分
    private TextView pointRule;//兑换规则
    private TextView tvPoint;//积分
    private boolean isFirst;
    private List<GoodsDetail> list;
    private MyAdapter adapter;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;
    private RelativeLayout loadingLayout;
    private static boolean flag;
    private User userLevel;
    private View headView;
    private long beginTime;
    private long endTime;
    private ProgressDialog dialog;
    private RelativeLayout emptyview;
    private Button emptyview_btn;
    private TextView emptyviewText;
    private ImageView emptyviewImg;
    private LinearLayout mallSign;
    private LinearLayout mallChou;
    private LinearLayout mallPlay;
    private ImageView pointMeet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_mall);
        CommonUtils.addActivity(this);
        beginTime = System.currentTimeMillis();
        CommonUtils.handleTitleBarRightGone(this, "积分商城");
        initView();
        setClickListener();
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        list = new ArrayList<GoodsDetail>();
        getGoodsListData();
        User user = User.getInstance();
        EventBus.getDefault().register(this);
        if (user.userType == User.TYPE_NORMAL_USER) {
            unRegLayout.setVisibility(View.GONE);
            normalLayout.setVisibility(View.VISIBLE);
            //getTodayGuessHappyData();
            getUserInfo();
        } else {
            unRegLayout.setVisibility(View.VISIBLE);
            normalLayout.setVisibility(View.GONE);
        }
        emptyview_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGoodsListData();
            }
        });
        int ismeet = (int) SPUtils.get(PointMallActivity.this, Constants.ISMEET, 0);
        if (ismeet == 1) {
            pointMeet.setImageResource(R.drawable.point_is_meet);
        }
    }

    private void initView() {
        headView = LayoutInflater.from(PointMallActivity.this).inflate(R.layout.header_activity_pointmall, null);
        pointMeet = (ImageView) headView.findViewById(R.id.pointmall_meet_img);
        pointIntegral = (RelativeLayout) headView.findViewById(R.id.point_dengji);
        pointDuihuan = (RelativeLayout) headView.findViewById(R.id.point_duihuan);
        unRegLayout = (RelativeLayout) headView.findViewById(R.id.point_unreg);
        loginRegLayout = (LinearLayout) headView.findViewById(R.id.point_loginreg);
        unRegRule = (TextView) headView.findViewById(R.id.mall_ruleunreg);
        normalLayout = (RelativeLayout) headView.findViewById(R.id.point_normal);
        pointLayout = (LinearLayout) headView.findViewById(R.id.point_point);
        pointRule = (TextView) headView.findViewById(R.id.mall_rulenormal);
        tvPoint = (TextView) headView.findViewById(R.id.tv_point);
        mallSign = (LinearLayout) headView.findViewById(R.id.point_mall_head_sign);
        mallChou = (LinearLayout) headView.findViewById(R.id.point_mall_head_chou);
        mallPlay = (LinearLayout) headView.findViewById(R.id.point_mall_head_play);
        refreshListView = (PullToRefreshListView) findViewById(R.id.mall_refresh_listview);
        refreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
        refreshListView.getLoadingLayoutProxy().setRefreshingLabel("正在刷新...");
        refreshListView.getLoadingLayoutProxy().setReleaseLabel("下拉刷新");
        refreshListView.getLoadingLayoutProxy().setPullLabel("释放立即刷新");
        mlistView = refreshListView.getRefreshableView();
        mlistView.addHeaderView(headView, null, false);
        loadingLayout = (RelativeLayout) findViewById(R.id.mall_loading);
        emptyview = (RelativeLayout) findViewById(R.id.emptyview);
        emptyview_btn = (Button) findViewById(R.id.emptyview_btn);
        emptyviewText = (TextView) findViewById(R.id.emptyview_text);
        emptyviewImg = (ImageView) findViewById(R.id.emptyview_img);
    }

    private void setClickListener() {
        pointIntegral.setOnClickListener(onClickListener);
        pointDuihuan.setOnClickListener(onClickListener);
        loginRegLayout.setOnClickListener(onClickListener);
        unRegRule.setOnClickListener(onClickListener);
        pointLayout.setOnClickListener(onClickListener);
        pointRule.setOnClickListener(onClickListener);
        mallSign.setOnClickListener(onClickListener);
        mallChou.setOnClickListener(onClickListener);
        mallPlay.setOnClickListener(onClickListener);
        mlistView.setOnItemClickListener(this);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent it;
            switch (v.getId()) {
                case R.id.point_dengji://积分记录
                    if (User.getInstance().userType != User.TYPE_NORMAL_USER) {
                        AppUtils.showRegAuthDialog(PointMallActivity.this, "请您先登录", 0);
                        return;
                    }
                    it = new Intent(PointMallActivity.this, MyPointsActivity.class);
                    startActivity(it);
                    break;

                case R.id.point_duihuan://兑换记录
                    if (User.getInstance().userType != User.TYPE_NORMAL_USER) {
                        AppUtils.showRegAuthDialog(PointMallActivity.this, "请您先登录", 0);
                        return;
                    }
                    Intent itRecord = new Intent(PointMallActivity.this, ExchangeRecordActivity.class);
                    startActivity(itRecord);
                    break;

                case R.id.point_loginreg://去登录注册
                    flag = true;
                    it = new Intent(PointMallActivity.this, LoginActivity.class);
                    startActivity(it);
                    break;

                case R.id.mall_ruleunreg://积分规则
                    Intent intent = new Intent(PointMallActivity.this, CommonRuleActivity.class);
                    intent.putExtra("title", "积分规则");
                    intent.putExtra("url", Constants.INTEGRAL_RULE);
                    startActivity(intent);
                    break;

                case R.id.point_point://我的等级
                    if (User.getInstance().userType != User.TYPE_NORMAL_USER) {
                        AppUtils.showRegAuthDialog(PointMallActivity.this, "请您先登录", 0);
                        return;
                    }
                    it = new Intent(PointMallActivity.this, MyLevelActivity.class);
                    startActivity(it);
                    break;

                case R.id.mall_rulenormal://积分规则
                    intent = new Intent(PointMallActivity.this, CommonRuleActivity.class);
                    intent.putExtra("title", "积分规则");
                    intent.putExtra("url", Constants.INTEGRAL_RULE);
                    startActivity(intent);
                    break;

                case R.id.point_mall_head_sign://签到
                    Map<String, String> map1 = new HashMap<String, String>();
                    MobclickAgent.onEvent(PointMallActivity.this, "signinclick", map1);
                    if (User.getInstance().userType == User.TYPE_NORMAL_USER) {
                        if (User.getInstance().verify == User.USER_THROUGH) {
                            if (User.getInstance().style == 1) {
                                getMeet();
                            } else {
                                Toast.makeText(PointMallActivity.this, "亲，成功带看两次就可晋升为标准经纪人啦!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (User.getInstance().verify == User.USER_DEAL) {
                                Toast.makeText(PointMallActivity.this, "您的认证已提交，请耐心等待。", Toast.LENGTH_SHORT).show();
                            } else {
                                AppUtils.showRegAuthDialog(PointMallActivity.this, "请您先认证", 1);
                            }
                        }
                    } else {
                        AppUtils.showRegAuthDialog(PointMallActivity.this, "请您先登录", 0);
                    }
                    break;
                case R.id.point_mall_head_chou://抽奖
                    Map<String, String> map = new HashMap<String, String>();
                    MobclickAgent.onEvent(PointMallActivity.this, "luckdraw", map);
                    intent = new Intent(PointMallActivity.this, DazhuanPanActivity.class);
                    if (User.getInstance().userType == User.TYPE_NORMAL_USER) {
                        int openActivity = (int) SPUtils.get(PointMallActivity.this, Constants.NAVIACTIVITY, 0);
                        if (openActivity == 1) {
                            intent.putExtra("title", "活动");
                            intent.putExtra("url", (String) SPUtils.get(PointMallActivity.this, Constants.ACTIVITURL, "") + "&uid=" + User.getInstance().uid);
                            //intent.putExtra("task", 81);
                            startActivity(intent);
                        } else {
                            Toast.makeText(PointMallActivity.this, "精彩活动即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        AppUtils.showRegAuthDialog(PointMallActivity.this, "请您先登录", 0);
                    }
                    break;
                case R.id.point_mall_head_play://玩玩乐
                    intent = new Intent(PointMallActivity.this, CommonRuleActivity.class);
                    if (User.getInstance().userType == User.TYPE_NORMAL_USER) {
                        int openGame = (int) SPUtils.get(PointMallActivity.this, Constants.NAVIGAME, 0);
                        if (openGame == 1) {
                            intent.putExtra("title", "玩玩乐");
                            intent.putExtra("url", (String) SPUtils.get(PointMallActivity.this, Constants.GAMEURL, "") + "&uid=" + User.getInstance().uid);
                            intent.putExtra("task", 81);
                            startActivity(intent);
                        } else {
                            Toast.makeText(PointMallActivity.this, "精彩游戏即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        AppUtils.showRegAuthDialog(PointMallActivity.this, "请您先登录", 0);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void initIsFirst() {
        isFirst = (boolean) SPUtils.get(PointMallActivity.this, "firstEnterPoint", true);
        if (isFirst) {
            SPUtils.put(PointMallActivity.this, "firstEnterPoint", false);
        }
    }

    //签到
    private void getMeet() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "doMeet");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(PointMallActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                String notice = jsonObject.getString("notice");
                                SPUtils.put(PointMallActivity.this, Constants.ISMEET, 1);
                                Toast.makeText(PointMallActivity.this, notice, Toast.LENGTH_SHORT).show();
                                pointMeet.setImageResource(R.drawable.point_is_meet);
                                getUserInfo();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(PointMallActivity.this, ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    /*private void showQuestionDialog(Context ctx, final GuessHappy guessHappy) {
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.common_dialog_question, null);
        final TextView title = (TextView) layout.findViewById(R.id.dialog_caiyicai_content);
        ImageView close = (ImageView) layout.findViewById(R.id.dialog_caiyicai_close);
        TextView remark = (TextView) layout.findViewById(R.id.dialog_caiyicai_remark);
        final ImageView ok = (ImageView) layout.findViewById(R.id.dialog_caiyicai_ok);
        ImageView ivOne = (ImageView) layout.findViewById(R.id.caiyicai_answerone_iv);
        final ImageView okClose = (ImageView) layout.findViewById(R.id.dialog_caiyicai_image_close);
        final TextView tvOne = (TextView) layout.findViewById(R.id.caiyicai_answerone_tv);
        ImageView ivTwo = (ImageView) layout.findViewById(R.id.caiyicai_answertwo_iv);
        final TextView tvTwo = (TextView) layout.findViewById(R.id.caiyicai_answertwo_tv);
        ImageView ivThree = (ImageView) layout.findViewById(R.id.caiyicai_answerthree_iv);
        final TextView tvThree = (TextView) layout.findViewById(R.id.caiyicai_answerthree_tv);
        final ImageView answerOne = (ImageView) layout.findViewById(R.id.caiyicai_answerone_ok);
        final ImageView answerTwo = (ImageView) layout.findViewById(R.id.caiyicai_answertwo_ok);
        final ImageView answerThree = (ImageView) layout.findViewById(R.id.caiyicai_answerthree_ok);
        title.setText(guessHappy.getTittle());
        remark.setText("备注：" + guessHappy.getRemark());
        String[] url = guessHappy.getContent();
        imageLoader.displayImage(url[0], ivOne, options);
        imageLoader.displayImage(url[1], ivTwo, options);
        imageLoader.displayImage(url[2], ivThree, options);
        tvOne.setSelected(false);
        tvTwo.setSelected(false);
        tvThree.setSelected(false);
        final Dialog dialog = new AlertDialog.Builder(this).create();

        dialog.setCanceledOnTouchOutside(true);
        if (!(this.isFinishing())) {
            dialog.show();
        }
        dialog.getWindow().setContentView(layout);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        dialog.getWindow().setGravity(Gravity.TOP);
        lp.y = (int) (44 * CommonUtils.getDensity(ctx) + 0.5f);
        dialog.getWindow().setAttributes(lp);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        okClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tvOne.isSelected() && !tvTwo.isSelected() && !tvThree.isSelected()) {
                    Toast.makeText(PointMallActivity.this, "请选择答案", Toast.LENGTH_SHORT).show();
                    return;
                }
                okClose.setVisibility(View.VISIBLE);
                CommonUtils.setTaskDone(PointMallActivity.this, 80);
                int answer;
                if (tvOne.isSelected()) {
                    answer = 0;
                } else if (tvTwo.isSelected()) {
                    answer = 1;
                } else {
                    answer = 2;
                }
                Answer answer1 = new Answer();
                answer1.setAnswer(guessHappy.getAnswer());
                answer1.setError(guessHappy.getError());
                answer1.setTitle(guessHappy.getCorrect());
                getAnswerData(title, answerOne, answerTwo, answerThree, guessHappy.getKid(), answer, answer1);
            }
        });
        ivOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOne.setSelected(true);
                tvTwo.setSelected(false);
                tvThree.setSelected(false);
            }
        });
        tvOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOne.setSelected(true);
                tvTwo.setSelected(false);
                tvThree.setSelected(false);
            }
        });
        ivTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOne.setSelected(false);
                tvTwo.setSelected(true);
                tvThree.setSelected(false);
            }
        });
        tvTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOne.setSelected(false);
                tvTwo.setSelected(true);
                tvThree.setSelected(false);
            }
        });
        ivThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOne.setSelected(false);
                tvTwo.setSelected(false);
                tvThree.setSelected(true);
            }
        });
        tvThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOne.setSelected(false);
                tvTwo.setSelected(false);
                tvThree.setSelected(true);
            }
        });
    }

    private void showHappyDialog(Context ctx, GuessHappy guessHappy) {
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.common_dialog_happy, null);
        TextView title = (TextView) layout.findViewById(R.id.happydialog_title);
        TextView content = (TextView) layout.findViewById(R.id.happydialog_content);
        ImageView close = (ImageView) layout.findViewById(R.id.happydialog_close);
        title.setText(guessHappy.getTittle());
        content.setText(guessHappy.getContent()[0]);
        final Dialog dialog = new AlertDialog.Builder(this).create();
        dialog.setCanceledOnTouchOutside(true);
        if (!(this.isFinishing())) {
            dialog.show();
        }
        dialog.getWindow().setContentView(layout);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        dialog.getWindow().setGravity(Gravity.TOP);
        lp.y = (int) (147 * CommonUtils.getDensity(ctx) + 0.5f);
        dialog.getWindow().setAttributes(lp);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }*/

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("type", "listclick");
        MobclickAgent.onEvent(PointMallActivity.this, "goodsclick", map);
        Intent intent = new Intent(PointMallActivity.this, IntegralDetailActivity.class);
        intent.putExtra("goodsdetail", list.get(i - 1));
        startActivity(intent);
    }

    //获取商品列表
    private void getGoodsListData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getWareList");
        map.put("module", Constants.MODULE_MALL);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        map.put("cityId", String.valueOf(SPUtils.get(PointMallActivity.this, Constants.BUILD_CITY_ID, 1)));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        list.clear();
                        loadingLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (adapter == null) {
                            adapter = new MyAdapter(PointMallActivity.this, list);
                            mlistView.setAdapter(adapter);
                        } else {
                            adapter.list = list;
                            adapter.notifyDataSetChanged();
                        }
                        emptyviewText.setText("世界那么大，没网怎么看！！！");
                        emptyviewImg.setImageResource(R.drawable.common_empty);
                        emptyview.setVisibility(View.VISIBLE);
                        Toast.makeText(PointMallActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject1 = new JSONObject(response);
                            int code = jsonObject1.getInt("code");
                            String notice = jsonObject1.getString("notice");
                            if (code > Result.RESULT_OK) {
                                emptyviewText.setText("此地区暂不参与积分商城活动");
                                JSONArray jsonArray = jsonObject1.getJSONArray("content");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    GoodsDetail goodsDetail = new GoodsDetail();
                                    if (!jsonObject.isNull("wid")) {
                                        goodsDetail.setWid(jsonObject.getInt("wid"));
                                    }

                                    if (!jsonObject.isNull("type")) {
                                        goodsDetail.setType(jsonObject.getInt("type"));
                                    } else {
                                        goodsDetail.setType(0);
                                    }

                                    if (!jsonObject.isNull("integrate")) {
                                        goodsDetail.setIntegrate(jsonObject.getInt("integrate"));
                                    } else {
                                        goodsDetail.setIntegrate(0);
                                    }

                                    if (!jsonObject.isNull("stock")) {
                                        goodsDetail.setStock(jsonObject.getInt("stock"));
                                    } else {
                                        goodsDetail.setStock(0);
                                    }

                                    if (!jsonObject.isNull("change")) {
                                        goodsDetail.setChange(jsonObject.getInt("change"));
                                    } else {
                                        goodsDetail.setChange(0);
                                    }

                                    if (!jsonObject.isNull("title")) {
                                        goodsDetail.setTitle(jsonObject.getString("title"));
                                    } else {
                                        goodsDetail.setTitle("");
                                    }

                                    if (!jsonObject.isNull("worth")) {
                                        goodsDetail.setWorth(jsonObject.getString("worth"));
                                    } else {
                                        goodsDetail.setWorth("");
                                    }

                                    if (!jsonObject.isNull("city")) {
                                        goodsDetail.setCity(jsonObject.getString("city"));
                                    } else {
                                        goodsDetail.setCity("北京");
                                    }

                                    if (!jsonObject.isNull("format")) {
                                        goodsDetail.setFormat(jsonObject.getString("format"));
                                    } else {
                                        goodsDetail.setFormat("");
                                    }

                                    if (!jsonObject.isNull("freight")) {
                                        goodsDetail.setFreight(jsonObject.getInt("freight"));
                                    } else {
                                        goodsDetail.setFreight(0);
                                    }
                                    JSONArray array;
                                    if (!jsonObject.isNull("pic")) {
                                        array = jsonObject.getJSONArray("pic");
                                    } else {
                                        array = new JSONArray();
                                    }

                                    List<String> data = new ArrayList<String>();
                                    int size = array.length();
                                    for (int j = 0; j < size; j++) {
                                        data.add((String) array.get(j));
                                    }
                                    goodsDetail.setPic(data);
                                    list.add(goodsDetail);
                                }
                            } else {
                                Toast.makeText(PointMallActivity.this, notice, Toast.LENGTH_SHORT).show();
                            }
                            if (adapter == null) {
                                adapter = new MyAdapter(PointMallActivity.this, list);
                                mlistView.setAdapter(adapter);
                            } else {
                                adapter.list = list;
                                adapter.notifyDataSetChanged();
                            }

                            if (list.size() == 0) {
                                emptyviewText.setText("此地区暂不参与积分商城活动");
                                emptyviewImg.setImageResource(R.drawable.common_empty_nodata);
                                emptyview.setVisibility(View.VISIBLE);
                            } else {
                                emptyview.setVisibility(View.GONE);
                            }

                        } catch (JSONException e) {
                            Toast.makeText(PointMallActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        loadingLayout.setVisibility(View.GONE);
                    }
                });
    }

    //兑换虚拟商品
    private void getExchangVirtualData(int wid, final int worth) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "exchangeDummy");
        map.put("module", Constants.MODULE_MALL);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        map.put("wid", String.valueOf(wid));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    private ProgressDialog dialog;

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        dialog = new ProgressDialog(PointMallActivity.this);
                        CommonUtils.progressDialogShow(dialog, "正在兑换中");
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(PointMallActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                getGoodsListData();
                                EventBus.getDefault().post(User.getInstance());
                                String json = jsonObject.getJSONObject("content").toString();
                                String title;
                                try {
                                    JSONObject object = new JSONObject(json);
                                    title = object.getString("tip");
                                } catch (JSONException e) {
                                    title = "兑换失败";
                                    Toast.makeText(PointMallActivity.this, "数据格式错误", Toast.LENGTH_SHORT).show();
                                }
                                View dialogView = LayoutInflater.from(PointMallActivity.this).inflate(R.layout.common_dialog_onebtn, null);
                                final AppDialog.Builder builder = new AppDialog.Builder(PointMallActivity.this, AppDialog.Builder.COMMONDIALOG);
                                builder.setContentView(dialogView).
                                        setTitle(title).
                                        setPositiveButton("知道了", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                builder.dismiss();
                                            }
                                        }).create();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(PointMallActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        CommonUtils.progressDialogDismiss(dialog);
                    }
                });
    }

    /*//获取今日的猜一猜或者乐一乐
    private void getTodayGuessHappyData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getTodayknow");
        map.put("module", Constants.MODULE_MALL);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(PointMallActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                initIsFirst();
                                flag = false;
                                GuessHappy guessHappy = new GuessHappy();
                                String json = jsonObject.getJSONObject("content").toString();
                                JSONObject object = new JSONObject(json);
                                guessHappy.setKid(object.getInt("kid"));
                                if (!object.isNull("title")) {
                                    guessHappy.setTittle(object.getString("title"));
                                }
                                int type = object.getInt("type");
                                guessHappy.setType(type);
                                if (type == GuessHappy.GUESS) {
                                    guessHappy.setRemark(object.getString("remark"));
                                    JSONArray jsonArray = object.getJSONArray("content");
                                    String[] content = new String[jsonArray.length()];
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        content[i] = jsonArray.getString(i);
                                    }
                                    guessHappy.setContent(content);
                                    guessHappy.setCorrect(object.getString("correct"));
                                    guessHappy.setError(object.getString("error"));
                                    guessHappy.setAnswer(object.getInt("answer"));
                                } else {
                                    String[] content = new String[1];
                                    content[0] = object.getString("content");
                                    guessHappy.setContent(content);
                                }
                                if (isFirst) {
                                    if (guessHappy.getType() == GuessHappy.GUESS) {
                                        showQuestionDialog(PointMallActivity.this, guessHappy);
                                    } else {
                                        showHappyDialog(PointMallActivity.this, guessHappy);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(PointMallActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //猜一猜的时候提交答案
    private void getAnswerData(final TextView resultguess, final ImageView answerOne, final ImageView answerTwo,
                               final ImageView answerThree, int kid, int answers, final Answer answer1) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "knowAnswer");
        map.put("module", Constants.MODULE_MALL);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        map.put("kid", String.valueOf(kid));
        map.put("answer", String.valueOf(answers));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new AnswerCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(PointMallActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Result<Answer> response) {
                        if (response.getCode() > Result.RESULT_OK) {
                            Answer answer = response.getContent();
                            answer.setTitle(answer1.getTitle());
                            answer.setError(answer1.getError());
                            answer.setAnswer(answer1.getAnswer());
                            String str;
                            if (answer.getAnswer() == 0) {
                                answerOne.setVisibility(View.VISIBLE);
                                str = "A";
                            } else if (answer.getAnswer() == 1) {
                                answerTwo.setVisibility(View.VISIBLE);
                                str = "B";
                            } else {
                                answerThree.setVisibility(View.VISIBLE);
                                str = "C";
                            }
                            if (response.getCode() == 2073) {
                                SpannableString string = new SpannableString(answer.getTitle() + "+" + answer.getIntegrate() + "积分" + "\n" + "正确答案：" + str);
                                int start = answer.getTitle().length();
                                int end = start + String.valueOf(answer.getIntegrate()).length() + 3;
                                string.setSpan(new ForegroundColorSpan(Color.parseColor("#be1a20")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                resultguess.setText(string);
                                getUserInfo();
                            } else {
                                resultguess.setText(answer.getError() + "\n" + "正确答案：" + str);
                            }
                        } else {
                            Toast.makeText(PointMallActivity.this, response.getNotice(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private abstract class AnswerCallback extends Callback<Result<Answer>>{
        @Override
        public Result<Answer> parseNetworkResponse(Response response) throws Exception {
            Result<Answer> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<Answer>>(){}.getType());
            }catch (Exception e){
                return null;
            }
            return result;
        }
    }
    */
    //商品列表展
    class MyAdapter extends BaseAdapter {

        LayoutInflater inflater;
        List<GoodsDetail> list;

        public MyAdapter(Context ctx, List<GoodsDetail> list) {
            inflater = LayoutInflater.from(ctx);
            this.list = list;
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
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_point_mall, parent, false);
                holder.img = (ImageView) convertView.findViewById(R.id.item_point_mall_img);
                holder.goodsName = (TextView) convertView.findViewById(R.id.item_point_mall_goodsname);
                holder.goodsCity = (TextView) convertView.findViewById(R.id.item_point_mall_city);
                holder.goodsIntegral = (TextView) convertView.findViewById(R.id.item_point_mall_integral);
                holder.goodsStock = (TextView) convertView.findViewById(R.id.item_point_mall_stock);
                holder.goodsChange = (Button) convertView.findViewById(R.id.item_point_mall_change);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final GoodsDetail goodsDetail = list.get(position);
            final int wid = goodsDetail.getWid();
            final int point = goodsDetail.getIntegrate();
            final int type = goodsDetail.getType();
            holder.goodsChange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("type", "book");
                    map.put("quantity", "3");
                    MobclickAgent.onEvent(PointMallActivity.this, "purchase", map);
                    User user = User.getInstance();
                    if (user.verify == User.USER_THROUGH) {
                        if (user.cityId <= 0) {
                            showSignDailog();
                        } else {
                            if (User.getInstance().style != User.NORMAL_BROKER) {
                                Toast.makeText(PointMallActivity.this, "亲，成功带看两次就可晋升为标准经纪人参加活动啦！", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (user.cityId == (int) SPUtils.get(PointMallActivity.this, Constants.BUILD_CITY_ID, 1)) {
                                if (user.interage < point) {
                                    Toast.makeText(PointMallActivity.this, "您的积分不足！", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (goodsDetail.getStock() == 0) {
                                    Toast.makeText(PointMallActivity.this, "没有库存", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (type == 2) {
                                    //实物兑换
                                    Intent intent = new Intent(PointMallActivity.this, GoodsChangeActivity.class);
                                    intent.putExtra("goodsdetail", goodsDetail);
                                    startActivity(intent);
                                } else {
                                    //虚拟兑换
                                    View v = LayoutInflater.from(PointMallActivity.this).inflate(R.layout.common_dialog_custom, null);
                                    final AppDialog.Builder builder = new AppDialog.Builder(PointMallActivity.this, AppDialog.Builder.COMMONDIALOG);
                                    builder.setContentView(v).setTitle("是否确定兑换该商品吗？")
                                            .setPositiveButton("确定", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    getExchangVirtualData(wid, point);
                                                    builder.dismiss();
                                                }
                                            })
                                            .setNegativeButton("取消", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    builder.dismiss();
                                                }
                                            }).create();
                                }
                            } else {
                                Toast.makeText(PointMallActivity.this, "亲，当前您只能兑换认证城市的商品", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        if (user.verify == User.USER_DEAL) {
                            Toast.makeText(PointMallActivity.this, "认证通过才能兑换，请耐心等待！", Toast.LENGTH_SHORT).show();
                        } else {
                            if (user.userType != User.TYPE_NORMAL_USER) {
                                AppUtils.showRegAuthDialog(PointMallActivity.this, "亲，仅支持活动区域注册认证后的标准经纪人兑换", 0);
                            } else {
                                AppUtils.showRegAuthDialog(PointMallActivity.this, "亲，仅支持活动区域认证后的标准经纪人兑换", 1);
                            }
                        }
                    }
                }
            });

            List<String> l = goodsDetail.getPic();
            imageLoader.displayImage(l.get(0), holder.img, options);
            holder.goodsName.setText(goodsDetail.getTitle());
            holder.goodsStock.setText(String.valueOf(goodsDetail.getStock()));
            holder.goodsCity.setText(goodsDetail.getCity());
            holder.goodsIntegral.setText(String.valueOf(goodsDetail.getIntegrate()));
            return convertView;
        }
    }

    static class ViewHolder {
        private ImageView img;
        private TextView goodsName;
        private TextView goodsIntegral;
        private TextView goodsCity;
        private TextView goodsStock;
        private Button goodsChange;
    }

    public void onEventMainThread(User event) {
        if (event.uid == -1) {
            normalLayout.setVisibility(View.GONE);
            unRegLayout.setVisibility(View.VISIBLE);
        } else {
            normalLayout.setVisibility(View.VISIBLE);
            unRegLayout.setVisibility(View.GONE);
            getUserInfo();
        }
    }

    public void onEventMainThread(GoodsDetail event) {
        getGoodsListData();
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
    protected void onStop() {
        super.onStop();
        endTime = System.currentTimeMillis();
        int duration = (int) (endTime - beginTime);
        Map<String, String> map_value = new HashMap<String, String>();
        MobclickAgent.onEventValue(this, "jifen", map_value, duration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
        CommonUtils.closeDialog(dialog);
        EventBus.getDefault().unregister(this);
    }

    private void showSignDailog() {
        View view = LayoutInflater.from(PointMallActivity.this).inflate(R.layout.common_dialog_onebtn, null);
        final AppDialog.Builder builder = new AppDialog.Builder(PointMallActivity.this, AppDialog.Builder.COMMONDIALOG);
        builder.setContentView(view).setTitle("亲，当前只支持活动区域经纪人兑换。")
                .setPositiveButton("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.dismiss();
                    }
                }).create();
    }

    //刷新积分
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
                        tvPoint.setText(String.valueOf(User.getInstance().interage));
                        Toast.makeText(PointMallActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
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
                                JSONObject json = new JSONObject(str);
                                user = User.parseUser(PointMallActivity.this, json, preUser.userType);
                                User.saveUser(user, PointMallActivity.this);
                            }
                            tvPoint.setText(String.valueOf(User.getInstance().interage));
                        } catch (JSONException e) {
                            tvPoint.setText(String.valueOf(User.getInstance().interage));
                            Toast.makeText(PointMallActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }
}
