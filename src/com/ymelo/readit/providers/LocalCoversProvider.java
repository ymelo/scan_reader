package com.ymelo.readit.providers;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.ymelo.readit.datastore.LocalStore;

public class LocalCoversProvider extends AbstractCoversProvider{
	private static final String SELF_URI = "com.ymelo.readit.local";
	 static
	    {
		 	sUriMatcher.addURI(SELF_URI, TABLE_LIST, TABLE_LIST_ID);
		 	sUriMatcher.addURI(SELF_URI, TABLE_CONTENT_IMAGE, TABLE_CONTENT_IMAGE_ID);
		 	sUriMatcher.addURI(SELF_URI, TABLE_LOCALPATH, TABLE_LOCALPATH_ID);
	    }
	@Override
	public boolean onCreate() {
		store = new LocalStore();
		return true;
	}
		
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		LocalStore store = new LocalStore();
		switch (sUriMatcher.match(uri)) {
		case TABLE_LIST_ID:
			return store.getCoverCursor(getContext(), null);
		case TABLE_CONTENT_IMAGE_ID:
			return store.getPageListCursor(getContext(), selection, null, null, selectionArgs[0], selectionArgs[1]);
		}
		
		return null;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
