package com.net.wifimanagedsdn.protocol;

public class Managed_Ap_req {
	public static byte[] aSop = new byte[] {(byte)0xFE, 0x02};
	public static byte bControlType = 0x00;
	public static byte bPacketType = (byte)Packet_Type.MANAGED_AP_REQ;
	public byte[] aUser = new byte[32];
	public byte[] aPass = new byte[32];
	public int iLeng = 8;
	public long lDBVersion;
	
	public Managed_Ap_req(String user, String pass, long version) {
		lDBVersion = version;
		System.arraycopy(user.getBytes(), 0, aUser, 0, user.length());
		System.arraycopy(pass.getBytes(), 0, aPass, 0, pass.length());
	}
	
	public static byte[] intToByteArray(int a) {
		return new byte[] {
			(byte) ((a >> 24) & 0xFF),
			(byte) ((a >> 16) & 0xFF),   
			(byte) ((a >> 8) & 0xFF),   
			(byte) (a & 0xFF)
		};
	}
	
	public static byte[] longToByteArray(long a) {
		return new byte[] {
			(byte) ((a >> 56) & 0xFF),
			(byte) ((a >> 48) & 0xFF),   
			(byte) ((a >> 40) & 0xFF),   
			(byte) ((a >> 32) & 0xFF),
			(byte) ((a >> 24) & 0xFF),
			(byte) ((a >> 16) & 0xFF),   
			(byte) ((a >> 8) & 0xFF),   
			(byte) (a & 0xFF)
		};
	}
	
	public byte[] shortToByteArray(short a) {
		return new byte[] {  
				(byte) ((a >> 8) & 0xFF),   
				(byte) (a & 0xFF)
			};
	}
	
	public byte[] getBytes() {
		byte[] tmp = new byte[80];
		System.arraycopy(aSop, 0, tmp, 0, 2);
		tmp[2] = bControlType;
		tmp[3] = bPacketType;
		System.arraycopy(intToByteArray(iLeng), 0, tmp, 4, 4);
		System.arraycopy(aUser, 0, tmp, 8, 32);
		System.arraycopy(aPass, 0, tmp, 40, 32);
		System.arraycopy(longToByteArray(lDBVersion), 0, tmp, 72, 8);
//		System.arraycopy(aChecksum, 0, tmp, 16, 2);
//		System.arraycopy(aEop, 0, tmp, 18, 2);
		
		return tmp;
	}
}
