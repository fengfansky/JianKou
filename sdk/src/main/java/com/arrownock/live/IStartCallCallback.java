package com.arrownock.live;

import com.arrownock.exception.ArrownockException;

public interface IStartCallCallback {
	void onReady(String sessionId);
	void onFailure(ArrownockException e);
}
