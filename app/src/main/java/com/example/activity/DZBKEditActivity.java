package com.example.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.adapter.DZBKEditFixedListAdapter;
import com.example.app.MyApp;
import com.example.custom.DragSortListView;
import com.example.custom.DragSortListView.RemoveListener;
import com.example.listviewone.R;

import entity.Item;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DZBKEditActivity extends Activity implements View.OnClickListener {
	private MyApp myApp;
	private Context mContext;
	/** 标题栏左边的返回按钮 *****/
	private ImageView imgLeftNote;

	/** 首页显示列表 **/
	private ArrayList<Item> mDBZQMarkets;
	/** 固定列表 **/
	private ArrayList<Item> mDBZQMarketsFixed;
	/** 可编辑列表 **/
	private ArrayList<Item> mDBZQMarketsEdit;
	public List<String> mNames;
	private ListView mListViewFixed;
	private DZBKEditFixedListAdapter mFixedListAdapter;

	private DragSortListView mDragListView;
	private DragAdapter mDragAdapter;
	/** 显示的list data，包含可编辑、自定义title和可添加的列表 **/
	private ArrayList<Item> mDBZQMarketList;

	/** 监听器在手机拖动停下的时候触发 **/
	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {// from to 分别表示 被拖动控件原位置 和目标位置
			if (from != to) {
				Item item = (Item) mDragAdapter.getItem(from);// 得到listview的适配器
				if (to >= mDragAdapter.mSepIndex && item.isChecked) {
					item.isChecked = false;
					mDragAdapter.mSepIndex--;
				} else if (to <= mDragAdapter.mSepIndex && !item.isChecked) {
					item.isChecked = true;
					mDragAdapter.mSepIndex++;
				} else if (from == mDragAdapter.mSepIndex) {
					return;
				}
				mDragAdapter.remove(from);// 在适配器中”原位置“的数据。
				mDragAdapter.insert(item, to);// 在目标位置中插入被拖动的控件。
			}
		}
	};
	/** 删除监听器，点击左边差号就触发。删除item操作。 **/
	private RemoveListener onRemove = new DragSortListView.RemoveListener() {
		@Override
		public void remove(int which) {
			mDragAdapter.remove(which);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dzbk_edit_activity);
		myApp = (MyApp) getApplication();
		mContext = this;
		mDBZQMarkets = myApp.mItems_DZBK;
		//Log.d("msg", String.valueOf(mDBZQMarkets.size()));
		mDBZQMarketsEdit = new ArrayList<Item>();
		mDBZQMarketList = new ArrayList<Item>();
		mNames = new ArrayList<String>();
		for (int i = 0; i < mDBZQMarkets.size(); i++) {
			Item info = mDBZQMarkets.get(i);
			if (!info.isFixed) {
				mDBZQMarketsEdit.add(info);
				mDBZQMarketList.add(info);
			}
		}
		/** 添加自定义title到显示list **/
		Item data = new Item();
		data.name = "可添加";
		mDBZQMarketList.add(data);

		/** 添加剩余的市场是可自定义添加列表显示 ***/
		for (int i = 0; i < myApp.mItems_DZBK_ALL.size(); i++) {
			boolean bNeedAdd = true;
			Item mItem = myApp.mItems_DZBK_ALL.get(i);
			for (int j = 0; j < mDBZQMarkets.size(); j++) {
				if (mDBZQMarkets.get(j).id.equals(mItem.id)) {
					bNeedAdd = false;
					break;
				}
			}
			if (bNeedAdd) {
				mItem.isChecked = false;
				mDBZQMarketList.add(mItem);
			}
		}
		initDatas();
		/** 初始化 插件 **/
		initView();
	}

	private void initDatas() {
		mDragListView = (DragSortListView) findViewById(R.id.dslvList);

		mDragListView.setDropListener(onDrop);
		mDragListView.setRemoveListener(onRemove);

		mDragAdapter = new DragAdapter(DZBKEditActivity.this, mDBZQMarketList,
				mDBZQMarketsEdit.size());
		mDragListView.setAdapter(mDragAdapter);
		mDragListView.setDragEnabled(true); // 设置是否可拖动。
		mDragListView.setClickable(false);
	}

	/**
	 * 初始化 标头的插件
	 */
	private void initView() {

		imgLeftNote = (ImageView) findViewById(R.id.img_public_black_head_title_left_white_back);
		imgLeftNote.setVisibility(View.VISIBLE);
		imgLeftNote.setOnClickListener(this);

		this.mListViewFixed = (ListView) this.findViewById(R.id.lv_home_fixed);
		mDBZQMarketsFixed = new ArrayList<Item>();
		for (int i = 0; i < mDBZQMarkets.size(); i++) {
			Item market = mDBZQMarkets.get(i);
			if (market.isChecked) {
				mDBZQMarketsFixed.add(market);
			}
		}
		this.mFixedListAdapter = new DZBKEditFixedListAdapter(this,
				mDBZQMarketsFixed);
		mListViewFixed.setAdapter(mFixedListAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_public_black_head_title_left_white_back:
			saveEdit();
			DZBKEditActivity.this.finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void finish() {
		saveEdit();
		super.finish();
	}

	@SuppressLint("NewApi")
	private void saveEdit() {
		ArrayList<Item> newMarketList = new ArrayList<Item>();
		/*
		for (int i = 0; i < mDBZQMarketsFixed.size(); i++) {
			Item market = mDBZQMarketsFixed.get(i);
			newMarketList.add(market);
			Log.d("msg", "mDBZQMarketsFixed");
		}
		*/
		for (int i = 0; i < mDragAdapter.getCount(); i++) {
			Item market = (Item) mDragAdapter.getItem(i);
			if (market.isChecked && !market.id.isEmpty()) {
				newMarketList.add(market);
				//Log.d("msg", "mDragAdapter");
			}
		}
		myApp.UpdateAllDZBKMarketList(newMarketList);
	}

	public class DragAdapter extends BaseAdapter {

		private Context context;
		/** 适配器的数据源 **/
		ArrayList<Item> items;
		/** 自定义添加title分割所在index **/
		public int mSepIndex;

		public DragAdapter(Context context, ArrayList<Item> list,
				int separateIndex) {
			this.context = context;
			this.items = list;
			mSepIndex = separateIndex;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int arg0) {
			return items.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		public void remove(int arg0) {// 删除指定位置的item
			items.remove(arg0);
			this.notifyDataSetChanged();// 不要忘记更改适配器对象的数据源
		}

		public void insert(Item item, int arg0) {
			items.add(arg0, item);
			this.notifyDataSetChanged();
		}

		@SuppressLint("NewApi")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Item item = (Item) getItem(position);
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.zq_dzbk_list_item_home_drag, null);
				viewHolder.tvName = (TextView) convertView
						.findViewById(R.id.headEditActivity_text);
				viewHolder.tvName1 = (TextView) convertView
						.findViewById(R.id.headEditActivity_text_tuodong);
				viewHolder.ivDelete = (ImageView) convertView
						.findViewById(R.id.click_delete);
				viewHolder.ivDragHandle = (ImageView) convertView
						.findViewById(R.id.drag_handle);
				// viewHolder.img_icon = (ImageView)
				// convertView.findViewById(R.id.img_market_icon);
				viewHolder.img_add = (ImageView) convertView
						.findViewById(R.id.click_add);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.tvName.setText(item.name);
			if (item.id.isEmpty()) {
				viewHolder.ivDelete.setVisibility(View.GONE);
				viewHolder.ivDragHandle.setVisibility(View.GONE);
				// viewHolder.img_icon.setVisibility(View.GONE);
				viewHolder.img_add.setVisibility(View.GONE);
				viewHolder.tvName1.setVisibility(View.VISIBLE);
				convertView.setBackgroundResource(R.color.zq_515151);
				viewHolder.tvName.setTextColor(mContext.getResources()
						.getColor(R.color.zq_ffffff));
			} else {
				convertView.setBackgroundResource(R.color.white);
				viewHolder.tvName1.setVisibility(View.GONE);
				viewHolder.tvName.setTextColor(mContext.getResources()
						.getColor(R.color.zq_515151));
				if (item.isChecked) {
					viewHolder.ivDelete.setVisibility(View.VISIBLE);
					viewHolder.ivDragHandle.setVisibility(View.VISIBLE);
					// viewHolder.img_icon.setVisibility(View.VISIBLE);
					viewHolder.img_add.setVisibility(View.GONE);
				} else {
					viewHolder.ivDelete.setVisibility(View.GONE);
					viewHolder.ivDragHandle.setVisibility(View.VISIBLE);
					// viewHolder.img_icon.setVisibility(View.VISIBLE);
					viewHolder.img_add.setVisibility(View.VISIBLE);

				}

			}
			viewHolder.img_add.setOnClickListener(new AddClickListener(
					position, viewHolder));
			viewHolder.ivDelete.setOnClickListener(new AddClickListener(
					position, viewHolder));
			// int resId = context.getResources().getIdentifier(item.NormalIcon,
			// "drawable", context.getPackageName());
			// viewHolder.img_icon.setImageResource(resId);

			return convertView;
		}

		class AddClickListener implements OnClickListener {
			private int mPosition;
			private ViewHolder mViewHolder;

			public AddClickListener(int position, ViewHolder viewHolder) {
				this.mPosition = position;
				this.mViewHolder = viewHolder;
			}

			@Override
			public void onClick(View v) {
				if (v == mViewHolder.img_add) {
					if (items != null) {
						int num = getCount();
						if (this.mPosition < num) {
							Item item = (Item) getItem(mPosition);
							item.isChecked = true;
							mDragAdapter.remove(mPosition);// 在适配器中”原位置“的数据。
							mDragAdapter.insert(item, mSepIndex);// 在目标位置中插入被拖动的控件。
							mSepIndex++;
						}
					}
				} else if (v == mViewHolder.ivDelete) {
					if (items != null) {
						int num = getCount();
						if (this.mPosition < num) {
							Item item = (Item) getItem(mPosition);
							item.isChecked = false;
							mDragAdapter.remove(mPosition);// 在适配器中”原位置“的数据。
							mSepIndex--;
							mDragAdapter.insert(item, mSepIndex + 1);// 在目标位置中插入被拖动的控件。
						}
					}

				}
			}
		}

		class ViewHolder {
			TextView tvName;
			TextView tvName1;
			ImageView ivDelete;
			ImageView ivDragHandle;
			ImageView img_add;
		}
	}
}
