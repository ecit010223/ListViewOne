package com.example.util;

import android.text.format.Time;

public class STD {
	public static final int StringToInt(String text) {
		if (text == null || text.length() <= 0)
			text = "0";
		try {
			return Integer.parseInt(text);
		} catch (Exception e) {
			return 0;
		}
	}

	// 获取当前数据时间， 格式：低四字节为时间(hhmmss)，高四字节为日期(yyyymmdd)
	public static final long getCurDataTime() {

		Time t = new Time("GMT+8");
		t.setToNow(); // 取得系统时间

		int date = t.year * 10000 + t.month * 100 + t.monthDay;
		int time = t.hour * 10000 + t.minute * 100 + t.second;

		return (((long) date << 32) & 0xffffffff00000000L) + time;
	}

	public static void memcpy(byte[] data, byte[] src, int len) {
		memset(data);
		for (int i = 0; i < len; i++) {
			data[i] = src[i];
		}
	}

	public static void memset(byte[] str) {
		memset(str, 0, str.length);
	}

	public static void memset(byte[] str, int start, int len) {
		int num = Math.min(str.length - start, len);
		for (int i = 0; i < num; i++) {
			str[start + i] = 0;
		}
	}

	public static int getByteStringLen(byte[] str, int offset, int len) {
		for (int i = 0; i < len; i++) {
			if (str[offset + i] == 0) {
				len = i;
				break;
			}
		}
		return len;
	}
}
