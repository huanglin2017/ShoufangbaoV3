package com.kupangstudio.shoufangbao.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.kupangstudio.shoufangbao.CommunityDetailActivity;
import com.kupangstudio.shoufangbao.ImageDetailActivity;
import com.kupangstudio.shoufangbao.PointMallActivity;
import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.base.BaseFragment;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.ReportBuild;
import com.kupangstudio.shoufangbao.model.Scene;
import com.kupangstudio.shoufangbao.model.ScenePic;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.DensityUtils;
import com.kupangstudio.shoufangbao.utils.FileUtils;
import com.kupangstudio.shoufangbao.utils.ImageUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.utils.TimeUtils;
import com.kupangstudio.shoufangbao.widget.PullToRefreshListView;
import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.pickerview.lib.DensityUtil;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.system.text.ShortMessage;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import okhttp3.Call;
import okhttp3.Request;

/**
 * 新社区界面
 */
public class SceneFragment extends BaseFragment {

    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private PullToRefreshListView refreshListView;
    private ListView mLv;
    private List<Scene> data;
    private SceneAdapter mAdapter;
    private int currentCityId;
    /**
     * 标志位，标志已经初始化完成
     */
    private boolean isPrepared;
    /**
     * 是否已被加载过一次，第二次就不再去请求数据了
     */
    private boolean mHasLoadedOnce;
    private int page = 1;
    private RelativeLayout loading;
    private ArrayList<String> picList = new ArrayList<String>();
    private long startTime;
    private long endTime;

    public SceneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ret = null;
        ret = inflater.inflate(R.layout.fragment_scene, container, false);
        initView(ret);
        data = new ArrayList<Scene>();
        currentCityId = (int) SPUtils.get(getActivity(), Constants.BUILD_CITY_ID, 1);
        refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;
                data = new ArrayList<Scene>();
                getSceneData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                page++;
                getSceneData();
            }
        });
        isPrepared = true;
        lazyLoad();
        return ret;
    }

    private void initView(View view) {
        TextView title = (TextView) view.findViewById(R.id.navbar_title);
        title.setText("V动态");
        refreshListView = (PullToRefreshListView) view.findViewById(R.id.scene_list);
        refreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mLv = refreshListView.getRefreshableView();
        mLv.setSelector(R.color.transparent);
        loading = (RelativeLayout) view.findViewById(R.id.loading_scene);
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new SceneAdapter(data, getActivity());
            mLv.setAdapter(mAdapter);
        } else {
            mAdapter.list = data;
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible || mHasLoadedOnce) {
            return;
        }
        getSceneData();
    }

    class SceneAdapter extends BaseAdapter {
        private List<Scene> list;
        private LayoutInflater inflater;
        private final static int ONLY_IMAGE = 0;
        private final static int THREE_IMAGE_CONTENT = 1;
        private final static int TWO_IMAGE_CONTENT = 2;
        private final static int ONE_IMAGE_CONTENT = 3;
        private final static int ONLY_CONTENT = 4;
        private final static int ONLY_LINK = 5;

        public SceneAdapter(List<Scene> list, Context context) {
            this.list = list;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public int getViewTypeCount() {
            return 6;
        }

        @Override
        public int getItemViewType(int position) {
            int ret = 0;
            if (list != null && list.size() > 0) {
                ret = list.get(position).getType();
            }
            return ret;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder1 holder1 = null;
            ViewHolder2 holder2 = null;
            ViewHolder3 holder3 = null;
            ViewHolder4 holder4 = null;
            ViewHolder5 holder5 = null;
            ViewHolder6 holder6 = null;
            int type = getItemViewType(position);
            final Scene scene = list.get(position);
            if (convertView == null) {
                switch (type) {
                    case ONLY_IMAGE:
                        convertView = inflater.inflate(R.layout.item_scene_1, parent, false);
                        holder1 = new ViewHolder1();
                        holder1.firstHead = (ImageView) convertView.findViewById(R.id.item_scene1_head);
                        holder1.firstName = (TextView) convertView.findViewById(R.id.item_scene1_name);
                        holder1.firstRecommend = (ImageView) convertView.findViewById(R.id.item_scene1_recommend);
                        holder1.firstTime = (TextView) convertView.findViewById(R.id.item_scene1_time);
                        holder1.firstShare = (ImageView) convertView.findViewById(R.id.item_scene1_share);
                        holder1.firstImg1 = (ImageView) convertView.findViewById(R.id.item_scene1_showimg);
                        convertView.setTag(holder1);
                        break;
                    case THREE_IMAGE_CONTENT:
                        convertView = inflater.inflate(R.layout.item_scene_2, parent, false);
                        holder2 = new ViewHolder2();
                        holder2.twoHead = (ImageView) convertView.findViewById(R.id.item_scene2_head);
                        holder2.twoName = (TextView) convertView.findViewById(R.id.item_scene2_name);
                        holder2.twoRecommend = (ImageView) convertView.findViewById(R.id.item_scene2_recommend);
                        holder2.twoTime = (TextView) convertView.findViewById(R.id.item_scene2_time);
                        holder2.twoShare = (ImageView) convertView.findViewById(R.id.item_scene2_share);
                        holder2.twoContent = (TextView) convertView.findViewById(R.id.item_scene2_content);
                        holder2.twoImg1 = (ImageView) convertView.findViewById(R.id.item_scene2_showimg1);
                        holder2.twoImg2 = (ImageView) convertView.findViewById(R.id.item_scene2_showimg2);
                        holder2.twoImg3 = (ImageView) convertView.findViewById(R.id.item_scene2_showimg3);
                        convertView.setTag(holder2);
                        break;
                    case TWO_IMAGE_CONTENT:
                        convertView = inflater.inflate(R.layout.item_scene_3, parent, false);
                        holder3 = new ViewHolder3();
                        holder3.threeHead = (ImageView) convertView.findViewById(R.id.item_scene3_head);
                        holder3.threeName = (TextView) convertView.findViewById(R.id.item_scene3_name);
                        holder3.threeRecommend = (ImageView) convertView.findViewById(R.id.item_scene3_recommend);
                        holder3.threeTime = (TextView) convertView.findViewById(R.id.item_scene3_time);
                        holder3.threeShare = (ImageView) convertView.findViewById(R.id.item_scene3_share);
                        holder3.threeContent = (TextView) convertView.findViewById(R.id.item_scene3_content);
                        holder3.threeImg1 = (ImageView) convertView.findViewById(R.id.item_scene3_showimg1);
                        holder3.threeImg2 = (ImageView) convertView.findViewById(R.id.item_scene3_showimg2);
                        convertView.setTag(holder3);
                        break;
                    case ONE_IMAGE_CONTENT:
                        convertView = inflater.inflate(R.layout.item_scene_4, parent, false);
                        holder4 = new ViewHolder4();
                        holder4.fourHead = (ImageView) convertView.findViewById(R.id.item_scene4_head);
                        holder4.fourName = (TextView) convertView.findViewById(R.id.item_scene4_name);
                        holder4.fourRecommend = (ImageView) convertView.findViewById(R.id.item_scene4_recommend);
                        holder4.fourTime = (TextView) convertView.findViewById(R.id.item_scene4_time);
                        holder4.fourShare = (ImageView) convertView.findViewById(R.id.item_scene4_share);
                        holder4.fourContent = (TextView) convertView.findViewById(R.id.item_scene4_content);
                        holder4.fourImg1 = (ImageView) convertView.findViewById(R.id.item_scene4_showimg1);
                        convertView.setTag(holder4);
                        break;
                    case ONLY_CONTENT:
                        convertView = inflater.inflate(R.layout.item_scene_5, parent, false);
                        holder5 = new ViewHolder5();
                        holder5.fiveHead = (ImageView) convertView.findViewById(R.id.item_scene5_head);
                        holder5.fiveName = (TextView) convertView.findViewById(R.id.item_scene5_name);
                        holder5.fiveRecommend = (ImageView) convertView.findViewById(R.id.item_scene5_recommend);
                        holder5.fiveTime = (TextView) convertView.findViewById(R.id.item_scene5_time);
                        holder5.fiveShare = (ImageView) convertView.findViewById(R.id.item_scene5_share);
                        holder5.fiveContent = (TextView) convertView.findViewById(R.id.item_scene5_content);
                        convertView.setTag(holder5);
                        break;
                    case ONLY_LINK:
                        convertView = inflater.inflate(R.layout.item_scene_6, parent, false);
                        holder6 = new ViewHolder6();
                        holder6.sixHead = (ImageView) convertView.findViewById(R.id.item_scene6_head);
                        holder6.sixName = (TextView) convertView.findViewById(R.id.item_scene6_name);
                        holder6.sixRecommend = (ImageView) convertView.findViewById(R.id.item_scene6_recommend);
                        holder6.sixTime = (TextView) convertView.findViewById(R.id.item_scene6_time);
                        holder6.sixShare = (ImageView) convertView.findViewById(R.id.item_scene6_share);
                        holder6.sixImg1 = (ImageView) convertView.findViewById(R.id.item_scene6_showimg);
                        holder6.sixContent = (TextView) convertView.findViewById(R.id.item_scene6_content);
                        holder6.sixLink = (LinearLayout) convertView.findViewById(R.id.item_scene6_link);
                        convertView.setTag(holder6);
                        break;
                    default:
                        break;
                }
            } else {
                switch (type) {
                    case ONLY_IMAGE:
                        holder1 = (ViewHolder1) convertView.getTag();
                        break;
                    case THREE_IMAGE_CONTENT:
                        holder2 = (ViewHolder2) convertView.getTag();
                        break;
                    case TWO_IMAGE_CONTENT:
                        holder3 = (ViewHolder3) convertView.getTag();
                        break;
                    case ONE_IMAGE_CONTENT:
                        holder4 = (ViewHolder4) convertView.getTag();
                        break;
                    case ONLY_CONTENT:
                        holder5 = (ViewHolder5) convertView.getTag();
                        break;
                    case ONLY_LINK:
                        holder6 = (ViewHolder6) convertView.getTag();
                        break;
                    default:
                        break;
                }
            }
            switch (type) {
                case ONLY_IMAGE:
                    holder1.firstName.setText(scene.getRealname());
                    if (scene.getCreate_type() == 1) {
                        holder1.firstName.getPaint().setFakeBoldText(true);
                        holder1.firstName.setTextColor(getResources().getColor(R.color.common_select));
                        imageLoader.displayImage(scene.getFace(), holder1.firstHead, options);
                    } else {
                        holder1.firstName.getPaint().setFakeBoldText(false);
                        holder1.firstName.setTextColor(getResources().getColor(R.color.small_title));
                        imageLoader.displayImage(scene.getFace(), holder1.firstHead, options);
                    }
                    holder1.firstTime.setText(TimeUtils.getCustomReportData(scene.getCtime() * 1000));
                    imageLoader.displayImage(scene.getPicture().get(0).getUrl(), holder1.firstImg1, options);
                    if (scene.getIs_recommend() == Scene.ISRECOMMEND) {
                        holder1.firstRecommend.setVisibility(View.VISIBLE);
                    } else {
                        holder1.firstRecommend.setVisibility(View.GONE);
                    }
                    if (scene.getPicture().size() == 1) {
                        holder1.firstShare.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showShare("", "", "", scene.getPicture().get(0).getUrl(), 2);
                            }
                        });
                        holder1.firstImg1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
                                picList.clear();
                                for (int i = 0; i < scene.getPicture().size(); i++) {
                                    picList.add(scene.getPicture().get(i).getUrl());
                                }
                                intent.putStringArrayListExtra("url", picList);
                                intent.putExtra("position", 0);
                                startActivity(intent);
                            }
                        });
                    }
                    break;
                case THREE_IMAGE_CONTENT:
                    holder2.twoName.setText(scene.getRealname());
                    if (scene.getCreate_type() == 1) {
                        holder2.twoName.getPaint().setFakeBoldText(true);
                        holder2.twoName.setTextColor(getResources().getColor(R.color.common_select));
                        imageLoader.displayImage(scene.getFace(), holder2.twoHead, options);
                    } else {
                        holder2.twoName.getPaint().setFakeBoldText(false);
                        holder2.twoName.setTextColor(getResources().getColor(R.color.small_title));
                        imageLoader.displayImage(scene.getFace(), holder2.twoHead, options);
                    }
                    holder2.twoTime.setText(TimeUtils.getCustomReportData(scene.getCtime() * 1000));
                    if (scene.getIs_recommend() == Scene.ISRECOMMEND) {
                        holder2.twoRecommend.setVisibility(View.VISIBLE);
                    } else {
                        holder2.twoRecommend.setVisibility(View.GONE);
                    }
                    holder2.twoShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showShare(scene.getTitle(), scene.getContent(), scene.getUrl(), scene.getPicture().get(0).getUrl(), 1);
                        }
                    });
                    holder2.twoContent.setText(scene.getContent());
                    imageLoader.displayImage(scene.getPicture().get(0).getUrl(), holder2.twoImg1, options);
                    imageLoader.displayImage(scene.getPicture().get(1).getUrl(), holder2.twoImg2, options);
                    imageLoader.displayImage(scene.getPicture().get(2).getUrl(), holder2.twoImg3, options);
                    holder2.twoImg1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
                            picList.clear();
                            for (int i = 0; i < scene.getPicture().size(); i++) {
                                picList.add(scene.getPicture().get(i).getUrl());
                            }
                            intent.putStringArrayListExtra("url", picList);
                            intent.putExtra("position", 0);
                            startActivity(intent);
                        }
                    });
                    holder2.twoImg2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
                            picList.clear();
                            for (int i = 0; i < scene.getPicture().size(); i++) {
                                picList.add(scene.getPicture().get(i).getUrl());
                            }
                            intent.putStringArrayListExtra("url", picList);
                            intent.putExtra("position", 1);
                            startActivity(intent);
                        }
                    });
                    holder2.twoImg3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
                            picList.clear();
                            for (int i = 0; i < scene.getPicture().size(); i++) {
                                picList.add(scene.getPicture().get(i).getUrl());
                            }
                            intent.putStringArrayListExtra("url", picList);
                            intent.putExtra("position", 2);
                            startActivity(intent);
                        }
                    });
                    break;
                case TWO_IMAGE_CONTENT:
                    holder3.threeName.setText(scene.getRealname());
                    if (scene.getCreate_type() == 1) {
                        holder3.threeName.getPaint().setFakeBoldText(true);
                        holder3.threeName.setTextColor(getResources().getColor(R.color.common_select));
                        imageLoader.displayImage(scene.getFace(), holder3.threeHead, options);
                    } else {
                        holder3.threeName.getPaint().setFakeBoldText(false);
                        holder3.threeName.setTextColor(getResources().getColor(R.color.small_title));
                        imageLoader.displayImage(scene.getFace(), holder3.threeHead, options);
                    }
                    holder3.threeTime.setText(TimeUtils.getCustomReportData(scene.getCtime() * 1000));
                    if (scene.getIs_recommend() == Scene.ISRECOMMEND) {
                        holder3.threeRecommend.setVisibility(View.VISIBLE);
                    } else {
                        holder3.threeRecommend.setVisibility(View.GONE);
                    }
                    holder3.threeShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showShare(scene.getTitle(), scene.getContent(), scene.getUrl(), scene.getPicture().get(0).getUrl(), 1);
                        }
                    });
                    holder3.threeContent.setText(scene.getContent());
                    imageLoader.displayImage(scene.getPicture().get(0).getUrl(), holder3.threeImg1, options);
                    imageLoader.displayImage(scene.getPicture().get(1).getUrl(), holder3.threeImg2, options);
                    holder3.threeImg1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
                            picList.clear();
                            for (int i = 0; i < scene.getPicture().size(); i++) {
                                picList.add(scene.getPicture().get(i).getUrl());
                            }
                            intent.putStringArrayListExtra("url", picList);
                            intent.putExtra("position", 0);
                            startActivity(intent);
                        }
                    });
                    holder3.threeImg2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
                            picList.clear();
                            for (int i = 0; i < scene.getPicture().size(); i++) {
                                picList.add(scene.getPicture().get(i).getUrl());
                            }
                            intent.putStringArrayListExtra("url", picList);
                            intent.putExtra("position", 1);
                            startActivity(intent);
                        }
                    });
                    break;
                case ONE_IMAGE_CONTENT:
                    holder4.fourName.setText(scene.getRealname());
                    if (scene.getCreate_type() == 1) {
                        holder4.fourName.getPaint().setFakeBoldText(true);
                        holder4.fourName.setTextColor(getResources().getColor(R.color.common_select));
                        imageLoader.displayImage(scene.getFace(), holder4.fourHead, options);
                    } else {
                        holder4.fourName.getPaint().setFakeBoldText(false);
                        holder4.fourName.setTextColor(getResources().getColor(R.color.small_title));
                        imageLoader.displayImage(scene.getFace(), holder4.fourHead, options);
                    }
                    holder4.fourTime.setText(TimeUtils.getCustomReportData(scene.getCtime() * 1000));
                    if (scene.getIs_recommend() == Scene.ISRECOMMEND) {
                        holder4.fourRecommend.setVisibility(View.VISIBLE);
                    } else {
                        holder4.fourRecommend.setVisibility(View.GONE);
                    }
                    if (scene.getPicture().size() == 1) {
                        holder4.fourShare.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showShare(scene.getTitle(), scene.getContent(), scene.getUrl(), scene.getPicture().get(0).getUrl(), 1);
                            }
                        });
                        holder4.fourContent.setText(scene.getContent());
                        imageLoader.displayImage(scene.getPicture().get(0).getUrl(), holder4.fourImg1, options);
                        holder4.fourImg1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
                                picList.clear();
                                for (int i = 0; i < scene.getPicture().size(); i++) {
                                    picList.add(scene.getPicture().get(i).getUrl());
                                }
                                intent.putStringArrayListExtra("url", picList);
                                intent.putExtra("position", 0);
                                startActivity(intent);
                            }
                        });
                    }
                    break;
                case ONLY_CONTENT:
                    holder5.fiveName.setText(scene.getRealname());
                    if (scene.getCreate_type() == 1) {
                        holder5.fiveName.getPaint().setFakeBoldText(true);
                        holder5.fiveName.setTextColor(getResources().getColor(R.color.common_select));
                        imageLoader.displayImage(scene.getFace(), holder5.fiveHead, options);
                    } else {
                        holder5.fiveName.getPaint().setFakeBoldText(false);
                        holder5.fiveName.setTextColor(getResources().getColor(R.color.small_title));
                        imageLoader.displayImage(scene.getFace(), holder5.fiveHead, options);
                    }
                    holder5.fiveTime.setText(TimeUtils.getCustomReportData(scene.getCtime() * 1000));
                    if (scene.getIs_recommend() == Scene.ISRECOMMEND) {
                        holder5.fiveRecommend.setVisibility(View.VISIBLE);
                    } else {
                        holder5.fiveRecommend.setVisibility(View.GONE);
                    }
                    holder5.fiveShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showShare(scene.getTitle(), scene.getContent(), "", "", 3);
                        }
                    });
                    holder5.fiveContent.setText(scene.getContent());
                    break;
                case ONLY_LINK:
                    holder6.sixName.setText(scene.getRealname());
                    if (scene.getCreate_type() == 1) {
                        holder6.sixName.setTextColor(getResources().getColor(R.color.common_select));
                        holder6.sixName.getPaint().setFakeBoldText(true);
                        imageLoader.displayImage(scene.getFace(), holder6.sixHead, options);
                    } else {
                        holder6.sixName.getPaint().setFakeBoldText(false);
                        holder6.sixName.setTextColor(getResources().getColor(R.color.small_title));
                        imageLoader.displayImage(scene.getFace(), holder6.sixHead, options);
                    }
                    holder6.sixTime.setText(TimeUtils.getCustomReportData(scene.getCtime() * 1000));
                    if (scene.getIs_recommend() == Scene.ISRECOMMEND) {
                        holder6.sixRecommend.setVisibility(View.VISIBLE);
                    } else {
                        holder6.sixRecommend.setVisibility(View.GONE);
                    }
                    holder6.sixShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showShare(scene.getTitle(), scene.getContent(),
                                    scene.getUrl() + "&sfb=1" + "&uid=" + User.getInstance().uid,
                                    scene.getPicture().get(0).getUrl(), 1);
                        }
                    });
                    holder6.sixContent.setText(scene.getContent());
                    imageLoader.displayImage(scene.getPicture().get(0).getUrl(), holder6.sixImg1, options);
                    holder6.sixLink.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), CommunityDetailActivity.class);
                            intent.putExtra("url", scene.getUrl() + "&uid=" + User.getInstance().uid);
                            intent.putExtra("image", scene.getPicture().get(0));
                            intent.putExtra("content", scene.getContent());
                            intent.putExtra("title", scene.getTitle());
                            intent.putExtra("newsid", "1");
                            startActivity(intent);
                        }
                    });
                    break;
                default:
                    break;
            }
            return convertView;
        }

        public class ViewHolder1 {
            ImageView firstHead;
            TextView firstName;
            ImageView firstImg1;
            TextView firstTime;
            ImageView firstShare;
            ImageView firstRecommend;
        }

        public class ViewHolder2 {
            ImageView twoHead;
            TextView twoName;
            TextView twoContent;
            ImageView twoImg1;
            ImageView twoImg2;
            ImageView twoImg3;
            TextView twoTime;
            ImageView twoShare;
            ImageView twoRecommend;
        }

        public class ViewHolder3 {
            ImageView threeHead;
            TextView threeName;
            TextView threeContent;
            ImageView threeImg1;
            ImageView threeImg2;
            TextView threeTime;
            ImageView threeShare;
            ImageView threeRecommend;
        }

        public class ViewHolder4 {
            ImageView fourHead;
            TextView fourName;
            TextView fourContent;
            ImageView fourImg1;
            TextView fourTime;
            ImageView fourShare;
            ImageView fourRecommend;
        }

        public class ViewHolder5 {
            ImageView fiveHead;
            TextView fiveName;
            TextView fiveContent;
            TextView fiveTime;
            ImageView fiveShare;
            ImageView fiveRecommend;
        }

        public class ViewHolder6 {
            ImageView sixHead;
            TextView sixName;
            LinearLayout sixLink;
            TextView sixContent;
            ImageView sixImg1;
            TextView sixTime;
            ImageView sixShare;
            ImageView sixRecommend;
        }
    }

    //获取圈子列表
    private void getSceneData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getConsultList");
        map.put("module", Constants.MODULE_COMMUNITY);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        map.put("cityid", String.valueOf(currentCityId));
        map.put("page", String.valueOf(page));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        if (!mHasLoadedOnce) {
                            loading.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (page == 1) {
                            data = DataSupport.findAll(Scene.class);
                            for (int i = 0; i < data.size(); i++) {
                                Scene scene = data.get(i);
                                List<ScenePic> scenePicList = DataSupport.where("scene_id = ?", String.valueOf(scene.getId())).find(ScenePic.class);
                                scene.setPicture(scenePicList);
                            }
                        }
                    }

                    @Override
                    public void onResponse(String response) {
                        parseJson(response);
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        refreshListView.onRefreshComplete();
                        setAdapter();
                        mHasLoadedOnce = true;
                        loading.setVisibility(View.GONE);
                    }
                });
    }

    private void parseJson(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            int code = jsonObject.getInt("code");
            if (code < Result.RESULT_OK) {
                String notice = jsonObject.getString("notice");
                Toast.makeText(getActivity(), notice, Toast.LENGTH_SHORT).show();
            } else {
                JSONArray json = jsonObject.getJSONArray("content");
                for (int i = 0; i < json.length(); i++) {
                    final Scene scene = new Scene();
                    ArrayList<ScenePic> pic = new ArrayList<ScenePic>();
                    JSONObject object = json.getJSONObject(i);
                    if (!object.isNull("create_type")) {
                        scene.setCreate_type(object.getInt("create_type"));
                    }
                    if (!object.isNull("face")) {
                        scene.setFace(object.getString("face"));
                    }
                    if (!object.isNull("realname")) {
                        scene.setRealname(object.getString("realname"));
                    }
                    if (!object.isNull("is_recommend")) {
                        scene.setIs_recommend(object.getInt("is_recommend"));
                    }
                    if (!object.isNull("consult_type")) {
                        scene.setConsult_type(object.getInt("consult_type"));
                    }
                    if (!object.isNull("content")) {
                        scene.setContent(object.getString("content"));
                    }
                    if (!object.isNull("title")) {
                        scene.setTitle(object.getString("title"));
                    }
                    if (!object.isNull("picture")) {
                        JSONArray array = object.getJSONArray("picture");
                        for (int j = 0; j < array.length(); j++) {
                            ScenePic scenePic = new ScenePic();
                            scenePic.setUrl(array.getString(j));
                            scenePic.setPosition(j);
                            pic.add(scenePic);
                        }
                        scene.setPicture(pic);
                    }
                    if (!object.isNull("url")) {
                        scene.setUrl(object.getString("url"));
                    }
                    if (!object.isNull("ctime")) {
                        scene.setCtime(object.getLong("ctime"));
                    }
                    if (scene.getConsult_type() == 2) {
                        //链接
                        scene.setType(5);
                    } else {
                        switch (scene.getPicture().size()) {
                            case 0:
                                scene.setType(4);
                                break;
                            case 1:
                                scene.setType(3);
                                break;
                            case 2:
                                scene.setType(2);
                                break;
                            case 3:
                                scene.setType(1);
                                break;
                            default:
                                break;
                        }
                    }
                    if ((scene.getContent() == null || scene.getContent().equals(""))
                            && scene.getPicture().size() > 0) {
                        scene.setType(0);
                    }
                    data.add(scene);
                }
                DataSupport.deleteAll(Scene.class);
                for (int i = 0; i < data.size(); i++) {
                    DataSupport.saveAll(data.get(i).getPicture());
                    data.get(i).save();
                }
            }
        } catch (JSONException e) {
            Toast.makeText(getActivity(), ResultError.MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
            if (page == 1) {
                data = DataSupport.findAll(Scene.class);
                for (int i = 0; i < data.size(); i++) {
                    Scene scene = data.get(i);
                    List<ScenePic> scenePicList = DataSupport.where("scene_id = ?", String.valueOf(scene.getId())).find(ScenePic.class);
                    scene.setPicture(scenePicList);
                }
            }
        }
    }

    private void showShare(final String title, final String content, String shareUrl, final String imageUrl, int type) {
        ShareSDK.initSDK(getActivity());
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        oks.addHiddenPlatform(ShortMessage.NAME);
        oks.addHiddenPlatform(QZone.NAME);
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                if (platform.getName().equals(Wechat.NAME)) {
                    paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
                }
            }
        });

        switch (type) {
            case 1: //正常分享
                shareUrl = shareUrl + "&sfb=1";
                oks.setTitle(title);
                oks.setTitleUrl(shareUrl);
                oks.setText(content);
                oks.setUrl(shareUrl);
                oks.setImageUrl(imageUrl);
                oks.setSite(getString(R.string.app_name));
                oks.setDialogMode();
                oks.setSiteUrl(shareUrl);
                break;
            case 2://图片
                oks.setImageUrl(imageUrl);
                oks.setSite(getString(R.string.app_name));
                oks.setDialogMode();
                break;
            case 3://纯文本
                oks.setText(content);
                oks.setSite(getString(R.string.app_name));
                oks.setDialogMode();
                break;
            default:
                break;
        }
        oks.show(getActivity());
        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                addIntegrate(2);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {

            }

            @Override
            public void onCancel(Platform platform, int i) {

            }
        });
    }

    /**
     * 添加积分
     */
    private void addIntegrate(int type) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "addIntegrate");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("type", String.valueOf(type));
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

    @Override
    public void onResume() {
        super.onResume();
        if (currentCityId != (int) SPUtils.get(getActivity(), Constants.BUILD_CITY_ID, 1)) {
            currentCityId = (int) SPUtils.get(getActivity(), Constants.BUILD_CITY_ID, 1);
            page = 1;
            data = new ArrayList<Scene>();
            getSceneData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        endTime = System.currentTimeMillis();
        int duration = (int) (endTime - startTime);
        MobclickAgent.onEventValue(getActivity(), "vcircletime", null, duration);
    }
}
