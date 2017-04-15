package com.kupangstudio.shoufangbao;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

/**
 * Created by Jsmi on 2015/11/23.
 * 计算结果
 */
public class CalcResultActivity extends BaseActivity {

    ListView mListview;
    ArrayList<String> results;
    ArrayList<String> result;


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle b = getIntent().getExtras();
            results = b.getStringArrayList("calcres");
            //resultguess = b.getStringArrayList("text");
        }

        if (results == null) {
            results = new ArrayList<String>();
        }
        CommonUtils.addActivity(this);
        this.setContentView(R.layout.activity_calc_res);

        TextView title = (TextView) findViewById(R.id.navbar_title);
        title.setText("计算结果");
        ImageView leftbutton = (ImageView) findViewById(R.id.navbar_image_left);
        leftbutton.setImageResource(R.drawable.common_titlebar_left);
        leftbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //返回钮
                finish();
            }
        });

        ImageView rightButton = (ImageView) findViewById(R.id.navbar_image_right);
        rightButton.setVisibility(View.GONE);

        TextView beizhu = (TextView) findViewById(R.id.jisuanqi_beizhu);
        if (result != null) {
            beizhu.setVisibility(View.VISIBLE);
        }else {
            beizhu.setVisibility(View.GONE);
        }
        mListview = (ListView) this.findViewById(R.id.calcres_list);
        mListview.setAdapter(new CalcResAdapter(this));
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
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    class CalcResAdapter extends BaseAdapter {

        LayoutInflater inflater;

        public CalcResAdapter(Context ctx) {
            inflater = LayoutInflater.from(ctx);
        }


        @Override
        public int getCount() {
            return results.size();
        }

        @Override
        public Object getItem(int pos) {
            return results.get(pos);
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
                contentView = inflater.inflate(R.layout.item_calc_res, parent, false);
                holder.title = (TextView) contentView.findViewById(R.id.calcres_text);
                holder.title.getPaint().setFakeBoldText(true);
                contentView.setTag(holder);
            }

            holder.title.setText(results.get(pos));

            return contentView;
        }


        class ViewHolder {
            TextView title;
        }

    }

}

