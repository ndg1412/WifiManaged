package com.net.wifimanagedsdn.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.util.Log;

import com.net.wifimanagedsdn.MainActivity;
import com.net.wifimanagedsdn.Sdn_Service;
import com.net.wifimanagedsdn.sqlite.Wifi;

@SuppressLint("DefaultLocale")
public class ReadFileConfig {
	private static final String TAG = "ReadFileConfig";

	@SuppressWarnings("resource")
	public String getAdressServer() {
		String address = null;
		
		try {
			FileReader filereader = new FileReader("/sdcard/Android/data/com.net.wifimanagedsdn/ip_config");
			BufferedReader buffreader = new BufferedReader(filereader);
			String line;
			while((line = buffreader.readLine()) != null) {
				Log.d(TAG, line);
				if(line.split(":")[0].equals("IP"))
					address = line.split(":")[1].trim();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			
		}
		
		return address;
	}
	
	@SuppressWarnings("resource")
	public String getNatAdressServer() {
		String address = null;
		
		try {
			FileReader filereader = new FileReader("/sdcard/Android/data/com.net.wifimanagedsdn/ip_config");
			BufferedReader buffreader = new BufferedReader(filereader);
			String line;
			while((line = buffreader.readLine()) != null) {
				Log.d(TAG, line);
				if(line.split(":")[0].equals("NAT_IP"))
					address = line.split(":")[1].trim();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			
		}
		
		return address;
	}
	
	@SuppressWarnings("resource")
	public int getPortServer() {
		int port = 0;
		
		try {
			FileReader filereader = new FileReader("/sdcard/Android/data/com.net.wifimanagedsdn/ip_config");
			BufferedReader buffreader = new BufferedReader(filereader);
			String line;
			while((line = buffreader.readLine()) != null) {
				Log.d(TAG, line);
				if(line.split(":")[0].equals("PORT"))
					port = Integer.valueOf(line.split(":")[1].trim());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			
		}
		
		return port;
	}
	
	@SuppressWarnings("resource")
	public int getNatPortServer() {
		int port = 0;
		
		try {
			FileReader filereader = new FileReader("/sdcard/Android/data/com.net.wifimanagedsdn/ip_config");
			BufferedReader buffreader = new BufferedReader(filereader);
			String line;
			while((line = buffreader.readLine()) != null) {
				Log.d(TAG, line);
				if(line.split(":")[0].equals("NAT_PORT"))
					port = Integer.valueOf(line.split(":")[1].trim());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			
		}
		
		return port;
	}
	
	@SuppressWarnings("resource")
	public String getSenderId() {
		String id = null;
		
		try {
			FileReader filereader = new FileReader("/sdcard/Android/data/com.net.wifimanagedsdn/gcm");
			BufferedReader buffreader = new BufferedReader(filereader);
			String line;
			while((line = buffreader.readLine()) != null) {
				Log.d(TAG, line);
				if(line.contains("SENDER_ID"))
					id = line.split(":")[1].trim();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			
		}
		
		return id;
	}
	
	@SuppressWarnings("resource")
	public void getApList() {
		Log.d(TAG, "getApList start=====================>");
		try {
			FileReader filereader = new FileReader("/sdcard/Android/data/com.net.wifimanagedsdn/wifi_list");
			BufferedReader buffreader = new BufferedReader(filereader);
			String line;
			while((line = buffreader.readLine()) != null) {
				Log.d(TAG, line);
				String ssid = line.split(",")[0].replace("SSID:", "").trim();
				Log.d(TAG, "SSID=========" + ssid + "================");
				String mac = line.split(",")[1].replace("MAC:", "").trim();
				mac = mac.toUpperCase();
				Log.d(TAG, "Mac=========" + mac + "================");
				if((!ssid.equals("")) && (!mac.equals(""))) {
					Wifi wifi = Sdn_Service.db.getWifi(mac);				
					
					if(wifi == null)
						Sdn_Service.db.addWifi(new Wifi(ssid, mac));
					else {
						Log.d(TAG, "get ID=========" + wifi.getID() + "================");
						Log.d(TAG, "get SSID=========" + wifi.getSSID() + "================");
						Log.d(TAG, "get Mac=========" + wifi.getMac() + "================");
					}
				}
			}
		} catch (FileNotFoundException e) {
			Log.d(TAG, "getApList FileNotFoundException=====================>");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(TAG, "getApList IOException=====================>");
		}
		Log.d(TAG, "getApList end=====================>");
	}
}
