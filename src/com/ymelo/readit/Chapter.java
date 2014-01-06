package com.ymelo.readit;

import java.io.File;

import com.ymelo.readit.datastore.ResourceDescriptionContract.BookInformation;
import com.ymelo.readit.datastore.ResourceDescriptionContract.ChapterInformation;

import android.database.Cursor;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

public class Chapter {
	private static final String TAG = Chapter.class.getSimpleName();
	public Book parent;
	public long chapterId;
	public String chapterName;
	public String localResourceUri;
	public String remoteResourceUri;
	
	public Chapter(Book parent_, long chapterId_, String chapterName_, String localResourceUri_, String remoteResourceUri_) {
		parent = parent_;
		chapterId = chapterId_;
		chapterName = chapterName_;
		localResourceUri = localResourceUri_;
		remoteResourceUri = remoteResourceUri_;
	}
	
	public Chapter(String chapterName_, String localResourceUri_, String remoteResourceUri_) {
		parent = null;
		chapterId = -1;
		chapterName = chapterName_;
		localResourceUri = localResourceUri_;
		remoteResourceUri = remoteResourceUri_;
	}

	public static Chapter getChapterFromCursor(Cursor cursor) {
		final String bookName = cursor.getString(cursor
				.getColumnIndex(BookInformation.COL_TITLE));
		final long bookId = cursor.getLong(cursor
				.getColumnIndex(BookInformation._ID));
		final long id = cursor.getLong(cursor
				.getColumnIndex(BaseColumns._ID));
		final String name = cursor.getString(cursor
				.getColumnIndex(ChapterInformation.COL_COVER_DISPLAY_NAME));
		final String path = cursor.getString(cursor
				.getColumnIndex(ChapterInformation.COL_COVER_LOCAL));
		final String remoteUrl = cursor.getString(cursor.getColumnIndex(ChapterInformation.COL_COVER_REMOTE));
		return new Chapter(new Book(bookId, bookName), id, name, path, remoteUrl);
	}
	
	static public String extrapolateImageLocalFilePath(String title, String uri) {
		String pathName = null;
		File ext = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File dir = new File(ext.getAbsoluteFile() + "/"
				+ Config.SHARED_DIR_NAME + "/" + title);

		int last = uri.lastIndexOf('/');
		String fileName = uri.substring(last);
		pathName = dir.getAbsoluteFile() + fileName;
		File imgFile = new File(pathName);
		if(Config.DEBUG) {
			Log.d(TAG, "Reading file " + pathName);
		}
		if(imgFile.exists()) {
			return imgFile.getAbsolutePath();
		} else {
			if(Config.DEBUG) {
				Log.i(TAG, "File " + pathName + " not found");
			}
		}
		return null;
	}
}
