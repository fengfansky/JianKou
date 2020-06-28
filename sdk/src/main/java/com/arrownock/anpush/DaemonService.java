package com.arrownock.anpush;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.arrownock.internal.push.DaemonConfigurations;
import com.arrownock.internal.push.IDaemonStrategy;

public class DaemonService extends Service {
	public final static String LOG_TAG = DaemonService.class.getName();
	private BroadcastReceiver daemonReceiver = null;
	private DaemonConfigurations mConfigurations = null;
	private boolean isCreated = false;
	
	@Override
	public void onCreate() {
		Log.d(LOG_TAG, "Creating daemon service...");
		if(!isCreated) {
			if(daemonReceiver == null) {
				daemonReceiver = new DaemonReceiver();
			}
			this.registerReceiver(daemonReceiver, new IntentFilter());
				
			String persistProcess = getPackageName() + ":push";
			String daemonProcess = getPackageName() + ":daemon";
			if(mConfigurations == null) {
				DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(persistProcess, PushService.class.getCanonicalName(), PushReceiver.class.getCanonicalName());
				DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(daemonProcess, DaemonService.class.getCanonicalName(), DaemonReceiver.class.getCanonicalName());
				mConfigurations = new DaemonConfigurations(configuration1, configuration2, null);
			}
			IDaemonStrategy.Fetcher.fetchStrategy().onDaemonAssistantCreate(this.getBaseContext(), mConfigurations);
			isCreated = true;
		}
		super.onCreate();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
