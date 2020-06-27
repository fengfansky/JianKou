package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

public interface IAnIMPushNotificationSettingsCallback {
	void onSuccess();
	void onError(ArrownockException e);
}
