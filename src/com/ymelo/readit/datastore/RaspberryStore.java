package com.ymelo.readit.datastore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.ymelo.readit.Book;
import com.ymelo.readit.Chapter;
import com.ymelo.readit.Config;
import com.ymelo.readit.Page;
import com.ymelo.readit.datastore.ResourceDescriptionContract.BookInformation;
import com.ymelo.readit.datastore.ResourceDescriptionContract.ChapterInformation;

public class RaspberryStore extends AbstractStore {
	private static final String EMPTY = "";
	private static final int DEFAULT_MORE = 10;
	private static final String TAG = RaspberryStore.class.getSimpleName();

	public RaspberryStore() {
		type = 1;
	}

	public ArrayList<Chapter> getMoreCovers(Context c, String searchKey) {

		return getMoreCovers(c, DEFAULT_MORE, searchKey);
	}

	public ArrayList<Chapter> getMoreCovers(Context context, int amount, String searchKey) {
		ResourceDatabaseHelper mDbHelper = new ResourceDatabaseHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		Cursor cursor = ResourceDescriptionContract.getChapters(db, context, -1, searchKey);
		ArrayList<Chapter> covers = new ArrayList<Chapter>();
		//cursor.moveToFirst();
		while (cursor.moveToNext()){
			String bookName = cursor.getString(cursor
					.getColumnIndexOrThrow(BookInformation.COL_TITLE));
			long bookId = cursor.getLong(cursor.getColumnIndex(ChapterInformation.COL_BOOK_ID));
			long chapterId = cursor.getLong(cursor.getColumnIndex(ChapterInformation._ID));
			//String title = ResourceDescriptionContract.getBookName(context, bookId);
			
			String name = cursor.getString(cursor
					.getColumnIndexOrThrow(ChapterInformation.COL_COVER_DISPLAY_NAME));
			String local = cursor.getString(cursor
					.getColumnIndexOrThrow(ChapterInformation.COL_COVER_LOCAL));
			String remote = cursor.getString(cursor
					.getColumnIndexOrThrow(ChapterInformation.COL_COVER_REMOTE));
			Chapter c = new Chapter(new Book(bookId, bookName), chapterId, name, local, remote);
			covers.add(c);
		}
		mDbHelper.close();
		return covers;

//		try {
//			URL url = new URL(Config.SERVER_URL
//					+ "/cover.php?covers=1&number_of_pages=" + amount);
//			HttpURLConnection con = (HttpURLConnection) url.openConnection();
//			return readStream(con.getInputStream());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;

	}

	public ArrayList<Chapter> getCovers(Context c, String searchKey) {
		
		return getMoreCovers(c, searchKey);
	}

	public ArrayList<Chapter> streamStringToChapterArray(String stream) {
		ArrayList<Chapter> covers = new ArrayList<Chapter>();
		String array[] = stream.split("\n");
		if(array == null || array.length == 0) {
			return null;
		}
		if(array != null && array.length > 0) {
			for (String string : array) {
				String[] s = string.split(";");
				Book b = new Book(-1, s[0]);
				if(s != null && s.length >= 2) {
					Chapter c = new Chapter(b, -1, s[1], EMPTY, s[2]);
					covers.add(c);	
				}
			}	
		}
		
		return covers;
	}
	
	public ArrayList<Page> streamStringToPageArray(String stream) {
		ArrayList<Page> pages = new ArrayList<Page>();
		String array[] = stream.split("\n");
		for (String string : array) {
			Page c = new Page("-1","-1", "-1",null, string);
			pages.add(c);
		}
		return pages;
	}
	
//	private ArrayList<Cover> readStream(InputStream in) {
//		ArrayList<Cover> covers = new ArrayList<Cover>();
//		BufferedReader reader = null;
//		try {
//			reader = new BufferedReader(new InputStreamReader(in));
//			String line = "";
//			while ((line = reader.readLine()) != null) {
//				if(Config.DEBUG) {
//					Log.d(TAG, "read line: " + line);
//				}
//				String[] s = line.split(";");
//				covers.add(new Cover(s[0], Config.SERVER_URL, s[1]));
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (reader != null) {
//				try {
//					reader.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		return covers;
//	}

	public Bitmap getCoverBitmap(Chapter c) throws MalformedURLException,
			IOException {
		InputStream is = (InputStream) new URL(c.localResourceUri)
				.getContent();
		return BitmapFactory.decodeStream(is);
	}

	public Drawable loadImageFromURL(String url, String name) {
		try {
			InputStream is = (InputStream) new URL(url).getContent();
			Drawable d = Drawable.createFromStream(is, name);
			return d;
		} catch (Exception e) {
			return null;
		}
	}

	public String[] readImageListResponse(InputStream in) {
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				if(Config.DEBUG) {
					Log.d(TAG, "read line: " + line);
				}
				list.add(new String(Config.SERVER_URL + "/" + line));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		String[] strings = new String[list.size()];
		return list.toArray(strings);
	}

	@Override
	public String[] getImagePaths(Context context, String urlPath) {
		try {
			URL url = new URL(urlPath);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			return readImageListResponse(con.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

//	public AsyncTask<Cover, Void, Bitmap> getAsyncTask(ImageView img) {
//		return new ThumbAsyncTaskFromInternet(img);
//	}
//
//	private class ThumbAsyncTaskFromInternet extends ThumbnailAsyncTask {
//
//		private final ImageView mTarget;
//
//		public ThumbAsyncTaskFromInternet(ImageView target) {
//			mTarget = target;
//		}
//
//		@Override
//		protected void onPreExecute() {
//			mTarget.setTag(this);
//		}
//
//		@Override
//		protected Bitmap doInBackground(Cover... params) {
//			final Cover c = params[0];
//			InputStream is = null;
//			try {
//				Log.d(TAG, "downloading " + c.getCoverBitmapPath());
//				is = (InputStream) new URL(c.getCoverBitmapPath()).getContent();
//				return BitmapFactory.decodeStream(is);
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Bitmap result) {
//			if (mTarget.getTag() == this) {
//				mTarget.setImageBitmap(result);
//				mTarget.setTag(null);
//				// updateCacheStatsUi();
//			}
//		}
//	}

	public String downloadImage(String title, String uri) {
		InputStream is = null;
		FileOutputStream fos = null;
		String pathName = null;
		try {
			if(Config.DEBUG) {
				Log.d(TAG, "downloading " + uri);	
			}
			URL url = new URL(uri);
			
			
			File ext = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			File dir = new File(ext.getAbsoluteFile() + "/"
					+ Config.SHARED_DIR_NAME + "/" + title);
			dir.mkdirs();
			int last = uri.lastIndexOf('/');
			String fileName = uri.substring(last);
			pathName = dir.getAbsoluteFile() + fileName;
			File imgFile = new File(pathName);
			if(imgFile.exists()) {
				if(Config.DEBUG) {
					Log.d(TAG,  "file " + imgFile.getAbsolutePath() + " was found, using it instead of downloading new one");
				}
				return imgFile.getAbsolutePath();
			}
				
			is = (InputStream) url.getContent();
			fos = new FileOutputStream(new File(pathName));

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = is.read(bytes)) != -1) {
				fos.write(bytes, 0, read);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (is != null) {
					is.close();
				}
				if (fos != null) {
					fos.close();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return pathName;
	}


	public String getImage(Chapter c) {
		return Chapter.extrapolateImageLocalFilePath(c.chapterName, c.localResourceUri);
	}

}
