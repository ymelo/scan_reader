package com.ymelo.readit.providers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.ymelo.readit.Config;
import com.ymelo.readit.Cover;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class CoversProvider extends ContentProvider{
	@Override
	public boolean onCreate() {
		
		return true;
	}
	
	private ArrayList<Cover> readStream(InputStream in) {
		ArrayList<Cover> covers = new ArrayList<Cover>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				String[] s = line.split(";");
				//covers.add(new Cover(s[0], Config.SERVER_URL, s[1]));
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
		return covers;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		MatrixCursor matrix = null;
		try {
			URL url = new URL(
					Config.SERVER_URL + "/cover.php?number_of_pages=15");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			ArrayList<Cover> covers = readStream(con.getInputStream());
			
			
			String[] names = {Cover.COL_TITLE, Cover.COL_PATH_TO_ROOT, "" /*Cover.COL_PROVIDER*/, Cover.COL_COVER_NAME};
			matrix = new MatrixCursor(names);

			matrix.addRow(covers.toArray(new Cover[0]));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return matrix;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
