package com.kupangstudio.shoufangbao.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.kupangstudio.shoufangbao.AddCustomActivity;
import com.kupangstudio.shoufangbao.EditCustomActivity;
import com.kupangstudio.shoufangbao.CustomAnalysisActivity;
import com.kupangstudio.shoufangbao.CustomDetailActivity;
import com.kupangstudio.shoufangbao.MultiAddActivity;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.base.BaseFragment;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Custom;
import com.kupangstudio.shoufangbao.model.CustomFollow;
import com.kupangstudio.shoufangbao.model.FollowList;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CharacterParser;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.PinyinComparator;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.kupangstudio.shoufangbao.widget.PopMenu;
import com.kupangstudio.shoufangbao.widget.PullToRefreshListView;
import com.kupangstudio.shoufangbao.widget.SearchEditText;
import com.kupangstudio.shoufangbao.widget.SelectPopWindow;
import com.kupangstudio.shoufangbao.widget.SideBar;
import com.kupangstudio.shoufangbao.widget.SideBar.OnTouchingLetterChangedListener;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Jsmi on 2015/11/6.
 * 客户模块
 */

public class CustomFragment extends BaseFragment implements SectionIndexer, OnClickListener {
    private boolean isSelected;
    private PullToRefreshListView mPullRefreshListView;
    private RelativeLayout mPb;//进度条
    private RelativeLayout mEmptyLayout;
    private TextView titleView;
    private TextView dialog;
    // 客户列表
    private ListView mListView;
    // 字母列表
    private SideBar sideBar;
    // 所有客户数组
    private ArrayList<Custom> customs;
    private CustomAdapter adapter;
    // 搜索过滤联系人EditText
    private SearchEditText mClearEditText;
    // 没有匹配联系人
    private LinearLayout titleLayout;
    private TextView title;
    //private LoadDataTask task;
    private Button cEmptyBtn;
    ArrayList<Custom> filterCustomList;
    /**
     * 上次第一个可见元素，用于滚动时记录标识。
     */
    private int lastFirstVisibleItem = -1;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;
    private boolean isfirst = true;
    private boolean isshow = false;// titleLayout
    private PopMenu popMenu;
    private boolean isSearch;
    private Button searchcustom;
    private Button addcustom;
    private Button selectcustom;
    private TextView cEmptyText;
    private View view;
    private SelectPopWindow popWindow;
    public static int selectpos;
    private TextView rightButton;
    private boolean isFollow = false;
    /**
     * 标志位，标志已经初始化完成
     */
    private boolean isPrepared;
    /**
     * 是否已被加载过一次，第二次就不再去请求数据了
     */
    private boolean mHasLoadedOnce;
    private boolean isPopWindow;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_custom, container,
                    false);

            initView(view);
            initData();
            setClickListener();

            // 设置右侧触摸监听
            sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

                @Override
                public void onTouchingLetterChanged(String s) {
                    // 该字母首次出现的位置
                    if (customs != null) {
                        int position = getPositionForSection(s.charAt(0));
                        if (position != -1) {
                            mListView.setSelection(position);
                        }
                    }
                }
            });
            // 刷新
            mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
                @Override
                public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                    if (isSelected) {
                        mPullRefreshListView.onRefreshComplete();
                        return;
                    } else {
                        isfirst = false;
                        titleLayout.setVisibility(View.GONE);
                        getData();
                    }
                }
            });

            mListView.setOnScrollListener(new OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == SCROLL_STATE_TOUCH_SCROLL
                            && mPullRefreshListView.isEnabled() && isshow) {
                        titleLayout.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    if (customs == null) {
                        return;
                    }
                    int section = getSectionForPosition(firstVisibleItem);
                    int nextSection = getSectionForPosition(firstVisibleItem + 1);
                    int nextSecPosition = getPositionForSection(nextSection);
                    if (firstVisibleItem != lastFirstVisibleItem) {
                        MarginLayoutParams params = (MarginLayoutParams) titleLayout
                                .getLayoutParams();
                        params.topMargin = 0;
                        titleLayout.setLayoutParams(params);
                        int index = getPositionForSection(section);
                        if (index >= 0 && index < customs.size()) {
                            title.setText(customs.get(index).getSortLetters());
                        }
                    }
                    if (nextSecPosition == firstVisibleItem + 1) {
                        View childView = view.getChildAt(0);
                        if (childView != null) {
                            int titleHeight = titleLayout.getHeight();
                            int bottom = childView.getBottom();
                            MarginLayoutParams params = (MarginLayoutParams) titleLayout
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


            mListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> view, View itemview, int pos, long arg3) {
                    if (!mPullRefreshListView.isRefreshing()) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("pos", pos + "");
                        map.put("cid", customs.get(pos).getCid() % 1000 + "");
                        MobclickAgent.onEvent(getActivity(), "cusDetailClick", map);
                        Intent it = new Intent();
                        it.setClassName(getActivity(), CustomDetailActivity.class.getName());
                        if (isSearch) {
                            it.putExtra("custom", filterCustomList.get(pos));
                        } else {
                            Custom c = customs.get(pos);
                            it.putExtra("custom", c);
                        }
                        getActivity().startActivity(it);
                    }
                }
            });

            //删除客户
            mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                               int pos, long arg3) {
                    mListView.setLongClickable(false);
                    mListView.setClickable(false);
                    if (customs == null || customs.size() < pos) {
                        return false;
                    }
                    final Custom cus = customs.get(pos);
                    View dialog = LayoutInflater.from(getActivity()).
                            inflate(R.layout.common_dialog_custom, null);
                    final AppDialog.Builder builder = new AppDialog.Builder(getActivity(),
                            AppDialog.Builder.COMMONDIALOG);
                    builder.setContentView(dialog).setMessage("删除数据：" + cus.getName())
                            .setPositiveButton("确定", new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DataSupport.deleteAll(Custom.class, "cid = ? and uid = ?", String.valueOf(cus.getCid()),
                                            String.valueOf(User.getInstance().uid));
                                    getDelCustomData(cus.getCid());
                                    // 刷新列表
                                    CustomFragment.this.customs.remove(cus);
                                    adapter.list = CustomFragment.this.customs;
                                    if (CustomFragment.this.customs.size() < 1) {
                                        titleLayout.setVisibility(View.GONE);
                                    }
                                    adapter.notifyDataSetChanged();
                                    int index = mListView.getFirstVisiblePosition();
                                    if (index >= 0 && index < CustomFragment.this.customs.size()) {
                                        title.setText(CustomFragment.this.customs.get(index).getSortLetters());
                                    }
                                    titleView.setText(formatCustomNum(CustomFragment.this.customs));
                                    builder.dismiss();
                                }
                            })
                            .setNegativeButton("取消", new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    builder.dismiss();
                                }
                            })
                            .create();
                    mListView.setLongClickable(true);
                    mListView.setClickable(true);
                    return true;
                }

            });
            isPrepared = true;
        }
        return view;
    }

    private void initData() {
        filterCustomList = new ArrayList<Custom>();
        cEmptyBtn.setText("添加客户");
        cEmptyText.setText("您暂时还没有客户");
        searchcustom.setSelected(true);
        popWindow = new SelectPopWindow(getActivity(), SelectPopWindow.SELECT_CUSTOM, Constants.REPORTITEMS);
        if (isSelected) {
            int index = mListView.getFirstVisiblePosition();
            if (index >= 0 && index < customs.size()) {
                title.setText(customs.get(index).getSortLetters());
            }
        }

        int[] drawable = new int[]{R.drawable.custom_popmenu_alanysis, R.drawable.custom_popmenu_multiadd};
        popMenu = new PopMenu(getActivity(), drawable);
        popMenu.addItems(new String[]{"客户分析", "批量添加"});

        // 实例化汉字转拼音类
        pinyinComparator = new PinyinComparator();
        sideBar.setAlphaColor(getActivity().getResources().getColor(R.color.common_select));
        sideBar.setTextView(dialog, getActivity());
        //初始化标题
        titleView.setText("客户");
        //设置客户列表
        mListView = mPullRefreshListView.getRefreshableView();
        mListView.setFooterDividersEnabled(false);
        mListView.setHeaderDividersEnabled(false);
        mListView.setDivider(getResources().getDrawable(R.color.common_listview_div));
        mListView.setDividerHeight((int) getResources().getDisplayMetrics().scaledDensity);
        mListView.setEmptyView(mEmptyLayout);

        User user = User.getInstance();
        if (user.userType == User.TYPE_DEFAULT_USER) {
            mPb.setVisibility(View.GONE);
            mPullRefreshListView.setVisibility(View.VISIBLE);
        }
    }

    private void initView(View view) {

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.custom_list);
        mPb = (RelativeLayout) view.findViewById(R.id.custom_loading);
        mEmptyLayout = (RelativeLayout) view.findViewById(R.id.custom_emptyview);

        //空状态的设置
        cEmptyBtn = (Button) view.findViewById(R.id.emptyview_btn);
        cEmptyText = (TextView) view.findViewById(R.id.emptyview_text);

        //模糊搜索，条件搜索，
        mClearEditText = (SearchEditText) view.findViewById(R.id.custom_filter_edit);
        searchcustom = (Button) view.findViewById(R.id.custom_titlebar_search);
        selectcustom = (Button) view.findViewById(R.id.custom_titlebar_select);
        addcustom = (Button) view.findViewById(R.id.custom_titlebar_add);

        titleLayout = (LinearLayout) view.findViewById(R.id.title_layout);
        title = (TextView) view.findViewById(R.id.title_layout_catalog);

        sideBar = (SideBar) view.findViewById(R.id.sidrbar);
        dialog = (TextView) view.findViewById(R.id.dialog);

        titleView = (TextView) view.findViewById(R.id.navbar_title);
        rightButton = (TextView) view.findViewById(R.id.navbar_image_right_custom);

    }

    private void setClickListener() {
        searchcustom.setOnClickListener(this);
        selectcustom.setOnClickListener(this);
        addcustom.setOnClickListener(this);
        cEmptyBtn.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        // 菜单项点击监听器
        popMenu.setOnItemClickListener(popmenuItemClickListener);
        popWindow.setOnItemClickListener(listener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.custom_titlebar_search://点击搜索客户
                isPopWindow = false;
                searchcustom.setSelected(true);
                selectcustom.setSelected(false);
                mClearEditText.setSelected(true);
                mClearEditText.setVisibility(View.VISIBLE);
                break;
            case R.id.custom_titlebar_select://点击筛选条件
                searchcustom.setSelected(false);
                selectcustom.setSelected(true);
                if (isPopWindow) {
                    isPopWindow = false;
                    popWindow.dismiss();
                } else {
                    isPopWindow = true;
                    popWindow.showAsDropDown(selectcustom);
                }
                mClearEditText.setVisibility(View.GONE);
                break;
            case R.id.custom_titlebar_add://点击添加客户
                //selectcustom.setSelected(false);
                mClearEditText.setText("");
                isPopWindow = false;
                //mClearEditText.setVisibility(View.GONE);
                Map<String, String> map = new HashMap<String, String>();
                MobclickAgent.onEvent(getActivity(), "btnaddcustom", map);
                Intent it = new Intent(getActivity(), AddCustomActivity.class);
                startActivity(it);
                break;
            case R.id.navbar_image_right_custom://点击更多
                popMenu.showAsDropDown(v);
                isPopWindow = false;
                break;
            case R.id.emptyview_btn://点击空状态添加客户
                Intent intent = new Intent(getActivity(), AddCustomActivity.class);
                Map<String, String> map1 = new HashMap<String, String>();
                MobclickAgent.onEvent(getActivity(), "btnaddcustom", map1);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (mPullRefreshListView == null) {
                        mHandler.sendEmptyMessageDelayed(1, 20);
                    }
                    if (mPullRefreshListView.isRefreshing()) {
                        mPb.setVisibility(View.VISIBLE);
                        mPullRefreshListView.setVisibility(View.GONE);
                    } else {
                        mPb.setVisibility(View.GONE);
                        mPullRefreshListView.setVisibility(View.VISIBLE);
                    }
                    break;
                case 2:
                    Toast.makeText(getActivity(), "暂无匹配客户", Toast.LENGTH_SHORT)
                            .show();
                    // 重新加载数据
                    if (adapter == null) {
                        adapter = new CustomAdapter(getActivity(), customs);
                        mListView.setAdapter(adapter);
                    } else {
                        adapter.list = customs;
                        adapter.notifyDataSetChanged();
                    }
                    title.setText("");
                    titleView.setText(formatCustomNum(customs));
                    break;
                case 3:
                    // 重新加载数据
                    if (adapter == null) {
                        adapter = new CustomAdapter(getActivity(), customs);
                        mListView.setAdapter(adapter);
                    } else {
                        adapter.list = customs;
                        adapter.notifyDataSetChanged();
                    }
                    int index = mListView.getFirstVisiblePosition();
                    if (index >= 0 && index < customs.size()) {
                        title.setText(customs.get(index).getSortLetters());
                    }
                    titleView.setText(formatCustomNum(customs));
                    break;
                case 4:
                    break;
            }

        }
    };

    public void onStart() {
        super.onStart();

        // 根据输入框输入值的改变来过滤搜索
        if (mClearEditText != null && customs != null) {
            mClearEditText.addTextChangedListener(mWatcher);
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void onStop() {
        super.onStop();

        if (mClearEditText != null) {
            mClearEditText.removeTextChangedListener(mWatcher);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private String formatCustomNum(ArrayList<Custom> list) {
        if (list == null || list.size() <= 0) {
            return "客户";
        }

        return "客户（" + list.size() + "人）";
    }

    public void onEventMainThread(Custom event) {
        if (event.getDATEACTION() == Constants.CUSTOM_ADD) {
            event = CharacterParser.filledData(event);
            if (customs == null) {
                customs = new ArrayList<Custom>();
            }
            customs.add(0, event);
            Collections.sort(customs, pinyinComparator);
        } else {
            for (int i = 0; i < customs.size(); i++) {
                if (customs.get(i).getCid() == event.getCid()) {
                    customs.set(i, event);
                }
            }
        }

        notifyAdapter();
    }

    public void onEventMainThread(User event) {
        if (event.userType == User.TYPE_DEFAULT_USER) {
            // 退出登录
            customs = new ArrayList<Custom>();

            if (adapter == null) {
                adapter = new CustomAdapter(getActivity(), customs);
                mListView.setAdapter(adapter);
            } else {
                adapter.list = customs;
                adapter.notifyDataSetChanged();
            }
            titleView.setText(formatCustomNum(customs));
        } else {

            if (event.userType == User.TYPE_NORMAL_USER) {
                isfirst = true;
                titleLayout.setVisibility(View.GONE);
                //强制刷新
                getData();
            }
        }
    }

    public void onEventMainThread(ArrayList<Custom> event) {
        if (event == null) {
            return;
        }
        if (event.size() == 0) {
            return;
        }

        if (customs == null) {
            event = CharacterParser.filledData(event);
            customs = event;
            Collections.sort(customs, pinyinComparator);
        } else {
            event = CharacterParser.filledData(event);
            customs.addAll(0, event);
            Collections.sort(customs, pinyinComparator);
        }

        if (adapter == null) {
            adapter = new CustomAdapter(getActivity(), customs);
            mListView.setAdapter(adapter);
        } else {
            adapter.list = customs;
            adapter.notifyDataSetChanged();
        }
        titleView.setText(formatCustomNum(customs));
    }


    private void position(final int pos) {
        new Thread() {
            @Override
            public void run() {
                isSelected = true;
                User user = User.getInstance();
                customs = ((ArrayList<Custom>) DataSupport.
                        where("uid = ? and status = ? and isend = ?", String.valueOf(user.uid),
                                String.valueOf(pos), String.valueOf(0)).find(Custom.class));
                customs = CharacterParser.filledData(customs);
                Collections.sort(customs, pinyinComparator);
                if (customs.size() < 1) {
                    mHandler.sendEmptyMessage(2);
                } else {
                    mHandler.sendEmptyMessage(3);
                    customs = CharacterParser.filledData((ArrayList<Custom>) DataSupport.
                            where("uid = ? and status = ? and isend = ?", String.valueOf(user.uid),
                                    String.valueOf(pos), String.valueOf(0)).find(Custom.class));
                    Collections.sort(customs, pinyinComparator);
                    if (customs.size() < 1) {
                        mHandler.sendEmptyMessage(2);
                    } else {
                        mHandler.sendEmptyMessage(3);
                    }
                }
            }
        }.start();
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible || mHasLoadedOnce) {
            return;
        }
        getData();
    }

    class CustomAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private ArrayList<Custom> list;

        public CustomAdapter(Context ctx, ArrayList<Custom> list) {
            inflater = LayoutInflater.from(ctx);
            this.list = list;
        }

        // 当联系人列表数据发生变化时,用此方法来更新列表
        public void updateListView(ArrayList<Custom> customs) {
            this.list = customs;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int pos) {
            return list.get(pos);
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
                contentView = inflater.inflate(R.layout.item_custom_list,
                        parent, false);
                holder.identity = (ImageView) contentView
                        .findViewById(R.id.item_custom_identity);
                holder.call = (ImageView) contentView
                        .findViewById(R.id.item_custom_call);
                holder.msg = (ImageView) contentView
                        .findViewById(R.id.item_custom_msg);
                holder.title = (TextView) contentView
                        .findViewById(R.id.item_custom_name);
                holder.state = (TextView) contentView
                        .findViewById(R.id.item_custom_state);
                holder.times = (TextView) contentView
                        .findViewById(R.id.item_custom_times);
                holder.letterTag = (TextView) contentView
                        .findViewById(R.id.catalog);
                contentView.setTag(holder);
            }
            final Custom c = list.get(pos);
            holder.title.setText(c.getName());
            if (c.getIsend() == 1) {
                holder.state.setText("交易终止");
            } else {
                holder.state.setText(Constants.STATEITEMS[c.getStatus()]);
            }
            String firstLetter = c.getSortLetters().toUpperCase();
            if (pos == 0) {
                if (list.size() > 0) {
                    holder.letterTag.setVisibility(View.VISIBLE);
                    holder.letterTag.setText(firstLetter);
                } else {
                    holder.letterTag.setVisibility(View.GONE);
                }
            } else {
                if (list.size() > 0) {
                    String firstLetterPre = list.get(pos - 1).getSortLetters()
                            .toUpperCase();
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
            if (list.size() > 0) {
                titleLayout.setVisibility(View.VISIBLE);
            } else {
                titleLayout.setVisibility(View.GONE);
            }

            if (isFollow) {
                if (c.getFollow() == null) {
                    holder.times.setText("无沟通记录");
                } else if (c.getFollow().getCtime() == null) {
                    holder.times.setText("无沟通记录");
                } else {
                    long offset = System.currentTimeMillis() / 1000
                            - c.getFollow().getCtime();

                    if (offset <= 0) {
                        holder.times.setText("刚刚沟通过");
                    } else {

                        int min = (int) (offset / 60);
                        if (min < 60) {
                            if (min < 5) {
                                holder.times.setText("刚刚沟通过");
                            } else {
                                holder.times.setText(min + "分钟前沟通");
                            }

                        } else {
                            int hour = (int) (min / (60));

                            if (hour < 24) {
                                holder.times.setText(hour + "小时前沟通");
                            } else {
                                int day = hour / 24;
                                if (day > 90) {
                                    holder.times.setText("无沟通记录");
                                } else {
                                    holder.times.setText(day + "天前沟通记录");
                                }
                            }
                        }

                    }
                }
            } else {
                if (c.getFollow().getCtime() == null) {
                    holder.times.setText("无沟通记录");
                } else {
                    long offset = System.currentTimeMillis() / 1000
                            - c.getFollow().getCtime();

                    if (offset <= 0) {
                        holder.times.setText("刚刚沟通过");
                    } else {

                        int min = (int) (offset / 60);
                        if (min < 60) {
                            if (min < 5) {
                                holder.times.setText("刚刚沟通过");
                            } else {
                                holder.times.setText(min + "分钟前沟通");
                            }

                        } else {
                            int hour = (int) (min / (60));

                            if (hour < 24) {
                                holder.times.setText(hour + "小时前沟通");
                            } else {
                                int day = hour / 24;
                                if (day > 90) {
                                    holder.times.setText("无沟通记录");
                                } else {
                                    holder.times.setText(day + "天前沟通记录");
                                }
                            }
                        }

                    }
                }
            }
            String phone = c.getTel();
            addViewListener(holder.call, c, pos);
            addViewListener(holder.msg, c, pos);
            return contentView;
        }

        class ViewHolder {
            ImageView call;
            ImageView msg;
            ImageView identity;
            TextView title;
            TextView state;
            TextView times;
            TextView letterTag;
        }

        //打电话，发短信生成跟进
        private void addViewListener(View view, final Custom cus, final int position) {
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    long time = System.currentTimeMillis() / 1000;
                    int type = 0;
                    cus.setCtime(time);
                    list.set(position, cus);
                    adapter.notifyDataSetChanged();
                    if (view.getId() == R.id.item_custom_call) {
                        type = 2;
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("type", "phone");
                        MobclickAgent.onEvent(getActivity(), "customclick", map);
                        //打电话，为了不重复插跟进
                        for (int i = 0; i < cus.getTel().length(); i++) {
                            if (cus.getTel().substring(i, i + 1).equals("*")) {
                                Toast.makeText(getActivity(), "号码不全，请更新客户信息后联系", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        try {
                            Uri uri = Uri.parse("tel:" + cus.getTel());
                            Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                            getActivity().startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getActivity(), "抱歉，该设备暂无打电话功能。", Toast.LENGTH_SHORT).show();
                        }
                        CommonUtils.setTaskDone(getActivity(), 21);
                    } else {
                        type = 3;
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("type", "msg");
                        MobclickAgent.onEvent(getActivity(), "customclick", map);
                        String phone = cus.getTel();
                        int len = phone.length();
                        for (int i = 0; i < len; i++) {
                            if (phone.substring(i, i + 1).equals("*")) {
                                Toast.makeText(getActivity(), "号码不全，请更新客户信息后联系", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Uri uri = Uri.parse("sms:" + cus.getTel());
                        Intent it = new Intent(Intent.ACTION_VIEW, uri);
                        getActivity().startActivity(it);
                    }

                    if (type == 3) {
                        getAddCustomFollowData(cus, "短信沟通", time);
                    }

                    if (type == 2) {
                        getAddCustomFollowData(cus, "电话沟通", time);
                    }
                }
            });
        }
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */

    public void filterData(String filterStr) {
        filterCustomList.clear();
        for (Custom c : customs) {
            String name = c.getName();
            if (name.indexOf(filterStr.toString()) != -1 || CommonUtils.getPingYin(name).startsWith(
                    filterStr.toString())) {
                filterCustomList.add(c);
            }
        }
        // 根据a-z进行排序
        Collections.sort(filterCustomList, pinyinComparator);
        adapter.updateListView(filterCustomList);
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
        for (int i = 0; i < customs.size(); i++) {
            String sortStr = customs.get(i).getSortLetters();
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

        if (customs != null && customs.size() > position) {
            return customs.get(position).getSortLetters().charAt(0);
        }
        return 0;
    }

    //拉取客户列表
    private void getData() {
        if (isfirst) {
            mPb.setVisibility(View.VISIBLE);
        }
        customs = new ArrayList<Custom>();
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getCustomerList");
        map.put("module", Constants.MODULE_CUSTOM);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new GetCustomCallBack() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        mPb.setVisibility(View.GONE);
                        mPullRefreshListView.setVisibility(View.VISIBLE);
                        mPullRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                        if (!isfirst) {
                            mPullRefreshListView.onRefreshComplete();
                        }
                        customs = (ArrayList<Custom>) DataSupport.where("uid = ?",
                                String.valueOf(User.getInstance().uid)).find(Custom.class);
                        for (int i = 0; i < customs.size(); i++) {
                            List<FollowList> lists = DataSupport.where("custom_id = ?", String.valueOf(customs.get(i).getId())).find(FollowList.class);
                            customs.get(i).setFollow(lists.get(0));
                        }
                        if (customs.size() == 0) {
                            mEmptyLayout.setVisibility(View.VISIBLE);
                            return;
                        }
                        customs = CharacterParser.filledData(customs);
                        if (adapter == null) {
                            adapter = new CustomAdapter(getActivity(), customs);
                            mListView.setAdapter(adapter);
                        } else {
                            adapter.list = customs;
                            adapter.notifyDataSetChanged();
                        }
                        titleView.setText(formatCustomNum(customs));
                    }

                    @Override
                    public void onResponse(Result<ArrayList<Custom>> response) {
                        if (response == null) {
                            mPb.setVisibility(View.GONE);
                            mPullRefreshListView.setVisibility(View.VISIBLE);
                            mPullRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                            if (!isfirst) {
                                mPullRefreshListView.onRefreshComplete();
                            }
                            customs = (ArrayList<Custom>) DataSupport.where("uid = ?",
                                    String.valueOf(User.getInstance().uid)).find(Custom.class);
                            for (int i = 0; i < customs.size(); i++) {
                                List<FollowList> lists = DataSupport.where("custom_id = ?", String.valueOf(customs.get(i).getId())).find(FollowList.class);
                                customs.get(i).setFollow(lists.get(0));
                            }
                            if (customs.size() == 0) {
                                mEmptyLayout.setVisibility(View.VISIBLE);
                                return;
                            }
                            customs = CharacterParser.filledData(customs);
                            if (adapter == null) {
                                adapter = new CustomAdapter(getActivity(), customs);
                                mListView.setAdapter(adapter);
                            } else {
                                adapter.list = customs;
                                adapter.notifyDataSetChanged();
                            }
                            titleView.setText(formatCustomNum(customs));
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mPb.setVisibility(View.GONE);
                        if (!isfirst) {
                            mPullRefreshListView.onRefreshComplete();
                        }
                        mPullRefreshListView.setVisibility(View.VISIBLE);
                        customs = response.getContent();
                        // 根据拼音为联系人数组进行排序
                        if (customs != null && customs.size() > 0) {
                            customs = CharacterParser.filledData(response.getContent());
                            Collections.sort(customs, pinyinComparator);
                        }
                        if (response.getCode() < Result.RESULT_OK) {
                            if (isfirst && response.getCode() == 1070) {
                                mEmptyLayout.setVisibility(View.VISIBLE);
                                return;
                            } else {
                                customs = (ArrayList<Custom>) DataSupport.where("uid = ?", String.valueOf(User.getInstance().uid)).find(Custom.class);
                                for (int i = 0; i < customs.size(); i++) {
                                    List<FollowList> lists = DataSupport.where("custom_id = ?", String.valueOf(customs.get(i).getId())).find(FollowList.class);
                                    customs.get(i).setFollow(lists.get(0));
                                }
                                // 提示错误信息
                                Toast.makeText(getActivity(), response.getNotice(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            DataSupport.deleteAll(Custom.class);
                            for (int i = 0; i < customs.size(); i++) {
                                if (customs.get(i).getFollow() == null) {
                                    long ctime = System.currentTimeMillis() / 1000;
                                    int cid = customs.get(i).getCid();
                                    FollowList follow = new FollowList();
                                    follow.setCid(cid);
                                    follow.setCtime(ctime);
                                    customs.get(i).setFollow(follow);
                                }
                                customs.get(i).getFollow().save();
                            }
                            DataSupport.saveAll(customs);
                        }
                        if (customs != null && customs.size() > 0) {
                            if (adapter == null) {
                                adapter = new CustomAdapter(getActivity(), customs);
                                mListView.setAdapter(adapter);
                            } else {
                                adapter.list = customs;
                                adapter.notifyDataSetChanged();
                            }
                        }
                        titleView.setText(formatCustomNum(customs));
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        mHasLoadedOnce = true;
                    }
                });
    }

    private abstract class GetCustomCallBack extends Callback<Result<ArrayList<Custom>>> {
        @Override
        public Result<ArrayList<Custom>> parseNetworkResponse(Response response) throws Exception {
            String json = response.body().string();
            Result<ArrayList<Custom>> result = null;
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<ArrayList<Custom>>>() {
                        }.getType());
            } catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    //删除客户
    private void getDelCustomData(int cid) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "delCustomer");
        map.put("module", Constants.MODULE_CUSTOM);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        map.put("cid", String.valueOf(cid));
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
                            String notice = jsonObject.getString("notice");
                            Toast.makeText(getActivity(), notice, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    /**
     * 添加跟进
     *
     * @param cus     对应客户
     * @param content 跟进内容
     */
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
                        Toast.makeText(getActivity(), ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if (code > Result.RESULT_OK) {
                                JSONObject object = jsonObject.getJSONObject("content");
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

                                for (int i = 0; i < customs.size(); i++) {
                                    if (customs.get(i).getCid() == cus.getCid()) {
                                        customs.set(i, cus);
                                        notifyAdapter();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }
    /*@Override
    public int getPos() {
        return MainActivity.POS_CUSTOM;
    }*/

    private TextWatcher mWatcher = new TextWatcher() {

        @SuppressWarnings("deprecation")
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            // 这个时候不需要挤压效果 就把他隐藏掉
            isshow = true;
            titleLayout.setVisibility(View.GONE);
            mPullRefreshListView.setPullToRefreshEnabled(false);// 关闭下拉刷新
            // 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
            if (!"".equals(s.toString().trim())) {
                filterData(s.toString());
                isSearch = true;
            } else {
                isSearch = false;
                if (customs != null) {
                    adapter.updateListView(customs);
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @SuppressWarnings("deprecation")
        @Override
        public void afterTextChanged(Editable s) {
            if ("".equals(s.toString().trim())) {
                mPullRefreshListView.setPullToRefreshEnabled(true);// 打开下拉刷新
                isshow = false;
            }
        }
    };

    //第二个筛选按钮的具体实现逻辑
    OnItemClickListener listener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectpos = i - 1;
            popWindow.setSelection(i);
            isFollow = true;
            switch (i) {
                case 0:
                    isSelected = false;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            customs = (ArrayList<Custom>) DataSupport.where("uid = ?",
                                    String.valueOf(User.getInstance().uid)).find(Custom.class);
                            CharacterParser.filledData(customs);
                            Collections.sort(customs, pinyinComparator);
                            if (customs.size() < 1) {
                                mHandler.sendEmptyMessage(2);
                            } else {
                                mHandler.sendEmptyMessage(3);
                            }
                        }
                    }).start();
                    selectcustom.setText("筛选条件");
                    selectcustom.setSelected(false);
                    popWindow.dismiss();
                    break;
                case 1:
                    position(selectpos);
                    selectcustom.setText("暂未报备");
                    selectcustom.setSelected(true);
                    popWindow.dismiss();
                    break;
                case 2:
                    position(selectpos);
                    selectcustom.setText("已经报备");
                    selectcustom.setSelected(true);
                    popWindow.dismiss();
                    break;
                case 3:
                    position(selectpos);
                    selectcustom.setText("已经确认");
                    selectcustom.setSelected(true);
                    popWindow.dismiss();
                    break;
                case 4:
                    position(selectpos);
                    selectcustom.setText("已经带看");
                    selectcustom.setSelected(true);
                    popWindow.dismiss();
                    break;
                case 5:
                    position(selectpos);
                    selectcustom.setText("已经认购");
                    selectcustom.setSelected(true);
                    popWindow.dismiss();
                    break;
                case 6:
                    position(selectpos);
                    selectcustom.setText("已经成交");
                    selectcustom.setSelected(true);
                    popWindow.dismiss();
                    break;
                case 7:
                    position(selectpos);
                    selectcustom.setText("已经结佣");
                    selectcustom.setSelected(true);
                    popWindow.dismiss();
                    break;
                case 8:
                    new Thread() {
                        @Override
                        public void run() {
                            isSelected = true;
                            User user = User.getInstance();
                            //TODO:筛选查询
                            customs = ((ArrayList<Custom>) DataSupport.
                                    where("uid = ? and isend = ?", String.valueOf(user.uid), String.valueOf(1)
                                    ).find(Custom.class));
                            customs = CharacterParser.filledData(customs);
                            Collections.sort(customs, pinyinComparator);
                            if (customs.size() < 1) {
                                mHandler.sendEmptyMessage(2);
                            } else {
                                mHandler.sendEmptyMessage(3);
                                customs = CharacterParser.filledData((ArrayList<Custom>) DataSupport.
                                        where("uid = ? and isend = ?", String.valueOf(user.uid),
                                                String.valueOf(1)).find(Custom.class));
                                Collections.sort(customs, pinyinComparator);
                                if (customs.size() < 1) {
                                    mHandler.sendEmptyMessage(2);
                                } else {
                                    mHandler.sendEmptyMessage(3);
                                }
                            }
                        }
                    }.start();
                    selectcustom.setText("交易终止");
                    selectcustom.setSelected(true);
                    popWindow.dismiss();
                    break;
                default:
                    break;
            }

        }
    };
    // 弹出菜单监听器
    OnItemClickListener popmenuItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            switch (position) {
                case 0:
                    if (customs == null || customs.size() <= 0) {
                        Toast.makeText(getActivity(), "暂无客户数据，请添加客户",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent it = new Intent(getActivity(), CustomAnalysisActivity.class);
                    startActivity(it);
                    break;
                case 1:
                    it = new Intent(getActivity(), MultiAddActivity.class);
                    startActivity(it);
                    break;
            }
            popMenu.dismiss();
        }
    };


    private void handleDefaultUserTask() {

        new Thread() {

            public void run() {

                // 读取本地缓存
                ArrayList<Custom> locdatas = (ArrayList<Custom>) DataSupport.findAll(Custom.class);

                if (locdatas == null || locdatas.size() <= 0) {
                    return;
                }

                customs = CharacterParser.filledData(locdatas);
                // 根据拼音为联系人数组进行排序
                pinyinComparator = new PinyinComparator();
                Collections.sort(customs, pinyinComparator);

                // 主线程展示数据
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {

                        mPb.setVisibility(View.GONE);
                        mPullRefreshListView.setVisibility(View.VISIBLE);

                        if (adapter == null) {
                            adapter = new CustomAdapter(getActivity(), customs);
                            mListView.setAdapter(adapter);
                        } else {
                            adapter.list = customs;
                            adapter.notifyDataSetChanged();
                        }

                        titleView.setText(formatCustomNum(customs));
                    }
                });

            }

        }.start();
    }

    //刷新界面的步骤
    private void notifyAdapter() {
        if (adapter == null) {
            adapter = new CustomAdapter(getActivity(), customs);
            mListView.setAdapter(adapter);
        } else {
            adapter.list = customs;
            adapter.notifyDataSetChanged();
        }
        int index = mListView.getFirstVisiblePosition();
        if (index >= 0 && index < customs.size()) {
            title.setText(customs.get(index).getSortLetters());
        }
        titleView.setText(formatCustomNum(customs));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (view != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }
}
