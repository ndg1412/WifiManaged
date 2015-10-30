package com.net.wifimanagedsdn.protocol;

import java.util.Arrays;

import android.util.Log;

@SuppressWarnings("unused")
public class Ap_type {
	private static final String TAG = "Ap_type";
	byte[] aSsid = new byte[32];
	byte[] aMac = new byte[6];
	
	public Ap_type(String ssid, String mac) {
//		Log.d(TAG, "Ap_type ssid: " + ssid + "mac: " + mac);
		Arrays.fill(aSsid, (byte)0x00);
		Arrays.fill(aMac, (byte)0x00);
		System.arraycopy(ssid.getBytes(), 0, aSsid, 0, ssid.length());
		int[] tmp = new int[6];
		String[] strMac = mac.split(":");
		for(int i = 0; i < 6; i++) {
			tmp[i] = Integer.parseInt(strMac[i], 16);
			aMac[i] = (byte)tmp[i];
//			Log.d(TAG, "Mac " + i + ": " + String.format("%02X", tmp[i]));
		}
//		System.arraycopy(mac.getBytes(), 0, aSsid, 0, mac.length());
	}
	
	public Ap_type(byte[] in) {
		System.arraycopy(in, 0, aSsid, 0, 32);
		System.arraycopy(in, 32, aMac, 0, 6);		
	}
	
	public String getSSID() {
		int size = 0;
		while(size < aSsid.length) {
			if(aSsid[size] == 0)
				break;
			size++;
		}
		return new String(aSsid, 0, size);
	}
	
	public String getMac() {
		StringBuilder sb = new StringBuilder();
        for (int i = 0; i < aMac.length; i++) {
        	sb.append(String.format("%02X%s", aMac[i] & 0xFF, (i < aMac.length - 1) ? ":" : ""));        
        }
        return sb.toString();
	}
	public byte[] getBytes() {
		byte[] tmp = new byte[38];
		System.arraycopy(aSsid, 0, tmp, 0, 32);
		System.arraycopy(aMac, 0, tmp, 32, 6);
		return tmp;
	}
}
