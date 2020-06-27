package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

import java.util.List;

public interface IAnIMListBlacklistsCallback {
	void onSuccess(List<String> clients);
	void onError(ArrownockException e);
}
