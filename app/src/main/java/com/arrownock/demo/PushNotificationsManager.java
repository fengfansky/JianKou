package com.arrownock.demo;

import android.content.Context;
import android.util.Log;

import com.arrownock.exception.ArrownockException;
import com.arrownock.anpush.AnPush;

/**
 * Push notifications management static class.
 * Push service can be started from here.
 */
public class PushNotificationsManager{
	public final static String LOG_TAG = PushNotificationsManager.class.getName();
	
	public static void startPush(final Context context) {
    	try {
			AnPush.getInstance(context).enable();
		} catch (ArrownockException ex) {
			Log.e(LOG_TAG, "Push Service occurs an exception.", ex);
		}
	}
	
	public static void stopPush(Context context) {
		try {
			AnPush.getInstance(context).disable();
		} catch (ArrownockException e1) {
			e1.printStackTrace();
		}
	}
}