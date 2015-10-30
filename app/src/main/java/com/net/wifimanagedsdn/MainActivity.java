package com.net.wifimanagedsdn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.android.gcm.GCMRegistrar;
import com.net.wifimanagedsdn.sqlite.DatabaseHandler;
import com.net.wifimanagedsdn.util.Constan;
import com.net.wifimanagedsdn.util.ReadFileConfig;
import com.net.wifimanagedsdn.util.share;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

@SuppressLint({ "DefaultLocale", "CommitPrefEdits" })
public class MainActivity extends Activity {	
	
	protected static final String TAG = "MainActivity";
	static WifiManager wifi;
	static Network nwNetwork;
	static Context context;
	public static DatabaseHandler db;	
	Button btUserInCon;
	Button btComCon;
	String STR_PREFS_NAME = "SDN_LOADBALANCING";
	String STR_PUT_VALUE = "runprocess";
	SharedPreferences settings;
	Switch swRunProcess;
	Intent iSdn_service = null;
	public boolean bRunProcess;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_app);
		
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		db = new DatabaseHandler(this);		
		nwNetwork = new Network(wifi, this);
		context = this;
		settings = getSharedPreferences(share.STR_PREFS_MAIN, 0);
		
		btUserInCon = (Button)findViewById(R.id.btUserInCon);
		btComCon = (Button)findViewById(R.id.btComCon);
		btUserInCon.setOnClickListener(userinforListener);
		btComCon.setOnClickListener(communicateListener);
		swRunProcess = (Switch)findViewById(R.id.swRunProcess);
		swRunProcess.setOnCheckedChangeListener(processListener);		
		
		bRunProcess = settings.getBoolean(share.STR_RUN_PROCESS, true);
		swRunProcess.setChecked(bRunProcess);
		//copy raw resource to sdcard
		CopyResourceToFlash();		
		Log.d(TAG, "wifi ip: " + nwNetwork.getWifiIp());
		
		//register device to gcm
		/*ReadFileConfig config = new ReadFileConfig();
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		Log.d(TAG, "check internet: " + nwNetwork.isInternet());*/
		
		//set state wifi
		boolean bFirstTime = settings.getBoolean(share.STR_FIRST_TIME_MAIN, true);
		if(bFirstTime) {
			wifi.setWifiEnabled(false);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		GCMRegistrar.register(this, config.getSenderId());
		
		/*if(nwNetwork.isInternet()) {
			Log.d(TAG, "register gcm device--------->");
			if(!GCMRegistrar.isRegistered(this)) {
				GCMRegistrar.register(this, config.getSenderId());
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Log.d(TAG, "register gcm device: " + GCMRegistrar.getRegistrationId(context));
		}*/
		
		
//		//enable 3G
//		if(!nwNetwork.isMobileConnected())
//			nwNetwork.EnableMobileData();		

		if(db.getUser() == null)
			Toast.makeText(this, "Please config user information", Toast.LENGTH_LONG).show();
		if(db.getTime() == null)
			Toast.makeText(this, "Please config communication time", Toast.LENGTH_LONG).show();		
		
		
		//Start sdn service
		if(bRunProcess) {
			iSdn_service = new Intent(this, Sdn_Service.class);
			startService(iSdn_service);
			Log.d(TAG, "Enable process load balancing in android");
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}
	
		
	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");		
	}
	
	public void onExitAction(View botton) {
		Log.d(TAG, "onExitAction");
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");		
		super.onDestroy();
		db.close();
		if(iSdn_service != null) {
			stopService(iSdn_service);
			iSdn_service = null;
		}
	}	
		
	public OnClickListener userinforListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(bRunProcess) {
//				try {
//					WifiEnableService.iDemManual = 0;
//					WifiEnableService.bScan = false;
//					Sdn_Service.strGBestAP = null;
//					Sdn_Service.CloseConnect();
//					Sdn_Service.bRun = false;
//					Sdn_Service.bttReqManagedApList = false;
//					Sdn_Service.bttRepScanAp = false;
//					Thread.sleep(1000);
//					if(Sdn_Service.tiRepScanAp != null) {
//						Sdn_Service.tiRepScanAp.cancel();
//						Sdn_Service.tiRepScanAp.purge();
//					}
//					if(Sdn_Service.tiReqManagedAPList != null) {
//						Sdn_Service.tiReqManagedAPList.cancel();
//						Sdn_Service.tiReqManagedAPList.purge();
//					}
//					if(Sdn_Service.thWifiManaged != null)
//						Sdn_Service.thWifiManaged.join();
//					
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
			Intent intent = new Intent(MainActivity.this, UserInformationConfig.class);
//			intent.putExtra("key", value); //Optional parameters
			startActivityForResult(intent, Constan.INTENT_USER_INFORMATION_CONFIG_START);
		}
	};
	
	public OnClickListener communicateListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(bRunProcess) {
//				try {
//					WifiEnableService.iDemManual = 0;
//					WifiEnableService.bScan = false;
//					Sdn_Service.strGBestAP = null;
//					Sdn_Service.CloseConnect();
//					Sdn_Service.bRun = false;
//					Sdn_Service.bttReqManagedApList = false;
//					Sdn_Service.bttRepScanAp = false;
//					Thread.sleep(1000);
//					if(Sdn_Service.tiRepScanAp != null) {
//						Sdn_Service.tiRepScanAp.cancel();
//						Sdn_Service.tiRepScanAp.purge();
//					}
//					if(Sdn_Service.tiReqManagedAPList != null) {
//						Sdn_Service.tiReqManagedAPList.cancel();
//						Sdn_Service.tiReqManagedAPList.purge();
//					}
//					if(Sdn_Service.thWifiManaged != null)
//						Sdn_Service.thWifiManaged.join();
//					
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
			
			Intent intent = new Intent(MainActivity.this, CommunicationConfig.class);
//			intent.putExtra("key", value); //Optional parameters
			startActivityForResult(intent, Constan.INTENT_COMMUNICATION_CONFIG_START);
			
		}
	};
	
	public OnCheckedChangeListener processListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			bRunProcess = isChecked;
			SharedPreferences.Editor eRunProcess = settings.edit();
			eRunProcess.putBoolean(share.STR_RUN_PROCESS, isChecked);
			eRunProcess.commit();
			SharedPreferences.Editor eFirstTime = settings.edit();
			eFirstTime.putBoolean(share.STR_FIRST_TIME_MAIN, !isChecked);
			eFirstTime.commit();
			
			if(isChecked) {
				Log.d(TAG, "Enable process load balancing in android");
				if(iSdn_service == null) {
					iSdn_service = new Intent(MainActivity.this, Sdn_Service.class);
					startService(iSdn_service);
				}
			} else {
				Log.d(TAG, " Disable process load balancing in android");
				WifiInfo info = wifi.getConnectionInfo();
				if(info != null)
					wifi.removeNetwork(info.getNetworkId());					
				if(iSdn_service != null) {
					stopService(iSdn_service);
					iSdn_service = null;
				}
			}
		}
	};
	
	public void CopyResourceToFlash() {
		Log.d(TAG, "CopyResourceToFlash start");
		String path = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.net.wifimanagedsdn";
		File dir = new File(path);
		File tmp;
		if (dir.mkdirs() || dir.isDirectory()) {
			try {
				tmp = new File(path + "/gcm");
				if(!tmp.exists()) {
					Log.d(TAG, "CopyResourceToFlash gcm start");
					CopyRAWtoSDCard(context, R.raw.gcm, path + "/gcm");
					Log.d(TAG, "CopyResourceToFlash gcm stop");
				}	
				tmp = new File(path + "/ip_config");
				if(!tmp.exists())
					CopyRAWtoSDCard(context, R.raw.ip_config, path + "/ip_config");
				tmp = new File(path + "/wifi_list");
				if(!tmp.exists())
					CopyRAWtoSDCard(context, R.raw.wifi_list, path + "/wifi_list");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		Log.d(TAG, "CopyResourceToFlash end");
	}
	
	public void CopyRAWtoSDCard(Context context, int id, String path) throws IOException {
		Log.d(TAG, "CopyRAWtoSDCard start");
		InputStream in = getResources().openRawResource(id);
		FileOutputStream out = new FileOutputStream(path);
		byte[] buff = new byte[1024];
		int read = 0;
		try {
			while ((read = in.read(buff)) > 0) {
				out.write(buff, 0, read);
			}
			in.close();
			in = null;
			out.close();
			out.flush();
			out = null;
		} catch (Exception e) {
			e.printStackTrace();
			in.close();
			out.close();
		}
		Log.d(TAG, "CopyRAWtoSDCard end");
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub		
		Log.d(TAG, "requestCode: " + requestCode);
		Log.d(TAG, "resultCode: " + resultCode);
		// TODO Auto-generated method stub
		if(((resultCode == Constan.INTENT_USER_INFORMATION_CONFIG_OK) || (resultCode == Constan.INTENT_COMMUNICATION_CONFIG_OK)) && (bRunProcess)) {
			try {
				WifiEnableService.iDemManual = 0;
				WifiEnableService.bScan = false;
				Sdn_Service.strGBestAP = null;
				Sdn_Service.CloseConnect();
				Sdn_Service.bRun = false;
				Sdn_Service.bttReqManagedApList = false;
				Sdn_Service.bttRepScanAp = false;
				Thread.sleep(1000);
				if(Sdn_Service.tiRepScanAp != null) {
					Sdn_Service.tiRepScanAp.cancel();
					Sdn_Service.tiRepScanAp.purge();
				}
				if(Sdn_Service.tiReqManagedAPList != null) {
					Sdn_Service.tiReqManagedAPList.cancel();
					Sdn_Service.tiReqManagedAPList.purge();
				}
				if(Sdn_Service.thWifiManaged != null)
					Sdn_Service.thWifiManaged.join();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if((db.getUser() != null) && (db.getTime() != null) && (wifi.isWifiEnabled())) {
				Log.d(TAG, "========================onActivityResult====================================>");
				try {				
					Thread.sleep(2000);
					Sdn_Service.iIdBestAp = -1;
					Sdn_Service.strGBestAP = null;
					Sdn_Service.iInitconn = 0;
					Sdn_Service.brnWifiManaged = true;
					Sdn_Service.bttReqManagedApList = true;
					Sdn_Service.bttRepScanAp = true;
					Sdn_Service.bRun = true;
					Sdn_Service.thWifiManaged = new Thread(Sdn_Service.rnWifiManaged);
					Sdn_Service.thWifiManaged.start();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void disableWifiBroadcastReceiver() {
		ComponentName receiver = new ComponentName(this, WifiEnableService.class);
		PackageManager pm = this.getPackageManager();

		pm.setComponentEnabledSetting(receiver,
		PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		PackageManager.DONT_KILL_APP);
		Toast.makeText(this, "Disabled broadcst receiver", Toast.LENGTH_SHORT).show();
	}
	
	public void disableGCMBroadcastReceiver() {
		ComponentName receiver = new ComponentName(this, GCMBroadcastReceiver.class);
		PackageManager pm = this.getPackageManager();

		pm.setComponentEnabledSetting(receiver,
		PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		PackageManager.DONT_KILL_APP);
	}
}
