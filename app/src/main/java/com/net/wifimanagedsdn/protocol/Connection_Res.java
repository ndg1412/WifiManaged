package com.net.wifimanagedsdn.protocol;

public class Connection_Res {
	public static byte[] aSop = new byte[2];
	public static byte bControlType;
	public static byte bPacketType;
	public int iType;
	public byte[] aLeng = new byte[4];
//	public byte[] aChecksum = new byte[2];
//	public byte[] aEop = new byte[2];
	
	public Connection_Res(byte[] in) {
		System.arraycopy(in, 0, aSop, 0, 2);
		bControlType = in[2];
		bPacketType = in[3];
		if((bPacketType & 0xFF) == Packet_Type.CONNECTION_RESP_FAIL) {
			iType = in[4] & 0xFF;
		}
		System.arraycopy(in, 4, aLeng, 0, 4);
	}
	
	public boolean isSuccess() {
		if(((aSop[0] & 0xFF) == 0xFE) && ((aSop[1] & 0xFF) == 0x02) &&
				(bControlType == 1) && ((bPacketType & 0xFF) == Packet_Type.CONNECTION_RESP_SUCCESS))
			return true;
		else
			return false;
	}
	
	public boolean isFail() {
		if(((aSop[0] & 0xFF) == 0xFE) && ((aSop[1] & 0xFF) == 0x02) &&
				(bControlType == 1) && ((bPacketType & 0xFF) == Packet_Type.CONNECTION_RESP_FAIL))
			return true;
		else
			return false;
	}
	
	public int getTypeFail() {
		return iType;
	}
}
