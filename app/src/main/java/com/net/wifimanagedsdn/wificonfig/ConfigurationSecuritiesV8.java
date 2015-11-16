package com.net.wifimanagedsdn.wificonfig;

import android.annotation.SuppressLint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.util.Log;

public class ConfigurationSecuritiesV8 extends ConfigurationSecurities {

	public static final int SECURITY_NONE = 0;
	public static final int SECURITY_WEP = 1;
	public static final int SECURITY_PSK = 2;
	public static final int SECURITY_EAP = 3;

	enum PskType {
		UNKNOWN, WPA, WPA2, WPA_WPA2
	}

	private static final String TAG = "ConfigurationSecuritiesV14";

	public static int getSecurity(WifiConfiguration config) {
		if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
			return SECURITY_PSK;
		}
		if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
			return SECURITY_EAP;
		}
		
		return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
	}

	public static int getSecurity(ScanResult result) {
		if (result.capabilities.contains("WEP")) {
			return SECURITY_WEP;
		} else if (result.capabilities.contains("PSK")) {
			return SECURITY_PSK;
		} else if (result.capabilities.contains("EAP")) {
			return SECURITY_EAP;
		}		
		
		return SECURITY_NONE;
	}
	
	public int getSecurity(String sec) {
		if (sec.contains("WEP")) {
			return SECURITY_WEP;
		} else if (sec.contains("PSK")) {
			return SECURITY_PSK;
		} else if (sec.contains("EAP")) {
			return SECURITY_EAP;
		}		
		
		return SECURITY_NONE;
	}

	@Override
	public String getWifiConfigurationSecurity(WifiConfiguration wifiConfig) {
		return String.valueOf(getSecurity(wifiConfig));
	}

	@Override
	public String getScanResultSecurity(ScanResult scanResult) {
		return String.valueOf(getSecurity(scanResult));
	}

	@Override
	public void setupSecurity(WifiConfiguration config, String security, String password) {
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		Log.d(TAG, "security: " + security);

		final int sec = security == null ? SECURITY_NONE : Integer.valueOf(security);
		final int passwordLen = password == null ? 0 : password.length();
		switch (sec) {
			case SECURITY_NONE:
				config.allowedKeyManagement.set(KeyMgmt.NONE);
				break;

			case SECURITY_WEP:
				config.allowedKeyManagement.set(KeyMgmt.NONE);
				config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
				config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
				if (passwordLen != 0) {
					// WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
					if ((passwordLen == 10 || passwordLen == 26 || passwordLen == 58) && password.matches("[0-9A-Fa-f]*")) {
						config.wepKeys[0] = password;
					} else {
						config.wepKeys[0] = '"' + password + '"';
					}
				}
				break;

			case SECURITY_PSK:
				config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
				if (passwordLen != 0) {
					if (password.matches("[0-9A-Fa-f]{64}")) {
						config.preSharedKey = password;
					} else {
						config.preSharedKey = '\"' + password + '\"';
					}
				}
				Log.d(TAG, "config.preSharedKey " + config.preSharedKey);
				break;

			case SECURITY_EAP:
				config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
				config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
				break;

			default:
				Log.d(TAG, "Invalid security type: " + sec);
		}
	}
	
	@Override
	public void setupSecurity(WifiConfiguration config, String security,
			String user, String password) {
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		Log.d(TAG, "security: " + security);

		final int sec = security == null ? SECURITY_NONE : Integer.valueOf(security);
		final int passwordLen = password == null ? 0 : password.length();
		switch (sec) {
			case SECURITY_NONE:
				config.allowedKeyManagement.set(KeyMgmt.NONE);
				break;

			case SECURITY_WEP:
				config.allowedKeyManagement.set(KeyMgmt.NONE);
				config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
				config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
				if (passwordLen != 0) {
					// WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
					if ((passwordLen == 10 || passwordLen == 26 || passwordLen == 58) && password.matches("[0-9A-Fa-f]*")) {
						config.wepKeys[0] = password;
					} else {
						config.wepKeys[0] = '"' + password + '"';
					}
				}
				break;

			case SECURITY_PSK:
				config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
				if (passwordLen != 0) {
					if (password.matches("[0-9A-Fa-f]{64}")) {
						config.preSharedKey = password;
					} else {
						config.preSharedKey = '\"' + password + '\"';
					}
				}
				Log.d(TAG, "config.preSharedKey " + config.preSharedKey);
				break;

			case SECURITY_EAP:
				config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
				config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
				WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
				enterpriseConfig.setIdentity(user);
//				Log.d(TAG, "enterpriseConfig id: " + enterpriseConfig.getIdentity());
				enterpriseConfig.setPassword(password);
//				Log.d(TAG, "enterpriseConfig passwd: " + enterpriseConfig.getPassword());
				enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
//				Log.d(TAG, "enterpriseConfig EapMethod: " + enterpriseConfig.getEapMethod());
//				enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);
				config.enterpriseConfig = enterpriseConfig;
				break;

			default:
				Log.d(TAG, "Invalid security type: " + sec);
		}
	}

	private static PskType getPskType(ScanResult result) {
		boolean wpa = result.capabilities.contains("WPA-PSK");
		boolean wpa2 = result.capabilities.contains("WPA2-PSK");
		if (wpa2 && wpa) {
			return PskType.WPA_WPA2;
		} else if (wpa2) {
			return PskType.WPA2;
		} else if (wpa) {
			return PskType.WPA;
		} else {
			Log.d(TAG, "Received abnormal flag string: " + result.capabilities);
			return PskType.UNKNOWN;
		}
	}

	@Override
	public String getDisplaySecirityString(final ScanResult scanResult) {
		final int security = getSecurity(scanResult);
			if (security == SECURITY_PSK) {
			switch (getPskType(scanResult)) {
				case WPA:
					return "WPA";
				case WPA_WPA2:
				case WPA2:
					return "WPA2";
				default:
					return "?";
			}
		} else {
			switch (security) {
				case SECURITY_NONE:
					return "OPEN";
				case SECURITY_WEP:
					return "WEP";
				case SECURITY_EAP:
					return "EAP";
			}
		}

		return "?";
	}

	@Override
	public boolean isOpenNetwork(String security) {
		return String.valueOf(SECURITY_NONE).equals(security);
	}
}