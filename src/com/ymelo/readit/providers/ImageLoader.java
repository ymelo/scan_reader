package com.ymelo.readit.providers;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.ymelo.readit.Cover;
import com.ymelo.readit.datastore.AbstractStore;

public class ImageLoader  extends AsyncTaskLoader<Cursor> {
	private static final long STALE_DELTA = 1000;
	AbstractStore store;
	Cursor cursor;
	long lastLoad = 0;
	Cover cover;

	public ImageLoader(Context context, AbstractStore store2, Cover cover) {
		super(context);
		this.store = store2;
		this.cover = cover;
	}

	@Override
	public Cursor loadInBackground() {
		//cursor = store.getImageListCursor(getContext(), cover);
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

