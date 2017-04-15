package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.StringUtils;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;

/**
 * Created by Jsmi on 2015/11/23.
 * 房贷计算器
 */
public class LoanCalcActivity extends BaseActivity {

    //公积金税率
    private float LOW_GJJ = 0.0275f;
    private float HIGH_GJJ = 0.0325f;
    //商业贷款税率
    private float LOW_SDS = 0.0475f;
    private float HIGH_SDS = 0.0490f;
    private Float ONEYEAR_SDS = 0.0435f;

    private List<String> list = new LinkedList<String>();
    TextView mTitlesy;
    TextView mTitlegj;
    TextView mTitlezh;

    RelativeLayout mPayTypeLayout;
    TextView mPayTypeText;

    RelativeLayout mCountTypeLayout;
    TextView mCountTypeText;

    RelativeLayout mChengshuTypeLayout;
    TextView mChengshuTypeText;


    LinearLayout mNumLayout1;
    LinearLayout mNumLayout2;
    LinearLayout mNumLayout3;

    EditText mTotalPrice;
    EditText mTotalPricesy;
    EditText mTotalPricegj;

    EditText mPrePrice;
    EditText mSize;

    RelativeLayout mYearTypeLayout;
    TextView mYearTypeText;

    RelativeLayout mRategjTypeLayout;
    TextView mRategjTypeText;

    RelativeLayout mRatesyTypeLayout;
    TextView mRatesyTypeText;
    EditText mRatesy;

    Button mCountButton;

    int currentArrayId = 0;

    //商业贷款0，公积金1，组合2
    int typeIndex = 0;
    //还款方式 等额本金0， 等额本息1
    int payTypeIndex = 0;
    //计算方式 根据单价面积0，根据总额1
    int countTypeIndex = 0;
    //按揭成数 从1到9成
    int chengshuTypeIndex = 6;

    //年数 1-20,25,30
    int yearTypeIndex = 19;
    //公积金利率
    int rategjTypeIndex = 0;
    //商业利率
    int ratesyTypeIndex = 3;
    private Button chongzhi;
    private View line;
    private View line1;
    private TextView gongjijin_beishu;


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        this.setContentView(R.layout.activity_loan_calc);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "房贷计算器");
        getData();


        chongzhi = (Button) findViewById(R.id.loa_button_chongzhi);
        chongzhi.setOnClickListener(mTitleClickListener);
        line = findViewById(R.id.loa_line);
        line1 = findViewById(R.id.loa_line_1);

        //头部选项卡
        mTitlesy = (TextView) findViewById(R.id.loan_title_sy);
        mTitlegj = (TextView) findViewById(R.id.loan_title_gj);
        mTitlezh = (TextView) findViewById(R.id.loan_title_zh);
        mTitlesy.setSelected(true);
        mTitlezh.setSelected(false);
        mTitlegj.setSelected(false);

        mTitlesy.setOnClickListener(mTitleClickListener);
        mTitlegj.setOnClickListener(mTitleClickListener);
        mTitlezh.setOnClickListener(mTitleClickListener);

        //还款方式
        mPayTypeLayout = (RelativeLayout) this.findViewById(R.id.loancalc_paytypelayout);
        mPayTypeText = (TextView) this.findViewById(R.id.loancalc_paytypetext);
        mPayTypeText.setText("等额本息");

        mPayTypeLayout.setOnClickListener(mOptionListener);

        //计算方式
        mCountTypeLayout = (RelativeLayout) this.findViewById(R.id.loancalc_counttypelayout);
        mCountTypeText = (TextView) this.findViewById(R.id.loancalc_counttypetext);
        mCountTypeText.setText("根据面积，单价计算");

        mCountTypeLayout.setOnClickListener(mOptionListener);


        //贷款总额计算
        mNumLayout1 = (LinearLayout) this.findViewById(R.id.loancalc_numtype1);
        mNumLayout2 = (LinearLayout) this.findViewById(R.id.loancalc_numtype2);
        mNumLayout3 = (LinearLayout) this.findViewById(R.id.loancalc_numtype3);

        mPrePrice = (EditText) this.findViewById(R.id.loancalc_preprice);
        mSize = (EditText) this.findViewById(R.id.loancalc_size);
        mChengshuTypeLayout = (RelativeLayout) this.findViewById(R.id.loancalc_chengshulayout);
        mChengshuTypeText = (TextView) this.findViewById(R.id.loancalc_chengshutext);

        mChengshuTypeText.setText("7成");
        mChengshuTypeLayout.setOnClickListener(mOptionListener);

        mTotalPrice = (EditText) this.findViewById(R.id.loancalc_pricetotal);
        mTotalPricesy = (EditText) this.findViewById(R.id.loancalc_pricetotalsy);
        mTotalPricegj = (EditText) this.findViewById(R.id.loancalc_pricetotalgj);


        //年数和利率
        mYearTypeLayout = (RelativeLayout) this.findViewById(R.id.loancalc_yearslayout);
        mYearTypeText = (TextView) this.findViewById(R.id.loancalc_yearstext);
        mYearTypeText.setText("20年（240期）");
        mYearTypeLayout.setOnClickListener(mOptionListener);

        mRategjTypeLayout = (RelativeLayout) this.findViewById(R.id.loancalc_rategjlayout);
        mRategjTypeText = (TextView) this.findViewById(R.id.loancalc_rategjtext);

        mRategjTypeLayout.setVisibility(View.GONE);

        mRatesyTypeLayout = (RelativeLayout) this.findViewById(R.id.loancalc_ratesylayout);
        mRatesyTypeText = (TextView) this.findViewById(R.id.loancalc_ratesytext);
        gongjijin_beishu = (TextView) this.findViewById(R.id.loancalc_ratesytext_1);
        mRatesy = (EditText) this.findViewById(R.id.loancalc_editratesy);
        mRatesyTypeText.setText("最新基准利率");
        gongjijin_beishu.setText("最新基准利率");
        mRatesy.setTextColor(Color.GRAY);
        mRategjTypeText.setTextColor(Color.GRAY);
        mRategjTypeLayout.setOnClickListener(mOptionListener);
        mRatesyTypeLayout.setOnClickListener(mOptionListener);

        mCountButton = (Button) this.findViewById(R.id.loancalc_button);
        mCountButton.setOnClickListener(mCountListener);
        mCountButton.setEnabled(false);
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

    private void getData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getRageConfig");
        map.put("module", Constants.MODULE_CONFIG);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        mCountButton.setEnabled(true);
                        mRategjTypeText.setText("" + HIGH_GJJ * 100);
                        mRatesy.setText(HIGH_SDS * 100 + "");
                        Toast.makeText(LoanCalcActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject1 = new JSONObject(response);
                            int code = jsonObject1.getInt("code");
                            if(code > Result.RESULT_OK) {
                                JSONObject jsonObject = jsonObject1.getJSONObject("content");
                                JSONArray jsonArray = jsonObject.getJSONArray("rage");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    list.add(String.valueOf(jsonArray.get(i)));
                                }
                                if (list != null && list.size() == 5) {
                                    LOW_SDS = Float.parseFloat(list.get(0)) / 100;
                                    HIGH_SDS = Float.parseFloat(list.get(1)) / 100;
                                    LOW_GJJ = Float.parseFloat(list.get(2)) / 100;
                                    HIGH_GJJ = Float.parseFloat(list.get(3)) / 100;
                                    ONEYEAR_SDS = Float.parseFloat(list.get(4)) / 100;

                                    SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                                    SharedPreferences.Editor edit = preferences.edit();
                                    edit.putFloat("LOW_SDS", LOW_SDS);
                                    edit.putFloat("HIGH_SDS", HIGH_SDS);
                                    edit.putFloat("LOW_GJJ", LOW_GJJ);
                                    edit.putFloat("HIGH_GJJ", HIGH_GJJ);
                                    edit.putFloat("ONEYEAR_SDS", ONEYEAR_SDS);
                                    edit.commit();
                                }
                            } else {
                                SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                                LOW_SDS = preferences.getFloat("LOW_SDS", LOW_SDS);
                                HIGH_SDS = preferences.getFloat("HIGH_SDS", HIGH_SDS);
                                LOW_GJJ = preferences.getFloat("LOW_GJJ", LOW_GJJ);
                                HIGH_GJJ = preferences.getFloat("HIGH_GJJ", HIGH_GJJ);
                                ONEYEAR_SDS = preferences.getFloat("ONEYEAR_SDS", ONEYEAR_SDS);
                            }
                            mRategjTypeText.setText("" + HIGH_GJJ * 100);
                            mRatesy.setText(HIGH_SDS * 100 + "");
                        } catch (JSONException e) {
                            Toast.makeText(LoanCalcActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    View.OnClickListener mTitleClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.loan_title_sy:

                    mTitlesy.setSelected(true);
                    mTitlezh.setSelected(false);
                    mTitlegj.setSelected(false);
                    line.setVisibility(View.VISIBLE);
                    line1.setVisibility(View.VISIBLE);
                    typeIndex = 0;

                    mCountTypeLayout.setVisibility(View.VISIBLE);

                    if (countTypeIndex > 0) {
                        mNumLayout1.setVisibility(View.GONE);
                        mNumLayout2.setVisibility(View.VISIBLE);
                        mNumLayout3.setVisibility(View.GONE);
                    } else {
                        mNumLayout1.setVisibility(View.VISIBLE);
                        mNumLayout2.setVisibility(View.GONE);
                        mNumLayout3.setVisibility(View.GONE);
                    }


                    if (mCountTypeText.getText().toString().equals("根据面积，单价计算")) {
                        mNumLayout1.setVisibility(View.VISIBLE);
                        mNumLayout2.setVisibility(View.GONE);
                        mNumLayout3.setVisibility(View.GONE);
                    }
                    //隐藏公积金利率
                    mRategjTypeLayout.setVisibility(View.GONE);
                    mRatesyTypeLayout.setVisibility(View.VISIBLE);


                    break;
                case R.id.loan_title_gj:

                    mTitlesy.setSelected(false);
                    mTitlezh.setSelected(false);
                    mTitlegj.setSelected(true);
                    line.setVisibility(View.GONE);
                    line1.setVisibility(View.VISIBLE);
                    typeIndex = 1;

                    mCountTypeLayout.setVisibility(View.VISIBLE);
                    if (countTypeIndex > 0) {
                        mNumLayout1.setVisibility(View.GONE);
                        mNumLayout2.setVisibility(View.VISIBLE);
                        mNumLayout3.setVisibility(View.GONE);
                    } else {
                        mNumLayout1.setVisibility(View.VISIBLE);
                        mNumLayout2.setVisibility(View.GONE);
                        mNumLayout3.setVisibility(View.GONE);
                    }

                    if (mCountTypeText.getText().toString().equals("根据面积，单价计算")) {
                        mNumLayout1.setVisibility(View.VISIBLE);
                        mNumLayout2.setVisibility(View.GONE);
                        mNumLayout3.setVisibility(View.GONE);
                    }
                    mRategjTypeLayout.setVisibility(View.VISIBLE);
                    mRatesyTypeLayout.setVisibility(View.GONE);

                    break;
                case R.id.loan_title_zh:
                    mTitlesy.setSelected(false);
                    mTitlezh.setSelected(true);
                    mTitlegj.setSelected(false);
                    line.setVisibility(View.VISIBLE);
                    line1.setVisibility(View.GONE);
                    typeIndex = 2;

                    mCountTypeLayout.setVisibility(View.GONE);

                    mNumLayout1.setVisibility(View.GONE);
                    mNumLayout2.setVisibility(View.GONE);
                    mNumLayout3.setVisibility(View.VISIBLE);

                    mRategjTypeLayout.setVisibility(View.VISIBLE);
                    mRatesyTypeLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.loa_button_chongzhi:
                    mTotalPricegj.setText("");
                    mTotalPrice.setText("");
                    mTotalPricesy.setText("");
                    mPrePrice.setText("");
                    mSize.setText("");
                    if (!mTitlezh.isSelected()) {
                        mNumLayout1.setVisibility(View.VISIBLE);
                        mNumLayout2.setVisibility(View.GONE);
                        mNumLayout3.setVisibility(View.GONE);
                    }
                    mPayTypeText.setText("等额本息");
                    mYearTypeText.setText("20年（240期）");
                    mRategjTypeText.setText(HIGH_GJJ * 100 + "");
                    mRatesyTypeText.setText("最新基准利率");
                    gongjijin_beishu.setText("最新基准利率");
                    mChengshuTypeText.setText("7成");
                    mCountTypeText.setText("根据面积，单价计算");
                    mRatesy.setText(HIGH_SDS * 100 + "");
                    break;
                default:
                    break;
            }
        }
    };

    View.OnClickListener mOptionListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.loancalc_paytypelayout://还款方式
                    currentArrayId = R.array.loanpaytype;
                    View dialogView1 = LayoutInflater.from(LoanCalcActivity.this).inflate(R.layout.common_dialog_list, null);
                    final AppDialog.Builder builder = new AppDialog.Builder(LoanCalcActivity.this,
                            AppDialog.Builder.SINGLECHOICE, getResources().getStringArray(currentArrayId));
                    builder.setContentView(dialogView1).setItemClick(
                            new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    payTypeIndex = position;
                                    mPayTypeText.setText(getResources().getStringArray(currentArrayId)[position]);
                                    builder.dismiss();
                                }
                            }
                    ).create();

                    break;
                case R.id.loancalc_counttypelayout://计算方式
                    currentArrayId = R.array.loancounttype;
                    View dialogView2 = LayoutInflater.from(LoanCalcActivity.this).inflate(R.layout.common_dialog_list, null);
                    final AppDialog.Builder builder1 = new AppDialog.Builder(LoanCalcActivity.this,
                            AppDialog.Builder.SINGLECHOICE, getResources().getStringArray(currentArrayId));
                    builder1.setContentView(dialogView2).setItemClick(
                            new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    countTypeIndex = position;
                                    mCountTypeText.setText(getResources().getStringArray(currentArrayId)[position]);
                                    if (countTypeIndex > 0) {
                                        mNumLayout1.setVisibility(View.GONE);
                                        mNumLayout2.setVisibility(View.VISIBLE);
                                        mNumLayout3.setVisibility(View.GONE);
                                    } else {
                                        mNumLayout1.setVisibility(View.VISIBLE);
                                        mNumLayout2.setVisibility(View.GONE);
                                        mNumLayout3.setVisibility(View.GONE);
                                    }
                                    builder1.dismiss();
                                }
                            }
                    ).create();
                    break;
                case R.id.loancalc_chengshulayout://按揭成数
                    currentArrayId = R.array.loanchengshutype;
                    View dialogView3 = LayoutInflater.from(LoanCalcActivity.this).inflate(R.layout.common_dialog_list, null);
                    final AppDialog.Builder builder2 = new AppDialog.Builder(LoanCalcActivity.this,
                            AppDialog.Builder.SINGLECHOICE, getResources().getStringArray(currentArrayId));
                    builder2.setContentView(dialogView3).setItemClick(
                            new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    chengshuTypeIndex = position;
                                    mChengshuTypeText.setText(getResources().getStringArray(currentArrayId)[position]);
                                    builder2.dismiss();
                                }
                            }
                    ).create();
                    break;
                case R.id.loancalc_yearslayout://按揭年数
                    currentArrayId = R.array.loanyeartype;
                    View dialogView4 = LayoutInflater.from(LoanCalcActivity.this).inflate(R.layout.common_dialog_list, null);
                    final AppDialog.Builder builder3 = new AppDialog.Builder(LoanCalcActivity.this,
                            AppDialog.Builder.SINGLECHOICE, getResources().getStringArray(currentArrayId));
                    builder3.setContentView(dialogView4).setItemClick(
                            new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    yearTypeIndex = position;
                                    mYearTypeText.setText(getResources().getStringArray(currentArrayId)[position]);

                                    //年数不一致，公积金利率不一致
                                    if (yearTypeIndex >= 5) {
                                        rategjTypeIndex = 0;
                                        mRatesy.setText(HIGH_SDS * 100 * getSyRate() + "");
                                        //mRatesyTypeText.setText(getResources().getStringArray(R.array.loanratesttype)[rategjTypeIndex]);
                                        mRategjTypeText.setText(HIGH_GJJ * 100 * getSyRate() + "");
                                    } else if (yearTypeIndex == 0) {
                                        mRatesy.setText(ONEYEAR_SDS * 100 * getSyRate() + "");
                                        mRategjTypeText.setText(LOW_GJJ * 100 * getSyRate() + "");
                                    } else {
                                        rategjTypeIndex = 1;
                                        mRatesy.setText(LOW_SDS * 100 * getSyRate() + "");
                                        //mRatesyTypeText.setText(getResources().getStringArray(R.array.loanratesttype)[rategjTypeIndex]);
                                        mRategjTypeText.setText(LOW_GJJ * 100 * getSyRate() + "");
                                    }
                                    builder3.dismiss();
                                }
                            }
                    ).create();
                    break;
                case R.id.loancalc_ratesylayout://商业利率
                    currentArrayId = R.array.loanratesytype;
                    View dialogView5 = LayoutInflater.from(LoanCalcActivity.this).inflate(R.layout.common_dialog_list, null);
                    final AppDialog.Builder builder4 = new AppDialog.Builder(LoanCalcActivity.this,
                            AppDialog.Builder.SINGLECHOICE, getResources().getStringArray(R.array.loanratesytype));
                    builder4.setContentView(dialogView5).setItemClick(
                            new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    ratesyTypeIndex = position;
                                    mRatesyTypeText.setText(getResources().getStringArray(currentArrayId)[position]);
                                    if (yearTypeIndex >= 5) {
                                        mRatesy.setText(HIGH_SDS * 100 * getSyRate() + "");
                                    } else if (yearTypeIndex == 0) {
                                        mRatesy.setText(ONEYEAR_SDS * 100 * getSyRate() + "");
                                    } else {
                                        mRatesy.setText(LOW_SDS * 100 * getSyRate() + "");
                                    }
                                    builder4.dismiss();
                                }
                            }
                    ).create();
                    break;
                case R.id.loancalc_rategjlayout://公积金利率
                    currentArrayId = R.array.loanratesytype;
                    View dialogView6 = LayoutInflater.from(LoanCalcActivity.this).inflate(R.layout.common_dialog_list, null);
                    final AppDialog.Builder builder5 = new AppDialog.Builder(LoanCalcActivity.this,
                            AppDialog.Builder.SINGLECHOICE, getResources().getStringArray(R.array.loanratesytype));
                    builder5.setContentView(dialogView6).setItemClick(
                            new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    ratesyTypeIndex = position;
                                    gongjijin_beishu.setText(getResources().getStringArray(currentArrayId)[position]);
                                    if (yearTypeIndex >= 5) {
                                        mRategjTypeText.setText(HIGH_GJJ * 100 * getSyRate() + "");
                                    } else {
                                        mRategjTypeText.setText(LOW_GJJ * 100 * getSyRate() + "");
                                    }
                                    builder5.dismiss();
                                }
                            }
                    ).create();
                    break;
            }
        }
    };


    View.OnClickListener mCountListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            //定义局部变量
            int bizVal; //商业贷款
            int foundVal; //公积金
            float totalVal; //总价 (单位*面积 或者 总价 或者 商贷+公积金)

//		    int totalMonth = [[[_caculatorModel.sectionOfMortgageyearsAndRate objectAtIndex:0] objectAtIndex:2] intValue] * 12; //按揭月数

            //        float bizRate = 0.0f; //商业贷款利率
            //       float foundRate = 0.0f; //公积金贷款利率

            int totalMonth = 0;

            if (yearTypeIndex < 20) {
                totalMonth = yearTypeIndex * 12 + 12;
            } else {
                if (yearTypeIndex == 20) {
                    //25年
                    totalMonth = 300;
                } else {
                    //30年
                    totalMonth = 360;
                }
            }


            /*//获取当前选择的汇率
            switch (typeIndex) {
                case 1:
                    if (totalMonth <= 60) {
                        foundRate = LOW_GJJ * getSyRate();
                    } else {
                        foundRate = HIGH_GJJ * getSyRate();
                    }
                    break;
                case 0:

                    if (totalMonth <= 60) {
                        bizRate = LOW_SDS * getSyRate();
                    } else {
                        bizRate = HIGH_SDS * getSyRate();
                    }
                    break;
                case 2:

                    if (totalMonth <= 60) {
                        foundRate = LOW_GJJ * getSyRate();
                        bizRate = LOW_SDS * getSyRate();
                    } else {
                        foundRate = HIGH_GJJ * getSyRate();
                        bizRate = HIGH_SDS * getSyRate();
                    }
                    break;

                default:
                    break;
            }*/


            float repayMonth; //月均还款 或者 每个月还款金额
            float repayTotal = 0.0f; //还款总额
            float repayAccrual; //支付利息

            ArrayList<String> results = new ArrayList<String>();

            //组合型
            if (typeIndex == 2) {
                String sytext = mTotalPricesy.getEditableText().toString();
                float synum = 0.0f;
                if (!StringUtils.isEmpty(sytext)) {
                    synum = Float.parseFloat(sytext);
                }

                if (synum <= 0.0f) {
                    synum = 0.0f;
                }

                String gjtext = mTotalPricegj.getEditableText().toString();
                float gjnum = 0.0f;
                if (!StringUtils.isEmpty(gjtext)) {
                    gjnum = Float.parseFloat(gjtext);
                }

                if (gjnum <= 0.0f) {
                    gjnum = 0.0f;
                }

                bizVal = (int) (synum * 10000);
                foundVal = (int) (gjnum * 10000);

                totalVal = bizVal + foundVal;

                if (payTypeIndex == 1) {
                    //等额本金
                    ArrayList<String> repaylist = new ArrayList<String>();

                    for (int i = 0; i < totalMonth; i++) {
                        repayMonth = getSumMonthRepay(Float.parseFloat(mRategjTypeText.getText().toString()), foundVal, totalMonth, i) + getSumMonthRepay(Float.parseFloat(mRatesy.getText().toString()), bizVal, totalMonth, i);
                        repayTotal += repayMonth;

                        repaylist.add((i + 1) + "月 - " + getMoney(repayMonth));
                    }

                    repayAccrual = repayTotal - totalVal;

                    results.add("计算结果(组合型 - 等额本金)");
                    results.add("贷款总额: " + getMoney(totalVal));
                    results.add("还款总额: " + getMoney(repayTotal));
                    results.add("支付利息: " + getMoney(repayAccrual));
                    results.add("贷款月数: " + totalMonth);
                    results.add("月均详情: ");
                    results.addAll(repaylist);

                } else {
                    //等额本息
                    repayMonth = getRateMonthRepay(Float.parseFloat(mRategjTypeText.getText().toString()), foundVal, totalMonth) + getRateMonthRepay(Float.parseFloat(mRatesy.getText().toString()), bizVal, totalMonth);
                    repayTotal = repayMonth * totalMonth;
                    repayAccrual = repayTotal - totalVal;

                    results.add("计算结果(组合型 - 等额本息)");
                    results.add("贷款总额: " + getMoney(totalVal));
                    results.add("还款总额: " + getMoney(repayTotal));
                    results.add("支付利息: " + getMoney(repayAccrual));
                    results.add("贷款月数: " + totalMonth);
                    results.add("月均还款: " + getMoney(repayMonth));
                }

            } else {
                //商业或者公积金
                float currentRate;
                String title;

                if (typeIndex == 1) {
                    currentRate = Float.parseFloat(mRategjTypeText.getText().toString());
                    if (payTypeIndex == 1) {
                        title = "计算结果(公积金 - 等额本金)";
                    } else {
                        title = "计算结果(公积金 - 等额本息)";
                    }

                } else {
                    currentRate = Float.parseFloat(mRatesy.getText().toString());
                    if (payTypeIndex == 1) {
                        title = "计算结果(商业贷款 - 等额本金)";
                    } else {
                        title = "计算结果(商业贷款 - 等额本息)";
                    }

                }

                if (countTypeIndex == 1) {
                    //根据总价
                    String text = mTotalPrice.getEditableText().toString();
                    float num = 0.0f;
                    if (!StringUtils.isEmpty(text)) {
                        num = Float.parseFloat(text);
                    }

                    if (num <= 0.0f) {
                        num = 0.0f;
                    }

                    totalVal = num * 10000;

                    if (payTypeIndex == 1) {
                        //等额本金
                        ArrayList<String> repaylist = new ArrayList<String>();

                        for (int i = 0; i < totalMonth; i++) {
                            repayMonth = getSumMonthRepay(currentRate, totalVal, totalMonth, i);
                            repayTotal += repayMonth;

                            repaylist.add((i + 1) + "月 - " + getMoney(repayMonth));
                        }

                        repayAccrual = repayTotal - totalVal;

                        results.add(title);
                        results.add("贷款总额: " + getMoney(totalVal));
                        results.add("还款总额: " + getMoney(repayTotal));
                        results.add("支付利息: " + getMoney(repayAccrual));
                        results.add("贷款月数: " + totalMonth);
                        results.add("月均详情: ");
                        results.addAll(repaylist);

                    } else {
                        //等额本息
                        repayMonth = getRateMonthRepay(currentRate, totalVal, totalMonth);
                        repayTotal = repayMonth * totalMonth;
                        repayAccrual = repayTotal - totalVal;

                        results.add(title);
                        results.add("贷款总额: " + getMoney(totalVal));
                        results.add("还款总额: " + getMoney(repayTotal));
                        results.add("支付利息: " + getMoney(repayAccrual));
                        results.add("贷款月数: " + totalMonth);
                        results.add("月均还款: " + getMoney(repayMonth));
                    }

                } else {
                    //根据单价面积
                    String pretext = mPrePrice.getEditableText().toString();
                    float prenum = 0.0f;
                    if (!StringUtils.isEmpty(pretext)) {
                        prenum = Float.parseFloat(pretext);
                    }

                    if (prenum <= 0.0f) {
                        prenum = 0.0f;
                    }

                    String sizetext = mSize.getEditableText().toString();
                    float sizenum = 0.0f;
                    if (!StringUtils.isEmpty(sizetext)) {
                        sizenum = Float.parseFloat(sizetext);
                    }

                    if (sizenum <= 0.0f) {
                        sizenum = 0.0f;
                    }


                    totalVal = sizenum * prenum;
                    float totalLoan = totalVal * getchengshu();
                    float firstPay = totalVal - totalLoan;

                    Log.d("", "currentRate " + currentRate);

                    //等额本金
                    if (payTypeIndex == 1) {
                        ArrayList<String> repaylist = new ArrayList<String>();

                        for (int i = 0; i < totalMonth; i++) {
                            repayMonth = getSumMonthRepay(currentRate, totalLoan, totalMonth, i);
                            repayTotal = repayTotal + repayMonth;

                            repaylist.add((i + 1) + "月 - " + getMoney(repayMonth));
                        }
                        repayAccrual = repayTotal - totalLoan;

                        results.add(title);
                        results.add("贷款总额: " + getMoney(totalLoan));
                        results.add("还款总额: " + getMoney(repayTotal));
                        results.add("支付利息: " + getMoney(repayAccrual));
                        results.add("首期付款: " + getMoney(firstPay));
                        results.add("贷款月数: " + totalMonth);
                        results.add("月均详情: ");
                        results.addAll(repaylist);

                    }
                    //等额本息
                    else {

                        repayMonth = getRateMonthRepay(currentRate, totalLoan, totalMonth);
                        repayTotal = repayMonth * totalMonth;
                        repayAccrual = repayTotal - totalLoan;

                        results.add(title);
                        results.add("房款总额: " + getMoney(totalVal));
                        results.add("贷款总额: " + getMoney(totalLoan));
                        results.add("还款总额: " + getMoney(repayTotal));
                        results.add("支付利息: " + getMoney(repayAccrual));
                        results.add("首期付款: " + getMoney(firstPay));
                        results.add("贷款月数: " + totalMonth);
                        results.add("月均还款: " + getMoney(repayMonth));

                    }

                }

            }


            Intent it = new Intent();
            it.setClassName(LoanCalcActivity.this, CalcResultActivity.class.getName());
            it.putStringArrayListExtra("calcres", results);
            startActivity(it);


        }
    };

    private float getSyRate() {
        switch (ratesyTypeIndex) {
            case 0:
                return 1.3f;
            case 1:
                return 1.2f;
            case 2:
                return 1.1f;
            case 3:
                return 1.0f;
            case 4:
                return 0.9f;
            case 5:
                return 0.85f;
            case 6:
                return 0.8f;
            case 7:
                return 0.7f;
        }

        return 1.0f;
    }

    private float getchengshu() {
        return (chengshuTypeIndex + 1) * 0.1f;
    }

    //本金还款的月还款额(参数: 年利率 / 贷款总额 / 贷款总月份 / 贷款当前月0～length-1)
    private float getSumMonthRepay(float rate, float total, int month, int curMonth) {
        float monthRate = rate / 12 / 100;
        float sumMonth = total / month;
        return (total - sumMonth * curMonth) * monthRate + sumMonth;
    }

    //本息还款的月还款额(参数: 年利率/贷款总额/贷款总月份)
    private float getRateMonthRepay(float rate, float total, int month) {
        float monthRate = rate / 12 / 100;
        return (float) (total * monthRate * Math.pow((1 + monthRate), month) / (Math.pow((1 + monthRate), month) - 1));
    }

    private String getMoney(float x) {
        String str;
        DecimalFormat df = new DecimalFormat("###.000000");
        DecimalFormat df1 = new DecimalFormat("###.00");
        if (x > 10000f) {
            float y = x / 10000;
            str = df.format(y) + "万元";
        } else if (x <= 0f) {
            str = "0.00";
        } else {
            float y = x;
            str = df1.format(y) + "元";
        }
        return str;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
