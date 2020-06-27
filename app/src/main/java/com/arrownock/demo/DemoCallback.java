package com.arrownock.demo;

import android.os.Handler;
import android.util.Log;

import com.arrownock.push.AnPushCallbackAdapter;
import com.arrownock.exception.ArrownockException;

public class DemoCallback extends AnPushCallbackAdapter {
	public final static String LOG_TAG = DemoCallback.class.getName();
	private Handler activityHandler = new Handler();
	private String mDeviceID;
	
	@Override
	public void register(boolean err, final String anid, ArrownockException exception) {
		if (!err) {
			activityHandler.post(new Runnable(){
				public void run() {
				mDeviceID = anid;
				ReceiveActivity.anidView.setText(mDeviceID);
				}
			});
			Log.d(LOG_TAG, "anid = " + anid);
		} else {
			Log.e(LOG_TAG, exception.getMessage());
		}
	}
}
