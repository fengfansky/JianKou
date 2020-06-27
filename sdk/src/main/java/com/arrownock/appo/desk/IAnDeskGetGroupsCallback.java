package com.arrownock.appo.desk;

import com.arrownock.exception.ArrownockException;

import java.util.List;

public interface IAnDeskGetGroupsCallback {
	void onSuccess(List<Group> groups);
	void onFailure(ArrownockException e);
}
