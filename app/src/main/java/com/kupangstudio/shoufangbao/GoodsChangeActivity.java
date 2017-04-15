package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.GoodsDetail;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * Created by Jsmi on 2015/11/24.
 * 商品兑换
 */
public class GoodsChangeActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private EditText name;
    private EditText mobile;
    private Button moneyExchange;
    private Button integralExchange;
    private Button comeExchange;
    private EditText address;
    private TextView goods;
    private Button dec;
    private EditText num;
    private Button plus;
    private TextView integral;
    private TextView freight;
    private CheckBox rule;
    private Button submit;
    private int FLAG;
    private int FLAG_COUNT;
    private boolean CHECK = true;
    private int count = 1;
    private int stock;
    private LinearLayout addressLayout;
    private View addressLine;
    private GoodsDetail goodsDetail;
    private int point;
    private int userIntegral;
    private TextView ruleTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_change);
        CommonUtils.addActivity(this);
        init();
        setClickListener();
        CommonUtils.handleTitleBarRightGone(this, "兑换详情");
        Intent intent = getIntent();
        goodsDetail = (GoodsDetail) intent.getSerializableExtra("goodsdetail");
        userIntegral = User.getInstance().interage;
        if (goodsDetail != null) {
            User user = User.getInstance();
            stock = goodsDetail.getStock();
            point = goodsDetail.getIntegrate();
            name.setText(user.realName);
            mobile.setText(user.mobile);
            goods.setText(goodsDetail.getTitle());
            integral.setText(String.valueOf(point) + "积分");
            num.setText(String.valueOf(count));
            freight.setText("运费自付");
            ruleTxt.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
            rule.setChecked(true);
            moneyExchange.setSelected(true);
            integralExchange.setSelected(false);
            comeExchange.setSelected(false);
        }

        //地址的滑动事件屏蔽
        address.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // TODO Auto-generated method stub
                if (view.getId() == R.id.consignee_address) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
    }

    private void init() {
        name = (EditText) findViewById(R.id.consignee_name);
        mobile = (EditText) findViewById(R.id.consignee_mobile);
        moneyExchange = (Button) findViewById(R.id.consignee_money_exchange);
        integralExchange = (Button) findViewById(R.id.consignee_integral_enchange);
        comeExchange = (Button) findViewById(R.id.consignee_come_exchnge);
        address = (EditText) findViewById(R.id.consignee_address);
        addressLayout = (LinearLayout) findViewById(R.id.consignee_address_layout);
        addressLine = findViewById(R.id.consignee_address_line);
        goods = (TextView) findViewById(R.id.consignee_goods);
        dec = (Button) findViewById(R.id.consignee_dec);
        num = (EditText) findViewById(R.id.consignee_num);
        plus = (Button) findViewById(R.id.consignee_plus);
        integral = (TextView) findViewById(R.id.consignee_integral);
        freight = (TextView) findViewById(R.id.consignee_freight);
        rule = (CheckBox) findViewById(R.id.consignee_select);
        submit = (Button) findViewById(R.id.consignee_submit);
        ruleTxt = (TextView) findViewById(R.id.consignee_exchange_rule);
    }

    private void setClickListener() {
        rule.setOnCheckedChangeListener(this);
        moneyExchange.setOnClickListener(this);
        integralExchange.setOnClickListener(this);
        comeExchange.setOnClickListener(this);
        dec.setOnClickListener(this);
        plus.setOnClickListener(this);
        submit.setOnClickListener(this);
        ruleTxt.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        //售房宝兑换规则
        CHECK = b;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.consignee_money_exchange://货到付款
                FLAG = 0;
                FLAG_COUNT = 0;
                addressLayout.setVisibility(View.VISIBLE);
                addressLine.setVisibility(View.VISIBLE);
                freight.setText("运费自付");
                moneyExchange.setSelected(true);
                integralExchange.setSelected(false);
                comeExchange.setSelected(false);
                break;
            case R.id.consignee_integral_enchange://积分抵换
                FLAG = 1;
                FLAG_COUNT = goodsDetail.getFreight();
                addressLayout.setVisibility(View.VISIBLE);
                addressLine.setVisibility(View.VISIBLE);
                freight.setText(String.valueOf(goodsDetail.getFreight()) + "积分");
                moneyExchange.setSelected(false);
                integralExchange.setSelected(true);
                comeExchange.setSelected(false);
                break;
            case R.id.consignee_come_exchnge://上门取件
                FLAG = 2;
                FLAG_COUNT = 0;
                addressLayout.setVisibility(View.GONE);
                addressLine.setVisibility(View.GONE);
                freight.setText("无需运费");
                moneyExchange.setSelected(false);
                integralExchange.setSelected(false);
                comeExchange.setSelected(true);
                break;
            case R.id.consignee_dec://降低商品数量
                if (count == 1) {
                    return;
                }
                count--;
                integral.setText(String.valueOf(point * count) + "积分");
                num.setText(String.valueOf(count));
                break;
            case R.id.consignee_plus://增加商品数量
                if (count == stock) {
                    Toast.makeText(GoodsChangeActivity.this, "已达库存上限！", Toast.LENGTH_SHORT).show();
                    return;
                }
                count++;
                integral.setText(String.valueOf(point * count) + "积分");
                num.setText(String.valueOf(count));
                break;
            case R.id.consignee_submit://确定兑换
                if (name.getText().toString().equals("")) {
                    Toast.makeText(GoodsChangeActivity.this, "请输入收件人姓名！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mobile.getText().toString().equals("")) {
                    Toast.makeText(GoodsChangeActivity.this, "请输入收件人电话！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (FLAG != 2) {
                    if (address.getText().toString().equals("")) {
                        Toast.makeText(GoodsChangeActivity.this, "请输入收件人地址！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (FLAG == 1) {
                    if (userIntegral < point * count + goodsDetail.getFreight()) {
                        Toast.makeText(GoodsChangeActivity.this, "您的积分不足！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (!CHECK) {
                    Toast.makeText(GoodsChangeActivity.this, "请勾选售房宝积分兑换规则！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (userIntegral < point * count) {
                    Toast.makeText(GoodsChangeActivity.this, "您的积分不足！", Toast.LENGTH_SHORT).show();
                    return;
                }
                String names = name.getText().toString();
                String mobiles = mobile.getText().toString();
                String addresses = address.getText().toString();
                getData(names, mobiles, addresses);
                break;
            case R.id.consignee_exchange_rule://规则
                Intent intent = new Intent(GoodsChangeActivity.this, CommonRuleActivity.class);
                intent.putExtra("title", "积分规则");
                intent.putExtra("url", Constants.INTEGRAL_RULE);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    //实物商品兑换
    private void getData(String name, String tel, String address) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "exchangeEntity");
        map.put("module", Constants.MODULE_MALL);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        map.put("wid", String.valueOf(goodsDetail.getWid()));
        map.put("number", String.valueOf(count));
        map.put("postagetype", String.valueOf(FLAG));
        map.put("postage", String.valueOf(FLAG_COUNT));
        map.put("name", name);
        map.put("tel", tel);
        map.put("address", address);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(GoodsChangeActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            Toast.makeText(GoodsChangeActivity.this, notice, Toast.LENGTH_SHORT).show();
                            if(code > Result.RESULT_OK) {
                                EventBus.getDefault().post(goodsDetail);
                                EventBus.getDefault().post(User.getInstance());
                                finish();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(GoodsChangeActivity.this, ResultError.MESSAGE_ERROR
                            ,Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
