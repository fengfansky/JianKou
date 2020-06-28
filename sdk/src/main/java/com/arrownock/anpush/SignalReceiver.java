package com.arrownock.anpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class SignalReceiver extends BroadcastReceiver {
	protected static final String PUSH_SIGNAL = "com.arrownock.push.SIGNAL";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent != null) {
			String action = intent.getAction();
			Log.d(SignalReceiver.class.getName(), "Receiving signal: " + action);
			
			if(PUSH_SIGNAL.equalsIgnoreCase(action)) {
				// internal push commands
				int command = intent.getExtras().getInt("command");
				switch(command) {
				case 1:	// restart service
					PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		            WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "anPush");
		            wl.acquire();
					Log.d(SignalReceiver.class.getName(), "Got restart push service signal");
					PushService.actionCheckStart(context);
					wl.release();
					break;
				}
			} else if(ConnectivityManager.CONNECTIVITY_ACTION.equalsIgnoreCase(action)) {
				// network state changing
				Log.d(SignalReceiver.class.getName(), "Got push service signal: " + action);
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		        WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "anPush");
		        wl.acquire();
		        PushService.actionConnectivityChanged(context);
				wl.release();
			} else {
				Log.d(SignalReceiver.class.getName(), "Got push service signal: " + action);
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		        WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "anPush");
		        wl.acquire();
		        PushService.actionCheckStart(context);
				wl.release();
			}
		}
	}

}
