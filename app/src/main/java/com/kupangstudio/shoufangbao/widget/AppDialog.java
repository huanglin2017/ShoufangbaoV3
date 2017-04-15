package com.kupangstudio.shoufangbao.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.model.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义dialog类
 * Created by long on 15/10/29.
 * Copyright 2015 android_xiaobai.
 */
public class AppDialog extends Dialog {

    private static TextView titleTv1;

    public AppDialog(Context context) {
        super(context);
    }

    public AppDialog(Context context, int theme) {
        super(context, theme);
    }

    public AppDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    public static class Builder {
        public final static int SINGLECHOICE = 1;
        public final static int COMMONDIALOG = 2;
        public final static int SINGLE_OK = 3;
        public final static int COMMUNITYSHARE = 4;
        public final static int COMMON_OK = 5;
        private int type;
        private Context context;
        private String title;
        private String message;
        private String positiveText;
        private String negativeText;
        private String[] items = new String[]{};
        private View contentView;
        private View.OnClickListener posButtonListener;
        private View.OnClickListener negButtonListener;
        private View.OnClickListener shareButtonListener;
        private AdapterView.OnItemClickListener itemListener;
        private TextView titleTv;
        private TextView messageTv;
        private Button positiveBtn;
        private Button negativeBtn;
        private AppDialog dialog;
        //批量添加
        private List<HashMap<String, String>> repeatMap;
        private List<HashMap<String, String>> failMap;
        private TextView tvSuccessAdd;
        private LinearLayout shareWX;
        private LinearLayout shareFriend;
        private LinearLayout shareQQ;
        private LinearLayout shareMessage;

        public Builder(Context context, int type) {
            this.context = context;
            this.type = type;
        }

        public Builder(Context context, int type, List<HashMap<String, String>> repeatMap, List<HashMap<String, String>> failMap) {
            this.context = context;
            this.type = type;
            this.repeatMap = repeatMap;
            this.failMap = failMap;
        }

        public Builder(Context context, int type, String[] items) {
            this.context = context;
            this.type = type;
            this.items = items;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContentView(View contentView) {
            this.contentView = contentView;
            return this;
        }

        public Builder setPositiveButton(String positiveText, View.OnClickListener listener) {
            this.positiveText = positiveText;
            this.posButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeText, View.OnClickListener listener) {
            this.negativeText = negativeText;
            this.negButtonListener = listener;
            return this;
        }

        //分享事件
        public Builder setShareButton(View.OnClickListener listener) {
            this.shareButtonListener = listener;
            return this;
        }

        public Builder setItemClick(AdapterView.OnItemClickListener listener) {
            this.itemListener = listener;
            return this;
        }

        public AppDialog create() {
            dialog = new AppDialog(context, R.style.DialogNoTitle);
            dialog.setContentView(contentView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            switch (type) {
                case SINGLECHOICE://条目单选
                    ListView lv = (ListView) contentView.findViewById(R.id.common_dialog_single);
                    positiveBtn = (Button) contentView.findViewById(R.id.common_positive_btn);
                    SimpleAdapter adapter = new SimpleAdapter(context, getData(), R.layout.item_common_dialog,
                            new String[]{"content"}, new int[]{R.id.item_common_dialog_tv});
                    lv.setAdapter(adapter);
                    lv.setOnItemClickListener(itemListener);
                    positiveBtn.setOnClickListener(posButtonListener);
                    break;
                case COMMONDIALOG://通用布局
                    titleTv = (TextView) contentView.findViewById(R.id.common_dialog_title);
                    messageTv = (TextView) contentView.findViewById(R.id.common_dialog_message);
                    positiveBtn = (Button) contentView.findViewById(R.id.common_positive_btn);
                    negativeBtn = (Button) contentView.findViewById(R.id.common_negative_btn);
                    titleTv.setText(title);
                    messageTv.setText(message);
                    positiveBtn.setOnClickListener(posButtonListener);
                    negativeBtn.setOnClickListener(negButtonListener);
                    break;
                case SINGLE_OK://单选展示下面有OK
                    positiveBtn = (Button) contentView.findViewById(R.id.dialog_multiadd_btn);
                    TextView tvRepeatAdd = (TextView) contentView.findViewById(R.id.dialog_multiadd_repeatnum);
                    TextView tvFailAdd = (TextView) contentView.findViewById(R.id.dialog_multiadd_failnum);
                    ListView lvFail = (ListView) contentView.findViewById(R.id.dialog_multiadd_lvfail);
                    ListView lvRepeat = (ListView) contentView.findViewById(R.id.dialog_multiadd_lvrepeat);
                    if (repeatMap != null && repeatMap.size() > 0) {
                        tvRepeatAdd.setText(repeatMap.size() + "");
                        SimpleAdapter repeatAdapter = new SimpleAdapter(context, repeatMap, R.layout.item_dialog_multiadd, new String[]{"content"},
                                new int[]{R.id.item_dialog_multiadd_tv});
                        lvRepeat.setAdapter(repeatAdapter);
                    } else {
                        contentView.findViewById(R.id.dialog_multiadd_repeatlayout).setVisibility(View.GONE);
                        contentView.findViewById(R.id.dialog_multiadd_repeatline).setVisibility(View.GONE);
                        lvRepeat.setVisibility(View.GONE);
                    }
                    if (failMap != null && failMap.size() > 0) {
                        tvFailAdd.setText(failMap.size() + "");
                        SimpleAdapter failAdapter = new SimpleAdapter(context, failMap, R.layout.item_dialog_multiadd, new String[]{"content"},
                                new int[]{R.id.item_dialog_multiadd_tv});
                        lvFail.setAdapter(failAdapter);
                    } else {
                        contentView.findViewById(R.id.dialog_multiadd_faillayout).setVisibility(View.GONE);
                        contentView.findViewById(R.id.dialog_multiadd_failline).setVisibility(View.GONE);
                        lvFail.setVisibility(View.GONE);
                    }
                    if ((repeatMap == null || repeatMap.size() == 0) && (failMap == null || failMap.size() == 0)) {
                        contentView.findViewById(R.id.dialog_multiadd_successline).setVisibility(View.GONE);
                    }
                    positiveBtn.setOnClickListener(posButtonListener);
                    break;
                case COMMUNITYSHARE:
                    shareWX = (LinearLayout) contentView.findViewById(R.id.share_wx);
                    shareFriend = (LinearLayout) contentView.findViewById(R.id.share_friend);
                    shareQQ = (LinearLayout) contentView.findViewById(R.id.share_qq);
                    shareMessage = (LinearLayout) contentView.findViewById(R.id.share_message);
                    shareWX.setOnClickListener(shareButtonListener);
                    shareFriend.setOnClickListener(shareButtonListener);
                    shareQQ.setOnClickListener(shareButtonListener);
                    shareMessage.setOnClickListener(shareButtonListener);
                    break;
                case COMMON_OK:
                    messageTv = (TextView) contentView.findViewById(R.id.commonok_dailog_content);
                    positiveBtn = (Button) contentView.findViewById(R.id.commomok_dialog_btn);
                    titleTv = (TextView) contentView.findViewById(R.id.commonok_dialog_title);
                    if(title == null || title.equals(" ")) {
                        titleTv.setVisibility(View.GONE);
                    } else {
                        titleTv.setText(title);
                    }
                    messageTv.setText(message);
                    positiveBtn.setOnClickListener(posButtonListener);
                    break;
                default:
                    titleTv = (TextView) contentView.findViewById(R.id.common_dialog_title);
                    messageTv = (TextView) contentView.findViewById(R.id.common_dialog_message);
                    positiveBtn = (Button) contentView.findViewById(R.id.common_positive_btn);
                    negativeBtn = (Button) contentView.findViewById(R.id.common_negative_btn);
                    titleTv.setText(title);
                    messageTv.setText(message);
                    positiveBtn.setOnClickListener(posButtonListener);
                    negativeBtn.setOnClickListener(negButtonListener);
                    break;
            }
            if (!dialog.isShowing()) {
                dialog.show();
            }
            return dialog;
        }

        public void dismiss() {
            if(dialog.isShowing()){
                dialog.dismiss();
            }
        }

        //批量添加
        public Builder setSuccessNum(int num) {
            tvSuccessAdd = (TextView) contentView.findViewById(R.id.dialog_multiadd_successnum);
            tvSuccessAdd.setText(String.valueOf(num));
            return this;
        }

        public Builder setSuccessGone() {
            contentView.findViewById(R.id.dialog_multiadd_successlayout).setVisibility(View.GONE);
            contentView.findViewById(R.id.dialog_multiadd_successline).setVisibility(View.GONE);
            return this;
        }

        private List<Map<String, Object>> getData() {
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            Map<String, Object> map;
            for (int i = 0; i < items.length; i++) {
                map = new HashMap<String, Object>();
                map.put("content", items[i]);
                list.add(map);
            }
            return list;
        }
    }
}
