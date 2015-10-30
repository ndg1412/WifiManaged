package com.net.wifimanagedsdn.sqlite;

public class Time {
	//private variables
		int _id;
		int _scan;
		int _list;

		// Empty constructor
		public Time() {

		}
		// constructor
		public Time(int id, int scan, int list) {
			this._id = id;
			this._scan = scan;
			this._list = list;
		}

		// constructor
		public Time(int scan, int list) {
			this._scan = scan;
			this._list = list;
		}
		
		// getting ID
		public int getId() {
			return this._id;
		}

		// setting id
		public void setId(int id) {
			this._scan = id;
		}

		// getting Scan time
		public int getScanTime() {
			return this._scan;
		}

		// setting scan time
		public void setScanTime(int time) {
			this._scan = time;
		}

		// getting update time
		public int getUpdateTime() {
			return this._list;
		}

		// setting update time
		public void setUpdateTime(int time) {
			this._list = time;
		}
}
