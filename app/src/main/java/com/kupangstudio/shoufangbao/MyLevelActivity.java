package com.kupangstudio.shoufangbao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.FileUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Jsmi on 2015/11/24.
 * 我的等级
 */
public class MyLevelActivity extends BaseActivity implements View.OnClickListener {
    private TextView right;
    private CircleImageView header;
    private TextView now;
    private TextView name;
    private TextView ness;
    private ImageView img;
    private TextView left1;
    private TextView left2;
    private TextView right1;
    private TextView right2;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private ImageView left;
    private List<String> list;
    private long beginTime;
    private long endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mylevel);
        CommonUtils.addActivity(this);
        beginTime = System.currentTimeMillis();
        init();
        setClickListener();
        list = new LinkedList<String>();
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.center_head_default)
                .showImageForEmptyUri(R.drawable.center_head_default)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        User user = User.getInstance();
        Intent intent = getIntent();
        //userLevel = (UserLevel)intent.getSerializableExtra("level");
        File file = new File("");
        String json = FileUtils.readFileContentStr(file, this, "json");
        if (json == null) {
            list.add("V1");
            list.add("V2");
            list.add("V3");
            list.add("V4");
            list.add("V5");
            list.add("V6");
            list.add("V7");
            list.add("V8");
            list.add("V9");
            list.add("V10");
            list.add("V11");
            list.add("V12");
            list.add("V13");
            list.add("V14");
            list.add("V15");
        } else {
            JSONArray array = null;
            try {
                array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    list.add(array.getString(i));
                }
            } catch (JSONException e) {
                list.add("V1");
                list.add("V2");
                list.add("V3");
                list.add("V4");
                list.add("V5");
                list.add("V6");
                list.add("V7");
                list.add("V8");
                list.add("V9");
                list.add("V10");
                list.add("V11");
                list.add("V12");
                list.add("V13");
                list.add("V14");
                list.add("V15");
            }
        }
        now.setText(user.level);
        imageLoader.displayImage(user.face, header, options);
        ness.setText(String.valueOf(user.nextlevel));
        //会员等级的判断
        switch (User.getInstance().grade) {
            case 1:
                left1.setText("/");
                left2.setText("/");
                now.setText(list.get(0));
                right1.setText(list.get(1));
                right2.setText(list.get(2));
                name.setText("草房");
                img.setImageResource(R.drawable.point_mall_mylevel_thatched);
                break;
            case 2:
                left1.setText("/");
                left2.setText(list.get(0));
                now.setText(list.get(1));
                right1.setText(list.get(2));
                right2.setText(list.get(3));
                name.setText("草房");
                img.setImageResource(R.drawable.point_mall_mylevel_thatched);
                break;
            case 3:
                left1.setText(list.get(0));
                left2.setText(list.get(1));
                now.setText(list.get(2));
                right1.setText(list.get(3));
                right2.setText(list.get(4));
                name.setText("草房");
                img.setImageResource(R.drawable.point_mall_mylevel_thatched);
                break;
            case 4:
                left1.setText(list.get(1));
                left2.setText(list.get(2));
                now.setText(list.get(3));
                right1.setText(list.get(4));
                right2.setText(list.get(5));
                name.setText("土房");
                img.setImageResource(R.drawable.point_mall_mylevel_dirtroom);
                break;
            case 5:
                left1.setText(list.get(2));
                left2.setText(list.get(3));
                now.setText(list.get(4));
                right1.setText(list.get(5));
                right2.setText(list.get(6));
                name.setText("土房");
                img.setImageResource(R.drawable.point_mall_mylevel_dirtroom);
                break;
            case 6:
                left1.setText(list.get(3));
                left2.setText(list.get(4));
                now.setText(list.get(5));
                right1.setText(list.get(6));
                right2.setText(list.get(7));
                name.setText("土房");
                img.setImageResource(R.drawable.point_mall_mylevel_dirtroom);
                break;
            case 7:
                left1.setText(list.get(4));
                left2.setText(list.get(5));
                now.setText(list.get(6));
                right1.setText(list.get(7));
                right2.setText(list.get(8));
                name.setText("砖房");
                img.setImageResource(R.drawable.point_mall_mylevel_brickwork);
                break;
            case 8:
                left1.setText(list.get(5));
                left2.setText(list.get(6));
                now.setText(list.get(7));
                right1.setText(list.get(8));
                right2.setText(list.get(9));
                name.setText("砖房");
                img.setImageResource(R.drawable.point_mall_mylevel_brickwork);
                break;
            case 9:
                left1.setText(list.get(6));
                left2.setText(list.get(7));
                now.setText(list.get(8));
                right1.setText(list.get(9));
                right2.setText(list.get(10));
                name.setText("砖房");
                img.setImageResource(R.drawable.point_mall_mylevel_brickwork);
                break;
            case 10:
                left1.setText(list.get(7));
                left2.setText(list.get(8));
                now.setText(list.get(9));
                right1.setText(list.get(10));
                right2.setText(list.get(11));
                name.setText("小区");
                img.setImageResource(R.drawable.point_mall_mylevel_plot);
                break;
            case 11:
                left1.setText(list.get(8));
                left2.setText(list.get(9));
                now.setText(list.get(10));
                right1.setText(list.get(11));
                right2.setText(list.get(12));
                name.setText("小区");
                img.setImageResource(R.drawable.point_mall_mylevel_plot);
                break;
            case 12:
                left1.setText(list.get(9));
                left2.setText(list.get(10));
                now.setText(list.get(11));
                right1.setText(list.get(12));
                right2.setText(list.get(13));
                name.setText("小区");
                img.setImageResource(R.drawable.point_mall_mylevel_plot);
                break;
            case 13:
                left1.setText(list.get(10));
                left2.setText(list.get(11));
                now.setText(list.get(12));
                right1.setText(list.get(13));
                right2.setText(list.get(14));
                name.setText("别墅");
                img.setImageResource(R.drawable.point_mall_mylevel_cottage);
                break;
            case 14:
                left1.setText(list.get(11));
                left2.setText(list.get(12));
                now.setText(list.get(13));
                right1.setText(list.get(14));
                right2.setText("/");
                name.setText("别墅");
                img.setImageResource(R.drawable.point_mall_mylevel_cottage);
                break;
            case 15:
                left1.setText(list.get(12));
                left2.setText(list.get(13));
                now.setText(list.get(14));
                right1.setText("/");
                right2.setText("/");
                name.setText("别墅");
                img.setImageResource(R.drawable.point_mall_mylevel_cottage);
                break;
            default:
                left1.setText("/");
                left2.setText("/");
                now.setText("/");
                right1.setText("/");
                right2.setText("/");
                name.setText("无网络的房子");
                img.setImageResource(R.drawable.point_mall_mylevel_thatched);
                break;
        }
    }

    private void init() {
        right = (TextView) findViewById(R.id.navbar_image_right);
        left = (ImageView) findViewById(R.id.navbar_image_left);
        header = (CircleImageView) findViewById(R.id.my_level_header);
        now = (TextView) findViewById(R.id.my_level_now);
        left1 = (TextView) findViewById(R.id.my_level_left_1);
        left2 = (TextView) findViewById(R.id.my_level_left_2);
        right1 = (TextView) findViewById(R.id.my_level_right_1);
        right2 = (TextView) findViewById(R.id.my_level_right_2);
        name = (TextView) findViewById(R.id.my_level_name);
        ness = (TextView) findViewById(R.id.my_level_ness);
        img = (ImageView) findViewById(R.id.my_level_img);
    }

    private void setClickListener() {
        right.setOnClickListener(this);
        left.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        endTime = System.currentTimeMillis();
        int duration = (int) ((endTime - beginTime) / 1000);
        Map<String, String> map_value = new HashMap<String, String>();
        MobclickAgent.onEventValue(this, "mygrade", map_value, duration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.navbar_image_right:
                //等级规则
                Intent intent = new Intent(MyLevelActivity.this, CommonRuleActivity.class);
                intent.putExtra("title", "积分规则");
                intent.putExtra("url", Constants.INTEGRAL_RULE);
                startActivity(intent);
                break;
            case R.id.navbar_image_left:
                finish();
                break;
            default:
                break;
        }
    }

}
