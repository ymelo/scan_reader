package com.ymelo.readit;

import android.database.Cursor;

public class Downloadable {
	public String imagePath;
	
	public Downloadable(String imagePath) {
		this.imagePath = imagePath;
	}
	public static Downloadable getDownloadableFromCursor(Cursor cursor) {
		final String imagePath = cursor.getString(cursor
				.getColumnIndex(Cover.COL_IMAGE_PATH));
		return new Downloadable(imagePath);
	}
}
