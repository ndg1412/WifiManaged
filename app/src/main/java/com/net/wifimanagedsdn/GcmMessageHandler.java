package com.net.wifimanagedsdn;

/**
 * Created by Giang on 11/12/2015.
 */
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.net.wifimanagedsdn.GCMBroadcastReceiver;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class GcmMessageHandler extends IntentService {

	private static final String TAG = "GcmMessageHandler";

	String mes;
	private Handler handler;
	public GcmMessageHandler() {
		super("GcmMessageHandler");
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		handler = new Handler();
	}
	@Override
	protected void onHandleIntent(Intent intent) {

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		mes = intent.getStringExtra("message");
		Log.d(TAG, "message = " + mes);

		GCMBroadcastReceiver.completeWakefulIntent(intent);

	}
}

