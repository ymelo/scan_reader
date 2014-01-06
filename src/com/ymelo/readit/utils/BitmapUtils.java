package com.ymelo.readit.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.ymelo.readit.Config;
import com.ymelo.readit.datastore.ResourceDatabaseHelper;
import com.ymelo.readit.datastore.ResourceDescriptionContract;

public class BitmapUtils {
	
	private static final String TAG = BitmapUtils.class.getSimpleName();

	public static Bitmap decodeAndDownscaleBitmap(String filePath, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, options);
	}
	public static Bitmap decodeSampledBitmapFromInputSteam(InputStream is,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeStream(is);//, null, options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		if(Config.DEBUG) {
			Log.d("bitmap util", "scaling down the bitmap to: 1/" + inSampleSize);
		}
		return inSampleSize;
	}
	
	public static String localPathFromChapterInformation(String bookName, String chapterName, String imageUrl) {
		String pathName = null;
		File ext = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File dir = new File(ext.getAbsoluteFile() + "/"
				+ Config.SHARED_DIR_NAME + "/" + bookName + "/" + chapterName);
		dir.mkdirs();
		int last = imageUrl.lastIndexOf('/');
		String fileName = imageUrl.substring(last);
		pathName = dir.getAbsoluteFile() + fileName;
		return pathName;
	}
	
	public static String downloadImage(Context context, String remoteUrl, String destination) {
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			
			String local = shouldBitmapBeAvailable(context, remoteUrl);
			if(local != null) {
				if(Config.DEBUG) {
					Log.d(TAG, remoteUrl + " SHOULD be available on device, download canceled");
				}
				return local;
			}
			URL url = new URL(remoteUrl);
			
			File imgFile = new File(destination);
			if(imgFile.exists()) {
				if(Config.DEBUG) {
					Log.d(TAG,  "file " + imgFile.getAbsolutePath() + " was found, using it instead of downloading new one");
				}
				return imgFile.getAbsolutePath();
			}
			if(Config.DEBUG) {
				Log.d(TAG, "Current task: downloading " + remoteUrl);
			}
			is = (InputStream) url.getContent();
			fos = new FileOutputStream(new File(destination));

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = is.read(bytes)) != -1) {
				fos.write(bytes, 0, read);
			}
			ResourceDatabaseHelper dbHelper = new ResourceDatabaseHelper(context);
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ResourceDescriptionContract.updateLocalUri(db, context, destination, remoteUrl);
			db.close();
			dbHelper.close();
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
		return destination;
	}
	
	/**
	 * 
	 * @return String the local path to bitmap if the bitmap SHOULD be available for display
	 * Note that the bitmap MAY not be available, as this only checks the database for previous
	 * download.
	 * The bitmap may not be present, have been deleted...
	 */
	public static String shouldBitmapBeAvailable(Context context, String remoteUrl) {
		ResourceDatabaseHelper dbHelper = new ResourceDatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String s = ResourceDescriptionContract.getLocalUri(db, context, remoteUrl);
		db.close();
		dbHelper.close();
		return s;
	}
}
