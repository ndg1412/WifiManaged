package com.net.wifimanagedsdn;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.net.wifimanagedsdn.protocol.Connection_Req;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class Test extends Activity {
	private static final String TAG = "Test";
	

	EditText etAppId, etMacAddr, etPhoneNum, etUserId, etPassword;
	Button btSet_User, btCancel_User, btResetToDefault;
	WifiManager wifi;
	static String strWifiMac;
	static String strPhoneNum;
	static String strUser;
	static String strPass;
	
	static Socket skClient = null;
	static BufferedReader brRecv = null;
	static DataOutputStream dosSend = null;
	
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
		
		btSet_User = (Button)findViewById(R.id.btSet_User);
		btCancel_User = (Button)findViewById(R.id.btCancel_User);
		btResetToDefault = (Button)findViewById(R.id.btResetToDefault);
		btSet_User.setOnClickListener(setListener);
		btCancel_User.setOnClickListener(cancelListener);
		btResetToDefault.setOnClickListener(resetListener);
		
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInf = wifi.getConnectionInfo();
		strWifiMac = wifiInf.getMacAddress().toUpperCase();
		etMacAddr.setText(strWifiMac);
		Thread tt = new Thread(test);
		tt.start();
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
			etAppId.setText("");
			etAppId.setHint(getResources().getString(R.string.etAppId_text));
			etMacAddr.setText(strWifiMac);
			etPhoneNum.setText("");
			etPhoneNum.setHint(getResources().getString(R.string.etPhoneNum_text));		
			etUserId.setText("");
			etUserId.setHint(getResources().getString(R.string.etUserId_text));
			etPassword.setText("");
			etPassword.setHint(getResources().getString(R.string.etPassword_text));
//			InitConnect("192.168.3.214");
		}
	};
	
	public OnClickListener setListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//db
//			DatabaseHandler db = MainActivity.getDatabaseHandler();
//			User user = db.getUser();
//			if(user != null) {
//				Log.d(TAG, "user db: " + user.getUser());
//				Log.d(TAG, "pass db: " + user.getPass());
//				Log.d(TAG, "phone db: " + user.getPhone());
//				db.updateUser(etPhoneNum.getText().toString(), etUserId.getText().toString(),
//						etPassword.getText().toString());
//			} else {
//				db.addUser(new User(etPhoneNum.getText().toString(), etUserId.getText().toString(),
//						etPassword.getText().toString()));
//			}			
//			finish();	
			Send_Connection_Req();
			
		}
	};
	
	public OnClickListener cancelListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}
	};
	
	public static void InitConnect() {
		try {
			Log.d(TAG, "InitConnect===============================: ");
			skClient = new Socket("113.172.146.116", 11000);
			Log.d(TAG, "socket: " + skClient);
			brRecv = new BufferedReader(new InputStreamReader(skClient.getInputStream()));
			Log.d(TAG, "brRecv: " + brRecv);
			dosSend = new DataOutputStream(skClient.getOutputStream());
			Log.d(TAG, "dosSend: " + dosSend);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(TAG, "exception"  + e.toString());
		}
	}
	
	public boolean Send_Connection_Req() {
		Connection_Req req = new Connection_Req(etPhoneNum.getText().toString(),
				etUserId.getText().toString(), etPassword.getText().toString(), "");
		try {
			Log.d(TAG, "req.getBytes() : "+ req.getBytes().toString());
			Log.d(TAG, "req.getBytes() leng: "+ req.getBytes().length);
			String tmp = "";
			for(int i = 0; i < req.getBytes().length; i++)
				tmp += String.format(" %02X", req.getBytes()[i] & 0xFF);
			Log.d(TAG, tmp);
			Log.d(TAG, "1111111111111111 ");
			dosSend.write(req.getBytes());
			Thread recvThread = new Thread(recv);
			recvThread.start();
//			Connection_Res res = new Connection_Res(strRes.getBytes());
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	public static Runnable test = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			InitConnect();
		}
	};
	
	public static Runnable recv = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d(TAG, "get response from server");
			String strRes = "";
			try {
				Log.d(TAG, "brRecv.ready(): " + brRecv.ready());
				strRes = brRecv.readLine();
				Log.d(TAG, strRes);
				String tmp_res = "";
				for(int i = 0; i < strRes.length(); i++)
					tmp_res += String.format(" %02X", strRes.getBytes()[i]);
				Log.d(TAG, tmp_res);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	};
}
