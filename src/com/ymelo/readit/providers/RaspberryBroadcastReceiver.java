package com.ymelo.readit.providers;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.ymelo.readit.Chapter;
import com.ymelo.readit.Config;
import com.ymelo.readit.Page;
import com.ymelo.readit.datastore.RaspberryStore;
import com.ymelo.readit.datastore.ResourceDatabaseHelper;
import com.ymelo.readit.datastore.ResourceDescriptionContract;
import com.ymelo.readit.datastore.ResourceDescriptionContract.Pages;

public class RaspberryBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = RaspberryBroadcastReceiver.class
			.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Bundle bundle = intent.getExtras();
		String s = bundle.getString(Config.PAGE_KEY);
		ResourceDatabaseHelper mDbHelper = new ResourceDatabaseHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		/*
		 * Clean up the current database (remove chapter without pages, remove books without chapters)
		 */
		ResourceDescriptionContract.deleteChapter(db, context);
		ResourceDescriptionContract.deleteBook(db, context);
		db.close();
		mDbHelper.close();
		if(s != null) {
			String bookId = bundle.getString(Config.BOOK_ID_KEY);
			String chapterId = bundle.getString(Config.CHAPTER_ID_KEY);
			parsePages(context, bookId, chapterId, s);
		}
		else {
			s = bundle.getString(Config.CHAPTER_ID_KEY);
			parseChapters(context, s);
		}
		
	}
	
	public void parsePages(Context context, String bookId, String chapterId, String s) {
		boolean notifyDataChanged = true;
		if(Config.DEBUG) {
			Log.d(TAG, " RaspberryBroadcastReceiver received pages ");
		}
		RaspberryStore store = new RaspberryStore();
		ArrayList<Page> pages = store.streamStringToPageArray(s);
		
		ResourceDatabaseHelper mDbHelper = new ResourceDatabaseHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		for (int i = pages.size() - 1; i-- > 0;) {
			if(ResourceDescriptionContract.getPageId(db, context, Config.SERVER_URL + "/" + pages.get(i).remoteResourceUrl) == -1) {
				if(!notifyDataChanged) {
					notifyDataChanged = true;
				}
				ResourceDescriptionContract.insertResource(db, bookId, chapterId, Config.SERVER_URL + "/" + pages.get(i).remoteResourceUrl, null, context);
			}
		}
		mDbHelper.close();
		if(notifyDataChanged) {
			Uri uri = Uri.withAppendedPath(RaspberryCoversProvider.CONTENT_URI, AbstractCoversProvider.TABLE_CONTENT_IMAGE);
			context.getContentResolver().notifyChange(uri, null);
		}
	}
	
	public void parseChapters(Context context, String s) {
		boolean notifyDataChanged = true;
		if(Config.DEBUG) {
			Log.d(TAG, " RaspberryBroadcastReceiver received data ");
		}
		RaspberryStore store = new RaspberryStore();
		ArrayList<Chapter> chapters = store.streamStringToChapterArray(s);
		long bookId, chapterId;
		ResourceDatabaseHelper mDbHelper = new ResourceDatabaseHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		for (int i = chapters.size() - 1; i-- > 0;) {
			bookId = ResourceDescriptionContract.getBookId(db, context, chapters.get(i).parent.bookName, RaspberryStore.class.getSimpleName());
			if(bookId == -1) {
				
				 bookId = ResourceDescriptionContract.insertBook(db, context,
						 chapters.get(i).parent.bookName,
						 RaspberryStore.class.getSimpleName());
				 notifyDataChanged = true;
				 if(Config.DEBUG) {
						Log.d(TAG, " RaspberryBroadcastReceiver inserted book " + chapters.get(i).parent.bookName + " with bookId " + bookId);
				}
			}
			chapterId = ResourceDescriptionContract.getChapterId(db, context, bookId, chapters.get(i).chapterName);
			if(chapterId == -1) {
				chapterId = ResourceDescriptionContract.insertChapter(db, bookId,
						 chapters.get(i).chapterName, Config.SERVER_URL + "/" + chapters.get(i).remoteResourceUri,
						 context);
				notifyDataChanged = true;
				if(Config.DEBUG) {
					Log.d(TAG, " RaspberryBroadcastReceiver inserted chapter " + chapters.get(i).chapterName + " with chapterId " + chapterId);
				}
			}			
		}
		mDbHelper.close();
		if(notifyDataChanged) {
			Uri uri = Uri.withAppendedPath(RaspberryCoversProvider.CONTENT_URI, AbstractCoversProvider.TABLE_LIST);
			context.getContentResolver().notifyChange(uri, null);
		}
	}
}
