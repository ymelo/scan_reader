package com.ymelo.readit;

public class Config {
	public static final boolean DEBUG = false;
	/**
	 * This field is updated when the sharedpreferences are changed
	 * You can rely on it for testing purpose, but this should be removed before releasing
	 * the first stable version
	 */
	public static String SERVER_URL = "";
	public static String SHARED_DIR_NAME = "readit";
	
	public static final String CONTENT_PROVIDER_URI = "content_provider_uri";
	public static final String PAGE_KEY = "page";
	public static final String URL_KEY = "url";
	public static final String CHAPTER_ID_KEY = "chapter_id";
	public static final String BOOK_ID_KEY = "book_id";
	//public static final String SPREF = "com.ymelo.readit.sharedpreferences.config";
	/**
	 * the minimum time before the service should run an update on the list of chapters shown
	 */
	public static final String CAN_UPDATE_LIST = "CAN_UPDATE_LIST";
	public static final int DEFAULT_LOAD_IMAGES = 2;
	public static final boolean WARNING = true;
	public static final String SHOW_START_DIALOG = "show_start_dialog";
	public static final String SHOW_PROGRESS = "show_progress";
	
}
