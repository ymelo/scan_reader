package com.ymelo.readit.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ymelo.readit.Config;
import com.ymelo.readit.R;

public class ListCoversActivity extends ActionBarActivity {

	String[] mPlanetTitles;
	DrawerLayout mDrawerLayout;
	ListView mDrawerList;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.fragment_main);
		/* navigation drawer */
		mPlanetTitles = getResources().getStringArray(R.array.drawer_list);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
     // Set the adapter for the list view
        
        mDrawerList.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
                R.layout.drawer_list_item, mPlanetTitles));
        // Set the list's click listener
       mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	}
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		Config.SERVER_URL = sharedPref.getString(getResources().getString(R.string.pref_key_server), getResources().getString(R.string.pref_default_server));
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_settings) {
			Intent intent = new Intent(this, SettingActivity.class);
			startActivity(intent);
			return true;
		}
		return false;
	}
	

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    public void onItemClick(AdapterView parent, View view, int position, long id) {
	        selectItem(position);
	    }
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		if(position == 0) {
		    // Create a new fragment and specify the planet to show based on position
		    Fragment fragment = new ListCoversRaspberryFragment();
		    // Insert the fragment by replacing any existing fragment
		    FragmentManager fragmentManager = getSupportFragmentManager();
		    fragmentManager.beginTransaction()
		                   .replace(R.id.content_frame, fragment)
		                   .commit();			
		}
		else {
			Toast.makeText(this, "Not available yet", Toast.LENGTH_LONG).show();
//		    // Create a new fragment and specify the planet to show based on position
//		    Fragment fragment = new ListCoversLocalFragment();
//		    // Insert the fragment by replacing any existing fragment
//		    FragmentManager fragmentManager = getSupportFragmentManager();
//		    fragmentManager.beginTransaction()
//		                   .replace(R.id.content_frame, fragment)
//		                   .commit();

		}

	    // Highlight the selected item, update the title, and close the drawer
	    mDrawerList.setItemChecked(position, true);
	    setTitle(mPlanetTitles[position]);
	    mDrawerLayout.closeDrawer(mDrawerList);
	}

	
	public void setTitle(CharSequence title) {
	    getActionBar().setTitle(title);
	}

}
