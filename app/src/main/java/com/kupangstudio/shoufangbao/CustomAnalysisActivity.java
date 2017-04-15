package com.kupangstudio.shoufangbao;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Custom;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

import org.litepal.crud.DataSupport;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jsmi on 2015/11/4.
 * 客户分析
 */
public class CustomAnalysisActivity extends Activity implements View.OnClickListener {

    private PieChart mChart;
    private int[] mColor;
    private ArrayList<Custom> customs;
    private Button[] mTabs;
    private Button btnGender;//性别
    private TextView labelOne;
    private LinearLayout layoutOne;
    private Button btnStrength;//强度
    private LinearLayout layoutTwo;
    private TextView labelTwo;
    private Button btnLayout;//户型
    private TextView labelThree;
    private LinearLayout layoutThree;
    private Button btnSize;//面积
    private TextView labelFour;
    private LinearLayout layoutFour;
    private Button btnPrice;//价格
    private TextView labelFive;
    private LinearLayout layoutFive;
    private Button btnType;//类别
    private TextView labelSix;
    private LinearLayout layoutSix;
    private TextView labelSeven;
    private LinearLayout layoutSeven;
    private TextView labelEight;
    private LinearLayout layoutEight;
    private int currentPos;
    private int pos;
    private TextView[] tv;
    private LinearLayout[] layout;
    private ImageView left;
    private ImageView right;
    private LinearLayout centerLayout;//中心
    private TextView tvTotal, tvType;
    private Handler mHandler;
    private long beginTime;
    private long endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_analysis);
        CommonUtils.addActivity(this);
        beginTime = System.currentTimeMillis();
        //任务机制
        CommonUtils.setTaskDone(this, 24);
        initView();
        setClickListener();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        centerLayout.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        CommonUtils.handleTitleBarRightGone(CustomAnalysisActivity.this, "统计分析");
                        tvTotal.setText("状态统计共" + customs.size() + "人");
                        calStrength();
                        break;
                    default:
                        break;
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                customs = (ArrayList<Custom>) DataSupport.where("uid = ?",
                        String.valueOf(User.getInstance().uid)).find(Custom.class);
                mHandler.sendEmptyMessage(1);
            }
        }).start();
        currentPos = pos = 1;
        mTabs = new Button[6];
        mTabs[0] = btnGender;
        mTabs[1] = btnStrength;
        mTabs[2] = btnLayout;
        mTabs[3] = btnSize;
        mTabs[4] = btnPrice;
        mTabs[5] = btnType;
        tv = new TextView[8];
        tv[0] = labelOne;
        tv[1] = labelTwo;
        tv[2] = labelThree;
        tv[3] = labelFour;
        tv[4] = labelFive;
        tv[5] = labelSix;
        tv[6] = labelSeven;
        tv[7] = labelEight;
        layout = new LinearLayout[8];
        layout[0] = layoutOne;
        layout[1] = layoutTwo;
        layout[2] = layoutThree;
        layout[3] = layoutFour;
        layout[4] = layoutFive;
        layout[5] = layoutSix;
        layout[6] = layoutSeven;
        layout[7] = layoutEight;
        mTabs[currentPos].setSelected(true);
        initColor();
        mChart = (PieChart) findViewById(R.id.chart);
        mChart.setUsePercentValues(true);
        mChart.setDescription("");
        mChart.setDragDecelerationFrictionCoef(0.95f);
        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColorTransparent(true);
        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);
        mChart.setDrawCenterText(true);
        mChart.setRotationAngle(0);
        mChart.setRotationEnabled(true);
        mChart.setDrawSliceText(false);
        mChart.setCenterText("");
        mChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);
        Legend l = mChart.getLegend();
        l.setEnabled(false);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        int duration = (int) ((endTime - beginTime) / 1000);
        endTime = System.currentTimeMillis();
        Map<String, String> map_value = new HashMap<String, String>();
        MobclickAgent.onEventValue(this, "customAnalysis", map_value, duration);
    }

    private void initView() {
        btnGender = (Button) findViewById(R.id.btnSex);
        btnStrength = (Button) findViewById(R.id.btnStrength);
        btnLayout = (Button) findViewById(R.id.btnLayout);
        btnSize = (Button) findViewById(R.id.btnSize);
        btnPrice = (Button) findViewById(R.id.btnPrice);
        btnType = (Button) findViewById(R.id.btnType);
        labelOne = (TextView) findViewById(R.id.label_one);
        labelTwo = (TextView) findViewById(R.id.label_two);
        labelThree = (TextView) findViewById(R.id.label_three);
        labelFour = (TextView) findViewById(R.id.label_four);
        labelFive = (TextView) findViewById(R.id.label_five);
        labelSix = (TextView) findViewById(R.id.label_six);
        layoutOne = (LinearLayout) findViewById(R.id.layoutOne);
        layoutTwo = (LinearLayout) findViewById(R.id.layoutTwo);
        layoutThree = (LinearLayout) findViewById(R.id.layoutThree);
        layoutFour = (LinearLayout) findViewById(R.id.layoutFour);
        layoutFive = (LinearLayout) findViewById(R.id.layoutFive);
        layoutSix = (LinearLayout) findViewById(R.id.layoutSix);
        left = (ImageView) findViewById(R.id.analysis_left);
        right = (ImageView) findViewById(R.id.analysis_right);
        centerLayout = (LinearLayout) findViewById(R.id.analysis_center_layout);
        tvType = (TextView) findViewById(R.id.analysis_type);
        tvTotal = (TextView) findViewById(R.id.analysis_total);
        labelSeven = (TextView) findViewById(R.id.label_seven);
        layoutSeven = (LinearLayout) findViewById(R.id.layoutSeven);
        labelEight = (TextView) findViewById(R.id.label_eight);
        layoutEight = (LinearLayout) findViewById(R.id.layoutEight);
    }

    private void setClickListener() {
        btnGender.setOnClickListener(this);
        btnStrength.setOnClickListener(this);
        btnLayout.setOnClickListener(this);
        btnSize.setOnClickListener(this);
        btnPrice.setOnClickListener(this);
        btnType.setOnClickListener(this);
        right.setOnClickListener(this);
        left.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.analysis_left://左边箭头
                if (currentPos >= 1) {
                    currentPos -= 1;
                } else {
                    currentPos = 5;
                }
                if (currentPos != pos) {
                    mTabs[currentPos].setSelected(true);
                    mTabs[pos].setSelected(false);
                    pos = currentPos;
                }
                switch (currentPos) {
                    case 0:
                        calGender();
                        break;
                    case 1:
                        calStrength();
                        break;
                    case 2:
                        calLayout();
                        break;
                    case 3:
                        calSize();
                        break;
                    case 4:
                        calPrice();
                        break;
                    case 5:
                        calType();
                        break;
                }
                break;
            case R.id.analysis_right://右边箭头
                if (currentPos <= 4) {
                    currentPos += 1;
                } else {
                    currentPos = 0;
                }
                if (currentPos != pos) {
                    mTabs[currentPos].setSelected(true);
                    mTabs[pos].setSelected(false);
                    pos = currentPos;
                }
                switch (currentPos) {
                    case 0:
                        calGender();
                        break;
                    case 1:
                        calStrength();
                        break;
                    case 2:
                        calLayout();
                        break;
                    case 3:
                        calSize();
                        break;
                    case 4:
                        calPrice();
                        break;
                    case 5:
                        calType();
                        break;
                }
                break;
            case R.id.btnSex://选择性别
                currentPos = 0;
                calGender();
                break;
            case R.id.btnStrength://选择意向强度
                currentPos = 1;
                calStrength();
                break;
            case R.id.btnLayout://选择意向户型
                currentPos = 2;
                calLayout();
                break;
            case R.id.btnSize://选择意向面积
                currentPos = 3;
                calSize();
                break;
            case R.id.btnPrice://选择意向价格
                currentPos = 4;
                calPrice();
                break;
            case R.id.btnType://选择意向类型
                currentPos = 5;
                calType();
                break;
            default:
                break;
        }
        if (currentPos != pos) {
            mTabs[currentPos].setSelected(true);
            mTabs[pos].setSelected(false);
            pos = currentPos;
        }
    }


    //性别百分比计算
    private void calGender() {
        currentPos = 0;
        if (currentPos != pos) {
            mTabs[currentPos].setSelected(true);
            mTabs[pos].setSelected(false);
            pos = currentPos;
        }
        int one = 0;
        int two = 0;
        for (int i = 0; i < customs.size(); i++) {
            switch (customs.get(i).getGender()) {
                case 1:
                    one++;
                    break;
                case 2:
                    two++;
                    break;
                default:
                    break;
            }
        }
        ArrayList<Entry> yValues = new ArrayList<Entry>();
        yValues.add(new Entry(one, 1));
        yValues.add(new Entry(two, 2));
        setIsShow(2);
        int sum = one + two;
        String[] items = new String[]{"男", "女"};
        if (one != 0) {
            String str = getPercent(one * 1.0, sum * 1.0);
            tv[0].setText(items[0] + "（" + one + "人 " + str + "）");
        } else {
            tv[0].setText(items[0] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (two != 0) {
            String str = getPercent(two * 1.0, sum * 1.0);
            tv[1].setText(items[1] + "（" + two + "人 " + str + "）");
        } else {
            tv[1].setText(items[1] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        setPieData(2, yValues, items, "性别");
    }

    //意向强度百分比计算
    private void calStrength() {
        currentPos = 1;
        if (currentPos != pos) {
            mTabs[currentPos].setSelected(true);
            mTabs[pos].setSelected(false);
            pos = currentPos;
        }
        int one = 0;
        int two = 0;
        int three = 0;
        int four = 0;
        for (int i = 0; i < customs.size(); i++) {
            switch (customs.get(i).getWill()) {
                case 0:
                    one++;
                    break;
                case 1:
                    two++;
                    break;
                case 2:
                    three++;
                    break;
                case 3:
                    four++;
                    break;
                default:
                    break;
            }
        }
        ArrayList<Entry> yValues = new ArrayList<Entry>();
        yValues.add(new Entry(one, 0));
        yValues.add(new Entry(two, 1));
        yValues.add(new Entry(three, 2));
        yValues.add(new Entry(four, 3));
        setIsShow(4);
        int sum = one + two + three + four;
        if (one != 0) {
            String str = getPercent(one * 1.0, sum * 1.0);
            tv[0].setText(Constants.WILLITEMS[0] + "（" + one + "人 " + str + "）");
        } else {
            tv[0].setText(Constants.WILLITEMS[0] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (two != 0) {
            String str = getPercent(two * 1.0, sum * 1.0);
            tv[1].setText(Constants.WILLITEMS[1] + "（" + two + "人 " + str + "）");
        } else {
            tv[1].setText(Constants.WILLITEMS[1] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (three != 0) {
            String str = getPercent(three * 1.0, sum * 1.0);
            tv[2].setText(Constants.WILLITEMS[2] + "（" + three + "人 " + str + "）");
        } else {
            tv[2].setText(Constants.WILLITEMS[2] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (four != 0) {
            String str = getPercent(four * 1.0, sum * 1.0);
            tv[3].setText(Constants.WILLITEMS[3] + "（" + four + "人 " + str + "）");
        } else {
            tv[3].setText(Constants.WILLITEMS[3] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        setPieData(yValues.size(), yValues, Constants.WILLITEMS, "意向强度");
    }

    //意向户型百分比计算
    private void calLayout() {
        currentPos = 2;
        if (currentPos != pos) {
            mTabs[currentPos].setSelected(true);
            mTabs[pos].setSelected(false);
            pos = currentPos;
        }
        int one = 0;
        int two = 0;
        int three = 0;
        int four = 0;
        int five = 0;
        int six = 0;
        for (int i = 0; i < customs.size(); i++) {
            switch (customs.get(i).getLayout()) {
                case 0:
                    one++;
                    break;
                case 1:
                    two++;
                    break;
                case 2:
                    three++;
                    break;
                case 3:
                    four++;
                    break;
                case 4:
                    five++;
                    break;
                case 5:
                    six++;
                    break;
                default:
                    break;
            }
        }
        ArrayList<Entry> yValues = new ArrayList<Entry>();
        yValues.add(new Entry(one, 0));
        yValues.add(new Entry(two, 1));
        yValues.add(new Entry(three, 2));
        yValues.add(new Entry(four, 3));
        yValues.add(new Entry(five, 4));
        yValues.add(new Entry(six, 5));
        setIsShow(6);
        int sum = one + two + three + four + five + six;
        if (one != 0) {
            String str = getPercent(one * 1.0, sum * 1.0);
            tv[0].setText(Constants.LAYOUTITEMS[0] + "（" + one + "人 " + str + "）");
        } else {
            tv[0].setText(Constants.LAYOUTITEMS[0] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (two != 0) {
            String str = getPercent(two * 1.0, sum * 1.0);
            tv[1].setText(Constants.LAYOUTITEMS[1] + "（" + two + "人 " + str + "）");
        } else {
            tv[1].setText(Constants.LAYOUTITEMS[1] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (three != 0) {
            String str = getPercent(three * 1.0, sum * 1.0);
            tv[2].setText(Constants.LAYOUTITEMS[2] + "（" + three + "人 " + str + "）");
        } else {
            tv[2].setText(Constants.LAYOUTITEMS[2] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (four != 0) {
            String str = getPercent(four * 1.0, sum * 1.0);
            tv[3].setText(Constants.LAYOUTITEMS[3] + "（" + four + "人 " + str + "）");
        } else {
            tv[3].setText(Constants.LAYOUTITEMS[3] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (five != 0) {
            String str = getPercent(five * 1.0, sum * 1.0);
            tv[4].setText(Constants.LAYOUTITEMS[4] + "（" + five + "人 " + str + "）");
        } else {
            tv[4].setText(Constants.LAYOUTITEMS[4] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (six != 0) {
            String str = getPercent(six * 1.0, sum * 1.0);
            tv[5].setText(Constants.LAYOUTITEMS[5] + "（" + six + "人 " + str + "）");
        } else {
            tv[5].setText(Constants.LAYOUTITEMS[5] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        setPieData(6, yValues, Constants.LAYOUTITEMS, "意向户型");
    }

    //意向面积百分比计算
    private void calSize() {
        currentPos = 3;
        if (currentPos != pos) {
            mTabs[currentPos].setSelected(true);
            mTabs[pos].setSelected(false);
            pos = currentPos;
        }
        int one = 0;
        int two = 0;
        int three = 0;
        int four = 0;
        int five = 0;
        int six = 0;
        for (int i = 0; i < customs.size(); i++) {
            switch (customs.get(i).getSize()) {
                case 0:
                    one++;
                    break;
                case 1:
                    two++;
                    break;
                case 2:
                    three++;
                    break;
                case 3:
                    four++;
                    break;
                case 4:
                    five++;
                    break;
                case 5:
                    six++;
                    break;
                default:
                    break;
            }
        }
        ArrayList<Entry> yValues = new ArrayList<Entry>();
        yValues.add(new Entry(one, 0));
        yValues.add(new Entry(two, 1));
        yValues.add(new Entry(three, 2));
        yValues.add(new Entry(four, 3));
        yValues.add(new Entry(five, 4));
        yValues.add(new Entry(six, 5));
        setIsShow(6);
        int sum = one + two + three + four + five + six;
        if (one != 0) {
            String str = getPercent(one * 1.0, sum * 1.0);
            tv[0].setText(Constants.SIZEITEMS[0] + "（" + one + "人 " + str + "）");
        } else {
            tv[0].setText(Constants.SIZEITEMS[0] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (two != 0) {
            String str = getPercent(two * 1.0, sum * 1.0);
            tv[1].setText(Constants.SIZEITEMS[1] + "（" + two + "人 " + str + "）");
        } else {
            tv[1].setText(Constants.SIZEITEMS[1] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (three != 0) {
            String str = getPercent(three * 1.0, sum * 1.0);
            tv[2].setText(Constants.SIZEITEMS[2] + "（" + three + "人 " + str + "）");
        } else {
            tv[2].setText(Constants.SIZEITEMS[2] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (four != 0) {
            String str = getPercent(four * 1.0, sum * 1.0);
            tv[3].setText(Constants.SIZEITEMS[3] + "（" + four + "人 " + str + "）");
        } else {
            tv[3].setText(Constants.SIZEITEMS[3] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (five != 0) {
            String str = getPercent(five * 1.0, sum * 1.0);
            tv[4].setText(Constants.SIZEITEMS[4] + "（" + five + "人 " + str + "）");
        } else {
            tv[4].setText(Constants.SIZEITEMS[4] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (six != 0) {
            String str = getPercent(six * 1.0, sum * 1.0);
            tv[5].setText(Constants.SIZEITEMS[5] + "（" + six + "人 " + str + "）");
        } else {
            tv[5].setText(Constants.SIZEITEMS[5] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        setPieData(6, yValues, Constants.SIZEITEMS, "意向面积");
    }


    //意向价格百分比计算
    private void calPrice() {
        currentPos = 4;
        if (currentPos != pos) {
            mTabs[currentPos].setSelected(true);
            mTabs[pos].setSelected(false);
            pos = currentPos;
        }
        int one = 0;
        int two = 0;
        int three = 0;
        int four = 0;
        int five = 0;
        int six = 0;
        int seven = 0;
        for (int i = 0; i < customs.size(); i++) {
            switch (customs.get(i).getPrice()) {
                case 0:
                    one++;
                    break;
                case 1:
                    two++;
                    break;
                case 2:
                    three++;
                    break;
                case 3:
                    four++;
                    break;
                case 4:
                    five++;
                    break;
                case 5:
                    six++;
                    break;
                case 6:
                    seven++;
                    break;
                default:
                    break;
            }
        }
        ArrayList<Entry> yValues = new ArrayList<Entry>();
        yValues.add(new Entry(one, 0));
        yValues.add(new Entry(two, 1));
        yValues.add(new Entry(three, 2));
        yValues.add(new Entry(four, 3));
        yValues.add(new Entry(five, 4));
        yValues.add(new Entry(six, 5));
        yValues.add(new Entry(seven, 6));
        setIsShow(7);
        int sum = one + two + three + four + five + six + seven;
        if (one != 0) {
            String str = getPercent(one * 1.0, sum * 1.0);
            tv[0].setText(Constants.PRICEITEMS[0] + "（" + one + "人 " + str + "）");
        } else {
            tv[0].setText(Constants.PRICEITEMS[0] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (two != 0) {
            String str = getPercent(two * 1.0, sum * 1.0);
            tv[1].setText(Constants.PRICEITEMS[1] + "（" + two + "人 " + str + "）");
        } else {
            tv[1].setText(Constants.PRICEITEMS[1] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (three != 0) {
            String str = getPercent(three * 1.0, sum * 1.0);
            tv[2].setText(Constants.PRICEITEMS[2] + "（" + three + "人 " + str + "）");
        } else {
            tv[2].setText(Constants.PRICEITEMS[2] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (four != 0) {
            String str = getPercent(four * 1.0, sum * 1.0);
            tv[3].setText(Constants.PRICEITEMS[3] + "（" + four + "人 " + str + "）");
        } else {
            tv[3].setText(Constants.PRICEITEMS[3] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (five != 0) {
            String str = getPercent(five * 1.0, sum * 1.0);
            tv[4].setText(Constants.PRICEITEMS[4] + "（" + five + "人 " + str + "）");
        } else {
            tv[4].setText(Constants.PRICEITEMS[4] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (six != 0) {
            String str = getPercent(six * 1.0, sum * 1.0);
            tv[5].setText(Constants.PRICEITEMS[5] + "（" + six + "人 " + str + "）");
        } else {
            tv[5].setText(Constants.PRICEITEMS[5] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (seven != 0) {
            String str = getPercent(seven * 1.0, sum * 1.0);
            tv[6].setText(Constants.PRICEITEMS[6] + "（" + seven + "人 " + str + "）");
        } else {
            tv[6].setText(Constants.PRICEITEMS[6] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        setPieData(7, yValues, Constants.PRICEITEMS, "意向价格");
    }

    //意向类别百分比计算
    private void calType() {
        currentPos = 5;
        if (currentPos != pos) {
            mTabs[currentPos].setSelected(true);
            mTabs[pos].setSelected(false);
            pos = currentPos;
        }
        int one = 0;
        int two = 0;
        int three = 0;
        int four = 0;
        int five = 0;
        int six = 0;
        int seven = 0;

        for (int i = 0; i < customs.size(); i++) {
            switch (customs.get(i).getType()) {
                case 0:
                    one++;
                    break;
                case 1:
                    two++;
                    break;
                case 2:
                    three++;
                    break;
                case 3:
                    four++;
                    break;
                case 4:
                    five++;
                    break;
                case 5:
                    six++;
                    break;
                case 6:
                    seven++;
                    break;
                default:
                    break;
            }
        }
        ArrayList<Entry> yValues = new ArrayList<Entry>();
        yValues.add(new Entry(one, 0));
        yValues.add(new Entry(two, 1));
        yValues.add(new Entry(three, 2));
        yValues.add(new Entry(four, 3));
        yValues.add(new Entry(five, 4));
        yValues.add(new Entry(six, 5));
        yValues.add(new Entry(seven, 6));
        setIsShow(7);
        int sum = one + two + three + four + five + six + seven;
        if (one != 0) {
            String str = getPercent(one * 1.0, sum * 1.0);
            tv[0].setText(Constants.TYPEITEMS[0] + "（" + one + "人 " + str + "）");
        } else {
            tv[0].setText(Constants.TYPEITEMS[0] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (two != 0) {
            String str = getPercent(two * 1.0, sum * 1.0);
            tv[1].setText(Constants.TYPEITEMS[1] + "（" + two + "人 " + str + "）");
        } else {
            tv[1].setText(Constants.TYPEITEMS[1] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (three != 0) {
            String str = getPercent(three * 1.0, sum * 1.0);
            tv[2].setText(Constants.TYPEITEMS[2] + "（" + three + "人 " + str + "）");
        } else {
            tv[2].setText(Constants.TYPEITEMS[2] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (four != 0) {
            String str = getPercent(four * 1.0, sum * 1.0);
            tv[3].setText(Constants.TYPEITEMS[3] + "（" + four + "人 " + str + "）");
        } else {
            tv[3].setText(Constants.TYPEITEMS[3] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (five != 0) {
            String str = getPercent(five * 1.0, sum * 1.0);
            tv[4].setText(Constants.TYPEITEMS[4] + "（" + five + "人 " + str + "）");
        } else {
            tv[4].setText(Constants.TYPEITEMS[4] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (six != 0) {
            String str = getPercent(six * 1.0, sum * 1.0);
            tv[5].setText(Constants.TYPEITEMS[5] + "（" + six + "人 " + str + "）");
        } else {
            tv[5].setText(Constants.TYPEITEMS[5] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        if (seven != 0) {
            String str = getPercent(seven * 1.0, sum * 1.0);
            tv[6].setText(Constants.TYPEITEMS[6] + "（" + seven + "人 " + str + "）");
        } else {
            tv[6].setText(Constants.TYPEITEMS[6] + "（" + 0 + "人 " + "0" + "%" + "）");
        }
        setPieData(7, yValues, Constants.TYPEITEMS, "意向类型");
    }


    private void initColor() {
        mColor = new int[]{Color.rgb(247, 77, 77), Color.rgb(255, 183, 46), Color.rgb(128, 215, 57), Color.rgb(46, 179, 255), Color.rgb(209, 83, 241), Color.rgb(61, 246, 235)
                , Color.rgb(112, 54, 245), Color.rgb(211, 143, 113)};
    }

    private void setIsShow(int pos) {
        for (int i = 0; i < tv.length; i++) {
            if (i < pos) {
                layout[i].setVisibility(View.VISIBLE);
            } else {
                layout[i].setVisibility(View.GONE);
            }
        }
    }

    //计算百分比
    public String getPercent(double x, double total) {
        String result = "";//接受百分比的值
        double tempresult = x / total;
        DecimalFormat df1 = new DecimalFormat("0.00%");    //##.00%   百分比格式，后面不足2位的用0补齐
        result = df1.format(tempresult);
        return result;
    }

    /**
     * 饼图数据
     *
     * @param count 分成几部分
     * @param
     */
    private void setPieData(int count, ArrayList<Entry> yValues, String[] items, String str) {
        centerLayout.setVisibility(View.GONE);
        tvType.setText(str);
        ArrayList<String> xValues = new ArrayList<String>();  //xVals用来表示每个饼块上的内容

        for (int i = 0; i < count; i++) {
            xValues.add(items[i]);
        }
        //y轴的集合
        PieDataSet pieDataSet = new PieDataSet(yValues, " "/*显示在比例图上*/);
        pieDataSet.setSliceSpace(0f); //设置个饼状图之间的距离
        int[] colors = new int[count];
        // 饼图颜色
        for (int i = 0; i < count; i++) {
            colors[i] = mColor[i];
        }
        pieDataSet.setColors(colors);
        pieDataSet.setSelectionShift(0); // 选中态多出的长度
        PieData pieData = new PieData(xValues, pieDataSet);
        mChart.setData(pieData);
        mChart.getData().getDataSet().setDrawValues(false);
        mChart.invalidate();
        mChart.setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessageDelayed(0, 1500);
        mChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
