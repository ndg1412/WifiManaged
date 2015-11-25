package com.net.wifimanagedsdn;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Message;
import android.util.Log;

import com.net.wifimanagedsdn.protocol.Connection_Req;
import com.net.wifimanagedsdn.protocol.Connection_Res;
import com.net.wifimanagedsdn.sqlite.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.io.InterruptedIOException;

/**
 * Created by Giang on 11/20/2015.
 */
public class KickoutThread extends Thread {
	private static final  String TAG = "KickoutThread";
	String strMac;
	Socket skKickout = null;
	InputStream isRecv = null;
	OutputStream osSend = null;

	public KickoutThread(String ip, int port, String mac) {
		try {
			strMac = mac;
			skKickout = new Socket(ip, port);
			skKickout.setTcpNoDelay(true);
			isRecv = skKickout.getInputStream();
			osSend = skKickout.getOutputStream();
		} catch (IOException e) {
			if (!isInterrupted()) {
				e.printStackTrace();
			} else {
				System.out.println("Interrupted");
			}
		}

	}

	public void interrupt() {
		super.interrupt();
		if(skKickout != null) {
			try {
				isRecv.close();

				skKickout.close();
				skKickout = null;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void run() {
		try {
			if(Send_Connection_Req() == 1) {
				byte[] bBuff = new byte[1024];
				int leng = isRecv.read(bBuff);
				if(leng > 0) {
					byte[] tmp = new byte[leng];
					System.arraycopy(bBuff, 0, tmp, 0, leng);

				}
			}
		} catch (InterruptedIOException e) {
			Thread.currentThread().interrupt();
			System.out.println("Interrupted via InterruptedIOException");
		} catch (IOException e) {
			if (!isInterrupted()) {
				e.printStackTrace();
			} else {
				System.out.println("Interrupted");
			}
		}
		System.out.println("Shutting down thread");
	}

	public int Send_Connection_Req() {
		int iReturn = -1;
		Log.d(TAG, "--------------------Send_Connection_Req start------------------------");
		Connection_Req req = new Connection_Req(MainActivity.dbUser.getPhone(), MainActivity.dbUser.getUser(),
				MainActivity.dbUser.getPass(), strMac);
		Connection_Res res = null;
		try {
			String strBuff = "";
			for(byte bb : req.getBytes())
				strBuff += String.format(" %02X", bb);
			Log.d(TAG, "connection req: " + strBuff);
			osSend.write(req.getBytes());
			osSend.flush();
			byte[] abRecv = new byte[1024];
			int leng = isRecv.read(abRecv);
			Log.d(TAG, "leng Connection_Res: " + leng);
			byte[] tmp = new byte[leng];
			System.arraycopy(abRecv, 0, tmp, 0, leng);
			strBuff = "";
			for(byte bb : tmp)
				strBuff += String.format(" %02X", bb);
			Log.d(TAG, "connection res: " + strBuff);
			res = new Connection_Res(tmp);
			if(res.isSuccess())
				iReturn = 1;
			else if(res.isFail())
				iReturn = res.getTypeFail();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		} catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
		Log.d(TAG, "--------------------Send_Connection_Req end------------------------");
		return iReturn;
	}
}
