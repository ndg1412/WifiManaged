package com.net.wifimanagedsdn.protocol;

import android.util.Log;

public class Managed_Ap_res {
	private static final String TAG = "Managed_Ap_res";
	public static byte[] aSop = new byte[2];
	public static byte bControlType;
	public static byte bPacketType;
	public int iLeng;
	public long lDBVersion;
	public int iNumberPacket;
	public int iAp;
	
	public Managed_Ap_res(byte[] in) {
		System.arraycopy(in, 0, aSop, 0, 2);
		bControlType = in[2];
		bPacketType = in[3];
		Log.d(TAG, "bPacketType: " + String.format("%02X", bPacketType));
		byte[] aLeng = new byte[4];
		System.arraycopy(in, 4, aLeng, 0, 4);
		iLeng = byteArrayToInt(aLeng);
		Log.d(TAG, "leng of data: " + iLeng);
		byte[] aVersion = new byte[8];
		System.arraycopy(in, 8, aVersion, 0, 8);
		lDBVersion = byteArrayToLong(aVersion);
		Log.d(TAG, "version: " + lDBVersion);
		switch(bPacketType & 0xFF) {
			case Packet_Type.MANAGED_AP_RESP_NONE:
				break;				
			case Packet_Type.MANAGED_AP_RESP_LIST:
				iNumberPacket = in[16] & 0xFF;
				Log.d(TAG, "number packet is: " + iNumberPacket);
				byte[] aAp = new byte[2];
				System.arraycopy(in, 17, aAp, 0, 2);
				iAp = byteArrayToshort(aAp);
				break;
			default:
				break;
		}
	}
	
	public static int byteArrayToshort(byte[] b) {
		return  (b[1] & 0xFF) << 0 |
			(b[0] & 0xFF) << 8;
	}
	
	public static int byteArrayToInt(byte[] b) {
		return   b[3] & 0xFF |
			(b[2] & 0xFF) << 8 |
			(b[1] & 0xFF) << 16 |
			(b[0] & 0xFF) << 24;
	}
	
	public static int byteArrayToLong(byte[] b) {
		return   b[7] & 0xFF |
			(b[6] & 0xFF) << 8 |
			(b[5] & 0xFF) << 16 |
			(b[4] & 0xFF) << 24 |
			(b[3] & 0xFF) << 32 |
			(b[2] & 0xFF) << 40 |
			(b[1] & 0xFF) << 48 |
			(b[0] & 0xFF) << 56;
	}
	
	public int getPacketType() {
		return (int)(bPacketType & 0xFF);
	}
	
	public int getNumberPacket() {
		return iNumberPacket;
	}
	
	public int getNumberAp() {
		return iAp;
	}
	
	public long getVersion() {
		return lDBVersion;
	}
	
}
