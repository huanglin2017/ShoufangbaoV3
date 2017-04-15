package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by long1 on 15/11/21.
 * Copyright 15/11/21 android_xiaobai.
 * 工具中心
 */
public class ToolsActivity extends BaseActivity {

    private ListView lv;
    private int[] image;
    private String[] name;
    private String[] detail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);
        CommonUtils.addActivity(this);
        CommonUtils.handleTitleBarRightGone(this, "工具中心");
        lv = (ListView) findViewById(R.id.lv_tools);
        image = new int[]{R.drawable.tools_cal_fang,
                R.drawable.tools_cal_tax};
        name = new String[]{"房贷计算器", "税费计算器"};
        detail = new String[]{"房屋贷款精准计算", "税费精准计算"};

        SimpleAdapter adapter = new SimpleAdapter(this, initData(),
                R.layout.item_activity_tools, new String[]{"img", "name",
                "detail"}, new int[]{R.id.item_tools_iv, R.id.item_tools_name,
                R.id.item_tools_name_detail});
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map;
                Intent intent;
                switch (position) {
                    case 0://房贷计算器
                        map = new HashMap<String, String>();
                        map.put("type", "loancount");
                        MobclickAgent.onEvent(ToolsActivity.this, "mytoolsoptionclick", map);
                        intent = new Intent(ToolsActivity.this, LoanCalcActivity.class);
                        startActivity(intent);
                        break;
                    case 1://税费计算器
                        map = new HashMap<String, String>();
                        map.put("type", "taxcount");
                        MobclickAgent.onEvent(ToolsActivity.this, "mytoolsoptionclick", map);
                        intent = new Intent(ToolsActivity.this, TaxCalcActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private List<Map<String, Object>> initData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        for (int i = 0; i < image.length; i++) {
            map = new HashMap<String, Object>();
            map.put("img", image[i]);
            map.put("name", name[i]);
            map.put("detail", detail[i]);
            list.add(map);
        }
        return list;
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
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
