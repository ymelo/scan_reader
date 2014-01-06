package com.ymelo.readit.gui;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.ymelo.readit.Config;
import com.ymelo.readit.R;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		Config.SERVER_URL = sharedPref.getString(getResources().getString(R.string.pref_key_server), getResources().getString(R.string.pref_default_server));
		setContentView(R.layout.activity_main);
		// Create the adapter that will return a fragment for each of the three
		// primary sections
		// of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the 
		// corresponding tab.
		// We can also use ActionBar.Tab#select() to do this if we have a
		// reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter.
			// Also specify this Activity object, which implements the
			// TabListener interface, as the
			// listener for when this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_action_refresh) {
			mSectionsPagerAdapter.getFragmentAt(mViewPager.getCurrentItem()).restartLoader();
		} else {
			Intent intent = new Intent(this, SettingActivity.class);
			startActivity(intent);
		}
		return true;
	}

	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the primary sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private static final String TAG = "TAG";

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		private ListCoversFragment getFragmentAt(int position) {
			FragmentManager fm = MainActivity.this
					.getSupportFragmentManager();
			ListCoversFragment frg = (ListCoversFragment) fm.findFragmentByTag(TAG + position);
			return frg;
		}
		@Override
		public Fragment getItem(int i) {
			Bundle args = new Bundle();
			args.putString("TAG", TAG + i);
			Fragment fragment = getFragmentAt(i);
			if(i == 0) {
				//fragment = new DummySectionFragment();
				
				fragment = new ListCoversRaspberryFragment();
				args.putString(Config.CONTENT_PROVIDER_URI, "content://com.ymelo.readit.raspberry/");
			}
			else if(i == 1){
				fragment = new ListCoversLocalFragment();
				args.putString(Config.CONTENT_PROVIDER_URI, "content://com.ymelo.readit.local/");
			}
			else {
				fragment = new ListCoversRaspberryFragment();
				args.putString(Config.CONTENT_PROVIDER_URI, "content://com.ymelo.readit.raspberry/");
			}
			
			fragment.setArguments(args);
			return fragment;

		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.title_section1);
			case 1:
				return getString(R.string.title_section2);
			case 2:
				return getString(R.string.title_section3);
			}
			return null;
		}
	}

}
