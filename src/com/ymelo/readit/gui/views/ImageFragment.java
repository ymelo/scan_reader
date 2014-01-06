package com.ymelo.readit.gui.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ymelo.readit.Config;
import com.ymelo.readit.gui.ContentViewPager.LoadingErrorListener;
import com.ymelo.readit.gui.views.TouchImageView.OnDoubleTapListener;


// Instances of this class are fragments representing a single
// object in our collection.
public class ImageFragment extends Fragment implements OnDoubleTapListener{
	public static final String TITLE = "title";
	public static final String IMG_REMOTE_PATH = "remote_path";
	public static final String IMG_LOCAL_PATH = "local_path";
	public static final String ARG_OBJECT = "object";
	private static final String TAG = ImageFragment.class.getSimpleName();
	//private AbstractStore store = null;
	private String remoteImagePath, localImagePath;
	TouchImageView img;
	public OnDoubleTapListener listener;
	private LoadingErrorListener loadingErrorListener;
	
	public ImageFragment() {
		if(Config.DEBUG) {
			Log.d(TAG, "constructor called");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Bundle args = getArguments();
		remoteImagePath = args.getString(IMG_REMOTE_PATH);
		localImagePath = args.getString(IMG_LOCAL_PATH);
		img = new TouchImageView(getActivity().getApplicationContext());
		img.setImageBitmap(null);

		img.setMaxZoom(4f);
		img.listener = this;
		if(localImagePath != null)
			tryLoadingImage(localImagePath);

		return img;
	}
	@Override
	public void onPause() {
//		if(isBroadcastRegistered) {
//			getActivity().unregisterReceiver(receiver);
//			shouldRegisterBroadcastOnResume = true;
//			isBroadcastRegistered = false;
//		}
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
//		if(isBroadcastRegistered) {
//			getActivity().unregisterReceiver(receiver);
//			shouldRegisterBroadcastOnResume = true;
//			isBroadcastRegistered = false;
//		}
		super.onDestroy();
	}
	
	@Override
	public void onResume() {
//		if(shouldRegisterBroadcastOnResume) {
//			getActivity().registerReceiver(receiver, new IntentFilter(DownloadService.NOTIFICATION));
//			isBroadcastRegistered = true;
//			shouldRegisterBroadcastOnResume = false;
//		}
		super.onResume();
	}

	public void setLoadingErrorListener(LoadingErrorListener l) {
		loadingErrorListener = l;
	}
	
	public void tryLoadingImage(String localPath) {
		
		final AsyncTask<String,Void,Bitmap> task = new ThumbnailAsyncTask(img);
		img.setTag(task);
		task.execute(localPath);
	}

	public class ThumbnailAsyncTask extends AsyncTask<String, Void, Bitmap> {
		private final ImageView mTarget;

		public ThumbnailAsyncTask(ImageView target) {
			mTarget = target;
		}

		@Override
		protected void onPreExecute() {
			mTarget.setTag(this);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			String path = null;
			if(params != null && params.length == 1) 
				path = params[0];
			Bitmap bmp = null;
			synchronized(this) {
				if(Config.DEBUG) {
					Log.d(TAG, "Loading image " + path);
				}
				if (path != null) {
					bmp = BitmapFactory.decodeFile(path);
				} else {
					if(Config.DEBUG) {
						Log.d(TAG, "Loading image " + path + "(" + remoteImagePath + ") failed");
					}
					if(Config.WARNING) {
						Log.w(TAG, "ImageFragment doInBackground: path variable was null, bitmap decoding canceled");
					}
					bmp = BitmapFactory.decodeResource(getResources(),
							com.ymelo.readit.R.drawable.sample_7);
					//getActivity().registerReceiver(receiver, new IntentFilter(DownloadService.NOTIFICATION));
				}
			}
			
			return bmp;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if(result == null && loadingErrorListener != null) {
				loadingErrorListener.loadingBitmapError(remoteImagePath);
				return;
			}
			if (mTarget.getTag() == this) {
				mTarget.setImageBitmap(result);
				mTarget.setTag(null);
				// updateCacheStatsUi();
			}
		}
	}
	
	public void setOnDoubleTapListener(OnDoubleTapListener l) {
		listener = l;
	}

	public void onDoubleTap() {
		listener.onDoubleTap();
		
	}
}
