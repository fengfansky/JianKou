package com.arrownock.anpush;

import com.arrownock.exception.ArrownockException;

public interface IAnPushRegistrationCallback {
	void onSuccess(String anid);
	void onError(ArrownockException e);
}
