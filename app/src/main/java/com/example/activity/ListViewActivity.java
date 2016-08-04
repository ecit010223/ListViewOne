package com.example.activity;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.example.app.MyApp;
import com.example.custom.CustomListView;
import com.example.listviewone.R;

import entity.Item;

public class ListViewActivity extends Activity implements View.OnClickListener {
	private MyApp myApp;

	private LayoutInflater mInflater;
	/** 存放标题文本 ****/
	private ArrayList<String> mData = null;
	/** 文本的条数 ***/
	private int mDataSize = 0;

	/** 自定义的可横向滚动的ListView */
	private CustomListView mListView;
	/** 标题栏左边的编辑按钮 *****/
	private ImageView imgLeftNote;

	private DisplayMetrics mScreenSize;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		myApp = (MyApp) getApplication();
		initView();
		mData = new ArrayList<String>();
		initNotification();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 读取原始.xml配置文件
		// mData = myApp.getCustomItems(this);

		// 读取.自定义配置文件
		ArrayList<Item> mItems_DZBK = myApp.mItems_DZBK;
		if (mItems_DZBK.size() > 0) {
			mData.clear();
			for (int i = 0; i < mItems_DZBK.size(); i++) {
				mData.add(mItems_DZBK.get(i).name);
			}
			mDataSize = mData.size();
			mScreenSize = getScreenSize(this);
			mListView = (CustomListView) findViewById(android.R.id.list);
			LayoutParams lp = mListView.getLayoutParams();
			lp.width = mScreenSize.widthPixels * 3 / 10 * mDataSize;
			if (lp.width < mScreenSize.widthPixels)
				lp.width = mScreenSize.widthPixels;
			mListView.setLayoutParams(lp);

			LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.head);
			// 删除子控件
			mLinearLayout.removeAllViews();
			lp = mLinearLayout.getLayoutParams();
			lp.width = mScreenSize.widthPixels * 3 / 10 * mDataSize;
			if (lp.width < mScreenSize.widthPixels)
				lp.width = mScreenSize.widthPixels;
			mLinearLayout.setLayoutParams(lp);
			/** 向列头部分添加新的控件 ***/
			TextView tv = null;
			for (int i = 0; i < mDataSize; i++) {
				tv = new TextView(this);
				tv.setLayoutParams(new LayoutParams(
						mScreenSize.widthPixels * 3 / 10,
						LayoutParams.WRAP_CONTENT));
				tv.setGravity(Gravity.CENTER);
				tv.setText(mData.get(i));
				tv.setMinLines(2);
				tv.setTextColor(getResources()
						.getColor(R.color.gray_demand_one));
				tv.setTextSize(14.5f);
				mLinearLayout.addView(tv);
			}

			mListView.mListHead = mLinearLayout;
			// 设置数据
			mListView.setAdapter(new DataAdapter());

			mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		}
	}

	/** 初始化控件 ****/
	private void initView() {
		imgLeftNote = (ImageView) findViewById(R.id.img_public_black_head_title_left_note);
		imgLeftNote.setVisibility(View.VISIBLE);
		imgLeftNote.setOnClickListener(this);
	}

	/** 初始化Notificatin **/
	@SuppressLint("NewApi")
	private void initNotification() {
		RemoteViews rv = new RemoteViews(myApp.getPackageName(),
				R.layout.notify);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this);
		Notification mNotification = mBuilder
				.setContentTitle("title")
				.setContentText("text")
				.setSmallIcon(R.drawable.ic_launcher)
				.setLargeIcon(
						BitmapFactory.decodeResource(myApp.getResources(),
								R.drawable.ic_launcher)).build();
		mNotification.bigContentView = rv;
		/*
		 * .setStyle( new NotificationCompat.BigPictureStyle()
		 * .bigPicture(BitmapFactory.decodeResource(
		 * ListViewActivity.this.getResources(), R.drawable.ic_launcher)))
		 */
		Intent toDZBK = new Intent(this, DZBKEditActivity.class);
		toDZBK.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);

		Intent toListView = new Intent(this, ListViewActivity.class);
		toListView.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);

		// TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// stackBuilder.addParentStack(DZBKEditActivity.class);
		// stackBuilder.addNextIntent(toDZBK);

		PendingIntent pToDZBK = PendingIntent.getActivity(this, 0, toDZBK,
				PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pToListView = PendingIntent.getActivity(this, 0,
				toListView, PendingIntent.FLAG_UPDATE_CURRENT);
		// PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
		// PendingIntent.FLAG_UPDATE_CURRENT);
		// mBuilder.setContentIntent(notifyIntent);

		rv.setOnClickPendingIntent(R.id.lv_notify1, pToDZBK);
		rv.setOnClickPendingIntent(R.id.lv_notify2, pToListView);
		rv.setOnClickPendingIntent(R.id.lv_notify3, pToDZBK);
		rv.setOnClickPendingIntent(R.id.lv_notify4, pToListView);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(1, mNotification);
	}

	/** 自定义Adapter **/
	private class DataAdapter extends BaseAdapter {
		/** 保存getView方法中创建的新控件 **/
		private ViewHolder mViewHolder;
		/** 暂存mViewHolder对象内的mTextViews成员变量的值 **/
		private ArrayList<TextView> mTextViews = null;
		/** 暂存每个新建的TextView对象 **/
		private TextView mTextView;

		@Override
		public int getCount() {
			return 50;// 固定显示50行数据
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item, null);
				LinearLayout mLinearLayout = (LinearLayout) convertView
						.findViewById(R.id.head);
				mViewHolder = new ViewHolder();
				mViewHolder.mTextViews = new ArrayList<TextView>();
				/*********** 向数据展示部分添加新的控件 ********************/
				for (int i = 0; i < mDataSize; i++) {
					mTextView = new TextView(ListViewActivity.this);
					mTextView.setLayoutParams(new LayoutParams(
							mScreenSize.widthPixels * 3 / 10,
							LayoutParams.WRAP_CONTENT));
					mTextView.setGravity(Gravity.CENTER);
					mTextView.setMinLines(2);
					mTextView.setTextColor(getResources().getColor(
							R.color.gray_demand_one));
					mTextView.setTextSize(14.5f);
					mLinearLayout.addView(mTextView);
					mViewHolder.mTextViews.add(mTextView);
				}
				convertView.setTag(mViewHolder);
			} else {
				mViewHolder = (ViewHolder) convertView.getTag();
			}
			mTextViews = mViewHolder.mTextViews;
			((TextView) convertView.findViewById(R.id.item1)).setText("不动列行"
					+ position);
			for (int i = 0; i < mDataSize; i++) {
				mTextViews.get(i).setText(mData.get(i) + position);
			}
			// 校正（处理同时上下和左右滚动出现错位情况）
			/*
			 * View child = ((ViewGroup) convertView).getChildAt(1); int head =
			 * mListView.getHeadScrollX(); if (child.getScrollX() != head) {
			 * child.scrollTo(mListView.getHeadScrollX(), 0); }
			 */
			return convertView;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		class ViewHolder {
			ArrayList<TextView> mTextViews;
		}
	}

	/** 获取屏幕分辨率 ***/
	public static DisplayMetrics getScreenSize(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getApplicationContext().getResources().getDisplayMetrics();
		return dm;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_public_black_head_title_left_note:
			Intent intent = new Intent(this, DZBKEditActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}
}
