package com.arrownock.push;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

@SuppressLint("NewApi")
public class NotificationService extends JobService {
	public final static String LOG_TAG = NotificationService.class.getName();

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LOG_TAG, "Notification service is starting...");
        startForeground(11, new Notification());
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
    	Log.d(LOG_TAG, "Notification service enter into background...");
        stopForeground(true);
        super.onDestroy();
    }

	@Override
	public boolean onStartJob(JobParameters params) {
		Log.d(LOG_TAG, "Starting keepalive job...");
		Intent intent = new Intent();
		ComponentName componentName = new ComponentName(getPackageName(), PushService.class.getName());
		intent.setComponent(componentName);
		intent.setAction(PushService.ACTION_CHECK_START);
		startService(intent);
		Log.d(LOG_TAG, "Job started");
		return false;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		Log.d(LOG_TAG, "Job ended");
		return false;
	}
}
