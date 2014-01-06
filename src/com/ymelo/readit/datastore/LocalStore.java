package com.ymelo.readit.datastore;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;

import com.ymelo.readit.Chapter;



public class LocalStore extends AbstractStore{
	public static final String DEFAULT_PATH = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES) + "/readit";//"/mnt/sdcard/Pictures/readit";
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	private String pathToRoot;
	
	public LocalStore() {
		
		this.pathToRoot = DEFAULT_PATH;
		type = 0;
	}
	
	public LocalStore(String pathToRoot) {
		this.pathToRoot = pathToRoot;
		type = 0;
	}
	
	public ArrayList<Chapter> getCovers(Context c, String searchKey) {
		//String[] files = c.fileList();
		File file = new File(pathToRoot);
		File[] files = file.listFiles();
		ArrayList<Chapter> covers = new ArrayList<Chapter>();
		if(files == null)
			return null;
		for (File f : files) {
			File rep = new File(f.getAbsolutePath());
			File[] images = rep.listFiles();
			if(images != null && images.length >0 ) {
				Chapter chap = new Chapter(null, -1, rep.getName(), f.getAbsolutePath() + "/" + images[0].getName(), rep.getName());
				covers.add(chap);
			}
			else {
				Chapter chap = new Chapter(null, -1, rep.getName(), null, pathToRoot);
				covers.add(chap);
			}
			
		}
		return covers;
	}
	
	
	
	public String[] getImagePaths (Context context, String directory) {
		if(directory == null)
			return null;
		File file = new File(directory);
		File[] files = file.listFiles();
		if(file == null)
			return null;
		String[] paths = new String[files.length];
		for(int i = 0; i < files.length ; i++) {
			paths[i] = files[i].getAbsolutePath();
		}
		return paths;
	}
	
	public void externalAvailability() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
	}

	public AsyncTask<Chapter, Void, Bitmap> getAsyncTask(ImageView img) {
		return new ThumbnailAsyncTaskFromPath(img);
	}
	
	private class ThumbnailAsyncTaskFromPath extends ThumbnailAsyncTask {
        private final ImageView mTarget;

        public ThumbnailAsyncTaskFromPath(ImageView target) {
            mTarget = target;
        }

        @Override
        protected void onPreExecute() {
            mTarget.setTag(this);
        }

        @Override
        protected Bitmap doInBackground(Chapter... params) {
            final Chapter c = params[0];
			return BitmapFactory.decodeFile(c.localResourceUri);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (mTarget.getTag() == this) {
                mTarget.setImageBitmap(result);
                mTarget.setTag(null);
//                updateCacheStatsUi();
            }
        }        
    }

	public String getImage(Chapter c) {
		return c.localResourceUri;
	}
}
