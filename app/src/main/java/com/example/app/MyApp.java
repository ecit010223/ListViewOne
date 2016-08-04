package com.example.app;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.util.EncodingUtils;
import org.xmlpull.v1.XmlPullParser;

import com.example.net.MyByteBuffer;
import com.example.util.FileService;
import com.example.util.STD;

import entity.Item;

import android.app.Application;
import android.content.Context;
import android.util.Xml;

public class MyApp extends Application {
	/** custom_items.xml 文件名 **/
	private static final String CUSTOMITEM_FILE_NAME = "custom_items.xml";
	public static final String DZBKMARKET_PATH = "custom_items.dat";
	/** 配置文件custom_item.xml文件的版本号 */
	public String mPBMarketDZBKVersion = "1.0";
	/** 存放配置文件custom_items.xml中的所有的内容 **/
	public ArrayList<Item> mItems;
	/** 定制模块中的所有选项 ******/
	public ArrayList<Item> mItems_DZBK_ALL;
	/** 定制模块中的自定义列表 ******/
	public ArrayList<Item> mItems_DZBK;

	private static MyApp instance;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		mItems_DZBK = new ArrayList<Item>();
		anlayseCustomItemsXml();
		// 定制板块配置文件不存在，使用市场配置文件里default=1
		if (ReadDZBKMarketData_Ex() < 0) {
			for (int itemp = 0; itemp < mItems_DZBK_ALL.size(); itemp++) {
				if (mItems_DZBK_ALL.get(itemp).isChecked) {
					mItems_DZBK.add(mItems_DZBK_ALL.get(itemp));
				}
			}
			startSaveDZBKMarketImmediately();
		}
	}

	/**
	 * 没有新自选股时读取老的，读取dzbkmarket.dat
	 * 
	 * @return
	 */
	public synchronized int ReadDZBKMarketData_Ex() {
		FileService file = new FileService(this.getApplicationContext());
		int size = file.getFileSize(DZBKMARKET_PATH);
		byte[] data = new byte[size + 1];
		int ret = file.readFile(DZBKMARKET_PATH, data);
		if (ret == -1) {
			// 如果读到的为 -1 说明文件不存在
			return -1;
		}

		// 解析添加数据
		int offset = 0;
		// 版本号
		int version_len = STD.getByteStringLen(data, offset, 30);
		String version = new String(data, offset, version_len);
		offset += 30;
		if (!version.equalsIgnoreCase(this.mPBMarketDZBKVersion)) {
			return -1;// 市场配置文件版本号不相同，说明配置文件有更改，需要重置home items
		}
		// 保存时的服务器时间
		MyByteBuffer.getLong(data, offset);
		offset += 8;

		// 个数
		int num = MyByteBuffer.getShort(data, offset);
		offset += 2;
		for (int i = 0; i < num; i++) {
			int len = MyByteBuffer.getShort(data, offset);
			offset += 2;

			byte[] bytename = new byte[len];
			MyByteBuffer.getBytes(data, offset, bytename, 0, len);
			String Name = EncodingUtils.getString(bytename, "UTF-8");
			offset += len;

			len = MyByteBuffer.getShort(data, offset);
			offset += 2;

			byte[] byteid = new byte[len];
			MyByteBuffer.getBytes(data, offset, byteid, 0, len);
			String id = EncodingUtils.getString(byteid, "UTF-8");
			offset += len;

			byte isFixed = MyByteBuffer.getByte(data, offset);
			offset += 1;
			boolean bFixed = (isFixed == 1) ? true : false;

			byte isChecked = MyByteBuffer.getByte(data, offset);
			offset += 1;
			boolean bDefault = (isChecked == 1) ? true : false;

			Item pbmarket = new Item();
			pbmarket.name = Name;
			pbmarket.id = id;
			pbmarket.isFixed = bFixed;
			pbmarket.isChecked = bDefault;
			mItems_DZBK.add(pbmarket);
		}
		return 0;
	}

	public static MyApp getInstance() {
		return instance;
	}

	private void anlayseCustomItemsXml() {
		InputStream inStream = null;
		try {
			inStream = getApplicationContext().getResources().getAssets()
					.open(CUSTOMITEM_FILE_NAME);

		} catch (Exception e) {
			e.printStackTrace();
		}

		XmlPullParser parser = Xml.newPullParser();
		Item mItem = null;

		try {
			parser.setInput(inStream, "UTF-8");
			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {

				switch (eventType) {
				// 文档开始事件,可以进行数据初始化处理
				case XmlPullParser.START_DOCUMENT:
					if (mItems == null) {
						mItems = new ArrayList<Item>();
					}
					mItems.clear();
					if (mItems_DZBK_ALL == null) {
						mItems_DZBK_ALL = new ArrayList<Item>();
					}
					mItems_DZBK_ALL.clear();
					break;
				case XmlPullParser.START_TAG:// 开始元素事件
					String name = parser.getName();
					if (name.equalsIgnoreCase("items")) {
						this.mPBMarketDZBKVersion = parser.getAttributeValue(
								null, "version");
					} else if (name.equalsIgnoreCase("item")) {
						mItem = new Item();
						mItem.name = parser.getAttributeValue(null, "name");
						mItem.id = parser.getAttributeValue(null, "id");
						int isFixed = STD.StringToInt(parser.getAttributeValue(
								null, "fixed"));
						if (isFixed == 0) {
							mItem.isFixed = false;
						} else {
							mItem.isFixed = true;
						}

						int isChecked = STD.StringToInt(parser
								.getAttributeValue(null, "checked"));
						if (isChecked == 0) {
							mItem.isChecked = false;
						} else {
							mItem.isChecked = true;
						}
					}
					break;

				case XmlPullParser.END_TAG:// 结束元素事件
					if (parser.getName().equalsIgnoreCase("item")
							&& mItem != null) {
						if (mItems_DZBK_ALL == null) {
							mItems_DZBK_ALL = new ArrayList<Item>();
						}
						mItems_DZBK_ALL.add(mItem);
						if (mItems == null) {
							mItems = new ArrayList<Item>();
						}
						mItems.add(mItem);
						mItem = null;
					}
					break;
				}
				eventType = parser.next();
			}
			inStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 清空 */
	public synchronized void clearDZBKMarket() {
		mItems_DZBK.clear();
	}

	public synchronized int UpdateAllDZBKMarketList(ArrayList<Item> marketList) {
		clearDZBKMarket();
		for (int i = 0; i < marketList.size(); i++) {
			Item pbmarket = new Item();
			pbmarket.name = marketList.get(i).name;
			pbmarket.id = marketList.get(i).id;
			pbmarket.isFixed = marketList.get(i).isFixed;
			pbmarket.isChecked = marketList.get(i).isChecked;
			mItems_DZBK.add(pbmarket);
		}

		startSaveDZBKMarketImmediately();
		return 0;
	}

	/**
	 * 立即保存到本地文件
	 * 
	 * @return
	 */
	public int startSaveDZBKMarketImmediately() {
		long time = getServerTime();
		// 保存本地
		SaveDZBKMarketData_Ex(time);

		// 延迟10毫秒
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 定制板块市场列表 -- 保存、读取 -- 文件名dzbkmarket.dat
	 * 
	 * @param time
	 */
	public synchronized void SaveDZBKMarketData_Ex(long time) {
		int num = mItems_DZBK.size();
		byte[] data = new byte[40 + num * 200];
		int offset = 0;
		// 版本号
		MyByteBuffer.putBytes(data, offset, mPBMarketDZBKVersion.getBytes(), 0,
				mPBMarketDZBKVersion.getBytes().length);
		offset += 30;

		// 保存时的服务器时间
		MyByteBuffer.putLong(data, offset, time);
		offset += 8;

		// 个数
		MyByteBuffer.putShort(data, offset, (short) num);
		offset += 2;

		for (int i = 0; i < num; i++) {
			byte[] temp = mItems_DZBK.get(i).name.getBytes();
			int len = temp.length;
			MyByteBuffer.putShort(data, offset, (short) len);
			offset += 2;
			MyByteBuffer.putBytes(data, offset, temp, 0, len);
			offset += len;

			temp = mItems_DZBK.get(i).id.getBytes();
			int idlen = temp.length;
			MyByteBuffer.putShort(data, offset, (short) idlen);
			offset += 2;
			MyByteBuffer.putBytes(data, offset, temp, 0, idlen);
			offset += idlen;
			boolean btemp = mItems_DZBK.get(i).isFixed;
			byte value = (byte) (btemp ? 1 : 0);
			MyByteBuffer.putByte(data, offset, value);
			offset += 1;

			btemp = mItems_DZBK.get(i).isChecked;
			value = (byte) (btemp ? 1 : 0);
			MyByteBuffer.putByte(data, offset, value);
			offset += 1;
		}
		//
		FileService file = new FileService(this.getApplicationContext());
		try {
			file.saveToFile(DZBKMARKET_PATH, data, offset);
			// 保存成功
			data = null;
		} catch (Exception e) {
			// 保存失败
		}
	}

	/**
	 * 获取服务器时间,目前暂时获取当前时间
	 * 
	 * @return
	 */
	public long getServerTime() {
		return STD.getCurDataTime();
	}

	/**
	 * 读取custom_items.xml文件的内容
	 * 
	 * @param context
	 * @return custom_items.xml文件内所有checked=true的item结点
	 */
	public static ArrayList<String> getCustomItems(final Context context) {
		ArrayList<String> items = null;
		InputStream inStream = null;
		try {
			inStream = context.getResources().getAssets()
					.open(CUSTOMITEM_FILE_NAME);

		} catch (Exception e) {
			e.printStackTrace();
		}

		XmlPullParser parser = Xml.newPullParser();

		try {
			parser.setInput(inStream, "UTF-8");
			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {

				switch (eventType) {
				// 文档开始事件,可以进行数据初始化处理
				case XmlPullParser.START_DOCUMENT:
					if (items == null) {
						items = new ArrayList<String>();
					}
					items.clear();
					break;
				case XmlPullParser.START_TAG:// 开始元素事件
					String name = parser.getName();
					if (name.equalsIgnoreCase("item")) {
						// 如果custom_item.xml文件中设置该item的checked属性是“1”，则添加
						if (parser.getAttributeValue(null, "checked").equals(
								"1")) {
							items.add(parser.getAttributeValue(null, "name"));
						}
					}
					break;
				}
				eventType = parser.next();
				inStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return items;
	}
}
