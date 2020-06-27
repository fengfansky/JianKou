package com.arrownock.internal.push;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.arrownock.push.ANBase64;
import com.arrownock.push.PushService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

/**
 * the strategy in android API below 21.
 */
public class DaemonStrategyUnder21 implements IDaemonStrategy{
	private final String BINARY_DEST_DIR_NAME 	= "bin";
	private final String BINARY_FILE_NAME		= "daemon";
	
	private AlarmManager 			mAlarmManager;
	private PendingIntent			mPendingIntent;
	
	@Override
	public boolean onInitialization(Context context) {
		return installBinary(context);
	}

	@Override
	public void onPersistentCreate(final Context context, final DaemonConfigurations configs) {
		initAlarm(context, configs.DAEMON_ASSISTANT_CONFIG.SERVICE_NAME);
		Thread t = new Thread(){
			public void run() {
				File binaryFile = new File(context.getDir(BINARY_DEST_DIR_NAME, Context.MODE_PRIVATE), BINARY_FILE_NAME);
				new NativeAPIs().runDaemon(
						context.getPackageName(), 
						configs.DAEMON_ASSISTANT_CONFIG.SERVICE_NAME,
						binaryFile.getAbsolutePath());
			};
		};
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
		
		if(configs != null && configs.LISTENER != null){
			configs.LISTENER.onPersistentStart(context);
		}
	}

	@Override
	public void onDaemonAssistantCreate(Context context, DaemonConfigurations configs) {
		Intent intent = new Intent();
		ComponentName component = new ComponentName(context.getPackageName(), configs.PERSISTENT_CONFIG.SERVICE_NAME);
		intent.setComponent(component);
		intent.setAction(PushService.ACTION_CHECK_START);
		context.startService(intent);
		if(configs != null && configs.LISTENER != null){
			configs.LISTENER.onWatchDaemonDaed();
		}
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	
	@Override
	public void onDaemonDead() {
		mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 100, mPendingIntent);
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
            mPendingIntent = PendingIntent.getService(context, 0, intent, 0);
        }
        mAlarmManager.cancel(mPendingIntent);
	}
	
	
	private boolean installBinary(Context context){
		int binaryType = -1;
		String abi = Build.CPU_ABI;
		if (abi.startsWith("armeabi-v7a")) {
			binaryType = 2;  // armeabi-v7a
		}else if(abi.startsWith("x86")) {
			binaryType = 3;  // x86
		}else{
			binaryType = 1;  // armeabi
		}
		return install(context, BINARY_DEST_DIR_NAME, binaryType, BINARY_FILE_NAME);
	}
	
	
	private boolean install(Context context, String destDirName, int binaryType, String filename) {
		File file = new File(context.getDir(destDirName, Context.MODE_PRIVATE), filename);
		if (file.exists()) {
			return true;
		}
		try {
			copyAssets(context, binaryType, file, "700");
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private void copyAssets(Context context, int binaryType, File file, String mode) throws IOException, InterruptedException {
		byte[] binary = null;
		switch(binaryType) {
		case 1: // armeabi
			binary = ANBase64.decode(DaemonBinaries.DAEMON_ARMEABI);
			break;
		case 2: // armeabi-v7a
			binary = ANBase64.decode(DaemonBinaries.DAEMON_ARMEABI_V7A);
			break;
		case 3: // x86
			binary = ANBase64.decode(DaemonBinaries.DAEMON_X86);
			break;
		}
		final InputStream is = new ByteArrayInputStream(binary);
		copyFile(file, is, mode);
	}
	
	private void copyFile(File file, InputStream is, String mode) throws IOException, InterruptedException {
		if(!file.getParentFile().exists()){
			file.getParentFile().mkdirs();
		}
		final String abspath = file.getAbsolutePath();
		final FileOutputStream out = new FileOutputStream(file);
		byte buf[] = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.close();
		is.close();
		Runtime.getRuntime().exec("chmod " + mode + " " + abspath).waitFor();
	}
}
