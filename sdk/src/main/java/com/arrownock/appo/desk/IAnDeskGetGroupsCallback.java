package com.arrownock.appo.desk;

import java.util.List;

import com.arrownock.exception.ArrownockException;

public interface IAnDeskGetGroupsCallback {
	void onSuccess(List<Group> groups);
	void onFailure(ArrownockException e);
}
