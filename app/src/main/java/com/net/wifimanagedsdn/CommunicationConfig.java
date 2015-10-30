package com.net.wifimanagedsdn;

import com.net.wifimanagedsdn.sqlite.DatabaseHandler;
import com.net.wifimanagedsdn.sqlite.Time;
import com.net.wifimanagedsdn.util.Constan;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CommunicationConfig extends Activity {
	private static final String TAG = "CommunicationConfig";
	
	EditText etScanApUp, etApListUp;
	Button btSet_Communication, btCancel_Communication;
	DatabaseHandler db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.communication_configuration);
		
		etScanApUp = (EditText)findViewById(R.id.etScanApUp);
		etApListUp = (EditText)findViewById(R.id.etApListUp);
		btSet_Communication = (Button)findViewById(R.id.btSet_Communication);
		btCancel_Communication = (Button)findViewById(R.id.btCancel_Communication);
		btSet_Communication.setOnClickListener(setListener);
		btCancel_Communication.setOnClickListener(cancelListener);
	
		db = new DatabaseHandler(this);
		Time time = db.getTime();
		if(time != null) {
			Log.d(TAG, "db.getTime().getScanTime(): " + time.getScanTime());
			Log.d(TAG, "db.getTime().getScanTime(): " + time.getUpdateTime());
			etScanApUp.setText(String.valueOf(time.getScanTime()));
			etApListUp.setText(String.valueOf(time.getUpdateTime()));
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
	
	public OnClickListener setListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			try {
				Time time = db.getTime();
				int iTimeListUp = Integer.valueOf(etApListUp.getText().toString());
				int iTimeScanAp = Integer.valueOf(etScanApUp.getText().toString());
				if(time != null) {
					Log.d(TAG, "db time id: " + time.getId());
					Log.d(TAG, "db time ap scan time: " + iTimeScanAp);
					Log.d(TAG, "db time update list: " + iTimeListUp);
					db.updateTime(iTimeScanAp, iTimeListUp);
				} else
					db.addTime(new Time(iTimeScanAp, iTimeListUp));
				Intent returnResult = new Intent();
				setResult(Constan.INTENT_COMMUNICATION_CONFIG_OK, returnResult);
				finish();
			} catch (Exception e) {
				Toast.makeText(CommunicationConfig.this, "Please input time is number value", Toast.LENGTH_LONG).show();
			}
						
		}
	};
	
	public OnClickListener cancelListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}
	};
}
