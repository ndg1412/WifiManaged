package com.net.wifimanagedsdn.protocol;

import android.util.Log;

@SuppressWarnings("unused")
public class Best_Ap_req {
	private static final String TAG = "Best_Ap_req";
	public static byte[] aSop = new byte[] {(byte)0xFE, 0x02};
	public static byte bControlType = 0x00;
	public static byte bPacketType = (byte)Packet_Type.BEST_AP_REQ;
	public byte[] aLeng = new byte[4];
	public Ap_type[] atData;
//	public byte[] aChecksum = new byte[2];
//	public byte[] aEop = new byte[] {(byte)0xFA, 0x0D};
	
	public Best_Ap_req(Ap_type[] tmp) {
		aLeng = intToByteArray(tmp.length * 38);
		atData = new Ap_type[tmp.length];
		atData = tmp;
//		short sCheck = 0;
//		sCheck ^= bControlType;
//		sCheck ^= bPacketType;
//		for(byte i :  aLeng) 
//			sCheck ^= i;
//		for(Ap_type in : atData)
//			for(byte b : in.getBytes())
//				sCheck ^= b;
//		aChecksum = shortToByteArray(sCheck);
	}
	
	public static byte[] intToByteArray(int a) {
		return new byte[] {
			(byte) ((a >> 24) & 0xFF),
			(byte) ((a >> 16) & 0xFF),   
			(byte) ((a >> 8) & 0xFF),   
			(byte) (a & 0xFF)
		};
	}
	
	public static int byteArrayToInt(byte[] b) {
		return   b[3] & 0xFF |
			(b[2] & 0xFF) << 8 |
			(b[1] & 0xFF) << 16 |
			(b[0] & 0xFF) << 24;
	}
	
	public byte[] shortToByteArray(short a) {
		return new byte[] {  
				(byte) ((a >> 8) & 0xFF),   
				(byte) (a & 0xFF)
			};
	}
	
	public byte[] getBytes() {
		byte[] tmp = new byte[8 + byteArrayToInt(aLeng)];
		System.arraycopy(aSop, 0, tmp, 0, 2);
		tmp[2] = bControlType;
		tmp[3] = bPacketType;
		System.arraycopy(aLeng, 0, tmp, 4, 4);
		for(int i = 0; i < atData.length; i++) {
			System.arraycopy(atData[i].getBytes(), 0, tmp, 8+38*i, 38);
//			Log.d(TAG, "mac: " + i + ": " + atData[i].getMac());
//			Log.d(TAG, "ssid: " + i + ": " + atData[i].getSSID());
		}
//		System.arraycopy(aChecksum, 0, tmp, 8+38*atData.length, 2);
//		System.arraycopy(aEop, 0, tmp, 10+38*atData.length, 2);
		
		return tmp;
	}
}
