package com.net.wifimanagedsdn.protocol;

public class Packet_Type {
	public final static int CONNECTION_REQ			= 0xA1;
	public final static int BEST_AP_REQ				= 0xA5;
	public final static int MANAGED_AP_REQ			= 0xA8;
	public final static int CHECKSUM_ERROR			= 0xFF;
	public final static int CONNECTION_RESP_SUCCESS	= 0xA2;
	public final static int CONNECTION_RESP_FAIL		= 0xA3;
	public final static int BEST_AP_RESP = 0xA6;
	public final static int BEST_AP_FAIL = 0xA7;
	public final static int MANAGED_AP_RESP_NONE		= 0xA9;
	public final static int MANAGED_AP_RESP_LIST		= 0xAA;
	public final static int MANAGED_AP_RESP_UPDATE		= 0xAB;
	public final static int GCM_INFO_REQ				= 0xA4;
	public final static int GCM_DISCONNECT_REQ		= 0xAF;
	
	public final static int BEST_AP_ERROR_FULL = 0xE0;
	public final static int BEST_AP_ERROR_INVALID_LIST = 0xE1;
	public final static int BEST_AP_ERROR_UNKNOWN = 0xE2;
}
