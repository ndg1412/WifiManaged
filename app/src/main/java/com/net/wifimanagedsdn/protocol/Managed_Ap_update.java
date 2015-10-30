package com.net.wifimanagedsdn.protocol;

import android.util.Log;

public class Managed_Ap_update {
	private static final String TAG = "Managed_Ap_update";
	public static byte[] aSop = new byte[2];
	public static byte bControlType;
	public static byte bPacketType;
	public int iLeng;
	public int iIndex;
	public Ap_type[] atAp;
	
	public Managed_Ap_update(byte[] in) {
		System.arraycopy(in, 0, aSop, 0, 2);
		bControlType = in[2];
		bPacketType = in[3];
		Log.d(TAG, "bPacketType: " + String.format("%02X", bPacketType));
		byte[] aLeng = new byte[4];
		System.arraycopy(in, 4, aLeng, 0, 4);
		iLeng = byteArrayToInt(aLeng);
		Log.d(TAG, "leng of data: " + iLeng);
		iIndex = in[8] & 0xFF;
		Log.d(TAG, "index packet: " + iIndex);
		int leng = (iLeng - 1)/38;
		atAp = new Ap_type[leng];
		for(int i = 0; i < leng; i++) {
			byte[] tmp = new byte[38];
			System.arraycopy(in, 9 + 38*i, tmp, 0, 38);
			atAp[i] = new Ap_type(tmp);
		}
	}
	
	public static int byteArrayToInt(byte[] b) {
		return   b[3] & 0xFF |
			(b[2] & 0xFF) << 8 |
			(b[1] & 0xFF) << 16 |
			(b[0] & 0xFF) << 24;
	}
	
	public int getNumberAp() {
		return (iLeng - 1)/38;
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
	
	public int getIndexPacket() {
		return iIndex;
	}
	
	
	public Ap_type[] getAp_type() {
		return atAp;
	}
	
	public boolean isSuccess() {
		if((bPacketType & 0xFF) == Packet_Type.MANAGED_AP_RESP_UPDATE)
			return true;
		else
			return false;
	}
}
