package com.kupangstudio.shoufangbao;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.base.BaseActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.model.Custom;
import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageShareActivity extends BaseActivity {

	private ListView lv_message;
	private MessageAdapter adapter;
	User u;
	private ArrayList<Custom> listdatas;
	private static HashMap<Integer, Boolean> isSelected;
	private HashMap<Integer, String> map;
	private ArrayList<String> content;
	private ImageView leftbutton, rightbutton;
	private TextView title;
	private String msg_content;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_share);
		CommonUtils.addActivity(this);
		leftbutton = (ImageView) findViewById(R.id.navbar_image_left);
		rightbutton = (ImageView) findViewById(R.id.navbar_image_right);
		title = (TextView) findViewById(R.id.navbar_title);
		rightbutton.setImageResource(R.drawable.icon_done);
		msg_content = getIntent().getStringExtra("content");
		title.setText("短信分享");
		leftbutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		rightbutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (content.size() > 0) {
					content.clear();
				}
				for (Map.Entry<Integer, String> entry : map.entrySet()) {
					content.add(entry.getValue().toString());
				}
				Intent it = new Intent(Intent.ACTION_VIEW);
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < content.size(); i++) {
					sb.append(content.get(i));
					sb.append(";");
				}
				it.putExtra("address", sb.toString());
				it.putExtra("sms_body", msg_content);
				it.setType("vnd.android-dir/mms-sms");
				startActivity(it);
			}
		});
		lv_message = (ListView) findViewById(R.id.list_message_share);
		lv_message.setItemsCanFocus(false);
		lv_message.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		isSelected = new HashMap<Integer, Boolean>();
		map = new HashMap<Integer, String>();
		content = new ArrayList<String>();
		u = User.getInstance();
		lv_message.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Custom c = listdatas.get(position);
				ViewHolder holder = (ViewHolder) view.getTag();
				holder.cb.toggle();
				getIsSelected().put(position, holder.cb.isChecked());
				if (holder.cb.isChecked() == true) {
					map.put(position, c.getTel());
				} else {
					map.remove(position);
				}
			}
		});
		new Handler().post(new Runnable() {

			@Override
			public void run() {
				listdatas = (ArrayList<Custom>) DataSupport.where("uid = ?", String.valueOf(u.uid)).find(Custom.class);
				adapter = new MessageAdapter(MessageShareActivity.this,
						listdatas);
				lv_message.setAdapter(adapter);
			}
		});

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
	class ViewHolder {
		CheckBox cb;
		ImageView identity;
		TextView title;
		TextView effect;
		TextView state;
		TextView times;
	}

	class MessageAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private ArrayList<Custom> list;

		public MessageAdapter(Context ctx, ArrayList<Custom> list) {
			inflater = LayoutInflater.from(ctx);
			this.list = list;
			initData();
		}

		private void initData() {
			for (int i = 0; i < list.size(); i++) {
				getIsSelected().put(i, false);
			}

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
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.item_list_message_custom,
						parent, false);
				holder.identity = (ImageView) convertView
						.findViewById(R.id.item_message_identity);
				holder.title = (TextView) convertView
						.findViewById(R.id.item_message_name);
				holder.effect = (TextView) convertView
						.findViewById(R.id.item_message_effect);
				holder.state = (TextView) convertView
						.findViewById(R.id.item_message_state);
				holder.times = (TextView) convertView
						.findViewById(R.id.item_message_times);
				holder.cb = (CheckBox) convertView
						.findViewById(R.id.item_message_check);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final Custom c = list.get(position);
			holder.cb.setChecked(getIsSelected().get(position));
			holder.title.setText(c.getName());
			holder.effect.setText("意向" + Constants.WILLITEMS[c.getWill()]);
			return convertView;
		}

	}

	private HashMap<Integer, Boolean> getIsSelected() {
		return isSelected;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		CommonUtils.removeActivity(this);
	}
}
