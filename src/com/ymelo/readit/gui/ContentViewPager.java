package com.ymelo.readit.gui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ymelo.readit.Config;
import com.ymelo.readit.Page;
import com.ymelo.readit.R;
import com.ymelo.readit.datastore.ResourceDatabaseHelper;
import com.ymelo.readit.datastore.ResourceDescriptionContract;
import com.ymelo.readit.datastore.ResourceDescriptionContract.Pages;
import com.ymelo.readit.gui.views.ImageFragment;
import com.ymelo.readit.gui.views.TouchImageView.OnDoubleTapListener;
import com.ymelo.readit.providers.AbstractCoversProvider;
import com.ymelo.readit.service.DownloadService;

public class ContentViewPager extends FragmentActivity implements
		OnPageChangeListener, OnDoubleTapListener {
	public static final String TAG = "pagerviewtag";
	
	/*
	 * 
	 * SeekBar hiding stuff
	 */
	protected static final long HIDE_TIME = 3000;
	GestureDetector gestureDetector;
	
	/**
	 * All the pages that can be displayed on the view pager
	 */
	static Page[] pages = new Page[0];
	/**
	 * indexOnPages represents the starting index of a sub group in the group {@link pages}
	 * This group represents the pages for which we should preload the bitmap
	 * This group (called loadingPages for the explanation) starts at indexOnPages and ends
	 * 
	 * If loadingPages was an array on its own, we would fill it this way:
	 * int amountOfPreLoadedPages = 5;
	 * pages = new Page[100];
	 * //fill up pages
	 * loadingPages = new Page[amountOfPreLoadedPages];
	 * indexOnPages = 0;
	 * for(int i = 0; i < amountOfPreLoadedPages; i++) {
	 * 		loadingPages[i] = pages[i + indexOnPages];
	 * }
	 * 
	 */
	static int indexOnPages = 0;
	static int numberOfPagesToPreload = Config.DEFAULT_LOAD_IMAGES;
	
	/**
	 * Used as a temporary array every time we need to preload bitmaps
	 */
	static Page[] loadingPages = new Page[numberOfPagesToPreload];
	private int loaderCursorIndex = 0;
	
	long chapterId;
	long bookId;
	private CollectionPagerAdapter mDemoCollectionPagerAdapter;
	
	/*
	 * Views
	 */
	private ViewPager mViewPager;
	/**
	 * View seekPanel
	 * Displays a seekBar used to navigate through the pages
	 */
	protected View seekPanel;
	/**
	 * SeekBar seekBar
	 * Displays the current page selection
	 * Has to be updated every time the page is changed
	 * to reflect the current selection
	 */
	protected SeekBar seekBar;
	protected ProgressBar progress;
	/**
	 * TextView error
	 * Displays an error message
	 */
	protected TextView error;
	/**
	 * RelativeLayout loadingPanel
	 * Displays a loading wheel and contains {@link error}error
	 */
	protected RelativeLayout loadingPanel;
	
	/*
	 * Other
	 */
	protected String directory;
	//private AbstractStore store;
	/**
	 * Last value for the seekBar (last selected page)
	 */
	private int seekBarLastValue = -1;
	protected String contentProviderUri;

	private LoadingErrorListener listener = new LoadingErrorListener();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//getApplicationContext().getSharedPreferences(Config.SPREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean(Config.CAN_UPDATE_LIST, true);
	    editor.commit();
		setContentView(R.layout.content_pager);
		
		Bundle bundle = getIntent().getExtras();
		chapterId = bundle.getLong(Config.CHAPTER_ID_KEY);
		bookId = bundle.getLong(Config.BOOK_ID_KEY);
		mDemoCollectionPagerAdapter = new CollectionPagerAdapter(
				getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mDemoCollectionPagerAdapter);
		mViewPager.setOnPageChangeListener(this);
		
		seekBar = (SeekBar) findViewById(R.id.seekbar);
		seekPanel = (View) findViewById(R.id.seek_panel);
		//progress = (ProgressBar) findViewById(R.id.loading_pb);
		error = (TextView) findViewById(R.id.loading_tv_error);
		progress = (ProgressBar) findViewById(R.id.loading_pb);
		loadingPanel = (RelativeLayout) findViewById(R.id.loading_panel);
		/********styling*************/
		//seekBar.setThumb(null);
		
		
		/********listeners***********/
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {
				mViewPager.setCurrentItem(seekBarLastValue);
				if(Config.DEBUG) {
					Log.d(TAG, "pager current item set to: " + seekBarLastValue);
				}
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				seekBarLastValue = progress;
				
			}
		});
		contentProviderUri = bundle.getString(Config.CONTENT_PROVIDER_URI);
//		int type = bundle.getInt(AbstractStore.STORE_KEY);
//		if (type == 0) {
//			store = new LocalStore(LocalStore.DEFAULT_PATH);
//		} else {
//			store = new RaspberryStore();
//		}
		loaderCursorIndex = (int) (Math.random() * Integer.MAX_VALUE);
		getSupportLoaderManager().initLoader(loaderCursorIndex, null,
				coverLoaderCallbacks);
		displayProgress();
		
	}
	
	@Override
	protected void onResume() {
		registerReceiver(receiver, new IntentFilter(DownloadService.NOTIFICATION));
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}

	public class CollectionPagerAdapter extends FragmentStatePagerAdapter {
		private SparseArray<ImageFragment> mPageReferenceMap = new SparseArray<ImageFragment>();
		public CollectionPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		/**
		 * Searches in {@link FragmentManager} for {@link Fragment} at specified
		 * position
		 * 
		 * @param position
		 * @return
		 */

		private ImageFragment getFragmentAt(int position) {
			ImageFragment frg = null;
			frg = mPageReferenceMap.get(position);
			if(frg != null) {
				return frg;
			}
			FragmentManager fm = ContentViewPager.this
					.getSupportFragmentManager();
			frg = (ImageFragment) fm.findFragmentByTag(TAG + position);
			return frg;
		}

		@Override
		public ImageFragment getItem(int i) {
			
			if(Config.DEBUG) {
				Log.d(TAG, "fragment getItem(" + i + ")");
			}
			ImageFragment fragment = getFragmentAt(i);
			if (fragment == null) {
				fragment = new ImageFragment();
				fragment.setOnDoubleTapListener(ContentViewPager.this);
				fragment.setLoadingErrorListener(listener);
			}
			Bundle args = new Bundle();
			args.putString(ImageFragment.IMG_REMOTE_PATH, pages[i].remoteResourceUrl);
			args.putString(ImageFragment.IMG_LOCAL_PATH, pages[i].localResourceUri);
			args.putString(Config.CONTENT_PROVIDER_URI, contentProviderUri);
			args.putString("TAG", TAG + i);
			fragment.setArguments(args);
			mPageReferenceMap.put(i, fragment);
			// int progress = (int) ((i*100) / images.length);
			// ContentViewPager.this.pb.setProgress(progress);
			return fragment;
		}

		public int getItemPosition(Object item) {
			return POSITION_NONE;
		}

		@Override
		public int getCount() {
			return pages.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "OBJECT " + (position + 1);
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void destroyItem(View container, int position, Object object) {
		
			super.destroyItem(container, position, object);
			
			mPageReferenceMap.remove(position);
		}
	}

	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	public void onPageSelected(int page) {
		seekBar.setProgress(page);
		indexOnPages = page;
		hidePanels();
		preloadBitmapsViaService();
		

	}

	private final LoaderCallbacks<Cursor> coverLoaderCallbacks = new LoaderCallbacks<Cursor>() {

		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			//return new ImageLoader(getApplicationContext(), store, cover);
			
			return new  CursorLoader(ContentViewPager.this,
	                Uri.parse(contentProviderUri + AbstractCoversProvider.TABLE_CONTENT_IMAGE)
	                , null, null, new String[]{String.valueOf(bookId), String.valueOf(chapterId)}, null);
		}

		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			if(Config.DEBUG){
				Log.d(TAG, "onLoadFinished");
			}
			if(cursor == null) {
				displayError();
				return;
			}
			displaySeekPanelHideLoading();
				
			seekBar.setMax(cursor.getCount() - 1);
			cursor.moveToFirst();
			pages = new Page[cursor.getCount()];
			loadingPages = new Page[Config.DEFAULT_LOAD_IMAGES];
			for (int i = 0; i < cursor.getCount(); i++) {
				final String imagePath = cursor.getString(cursor
						.getColumnIndex(Pages.COL_LOCAL_RESOURCE));
				final String remoteImage = cursor.getString(cursor
						.getColumnIndex(Pages.COL_ONLINE_RESOURCE));
				pages[i] = new Page("", "", "", imagePath, remoteImage);
				
				cursor.moveToNext();
			}
			mDemoCollectionPagerAdapter.notifyDataSetChanged();
			preloadBitmapsViaService();
		}

		public void onLoaderReset(Loader<Cursor> loader) {
			if(Config.DEBUG) {
				Log.d(TAG, "Loader restarted");
			}
			getSupportLoaderManager().restartLoader(loaderCursorIndex, null,
					this);
		}

	};
	
	private void preloadBitmapsViaService() {
		for(int i = 0; i < Config.DEFAULT_LOAD_IMAGES; i++) {
			if(pages.length > indexOnPages + i) 
				loadingPages[i] = pages[indexOnPages + i];
		}
		Intent intent = new Intent(getApplicationContext(),
				DownloadService.class);
		intent.putExtra(DownloadService.COMMAND, DownloadService.CMD_LOAD_PAGES);
		intent.putExtra(DownloadService.TITLE, "test");
		intent.putExtra(DownloadService.PAGES, loadingPages);
		intent.putExtra(Config.BOOK_ID_KEY, bookId);
		intent.putExtra(Config.CHAPTER_ID_KEY, chapterId);
		startService(intent);
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				String remoteImg = bundle.getString(DownloadService.URLS);
				String localImg = bundle.getString(DownloadService.LOCAL_IMG_PATH);
				int resultCode = bundle.getInt(DownloadService.RESULT);
				if(Config.DEBUG) {
					Log.d(TAG, "Broadcast receiver");
				}
				if (resultCode == Activity.RESULT_OK) {
					int position = mViewPager.getCurrentItem();
					if(Config.DEBUG) {
						//Log.d(TAG, "Broadcast results OK");
						Log.d(TAG, "ContentViewPager Broadcast remoteImg: " + remoteImg);
						Log.d(TAG, "ContentViewPager Broadcast current page remoteImg: " + pages[position].remoteResourceUrl);
					}	
					if(remoteImg.equals(pages[position].remoteResourceUrl)) {
						if(mDemoCollectionPagerAdapter != null && mDemoCollectionPagerAdapter.getFragmentAt(position) != null)
							mDemoCollectionPagerAdapter.getFragmentAt(position).tryLoadingImage(localImg);
					}
				} else {			
				}
			}
		}
	};
	private Handler handler = new Handler();
	private Runnable taskHide = new Runnable() {

		public void run() {
			seekPanel.setVisibility(View.GONE);
		}

	};

	public void onDoubleTap() {
		if(Config.DEBUG) 
			Log.d(TAG, "view pager double tap event");
		handler.removeCallbacks(taskHide);
    	runOnUiThread(new Runnable() {

			public void run() {
				seekPanel.setVisibility(View.VISIBLE);	
				handler.postDelayed(taskHide, HIDE_TIME);
			}		
		});
		
	}
	
	public class LoadingErrorListener {
		public void loadingBitmapError(String remoteUrl) {
			ResourceDatabaseHelper dbHelper = new ResourceDatabaseHelper(getApplicationContext());
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ResourceDescriptionContract.updateLocalUri(db, getApplicationContext(), null, remoteUrl);
			db.close();
			dbHelper.close();
			Toast.makeText(getApplicationContext(), "Failed to retrieve bitmap at " + remoteUrl, Toast.LENGTH_SHORT).show();
			displayError();
			//preloadBitmapsViaService();
		}
	}
	
	private void displayError() {
		if(error.getVisibility() != View.VISIBLE) {
			error.setVisibility(View.VISIBLE);	
		}
		if(progress.getVisibility() != View.GONE){
			progress.setVisibility(View.GONE);	
		}
		if(loadingPanel.getVisibility() != View.VISIBLE) {
			loadingPanel.setVisibility(View.VISIBLE);	
		}
	}
	private void displayProgress() {
		if(error.getVisibility() == View.VISIBLE) {
			error.setVisibility(View.GONE);	
		}
		if(progress.getVisibility() != View.VISIBLE){
			progress.setVisibility(View.VISIBLE);	
		}
		if(loadingPanel.getVisibility() != View.VISIBLE) {
			loadingPanel.setVisibility(View.VISIBLE);	
		}
	}
	private void displaySeekPanelHideLoading() {
		if(loadingPanel.getVisibility() != View.GONE) {
			loadingPanel.setVisibility(View.GONE);
		}
		if(seekPanel.getVisibility() != View.VISIBLE) {
			seekPanel.setVisibility(View.VISIBLE);
			handler.postDelayed(taskHide, HIDE_TIME);	
		}
	}
	private void hidePanels() {
		if(loadingPanel.getVisibility() != View.GONE) {
			loadingPanel.setVisibility(View.GONE);
		}
		if(seekPanel.getVisibility() != View.GONE) {
			seekPanel.setVisibility(View.GONE);
		}
	}
	
}