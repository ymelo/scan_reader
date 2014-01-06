package com.ymelo.readit.gui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ymelo.readit.Config;

public class ListCoversLocalFragment extends ListCoversFragment{
	public ListCoversLocalFragment() {
		super();
		Bundle args = new Bundle();
		args.putString(Config.CONTENT_PROVIDER_URI, "content://com.ymelo.readit.local/");
		this.setArguments(args);
	}
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		if(Config.DEBUG){
			Log.d(TAG, "Local");
		}
		//store = new LocalStore("/mnt/sdcard/Pictures/readit");
		return super.onCreateView(inflater, container, savedInstanceState);
	}
}
