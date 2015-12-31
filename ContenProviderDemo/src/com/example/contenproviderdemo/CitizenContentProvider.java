package com.example.contenproviderdemo;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class CitizenContentProvider extends ContentProvider {

	private static final String DATABASE_NAME = "citizens.db";
	private static final int DATABASE_VERSION = 1;
	public static final String AUTHORITY = "example.contenproviderdemo.CitizenContentProvider";
	
	
	
	

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {

			super(context, DATABASE_NAME, null, DATABASE_VERSION);

		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = "CREATE TABLE" + CitizenTable.TABLE_NAME
					+ "(" + CitizenTable.ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," 
					+ CitizenTable.NAME + " TEXT," 
					+ CitizenTable.STATE+ " TEXT," 
					+ CitizenTable.INCOME + " INTEGER)";

			db.execSQL(sql);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

			Log.w("CitizenContentProvider", "Upgrading database from version:" + oldVersion + "to" + newVersion
					+ ",which will destroy all old data.");
			db.execSQL("DROP TABLE IF EXISTS " + CitizenTable.TABLE_NAME);
			onCreate(db);
		}

	}

	private static final int CITIZENS = 1;
	private static final int SSID = 2;
	private DatabaseHelper dbHelper;
	
	private static final UriMatcher sUriMatcher;
	private static final HashMap<String,String> projectionMap;
	//INSTANTIATE AND SET STATIC VARIABLES
	static{
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "citizen", CITIZENS);
		sUriMatcher.addURI(AUTHORITY, "citizen/#", SSID);
		//PROJECTION MAP USED FOR ROW ALIAS
		projectionMap = new HashMap<String, String>();
		projectionMap.put(CitizenTable.ID, CitizenTable.ID);
		projectionMap.put(CitizenTable.INCOME, CitizenTable.INCOME);
		projectionMap.put(CitizenTable.NAME, CitizenTable.NAME);
		projectionMap.put(CitizenTable.STATE, CitizenTable.STATE);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(CitizenTable.TABLE_NAME);
		switch (sUriMatcher.match(uri)) {
		case CITIZENS:
			qb.setProjectionMap(projectionMap);
			break;
		case SSID:
			String ssid = uri.getPathSegments().get(CitizenTable.SSID_PATH_POSITION);
			qb.setProjectionMap(projectionMap);
			//for querying by specific ssid
			qb.appendWhere(CitizenTable.ID+"="+ssid);
			break;

		default:
			throw new IllegalArgumentException("Unknow URI "+uri);
		}
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		//registers notification listener with given cursor
		//cursor knows when underlying data has changed
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case CITIZENS:
			return CitizenTable.CONTENT_TYPE;
		case SSID:
			return CitizenTable.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalAccessError("Unkonw URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		//only general citizens uri is allowed for inserts
		//doesn't make sense to specify a single citizen
		if(sUriMatcher.match(uri)!= CITIZENS){
			throw new IllegalArgumentException("Unkonwn URI "+uri);
		}
		ContentValues values;
		if(initialValues != null){
			values = new ContentValues(initialValues);
		}else{
			values = new ContentValues();
		}
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long rowId = db.insert(CitizenTable.TABLE_NAME, CitizenTable.NAME, values);
		if(rowId>0){
			Uri citizenUri = ContentUris.withAppendedId(CitizenTable.CONTENT_URI, rowId);
			//notify context of the change
			getContext().getContentResolver().notifyChange(citizenUri, null);
			return citizenUri;
		}else {
			throw new SQLException("Failed to insert row into " + uri);
		}
		
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case CITIZENS:
			//perform regular delete
			count = db.delete(CitizenTable.TABLE_NAME, selection, selectionArgs);
			break;

		case SSID:
			//from incoming uri get ssid
			String ssid = uri.getPathSegments().get(CitizenTable.SSID_PATH_POSITION);
			//user wants to delete a specific citizen
			String finalWhere = CitizenTable.ID+"="+ssid;
			//if user specifies where filter then append
			if(selection!=null){
				finalWhere = finalWhere + " AND " + selection;
			}
			count = db.delete(CitizenTable.TABLE_NAME, finalWhere, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case CITIZENS:
			//general update on all citizens
			count = db.update(CitizenTable.TABLE_NAME, values, selection, selectionArgs);
			break;

		case SSID:
			String ssid = uri.getPathSegments().get(CitizenTable.SSID_PATH_POSITION);
			//the user wants to update a specific citizen
			String finalWhere = CitizenTable.ID + "=" + ssid;
			if(selection!=null){
				finalWhere = finalWhere + " AND " + selection;
			}
			//perform the update on the specific citizen
			count = db.update(CitizenTable.TABLE_NAME, values, finalWhere, selectionArgs);
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
