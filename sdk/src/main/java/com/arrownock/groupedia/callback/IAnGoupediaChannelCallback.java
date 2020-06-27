package com.arrownock.groupedia.callback;

import com.arrownock.exception.ArrownockException;
import com.arrownock.groupedia.model.Channel;

import java.util.List;

public interface IAnGoupediaChannelCallback {
    void onSuccess(List<Channel> channels);

    void onFailure(ArrownockException e);
}