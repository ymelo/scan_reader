package com.ymelo.readit.providers;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ymelo.readit.Config;
import com.ymelo.readit.datastore.RaspberryStore;
import com.ymelo.readit.datastore.ResourceDatabaseHelper;
import com.ymelo.readit.datastore.ResourceDescriptionContract;
import com.ymelo.readit.datastore.ResourceDescriptionContract.BookInformation;
import com.ymelo.readit.datastore.ResourceDescriptionContract.ChapterInformation;
import com.ymelo.readit.service.DownloadService;

public class RaspberryCoversProvider extends AbstractCoversProvider {
	SharedPreferences settings;
	private static final String TAG = RaspberryCoversProvider.class
			.getSimpleName();
	private static final String SELF_URI = "com.ymelo.readit.raspberry";
	public static final Uri CONTENT_URI = Uri.parse("content://" + SELF_URI + "/");
	public static final String CONTENT_URI_PATH = "content://" + SELF_URI;
	public static final Uri CONTENT_URI_BOOK = Uri.parse("content://" + SELF_URI + "/" + TABLE_BOOK);
	public static final Uri CONTENT_URI_CHAPTER = Uri.parse("content://" + SELF_URI + "/" + TABLE_CHAPTER);
//	public static final String CONTENT_URI_BOOK = "content://" + SELF_URI + TABLE_BOOK;
//	public static final String CONTENT_URI_BOOK = "content://" + SELF_URI + TABLE_BOOK;
//	public static final String CONTENT_URI_BOOK = "content://" + SELF_URI + TABLE_BOOK;
	
	static {
		sUriMatcher.addURI(SELF_URI, TABLE_LIST, TABLE_LIST_ID);
		sUriMatcher.addURI(SELF_URI, TABLE_CONTENT_IMAGE,
				TABLE_CONTENT_IMAGE_ID);
		sUriMatcher.addURI(SELF_URI, TABLE_LOCALPATH, TABLE_LOCALPATH_ID);
		sUriMatcher.addURI(SELF_URI, TABLE_BOOK, TABLE_BOOK_ID);
		sUriMatcher.addURI(SELF_URI, TABLE_CHAPTER, TABLE_CHAPTER_ID);
	}

	@Override
	public boolean onCreate() {
		store = new RaspberryStore();
		settings = PreferenceManager.getDefaultSharedPreferences(getContext());//getContext().getSharedPreferences(Config.SPREF, Context.MODE_PRIVATE);
		return true;
	}
	
//	@Override
//	public void shutdown() {
//		mOpenHelper.close();
//	    super.shutdown();
//	}

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
		ResourceDatabaseHelper mDbHelper = new ResourceDatabaseHelper(getContext());
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		long id = -1;
		String table = null;
		
		switch (sUriMatcher.match(uri)) {
		case TABLE_BOOK_ID:
			table = BookInformation.TABLE_NAME;
			id = ResourceDescriptionContract.getBookId(db, getContext(), values.getAsString(BookInformation.COL_TITLE), values.getAsString(BookInformation.COL_PROVIDER_ID));
			break;
		case TABLE_CHAPTER_ID:
			table = ChapterInformation.TABLE_NAME;
			id = ResourceDescriptionContract.getChapterId(db, getContext(), 
					values.getAsLong(ChapterInformation.COL_BOOK_ID), values.getAsString(ChapterInformation.COL_COVER_DISPLAY_NAME));
			break;
		}
		if(id == -1) {
			id = ResourceDescriptionContract.insertResource(db, getContext(), table, values);
			getContext().getContentResolver().notifyChange(uri, null);

		}
		mDbHelper.close();
		StringBuilder sb = new StringBuilder("content://");
		sb.append(SELF_URI).append("/").append(table).append("/").append(id);
		
		return Uri.parse(sb.toString());
	}

	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor;
		if(Config.DEBUG) {
			Log.d(TAG, "Raspberry provider received query " + uri + " " + selection);
			if(selectionArgs != null && selectionArgs.length > 0) {
				Log.d(TAG, "with args " + selectionArgs[0]);
			}	
		}
		
		switch (sUriMatcher.match(uri)) {
		case TABLE_LIST_ID:
//			if(settings != null) {
//				/*
//				 * start service only if the min next time is BELOW the current time (eg, we waited enough)
//				 */
//				if(settings.getBoolean(Config.CAN_UPDATE_LIST, true)) {
//					SharedPreferences.Editor editor = settings.edit();
//					/*
//					 * Do NOT UPDATE until the activity resets this boolean to true
//					 */
//				    editor.putBoolean(Config.CAN_UPDATE_LIST, false);
//				    editor.commit();
//					Intent broadcastReceiverIntent = new Intent(this.getContext(),
//							RaspberryBroadcastReceiver.class);
//					// create pending intent for broadcasting the DataBroadcastReceiver
//					PendingIntent pi = PendingIntent.getBroadcast(this.getContext(), 0,
//							broadcastReceiverIntent, 0);
//					Bundle bundle = new Bundle();
//					bundle.putParcelable(DownloadService.RECEIVER, pi);
//					// we want to start our service (for handling our time-consuming
//					// operation)
//					Intent serviceIntent = new Intent(this.getContext(),
//							DownloadService.class);
//					serviceIntent.putExtra("type", Config.CHAPTER_ID_KEY);
//					serviceIntent.putExtra(DownloadService.URLS, Config.SERVER_URL
//							+ "/cover.php?covers=1&number_of_pages=10");
//					serviceIntent.putExtras(bundle);
//					this.getContext().startService(serviceIntent);
//				}
//				
//			}
//			
			String search = null;
			if(selectionArgs != null && selectionArgs.length > 0) {
				search = selectionArgs[0];
			}
			cursor = store.getCoverCursor(getContext(), search);
					cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		case TABLE_CONTENT_IMAGE_ID:
			if(settings != null) {
				if(settings.getBoolean(Config.CAN_UPDATE_LIST, true)) {
					SharedPreferences.Editor editor = settings.edit();
					/*
					 * Do NOT UPDATE until the activity resets this boolean to true
					 */
				    editor.putBoolean(Config.CAN_UPDATE_LIST, false);
				    editor.commit();
					Intent broadcastReceiverIntent = new Intent(this.getContext(),
							RaspberryBroadcastReceiver.class);
					// create pending intent for broadcasting the DataBroadcastReceiver
					PendingIntent pi = PendingIntent.getBroadcast(this.getContext(), 0,
							broadcastReceiverIntent, 0);
					Bundle bundle = new Bundle();
					bundle.putParcelable(DownloadService.RECEIVER, pi);
					// we want to start our service (for handling our time-consuming
					// operation)
					Intent serviceIntent = new Intent(this.getContext(),
							DownloadService.class);
					serviceIntent.putExtra("type", Config.PAGE_KEY);
					serviceIntent.putExtra(DownloadService.URLS, Config.SERVER_URL + "/cover.php?images_list=1");
					serviceIntent.putExtra(Config.BOOK_ID_KEY, selectionArgs[0]);
					serviceIntent.putExtra(Config.CHAPTER_ID_KEY, selectionArgs[1]);
					serviceIntent.putExtras(bundle);
					this.getContext().startService(serviceIntent);
				}
			}
			
			
			
//			MatrixCursor matrix = new MatrixCursor(names);
//			String[] images = getImagePaths(getContext(), Config.SERVER_URL + "/cover.php?images_list=1");
//			int i = 0;
//			if(images != null) {
//				for (String imageUrl : images) {
//					String local = "";//BitmapUtils.localPathFromChapterInformation(bookName, chapterName, imageUrl);
//					MatrixCursor.RowBuilder rb = matrix.newRow();
//					rb.add(i++);
//					rb.add(bookId);
//					rb.add(chapterId);
//					rb.add(imageUrl);
//					rb.add(local);
//				}
//				return matrix;	
//			}
			cursor = store.getPageListCursor(getContext(), selection, selectionArgs[0], selectionArgs[1], null, null); 
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
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
