package com.net.wifimanagedsdn;


import com.google.android.gcm.GCMRegistrar;
import com.net.wifimanagedsdn.sqlite.DatabaseHandler;
import com.net.wifimanagedsdn.sqlite.User;
import com.net.wifimanagedsdn.util.Constan;
import com.net.wifimanagedsdn.util.share;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UserInformationConfig extends Activity {
	private static final String TAG = "UserInformationConfig";

	EditText etAppId, etMacAddr, etPhoneNum, etUserId, etPassword;
	Button btSet_User, btCancel_User, btResetToDefault, btBack_User;
	TextView tvUserId, tvTwoPointUser, tvPass, tvTwoPointPass;
	WifiManager wifi;
	static String strWifiMac;
	static String strPhoneNum;
	static String strUser;
	static String strPass;
	public boolean bFirstTime;
	SharedPreferences settings;
	DatabaseHandler db;
	@SuppressLint("DefaultLocale")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_information);
		
		etAppId = (EditText)findViewById(R.id.etAppId);
		etMacAddr = (EditText)findViewById(R.id.etMacAddr);
		etPhoneNum = (EditText)findViewById(R.id.etPhoneNum);
		etUserId = (EditText)findViewById(R.id.etUserId);
		etPassword = (EditText)findViewById(R.id.etPassword);
		tvUserId = (TextView)findViewById(R.id.tvUserId);
		tvTwoPointUser = (TextView)findViewById(R.id.tvTwoPointUser);
		tvPass = (TextView)findViewById(R.id.tvPass);
		tvTwoPointPass = (TextView)findViewById(R.id.tvTwoPointPass);
		btBack_User = (Button)findViewById(R.id.btBack_User);
		btBack_User.setOnClickListener(backListener);
		btSet_User = (Button)findViewById(R.id.btSet_User);
		btCancel_User = (Button)findViewById(R.id.btCancel_User);
		btResetToDefault = (Button)findViewById(R.id.btResetToDefault);
		btSet_User.setOnClickListener(setListener);
		btCancel_User.setOnClickListener(cancelListener);
		btResetToDefault.setOnClickListener(resetListener);
		settings = getSharedPreferences(share.STR_PREFS_USER, 0);
		bFirstTime = settings.getBoolean(share.STR_FIRST_TIME_USER, true);
		if(!bFirstTime) {
			etUserId.setVisibility(View.GONE);
			etPassword.setVisibility(View.GONE);
			tvUserId.setVisibility(View.GONE);
			tvTwoPointUser.setVisibility(View.GONE);
			tvPass.setVisibility(View.GONE);
			tvTwoPointPass.setVisibility(View.GONE);
			btBack_User.setVisibility(View.VISIBLE);
			btSet_User.setVisibility(View.GONE);
			btCancel_User.setVisibility(View.GONE);
		}
		
		
		etAppId.setEnabled(false);
		etMacAddr.setEnabled(false);
		etPhoneNum.setEnabled(false);
		
		
		
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInf = wifi.getConnectionInfo();
		strWifiMac = wifiInf.getMacAddress().toUpperCase();
		etMacAddr.setText(strWifiMac);
		if(GCMRegistrar.isRegistered(this)) {
			String strAppId = GCMRegistrar.getRegistrationId(this);
			if(strAppId != null)
				etAppId.setText(strAppId);
		}		
		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);		
		if(mTelephonyMgr.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
			Log.d(TAG, "no sim card in phone");
		} else {
			String imsi = mTelephonyMgr.getSubscriberId();
			Log.d(TAG, "SubscriberId: " + imsi);
			etPhoneNum.setText(imsi);
		}		
		
		db = new DatabaseHandler(this);
		User user = db.getUser();
		if(user != null) {
			etUserId.setText(user.getUser());
			etPassword.setText(user.getPass());
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "=========" + "onStart" + "================");		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "=========" + "onResume" + "================");		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "=========" + "onPause" + "================");		
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "=========" + "onDestroy" + "================");		
		super.onDestroy();
		db.close();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		View view = getCurrentFocus();
		boolean ret = super.dispatchTouchEvent(event);

		if (view instanceof EditText) {
			View w = getCurrentFocus();
			int scrcoords[] = new int[2];
			w.getLocationOnScreen(scrcoords);
			float x = event.getRawX() + w.getLeft() - scrcoords[0];
			float y = event.getRawY() + w.getTop() - scrcoords[1];

			if (event.getAction() == MotionEvent.ACTION_UP 
				&& (x < w.getLeft() || x >= w.getRight() 
				|| y < w.getTop() || y > w.getBottom()) ) { 
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
			}
		}
		return ret;
	}
	public OnClickListener resetListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			etAppId.setText("");
//			etAppId.setHint(getResources().getString(R.string.etAppId_text));
//			etMacAddr.setText(strWifiMac);
//			etPhoneNum.setText("");
//			etPhoneNum.setHint(getResources().getString(R.string.etPhoneNum_text));		
			etUserId.setText("");
			etUserId.setHint(getResources().getString(R.string.etUserId_text));
			etPassword.setText("");
			etPassword.setHint(getResources().getString(R.string.etPassword_text));			
		}
	};
	
	public OnClickListener setListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.d(TAG, "user name: " + "--------------" + etUserId.getText().toString() + "---------------------");
			Log.d(TAG, "password: " + "---------------" + etPassword.getText().toString() + "--------------");
			if(etUserId.getText().toString().equals("")) {
				Log.d(TAG, "username select");
				Toast.makeText(UserInformationConfig.this, "Please input User name. User name value is string ", Toast.LENGTH_SHORT).show();;
				return;
			}
			if(etPassword.getText().toString().equals("")) {
				Log.d(TAG, "password select");
				Toast.makeText(UserInformationConfig.this, "Please input Password. Password value is string ", Toast.LENGTH_SHORT).show();;
				return;
			}
			
			if(bFirstTime) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean(share.STR_FIRST_TIME_USER, false);
				editor.commit();			
			}
			//db
			User user = db.getUser();
			if(user != null) {
				Log.d(TAG, "user db: " + user.getUser());
				Log.d(TAG, "pass db: " + user.getPass());
				Log.d(TAG, "phone db: " + user.getPhone());
				db.updateUser(etPhoneNum.getText().toString(), etUserId.getText().toString(),
						etPassword.getText().toString());
			} else {
				db.addUser(new User(etPhoneNum.getText().toString(), etUserId.getText().toString(),
						etPassword.getText().toString()));
			}
			Intent returnResult = new Intent();
			setResult(Constan.INTENT_USER_INFORMATION_CONFIG_OK, returnResult);
			finish();			
		}
	};
	
	public OnClickListener cancelListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}
	};
	
	public OnClickListener backListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}
	};
}
