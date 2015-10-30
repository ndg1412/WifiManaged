package com.net.wifimanagedsdn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class WifiEnableService extends BroadcastReceiver {
	private static final String TAG = "WifiEnableService";
	public static boolean bRunManual = false;
	public static boolean bScan = false;
	public Thread thWifiManual;
	static int iDemManual = 0;
	List<WifiConfiguration> listWifiConf = null;
	@SuppressWarnings("static-access")
	@Override
    public void onReceive(Context context, Intent intent) {
		//Log.d(TAG, "========================WifiEnableService====================================>");
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if(wifi.isWifiEnabled()) {
			Log.d(TAG, "--------------save list profile--------------------->");
			listWifiConf = wifi.getConfiguredNetworks();
		}
		Network nw = new Network(wifi, context);
		int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
		Log.d(TAG, "wifi state: " + wifiState);

		if(wifiState == WifiManager.WIFI_STATE_ENABLED) {
			iDemManual = 0;
			bScan = false;
			Log.d(TAG, "========================WIFI_STATE_ENABLED====================================>");
//			Sdn_Service.db.getAllWifi();
			if((Sdn_Service.db.getUser() != null) && (Sdn_Service.db.getTime() != null)) {
				Log.d(TAG, "========================wifi to enable run====================================>");
				try {
					Thread.sleep(2000);
					if(listWifiConf != null) {
						for(WifiConfiguration conf : listWifiConf) {
							boolean bCheck = wifi.removeNetwork(conf.networkId);
							Log.d(TAG, "ssid config: " + conf.SSID + ", id = " + conf.networkId + ", remove = " + bCheck);

						}
						wifi.saveConfiguration();
						listWifiConf = null;
					}
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
			} else {
				Toast.makeText(context, "please configuration: User Information and Communication before", Toast.LENGTH_LONG).show();
			}
					
		} else if(wifiState == WifiManager.WIFI_STATE_DISABLING) {
			Log.d(TAG, "========================WIFI_STATE_DISABLING====================================>");
			if(listWifiConf != null) {
				for(WifiConfiguration conf : listWifiConf) {
					boolean bCheck = wifi.removeNetwork(conf.networkId);
					Log.d(TAG, "ssid config: " + conf.SSID + ", id = " + conf.networkId + ", remove = " + bCheck);

				}
				wifi.saveConfiguration();
				listWifiConf = null;
			}
		}else if(wifiState == WifiManager.WIFI_STATE_DISABLED) {
			Log.d(TAG, "========================WIFI_STATE_DISABLED====================================>");
			try {
				if(listWifiConf != null) {
					for(WifiConfiguration conf : listWifiConf) {
						boolean bCheck = wifi.removeNetwork(conf.networkId);
						Log.d(TAG, "ssid config: " + conf.SSID + ", id = " + conf.networkId + ", remove = " + bCheck);

					}
					wifi.saveConfiguration();
					listWifiConf = null;
				}
				iDemManual = 0;
				bScan = false;
				Sdn_Service.strGBestAP = null;
				Sdn_Service.CloseConnect();
				Sdn_Service.bRun = false;
				Sdn_Service.bttReqManagedApList = false;
				Sdn_Service.bttRepScanAp = false;
				if(Sdn_Service.tiRepScanAp != null) {
					Sdn_Service.tiRepScanAp.cancel();
					Sdn_Service.tiRepScanAp.purge();
				}
				if(Sdn_Service.tiReqManagedAPList != null) {
					Sdn_Service.tiReqManagedAPList.cancel();
					Sdn_Service.tiReqManagedAPList.purge();
				}
				if(thWifiManual != null) {
					thWifiManual.join();
					thWifiManual = null;
				}
				if(Sdn_Service.thWifiManaged != null)
					Sdn_Service.thWifiManaged.join();				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*String action = intent.getAction();
//		Log.d(TAG, "action: " + action);
		Bundle bun = intent.getExtras();
//		Log.d(TAG, "bundle: " + bun);
		ConnectivityManager cm = (ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//		Log.d(TAG, "check network: " + nw.isInternet());
		if (networkInfo != null) {
			if(wifi.isWifiEnabled()) {
				if(nw.isWifiConnected() && nw.isInternet()) {
					WifiInfo info = wifi.getConnectionInfo();
					Log.d(TAG, "wifi info ssid :" + info.getSSID());
					Log.d(TAG, "strGBestAP: " + Sdn_Service.strGBestAP);
					if((!Sdn_Service.brnWifiManaged) && (!Sdn_Service.bTimerScan) &&
							(info != null) && (Sdn_Service.strGBestAP != null) &&
							(!info.getSSID().contains(Sdn_Service.strGBestAP)) && (iDemManual == 0)) {
						Log.d(TAG, "---------------------wifi connect success by manual--------------------------->");
						Toast.makeText(context, "User change wifi network by manual", Toast.LENGTH_LONG).show();
						if(Sdn_Service.iIdBestAp != -1) {
							wifi.removeNetwork(Sdn_Service.iIdBestAp);
							Sdn_Service.iIdBestAp = -1;
						}
						iDemManual++;
						try {
							bScan = false;
							Sdn_Service.strGBestAP = null;
							Sdn_Service.bRun = false;
							Sdn_Service.bttRepScanAp = false;
							if(Sdn_Service.tiRepScanAp != null) {
								Sdn_Service.tiRepScanAp.cancel();
								Sdn_Service.tiRepScanAp.purge();
							}
							if(Sdn_Service.tiReqManagedAPList != null) {
								Sdn_Service.tiReqManagedAPList.cancel();
								Sdn_Service.tiReqManagedAPList.purge();
							}
							if(thWifiManual != null) {
								thWifiManual.join();
								thWifiManual = null;
							}
							Thread.sleep(2000);
							thWifiManual = new Thread(rnWifiManual);
							thWifiManual.start();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {
					if(action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
						if(bun.toString().contains("DISCONNECTED")) {
							if((Sdn_Service.strGBestAP != null)) {
								bScan = true;
								wifi.startScan();
							}							
						}					
					}
					if(action.equals("android.net.wifi.SCAN_RESULTS")) {
						nw.scanWifi();
//						Log.d(TAG, "check best ap current live: " + nw.checkScanAP(Sdn_Service.strGBestAP));						
						if((Sdn_Service.strGBestAP != null) && (bScan) && (!nw.checkScanAP(Sdn_Service.strGBestAP))) {
							bScan = false;
							Log.d(TAG, "Best AP current lost connect");
							Toast.makeText(context, "Best AP current lost connect. Run process again", Toast.LENGTH_LONG).show();
							for(WifiConfiguration conf : wifi.getConfiguredNetworks()) {
//								Log.d(TAG, "ssid config: " + conf.SSID);
								if(conf.SSID.contains(Sdn_Service.strGBestAP)) {
									wifi.removeNetwork(conf.networkId);
									break;
								}
							}								
							try {
								//disable
								Sdn_Service.CloseConnect();
								Sdn_Service.bRun = false;
								Sdn_Service.bttReqManagedApList = false;
								Sdn_Service.bttRepScanAp = false;
								if(Sdn_Service.tiRepScanAp != null) {
									Sdn_Service.tiRepScanAp.cancel();
									Sdn_Service.tiRepScanAp.purge();
								}
								if(Sdn_Service.tiReqManagedAPList != null) {
									Sdn_Service.tiReqManagedAPList.cancel();
									Sdn_Service.tiReqManagedAPList.purge();
								}
								if(thWifiManual != null) {
									thWifiManual.join();
									thWifiManual = null;
								}
								if(Sdn_Service.thWifiManaged != null)
									Sdn_Service.thWifiManaged.join();
								
								//enable
								Thread.sleep(5000);
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
			}
			
		}*/
	}
	
	public static boolean isThread = true;
	static Runnable rnWifiManual = new Runnable() {
		public void run() {
//			while(isThread) {
				Sdn_Service.CloseConnect();
				if(Sdn_Service.InitConnect()) {
					int iSend = -1;
					for(int i = 0; i < 5; i++) {
						iSend = Sdn_Service.Send_Connection_Req();
						if(iSend != -1)
							break;
					}
					if(iSend == 1) {
						Sdn_Service.bttReqManagedApList = true;
						Sdn_Service.bttRepScanAp = true;
						Sdn_Service.bRun = true;
						int iTimeManaged = Sdn_Service.db.getTime().getUpdateTime();
						int iTimeScan = Sdn_Service.db.getTime().getScanTime();
						Log.d(TAG, "tiReqManagedAPList time delay: " + iTimeManaged);
						Log.d(TAG, "ttRepScanAp time delay: " + iTimeScan);
						Sdn_Service.tiRepScanAp = new Timer();
						Sdn_Service.tiRepScanAp.schedule(new TimerTask() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stube
								Sdn_Service.funcRepScanAp();							
							}
						}, 0, iTimeScan*1000);
						Sdn_Service.tiReqManagedAPList = new Timer();
						Sdn_Service.tiReqManagedAPList.schedule(new TimerTask() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Sdn_Service.funcReqManagedApList();
							}
						}, 5000, iTimeManaged*1000);
					}
				}
//			}
		}
	};
	
	public String getState(State state) {
		if(state == State.CONNECTED)
			return "CONNECTED";
		else if(state == State.CONNECTING)
			return "CONNECTING";
		else if(state == State.DISCONNECTED)
			return "DISCONNECTED";
		else if(state == State.DISCONNECTING)
			return "DISCONNECTING";
		else if(state == State.SUSPENDED)
			return "SUSPENDED";
		else
			return "UNKNOWN";
	}
}
