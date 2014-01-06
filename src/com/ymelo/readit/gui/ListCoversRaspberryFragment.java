package com.ymelo.readit.gui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ymelo.readit.Config;

public class ListCoversRaspberryFragment extends ListCoversFragment{
	
	public ListCoversRaspberryFragment() {
		super();
		Bundle args = new Bundle();
		args.putString(Config.CONTENT_PROVIDER_URI, "content://com.ymelo.readit.raspberry/");
		this.setArguments(args);
	}
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		if(Config.DEBUG){
			Log.d(TAG, "Raspberry");
		}
		
		//store = new RaspberryStore();
		return super.onCreateView(inflater, container, savedInstanceState);
	}
}
