package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.StringUtils;
import com.kupangstudio.shoufangbao.widget.AppDialog;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

/**
 * Created by Jsmi on 2015/11/23.
 * 税费计算器
 */
public class TaxCalcActivity extends BaseActivity implements View.OnClickListener {
    EditText mPrice;
    EditText mSize;
    TextView mRate;
    RelativeLayout mLayout;
    Button mButton;


    float rate = 0;
    private int flag_style = 0;
    private int flag_jizheng = 0;
    private int flag_mianji = 0;
    private int flag_five = 2;
    private int flag_first = 0;
    private int flag_weiyi = 0;
    private TextView mnew;
    private TextView mold;
    private LinearLayout new_jisuanqi;
    private LinearLayout old_jisuanqi;
    private EditText old_price;
    private EditText old_size;
    private EditText old_tolprice;
    private TextView style_show;
    private RelativeLayout style_selcet;
    private RelativeLayout jizheng_selcet;
    private TextView jizheng_show;
    private RadioButton first_yes;
    private RadioButton first_no;
    private RadioButton weiyi_yes;
    private RadioButton weiyi_no;
    private Button chongzhi;
    private Button mButton_old;
    private TextView year_show;
    private RelativeLayout year_selcet;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setContentView(R.layout.activity_tax_calc);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "税费计算器");

        mnew = (TextView) findViewById(R.id.taxcalc_new);
        mold = (TextView) findViewById(R.id.taxcalc_old);
        mnew.setOnClickListener(this);
        mold.setOnClickListener(this);


        new_jisuanqi = (LinearLayout) findViewById(R.id.taxcalc_new_jisuanqi);
        old_jisuanqi = (LinearLayout) findViewById(R.id.taxcalc_old_jisuanqi);

        old_price = (EditText) findViewById(R.id.taxcalc_old_price);
        old_size = (EditText) findViewById(R.id.taxcalc_old_size);
        old_tolprice = (EditText) findViewById(R.id.taxcalc_old_tolprice);

        jizheng_selcet = (RelativeLayout) findViewById(R.id.taxc_jizheng);
        jizheng_show = (TextView) findViewById(R.id.tax_jizheng_item);

        style_selcet = (RelativeLayout) findViewById(R.id.taxc_style);
        style_show = (TextView) findViewById(R.id.tax_style_item);

        year_selcet = (RelativeLayout) findViewById(R.id.taxc_year);
        year_show = (TextView) findViewById(R.id.tax_year_item);

        first_yes = (RadioButton) findViewById(R.id.taxc_select_first_yes);
        first_no = (RadioButton) findViewById(R.id.taxc_select_first_no);
        weiyi_yes = (RadioButton) findViewById(R.id.taxc_select_weiyi_yes);
        weiyi_no = (RadioButton) findViewById(R.id.taxc_select_weiyi_no);

        mPrice = (EditText) this.findViewById(R.id.taxcalc_price);
        mSize = (EditText) this.findViewById(R.id.taxcalc_size);
        mRate = (TextView) this.findViewById(R.id.taxcalc_ratetext);
        mLayout = (RelativeLayout) this.findViewById(R.id.taxcalc_ratelayout);
        mButton = (Button) this.findViewById(R.id.taxcalc_button);
        mButton_old = (Button) findViewById(R.id.taxcalc_button_old);
        chongzhi = (Button) findViewById(R.id.taxcalc_button_chongzhi);
        chongzhi.setOnClickListener(this);

        init();
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

    public void init() {
        new_jisuanqi.setVisibility(View.VISIBLE);
        old_jisuanqi.setVisibility(View.GONE);
        mButton.setVisibility(View.VISIBLE);
        mButton_old.setVisibility(View.GONE);
        mnew.setSelected(true);
        mold.setSelected(false);

        rate = 0.015f;
        mRate.setText("1.5%");
        mButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String ptext = mPrice.getEditableText().toString();
                String stext = mSize.getEditableText().toString();
                if (StringUtils.isEmpty(ptext)) {
                    Toast.makeText(getBaseContext(), "单价不能为空",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (StringUtils.isEmpty(stext)) {
                    Toast.makeText(getBaseContext(), "面积不能为空",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                float p = Float.parseFloat(ptext);
                float s = Float.parseFloat(stext);

                if (p <= 0) {
                    p = 0;
                }

                if (s <= 0) {
                    s = 0;
                }

                float total = p * s;
                ArrayList<String> res = new ArrayList<String>();
                res.add("税费计算结果(单位 元)");
                res.add("房款总价：" + getMoney(total));
                res.add("印花税：" + getMoney(total * 0.0005f));
                res.add("公证税：" + getMoney(total * 0.003f));
                res.add("契税：" + getMoney(total * rate));
                res.add("税费总价：" + getMoney(total * (0.0035f + rate)));

                Intent it = new Intent();
                it.setClassName(TaxCalcActivity.this,
                        CalcResultActivity.class.getName());
                it.putStringArrayListExtra("calcres", res);
                startActivity(it);

            }

        });

        mLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                View dialogView1 = LayoutInflater.from(TaxCalcActivity.this).inflate(R.layout.common_dialog_list, null);
                final AppDialog.Builder builder = new AppDialog.Builder(TaxCalcActivity.this,
                        AppDialog.Builder.SINGLECHOICE, getResources().getStringArray(R.array.taxrates));
                builder.setContentView(dialogView1).setItemClick(
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                switch (position) {
                                    case 0:
                                        rate = 0.010f;
                                        mRate.setText("1.0%");
                                        break;
                                    case 1:
                                        rate = 0.015f;
                                        mRate.setText("1.5%");
                                        break;
                                    case 2:
                                        rate = 0.030f;
                                        mRate.setText("3.0%");
                                        break;
                                }
                                builder.dismiss();
                            }
                        }
                ).create();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.taxcalc_button_chongzhi:
                rate = 0.015f;
                mRate.setText("1.5%");
                mPrice.setText("");
                mSize.setText("");
                old_price.setText("");
                old_size.setText("");
                old_tolprice.setText("");
                first_yes.setChecked(true);
                first_no.setChecked(false);
                weiyi_yes.setChecked(true);
                weiyi_no.setChecked(false);
                jizheng_show.setText("总价");
                style_show.setText("普通住宅");
                year_show.setText("两年以下");
                rate = 0;
                flag_style = 0;
                flag_jizheng = 0;
                flag_mianji = 0;
                flag_five = 2;
                flag_first = 0;
                flag_weiyi = 0;
                break;
            case R.id.taxcalc_new:
                init();
                break;
            case R.id.taxcalc_old:
                new_jisuanqi.setVisibility(View.GONE);
                old_jisuanqi.setVisibility(View.VISIBLE);
                mButton.setVisibility(View.GONE);
                mButton_old.setVisibility(View.VISIBLE);

                mnew.setSelected(false);
                mold.setSelected(true);

                first_yes.setChecked(true);
                first_no.setChecked(false);
                weiyi_yes.setChecked(true);
                weiyi_no.setChecked(false);
                jizheng_show.setText("总价");
                style_show.setText("普通住宅");
                year_show.setText("两年以下");

                style_selcet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View dialogView2 = LayoutInflater.from(TaxCalcActivity.this).inflate(R.layout.common_dialog_list, null);
                        final AppDialog.Builder builder = new AppDialog.Builder(TaxCalcActivity.this,
                                AppDialog.Builder.SINGLECHOICE, getResources().getStringArray(R.array.styles));
                        builder.setContentView(dialogView2).setItemClick(
                                new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        switch (position) {
                                            case 0:
                                                flag_style = 0;
                                                style_show.setText("普通住宅");
                                                break;
                                            case 1:
                                                flag_style = 1;
                                                style_show.setText("非普通住宅");
                                                break;
                                            case 2:
                                                flag_style = 2;
                                                style_show.setText("经济适用房");
                                                break;
                                        }
                                        builder.dismiss();
                                    }
                                }
                        ).create();
                    }

                });
                jizheng_selcet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View dialogView5 = LayoutInflater.from(TaxCalcActivity.this).inflate(R.layout.common_dialog_list, null);
                        final AppDialog.Builder builder = new AppDialog.Builder(TaxCalcActivity.this,
                                AppDialog.Builder.SINGLECHOICE, getResources().getStringArray(R.array.jizheng));
                        builder.setContentView(dialogView5).setItemClick(
                                new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        switch (position) {
                                            case 0:
                                                flag_jizheng = 0;
                                                jizheng_show.setText("总价");
                                                break;
                                            case 1:
                                                flag_jizheng = 1;
                                                jizheng_show.setText("差价");
                                                break;
                                        }
                                        builder.dismiss();
                                    }
                                }
                        ).create();
                    }

                });

                year_selcet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View dialogView3 = LayoutInflater.from(TaxCalcActivity.this).inflate(R.layout.common_dialog_list, null);
                        final AppDialog.Builder builder = new AppDialog.Builder(TaxCalcActivity.this,
                                AppDialog.Builder.SINGLECHOICE, getResources().getStringArray(R.array.years));
                        builder.setContentView(dialogView3).setItemClick(
                                new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        switch (position) {
                                            case 0:
                                                flag_five = 2;
                                                year_show.setText("两年以下");
                                                break;
                                            case 1:
                                                flag_five = 1;
                                                year_show.setText("两年至五年");
                                                break;
                                            case 2:
                                                flag_five = 0;
                                                year_show.setText("五年以上");
                                                break;
                                        }
                                        builder.dismiss();
                                    }
                                }
                        ).create();
                    }

                });

                first_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        first_yes.setSelected(true);
                        first_no.setSelected(false);
                        flag_first = 0;
                    }
                });
                first_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        first_yes.setSelected(false);
                        first_no.setSelected(true);
                        flag_first = 1;
                    }
                });

                weiyi_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        weiyi_yes.setSelected(true);
                        weiyi_no.setSelected(false);
                        flag_weiyi = 0;
                    }
                });
                weiyi_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        weiyi_yes.setSelected(false);
                        weiyi_no.setSelected(true);
                        flag_weiyi = 1;
                    }
                });
                mButton_old.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String ptext = old_price.getEditableText().toString();
                        String stext = old_size.getEditableText().toString();
                        String tolptext = old_tolprice.getEditableText().toString();
                        if (StringUtils.isEmpty(ptext)) {
                            Toast.makeText(getBaseContext(), "单价不能为空",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (StringUtils.isEmpty(stext)) {
                            Toast.makeText(getBaseContext(), "面积不能为空",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (Float.parseFloat(stext) > 140 && flag_five == 0 && StringUtils.isEmpty(tolptext)) {
                            Toast.makeText(getBaseContext(), "原总价不能为空",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (flag_jizheng == 1 && StringUtils.isEmpty(tolptext)) {
                            Toast.makeText(getBaseContext(), "原总价不能为空",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        float p = Float.parseFloat(ptext);
                        float s = Float.parseFloat(stext);
                        float t = 0;
                        if (tolptext != null && tolptext.trim().length() > 0) {
                            t = Float.parseFloat(tolptext) * 10000f;
                        }
                        float total = p * s;
                        float total_shuifei;

                        if (s < 90) {
                            flag_mianji = 0;
                        } else if (s > 140) {
                            flag_mianji = 2;
                        } else {
                            flag_mianji = 1;
                        }

                        ArrayList<String> text = new ArrayList<String>();
                        ArrayList<String> res = new ArrayList<String>();
                        res.add("税费计算结果(单位 元)");
                        res.add("房款总价：" + getMoney(total));
                        if (flag_style == 0) {
                            total_shuifei = 0;
                            res.add("印花税：免征");
                            res.add("工本印花税：5.0");
                            total_shuifei = total_shuifei + 5.0f;
                            res.add("综合地价款：免征");
                            if (flag_five == 2) {
                                res.add("营业税：" + getMoney(total * 0.056f));
                                total_shuifei = total_shuifei + getMoneyNum(total * 0.056f);
                            } else {
                                if (s > 140) {
                                    res.add("营业税：" + getMoney((total - t) * 0.056f));
                                    total_shuifei = total_shuifei + getMoneyNum((total - t) * 0.056f);
                                } else {
                                    res.add("营业税：免征");
                                }
                            }

                            if (flag_five == 0) {
                                if (flag_weiyi == 0) {
                                    res.add("个人所得税：免征");
                                } else {
                                    if (flag_jizheng == 0) {
                                        res.add("个人所得税：" + getMoney(total * 0.010f));
                                        total_shuifei = total_shuifei + getMoneyNum(total * 0.010f);
                                    } else {
                                        if ((total - t) * 0.200f < 0) {
                                            res.add("个人所得税：" + 0);
                                        } else {
                                            res.add("个人所得税：" + getMoney((total - t) * 0.200f));
                                            total_shuifei = total_shuifei + getMoneyNum((total - t) * 0.200f);
                                        }
                                    }
                                }
                            } else {
                                if (flag_jizheng == 0) {
                                    res.add("个人所得税：" + getMoney(total * 0.010f));
                                    total_shuifei = total_shuifei + getMoneyNum(total * 0.010f);
                                } else {
                                    if ((total - t) * 0.200f < 0) {
                                        res.add("个人所得税：" + 0);
                                    } else {
                                        res.add("个人所得税：" + getMoney((total - t) * 0.200f));
                                        total_shuifei = total_shuifei + getMoneyNum((total - t) * 0.200f);
                                    }

                                }
                            }

                            if (flag_first == 0) {

                                if (flag_mianji == 0) {
                                    res.add("契税：" + getMoney(total * 0.010f));
                                    total_shuifei = total_shuifei + getMoneyNum(total * 0.010f);
                                } else if (flag_mianji == 1) {
                                    res.add("契税：" + getMoney(total * 0.015f));
                                    total_shuifei = total_shuifei + getMoneyNum(total * 0.015f);
                                } else {
                                    res.add("契税：" + getMoney(total * 0.030f));
                                    total_shuifei = total_shuifei + getMoneyNum(total * 0.030f);
                                }

                            } else {
                                res.add("契税：" + getMoney(total * 0.030f));
                                total_shuifei = total_shuifei + getMoneyNum(total * 0.030f);
                            }

                            res.add("税费总价：" + getMoney(total_shuifei));
                            text.add("备注:\n        1.北京5环以内单价39600，总价468万，5环—6环单价31680" +
                                    "，总价374.4万，6环之外单价23670，总价280.8万，满足其中一项，" +
                                    "即为普通住宅；\n        2.【北京成本价购买的已购公房】土地出让金为：" +
                                    "15.6*建筑面积，【北京优惠价购买的已购公房】土地出让金为：15.6*" +
                                    "建筑面积*6；\n        3.另外，北京二手房普通住宅税费需缴纳工本费为90元，" +
                                    "经济适用房满5年上市，需要缴纳地区指导价的10%。");
                        } else if (flag_style == 1) {
                            total_shuifei = 0;
                            res.add("印花税：免征");
                            res.add("工本印花税：5.0");
                            total_shuifei = total_shuifei + 5.0f;
                            res.add("综合地价款：免征");
                            if (flag_five == 2) {
                                res.add("营业税：" + getMoney(total * 0.056f));
                                total_shuifei = total_shuifei + getMoneyNum(total * 0.056f);
                            } else {
                                res.add("营业税：" + getMoney((total - t) * 0.056f));
                                total_shuifei = total_shuifei + getMoneyNum((total - t) * 0.056f);
                            }
                            if (flag_five == 0) {
                                if (flag_weiyi == 0) {
                                    res.add("个人所得税：" + getMoney(total * 0.010f));
                                    total_shuifei = total_shuifei + getMoneyNum(total * 0.010f);
                                } else {
                                    if (flag_jizheng == 0) {
                                        res.add("个人所得税：" + getMoney(total * 0.010f));
                                        total_shuifei = total_shuifei + getMoneyNum(total * 0.010f);
                                    } else {
                                        if ((total - t) * 0.200f < 0) {
                                            res.add("个人所得税：" + 0);
                                        } else {
                                            res.add("个人所得税：" + getMoney((total - t) * 0.200f));
                                            total_shuifei = total_shuifei + getMoneyNum((total - t) * 0.200f);
                                        }

                                    }
                                }
                            } else {
                                if (flag_jizheng == 0) {
                                    res.add("个人所得税：" + getMoney(total * 0.010f));
                                    total_shuifei = total_shuifei + getMoneyNum(total * 0.010f);
                                } else {
                                    if ((total - t) * 0.200f < 0) {
                                        res.add("个人所得税：" + 0);
                                    } else {
                                        res.add("个人所得税：" + getMoney((total - t) * 0.200f));
                                        total_shuifei = total_shuifei + getMoneyNum((total - t) * 0.200f);
                                    }

                                }
                            }
                            res.add("契税：" + getMoney(total * 0.030f));
                            total_shuifei = total_shuifei + getMoneyNum(total * 0.030f);
                            res.add("税费总价：" + getMoney(total_shuifei));
                            text.add("备注:\n        1.北京5环以内单价39600，总价468万；5环—6环单价31680" +
                                    "，总价374.4万；6环之外单价23670，总价280.8万。满足其中一项，" +
                                    "即为普通住宅；\n        2.【北京成本价购买的已购公房】土地出让金为：" +
                                    "15.6*建筑面积；【北京优惠价购买的已购公房】土地出让金为：15.6*" +
                                    "建筑面积*6；\n        3.另外，北京二手房普通住宅税费需缴纳工本费为90元；" +
                                    "经济适用房满5年上市，需要缴纳地区指导价的10%。");
                        } else {
                            total_shuifei = 0;
                            res.add("印花税：免征");
                            res.add("工本印花税：5.0");
                            total_shuifei = total_shuifei + 5.0f;
                            if (flag_five == 0) {
                                res.add("综合地价款：" + getMoney(total * 0.10f));
                                total_shuifei = total_shuifei + getMoneyNum(total * 0.10f);
                            } else {
                                res.add("综合地价款：免征");
                            }
                            if (s > 140) {
                                res.add("营业税：" + getMoney((total - t) * 0.056f));
                                total_shuifei = total_shuifei + getMoneyNum((total - t) * 0.056f);
                            } else {
                                res.add("营业税：免征");
                            }
                            if (flag_weiyi == 0) {
                                res.add("个人所得税：免征");
                            } else {
                                res.add("个人所得税：" + getMoney(total * 0.010f));
                                total_shuifei = total_shuifei + getMoneyNum(total * 0.010f);
                            }
                            if (flag_first == 0) {

                                if (flag_mianji == 0) {
                                    res.add("契税：" + getMoney(total * 0.010f));
                                    total_shuifei = total_shuifei + getMoneyNum(total * 0.010f);
                                } else if (flag_mianji == 1) {
                                    res.add("契税：" + getMoney(total * 0.015f));
                                    total_shuifei = total_shuifei + getMoneyNum(total * 0.015f);
                                } else {
                                    res.add("契税：" + getMoney(total * 0.030f));
                                    total_shuifei = total_shuifei + getMoneyNum(total * 0.030f);
                                }
                            } else {
                                res.add("契税：" + getMoney(total * 0.030f));
                                total_shuifei = total_shuifei + getMoneyNum(total * 0.030f);
                            }
                            res.add("税费总价：" + getMoney(total_shuifei));
                            text.add("备注:\n        1.北京5环以内单价39600，总价468万；5环—6环单价31680" +
                                    "，总价374.4万；6环之外单价23670，总价280.8万。满足其中一项，" +
                                    "即为普通住宅；\n       2.【北京成本价购买的已购公房】土地出让金为：" +
                                    "15.6*建筑面积；【北京优惠价购买的已购公房】土地出让金为：15.6*" +
                                    "建筑面积*6；\n        3.另外，北京二手房普通住宅税费需缴纳工本费为90元；" +
                                    "经济适用房满5年上市，需要缴纳地区指导价的10%。");
                        }
                        Intent intent = new Intent(TaxCalcActivity.this, CalcResultActivity.class);
                        intent.putStringArrayListExtra("calcres", res);
                        //intent.putStringArrayListExtra("text", text);
                        startActivity(intent);
                    }
                });

                break;
            default:
                break;
        }
    }

    private String getMoney(float x) {
        String str;
        if (x > 10000f) {
            float y = x / 10000;
            str = y + "万元";
        } else if (x <= 0f) {
            str = String.valueOf(0);
        } else {
            float y = x;
            str = String.valueOf(y);
        }
        return str;
    }

    private float getMoneyNum(float x) {
        float y;
        if(x > 0f){
            y = x;
        }else {
            y = 0f;
        }
        return y;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
