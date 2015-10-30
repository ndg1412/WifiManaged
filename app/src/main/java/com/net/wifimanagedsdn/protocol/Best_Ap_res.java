package com.net.wifimanagedsdn.protocol;


public class Best_Ap_res {
	public static final String TAG = "Best_Ap_res";
	public static byte[] aSop = new byte[2];
	public static byte bControlType;
	public static byte bPacketType;
	int iErrorType;
	public int iLeng;
	public byte[] aData = new byte[38];
//	public byte[] aChecksum = new byte[2];
//	public byte[] aEop = new byte[2];
	
	public Best_Ap_res(byte[] in) {
		System.arraycopy(in, 0, aSop, 0, 2);
		bControlType = in[2];
		bPacketType = in[3];
		int iPackType = bPacketType & 0xFF;
		switch(iPackType) {
			case Packet_Type.BEST_AP_RESP:
				byte[] aLeng = new byte[4];
				System.arraycopy(in, 4, aLeng, 0, 4);
				iLeng = byteArrayToInt(aLeng);
				System.arraycopy(in, 8, aData, 0, 38);
				break;
			case Packet_Type.BEST_AP_FAIL:
				iErrorType = in[4] & 0xFF;
				break;
		}
	}
	
	public static int byteArrayToInt(byte[] b) {
		return   b[3] & 0xFF |
			(b[2] & 0xFF) << 8 |
			(b[1] & 0xFF) << 16 |
			(b[0] & 0xFF) << 24;
	}
	
	public boolean isSuccess() {
		if((bPacketType & 0xFF) == Packet_Type.BEST_AP_RESP)
			return true;
		else
			return false;
	}
	
	public boolean isFail() {
		if((bPacketType & 0xFF) == Packet_Type.BEST_AP_FAIL)
			return true;
		else
			return false;
	}
	
	public String getSSID() {
		byte[] bSsid = new byte[32];
		System.arraycopy(aData, 0, bSsid, 0, 32);
		int size = 0;
		while(size < bSsid.length) {
			if(bSsid[size] == 0)
				break;
			size++;
		}
		return new String(bSsid, 0, size);
	}
	
	public String getMac() {
		byte[] bMac = new byte[6];
		System.arraycopy(aData, 32, bMac, 0, 6);
		StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bMac.length; i++) {
        	sb.append(String.format("%02X%s", bMac[i] & 0xFF, (i < bMac.length - 1) ? ":" : ""));        
        }
        
        return sb.toString();
	}
	
	public int getErrorType() {
		return iErrorType;
	}
}
