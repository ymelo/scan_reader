package com.ymelo.readit.gui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ymelo.readit.Chapter;
import com.ymelo.readit.Config;
import com.ymelo.readit.R;
import com.ymelo.readit.datastore.ThumbnailAsyncTask;
import com.ymelo.readit.gui.views.StartDialog;
import com.ymelo.readit.providers.AbstractCoversProvider;
import com.ymelo.readit.providers.RaspberryBroadcastReceiver;
import com.ymelo.readit.service.DownloadService;
import com.ymelo.readit.utils.BitmapUtils;

public class ListCoversFragment extends Fragment implements
		android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>, OnQueryTextListener {
	protected static final String TAG = ListCoversFragment.class
			.getSimpleName();

	// private PendingIntent pi;

	private String contentProviderUri;
	private String selection;
	private String filter;
	private String[] selectionArgs;
	
	private int loaderCursor = 0;
	LayoutInflater gridInflater;
	private CoverAdapter adapter;
	private CoverCache mCache;
	// protected AbstractStore store;

	// private View mStats;
	private TextView mStatsSize;
	private TextView mStatsHits;
	private TextView mStatsMisses;
	private TextView mStatsEvictions;
	private View mStatsView;
	private boolean mShowStats = true;
	
	private boolean mShowProgressBar = true;

	public ListCoversFragment() {

	}

	public static final String ARG_SECTION_NUMBER = "section_number";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (loaderCursor == 0) {
			if (Config.DEBUG) {
				Log.d(TAG, "loaderCursor not 0");
			}
			loaderCursor = (int) (Math.random() * Integer.MAX_VALUE);
		}

		new Intent(this.getActivity().getApplicationContext(),
				RaspberryBroadcastReceiver.class);

		if (getLoaderManager().getLoader(loaderCursor) == null) {
			getLoaderManager().initLoader(loaderCursor, null, this);
		} else {
			getLoaderManager().restartLoader(loaderCursor, null, this);
		}
		setRetainInstance(true);
		//getActivity().setProgressBarIndeterminateVisibility(mShowProgressBar);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		cacheInit();

		Bundle args = getArguments();
		contentProviderUri = args.getString(Config.CONTENT_PROVIDER_URI);

		gridInflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View v = gridInflater.inflate(R.layout.grid_view, null);
		GridView gridView = (GridView) v.findViewById(R.id.gridview);
		adapter = new CoverAdapter(getActivity().getApplicationContext(),
				gridInflater);
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent intent = new Intent(getActivity()
						.getApplicationContext(), ContentViewPager.class);
				Cursor c = (Cursor) adapter.getItem(position);
				Chapter chapter = Chapter.getChapterFromCursor(c);
				intent.putExtra(Config.CHAPTER_ID_KEY, chapter.chapterId);
				intent.putExtra(Config.BOOK_ID_KEY, chapter.parent.bookId);
				// intent.putExtra(AbstractStore.STORE_KEY, store.getType());
				intent.putExtra(Config.CONTENT_PROVIDER_URI, contentProviderUri);
				startActivity(intent);
			}
		});

		gridView.setRecyclerListener(new RecyclerListener() {
			public void onMovedToScrapHeap(View view) {
				if(Config.DEBUG) {
					Log.d("recycling", "recycling image from cover_item_image");
				}
				// Release strong reference when a view is recycled
				final ImageView imageView = (ImageView) view
						.findViewById(R.id.cover_item_image);
				imageView.setImageBitmap(null);
			}
		});

		// mStats = v.findViewById(R.id.cache_stats_view);
		mStatsSize = (TextView) v.findViewById(R.id.cache_stats_tv_size);
		mStatsHits = (TextView) v.findViewById(R.id.cache_stats_tv_hit);
		mStatsMisses = (TextView) v.findViewById(R.id.cache_stats_tv_miss);
		mStatsEvictions = (TextView) v
				.findViewById(R.id.cache_stats_tv_eviction);
		mStatsView = v.findViewById(R.id.cache_stats_view);
		TextView tv = (TextView) v.findViewById(R.id.cache_stats_tv_name);
		tv.setText(contentProviderUri);
		
		getActivity().setProgressBarIndeterminate(true);
		return v;
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.remove("android:support:fragments");
	    
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		super.onCreateOptionsMenu(menu, inflater);
//		inflater.inflate(R.menu.main_actions, menu);
//		MenuItem item = menu.add("Search");
//        item.setIcon(android.R.drawable.ic_menu_search);
//        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//        SearchView sv = new SearchView(getActivity());
//        sv.setOnQueryTextListener(this);
//        item.setActionView(sv);
        
        
        inflater.inflate(R.menu.main_actions, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_action_search);
        SearchView mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);

	}

	public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        filter = !TextUtils.isEmpty(newText) ? newText : null;
        //if(filter != null && filter.length() >= 1)
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    public boolean onQueryTextSubmit(String query) {
        // Don't care about this.
        return true;
    }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_action_search) {
			
		}
		else if(item.getItemId() == R.id.menu_action_refresh) {
			DownloadService.defaultStartService(this.getActivity()
					.getApplicationContext());	
//			mShowProgressBar = true;
//			getActivity().setProgressBarIndeterminateVisibility(true);
		}
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
//			getActivity().getApplicationContext()
//				.getSharedPreferences(Config.SPREF, Context.MODE_PRIVATE);
		
		SharedPreferences.Editor editor = settings.edit();
		if (settings.getBoolean(Config.SHOW_START_DIALOG, true)) {
			showDialog();
			editor.putBoolean(Config.SHOW_START_DIALOG, false);
		}
		//editor.putBoolean(Config.CAN_UPDATE_LIST, true);
		editor.commit();
//		mShowStats = settings.getBoolean(
//				getString(R.string.pref_key_show_cache), true);
		mShowStats = settings.getBoolean("pref_key_show_cache", true);
		if(mStatsView != null) {
			if(!mShowStats && mStatsView.getVisibility() == View.VISIBLE) {
				mStatsView.setVisibility(View.GONE);
			}
			else if(mShowStats){
				mStatsView.setVisibility(View.VISIBLE);
			}
			
		}
	}
	
	void cacheInit() {
		/*
		 * Cache init
		 */
		final ActivityManager am = (ActivityManager) getActivity()
				.getSystemService(Context.ACTIVITY_SERVICE);
		final int memoryClassBytes = am.getMemoryClass() * 1024 * 1024;
		mCache = new CoverCache(memoryClassBytes / 2);

	}

	void showDialog() {
		// DialogFragment.show() will take care of adding the fragment
		// in a transaction. We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		DialogFragment newFragment = StartDialog.newInstance(0);
		newFragment.show(ft, "dialog");
	}

	/**
	 * Update UI that shows cache statistics.
	 */
	private void updateCacheStatsUi() {
		if (mShowStats) {
			Activity a = getActivity();
			if (a != null) {
				a.runOnUiThread(new Runnable() {

					public void run() {
						mStatsSize.setText(Formatter.formatFileSize(
								getActivity(), mCache.size()));
						mStatsHits.setText(Integer.toString(mCache.hitCount()));
						mStatsMisses.setText(Integer.toString(mCache
								.missCount()));
						mStatsEvictions.setText(Integer.toString(mCache
								.evictionCount()));
					}
				});
			}
		}
	}

	private class ThumbAsyncTask extends ThumbnailAsyncTask {

		private final ImageView mTarget;

		public ThumbAsyncTask(ImageView target) {
			mTarget = target;
		}

		@Override
		protected void onPreExecute() {
			mTarget.setTag(this);
		}

		@Override
		protected Bitmap doInBackground(Chapter... params) {
			final Chapter c = params[0];
			// try cache first
			final Bitmap cachedResult = mCache.get(c.chapterId);
			if (cachedResult != null) {
				updateCacheStatsUi();
				return cachedResult;
			}

			// if nothing found in cache, try looking for saved files on the
			// device
			if (c.localResourceUri == null) {
				// no saved file on the device, download file
				// try {
				// Thread.sleep(0);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				c.localResourceUri = BitmapUtils
						.localPathFromChapterInformation(c.parent.bookName,
								c.chapterName, c.remoteResourceUri);
				Activity a = getActivity();
				if (a != null)
					BitmapUtils.downloadImage(a.getApplicationContext(),
							c.remoteResourceUri, c.localResourceUri);
			}
			/*
			 * file is on device (downloaded or was already there) use normal
			 * file loading to retrieve the bitmap
			 */
			if(Config.DEBUG) {
				Log.d(TAG, "retrieving bitmap " + c.localResourceUri);
			}
			// is = (InputStream) new URL(c.getCoverBitmapPath()).getContent();
			String fileName = c.localResourceUri;
			Bitmap bmp = null;
			if (fileName != null && fileName.length() > 0) {
				bmp = BitmapUtils.decodeAndDownscaleBitmap(fileName,
						mTarget.getWidth(), mTarget.getHeight());
			}
			/*
			 * Add bitmap to cache
			 */
			if (bmp != null)
				mCache.put(c.chapterId, bmp);
			return bmp;

		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				/*
				 * Animate the loading of the bitmap into the view
				 */
				Animation fadeIn;
				fadeIn = new AlphaAnimation(0, 1);
				fadeIn.setDuration(1000);
				if (mTarget.getTag() == this) {
					mTarget.setImageBitmap(result);
					mTarget.setTag(null);
					updateCacheStatsUi();
					mTarget.setAnimation(fadeIn);
				}
			}
		}
	}

	public class CoverAdapter extends CursorAdapter {
		ArrayList<Chapter> covers;
		LayoutInflater gridInflater;

		public CoverAdapter(Context c, LayoutInflater infl) {
			super(c, null, false);
			covers = new ArrayList<Chapter>();
			this.gridInflater = infl;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			Chapter c = Chapter.getChapterFromCursor(cursor);

			ImageView imageView = (ImageView) view
					.findViewById(R.id.cover_item_image);
			TextView tv = (TextView) view
					.findViewById(R.id.cover_item_tv_title);
			TextView providerTv = (TextView) view
					.findViewById(R.id.cover_item_tv_provider);

			final ThumbAsyncTask oldTask = (ThumbAsyncTask) imageView.getTag();
			if (oldTask != null) {
				oldTask.cancel(false);
			}
			final ThumbAsyncTask task = new ThumbAsyncTask(imageView);
			imageView.setImageBitmap(null);
			imageView.setTag(task);
			task.execute(c);
			tv.setText(c.chapterName);
			providerTv.setText(c.parent.bookName);

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return gridInflater.inflate(R.layout.cover_item, parent, false);
		}

		@Override
		public Cursor swapCursor(Cursor newCursor) {
			// TODO Auto-generated method stub
			return super.swapCursor(newCursor);
		}

		public void clear() {
			covers.clear();
		}

	}

	/**
	 * Simple extension that uses {@link Bitmap} instances as keys, using their
	 * memory footprint in bytes for sizing.
	 */
	public static class CoverCache extends LruCache<Long, Bitmap> {
		public CoverCache(int maxSizeBytes) {
			super(maxSizeBytes);
		}

		@Override
		protected int sizeOf(Long key, Bitmap value) {
			return value.getByteCount();
		}
	}

	
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// return new CoverLoader(this.getActivity(), store);
		selectionArgs = new String[]{filter};
		return new CursorLoader(this.getActivity(),
				Uri.parse(contentProviderUri
						+ AbstractCoversProvider.TABLE_LIST),
				new String[] { "col1" }, selection, selectionArgs, null);
	}

	public void onLoadFinished(Loader<android.database.Cursor> loader,
			android.database.Cursor cursor) {
		if (Config.DEBUG) {
			Log.d(TAG, "Loader finished");
		}
		adapter.swapCursor(cursor);
//		mShowProgressBar = false;
//		getActivity().setProgressBarIndeterminateVisibility(false);
	}

	public void onLoaderReset(Loader<android.database.Cursor> arg0) {
		if (Config.DEBUG) {
			Log.d(TAG, "Loader reset");
		}
		adapter.clear();
	};

	public void restartLoader() {
		if (Config.DEBUG) {
			Log.d(TAG, "Loader restarted");
		}
		adapter.clear();
		getActivity().getSupportLoaderManager().restartLoader(loaderCursor,
				null, this);
	}
}
