package com.net.wifimanagedsdn.sqlite;

public class User {
	//private variables
	int _id;
	String _phone;
	String _user;
	String _pass;

	// Empty constructor
	public User() {

	}
	// constructor
	public User(int id, String phone, String user, String pass) {
		this._id = id;
		this._phone = phone;
		this._user = user;
		this._pass = pass;
	}

	// constructor
	public User(String phone, String user, String pass) {
		this._phone = phone;
		this._user = user;
		this._pass = pass;
	}
	
	// getting ID
	public int getID() {
		return this._id;
	}

	// setting id
	public void setID(int id) {
		this._id = id;
	}

	// getting phone
	public String getPhone() {
		return this._phone;
	}

	// setting phone
	public void setPhone(String phone) {
		this._phone = phone;
	}

	// getting user
	public String getUser() {
		return this._user;
	}

	// setting user
	public void setUser(String user) {
		this._user = user;
	}
	
	// getting pass
	public String getPass() {
		return this._pass;
	}

	// setting pass
	public void setPass(String pass) {
		this._pass = pass;
	}
}
