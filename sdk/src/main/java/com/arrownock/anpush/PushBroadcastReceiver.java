package com.arrownock.anpush;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.arrownock.exception.ArrownockException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class PushBroadcastReceiver extends BroadcastReceiver{
	public final static String LOG_TAG = PushBroadcastReceiver.class.getName();
	
	protected final static String USER_PRESENT = "android.intent.action.USER_PRESENT";
	public final static String MSG_ARRIVAL = PushService.ACTION_MSG_ARRIVAL;

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent == null || context == null)
			return;
		
		if (Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(intent.getAction())){
			IAnPushUtility util = new AnPushUtility(context);
			try {
				util.scheduleStartStopConnection();
			} catch (ArrownockException e) {
				Log.w(LOG_TAG, "Cannot start AlarmManager for start/stop service");
			}
		}
		
		try {
			if(!AnPush.getInstance(context).isServiceEnabled())
				return;
		} catch (ArrownockException e) {
			return;
		}
		
		if (Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(intent.getAction())){
			PushService.actionStart(context);
		}
		
		if (intent.getAction().equals(USER_PRESENT)) {
		}
		
		if (intent.getAction().equals(MSG_ARRIVAL)){
			String payloadStr = intent.getStringExtra("payload");
			
			// Transit payload from String to JSONObject
			JSONObject payload = null;
			try {
				payload = new JSONObject(payloadStr);
			} catch (JSONException ex) {
			}
			showNotification(context, payload);
		}
	}
	
	/**
	 * Show Notification according to payload
	 * @param context Android Context from a receiver
	 * @param payload The push notification data
	 */
	public void showNotification(Context context, JSONObject payload){
		showNotification(context, payload, -1);
	}
	
	/**
	 * Show Notification according to payload
	 * @param context Android Context from a receiver
	 * @param payload The push notification data
	 * @param notificationId Noification ID to be defined. If value is -1, it will generate a random one
	 */
	public void showNotification(Context context, JSONObject payload, int notificationId){
		if (payload == null) {
			Log.e(LOG_TAG, "Payload is null!");
		}
		
		// Ensure payload is correct, and get needed information from received payload
		String alert = null;
		boolean vibrate = false;
		boolean collapse = false;
		long[] vibrateTag = new long[]{0,500};
		String sound = null;
		Uri soundUri = null;
		String title = null;
		String icon = null;
		int iconID = 0;
		int badge = 0;
		
		try {
			JSONObject androidPartJson = payload.getJSONObject("android");
			alert = androidPartJson.optString("alert", null);
			vibrate = androidPartJson.optBoolean("vibrate", false);
			sound = androidPartJson.optString("sound", null);
			title = androidPartJson.optString("title", null);
			icon = androidPartJson.optString("icon", null);
			badge = androidPartJson.optInt("badge", 0);
			collapse = androidPartJson.optBoolean("collapse", false);
		} catch (JSONException ex) {
			if(alert==null) alert = payload.toString();
		}

		// Prepare notification to show message
		if (title == null) {
			try {
				title = context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString();
			} catch (Exception ex) {
				title = "";
			}
		}
		
		if (icon != null) {
			try {
				iconID = context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
				// context.getResources().getDrawable(icon);
				// R.drawable.ic_dialog_info;
				if (iconID < 1)
					iconID = context.getApplicationInfo().icon;
			} catch (Exception ex) {
				iconID = context.getApplicationInfo().icon;
			}
		} else {
			iconID = context.getApplicationInfo().icon;
		}
		
		if (sound == null) {
		} else if (sound.startsWith("media:")) {
			String number = null;
			try {
				number = sound.substring(6);
			} catch (Exception ex) {
			}
			if (number == null) {
			} else {
				soundUri = Uri.parse("content://media/internal/audio/media/" + number);
				// n.sound = Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI,
				// "6");
			}
		} else if (sound.startsWith("sd:")) {
			String name = null;
			try {
				name = sound.substring(3);
			} catch (Exception ex) {
			}
			if (name == null) {
			} else {
				soundUri = Uri.parse("file://sdcard/" + name);
			}
		} else {
			String uriAddr = "android.resource://" + context.getPackageName() + "/raw/" + sound;
			soundUri = Uri.parse(uriAddr);
//			String uriAddr = getAndTransferFile(context, sound);
//			soundUri = Uri.parse("file://" + uriAddr);
		}
		
		Class<?> messageArrivalActivity = null;
		try {
			String messageArrivalActivityName = null;
			messageArrivalActivityName = (String) context.getPackageManager().getReceiverInfo(new ComponentName(context, this.getClass()), PackageManager.GET_META_DATA).metaData.get("com.arrownock.push.BroadcastReceiver.ArrivalActivity");
			messageArrivalActivity = Class.forName(messageArrivalActivityName);
			if(!Activity.class.isAssignableFrom(messageArrivalActivity)){
				Log.e(LOG_TAG, "The class "+ messageArrivalActivity.getName() +" is not a subclass of Activity");
				messageArrivalActivity = null;
			}
		} catch (Exception ex) {
			Log.w(LOG_TAG, "No proper class for arrived notification. Need an Activity subclass.");
		}
		
		Intent intent = null;
		if (messageArrivalActivity == null) {
			intent = new Intent();
		} else {
			intent = new Intent(context, messageArrivalActivity);
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("payload", payload.toString());
		PendingIntent pi = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = null;
		
		/*
		if (Build.VERSION.SDK_INT < 11) {
			// Use previous method to create a Notification instance
			//Log.d(LOG_TAG, "Use Notification.new");
			n = new Notification();

			n.flags |= Notification.FLAG_SHOW_LIGHTS;
			n.flags |= Notification.FLAG_AUTO_CANCEL;
			//n.defaults = Notification.DEFAULT_ALL;
			if(sound!=null && sound.equals("default"))
				n.defaults |= Notification.DEFAULT_SOUND;
			else
				n.sound = soundUri;
			n.when = System.currentTimeMillis();
			n.icon = iconID;
			if(badge>0) n.number = badge;
			if(vibrate) n.vibrate = vibrateTag;
			n.setLatestEventInfo(context, title, alert, pi);
		} else{
		*/
			// Use Notification.builder to create a Notification instance
			//Log.d(LOG_TAG, "Use Notification.Builder");
			Notification.Builder builder = new Notification.Builder(context);
			builder.setContentIntent(pi).setSmallIcon(iconID).setWhen(System.currentTimeMillis()).setAutoCancel(true).setContentTitle(title);
			//builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), iconID));
			if(badge>0)
				builder.setNumber(badge);
			if(alert!=null && !"".equals(alert))
				builder.setContentText(alert);
			if(sound!=null && sound.equals("default"))
				builder.setDefaults(Notification.DEFAULT_SOUND);
			else
				builder.setSound(soundUri);
			if(vibrate) builder.setVibrate(vibrateTag);
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				n = builder.build();
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				n = builder.getNotification();
			}
			
		//}
		int notifyId = 1;
		if(!collapse) {
			notifyId = (notificationId == -1 ? (int) System.currentTimeMillis() : notificationId);
		}
		notifManager.notify(notifyId, n);
	}
	
	private String getAndTransferFile(Context context, String filename) {
		AssetManager assetManager = context.getAssets();
		String destFolder = "/sdcard/arrownock/sound/"+ context.getPackageName() + "/";

		try {
			long fromFileSize = assetManager.openFd("sound/" + filename).getLength();
			long destFileSize = 0;
			File destFile = new File(destFolder + filename);
			if (!destFile.exists()) {
				new File(destFolder).mkdirs();
			} else {
				destFileSize = destFile.length();
			}

			if (fromFileSize != destFileSize) {
				InputStream in = null;
				OutputStream out = null;
				in = assetManager.open("sound/" + filename);

				String newFileName = destFolder + filename;
				out = new FileOutputStream(newFileName);

				byte[] buffer = new byte[1024];
				int read;
				while ((read = in.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.getMessage());
		}
		return destFolder + filename;
	}
}
