package com.kupangstudio.shoufangbao;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import com.kupangstudio.shoufangbao.model.Answer;
import com.kupangstudio.shoufangbao.model.GuessHappy;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.network.ResultError;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.widget.PullToRefreshListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Jsmi on 2015/11/25.
 * 猜猜乐列表
 */
public class GuessHappyActivity extends BaseActivity {
    private PullToRefreshListView refreshListView;
    private ListView lv;
    private List<GuessHappy> list;
    private MyAdapter adapter;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;
    private LinearLayout emptyview;
    private RelativeLayout loadingLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_happy);
        CommonUtils.addActivity(this);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        CommonUtils.handleTitleBarRightGone(this, "猜猜乐");
        initView();
        list = new ArrayList<GuessHappy>();
        getGuessHappyListData();
    }

    private void initView() {
        refreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_listview);
        refreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        loadingLayout = (RelativeLayout) findViewById(R.id.exchange_record_loading);
        refreshListView.getLoadingLayoutProxy().setRefreshingLabel("正在刷新...");
        refreshListView.getLoadingLayoutProxy().setReleaseLabel("下拉刷新");
        refreshListView.getLoadingLayoutProxy().setPullLabel("释放立即刷新");
        lv = refreshListView.getRefreshableView();
        lv.setDividerHeight(1);
        emptyview = (LinearLayout) findViewById(R.id.guess_empty_layout);
        emptyview.setVisibility(View.GONE);
        refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                emptyview.setVisibility(View.GONE);
                list.clear();
                getGuessHappyListData();
            }

        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GuessHappy guessHappy = list.get(i);
                if (guessHappy.getType() == 1) {//猜一猜
                    showQuestionDialog(GuessHappyActivity.this, guessHappy);
                } else if (guessHappy.getType() == 2) {//乐一乐
                    showHappyDialog(GuessHappyActivity.this, guessHappy);
                }
            }
        });
    }

    //友盟监测
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

    //猜一猜
    private void showQuestionDialog(Context ctx, final GuessHappy guessHappy) {
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(this)
                .inflate(R.layout.common_dialog_question, null);
        final TextView title = (TextView) layout.findViewById(R.id.dialog_caiyicai_content);
        ImageView close = (ImageView) layout.findViewById(R.id.dialog_caiyicai_close);
        TextView remark = (TextView) layout.findViewById(R.id.dialog_caiyicai_remark);
        ImageView ok = (ImageView) layout.findViewById(R.id.dialog_caiyicai_ok);
        ImageView ivOne = (ImageView) layout.findViewById(R.id.caiyicai_answerone_iv);
        final TextView tvOne = (TextView) layout.findViewById(R.id.caiyicai_answerone_tv);
        ImageView ivTwo = (ImageView) layout.findViewById(R.id.caiyicai_answertwo_iv);
        final TextView tvTwo = (TextView) layout.findViewById(R.id.caiyicai_answertwo_tv);
        ImageView ivThree = (ImageView) layout.findViewById(R.id.caiyicai_answerthree_iv);
        final TextView tvThree = (TextView) layout.findViewById(R.id.caiyicai_answerthree_tv);
        final ImageView answerOne = (ImageView) layout.findViewById(R.id.caiyicai_answerone_ok);
        final ImageView answerTwo = (ImageView) layout.findViewById(R.id.caiyicai_answertwo_ok);
        final ImageView answerThree = (ImageView) layout.findViewById(R.id.caiyicai_answerthree_ok);
        title.setText(guessHappy.getTittle());
        remark.setText("备注：" + guessHappy.getRemark());
        String[] url = guessHappy.getContent();
        imageLoader.displayImage(url[0], ivOne, options);
        imageLoader.displayImage(url[1], ivTwo, options);
        imageLoader.displayImage(url[2], ivThree, options);
        tvOne.setSelected(false);
        tvTwo.setSelected(false);
        tvThree.setSelected(false);
        final Dialog dialog = new AlertDialog.Builder(this).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        dialog.getWindow().setContentView(layout);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        dialog.getWindow().setGravity(Gravity.TOP);
        lp.y = (int) (44 * CommonUtils.getDensity(ctx) + 0.5f);
        dialog.getWindow().setAttributes(lp);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tvOne.isSelected() && !tvTwo.isSelected() && !tvThree.isSelected()) {
                    Toast.makeText(GuessHappyActivity.this, "请选择答案", Toast.LENGTH_SHORT).show();
                    return;
                }
                CommonUtils.setTaskDone(GuessHappyActivity.this, 80);
                int answer;
                if (tvOne.isSelected()) {
                    answer = 0;
                } else if (tvTwo.isSelected()) {
                    answer = 1;
                } else {
                    answer = 2;
                }
                Answer answer1 = new Answer();
                answer1.setAnswer(guessHappy.getAnswer());
                answer1.setError(guessHappy.getError());
                answer1.setTitle(guessHappy.getCorrect());
                getAnswerData(title, answerOne, answerTwo, answerThree, guessHappy.getKid(), answer, answer1);
            }
        });
        ivOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOne.setSelected(true);
                tvTwo.setSelected(false);
                tvThree.setSelected(false);
            }
        });
        tvOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOne.setSelected(true);
                tvTwo.setSelected(false);
                tvThree.setSelected(false);
            }
        });
        ivTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOne.setSelected(false);
                tvTwo.setSelected(true);
                tvThree.setSelected(false);
            }
        });
        tvTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOne.setSelected(false);
                tvTwo.setSelected(true);
                tvThree.setSelected(false);
            }
        });
        ivThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOne.setSelected(false);
                tvTwo.setSelected(false);
                tvThree.setSelected(true);
            }
        });
        tvThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOne.setSelected(false);
                tvTwo.setSelected(false);
                tvThree.setSelected(true);
            }
        });
    }

    //自定义的dialog
    private void showHappyDialog(Context ctx, GuessHappy guessHappy) {
        TextView dialogtitle;
        TextView dialogremark;
        ImageView dialogclose;
        View layout = LayoutInflater.from(this).inflate(R.layout.common_dialog_happy, null);
        dialogtitle = (TextView) layout.findViewById(R.id.happydialog_title);
        dialogremark = (TextView) layout.findViewById(R.id.happydialog_content);
        dialogclose = (ImageView) layout.findViewById(R.id.happydialog_close);
        dialogtitle.setText(guessHappy.getTittle());
        dialogremark.setText(guessHappy.getContent()[0]);
        final Dialog dialog = new AlertDialog.Builder(this).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        dialog.getWindow().setContentView(layout);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        dialog.getWindow().setGravity(Gravity.TOP);
        lp.y = (int) (147 * CommonUtils.getDensity(ctx) + 0.5f);
        dialog.getWindow().setAttributes(lp);
        dialogclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    class MyAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        List<GuessHappy> list;

        public MyAdapter(Context context, List<GuessHappy> list) {
            this.inflater = LayoutInflater.from(context);
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            GuessHappy guessHappy = list.get(i);
            if (view == null) {
                holder = new ViewHolder();
                view = inflater.inflate(R.layout.item_guess_happy, viewGroup, false);
                holder.guesstime = (TextView) view.findViewById(R.id.guess_time);
                holder.guesstittle = (TextView) view.findViewById(R.id.guess_tittle);
                holder.guessremark = (TextView) view.findViewById(R.id.guess_remark);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.guesstime.setText(guessHappy.getDay());
            holder.guessremark.setText(guessHappy.getTittle());
            if (guessHappy.getType() == 1) {
                holder.guesstittle.setText("猜一猜");
            } else {
                holder.guesstittle.setText("乐一乐");
            }
            return view;
        }
    }

    static class ViewHolder {
        TextView guesstime;
        TextView guesstittle;
        TextView guessremark;
    }


    //猜一猜的时候提交答案
    private void getAnswerData(final TextView resultguess, final ImageView answerOne, final ImageView answerTwo,
                               final ImageView answerThree, final int kid, int answers, final Answer answer1) {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "knowAnswer");
        map.put("module", Constants.MODULE_MALL);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        map.put("kid", String.valueOf(kid));
        map.put("answer", String.valueOf(answers));
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new AnswerCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(GuessHappyActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Result<Answer> response) {
                        if (response == null) {
                            Toast.makeText(GuessHappyActivity.this, ResultError.MESSAGE_ERROR
                                    , Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (response.getCode() > Result.RESULT_OK) {
                            for (int i = 0; i < list.size(); i++) {
                                if (kid == list.get(i).getKid()) {
                                    list.remove(i);
                                    if (adapter == null) {
                                        adapter = new MyAdapter(GuessHappyActivity.this, list);
                                        lv.setAdapter(adapter);
                                    } else {
                                        adapter.list = list;
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                            Answer answer = response.getContent();
                            answer.setTitle(answer1.getTitle());
                            answer.setError(answer1.getError());
                            answer.setAnswer(answer1.getAnswer());
                            String str;
                            if (answer.getAnswer() == 0) {
                                answerOne.setVisibility(View.VISIBLE);
                                str = "A";
                            } else if (answer.getAnswer() == 1) {
                                answerTwo.setVisibility(View.VISIBLE);
                                str = "B";
                            } else {
                                answerThree.setVisibility(View.VISIBLE);
                                str = "C";
                            }
                            if (response.getCode() == 2073) {
                                SpannableString string = new SpannableString(answer.getTitle() + "+" +
                                        answer.getIntegrate() + "积分" + "\n" + "正确答案：" + str);
                                int start = answer.getTitle().length();
                                int end = start + String.valueOf(answer.getIntegrate()).length() + 3;
                                string.setSpan(new ForegroundColorSpan(Color.parseColor("#be1a20")), start,
                                        end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                resultguess.setText(string);
                            } else {
                                resultguess.setText(answer.getError() + "\n" + "正确答案：" + str);
                            }
                        } else {
                            Toast.makeText(GuessHappyActivity.this, response.getNotice(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private abstract class AnswerCallback extends Callback<Result<Answer>> {
        @Override
        public Result<Answer> parseNetworkResponse(Response response) throws Exception {
            Result<Answer> result = null;
            String json = response.body().string();
            try {
                result = new Gson().fromJson(json,
                        new TypeToken<Result<Answer>>() {
                        }.getType());
            } catch (Exception e) {

            }
            return result;
        }
    }

    //猜一猜列表
    private void getGuessHappyListData() {
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "getWeekKnow");
        map.put("module", Constants.MODULE_MALL);
        map.put("uid", String.valueOf(User.getInstance().uid));
        map.put("id", User.getInstance().salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        loadingLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(GuessHappyActivity.this, ResultError.MESSAGE_NULL, Toast.LENGTH_SHORT).show();
                        refreshListView.onRefreshComplete();
                        emptyview.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String notice = jsonObject.getString("notice");
                            if (code > Result.RESULT_OK) {
                                JSONArray array = jsonObject.getJSONArray("content");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject object = array.getJSONObject(i);
                                    GuessHappy guessHappy = new GuessHappy();
                                    guessHappy.setDay(object.getString("day"));
                                    if (!object.isNull("title")) {
                                        guessHappy.setTittle(object.getString("title"));
                                    }
                                    if (!object.isNull("kid")) {
                                        guessHappy.setKid(object.getInt("kid"));
                                    }
                                    if (!object.isNull("type")) {
                                        guessHappy.setType(object.getInt("type"));
                                    }
                                    if (!object.isNull("remark")) {
                                        guessHappy.setRemark(object.getString("remark"));
                                    }
                                    if (!object.isNull("content")) {
                                        if (guessHappy.getType() == GuessHappy.GUESS) {
                                            JSONArray jsonArray = object.getJSONArray("content");
                                            String[] content = new String[jsonArray.length()];
                                            for (int j = 0; j < jsonArray.length(); j++) {
                                                content[j] = jsonArray.getString(j);
                                            }
                                            guessHappy.setContent(content);
                                        } else {
                                            String[] content = new String[1];
                                            content[0] = object.getString("content");
                                            guessHappy.setContent(content);
                                        }
                                    }
                                    if (!object.isNull("correct")) {
                                        guessHappy.setCorrect(object.getString("correct"));
                                    }
                                    if (!object.isNull("error")) {
                                        guessHappy.setError(object.getString("error"));
                                    }
                                    if (!object.isNull("answer")) {
                                        guessHappy.setAnswer(object.getInt("answer"));
                                    }
                                    list.add(guessHappy);
                                }
                            } else {
                                emptyview.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(GuessHappyActivity.this, ResultError.MESSAGE_ERROR,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        loadingLayout.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.removeActivity(this);
    }
}
