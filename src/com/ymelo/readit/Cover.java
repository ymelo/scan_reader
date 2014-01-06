package com.ymelo.readit;

import java.io.File;

import android.database.Cursor;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.util.Log;

public class Cover implements Parcelable {
	private static final String TAG = Cover.class.getSimpleName();
	
	
	public static final String COVER_KEY = "cover";
	private static long idCounter = 0;
	public static final String COL_TITLE = "title";
	public static final String COL_PATH_TO_ROOT = "path_to_root";
	//public static final String COL_PROVIDER = "provider";
	public static final String COL_COVER_NAME = "cover_name";
	public static final String COL_IMAGE_PATH = "image_path";

	public String title;
	public String pathToRootDirectory;
	public String provider;
	public String coverName;
	public long id;

//	public Cover(long id, String title, String pathToRootDirectory, String coverName) {
//		this.title = title;
//		this.coverName = coverName;
//		this.pathToRootDirectory = pathToRootDirectory;
//		this.provider = "SD card";
//		this.id = id;
//	}
//	public Cover(String title, String pathToRootDirectory) {
//		this.title = title;
//		this.coverName = title;
//		this.pathToRootDirectory = pathToRootDirectory;
//		this.provider = "SD card";
//		id = idCounter++;
//	}
//
//	public Cover(String title, String pathToRootDirectory, String coverName) {
//		this.title = title;
//		this.coverName = coverName;
//		this.pathToRootDirectory = pathToRootDirectory;
//		this.provider = "SD card";
//		id = idCounter++;
//	}

	public long getId() {
		return id;
	}
	public String getCoverBitmapPath() {
		return pathToRootDirectory + "/" + coverName;
	}

	// public Bitmap getCoverBitmap() {
	// File f = new File(pathToRootDirectory + "/" + coverName);
	// String[] files = f.list();
	// if(files != null && files.length > 0)
	// return BitmapFactory.decodeFile(pathToRootDirectory + "/" + coverName +
	// "/" + files[0]);
	// else
	// return null;
	// }

	public String getDirectory() {
		return pathToRootDirectory;
	}

//	static public Cover getCoverFromCursor(Cursor cursor) {
//		final long id = cursor.getLong(cursor
//				.getColumnIndex(BaseColumns._ID));
//		final String name = cursor.getString(cursor
//				.getColumnIndex(Cover.COL_COVER_NAME));
//		final String path = cursor.getString(cursor
//				.getColumnIndex(Cover.COL_PATH_TO_ROOT));
//		final String title = cursor.getString(cursor
//				.getColumnIndex(Cover.COL_TITLE));
//		return new Cover(id, title, path, name);
//	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(pathToRootDirectory);
		dest.writeString(provider);
		dest.writeString(coverName);

	}

	public static final Parcelable.Creator<Cover> CREATOR = new Parcelable.Creator<Cover>() {
		public Cover createFromParcel(Parcel in) {
			return new Cover(in);
		}

		public Cover[] newArray(int size) {
			return new Cover[size];
		}
	};
	

	private Cover(Parcel in) {
		this.title = in.readString();
		this.pathToRootDirectory = in.readString();
		this.provider = in.readString();
		this.coverName = in.readString();
	}
	
	static private String getImageLocalFilePath(String title, String uri) {
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
