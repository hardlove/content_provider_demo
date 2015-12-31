package com.example.contenproviderdemo;

import android.net.Uri;

public class CitizenTable {

	public static final String TABLE_NAME = "citizen_table";
	
	//ID COLUMN MUSU LOOK LIKE THIS
	public static final String ID = "_id";
	public static final String NAME = "name";
	public static final String STATE = "state";
	public static final String INCOME = "income";

	//the content uri to our provider
	public static final Uri CONTENT_URI = Uri.parse("conten://"+CitizenContentProvider.AUTHORITY + "/citizen");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jwi512.citizen";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.jwi512.citizen";
	public static final int SSID_PATH_POSITION = 1;
	

	/**
	 * DEFINE THE CONTENT TYPE AND URI
	 */
	//TO BE DISCUSSED LATER...
	
}
