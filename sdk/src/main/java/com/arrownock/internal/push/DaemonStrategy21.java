package com.arrownock.internal.push;

import java.io.File;
import java.io.IOException;

import com.arrownock.push.PushService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * the strategy in android API 21.
 * 
 * @author Mars
 *
 */
public class DaemonStrategy21 implements IDaemonStrategy{
	private final static String INDICATOR_DIR_NAME 					= "i";
	private final static String INDICATOR_PERSISTENT_FILENAME 		= "ip";
	private final static String INDICATOR_DAEMON_ASSISTANT_FILENAME = "id";
	private final static String OBSERVER_PERSISTENT_FILENAME		= "op";
	private final static String OBSERVER_DAEMON_ASSISTANT_FILENAME	= "od";
	
	private AlarmManager			mAlarmManager;
	private PendingIntent 			mPendingIntent;
	private DaemonConfigurations 	mConfigs;

	@Override
	public boolean onInitialization(Context context) {
		return initIndicators(context);
	}

	@Override
	public void onPersistentCreate(final Context context, DaemonConfigurations configs) {
		Intent intent = new Intent();
		ComponentName componentName = new ComponentName(context.getPackageName(), configs.DAEMON_ASSISTANT_CONFIG.SERVICE_NAME);
		intent.setComponent(componentName);
		context.startService(intent);
		initAlarm(context, configs.PERSISTENT_CONFIG.SERVICE_NAME);
		
		Thread t = new Thread(){
			@Override
			public void run() {
				File indicatorDir = context.getDir(INDICATOR_DIR_NAME, Context.MODE_PRIVATE);
				new NativeAPIs().startDaemon(
						new File(indicatorDir, INDICATOR_PERSISTENT_FILENAME).getAbsolutePath(), 
						new File(indicatorDir, INDICATOR_DAEMON_ASSISTANT_FILENAME).getAbsolutePath(), 
						new File(indicatorDir, OBSERVER_PERSISTENT_FILENAME).getAbsolutePath(),
						new File(indicatorDir, OBSERVER_DAEMON_ASSISTANT_FILENAME).getAbsolutePath());
			}
		};
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
		
		if(configs != null && configs.LISTENER != null){
			this.mConfigs = configs;
			configs.LISTENER.onPersistentStart(context);
		}
	}

	@Override
	public void onDaemonAssistantCreate(final Context context, DaemonConfigurations configs) {
		Intent intent = new Intent();
		ComponentName componentName = new ComponentName(context.getPackageName(), configs.PERSISTENT_CONFIG.SERVICE_NAME);
		intent.setComponent(componentName);
		intent.setAction(PushService.ACTION_CHECK_START);
		context.startService(intent);
		
		initAlarm(context, configs.PERSISTENT_CONFIG.SERVICE_NAME);
		
		Thread t = new Thread(){
			public void run() {
				File indicatorDir = context.getDir(INDICATOR_DIR_NAME, Context.MODE_PRIVATE);
				new NativeAPIs().startDaemon(
						new File(indicatorDir, INDICATOR_DAEMON_ASSISTANT_FILENAME).getAbsolutePath(), 
						new File(indicatorDir, INDICATOR_PERSISTENT_FILENAME).getAbsolutePath(), 
						new File(indicatorDir, OBSERVER_DAEMON_ASSISTANT_FILENAME).getAbsolutePath(),
						new File(indicatorDir, OBSERVER_PERSISTENT_FILENAME).getAbsolutePath());
			};
		};
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
		
		if(configs != null && configs.LISTENER != null){
			this.mConfigs = configs;
			configs.LISTENER.onDaemonAssistantStart(context);
		}
	}
	
	@Override
	public void onDaemonDead() {
		mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 100, mPendingIntent);
		
		if(mConfigs != null && mConfigs.LISTENER != null){
			mConfigs.LISTENER.onWatchDaemonDaed();
		}
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	
	private void initAlarm(Context context, String serviceName){
		if(mAlarmManager == null){
            mAlarmManager = ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE));
        }
        if(mPendingIntent == null){
            Intent intent = new Intent();
			ComponentName component = new ComponentName(context.getPackageName(), serviceName);
			intent.setComponent(component);
            intent.setFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
            intent.setAction(PushService.ACTION_CHECK_START);
            mPendingIntent = PendingIntent.getService(context, 0, intent, 0);
        }
        mAlarmManager.cancel(mPendingIntent);
	}
	
	
	private boolean initIndicators(Context context){
		File dirFile = context.getDir(INDICATOR_DIR_NAME, Context.MODE_PRIVATE);
		if(!dirFile.exists()){
			dirFile.mkdirs();
		}
		try {
			createNewFile(dirFile, INDICATOR_PERSISTENT_FILENAME);
			createNewFile(dirFile, INDICATOR_DAEMON_ASSISTANT_FILENAME);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	private void createNewFile(File dirFile, String fileName) throws IOException{
		File file = new File(dirFile, fileName);
		if(!file.exists()){
			file.createNewFile();
		}
	}

}
