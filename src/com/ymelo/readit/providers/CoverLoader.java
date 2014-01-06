package com.ymelo.readit.providers;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.ymelo.readit.datastore.AbstractStore;

public class CoverLoader extends AsyncTaskLoader<Cursor> {
	private static final long STALE_DELTA = 1000;
	AbstractStore store;
	Cursor cursor;
	long lastLoad = 0;

	public CoverLoader(Context context, AbstractStore store2) {
		super(context);
		this.store = store2;
	}

	@Override
	public Cursor loadInBackground() {
		cursor = store.getCoverCursor(getContext(), null);
		return cursor;
	}

	@Override
	protected void onStartLoading() {
		if (cursor != null) {
			deliverResult(cursor);
		} else {
			if (System.currentTimeMillis() - lastLoad >= STALE_DELTA) {
				lastLoad = System.currentTimeMillis();
				forceLoad();
			}
	        
		}
	}
}
