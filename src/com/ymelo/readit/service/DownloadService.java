package com.ymelo.readit.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import com.ymelo.readit.Config;
import com.ymelo.readit.Page;
import com.ymelo.readit.providers.RaspberryBroadcastReceiver;
import com.ymelo.readit.utils.BitmapUtils;

public class DownloadService extends Service {
	private final IBinder mBinder = new MyBinder();
	public static final String RESULT = "result";
	public static final String NOTIFICATION = "com.ymelo.readit.service.receiver";
	public static final String COMMAND = "command";
	public static final String RECEIVER = "receiver";
	public static final String TITLE = "title";
	public static final String URLS = "urls";
	public static final String PAGES = "pages";
	public static final String PAGE = "page";
	public static final String CHAPTER ="chapter";
	public static final String LOCAL_IMG_PATH ="local_img";
	public static final int CMD_LOAD_LIST			= 0;
	public static final int CMD_LOAD_PAGES			= 1;
	public static final int CMD_LOAD 				= 2;
	public static final int CMD_ADD_URL 				= 3;
	public static final int CMD_CANCEL_ALL			= 4;
	private static final String TAG = DownloadService.class.getSimpleName();
	private static final int TIME_OUT = 10000;
	

	private ArrayList<Page> resourceUrls;
	ResultReceiver resultReceiver;
	

	public static void defaultStartService(Context context) {
		Intent broadcastReceiverIntent = new Intent(context,
				RaspberryBroadcastReceiver.class);
		// create pending intent for broadcasting the DataBroadcastReceiver
		PendingIntent pi = PendingIntent.getBroadcast(context, 0,
				broadcastReceiverIntent, 0);
		Bundle bundle = new Bundle();
		bundle.putParcelable(DownloadService.RECEIVER, pi);
		// we want to start our service (for handling our time-consuming
		// operation)
		Intent serviceIntent = new Intent(context,
				DownloadService.class);
		serviceIntent.putExtra("type", Config.CHAPTER_ID_KEY);
		serviceIntent.putExtra(DownloadService.URLS, Config.SERVER_URL
				+ "/cover.php?covers=1&number_of_pages=10");
		serviceIntent.putExtras(bundle);
		context.startService(serviceIntent);
	}
	
	public static void addChapterToDownloadQueue(Context context, String remoteUrl, long chapterId, PendingIntent receiver) {
		Intent serviceIntent = new Intent(context,
				DownloadService.class);
		serviceIntent.putExtra(DownloadService.COMMAND, DownloadService.CMD_ADD_URL);
		serviceIntent.putExtra(DownloadService.URLS, remoteUrl);
		serviceIntent.putExtra(DownloadService.CHAPTER, chapterId);
		serviceIntent.putExtra(DownloadService.RECEIVER, receiver);
		
		context.startService(serviceIntent);
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent);
		return Service.START_NOT_STICKY;
	}
	
	private void handleCommand(Intent intent) {

		Bundle bundle = intent.getExtras();
		int cmd = bundle.getInt(COMMAND);
		

		
		switch (cmd) {
		case CMD_LOAD_LIST:
			final String url = bundle.getString(URLS);
			final String type = bundle.getString("type");
			final String bookId = bundle.getString(Config.BOOK_ID_KEY);
			final String chapterId = bundle.getString(Config.CHAPTER_ID_KEY);
			final PendingIntent receiver = bundle.getParcelable("receiver");
			if(Config.DEBUG){
				Toast.makeText(this, "Service: " + url, Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(this, "Service downloading image list", Toast.LENGTH_SHORT).show();
			}
			
			Thread t = new Thread() {
				@Override
				public void run() {
					readHttpAndSendBackResults(type, receiver, url, bookId, chapterId);
				}
			};
			t.start();
			break;
		case CMD_LOAD_PAGES:
			
			boolean start = false;
			
			Parcelable[] parcel = bundle.getParcelableArray(PAGES);
			Page[] pages = new Page[parcel.length];
			for (int i = parcel.length; i-- > 0;) {
				pages[i] = (Page) parcel[i];
			}
			Toast.makeText(this, "Service: Downloading next " + pages.length + " pages", Toast.LENGTH_SHORT).show();
			if(resourceUrls == null || resourceUrls.size() == 0) {
				resourceUrls = new ArrayList<Page>();
				start = true;
			}
			for (int i = pages.length; i-- > 0;) {
				synchronized (this) {
					if(pages[i] != null) {
						if(Config.DEBUG){
							Log.d(TAG, "Received bitmap to load: " + pages[i].remoteResourceUrl);
						}
						resourceUrls.add(pages[i]);
					}
				}
			}
			if(start) {
				startDownloadingImages(getApplicationContext());	
			}
			
			break;
		case CMD_ADD_URL:
			Page p = bundle.getParcelable(PAGE);
			Toast.makeText(this, "Service: Adding " + p.remoteResourceUrl + " to downloading list", Toast.LENGTH_SHORT).show();
			if(resourceUrls != null && resourceUrls.size() > 0) {
				synchronized (this) {
					resourceUrls.add(p);
				}
				
			}
			else {
				synchronized (this) {
					resourceUrls = new ArrayList<Page>(20);
					resourceUrls.add(p);
				}
				startDownloadingImages(getApplicationContext());

			}

			break;
//		case CMD_LOAD:
//			Cover c = bundle.getParcelable(COVER);
//			title = c.title;
//			startDownloadingImages(c);
//			
//			break;

		default:
			break;
		}
	}
	
//	private void startDownloadingImages(Cover c) {
//		urls = store.getImagePaths(this, c.getDirectory());
//		startDownloadingImages();
//	}
	
	private void startDownloadingImages(final Context context) {
		Thread t = new Thread() {
			@Override
			public void run() {
				Page p;
				while(resourceUrls != null && resourceUrls.size() > 0) {
					p = null;
					synchronized (this) {
						p = resourceUrls.get(resourceUrls.size() -1 );
						resourceUrls.remove(resourceUrls.size() -1);
					}
					
					if(p.localResourceUri == null || p.localResourceUri.isEmpty()) {
						p.localResourceUri = BitmapUtils.localPathFromChapterInformation(p.bookId, p.chapterId, p.remoteResourceUrl);
						BitmapUtils.downloadImage(context, p.remoteResourceUrl, p.localResourceUri);	
					}
					publishResults(p, Activity.RESULT_OK);
				}
//				for(; urlIndex < urls.length;urlIndex++) {
//					downloadImage(title, urls[urlIndex]);	
//					publishResults(urls[urlIndex], Activity.RESULT_OK);
////					Bundle bundle = new Bundle();
////					bundle.putString(URLS, urls[urlIndex]);
////					resultReceiver.send(1, bundle);
//				}
			}
		};
		t.start();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class MyBinder extends Binder {
		DownloadService getService() {
			return DownloadService.this;
		}
	}
	
	private void publishResults(Page p, int result) {
	    Intent intent = new Intent(NOTIFICATION);
	    intent.putExtra(URLS, p.remoteResourceUrl);
	    intent.putExtra(LOCAL_IMG_PATH, p.localResourceUri);
	    intent.putExtra(Config.BOOK_ID_KEY, p.bookId);
	    intent.putExtra(Config.CHAPTER_ID_KEY, p.chapterId);
	    intent.putExtra(RESULT, result);
	    sendBroadcast(intent);
	}
	
	public void readHttpAndSendBackResults(String type, PendingIntent receiver, String urlString, String bookId, String chapterId) {
		try {
//			try {
//			Thread.sleep(3000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(TIME_OUT);
			if(receiver != null) {
				String s = readStream(con.getInputStream());
				Intent intent = new Intent();
				intent.putExtra(type, s);
				if(bookId != null) {
					intent.putExtra(Config.BOOK_ID_KEY, bookId);	
				}
				if(chapterId != null) {
					intent.putExtra(Config.CHAPTER_ID_KEY, chapterId);	
				}
				receiver.send(getApplicationContext(), 1, intent);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			showToastFromNonLooper(getString(com.ymelo.readit.R.string.err_connectivity));
		}
	}
	
	private String readStream(InputStream in) {
		StringBuilder lines = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				if(Config.DEBUG) {
					Log.d(TAG, "read line: " + line);
				}
				lines.append(line).append("\n");
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
		return lines.toString();
	}
	
	private void showToastFromNonLooper(final String message) {
		Handler h = new Handler(getApplicationContext().getMainLooper());
	    h.post(new Runnable() {
	        public void run() {
	        	Toast.makeText(getApplicationContext(), message , Toast.LENGTH_LONG).show();
	        }
	    });
	}


}
