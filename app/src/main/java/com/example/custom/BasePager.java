package com.example.custom;

import com.example.app.MyApp;

import android.app.Activity;

public abstract class BasePager {
	public Activity mActivity;
	public MyApp mMyApp;

	public BasePager(Activity activity) {
		mActivity = activity;
		mMyApp = (MyApp) mActivity.getApplication();
	}
}
