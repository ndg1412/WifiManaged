package com.net.wifimanagedsdn.protocol;

public class Checksum_Error {
	public static byte[] aSop = new byte[2];
	public static byte bControlType;
	public static byte bPacketType;
	public byte[] aLeng = new byte[4];
//	public byte[] aChecksum = new byte[2];
//	public byte[] aEop = new byte[2];
	
	public Checksum_Error(byte[] in) {
		System.arraycopy(in, 0, aSop, 0, 2);
		bControlType = in[2];
		bPacketType = in[3];
		System.arraycopy(in, 4, aLeng, 0, 4);
//		System.arraycopy(in, 8, aChecksum, 0, 2);
//		System.arraycopy(in, 10, aEop, 0, 2);
	}
	
	public boolean isSuccess() {
		if(((aSop[0] & 0xFF) == 0xFE) && ((aSop[1] & 0xFF) == 0x02) &&
//				((aEop[0] & 0xFF) == 0xFA) && ((aEop[1] & 0xFF) == 0x0D) &&
				(bControlType == 1) && ((bPacketType & 0xFF) == Packet_Type.CHECKSUM_ERROR))
			return true;
		else
			return false;
	}
}
