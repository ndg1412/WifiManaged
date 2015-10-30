package com.net.wifimanagedsdn.sqlite;

public class Wifi {
	//private variables
	int _id;
	String _ssid;
	String _mac;

	// Empty constructor
	public Wifi() {

	}
	// constructor
	public Wifi(int id, String ssid, String mac) {
		this._id = id;
		this._ssid = ssid;
		this._mac = mac;
	}

	// constructor
	public Wifi(String ssid, String mac) {
		this._ssid = ssid;
		this._mac = mac;
	}
	
	// getting ID
	public int getID() {
		return this._id;
	}

	// setting id
	public void setID(int id) {
		this._id = id;
	}

	// getting ssid
	public String getSSID() {
		return this._ssid;
	}

	// setting ssid
	public void setSSID(String ssid) {
		this._ssid = ssid;
	}

	// getting pwd
	public String getMac() {
		return this._mac;
	}

	// setting pwd
	public void setMac(String mac) {
		this._mac = mac;
	}
}
