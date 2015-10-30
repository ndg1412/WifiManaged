package com.net.wifimanagedsdn;


import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;





import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.net.wifimanagedsdn.wificonfig.ConfigurationSecuritiesV8;

public class Network {
	private static final String TAG = "Network";
	static WifiManager wifi;
	Context mContext;
	
	public Network(WifiManager wi, Context context) {
		wifi = wi;
		mContext = context;
	}
	
	public void forgetAllAP() {
		for(WifiConfiguration config : wifi.getConfiguredNetworks())
			wifi.removeNetwork(config.networkId);
	}
	public void ConfigWifi() {
		if(!wifi.isWifiEnabled()) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
	
			// set title
			alertDialogBuilder.setTitle("Config Wifi");
			alertDialogBuilder.setMessage("Are you sure you want turn on Wifi?")
				.setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						wifi.setWifiEnabled(true);
					}
				})
				.setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
	
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
	
			// show it
			alertDialog.show();
		} else
			Toast.makeText(mContext, "wifi is turn on", Toast.LENGTH_SHORT).show();
	}
	
	public void Config3G() {
		if(!isMobileConnected()) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
	
			// set title
			alertDialogBuilder.setTitle("Config 3G");
			alertDialogBuilder.setMessage("Are you sure you want use data 3G?")
				.setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// Enable data
						ConnectivityManager dataManager;
						dataManager  = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
						Method dataMtd;
						try {
							dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
							dataMtd.setAccessible(true);
							dataMtd.invoke(dataManager, true);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}).setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
	
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
	
			// show it
			alertDialog.show();
		} else
			Toast.makeText(mContext, "3G data is enable", Toast.LENGTH_SHORT).show();
			
	}
	
	public void EnableMobileData() {
		// Enable data
		ConnectivityManager dataManager;
		dataManager  = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		Method dataMtd;
		try {
			dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
			dataMtd.setAccessible(true);
			dataMtd.invoke(dataManager, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getMobileIp() {
		String ip = null;
		try {
			for (Enumeration<NetworkInterface> en = 
					NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
						NetworkInterface intf = en.nextElement();
						// Iterate over all IP addresses in each network interface.
						for (Enumeration<InetAddress> enumIPAddr = 
							intf.getInetAddresses(); enumIPAddr.hasMoreElements();) {
							InetAddress iNetAddress = enumIPAddr.nextElement();
							// Loop back address (127.0.0.1) doesn't count as an in-use 
							// IP address.
							if (!iNetAddress.isLoopbackAddress()) {
								String sLocalIP = iNetAddress.getHostAddress().toString();
								String sInterfaceName = intf.getName();
								Log.d(TAG, "interface: " + sInterfaceName + ", ip: " + sLocalIP);
								if(sInterfaceName.equals("rmnet0") || sInterfaceName.equals("rmnet_data0")
										|| sInterfaceName.equals("ccmni0"))
									ip = sLocalIP;							
							}
						}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}		
		return ip;
	}
	
	@SuppressLint("DefaultLocale")
	public String getWifiIp() {		
		WifiInfo wifiInfo = wifi.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String ipString = String.format(
				"%d.%d.%d.%d",
				(ip & 0xff),
				(ip >> 8 & 0xff),
				(ip >> 16 & 0xff),
				(ip >> 24 & 0xff));

		return ipString;
	}
	
	public boolean isMobileAvailble() {
		ConnectivityManager connManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		return ((netInfo != null) && netInfo.isAvailable());
	}
	
	public boolean isMobileConnected() {
		ConnectivityManager connManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		//Log.d(TAG, "3G " + netInfo);
		return ((netInfo != null) && netInfo.isConnected());
	}
	
	public boolean isWifiAvailble() {
		ConnectivityManager connManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		return ((netInfo != null) && netInfo.isAvailable());
	}
	
	public boolean isWifiConnected() {
		ConnectivityManager connManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		return ((netInfo != null) && netInfo.isConnectedOrConnecting());
	}
	
	public static ArrayList<HashMap<String, String>> scanWifi() {
//		wifi.disconnect();
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
//		WifiInfo info = wifi.getConnectionInfo();
		List<ScanResult> results = wifi.getScanResults();
		for (ScanResult result : results) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("SSID", result.SSID);
			map.put("BSSID", result.BSSID);
			map.put("SECURITY", String.valueOf(ConfigurationSecuritiesV8.getSecurity(result)));
			list.add(map);
		}
		return list;
	}
	
	@SuppressLint("DefaultLocale")
	public static ArrayList<HashMap<String, String>> scanWifiSec() {
//		wifi.disconnect();
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
//		WifiInfo info = wifi.getConnectionInfo();
		List<ScanResult> results = wifi.getScanResults();
		for (ScanResult result : results) {			
			if((ConfigurationSecuritiesV8.getSecurity(result) == ConfigurationSecuritiesV8.SECURITY_EAP) &&
					!result.SSID.trim().equals("")) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("SSID", result.SSID);
				map.put("BSSID", result.BSSID.toUpperCase());
				map.put("SECURITY", String.valueOf(ConfigurationSecuritiesV8.getSecurity(result)));
				list.add(map);
			}
		}
		return list;
	}
	
	public static ArrayList<HashMap<String, String>> scanWifiOpen() {
//		wifi.disconnect();
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
//		WifiInfo info = wifi.getConnectionInfo();
		List<ScanResult> results = wifi.getScanResults();
		for (ScanResult result : results) {
			
			if(ConfigurationSecuritiesV8.getSecurity(result) == ConfigurationSecuritiesV8.SECURITY_NONE) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("SSID", result.SSID);
				map.put("BSSID", result.BSSID);
				map.put("SECURITY", String.valueOf(ConfigurationSecuritiesV8.getSecurity(result)));
				list.add(map);
			}
		}
		return list;
	}
	
	public boolean checkScanAP(String ap) {
		boolean bCheck = false;
		ArrayList<HashMap<String, String>> list;
		list = scanWifiSec();
		int i = 0;
		for(HashMap<String, String> map : list) {
			String strSsid = map.get("SSID");
			Log.d(TAG, "strSsid " + i + " : " + strSsid);
			i++;
			if(strSsid.equals(ap)) {
				bCheck = true;
				break;
			}
		}
			
		return bCheck;
	}
	public boolean isInternet() {
		Runtime runtime = Runtime.getRuntime();
		try {

			Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
			int     exitValue = ipProcess.waitFor();
			return (exitValue == 0);

		} catch (Exception e)          { e.printStackTrace(); } 

		return false;
//		try {
//			InetAddress ipAddr = InetAddress.getByName("8.8.8.8"); //You can replace it with your name
//			Log.d(TAG, "ipAddr: " + ipAddr);
//		if (ipAddr.equals("")) {
//			return false;
//		} else {
//			return true;
//		}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
	}
}
