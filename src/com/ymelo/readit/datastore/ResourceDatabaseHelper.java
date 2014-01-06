package com.ymelo.readit.datastore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ymelo.readit.datastore.ResourceDescriptionContract.BookInformation;
import com.ymelo.readit.datastore.ResourceDescriptionContract.ChapterInformation;
import com.ymelo.readit.datastore.ResourceDescriptionContract.Pages;

public class ResourceDatabaseHelper extends SQLiteOpenHelper {
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	
	private static final String SQL_CREATE_BOOK =
		    "CREATE TABLE " + BookInformation.TABLE_NAME + " (" +
		    		BookInformation._ID + " INTEGER PRIMARY KEY," +
		    		BookInformation.COL_TITLE + TEXT_TYPE + COMMA_SEP +
		    		BookInformation.COL_PROVIDER_ID + TEXT_TYPE +
		    " )";

	private static final String SQL_CREATE_CHAPTER =
		    "CREATE TABLE " + ChapterInformation.TABLE_NAME + " (" +
		    		ChapterInformation._ID + " INTEGER PRIMARY KEY," +
		    		ChapterInformation.COL_BOOK_ID + TEXT_TYPE + COMMA_SEP +
		    		ChapterInformation.COL_COVER_DISPLAY_NAME + TEXT_TYPE + COMMA_SEP +
		    		ChapterInformation.COL_COVER_REMOTE + TEXT_TYPE + COMMA_SEP +
		    		ChapterInformation.COL_COVER_LOCAL + TEXT_TYPE +
		    " )";
	
	private static final String SQL_CREATE_OTL =
	    "CREATE TABLE " + Pages.TABLE_NAME + " (" +
	    		Pages._ID + " INTEGER PRIMARY KEY," +
	    		Pages.COL_BOOK_ID + TEXT_TYPE + COMMA_SEP +
	    		Pages.COL_CHAPTER_ID + TEXT_TYPE + COMMA_SEP +
	    		Pages.COL_LOCAL_RESOURCE + TEXT_TYPE + COMMA_SEP +
	    		Pages.COL_ONLINE_RESOURCE + TEXT_TYPE +
	    " )";

	
	private static final String SQL_DELETE_OTL =
	    "DROP TABLE IF EXISTS " + Pages.TABLE_NAME;
	private static final String SQL_DELETE_BOOK =
		    "DROP TABLE IF EXISTS " + BookInformation.TABLE_NAME;
	private static final String SQL_DELETE_CHAPTER =
		    "DROP TABLE IF EXISTS " + ChapterInformation.TABLE_NAME;
	
	public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "readit.db";

    public ResourceDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BOOK);
        db.execSQL(SQL_CREATE_CHAPTER);
        db.execSQL(SQL_CREATE_OTL);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_BOOK);
        db.execSQL(SQL_DELETE_OTL);
        db.execSQL(SQL_DELETE_CHAPTER);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    

}
