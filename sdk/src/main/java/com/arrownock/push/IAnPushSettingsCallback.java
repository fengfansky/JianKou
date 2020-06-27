package com.arrownock.push;

import com.arrownock.exception.ArrownockException;

public interface IAnPushSettingsCallback {
	void onSuccess();
	void onError(ArrownockException e);
}
