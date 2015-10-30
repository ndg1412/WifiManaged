package com.net.wifimanagedsdn.sqlite;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 3;

	// Database Name
	public static final String DATABASE_NAME = "wifi_list_db";

	// wifi table name
	private static final String TABLE_WIFI = "wifi";
	
	// user table name
	private static final String TABLE_USER = "user";
	
	// time table name
	private static final String TABLE_TIME = "time";
	
	// version table name
	private static final String TABLE_VERSION = "version";

	// wifi Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_SSID = "ssid";
	private static final String KEY_MAC = "mac";
	
	// user Table Columns names
	private static final String USER_KEY_ID = "id";
	private static final String USER_KEY_PHONE = "phone";
	private static final String USER_KEY_USER = "user";
	private static final String USER_KEY_PASS = "pass";
	
	// time Table Columns names
	private static final String TIME_KEY_ID = "id";
	private static final String TIME_KEY_SCAN = "scan_time";
	private static final String TIME_KEY_LIST = "list_time";
	
	// version Table Columns names
	private static final String VERSION_KEY_ID = "id";
	private static final String VERSION_KEY_VER = "version_managed";

	private static final String TAG = "DatabaseHandler";
	
	String CREATE_Wifi_TABLE = "CREATE TABLE " + TABLE_WIFI + "("
			+ KEY_ID + " INTEGER PRIMARY KEY, " + KEY_SSID + " TEXT, "
			+ KEY_MAC + " TEXT" + ")";
	
	String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
			+ USER_KEY_ID + " INTEGER PRIMARY KEY, " + USER_KEY_PHONE + " TEXT, "
			+ USER_KEY_USER + " TEXT, "
			+ USER_KEY_PASS + " TEXT" + ")";
	
	String CREATE_TIME_TABLE = "CREATE TABLE " + TABLE_TIME + "("
			+ TIME_KEY_ID + " INTEGER PRIMARY KEY, " + TIME_KEY_SCAN + " INTEGER, "
			+ TIME_KEY_LIST + " INTEGER" + ")";
	
	String CREATE_VERSION_TABLE = "CREATE TABLE " + TABLE_VERSION + "("
			+ VERSION_KEY_ID + " INTEGER PRIMARY KEY, " + VERSION_KEY_VER + " TEXT" + ")";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {		
		db.execSQL(CREATE_Wifi_TABLE);
		db.execSQL(CREATE_USER_TABLE);
		db.execSQL(CREATE_TIME_TABLE);
		db.execSQL(CREATE_VERSION_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_WIFI);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIME);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_VERSION);

		// Create tables again
		onCreate(db);
	}
	
	// Adding new wifi
	public int addWifi(Wifi wifi) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_SSID, wifi.getSSID());
		values.put(KEY_MAC, wifi.getMac());

		// Inserting Row
		int row = (int)db.insert(TABLE_WIFI, null, values);
		db.close(); // Closing database connection
		return row;
	}
	
	// Getting single wifi
	public Wifi getWifi(String mac) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_WIFI, new String[] { KEY_ID,
			KEY_SSID, KEY_MAC }, KEY_MAC + "=?",
			new String[] { mac }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		Wifi wifi;
		try {
			wifi = new Wifi(Integer.valueOf(cursor.getString(0)), 
					cursor.getString(1), cursor.getString(2));
		} catch (Exception e) {
			return null;
		}
		// return tag
		return wifi;
	}
	
	// Getting single wifi
	public Wifi getWifi(String ssid, String mac) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_WIFI, new String[] { KEY_ID,
			KEY_SSID, KEY_MAC }, KEY_SSID + "=?" + " and " +  KEY_MAC + "=?",
			new String[] { ssid, mac }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		Wifi wifi;
		try {
			wifi = new Wifi(Integer.valueOf(cursor.getString(0)), 
					cursor.getString(1), cursor.getString(2));
		} catch (Exception e) {
			return null;
		}
		// return tag
		return wifi;
	}
	
	// Getting All wifi
	public List<Wifi> getAllWifi() {
		List<Wifi> wifiList = new ArrayList<Wifi>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_WIFI;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Wifi wifi = new Wifi();
				wifi.setID(Integer.parseInt(cursor.getString(0)));
				Log.d(TAG, "ssid: " + cursor.getString(1));
				wifi.setSSID(cursor.getString(1));
				wifi.setMac(cursor.getString(2));
				Log.d(TAG, "ssid: " + cursor.getString(2));
				
				// Adding wifi to list
				wifiList.add(wifi);
			} while (cursor.moveToNext());
		}

		// return wifi list
		return wifiList;
	}
	
	// Getting wifi Count
	public int getWifiCount() {
		String countQuery = "SELECT  * FROM " + TABLE_WIFI;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		// return count
		return cursor.getCount();
	}
	
	// Updating single wifi
	public boolean updateWifi(int id, String ssid, String pass) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues args = new ContentValues();
		args.put(KEY_SSID, ssid);
		args.put(KEY_MAC, pass);
		return db.update(TABLE_WIFI, args, KEY_ID + "=" + id, null) > 0;
	}
	
	public int deleteWifi(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_WIFI, KEY_ID + " = ?",
				new String[] { String.valueOf(id) });
	}
	
	public int deleteAll() {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_WIFI, null, null);
	}
	
	// Adding new user
	public int addUser(User user) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(USER_KEY_PHONE, user.getPhone());
		values.put(USER_KEY_USER, user.getUser());
		values.put(USER_KEY_PASS, user.getPass());

		// Inserting Row
		 int row = (int)db.insert(TABLE_USER, null, values);
		db.close(); // Closing database connection
		return row;
	}
	
	// Getting single user
	public User getUser() {
		SQLiteDatabase db = this.getReadableDatabase();

		 String selectQuery = "SELECT  * FROM " + TABLE_USER + " WHERE "
				 + USER_KEY_ID + " = " + 1;
		 Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor != null)
			cursor.moveToFirst();
		User user;
		try {
			user = new User(Integer.valueOf(cursor.getString(0)), 
					cursor.getString(1), cursor.getString(2), cursor.getString(3));
		} catch (Exception e) {
			return null;
		}
		// return tag
		return user;
	}
	
	// Updating single wifi
	public boolean updateUser(String phone, String user, String pass) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues args = new ContentValues();
		args.put(USER_KEY_PHONE, phone);
		args.put(USER_KEY_USER, user);
		args.put(USER_KEY_PASS, pass);
		return db.update(TABLE_USER, args, USER_KEY_ID + "=" + 1, null) > 0;
	}
	
	public int deleteUser() {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_USER, USER_KEY_ID + " = ?",
				new String[] { String.valueOf(1) });
	}
	
	// Adding new time
	public int addTime(Time time) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(TIME_KEY_SCAN, time.getScanTime());
		values.put(TIME_KEY_LIST, time.getUpdateTime());

		// Inserting Row
		 int row = (int)db.insert(TABLE_TIME, null, values);
		db.close(); // Closing database connection
		return row;
	}
	
	// Getting single time
	public Time getTime() {
		SQLiteDatabase db = this.getReadableDatabase();

		 String selectQuery = "SELECT  * FROM " + TABLE_TIME + " WHERE "
				 + TIME_KEY_ID + " = " + 1;
		 Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor != null)
			cursor.moveToFirst();
		Time time;
		try {
			time = new Time(Integer.valueOf(cursor.getString(0)), 
					Integer.valueOf(cursor.getString(1)),
					Integer.valueOf(cursor.getString(2)));
		} catch (Exception e) {
			return null;
		}
		// return time
		return time;
	}
	
	// Updating single time
	public boolean updateTime(int time_scan, int time_list) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues args = new ContentValues();
		args.put(TIME_KEY_SCAN, time_scan);
		args.put(TIME_KEY_LIST, time_list);
		return db.update(TABLE_TIME, args, TIME_KEY_ID + "=" + 1, null) > 0;
	}
	
	public int deleteTime() {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_TIME, TIME_KEY_ID + " = ?",
				new String[] { String.valueOf(1) });
	}
	
	// Adding new version
	public int addVersionManaged(Version ver) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(VERSION_KEY_VER, ver.getVersion());

		// Inserting Row
		 int row = (int)db.insert(TABLE_VERSION, null, values);
		db.close(); // Closing database connection
		return row;
	}
	
	// Getting single version
	public Version getVersionManaged() {
		SQLiteDatabase db = this.getReadableDatabase();

		 String selectQuery = "SELECT  * FROM " + TABLE_VERSION + " WHERE "
				 + VERSION_KEY_ID + " = " + 1;
		 Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor != null)
			cursor.moveToFirst();
		Version ver;
		try {
			ver = new Version(Integer.valueOf(cursor.getString(0)), 
					Long.valueOf(cursor.getString(1)));
		} catch (Exception e) {
			return null;
		}
		// return time
		return ver;
	}
	
	// Updating single time
	public boolean updateVersionManaged(long ver) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues args = new ContentValues();
		args.put(VERSION_KEY_VER, ver);
		return db.update(TABLE_VERSION, args, VERSION_KEY_ID + "=" + 1, null) > 0;
	}
	
	public int deleteVersionManaged() {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_VERSION, VERSION_KEY_ID + " = ?",
				new String[] { String.valueOf(1) });
	}
}
