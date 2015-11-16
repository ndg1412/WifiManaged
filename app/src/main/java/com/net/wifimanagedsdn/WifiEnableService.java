package com.net.wifimanagedsdn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.net.wifimanagedsdn.wificonfig.ConfigurationSecuritiesV8;
public class WifiEnableService extends BroadcastReceiver {
	private static final String TAG = "WifiEnableService";
	public static boolean bRunManual = false;
	public static boolean bScan = false;
	public Thread thWifiManual;
	static int iDemManual = 0;
	boolean bDialog = false;
	WifiManager wifi;
	Context mContext;
	@SuppressWarnings("static-access")
	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.d(TAG, "========================WifiEnableService====================================>");
		mContext = context;
		wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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
//					Thread.sleep(1000);
					for(WifiConfiguration conf : wifi.getConfiguredNetworks()) {
						boolean bCheck = wifi.removeNetwork(conf.networkId);
						Log.d(TAG, "ssid config: " + conf.SSID + ", id = " + conf.networkId + ", remove = " + bCheck);

					}

					wifi.saveConfiguration();
					Sdn_Service.iIdAp = -1;
					Sdn_Service.strDataAP = null;
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

		}else if(wifiState == WifiManager.WIFI_STATE_DISABLED) {
			Log.d(TAG, "========================WIFI_STATE_DISABLED====================================>");
			try {
				iDemManual = 0;
				bScan = false;
				Sdn_Service.strDataAP = null;
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
		String action = intent.getAction();
//		Log.d(TAG, "action: " + action);
		Bundle bun = intent.getExtras();
//		Log.d(TAG, "bundle: " + bun);
		ConnectivityManager cm = (ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		State nwState = networkInfo.getState();
		Log.d(TAG, "check nwState: " + nwState);
		if((networkInfo != null) && wifi.isWifiEnabled()) {
			if((nwState == State.DISCONNECTED) || (nwState == State.CONNECTED)) {
				if(CheckManagerProfile()) {
					Log.d(TAG, "giang dbg -----------------------------------------");
					for(WifiConfiguration conf : wifi.getConfiguredNetworks()) {
						boolean bCheck = wifi.removeNetwork(conf.networkId);
						Log.d(TAG, "ssid config: " + conf.SSID + ", id = " + conf.networkId + ", remove = " + bCheck);

					}

					wifi.saveConfiguration();
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
					try {
						bScan = false;
						Sdn_Service.strDataAP = null;
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
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// set title
					alertDialogBuilder.setTitle("Fail");
					alertDialogBuilder.setMessage("Please, do not manually change AP. Turning off the WiFi.");
					alertDialogBuilder.setCancelable(false)
							.setNegativeButton("Ok",new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									// if this button is clicked, just close
									// the dialog box and do nothing
									wifi.setWifiEnabled(false);
									dialog.cancel();
								}
							});
					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();

					// show it
					alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					alertDialog.show();
					/*try {
						bScan = false;
						Sdn_Service.strDataAP = null;
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
						
						Sdn_Service.iIdAp = -1;
						Sdn_Service.strDataAP = null;
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
					}*/
				}
			}


					/*WifiInfo info = wifi.getConnectionInfo();
					Log.d(TAG, "wifi info ssid :" + info.getSSID());
					Log.d(TAG, "strDataAP: " + Sdn_Service.strDataAP);
					if((!Sdn_Service.brnWifiManaged) && (!Sdn_Service.bTimerDataAP) &&
							(info != null) && (((Sdn_Service.strDataAP != null) &&
							(!info.getSSID().contains(Sdn_Service.strDataAP))) || (Sdn_Service.strDataAP == null)) && (iDemManual == 0)) {
						Log.d(TAG, "---------------------wifi connect success by manual--------------------------->");
						Toast.makeText(context, "User change wifi network by manual", Toast.LENGTH_LONG).show();
						if(Sdn_Service.iIdAp != -1) {
							boolean bCheck = wifi.removeNetwork(Sdn_Service.iIdAp);
							Log.d(TAG, "connect manual by user remove previous profile: " + bCheck);
							wifi.saveConfiguration();
							Sdn_Service.iIdAp = -1;
						}
						iDemManual++;
						try {
							bScan = false;
							Sdn_Service.strDataAP = null;
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
							Sdn_Service.iIdAp = -1;
							Sdn_Service.strDataAP = null;
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
					}*/
			if(action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
				if(bun.toString().contains("DISCONNECTED")) {
					if((Sdn_Service.strDataAP != null)) {
						bScan = true;
						wifi.startScan();
					}
				}
			}
			if(action.equals("android.net.wifi.SCAN_RESULTS")) {
				nw.scanWifi();
//						Log.d(TAG, "check best ap current live: " + nw.checkScanAP(Sdn_Service.strDataAP));						
				if((Sdn_Service.strDataAP != null) && (bScan) && (!nw.checkScanAP(Sdn_Service.strDataAP))) {
					bScan = false;
					Log.d(TAG, "Best AP current lost connect");
					Toast.makeText(context, "Best AP current lost connect. Run process again", Toast.LENGTH_LONG).show();
					for(WifiConfiguration conf : wifi.getConfiguredNetworks()) {
//								Log.d(TAG, "ssid config: " + conf.SSID);
						if(conf.SSID.contains(Sdn_Service.strDataAP)) {
							wifi.removeNetwork(conf.networkId);
							wifi.saveConfiguration();
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
						Thread.sleep(2000);
						Sdn_Service.iIdAp = -1;
						Sdn_Service.strDataAP = null;
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

	public boolean CheckManualProfile(String ssid) {

		for(WifiConfiguration conf : wifi.getConfiguredNetworks()) {
			if(!conf.SSID.equals(ssid))
				return true;
		}
		return false;
	}


	public boolean CheckManagerProfile() {

		for(WifiConfiguration conf : wifi.getConfiguredNetworks()) {
			Log.d(TAG, "giang dbg conf sec: " + ConfigurationSecuritiesV8.getSecurity(conf));
			if(conf.SSID.contains("T wifi zone"))
				continue;
			if(ConfigurationSecuritiesV8.getSecurity(conf) != ConfigurationSecuritiesV8.SECURITY_EAP) {
				Log.d(TAG, "giang dbg conf ssid: " + conf.SSID);
				return true;
			}
			if(MainActivity.dbUser != null) {
				if (!conf.enterpriseConfig.getIdentity().equals(MainActivity.dbUser.getUser())) {
					Log.d(TAG, "giang dbg conf ssid11111: " + conf.SSID);
					return true;
				}

			}
		}
		return false;
	}

}
