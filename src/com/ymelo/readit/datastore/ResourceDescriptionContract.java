package com.ymelo.readit.datastore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class ResourceDescriptionContract {
	public ResourceDescriptionContract() {
	}

	public static abstract class Pages implements BaseColumns {
		public static final String TABLE_NAME = "online_to_local";
		public static final String _ID = "_id";
		public static final String COL_BOOK_ID = "book_id";
		public static final String COL_CHAPTER_ID = "chapter_id";
		public static final String COL_ONLINE_RESOURCE = "online_resource";
		public static final String COL_LOCAL_RESOURCE = "local_resource";
	}

	public static abstract class ChapterInformation implements BaseColumns {
		public static final String _ID = "_id";
		public static final String TABLE_NAME = "chapter";
		public static final String COL_BOOK_ID = "chapter_book_id";
		public static final String COL_COVER_DISPLAY_NAME = "chapter_title";
		public static final String COL_COVER_LOCAL = "chapter_local_resource";
		public static final String COL_COVER_REMOTE = "chapter_remote_resource";
	}

	public static abstract class BookInformation implements BaseColumns {
		public static final String _ID = "_id";
		public static final String TABLE_NAME = "book";
		public static final String COL_TITLE = "book_title";
		public static final String COL_PROVIDER_ID = "book_provider_id";
	}

	public static long insertBook(SQLiteDatabase db, Context context,
			String title, String providerName) {
		// ResourceDatabaseHelper mDbHelper = new
		// ResourceDatabaseHelper(context);
		// SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(BookInformation.COL_TITLE, title);
		values.put(BookInformation.COL_PROVIDER_ID, providerName);

		// Insert the new row, returning the primary key value of the new row
		long id = db.insert(BookInformation.TABLE_NAME, null, values);
		return id;

	}

	public static long insertChapter(SQLiteDatabase db, long bookId,
			String chapterName, String remoteUri, Context context) {
		// ResourceDatabaseHelper mDbHelper = new
		// ResourceDatabaseHelper(context);
		// SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ChapterInformation.COL_BOOK_ID, bookId);
		values.put(ChapterInformation.COL_COVER_DISPLAY_NAME, chapterName);
		values.put(ChapterInformation.COL_COVER_REMOTE, remoteUri);

		// Insert the new row, returning the primary key value of the new row
		long id = db.insert(ChapterInformation.TABLE_NAME, null, values);
		return id;
	}

	public static long insertResource(SQLiteDatabase db, String bookId,
			String chapterId, String onlineUrl, String localUri, Context context) {
		// ResourceDatabaseHelper mDbHelper = new
		// ResourceDatabaseHelper(context);
		// SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Pages.COL_BOOK_ID, bookId);
		values.put(Pages.COL_CHAPTER_ID, chapterId);
		values.put(Pages.COL_ONLINE_RESOURCE, onlineUrl);
		values.put(Pages.COL_LOCAL_RESOURCE, localUri);
		// Insert the new row, returning the primary key value of the new row
		long id = db.insert(Pages.TABLE_NAME, null, values);
		return id;

	}

	public static long insertResource(SQLiteDatabase db, Context context,
			String tableName, ContentValues values) {
		// ResourceDatabaseHelper mDbHelper = new
		// ResourceDatabaseHelper(context);
		// SQLiteDatabase db = mDbHelper.getWritableDatabase();
		long id = db.insert(tableName, null, values);
		return id;
	}

	public static long getBookId(SQLiteDatabase db, Context context,
			String bookName, String providerName) {
		// ResourceDatabaseHelper mDbHelper = new
		// ResourceDatabaseHelper(context);
		// SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String[] projection = { BookInformation._ID, BookInformation.COL_TITLE };
		String selection = BookInformation.COL_PROVIDER_ID + "=?";
		String[] selectionArgs = { providerName };
		Cursor cursor = db.query(BookInformation.TABLE_NAME, // The table to
																// query
				projection, // The columns to return
				selection, // The columns for the WHERE clause
				selectionArgs, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);
		cursor.moveToFirst();
		if (cursor.getCount() != 1) {
			return -1;
		}
		long id = cursor.getLong(cursor
				.getColumnIndexOrThrow(BookInformation._ID));
		// mDbHelper.close();
		return id;
	}

	/**
	 * 
	 * @param context
	 * @return cursor containing all the books rows with keys
	 *         {@link BookInformation._ID} {@link BookInformation.COL_TITLE} DO
	 *         NOT forget to close the database after closing the cursor
	 */
	public static Cursor getAllBooks(SQLiteDatabase db, Context context) {
		// ResourceDatabaseHelper mDbHelper = new
		// ResourceDatabaseHelper(context);
		// SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String[] projection = { BookInformation._ID, BookInformation.COL_TITLE };

		Cursor cursor = db.query(BookInformation.TABLE_NAME, // The table to
																// query
				projection, // The columns to return
				null, // The columns for the WHERE clause
				null, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);
		return cursor;

	}

	public static long getChapterId(SQLiteDatabase db, Context context,
			long bookId, String chapterName) {
		// ResourceDatabaseHelper mDbHelper = new
		// ResourceDatabaseHelper(context);
		// SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String[] projection = { ChapterInformation._ID };
		String selection = ChapterInformation.COL_BOOK_ID + "=? AND "
				+ ChapterInformation.COL_COVER_DISPLAY_NAME + "=?";
		String[] selectionArgs = { String.valueOf(bookId), chapterName };
		Cursor cursor = db.query(ChapterInformation.TABLE_NAME, // The table to
																// query
				projection, // The columns to return
				selection, // The columns for the WHERE clause
				selectionArgs, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);
		cursor.moveToFirst();
		if (cursor.getCount() != 1) {
			return -1;
		}
		long id = cursor.getLong(cursor
				.getColumnIndexOrThrow(ChapterInformation._ID));
		// mDbHelper.close();
		return id;
	}

	public static Cursor getChapters(SQLiteDatabase db, Context context,
			long bookId) {
		return getChapters(db, context, bookId, null);
	}
	/**
	 * 
	 * @param context
	 * @param bookId
	 * @return Cursor containing the list of all the chapters for the book
	 *         bookId Cursor contains {@link ChapterInformation.COL_BOOK_ID,
	 *         ChapterInformation.COL_COVER_DISPLAY_NAME,
	 *         ChapterInformation.COL_COVER_LOCAL,
	 *         ChapterInformation.COL_COVER_REMOTE} DO NOT forget to close the
	 *         database after closing the cursor
	 */
	public static Cursor getChapters(SQLiteDatabase db, Context context,
			long bookId, String searchKey) {
		// ResourceDatabaseHelper mDbHelper = new
		// ResourceDatabaseHelper(context);
		// SQLiteDatabase db = mDbHelper.getWritableDatabase();
		// String[] projection = { ChapterInformation._ID,
		// ChapterInformation.COL_BOOK_ID,
		// ChapterInformation.COL_COVER_DISPLAY_NAME,
		// ChapterInformation.COL_COVER_LOCAL,
		// ChapterInformation.COL_COVER_REMOTE, };

		String query = "SELECT " + ChapterInformation.TABLE_NAME + "."
				+ ChapterInformation._ID + ", " + ChapterInformation.TABLE_NAME
				+ "." + ChapterInformation.COL_BOOK_ID + ", "
				+ ChapterInformation.TABLE_NAME + "."
				+ ChapterInformation.COL_COVER_DISPLAY_NAME + ", "
				+ ChapterInformation.TABLE_NAME + "."
				+ ChapterInformation.COL_COVER_LOCAL + ", "
				+ ChapterInformation.TABLE_NAME + "."
				+ ChapterInformation.COL_COVER_REMOTE + ", "
				+ BookInformation.TABLE_NAME + "." + BookInformation.COL_TITLE
				+ " FROM " + ChapterInformation.TABLE_NAME + " INNER JOIN "
				+ BookInformation.TABLE_NAME + " on "
				+ ChapterInformation.TABLE_NAME + "."
				+ ChapterInformation.COL_BOOK_ID + "="
				+ BookInformation.TABLE_NAME + "." + BookInformation._ID
				;
		;
		String selection = null;
		if (bookId != -1) {
			selection = " WHERE " + ChapterInformation.COL_BOOK_ID + " = "
					+ bookId;
			query += selection;
		}
		if(searchKey != null) {
			selection = " WHERE " + ChapterInformation.COL_COVER_DISPLAY_NAME + " like '%" + searchKey + "%'";
			query += selection;
		}
		query += " ORDER BY " + ChapterInformation.TABLE_NAME + "."
		+ ChapterInformation._ID + " DESC";
		Cursor cursor = db.rawQuery(query, null);
		// Cursor cursor = db.query(ChapterInformation.TABLE_NAME, // The table
		// to
		// // query
		// projection, // The columns to return
		// selection, // The columns for the WHERE clause
		// null, // The values for the WHERE clause
		// null, // don't group the rows
		// null, // don't filter by row groups
		// null // The sort order
		// );
		return cursor;

	}

	// public static Cursor getAllChapters(Context context) {
	// return getChapters(context, -1);
	// }

	public static Cursor getBook(SQLiteDatabase db, Context context, long bookId) {
		// ResourceDatabaseHelper mDbHelper = new
		// ResourceDatabaseHelper(context);
		// SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String[] projection = { BookInformation._ID, BookInformation.COL_TITLE };
		String selection = " WHERE " + BookInformation._ID + " = " + bookId;
		Cursor cursor = db.query(BookInformation.TABLE_NAME, // The table to
																// query
				projection, // The columns to return
				selection, // The columns for the WHERE clause
				null, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);
		return cursor;
	}

	public static String getBookName(SQLiteDatabase db, Context context,
			long bookId) {
		Cursor cursor = getBook(db, context, bookId);
		if (cursor.getCount() == -1) {
			cursor.moveToFirst();
			String title = cursor.getString(cursor
					.getColumnIndexOrThrow(BookInformation.COL_TITLE));
			return title;
		}
		return null;

	}

	public static Cursor getPages(SQLiteDatabase db, Context context,
			String bookId) {
		String[] projection = { Pages._ID, Pages.COL_CHAPTER_ID,
				Pages.COL_LOCAL_RESOURCE, Pages.COL_ONLINE_RESOURCE };
		String selection = Pages.COL_BOOK_ID + " like ? ";
		Cursor cursor = db.query(Pages.TABLE_NAME, // The table to
													// query
				projection, // The columns to return
				selection, // The columns for the WHERE clause
				new String[] { bookId }, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);
		return cursor;

	}

	public static int updateLocalUri(SQLiteDatabase db, Context context,
			String localUri, String remoteUri) {
		ContentValues cv = new ContentValues();
		cv.put(Pages.COL_LOCAL_RESOURCE, localUri);
		int result = db.update(Pages.TABLE_NAME, cv, Pages.COL_ONLINE_RESOURCE
				+ " like ?", new String[] { remoteUri });
		return result;
	}

	public static String getLocalUri(SQLiteDatabase db, Context context,
			String remoteUri) {
		String[] projection = { Pages.COL_LOCAL_RESOURCE };
		String selection = Pages.COL_ONLINE_RESOURCE + " like ?";
		Cursor cursor = db.query(Pages.TABLE_NAME, // The table to
													// query
				projection, // The columns to return
				selection, // The columns for the WHERE clause
				null,// new String[]{remoteUri}, // The values for the WHERE
						// clause
				null, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {

			String title = cursor.getString(cursor
					.getColumnIndexOrThrow(Pages.COL_LOCAL_RESOURCE));
			return title;
		}

		return null;
	}

	public static long getPageId(SQLiteDatabase db, Context context,
			String remoteResource) {
		String[] projection = { Pages._ID };
		String selection = Pages.COL_ONLINE_RESOURCE + " like ?";
		String[] selectionArgs = { remoteResource };
		Cursor cursor = db.query(Pages.TABLE_NAME, // The table to
													// query
				projection, // The columns to return
				selection, // The columns for the WHERE clause
				selectionArgs, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);
		cursor.moveToFirst();
		if (cursor.getCount() != 1) {
			return -1;
		}
		long id = cursor.getLong(cursor.getColumnIndexOrThrow(Pages._ID));
		return id;
	}

	public static String[] getEmptyBookIds(SQLiteDatabase db, Context context) {
		String query = "SELECT " + BookInformation._ID + " FROM "
				+ BookInformation.TABLE_NAME + " WHERE " + BookInformation._ID
				+ " NOT IN (SELECT " + ChapterInformation.COL_BOOK_ID
				+ " FROM " + ChapterInformation.TABLE_NAME + ")";
		return getIds(db, context, query);
	}

	public static String[] getEmptyChapterIds(SQLiteDatabase db, Context context) {
		String query = "SELECT " + ChapterInformation._ID + " FROM "
				+ ChapterInformation.TABLE_NAME + " WHERE "
				+ ChapterInformation._ID + " NOT IN (SELECT "
				+ Pages.COL_CHAPTER_ID + " FROM " + Pages.TABLE_NAME
				+ " WHERE " + Pages.COL_LOCAL_RESOURCE
				+ " IS NOT NULL GROUP BY " + Pages.COL_BOOK_ID + ", "
				+ Pages.COL_CHAPTER_ID + " HAVING count(*) > 0 )";
		return getIds(db, context, query);
	}

	public static String[] getIds(SQLiteDatabase db, Context context,
			String query) {
		Cursor cursor = db.rawQuery(query, null);
		cursor.moveToFirst();
		String[] list = new String[cursor.getCount()];
		// ArrayList<Long> list = new ArrayList<Long>(cursor.getCount());
		int i = 0;
		do {
			list[i] = String.valueOf(cursor.getLong(0));
		} while (cursor.moveToNext());
		return list;
	}

	/**
	 * Removes books without chapters
	 * 
	 * @param db
	 * @param context
	 * @return
	 */
	public static long deleteBook(SQLiteDatabase db, Context context) {
		// String queryNotEmptyBook = "SELECT "
		// + ChapterInformation.COL_BOOK_ID
		// + " FROM " + ChapterInformation.TABLE_NAME;
		// String query = "delete from " + BookInformation.TABLE_NAME +
		// " WHERE " + BookInformation._ID + " NOT IN (" + queryNotEmptyBook +
		// ");";
		// db.rawQuery(query, null);
		String query = "SELECT " + BookInformation._ID + " FROM "
				+ BookInformation.TABLE_NAME + " WHERE " + BookInformation._ID
				+ " NOT IN (SELECT " + ChapterInformation.COL_BOOK_ID
				+ " FROM " + ChapterInformation.TABLE_NAME + ")";
		String deleteQuery = "DELETE FROM " + BookInformation.TABLE_NAME
				+ " WHERE " + BookInformation._ID + " IN (" + query + ")";
		db.execSQL(deleteQuery);
		return 0;
	}

	/**
	 * Removes chapters without pages
	 * 
	 * @param db
	 * @param context
	 * @return
	 */
	public static long deleteChapter(SQLiteDatabase db, Context context) {
		// String queryNotEmptyChapter = "SELECT "
		// + Pages.COL_CHAPTER_ID + " FROM " + Pages.TABLE_NAME
		// + " WHERE " + Pages.COL_LOCAL_RESOURCE + " is null or " +
		// Pages.COL_LOCAL_RESOURCE + "=''";
		// String query = "delete from " + ChapterInformation.TABLE_NAME +
		// " WHERE " + ChapterInformation._ID + " NOT IN (" +
		// queryNotEmptyChapter + ");";
		// db.rawQuery(query, null);
		String query = "SELECT " + ChapterInformation._ID + " FROM "
				+ ChapterInformation.TABLE_NAME + " WHERE "
				+ ChapterInformation._ID + " NOT IN (SELECT "
				+ Pages.COL_CHAPTER_ID + " FROM " + Pages.TABLE_NAME
				+ " WHERE " + Pages.COL_LOCAL_RESOURCE
				+ " IS NOT NULL GROUP BY " + Pages.COL_BOOK_ID + ", "
				+ Pages.COL_CHAPTER_ID + " HAVING count(*) > 0 )";
		String deleteQuery = "DELETE FROM " + ChapterInformation.TABLE_NAME
				+ " WHERE " + ChapterInformation._ID + " IN (" + query + ")";
		db.execSQL(deleteQuery);
		return 0;
	}
}
