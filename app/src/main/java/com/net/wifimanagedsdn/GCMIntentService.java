package com.net.wifimanagedsdn;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";

	// Use your PROJECT ID from Google API into SENDER_ID
	//public static final String SENDER_ID = MainActivity.getSenserId();
	public String strRegId;

	public GCMIntentService() {
//		super(SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		
		Log.i(TAG, "onRegistered: registrationId=" + registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {

		Log.i(TAG, "onUnregistered: registrationId=" + registrationId);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onMessage(Context context, Intent data) {
		Log.d(TAG, "msg from server--------------------------->: " + data);
//		WifiManager wmWifi= (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
//		WifiInfo wifiInf = wmWifi.getConnectionInfo();
//		String mac = wifiInf.getMacAddress();
		Log.d(TAG, "strRegId: " + strRegId);
		strRegId = GCMRegistrar.getRegistrationId(context);
		Log.d(TAG, "strRegId: " + strRegId);
		// Message from PHP server
		String message="";
		try {
			message = data.getStringExtra("message");
		} catch (Exception e) {
			
		}		
		Log.d(TAG, "message = " + message);
		if((message != null) && message.equals(strRegId)) {
			try {
//				MainActivity.wifi.disconnect();
				Log.d(TAG, "Sdn_Service.bRun :" + Sdn_Service.bRun);
				Log.d(TAG, "Sdn_Service.brnWifiManaged :" + Sdn_Service.brnWifiManaged);
				if(!Sdn_Service.brnWifiManaged) {
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

					WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
					WifiInfo info = wifi.getConnectionInfo();
					Log.d(TAG, "info ssid: " + info.getSSID());
					wifi.disconnect();
					
					for(WifiConfiguration conf : wifi.getConfiguredNetworks()) {
						wifi.removeNetwork(conf.networkId);						
					}
					wifi.saveConfiguration();
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
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}		
		
		// Open a new activity called GCMMessageView
		{
			// Wake Android Device when notification received
			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			final PowerManager.WakeLock mWakelock = pm.newWakeLock(
					PowerManager.FULL_WAKE_LOCK
							| PowerManager.ACQUIRE_CAUSES_WAKEUP, "GCM_PUSH");
			mWakelock.acquire();

			// Timer before putting Android Device to sleep mode.
			Timer timer = new Timer();
			TimerTask task = new TimerTask() {
				public void run() {
					mWakelock.release();
				}
			};
			timer.schedule(task, 5000);
		}

	}

	@Override
	protected void onError(Context arg0, String errorId) {

		Log.e(TAG, "onError: errorId =" + errorId);
	}

}