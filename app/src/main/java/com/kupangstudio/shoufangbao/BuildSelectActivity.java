package com.kupangstudio.shoufangbao;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.kupangstudio.shoufangbao.model.Build;
import com.kupangstudio.shoufangbao.model.Tag;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.widget.MoreSelectPopWindow;
import com.kupangstudio.shoufangbao.widget.PullToRefreshListView;
import com.kupangstudio.shoufangbao.widget.SelectPopWindow;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by long on 15/11/11.
 * Copyright 2015 android_xiaobai.
 * 楼盘筛选
 */
public class BuildSelectActivity extends BaseActivity {

    private PullToRefreshListView refreshListView;
    private ListView lv;
    private ArrayList<Build> builds;
    private List<Build> selectBuild;
    private int cityId;
    private Map<String, Integer> mAreaId = new HashMap<>();
    private ArrayList<String> areaList = new ArrayList<>();
    private int[] area = new int[]{1, 2, 9, 22, 107, 120, 121, 162, 166, 175, 223, 228, 229, 235, 240,
            258, 275, 289, 291, 385, 438};
    private String[] areaStr;
    private SelectPopWindow areaSelect;
    private SelectPopWindow typeSelect;
    private SelectPopWindow priceSelect;
    private MoreSelectPopWindow moreSelect;
    private Button btnArea;
    private Button btnType;
    private Button btnPrice;
    private LinearLayout header;
    private int selectArea;
    private int selectType;
    private int selectPrice;
    private int selectFeature;
    private int selectLayout;
    private boolean flag;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;
    private MyAdapter mAdapter;
    private String jsonStr;
    private RelativeLayout emptyView;
    private TextView empty_text;
    private Button empty_btn;
    private Button btnMore;
    private int posParent = 0;
    private int page = 1;
    private String[] itemTag;
    private int more;
    private TextView search;
    private RelativeLayout loading;
    private boolean areaFlag;
    private boolean priceFlag;
    private boolean typeFlag;
    private boolean moreFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_select);
        CommonUtils.addActivity(this);
        CommonUtils.setTaskDone(BuildSelectActivity.this, 2);
        initView();
        init();
        initAreaSelect();
        initPriceSelect();
        initTypeSelect();
        setClickListener();
        btnMore.setEnabled(false);

        Intent intent = getIntent();
        int area = intent.getIntExtra("area", 0);
        final int price = intent.getIntExtra("price", 0);
        int type = intent.getIntExtra("type", 0);
        more = intent.getIntExtra("feature", 0);
        setAdapter();
        getTagData();
        if (area == 1) {
            btnArea.post(new Runnable() {
                @Override
                public void run() {
                    resetFlag(1);
                    areaSelect.showAsDropDown(header, 0, 1);
                }
            });
        }
        if (price == 1) {
            btnPrice.post(new Runnable() {
                @Override
                public void run() {
                    resetFlag(2);
                    priceSelect.showAsDropDown(header, 0, 1);
                }
            });
        }
        if (type == 1) {
            btnType.post(new Runnable() {
                @Override
                public void run() {
                    resetFlag(3);
                    typeSelect.showAsDropDown(header, 0, 1);
                }
            });
        }
        areaSelect.setTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                areaSelect.dismiss();
                resetFlag(5);
                return false;
            }
        });
        typeSelect.setTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                typeSelect.dismiss();
                resetFlag(5);
                return false;
            }
        });
        priceSelect.setTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                priceSelect.dismiss();
                resetFlag(5);
                return false;
            }
        });
    }

    private void initView() {
        refreshListView = (PullToRefreshListView) findViewById(R.id.buildselect_list);
        loading = (RelativeLayout) findViewById(R.id.loading_select);
        refreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        lv = refreshListView.getRefreshableView();
        btnArea = (Button) findViewById(R.id.header_select_area);
        btnType = (Button) findViewById(R.id.header_select_type);
        btnPrice = (Button) findViewById(R.id.header_select_price);
        btnMore = (Button) findViewById(R.id.header_select_more);
        header = (LinearLayout) findViewById(R.id.header_select);
        emptyView = (RelativeLayout) findViewById(R.id.emptyview);
        empty_btn = (Button) findViewById(R.id.emptyview_btn);
        empty_text = (TextView) findViewById(R.id.emptyview_text);
        search = (TextView) findViewById(R.id.navbar_title);
        ImageView left = (ImageView) findViewById(R.id.navbar_image_left);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BuildSelectActivity.this.finish();
            }
        });
        empty_text.setText("没有符合条件的房源");
        empty_btn.setVisibility(View.GONE);
        lv.setEmptyView(emptyView);
    }

    private void init() {
        cityId = (int) SPUtils.get(BuildSelectActivity.this, Constants.BUILD_CITY_ID, 1);
        selectBuild = builds = (ArrayList<Build>) getIntent().getSerializableExtra("buildList");
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Build hb = selectBuild.get(position);
                int total = hb.getBrower() + 1;
                hb.setBrower(total);
                builds.set(position, hb);
                Intent it = new Intent(BuildSelectActivity.this, BuildDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("bid", hb.getBid());
                it.putExtras(bundle);
                startActivity(it);
            }
        });
        refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;
                builds = new ArrayList<Build>();
                selectBuild = new ArrayList<Build>();
                getBuildData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                page++;
                getBuildData();
            }
        });
    }

    private void setClickListener() {
        btnArea.setOnClickListener(mClickListener);
        btnType.setOnClickListener(mClickListener);
        btnPrice.setOnClickListener(mClickListener);
        btnMore.setOnClickListener(mClickListener);
        search.setOnClickListener(mClickListener);
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            HashMap<String, String> map;
            switch (v.getId()) {
                case R.id.navbar_title:
                    Intent it = new Intent(BuildSelectActivity.this, BuildSearchActivity.class);
                    it.putExtra("cityid", (int) SPUtils.get(BuildSelectActivity.this, Constants.BUILD_CITY_ID, 1));
                    startActivity(it);
                    break;
                case R.id.header_select_area:
                    if (areaFlag) {
                        resetFlag(5);
                        areaSelect.dismiss();
                    } else {
                        resetFlag(1);
                        areaSelect.showAsDropDown(header, 0, 1);
                    }
                    if (selectArea == 0) {
                        btnArea.setSelected(false);
                    } else {
                        btnArea.setSelected(true);
                    }
                    map = new HashMap<String, String>();
                    map.put("type", "area ");
                    MobclickAgent.onEvent(getApplicationContext(), "filterbuttonclick", map);
                    break;
                case R.id.header_select_type:
                    if (typeFlag) {
                        resetFlag(5);
                        typeSelect.dismiss();
                    } else {
                        resetFlag(3);
                        typeSelect.showAsDropDown(header, 0, 1);
                    }
                    if (selectType == 0) {
                        btnType.setSelected(false);
                    } else {
                        btnType.setSelected(true);
                    }
                    map = new HashMap<String, String>();
                    map.put("type", "buildtype");
                    MobclickAgent.onEvent(BuildSelectActivity.this, "filterbuttonclick", map);
                    break;
                case R.id.header_select_price:
                    if (priceFlag) {
                        resetFlag(5);
                        priceSelect.dismiss();
                    } else {
                        resetFlag(2);
                        priceSelect.showAsDropDown(header, 0, 1);
                    }
                    if (selectPrice == 0) {
                        btnPrice.setSelected(false);
                    } else {
                        btnPrice.setSelected(true);
                    }
                    map = new HashMap<String, String>();
                    map.put("type", "layout");
                    MobclickAgent.onEvent(BuildSelectActivity.this, "filterbuttonclick", map);
                    break;
                case R.id.header_select_more:
                    if (moreFlag) {
                        resetFlag(5);
                        moreSelect.dismiss();
                    } else {
                        resetFlag(4);
                        moreSelect.showAsDropDown(header, 0, 1);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void initAreaSelect() {
        for (int i = 0; i < area.length; i++) {
            if (area[i] == cityId) {
                flag = true;
                jsonStr = CommonUtils.initCityJsonData(BuildSelectActivity.this, cityId + ".txt");
                break;
            }
        }
        if (!flag) {
            File file = new File(Constants.CACHE_PATH + File.separator + "area", cityId + ".txt");
            if (!file.exists()) {
                flag = true;
                Toast.makeText(BuildSelectActivity.this, "初始化当前城市数据失败，请稍后重试", Toast.LENGTH_SHORT)
                        .show();
                jsonStr = CommonUtils.initCityJsonData(BuildSelectActivity.this, "1.txt");
            } else {
                jsonStr = CommonUtils.readCityJsonData(file, BuildSelectActivity.this, cityId + ".txt");
            }
        }
        areaList.add("不限");
        mAreaId.put("不限", 0);
        initData();
        areaStr = areaList.toArray(new String[areaList.size()]);
        areaSelect = new SelectPopWindow(BuildSelectActivity.this, SelectPopWindow.SELECT_BUILD, areaStr);
        areaSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                resetFlag(5);
                areaSelect.setSelection(position);
                areaSelect.dismiss();
                if (position == 0) {
                    btnArea.setSelected(false);
                    btnArea.setText("区域");
                    btnArea.setTextColor(Color.parseColor("#323232"));
                } else {
                    btnArea.setSelected(true);
                    btnArea.setText(areaStr[position]);
                    btnArea.setTextColor(Color.parseColor("#be1a20"));
                }
                Iterator<String> it = mAreaId.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    if (key == areaList.get(position)) {
                        selectArea = mAreaId.get(key);
                    }
                }
                selection(selectArea, selectPrice, selectType, selectFeature, selectLayout);
            }
        });
    }

    private void initData() {
        try {
            JSONObject object = new JSONObject(jsonStr);
            JSONArray jsonC = object.getJSONArray("list");
            for (int j = 0; j < jsonC.length(); j++) {
                JSONObject jsonCity = jsonC.getJSONObject(j);
                String city = jsonCity.getString("name");// 市名字
                int cid = jsonCity.getInt("areaId");// 市id
                areaList.add(city);
                mAreaId.put(city, cid);
                JSONArray jsonAreas = null;
                try {
                    jsonAreas = jsonCity.getJSONArray("list");
                } catch (Exception e2) {
                    continue;
                }
                String[] mArea = new String[jsonAreas.length()];
                for (int k = 0; k < jsonAreas.length(); k++) {
                    JSONObject jsonArea = jsonAreas.getJSONObject(k);
                    String area = jsonArea.getString("name");
                    mArea[k] = area;
                }
            }
        } catch (JSONException e) {
            Toast.makeText(BuildSelectActivity.this, "初始化当前城市数据失败，请稍后重试", Toast.LENGTH_SHORT)
                    .show();
            jsonStr = CommonUtils.initCityJsonData(BuildSelectActivity.this, "1.txt");
        }
        jsonStr = null;
    }

    private void initTypeSelect() {
        typeSelect = new SelectPopWindow(BuildSelectActivity.this, SelectPopWindow.SELECT_BUILD, getResources().getStringArray(R.array.build_type));
        typeSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                resetFlag(5);
                typeSelect.setSelection(position);
                typeSelect.dismiss();
                switch (position) {
                    case 0:
                        selectType = 0;
                        btnType.setSelected(false);
                        btnType.setText("类型");
                        btnType.setTextColor(Color.parseColor("#323232"));
                        break;
                    case 1:
                        selectType = 1;
                        btnType.setSelected(true);
                        btnType.setText("住宅");
                        btnType.setTextColor(Color.parseColor("#be1a20"));
                        break;
                    case 2:
                        selectType = 2;
                        btnType.setSelected(true);
                        btnType.setText("公寓");
                        btnType.setTextColor(Color.parseColor("#be1a20"));
                        break;
                    case 3:
                        selectType = 3;
                        btnType.setSelected(true);
                        btnType.setText("别墅");
                        btnType.setTextColor(Color.parseColor("#be1a20"));
                        break;
                    case 4:
                        selectType = 4;
                        btnType.setSelected(true);
                        btnType.setText("商铺");
                        btnType.setTextColor(Color.parseColor("#be1a20"));
                        break;
                    case 5:
                        selectType = 5;
                        btnType.setSelected(true);
                        btnType.setText("写字楼");
                        btnType.setTextColor(Color.parseColor("#be1a20"));
                        break;
                    case 6:
                        selectType = 6;
                        btnType.setSelected(true);
                        btnType.setText("其它");
                        btnType.setTextColor(Color.parseColor("#be1a20"));
                        break;
                }
                selection(selectArea, selectPrice, selectType, selectFeature, selectLayout);
            }
        });
    }

    private void initPriceSelect() {
        priceSelect = new SelectPopWindow(BuildSelectActivity.this, SelectPopWindow.SELECT_BUILD, Constants.PRICEITEMS);
        priceSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                priceSelect.setSelection(position);
                resetFlag(5);
                priceSelect.dismiss();
                if (position == 0) {
                    selectPrice = 0;
                    btnPrice.setSelected(false);
                    btnPrice.setText("价格");
                    btnPrice.setTextColor(Color.parseColor("#323232"));
                } else {
                    selectPrice = position;
                    btnPrice.setSelected(true);
                    btnPrice.setText(Constants.PRICEITEMS[position]);
                    btnPrice.setTextColor(Color.parseColor("#be1a20"));
                }
                selection(selectArea, selectPrice, selectType, selectFeature, selectLayout);
            }
        });
    }

    private void initMoreSelect() {
        final String[] item = new String[]{"特色", "户型"};
        final ArrayList<String[]> mList = new ArrayList<>();
        if (itemTag.length > 0) {
            mList.add(itemTag);
        } else {
            mList.add(Constants.FEATUREITEMS);
        }
        mList.add(Constants.LAYOUTITEMS);
        moreSelect = new MoreSelectPopWindow(BuildSelectActivity.this, MoreSelectPopWindow.SELECT_BUILD, item, Constants.FEATUREITEMS);
        posParent = 0;
        if (selectFeature != 0 && selectLayout != 0) {
            moreSelect.setSelection(0, 0, 1, mList, selectFeature, selectLayout);
        } else if (selectFeature != 0 && selectLayout == 0) {
            moreSelect.setSelection(0, 0, 2, mList, selectFeature, selectLayout);
        } else if (selectFeature == 0 && selectLayout != 0) {
            moreSelect.setSelection(0, 0, 1, mList, selectFeature, selectLayout);
        } else {
            moreSelect.setSelection(0, 0, 2, mList, selectFeature, selectLayout);
        }
        moreSelect.setFirstSelect();
        moreSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        posParent = 0;
                        if (selectFeature != 0 && selectLayout != 0) {
                            moreSelect.setSelection(posParent, posParent, 1, mList, selectFeature, selectLayout);
                        } else if (selectFeature != 0 && selectLayout == 0) {
                            moreSelect.setSelection(posParent, posParent, 2, mList, selectFeature, selectLayout);
                        } else if (selectFeature == 0 && selectLayout != 0) {
                            moreSelect.setSelection(posParent, posParent, 1, mList, selectFeature, selectLayout);
                        } else {
                            moreSelect.setSelection(posParent, posParent, 2, mList, selectFeature, selectLayout);
                        }
                        break;
                    case 1:
                        posParent = 1;
                        if (selectFeature != 0 && selectLayout != 0) {
                            moreSelect.setSelection(posParent, 0, posParent, mList, selectFeature, selectLayout);
                        } else if (selectFeature != 0 && selectLayout == 0) {
                            moreSelect.setSelection(posParent, 0, posParent, mList, selectFeature, selectLayout);
                        } else if (selectFeature == 0 && selectLayout != 0) {
                            moreSelect.setSelection(posParent, 2, posParent, mList, selectFeature, selectLayout);
                        } else {
                            moreSelect.setSelection(posParent, 2, posParent, mList, selectFeature, selectLayout);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        moreSelect.setOnItemClickListenerChild(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                moreSelect.dismiss();
                resetFlag(5);
                switch (posParent) {
                    case 0:
                        selectFeature = position;
                        moreSelect.setSelectionChild(position);
                        if (position == 0) {
                            if (selectLayout == 0) {
                                moreSelect.setSelectionParent(posParent, 2, item[0], "");
                            } else {
                                moreSelect.setSelectionParent(posParent, 1, item[0], mList.get(1)[selectLayout]);
                            }
                        } else {
                            if (selectLayout == 0) {
                                moreSelect.setSelectionParent(posParent, 2, mList.get(0)[position], "");
                            } else {
                                moreSelect.setSelectionParent(posParent, 1, mList.get(0)[position], mList.get(1)[selectLayout]);
                            }
                        }
                        if (selectFeature == 0 && selectLayout == 0) {
                            btnMore.setSelected(false);
                            btnMore.setTextColor(Color.parseColor("#323232"));
                        } else {
                            btnMore.setSelected(true);
                            btnMore.setTextColor(Color.parseColor("#be1a20"));
                        }
                        break;
                    case 1:
                        selectLayout = position;
                        moreSelect.setSelectionChild(position);
                        if (position == 0) {
                            if (selectFeature == 0) {
                                moreSelect.setSelectionParent(2, posParent, "", item[1]);
                            } else {
                                moreSelect.setSelectionParent(0, posParent, mList.get(0)[selectFeature], item[1]);
                            }
                        } else {
                            if (selectFeature == 0) {
                                moreSelect.setSelectionParent(2, posParent, "", mList.get(1)[position]);
                            } else {
                                moreSelect.setSelectionParent(0, posParent, mList.get(0)[selectFeature], mList.get(1)[position]);
                            }
                        }
                        if (selectFeature == 0 && selectLayout == 0) {
                            btnMore.setSelected(false);
                            btnMore.setTextColor(Color.parseColor("#323232"));
                        } else {
                            btnMore.setSelected(true);
                            btnMore.setTextColor(Color.parseColor("#be1a20"));
                        }
                        break;
                    default:
                        break;
                }
                selection(selectArea, selectPrice, selectType, selectFeature, selectLayout);
            }
        });
    }

    private void selection(int area, int price, int type, int feature, int layout) {
        selectBuild = new ArrayList<Build>();
        page = 1;
        getBuildData();
    }

    /**
     * 获得标签数据
     */
    private void getTagData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "getTagList");
        map.put("module", Constants.MODULE_BUILD);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("cityid", String.valueOf((int) SPUtils.get(BuildSelectActivity.this, Constants.BUILD_CITY_ID, 1)));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new TagCallback() {

                             @Override
                             public void onError(Call call, Exception e) {
                                 Toast.makeText(BuildSelectActivity.this, ResultError.MESSAGE_NULL,
                                         Toast.LENGTH_SHORT).show();
                                 itemTag = new String[0];
                                 initMoreSelect();
                                 if (more == 1) {
                                     btnMore.post(new Runnable() {
                                         @Override
                                         public void run() {
                                             resetFlag(4);
                                             moreSelect.showAsDropDown(header, 0, 1);
                                         }
                                     });
                                 }
                                 btnMore.setEnabled(true);
                             }

                             @Override
                             public void onResponse(Result<ArrayList<Tag>> response) {
                                 if (response == null) {
                                     Toast.makeText(BuildSelectActivity.this, ResultError.MESSAGE_ERROR,
                                             Toast.LENGTH_SHORT).show();
                                     itemTag = new String[0];
                                     initMoreSelect();
                                     if (more == 1) {
                                         btnMore.post(new Runnable() {
                                             @Override
                                             public void run() {
                                                 moreSelect.showAsDropDown(header, 0, 1);
                                             }
                                         });
                                     }
                                     btnMore.setEnabled(true);

                                 } else {
                                     if (response.getCode() > 2000 && response.getContent() != null) {
                                         ArrayList<Tag> ls = response.getContent();
                                         if (ls != null && ls.size() != 0) {
                                             itemTag = new String[ls.size() + 1];
                                             itemTag[0] = "不限";
                                             for (int i = 1; i < ls.size() + 1; i++) {
                                                 itemTag[i] = ls.get(i - 1).getTag();
                                             }
                                         }
                                         initMoreSelect();
                                         if (more == 1) {
                                             btnMore.post(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     moreSelect.showAsDropDown(header, 0, 1);
                                                 }
                                             });
                                         }
                                         btnMore.setEnabled(true);
                                     } else {
                                         itemTag = new String[0];
                                         initMoreSelect();
                                         if (more == 1) {
                                             btnMore.post(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     moreSelect.showAsDropDown(header, 0, 1);
                                                 }
                                             });
                                         }
                                         btnMore.setEnabled(true);
                                     }
                                 }
                             }

                             @Override
                             public void onAfter() {
                                 super.onAfter();
                                 moreSelect.setTouchListener(new View.OnTouchListener() {
                                     @Override
                                     public boolean onTouch(View v, MotionEvent event) {
                                         moreSelect.dismiss();
                                         resetFlag(5);
                                         return false;
                                     }
                                 });
                             }
                         }
                );
    }

    private abstract class TagCallback extends Callback<Result<ArrayList<Tag>>> {
        @Override
        public Result<ArrayList<Tag>> parseNetworkResponse(Response response) throws Exception {
            Result<ArrayList<Tag>> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<ArrayList<Tag>>>() {
                        }.getType());
            } catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    /**
     * 获得楼盘列表数据
     */
    private void getBuildData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        User user = User.getInstance();
        map.put("action", "buildList");
        map.put("module", Constants.MODULE_BUILD);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        map.put("currentCity", (String) SPUtils.get(BuildSelectActivity.this, Constants.CURRENTCITY, "北京"));
        map.put("cityid", String.valueOf((int) SPUtils.get(BuildSelectActivity.this, Constants.BUILD_CITY_ID, 1)));
        map.put("lat", (String) SPUtils.get(BuildSelectActivity.this, Constants.CURRENTLAT, "39.915168"));
        map.put("lng", (String) SPUtils.get(BuildSelectActivity.this, Constants.CURRENTLNG, "116.403875"));
        map.put("address", (String) SPUtils.get(BuildSelectActivity.this, Constants.CURRENTADDR, ""));
        map.put("style", selectType + "");
        map.put("layout", selectLayout + "");
        map.put("totalPrice", selectPrice + "");
        if (itemTag.length > 0) {
            if (!itemTag[selectFeature].equals("不限")) {
                map.put("tag", itemTag[selectFeature]);
            } else {
                map.put("tag", "");
            }
        } else {
            if (!Constants.FEATUREITEMS[selectFeature].equals("不限")) {
                map.put("tag", Constants.FEATUREITEMS[selectFeature]);
            } else {
                map.put("tag", "");
            }
        }
        map.put("areaid", selectArea + "");
        map.put("page", String.valueOf(page));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new BuildCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        refreshListView.onRefreshComplete();
                        if (page == 1) {
                            Toast.makeText(BuildSelectActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        } else {
                            page--;
                            Toast.makeText(BuildSelectActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onResponse(Result<ArrayList<Build>> response) {
                        if (response == null) {
                            Toast.makeText(BuildSelectActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            if (page == 1) {
                                Toast.makeText(BuildSelectActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                            } else {
                                page--;
                                Toast.makeText(BuildSelectActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                        refreshListView.onRefreshComplete();
                        if (response.getCode() > Result.RESULT_OK) {
                            selectBuild.addAll(response.getContent());
                        } else {
                            if (page == 1) {
                                Toast.makeText(BuildSelectActivity.this, "暂无更多楼盘", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(BuildSelectActivity.this, "暂无更多楼盘", Toast.LENGTH_SHORT).show();
                                page--;
                            }
                        }
                    }

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        if (page == 1) {
                            loading.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        loading.setVisibility(View.GONE);
                        setAdapter();
                    }
                });
    }

    private abstract class BuildCallback extends Callback<Result<ArrayList<Build>>> {
        @Override
        public Result<ArrayList<Build>> parseNetworkResponse(Response response) throws Exception {
            Result<ArrayList<Build>> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<ArrayList<Build>>>() {
                        }.getType());
            } catch (Exception e) {
                return null;
            }
            return result;
        }
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new MyAdapter(BuildSelectActivity.this, selectBuild);
            lv.setAdapter(mAdapter);
        } else {
            mAdapter.list = selectBuild;
            mAdapter.notifyDataSetChanged();
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

    class MyAdapter extends BaseAdapter {

        List<Build> list;
        LayoutInflater inflater;

        public MyAdapter(Context ctx, List<Build> list) {
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
            ViewHolder holder = null;
            Build build = list.get(position);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_buildfragment_list, parent, false);
                holder.buildImage = (ImageView) convertView.findViewById(R.id.item_build_image);
                holder.browseNum = (TextView) convertView.findViewById(R.id.item_build_browse_num);
                holder.buildHongbao = (ImageView) convertView.findViewById(R.id.item_build_hongbao);
                holder.buildAngle = (ImageView) convertView.findViewById(R.id.item_build_angle);
                holder.district = (TextView) convertView.findViewById(R.id.item_build_district);
                holder.name = (TextView) convertView.findViewById(R.id.item_build_name);
                holder.price = (TextView) convertView.findViewById(R.id.item_build_price);
                holder.commission = (TextView) convertView.findViewById(R.id.item_build_commission);
                holder.distanceLl = (LinearLayout) convertView.findViewById(R.id.item_build_diatance);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.name.setText(build.getName());
            holder.district.setText("[" + build.getArea() + "]");
            holder.price.setText(build.getPrice());
            if (User.getInstance().verify == User.USER_THROUGH) {
                holder.commission.setText(build.getCommission());
            } else {
                holder.commission.setText("认证可见");
            }
            int distance = 0;
            if (build.getDistance() == null || build.getDistance().equals("") || build.getDistance().equals("0")
                    || Integer.parseInt(build.getDistance()) >= 100000) {
                holder.distanceLl.setVisibility(View.GONE);
            } else {
                holder.distanceLl.setVisibility(View.VISIBLE);
                distance = Integer.parseInt(build.getDistance());
            }
            DecimalFormat df2 = new DecimalFormat("#0.00");//这样为保持2位
            holder.browseNum.setText("距您" + df2.format(distance / 1000.0) + "km");
            switch (build.getLabel()) {
                case Build.NEW:
                    if (build.getUnique_business() == 1) {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.unique_o2o);
                    } else {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.build_angle_new);
                    }
                    break;
                case Build.JIAN:
                    if (build.getUnique_business() == 1) {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.unique_o2o);
                    } else {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.build_angle_jian);
                    }
                    break;
                case Build.RED_PACKETS:
                case Build.DAIKAN_PACKET:
                    holder.buildAngle.setVisibility(View.VISIBLE);
                    holder.buildAngle.setImageResource(R.drawable.build_angle_packet);
                    break;
                default:
                    if (build.getUnique_business() == 1) {
                        holder.buildAngle.setVisibility(View.VISIBLE);
                        holder.buildAngle.setImageResource(R.drawable.unique_o2o);
                    } else {
                        holder.buildAngle.setVisibility(View.GONE);
                    }
                    break;
            }

            imageLoader.displayImage(build.getPic(), holder.buildImage, options);
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView buildImage;
        TextView browseNum;
        ImageView buildHongbao;
        ImageView buildAngle;
        TextView district;
        TextView name;
        TextView price;
        TextView commission;
        LinearLayout distanceLl;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    private void resetFlag(int type) {
        switch (type) {
            case 1:
                areaFlag = true;
                priceFlag = false;
                typeFlag = false;
                moreFlag = false;
                break;
            case 2:
                areaFlag = false;
                priceFlag = true;
                typeFlag = false;
                moreFlag = false;
                break;
            case 3:
                areaFlag = false;
                priceFlag = false;
                typeFlag = true;
                moreFlag = false;
                break;
            case 4:
                areaFlag = false;
                priceFlag = false;
                typeFlag = false;
                moreFlag = true;
                break;
            case 5:
                areaFlag = false;
                priceFlag = false;
                typeFlag = false;
                moreFlag = false;
                break;
            default:
                break;
        }
    }
}
