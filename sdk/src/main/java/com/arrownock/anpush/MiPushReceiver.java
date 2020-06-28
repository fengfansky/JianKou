package com.arrownock.anpush;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.arrownock.exception.ArrownockException;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MiPushReceiver extends PushMessageReceiver{
	public static final String LOG_TAG = MiPushReceiver.class.getName();
    public static final String MSG_ARRIVAL = PushService.ACTION_MSG_ARRIVAL;
 	static final String PENDING_PUSH_STORAGE = "com.arrownock.push.PENDING_PUSH_STORAGE";
 	static final String PENDING_PUSH_NOTIFICATIONS = "com.arrownock.push.PENDING_PUSH_NOTIFICATIONS";
 	
	@Override
	public void onCommandResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                String regId = cmdArg1;
                try {
					AnPush.getInstance(context).registerMiPushDevice(context, regId);
				} catch (ArrownockException e) {
					// mute
				}
            }
        }
	}

	@Override
	public void onReceiveMessage(Context context, MiPushMessage message) {
		String payload = message.getContent();
		if (payload == null || payload.equals(""))
			return;
		
		JSONObject payloadJson = null;
		try {
			payloadJson = new JSONObject(payload);
		} catch (JSONException ex) {
			// Not a Json
			payloadJson = new JSONObject();
			JSONObject androidJsonPart = new JSONObject();
			try {
				androidJsonPart.put("alert", payload);
				payloadJson.put("android", androidJsonPart);
			} catch (JSONException ex1) {
			}
		}
		
		/*
		SharedPreferences pref = context.getApplicationContext().getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE);
		boolean status = pref.getBoolean(AnPush.MIPUSH_SERVICE_STATUS, false);
		if(status) {
			// Show a notification
			Log.d(LOG_TAG, "New message arrived. Creating wakelock.");
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "anPush");
	        wl.acquire();
			
			Intent newIntent = new Intent();
			newIntent.setAction(MSG_ARRIVAL);
			newIntent.setPackage(context.getPackageName());
			newIntent.putExtra("payload", payloadJson.toString());
			context.sendBroadcast(newIntent);
	        
	        wl.release();
	        Log.d(LOG_TAG, "Releasing wakelock.");
		} else {
			storePendingNotifications(context.getApplicationContext(), payloadJson.toString());
		}
		*/
		Class<?> messageArrivalActivity = null;
		try {
			String messageArrivalActivityName = (String) context.getPackageManager().getReceiverInfo(new ComponentName(context, PushBroadcastReceiver.class), PackageManager.GET_META_DATA).metaData.get("com.arrownock.push.BroadcastReceiver.ArrivalActivity");
			messageArrivalActivity = Class.forName(messageArrivalActivityName);
			if(!Activity.class.isAssignableFrom(messageArrivalActivity)){
				Log.e(LOG_TAG, "The class "+ messageArrivalActivity.getName() +" is not a subclass of Activity");
				messageArrivalActivity = null;
			}
		} catch (Exception ex) {
			Log.w(LOG_TAG, "No proper class for arrived notification. Need an Activity subclass.");
		}
		
		if(messageArrivalActivity != null) {
			Intent newIntent = new Intent(context.getApplicationContext(), messageArrivalActivity);
			newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			newIntent.putExtra("payload", payloadJson.toString());
			context.getApplicationContext().startActivity(newIntent);
		}
	}
	/*
	private void storePendingNotifications(Context context, String payload) {
		SharedPreferences sp = context.getSharedPreferences(PENDING_PUSH_STORAGE, Context.MODE_PRIVATE);
		String storedPayloads = sp.getString(PENDING_PUSH_NOTIFICATIONS, null);
		JSONArray payloads = null;
		if(storedPayloads != null) {
			try {
				payloads = new JSONArray(storedPayloads);
			} catch(Exception e) {
				// parse old payloads failed, mute
			}
		}
		if(payloads == null) {
			payloads = new JSONArray();
		}
		payloads.put(payload);
		Editor editor = sp.edit();
		editor.putString(PENDING_PUSH_NOTIFICATIONS, payloads.toString());
		editor.commit();
	}
	*/
}
