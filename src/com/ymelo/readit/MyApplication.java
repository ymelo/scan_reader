package com.ymelo.readit;

import com.ymelo.readit.service.DownloadService;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MyApplication extends Application{
	@Override
	public void onCreate() {
		super.onCreate();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		Config.SERVER_URL = sharedPref.getString("pref_key_server", getResources().getString(R.string.pref_default_server));
		DownloadService.defaultStartService(getApplicationContext());
	}
}
