package com.net.wifimanagedsdn.protocol;

import java.util.Arrays;


public class Connection_Req {

	public static byte[] aSop = new byte[] {(byte)0xFE, 0x02};
	public static byte bControlType = 0x00;
	public static byte bPacketType = (byte)Packet_Type.CONNECTION_REQ;
	public byte[] aLeng = new byte[4];
	public byte[] aData = new byte[96];
	public byte[] aMac = new byte[6];
//	public byte[] aChecksum = new byte[2];
//	public byte[] aEop = new byte[] {(byte)0xFA, 0x0D};
	
	public Connection_Req(String phone, String user, String pass, String mac) {
		aLeng = intToByteArray(102);
		byte[] bUser = new byte[32];
		byte[] bPhone = new byte[32];
		byte[] bPass = new byte[32];
		Arrays.fill(bUser, (byte) 0x00);
		Arrays.fill(bPhone, (byte) 0x00);
		Arrays.fill(bPass, (byte) 0x00);
		System.arraycopy(phone.getBytes(), 0, bPhone, 0, phone.length());
		System.arraycopy(user.getBytes(), 0, bUser, 0, user.length());
		System.arraycopy(pass.getBytes(), 0, bPass, 0, pass.length());
		System.arraycopy(bPhone, 0, aData, 0, 32);
		System.arraycopy(bUser, 0, aData, 32, 32);
		System.arraycopy(bPass, 0, aData, 64, 32);
		int[] tmp = new int[6];
		String[] strMac = mac.split(":");
		for(int i = 0; i < 6; i++) {
			tmp[i] = Integer.parseInt(strMac[i], 16);
			aMac[i] = (byte)tmp[i];
		}
	}
	public static int byteArrayToInt(byte[] b) {
		return   b[3] & 0xFF |
			(b[2] & 0xFF) << 8 |
			(b[1] & 0xFF) << 16 |
			(b[0] & 0xFF) << 24;
	}

	public static byte[] intToByteArray(int a) {
		return new byte[] {
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
		byte[] tmp = new byte[110];
		System.arraycopy(aSop, 0, tmp, 0, 2);
		tmp[2] = bControlType;
		tmp[3] = bPacketType;
		System.arraycopy(aLeng, 0, tmp, 4, 4);
		System.arraycopy(aData, 0, tmp, 8, 96);
		System.arraycopy(aMac, 0, tmp, 104, 6);
		
		return tmp;
	}
}
