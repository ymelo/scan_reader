package com.ymelo.readit.datastore;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.ymelo.readit.Chapter;
import com.ymelo.readit.datastore.ResourceDescriptionContract.BookInformation;
import com.ymelo.readit.datastore.ResourceDescriptionContract.ChapterInformation;
import com.ymelo.readit.datastore.ResourceDescriptionContract.Pages;

public abstract class AbstractStore {
	public static final String STORE_KEY = "store";
	public int type;
	public static final int LOCAL = 0;
	public static final int RASPBERRY = 1;
	public abstract ArrayList<Chapter> getCovers(Context c, String searchString);
	public abstract String[] getImagePaths (Context context, String url);
	//public abstract AsyncTask<Cover, Void, Bitmap> getAsyncTask(ImageView img);
	
	public Cursor getCoverCursor(Context c, String searchString) {

		String[] names = { BaseColumns._ID, BookInformation.COL_TITLE, ChapterInformation.COL_COVER_DISPLAY_NAME,
				ChapterInformation.COL_COVER_LOCAL, ChapterInformation.COL_COVER_REMOTE};
		MatrixCursor matrix = new MatrixCursor(names);
		ArrayList<Chapter> chapters = getCovers(c, searchString);
		int i = 0;
		if(chapters == null)
			return null;
		for (Chapter chapter : chapters) {
			MatrixCursor.RowBuilder rb = matrix.newRow();
			rb.add(i++);
			rb.add(chapter.parent.bookName);
			rb.add(chapter.chapterName);
			rb.add(chapter.localResourceUri);
			rb.add(chapter.remoteResourceUri);
		}

		return matrix;
	}
	
	public Cursor getPageListCursor(Context context, String resource, String bookId, String chapterId, String bookName, String chapterName) {
		String[] names = { BaseColumns._ID, Pages.COL_BOOK_ID, Pages.COL_CHAPTER_ID, Pages.COL_ONLINE_RESOURCE, Pages.COL_LOCAL_RESOURCE};
		ResourceDatabaseHelper dbHelper = new ResourceDatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		Cursor cursor = ResourceDescriptionContract.getPages(db, context, bookId);
		
		return cursor;

//		return null;
	}
	
//	public Cursor getBookInformation(Context context, long bookId) {
//		return ResourceDescriptionContract.getBook(context, bookId);
//	}
	public int getType() {
		return type;
	}
//	public Store getStore(int storeType) {
//		if(storeType == LOCAL) {
//			return new 
//		}
//	}
	
	//abstract public String getImageFilePath(String uri);

}
