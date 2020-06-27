package com.arrownock.im.callback;

import java.util.List;

import com.arrownock.exception.ArrownockException;

public interface IAnIMListBlacklistsCallback {
	void onSuccess(List<String> clients);
	void onError(ArrownockException e);
}
