package com.arrownock.anpush;

import com.arrownock.exception.ArrownockException;

public interface IAnPushSettingsCallback {
	void onSuccess();
	void onError(ArrownockException e);
}
