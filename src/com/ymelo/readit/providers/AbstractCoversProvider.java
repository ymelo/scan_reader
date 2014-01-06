package com.ymelo.readit.providers;

import android.content.ContentProvider;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.ymelo.readit.datastore.AbstractStore;
import com.ymelo.readit.datastore.ResourceDescriptionContract.BookInformation;
import com.ymelo.readit.datastore.ResourceDescriptionContract.ChapterInformation;

public abstract class AbstractCoversProvider  extends ContentProvider{
	/**
	 * Tables and IDs
	 */
	public static final String TABLE_LIST = "list";
	public static final int TABLE_LIST_ID = 0;
	public static final String TABLE_CONTENT_IMAGE = "content_image";
	public static final int TABLE_CONTENT_IMAGE_ID = 1;
	public static final String TABLE_LOCALPATH = "local_paths";
	public static final int TABLE_LOCALPATH_ID = 2;
	public static final int TABLE_BOOK_ID = 3;
	public static final String TABLE_BOOK = BookInformation.TABLE_NAME;
	public static final int TABLE_CHAPTER_ID = 4;
	public static final String TABLE_CHAPTER = ChapterInformation.TABLE_NAME;
	
	protected static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	 
	 protected AbstractStore store;
	 
		@Override
		public Cursor query(Uri uri, String[] projection, String selection,
				String[] selectionArgs, String sortOrder) {
			switch (sUriMatcher.match(uri)) {
			case TABLE_LIST_ID:
				return store.getCoverCursor(getContext(), null);
			case TABLE_CONTENT_IMAGE_ID:
				return store.getPageListCursor(getContext(), selection, null, null, selectionArgs[0], selectionArgs[1]);
			}
			
			return null;

		}
}
