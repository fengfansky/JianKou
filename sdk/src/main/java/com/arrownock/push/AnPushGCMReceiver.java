package com.arrownock.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class AnPushGCMReceiver extends BroadcastReceiver {
	public final static String MSG_ARRIVAL = PushService.ACTION_MSG_ARRIVAL;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(AnPushGCMReceiver.class.getName(), "Received GCM message");
		if(intent == null || context == null)
			return;
		
		if ("com.google.android.c2dm.intent.RECEIVE".equals(intent.getAction())){
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "anPushGCMReceiver");
            wl.acquire();
            
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
			String messageType = gcm.getMessageType(intent);
			
			if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				String payloadStr = intent.getStringExtra("payload");
				if(payloadStr != null) {
					// Transit payload from String to JSONObject
					JSONObject payload = null;
					try {
						payload = new JSONObject(payloadStr);
					} catch (Exception ex) {
						// Not a Json
						payload = new JSONObject();
						JSONObject androidJsonPart = new JSONObject();
						try {
							androidJsonPart.put("alert", payload);
							payload.put("android", androidJsonPart);
						} catch (JSONException ex1) {
						}
					}
					
					if(payload != null) {
						Intent newIntent = new Intent();
						newIntent.setAction(MSG_ARRIVAL);
						newIntent.setPackage(context.getPackageName());
						newIntent.putExtra("payload", payload.toString());
						context.sendBroadcast(newIntent);
					}
				}
			}
			
			wl.release();
		}
	}
}
