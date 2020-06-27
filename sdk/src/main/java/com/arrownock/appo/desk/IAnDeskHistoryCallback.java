package com.arrownock.appo.desk;

import java.util.List;

import com.arrownock.exception.ArrownockException;

public interface IAnDeskHistoryCallback {
	void onSuccess(List<AnDeskMessage> messages, int count);
	void onError(ArrownockException e);
}
