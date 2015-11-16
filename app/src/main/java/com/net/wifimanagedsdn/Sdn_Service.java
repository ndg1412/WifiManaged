package com.net.wifimanagedsdn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gcm.GCMRegistrar;
import com.net.wifimanagedsdn.protocol.Ap_type;
import com.net.wifimanagedsdn.protocol.Best_Ap_req;
import com.net.wifimanagedsdn.protocol.Best_Ap_res;
import com.net.wifimanagedsdn.protocol.Connection_Req;
import com.net.wifimanagedsdn.protocol.Connection_Res;
import com.net.wifimanagedsdn.protocol.Connection_Res_type;
import com.net.wifimanagedsdn.protocol.GCM_info_req;
import com.net.wifimanagedsdn.protocol.Gcm_Disconnect_req;
import com.net.wifimanagedsdn.protocol.Managed_Ap_req;
import com.net.wifimanagedsdn.protocol.Managed_Ap_res;
import com.net.wifimanagedsdn.protocol.Managed_Ap_update;
import com.net.wifimanagedsdn.protocol.Packet_Type;
import com.net.wifimanagedsdn.sqlite.DatabaseHandler;
import com.net.wifimanagedsdn.sqlite.User;
import com.net.wifimanagedsdn.sqlite.Version;
import com.net.wifimanagedsdn.sqlite.Wifi;
import com.net.wifimanagedsdn.util.Constan;
import com.net.wifimanagedsdn.util.ReadFileConfig;
import com.net.wifimanagedsdn.wificonfig.ConfigurationSecuritiesV8;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class Sdn_Service extends Service{

	private static final String TAG = "Sdn_Service";

	WifiEnableService wes;
	static Network nwNetwork;
	static WifiManager wifi;
	public static DatabaseHandler db;
	static Context context;
	static Socket skClient;
	Timer tiPeriodicRequest, tiPeriodicReport;
	//	static boolean bthWifiManaged = true;
	static Thread thWifiManaged;
	static Handler hanAlert = new Handler();
	static Handler hanToast = new Handler();
	static Timer tiReqManagedAPList = null;
	static Timer tiRepScanAp = null;

	public static String strServerIp, strNatServerIp;
	public static int iServerPort, iNatServerPort;
	public static String strSenderId, strRegId;

	static boolean brnWifiManaged = true;
	static boolean bTimerUp = false;
	static boolean bTimerDataAP = false;
	static boolean bRun = true;
	public static boolean bttReqManagedApList = true;
	public static boolean bttRepScanAp = true;
	//	boolean bttReqManagedApList = false;
	public static int iInitconn = 0;
	public static boolean bWifiAuto = false;
	public static String strDataAP = null;
	public static int iIdAp = -1;
	public static boolean bGcm_info = false;


	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "service onCreate");
		context = this;
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		nwNetwork = new Network(wifi, context);
		ReadFileConfig config = new ReadFileConfig();
		db = new DatabaseHandler(this);

		//read text file, copy to database
		if(db.getAllWifi().size() == 0) {
			config.getApList();
		} else
			Log.d(TAG, "================================have db=========================");
		strServerIp = config.getAdressServer();
		strNatServerIp = config.getNatAdressServer();
		iServerPort = config.getPortServer();
		iNatServerPort = config.getNatPortServer();
		strSenderId = config.getSenderId();
		Log.d(TAG, "strServerIp: " + strServerIp);
		Log.d(TAG, "iServerPort: " + iServerPort);
		Log.d(TAG, "strSenderId: " + strSenderId);
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		/*Log.d(TAG, "================================check reg id=========================");
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

		// Showing status
		if(status == ConnectionResult.SUCCESS)
			Log.d(TAG, "Google Play Services are available==========================");
		else
			Log.d(TAG, "Google Play Services have error=================================");
		GCMRegistrar.register(this, config.getSenderId());
		String regid = GCMRegistrar.getRegistrationId(context);
		Log.d(TAG, "giang dbg reg id: " + regid);
		Log.d(TAG, "================================check reg id success=========================");*/


		//init variable
		iInitconn = 0;
		strDataAP = null;
		//set boolean
		brnWifiManaged = true;
		bttReqManagedApList = true;
		bttRepScanAp = true;
		bRun = true;
		bTimerUp = false;
		bTimerDataAP = false;

		//Start wifienable service receive
		wes = new WifiEnableService();
		IntentFilter filters = new IntentFilter();
		filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
		filters.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		filters.addAction("android.net.wifi.SCAN_RESULTS");
		registerReceiver(wes, filters);


	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "service onBind");
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*Log.d(TAG, "service onStartCommand");
		Log.d(TAG, "strServerIp: " + strServerIp);
		Log.d(TAG, "iServerPort: " + iServerPort);
		Log.d(TAG, "strSenderId: " + strSenderId);*/
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "service onDestroy");

		try {
			WifiEnableService.iDemManual = 0;
			WifiEnableService.bScan = false;
			Sdn_Service.strDataAP = null;

			CloseConnect();
			bRun = false;
			bttReqManagedApList = false;
			bttRepScanAp = false;
			Thread.sleep(1000);
			if(tiRepScanAp != null) {
				tiRepScanAp.cancel();
				tiRepScanAp.purge();
			}
			if(tiReqManagedAPList != null) {
				tiReqManagedAPList.cancel();
				tiReqManagedAPList.purge();
			}
			if(thWifiManaged != null)
				thWifiManaged.join();
			if(db != null)
				db.close();

			if(GCMRegistrar.isRegistered(context))
				GCMRegistrar.unregister(context);
			stopGCMService();
			bGcm_info = false;

			if(wes != null)
				unregisterReceiver(wes);

			if(!wifi.isWifiEnabled())
				wifi.setWifiEnabled(true);
			wifi.disconnect();
			Log.d(TAG, "giang dbg: turn on wifi ==========================>");
			Thread.sleep(1000);
			if(wifi.isWifiEnabled()) {
				for(WifiConfiguration conf : wifi.getConfiguredNetworks()) {
					wifi.removeNetwork(conf.networkId);
				}
				wifi.saveConfiguration();
				Log.d(TAG, "giang dbg: turn off wifi");
				boolean bTurn = wifi.setWifiEnabled(false);
				Log.d(TAG, "giang dbg: turn off wifi bTurn = " + bTurn);
			}
			Log.d(TAG, "giang dbg: turn on wifi ==========================>");
			mHandlerAlertCon_Req_Fail.removeMessages(0);
			mHandlerToast.removeMessages(0);
			mHandlerAlertDisConnect.removeMessages(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static Runnable rnWifiManaged = new Runnable() {
		public void run() {
			Log.d(TAG, "--------------------rnWifiManaged start------------------------");
			while(brnWifiManaged) {
				Log.d(TAG, "Thread brnWifiManaged start");

				if(nwNetwork.isWifiConnected()) {

					wifi.disconnect();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				for(WifiConfiguration conf : wifi.getConfiguredNetworks()) {
					boolean bCheck = wifi.removeNetwork(conf.networkId);
					Log.d(TAG, "ssid config: " + conf.SSID + ", id = " + conf.networkId + ", remove = " + bCheck);

				}
				wifi.saveConfiguration();

				Log.d(TAG, "number AP in db: " + db.getAllWifi().size());
				if(db.getAllWifi().size() > 0) {
					if(wifi.isWifiEnabled())
						ConnectManagedAp();
				} else {
					Log.d(TAG, "do not have AP in db===============================>");
					Log.d(TAG, "nwNetwork===============================>" + nwNetwork);
					Log.d(TAG, "isMobileConnected===============================>" + nwNetwork.isMobileConnected());
					if(nwNetwork.isMobileConnected() && !nwNetwork.getMobileIp().equals("0.0.0.0")) {
						Log.d(TAG, "have 3g internet ===============================>");
						if(InitConnectNat()) {

							funcReqManagedApList();
							CloseConnect();
							if(wifi.isWifiEnabled())
								ConnectManagedAp();
						} else {
							Message msgToast = new Message();
							msgToast.obj = "No connect to server!";
							mHandlerToast.sendMessage(msgToast);
						}
					}
				}
				Log.d(TAG,"rnWifiManaged: skClient: " + skClient);
				Log.d(TAG,"rnWifiManaged: isWifiConnected: " + nwNetwork.isWifiConnected());
				Log.d(TAG,"rnWifiManaged: isInternet: " + nwNetwork.isInternet());
				Log.d(TAG,"rnWifiManaged: wifi ip: " + !nwNetwork.getWifiIp().equals("0.0.0.0"));

				if((skClient != null)  && (!nwNetwork.getWifiIp().equals("0.0.0.0")) && (nwNetwork.isInternet())) {
					//run 2 timer
					bTimerDataAP = false;
					bTimerUp = false;
					try {
						Thread.sleep(2000);
						tiReqManagedAPList = new Timer();
						int iTimeManaged = db.getTime().getUpdateTime();
						int iTimeScan = db.getTime().getScanTime();
						Log.d(TAG, "tiReqManagedAPList time delay: " + iTimeManaged);
						tiReqManagedAPList.schedule(new TimerTask() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								funcReqManagedApList();
							}
						}, 0, iTimeManaged*1000);
						tiRepScanAp = new Timer();
						Log.d(TAG, "ttRepScanAp time delay: " + iTimeScan);
						tiRepScanAp.schedule(new TimerTask() {

							@Override
							public void run() {
								// TODO Auto-generated method stube
								funcRepScanAp();
							}
						}, 5000, iTimeScan*1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				brnWifiManaged = false;
				Log.d(TAG, "brnWifiManaged: " + brnWifiManaged);
			}
			Log.d(TAG, "--------------------rnWifiManaged end------------------------");
		}
	};

	public boolean checkDbExist(Context context, String dbName) {
		File dbFile = context.getDatabasePath(dbName);
		return dbFile.exists();
	}

	public static boolean InitConnect() {
		Log.d(TAG, "--------------------InitConnect start------------------------");
		try {
			Log.d(TAG, "strServerIp: " + strServerIp);
			Log.d(TAG, "iServerPort: " + iServerPort);
			if(skClient != null) {
				skClient.close();
				skClient = null;
			}
			skClient = new Socket(strServerIp, iServerPort);
			skClient.setSoTimeout(50000);
			skClient.setTcpNoDelay(true);
//			skClient.setKeepAlive(true);
			Log.d(TAG, "socket time out: " + skClient.getSoTimeout());
			Log.d(TAG, "socket no delay: " + skClient.getTcpNoDelay());
			Log.d(TAG, "===============init connect success: ");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "===============init connect fails: ");
			e.printStackTrace();
			CloseConnect();
			return false;
		}  catch (IOException e) {
			Log.d(TAG, "===============init connect fails: ");
			// TODO Auto-generated catch block
			e.printStackTrace();
			CloseConnect();
			return false;
		}
		Log.d(TAG, "--------------------InitConnect end------------------------");
		return true;
	}

	public static boolean InitConnectNat() {
		Log.d(TAG, "--------------------InitConnectNat start------------------------");
		try {
			Log.d(TAG, "strNatServerIp: " + strNatServerIp);
			Log.d(TAG, "iNatServerPort: " + iNatServerPort);
			if(skClient != null) {
				skClient.close();
				skClient = null;
			}
			skClient = new Socket(strNatServerIp, iNatServerPort);
			skClient.setSoTimeout(50000);
			skClient.setTcpNoDelay(true);
//			skClient.setKeepAlive(true);
			Log.d(TAG, "socket time out: " + skClient.getSoTimeout());
			Log.d(TAG, "socket no delay: " + skClient.getTcpNoDelay());
			Log.d(TAG, "===============InitConnectNat success: ");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "===============InitConnectNat fails: ");
			e.printStackTrace();
			CloseConnect();
			return false;
		}  catch (IOException e) {
			Log.d(TAG, "===============InitConnectNat fails: ");
			// TODO Auto-generated catch block
			e.printStackTrace();
			CloseConnect();
			return false;
		}
		Log.d(TAG, "--------------------InitConnectNat end------------------------");
		return true;
	}

	public static void CloseConnect() {
		Log.d(TAG, "--------------------CloseConnect start------------------------");
		try {
			if(skClient != null) {
				skClient.close();
				skClient = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "===============close connect fail: ");
		}
		Log.d(TAG, "--------------------CloseConnect end------------------------");

	}

	public static void funcReqManagedApList() {
		Log.d(TAG, "--------------------funcReqManagedApList start------------------------");
		Log.d(TAG, "--------------------funcReqManagedApList bttReqManagedApList: " + bttReqManagedApList);
		while(true) {
			if(!bTimerDataAP || !bRun)
				break;
		}
		if(bttReqManagedApList && !bTimerDataAP) {
			bTimerUp = true;
			long lVersion = 0;
			Version vVer = db.getVersionManaged();
			if(vVer != null)
				lVersion = vVer.getVersion();
			Log.d(TAG, "lVersion = " + lVersion);
			Managed_Ap_req req = new Managed_Ap_req(db.getUser().getUser(), db.getUser().getPass(), lVersion);
			Managed_Ap_res res = null;

			try {
				String strBuff = "";
				for(byte bb : req.getBytes())
					strBuff += String.format(" %02X", bb);
				Log.d(TAG, "Managed_Ap_req msg: " + strBuff);
//				Thread.sleep(5000);
//				if(!InitConnect())
//					return;
				Log.d(TAG, "state socket client connect: " + skClient.isConnected());
				InputStream isRecv = skClient.getInputStream();
				OutputStream osSend = skClient.getOutputStream();
				osSend.write(req.getBytes());
				osSend.flush();
				byte[] abRecv = new byte[19];
				int leng = isRecv.read(abRecv);

				if(leng < 0) {
					Log.d(TAG, "leng Managed_Ap_res res: " + leng);
					bTimerUp = false;
					Log.d(TAG, "--------------------funcReqManagedApList fail------------------------");
					return;
				}
				byte[] aApRes = new byte[leng];
				System.arraycopy(abRecv, 0, aApRes, 0, leng);
				strBuff = "";
				for(byte bb : aApRes)
					strBuff += String.format(" %02X", bb);
				Log.d(TAG, "Managed_Ap_res msg: " + strBuff);
				res = new Managed_Ap_res(aApRes);
				if(res.getPacketType() == Packet_Type.MANAGED_AP_RESP_LIST) {
					Log.d(TAG, "===========================MANAGED_AP_RESP_LIST===============================");
					int iTotalPacket = res.getNumberPacket();
					int iAp = res.getNumberAp();
					int iNumber = 0;
					Log.d(TAG, "===============================iAp = " + iAp);
					long lresVer = res.getVersion();
					if(lresVer > lVersion) {
						if(vVer == null)
							db.addVersionManaged(new Version(lresVer));
						db.updateVersionManaged(lresVer);
						db.deleteAll();
					}
					while(true) {
						if(!bRun)
							break;
						byte[] abUpdate = new byte[1149];
						int leng_up = isRecv.read(abUpdate);
//						if(leng_up < 0)
//							return;
						Log.d(TAG, "leng Managed_Ap_update res: " + leng_up);
						byte[] buff = new byte[leng_up];
						System.arraycopy(abUpdate, 0, buff, 0, leng_up);
						strBuff = "";
						for(byte bb : buff)
							strBuff += String.format(" %02X", bb);
						Log.d(TAG, "Managed_Ap_update msg: " + strBuff);
						Managed_Ap_update muAp = new Managed_Ap_update(buff);
						Log.d(TAG, "Managed_Ap_update type: " + muAp.getPacketType());
						int i = 0;
						if(muAp.isSuccess()) {
							Ap_type[] atList = muAp.getAp_type();
							iNumber = muAp.getIndexPacket();
							Log.d(TAG, "iNumber = " + iNumber);
							int iApPos = 0;
							for(Ap_type ap : atList) {
								Log.d(TAG, (30*iNumber + i) + " add ap to db ssid: " + ap.getSSID() + ", mac: " + ap.getMac());
								iApPos = 30*iNumber + i + 1;
								if(lresVer > lVersion)
									db.addWifi(new Wifi(ap.getSSID(), ap.getMac()));
								i++;
							}
							iNumber++;
							Log.d(TAG, "iAp = " + iAp);
							Log.d(TAG, "iTotalPacket = " + iTotalPacket);
							Log.d(TAG, "iNumber = " + iNumber);
							Log.d(TAG, "iApPos = " + iApPos);
							if((iNumber == iTotalPacket) || (iApPos == iAp)) {
								break;
							}

						}
					}
				} else if(res.getPacketType() == Packet_Type.MANAGED_AP_RESP_NONE) {
					Log.d(TAG, "===========================MANAGED_AP_RESP_NONE===============================");
//					db.deleteAll();
				}
//				CloseConnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				CloseConnect();
			} catch (Exception e) {
				e.printStackTrace();
				CloseConnect();
			}
			bTimerUp = false;
		}
		Log.d(TAG, "--------------------ttReqManagedApList end------------------------");
	}

	public static void funcRepScanAp() {
		while(true) {
			if(!bTimerUp || !bRun)
				break;
		}
		if(bttRepScanAp) {
			Log.d(TAG, "--------------------funcRepScanAp start------------------------");
			Log.d(TAG, "ttRepScanAp: 11111111111111111111111 " + bTimerUp);
			if(!bTimerUp)

				ConnectBestAp();
			Log.d(TAG, "--------------------funcRepScanAp end------------------------");
		}
	}

	@SuppressWarnings("static-access")
	public static boolean ConnectBestAp() {
		Log.d(TAG, "--------------------ConnectBestAp start------------------------");
		bTimerDataAP = true;
		boolean bBestAp = false;
		wifi.startScan();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<HashMap<String, String>> list = nwNetwork.scanWifiSec();
		Log.d(TAG, "number ap sec: " + list.size());
		Ap_type[] atAp = new Ap_type[list.size()];
		int i = 0;
		for(HashMap<String, String> map : list) {
			Log.d(TAG, "ssid: " + map.get("SSID") + ", Mac: " + map.get("BSSID") + ", sec: " + map.get("SECURITY"));
//			if(db.getWifi(map.get("SSID"), map.get("BSSID")) != null) {
//				atAp[i] = new Ap_type(map.get("SSID"), map.get("BSSID"));
//				i++;
//			}

			atAp[i] = new Ap_type(map.get("SSID"), map.get("BSSID"));
			i++;
		}
		Log.d(TAG, "-------------------------------> i = " + i);
		if(i == 0) {
			bTimerDataAP = false;
			Message msg = new Message();
			msg.obj = "No find WPA-EAP AP!";
			mHandlerToast.sendMessage(msg);
			return false;

		}
		Ap_type[] tmpAp = new Ap_type[i];
		System.arraycopy(atAp, 0, tmpAp, 0, i);
		Best_Ap_req req = new Best_Ap_req(tmpAp);
		Best_Ap_res res = null;
		String strBuff = "";
		Log.d(TAG, "Best_Ap_req leng: " + req.getBytes().length);
		for(byte bb : req.getBytes())
			strBuff += String.format(" %02X", bb);
		Log.d(TAG, "BestAp req: " + strBuff);

		try {
//			Thread.sleep(5000);
//			if(!InitConnect())
//				return false;
			Log.d(TAG, "state socket client connect: " + skClient.isConnected());
			InputStream isRecv = skClient.getInputStream();
			OutputStream osSend = skClient.getOutputStream();
			osSend.write(req.getBytes());
			osSend.flush();
			byte[] abRecv = new byte[1024];
			int leng = isRecv.read(abRecv);
//			CloseConnect();
			Log.d(TAG, "leng Best_Ap_res: " + leng);
			if(leng < 0) {
				bTimerDataAP = false;
				return false;
			}

			byte[] tmp = new byte[leng];
			System.arraycopy(abRecv, 0, tmp, 0, leng);
			strBuff = "";
			for(byte bb : tmp)
				strBuff += String.format(" %02X", bb);
			Log.d(TAG, "BestAp res: " + strBuff);
			res = new Best_Ap_res(tmp);
			if(res.isSuccess()) {
				WifiEnableService.iDemManual = 0;
				String strSsid = res.getSSID();
				String strMac = res.getMac();
				Log.d(TAG, "ssid: "+ strSsid);
				Log.d(TAG, "strMac: "+ strMac);

				WifiInfo wifiinfo = wifi.getConnectionInfo();
				if((wifiinfo.getSSID() == null) || (wifiinfo.getBSSID() == null)) {
					WifiConfiguration wc = new WifiConfiguration();
					ConfigurationSecuritiesV8 conf = new ConfigurationSecuritiesV8();
					String sec = String.valueOf(conf.getSecurity("WPA-EAP"));
					Log.d(TAG, "user: " + db.getUser().getUser() + ", Pass: " + db.getUser().getPass());
					conf.setupSecurity(wc, sec, db.getUser().getUser(), db.getUser().getPass());
					wc.hiddenSSID = true;
					wc.SSID = '\"' + strSsid + '\"';
//					Log.d(TAG, "config: " + wc);
					for(WifiConfiguration wcConf : wifi.getConfiguredNetworks()) {
//						Log.d(TAG, "ssid config: " + wcConf.SSID);
						if(wcConf.SSID.contains(strSsid)) {
							wifi.removeNetwork(wcConf.networkId);
							wifi.saveConfiguration();
							break;
						}
					}
					int iAdd = -1;
					for(int iii = 0; iii < 3; iii++) {
						iAdd = wifi.addNetwork(wc);
						Log.d(TAG, "add network to profile: " + iAdd);
						if(iAdd != -1)
							break;
						Thread.sleep(1000);
					}
					if(iAdd == -1) {
						bTimerDataAP = false;
						return false;
					}

					Log.d(TAG, "wifi add network: " + iAdd);
					bBestAp = wifi.enableNetwork(iAdd, true);
					Log.d(TAG, "wifi enableNetwork: " + bBestAp);
					if(bBestAp) {
						if((iIdAp != -1) && (iIdAp != iAdd)) {
							Log.d(TAG, "remove config=========================================>iIdAp: " + iIdAp);
							wifi.removeNetwork(iIdAp);
							wifi.saveConfiguration();
							iIdAp = iAdd;
						}
						else
							iIdAp = iAdd;
						CloseConnect();
//						Thread.sleep(8000);
						long time = System.currentTimeMillis();
						while(true) {
							if(!nwNetwork.getWifiIp().equals("0.0.0.0") || !wifi.isWifiEnabled())
								break;
							long timetmp = System.currentTimeMillis();
							if((timetmp - time) >= Constan.TIME_CONNECT_TO_AP)
								break;
						}
						Thread.sleep(3000);
						Log.d(TAG,"rnWifiManaged: wifi ip: " + nwNetwork.getWifiIp());
						Log.d(TAG,"rnWifiManaged: wifi ip: " + nwNetwork.isInternet());
						if(nwNetwork.isInternet()) {
							if(InitConnect()) {
								strDataAP = strSsid;
								Log.d(TAG, "strDataAP: " + strDataAP);
								int iSend = -1;
								for(int ii = 0; ii < 5; ii++) {
									iSend = Send_Connection_Req();
									Send_GCM_Info_Req();
									if(iSend != -1)
										break;
									Thread.sleep(1000);
								}
							}
						}
					} else {
						wifi.removeNetwork(iAdd);
						wifi.saveConfiguration();
					}
				} else if(wifiinfo.getSSID().equals("") || wifiinfo.getBSSID().equals("")) {
					Log.d(TAG, "space -> strSsid: " + strSsid);
					Log.d(TAG, "space -> strMac: " + strMac);
					WifiConfiguration wc = new WifiConfiguration();
					ConfigurationSecuritiesV8 conf = new ConfigurationSecuritiesV8();
					String sec = String.valueOf(conf.getSecurity("WPA-EAP"));
					Log.d(TAG, "user: " + db.getUser().getUser() + ", Pass: " + db.getUser().getPass());
					conf.setupSecurity(wc, sec, db.getUser().getUser(), db.getUser().getPass());
					wc.hiddenSSID = true;
					wc.SSID = '\"' + strSsid + '\"';
//					Log.d(TAG, "config: " + wc);
					for(WifiConfiguration wcConf : wifi.getConfiguredNetworks()) {
//						Log.d(TAG, "ssid config: " + wcConf.SSID);
						if(wcConf.SSID.contains(strSsid)) {
							wifi.removeNetwork(wcConf.networkId);
							wifi.saveConfiguration();
							break;
						}
					}
					int iAdd = -1;
					for(int iii = 0; iii < 3; iii++) {
						iAdd = wifi.addNetwork(wc);
						Log.d(TAG, "add network to profile: " + iAdd);
						if(iAdd != -1)
							break;
						Thread.sleep(1000);
					}
					if(iAdd == -1) {
						bTimerDataAP = false;
						return false;
					}
					Log.d(TAG, "wifi add network: " + iAdd);
					bBestAp = wifi.enableNetwork(iAdd, true);
					Log.d(TAG, "wifi enableNetwork: " + bBestAp);
					if(bBestAp) {
						if((iIdAp != -1) && (iIdAp != iAdd)) {
							Log.d(TAG, "remove config=========================================>iIdAp: " + iIdAp);
							wifi.removeNetwork(iIdAp);
							wifi.saveConfiguration();
							iIdAp = iAdd;
						}
						else
							iIdAp = iAdd;
						CloseConnect();
//						Thread.sleep(8000);
						long time = System.currentTimeMillis();
						while(true) {
							if(!nwNetwork.getWifiIp().equals("0.0.0.0") || !wifi.isWifiEnabled())
								break;
							long timetmp = System.currentTimeMillis();
							if((timetmp - time) >= Constan.TIME_CONNECT_TO_AP)
								break;
						}
						Thread.sleep(3000);
						Log.d(TAG,"ConnectBestAp: wifi ip: " + nwNetwork.getWifiIp());
						Log.d(TAG,"ConnectBestAp: wifi ip: " + nwNetwork.isInternet());
						if(nwNetwork.isInternet()) {

							if(InitConnect()) {
								strDataAP = strSsid;
								Log.d(TAG, "strDataAP: " + strDataAP);
								int iSend = -1;
								for(int ii = 0; ii < 5; ii++) {
									iSend = Send_Connection_Req();
									Send_GCM_Info_Req();
									if(iSend != -1)
										break;
									Thread.sleep(1000);
								}
							}
						}
					} else {
						wifi.removeNetwork(iAdd);
						wifi.saveConfiguration();
					}
				} else {
					String strinfoSsid = wifiinfo.getSSID();
					Log.d(TAG, "giang dbg: strinfoSsid: " + strinfoSsid);
					String strinfoMac = wifiinfo.getBSSID().toUpperCase();
					Log.d(TAG, "giang dbg: strinfoMac: " + strinfoMac);
					Log.d(TAG, "giang dbg: is isWifiConnected: " + nwNetwork.isWifiConnected());
					Log.d(TAG, "strSsid: " + strSsid);
					Log.d(TAG, "strMac: " + strMac);
					if(!strinfoSsid.contains(strSsid) || !strinfoMac.equals(strMac) || !nwNetwork.isWifiConnected()) {
						Log.d(TAG, "============================not contain=========================");
						wifi.disconnect();
						Thread.sleep(1000);
						for(WifiConfiguration conf : wifi.getConfiguredNetworks()) {
							boolean bCheck = wifi.removeNetwork(conf.networkId);
							Log.d(TAG, "ssid config: " + conf.SSID + ", id = " + conf.networkId + ", remove = " + bCheck);

						}
						wifi.saveConfiguration();
						WifiConfiguration wc = new WifiConfiguration();
						ConfigurationSecuritiesV8 conf = new ConfigurationSecuritiesV8();
						String sec = String.valueOf(conf.getSecurity("WPA-EAP"));
						Log.d(TAG, "user: " + db.getUser().getUser() + ", Pass: " + db.getUser().getPass());
						conf.setupSecurity(wc, sec, db.getUser().getUser(), db.getUser().getPass());
						wc.hiddenSSID = true;
						wc.SSID = '\"' + strSsid + '\"';
//						Log.d(TAG, "config: " + wc);
						/*for(WifiConfiguration wcConf : wifi.getConfiguredNetworks()) {
//							Log.d(TAG, "ssid config: " + wcConf.SSID);
							if(wcConf.SSID.contains(strSsid)) {
								wifi.removeNetwork(wcConf.networkId);
								wifi.saveConfiguration();
								break;
							}
						}*/
						int iAdd = -1;
						for(int iii = 0; iii < 3; iii++) {
							iAdd = wifi.addNetwork(wc);
							Log.d(TAG, "add network to profile: " + iAdd);
							if(iAdd != -1)
								break;
							Thread.sleep(1000);
						}
						if(iAdd == -1) {
							bTimerDataAP = false;
							return false;
						}
						Log.d(TAG, "wifi add network: " + iAdd);
						bBestAp = wifi.enableNetwork(iAdd, true);
						Log.d(TAG, "wifi enableNetwork: " + bBestAp);
						if(bBestAp) {
							if((iIdAp != -1) && (iIdAp != iAdd)) {
								Log.d(TAG, "remove config=========================================>iIdAp: " + iIdAp);
								wifi.removeNetwork(iIdAp);
								wifi.saveConfiguration();
								iIdAp = iAdd;
							}
							else
								iIdAp = iAdd;
							CloseConnect();
//							Thread.sleep(8000);
							long time = System.currentTimeMillis();
							while(true) {
								if(!nwNetwork.getWifiIp().equals("0.0.0.0") || !wifi.isWifiEnabled())
									break;
								long timetmp = System.currentTimeMillis();
								if((timetmp - time) >= Constan.TIME_CONNECT_TO_AP)
									break;
							}
							Thread.sleep(3000);
							Log.d(TAG,"ConnectBestAp: wifi ip: " + nwNetwork.getWifiIp());
							Log.d(TAG,"ConnectBestAp: wifi ip: " + nwNetwork.isInternet());
							if(nwNetwork.isInternet()) {
								if(InitConnect()) {
									strDataAP = strSsid;
									Log.d(TAG, "strDataAP: " + strDataAP);
									int iSend = -1;
									for(int ii = 0; ii < 5; ii++) {
										iSend = Send_Connection_Req();
										Send_GCM_Info_Req();
										if(iSend != -1)
											break;
										Thread.sleep(1000);
									}
								}
							}
						} else {
							wifi.removeNetwork(iAdd);
							wifi.saveConfiguration();
						}
					}
					else {
						strDataAP = strSsid;
						Send_GCM_Info_Req();
						Log.d(TAG, "strDataAP: " + strDataAP);
					}
				}
			} else if(res.isFail()) {
				CloseConnect();
				int iType = res.getErrorType();
				Message msgToast = new Message();
				switch(iType) {
					case Packet_Type.BEST_AP_ERROR_FULL:
						wifi.disconnect();
						mHandlerAlertDisConnect.sendMessage(msgToast);
						break;
					case Packet_Type.BEST_AP_ERROR_INVALID_LIST:
						msgToast.obj = "Unknow: Invalid list Ap!";
						mHandlerToast.sendMessage(msgToast);
						break;
					case Packet_Type.BEST_AP_ERROR_UNKNOWN:
						msgToast.obj = "Error: Unknow!";
						mHandlerToast.sendMessage(msgToast);
						break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			CloseConnect();
			bBestAp = false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bBestAp = false;
		}
//		CloseConnect();
		bTimerDataAP = false;
		Log.d(TAG, "--------------------ConnectBestAp end------------------------");
		return bBestAp;
	}

	@SuppressWarnings("static-access")
	public static void ConnectManagedAp() {
		Log.d(TAG, "--------------------ConnectManagedAp start------------------------");
		if(nwNetwork.isWifiConnected()) {
			wifi.disconnect();
			Log.d(TAG, "===============================disconnect==>");
		}
		wifi.startScan();
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<HashMap<String, String>> list;
		list = nwNetwork.scanWifiSec();
		Log.d(TAG, "giang dbg size list wifi scan: " + list.size());
		for(HashMap<String, String> map : list) {
			String strSsid = map.get("SSID");
			String strMac = map.get("BSSID");
			String strSec = map.get("SECURITY");
			Log.d(TAG, "ssid: " + strSsid + ", Mac: " + strMac + ", sec: " + strSec);
			if(db.getWifi(strSsid, strMac) != null) {
				Log.d(TAG, "===============================connect sec start==>");
				Log.d(TAG, "db user: " + db.getUser().getUser() + ", pass: " + db.getUser().getPass());
				WifiConfiguration wc = new WifiConfiguration();
				ConfigurationSecuritiesV8 conf = new ConfigurationSecuritiesV8();
				conf.setupSecurity(wc, strSec, db.getUser().getUser(), db.getUser().getPass());
				wc.SSID = '\"' + strSsid + '\"';
				for(WifiConfiguration wcConf : wifi.getConfiguredNetworks()) {
					if(wcConf.SSID.contains(strSsid)) {
						wifi.removeNetwork(wcConf.networkId);
						wifi.saveConfiguration();
						break;
					}
				}
				int iAdd = -1;
				for(int iii = 0; iii < 3; iii++) {
					iAdd = wifi.addNetwork(wc);
					Log.d(TAG, "add network to profile: " + iAdd);
					if(iAdd != -1)
						break;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(iAdd == -1) {
					continue;
				}

				boolean active = wifi.enableNetwork(iAdd, true);
				Log.d(TAG, "wifi enableNetwork: " + active);
				if(active) {
					if((iIdAp != -1) && (iIdAp != iAdd)) {
						Log.d(TAG, "remove config=========================================>iIdAp: " + iIdAp);
						wifi.removeNetwork(iIdAp);
						wifi.saveConfiguration();
						iIdAp = iAdd;
					}
					else
						iIdAp = iAdd;
					Log.d(TAG, "ConnectManagedAp sec 1111111111111111111111111111111111111");
					long time = System.currentTimeMillis();
					while(true) {
						if(!nwNetwork.getWifiIp().equals("0.0.0.0") || !wifi.isWifiEnabled())
							break;
						long timetmp = System.currentTimeMillis();
						if((timetmp - time) >= Constan.TIME_CONNECT_TO_AP)
							break;
					}
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					WifiInfo wifiinfo = wifi.getConnectionInfo();

					Log.d(TAG, "wifi info ssid: " + wifiinfo.getSSID());
					Log.d(TAG, "wifi info mac: " + wifiinfo.getBSSID());
					if((wifiinfo != null) && (!wifiinfo.getSSID().contains(strSsid) || !wifiinfo.getBSSID().toUpperCase().equals(strMac))) {
						return;
					}
					Log.d(TAG,"rnWifiManaged: wifi ip: " + nwNetwork.getWifiIp());
					Log.d(TAG, "ConnectManagedAp sec 222222222222222222222222222222222222222");
					Log.d(TAG, "connect to wifi AP SSID: " + strSsid);
					Log.d(TAG, "connect to wifi AP Mac: " + strMac);
					if(InitConnect()) {
						Log.d(TAG, "ConnectManagedAp ConnectBestAp=======================");
						int iSend = Send_Connection_Req();
						Log.d(TAG, "ConnectManagedAp=====================================isend: " + iSend);
						if(iSend == 1) {
							ConnectBestAp();
						} else if(iSend == -1){
							CloseConnect();
							Message msgToast = new Message();
							msgToast.obj = "No send connection request to server or no receive response from server!";
							mHandlerToast.sendMessage(msgToast);
						} else {
							CloseConnect();
							ConnectRespFail(iSend);
						}
						return;
					} else {
						Message msgToast = new Message();
						msgToast.obj = "No connect to server!";
						mHandlerToast.sendMessage(msgToast);
					}
				} else {
					wifi.removeNetwork(iAdd);
					wifi.saveConfiguration();
				}
				Log.d(TAG, "===============================connect sec ap stop=============================");
			}
		}
		Message msg = new Message();
		msg.obj = "No managed Ap near. Turn off wifi.";
		mHandlerToast.sendMessage(msg);
		wifi.setWifiEnabled(false);
		Log.d(TAG, "--------------------ConnectManagedAp end------------------------");
		return;
	}

	static Handler mHandlerAlertCon_Req_Fail = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

			// set title
			alertDialogBuilder.setTitle("Failed: " + msg.obj);
			alertDialogBuilder.setMessage("Touch ok is disable wifi!")
					.setCancelable(false)
//				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog,int id) {
//						ConnectManagedAp();
//					}
//				})
					.setNegativeButton("Ok",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							// if this button is clicked, just close
							// the dialog box and do nothing
							Toast.makeText(context, "Turning off the wifi.",
									Toast.LENGTH_LONG).show();
							for(WifiConfiguration conf : wifi.getConfiguredNetworks()) {
								wifi.removeNetwork(conf.networkId);
							}
							wifi.saveConfiguration();
							wifi.setWifiEnabled(false);
							dialog.cancel();
						}
					});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			alertDialog.show();
		}
	};

	static Handler mHandlerAlertDisConnect = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

			// set title
			alertDialogBuilder.setTitle("Wifi");
			alertDialogBuilder.setMessage("Session Disconnected due to High Congestion")
					.setCancelable(false)
					.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							Toast.makeText(context, "Turning off the wifi.",
									Toast.LENGTH_LONG).show();
							for(WifiConfiguration conf : wifi.getConfiguredNetworks()) {
								wifi.removeNetwork(conf.networkId);
							}
							wifi.saveConfiguration();
							wifi.setWifiEnabled(false);
							dialog.cancel();
						}
					});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			alertDialog.show();

		}
	};

	static Handler mHandlerToast = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Toast.makeText(context, String.valueOf(msg.obj),
					Toast.LENGTH_LONG).show();
		}
	};

	public static void ConnectRespFail(int type) {
		Log.d(TAG, "--------------------ConnectRespFail start------------------------");
		Message msgAlert = new Message();
		switch(type) {
			case Connection_Res_type.WRONG_USER:
				msgAlert.obj = "Wrong user name";
				break;
			case Connection_Res_type.WRONG_PASS:
				msgAlert.obj = "Wrong password";
				break;
			case Connection_Res_type.WRONG_PHONE_NUMBER:
				msgAlert.obj = "wrong Sim UUID";
				break;
			case Connection_Res_type.INVALID_MAC:
				msgAlert.obj = "Invalid Macc address";
				break;
			case Connection_Res_type.INVALID_APP_ID:
				msgAlert.obj = "Invalid application Id";
				break;
			case Connection_Res_type.UNKNOWN:
				msgAlert.obj = "Unknown error";
				break;
			default:
				break;
		}
		mHandlerAlertCon_Req_Fail.sendMessage(msgAlert);
		Log.d(TAG, "--------------------ConnectRespFail end------------------------");
	}

	public static void RecvDisconnectMsg() {
		Log.d(TAG, "--------------------RecvDisconnectMsg start------------------------");
		try {

			byte[] abRecv = new byte[1024];
			Thread.sleep(5000);
			if(!InitConnect())
				return;
			InputStream isRecv = skClient.getInputStream();
			int leng = isRecv.read(abRecv);
			CloseConnect();
			Log.d(TAG, "leng Gcm_Disconnect_req: " + leng);
			if(leng < 0)
				return;

			byte[] tmp = new byte[leng];
			System.arraycopy(abRecv, 0, tmp, 0, leng);
			String strBuff = "";
			for(byte bb : tmp)
				strBuff += String.format(" %02X", bb);
			Log.d(TAG, "Managed_Ap_req msg: " + strBuff);
			Gcm_Disconnect_req req = new Gcm_Disconnect_req(tmp);
			if(req.isSuccess())
				wifi.disconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG, "--------------------RecvDisconnectMsg end------------------------");
	}

	public static int Send_Connection_Req() {
		int iReturn = -1;
		Log.d(TAG, "--------------------Send_Connection_Req start------------------------");
		User user = db.getUser();
		Log.d(TAG, "uuid = " + user.getPhone());
		WifiInfo wifiInf = wifi.getConnectionInfo();
		String mac = wifiInf.getMacAddress().toUpperCase();
		Connection_Req req = new Connection_Req(user.getPhone(), user.getUser(), user.getPass(),
				mac);
		Connection_Res res = null;
		try {
			String strBuff = "";
			for(byte bb : req.getBytes())
				strBuff += String.format(" %02X", bb);
			Log.d(TAG, "connection req: " + strBuff);
//			Thread.sleep(5000);
//			if(!InitConnect())
//				return -1;
			Log.d(TAG, "state socket client connect: " + skClient.isConnected());
			InputStream isRecv = skClient.getInputStream();
			OutputStream osSend = skClient.getOutputStream();
			osSend.write(req.getBytes());
			osSend.flush();
			byte[] abRecv = new byte[1024];
			int leng = isRecv.read(abRecv);
//			CloseConnect();
			Log.d(TAG, "leng Connection_Res: " + leng);
			byte[] tmp = new byte[leng];
			System.arraycopy(abRecv, 0, tmp, 0, leng);
			strBuff = "";
			for(byte bb : tmp)
				strBuff += String.format(" %02X", bb);
			Log.d(TAG, "connection res: " + strBuff);
			res = new Connection_Res(tmp);
			if(res.isSuccess())
				iReturn = 1;
			else if(res.isFail())
				iReturn = res.getTypeFail();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			CloseConnect();
			if(iInitconn == 1) {
				Message msgToast = new Message();
				msgToast.obj = "lost connect to server. Turn off wifi!";
				mHandlerToast.sendMessage(msgToast);
				for(WifiConfiguration conf : wifi.getConfiguredNetworks()) {
					wifi.removeNetwork(conf.networkId);
				}
				wifi.saveConfiguration();
				wifi.setWifiEnabled(false);
			}
			if(InitConnect()) {
				iInitconn = 1;
				Send_Connection_Req();
			} else {
				Message msgToast = new Message();
				msgToast.obj = "No connect to server. Turn off wifi!";
				mHandlerToast.sendMessage(msgToast);
				for(WifiConfiguration conf : wifi.getConfiguredNetworks()) {
					wifi.removeNetwork(conf.networkId);
				}
				wifi.saveConfiguration();
				wifi.setWifiEnabled(false);
			}
			return -1;
		} catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
		Log.d(TAG, "--------------------Send_Connection_Req end------------------------");
		return iReturn;
	}

	public static int Send_GCM_Info_Req() {
		int iReturn = -1;
		Log.d(TAG, "--------------------Send_GCM_Info_Req start------------------------");
		//register device to gcm
		if(!GCMRegistrar.isRegistered(context)) {
			GCMRegistrar.register(context, strSenderId);
		}
		String regid = GCMRegistrar.getRegistrationId(context);
		if(regid.equals(""))
			return -1;
		Log.d(TAG, "giang dbg reg id: " + regid);
//		if(!bGcm_info) {
		GCM_info_req req = new GCM_info_req(regid);
		try {
			String strBuff = "";
			for(byte bb : req.getBytes())
				strBuff += String.format(" %02X", bb);
			Log.d(TAG, "GCM_Info_Req msg: " + strBuff);
			OutputStream osSend = skClient.getOutputStream();
			osSend.write(req.getBytes());
			osSend.flush();
			iReturn = 1;
			bGcm_info = true;
		} catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
//		}
		Log.d(TAG, "--------------------Send_GCM_Info_Req end------------------------");
		return iReturn;
	}

	public void stopGCMService() {
		Intent intent = new Intent(this, GCMIntentService.class);
		stopService(intent);
	}

	public static DatabaseHandler getDatabaseHandler() {
		return db;
	}
}
