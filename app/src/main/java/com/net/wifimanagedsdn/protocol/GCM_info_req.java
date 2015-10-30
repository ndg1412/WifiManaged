package com.net.wifimanagedsdn.protocol;


public class GCM_info_req {

	public static byte[] aSop = new byte[] {(byte)0xFE, 0x02};
	public static byte bControlType = 0x00;
	public static byte bPacketType = (byte)Packet_Type.GCM_INFO_REQ;
	public byte[] aLeng = new byte[4];
	public byte[] aRegId = null;
	
	public GCM_info_req(String id) {
		aLeng = intToByteArray(id.length());
		aRegId = new byte[id.length()];
		System.arraycopy(id.getBytes(), 0, aRegId, 0, id.length());
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
		byte[] tmp = new byte[8 + aRegId.length];
		System.arraycopy(aSop, 0, tmp, 0, 2);
		tmp[2] = bControlType;
		tmp[3] = bPacketType;
		System.arraycopy(aLeng, 0, tmp, 4, 4);
		System.arraycopy(aRegId, 0, tmp, 8, aRegId.length);
		
		return tmp;
	}
}
