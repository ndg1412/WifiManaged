package com.net.wifimanagedsdn.sqlite;

public class Version {
	//private variables
		int _id;
		long _version;

		// Empty constructor
		public Version() {

		}
		
		// constructor
		public Version(long ver) {
			this._version = ver;
		}
		
		// constructor
		public Version(int id, long ver) {
			this._id = id;
			this._version = ver;
		}
		
		// getting ID
		public long getVersion() {
			return this._version;
		}

		// setting id
		public void setVersion(int ver) {
			this._version = ver;
		}
}
