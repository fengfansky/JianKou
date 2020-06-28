package com.arrownock.anpush;

import com.arrownock.exception.ArrownockException;

public interface IAnPushRegisterAnIdCallback {
	void onSuccess();
	void onError(ArrownockException e, int count, String type);
}
