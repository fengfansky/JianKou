package com.arrownock.groupedia.callback;

import java.util.List;

import com.arrownock.exception.ArrownockException;
import com.arrownock.groupedia.model.Channel;

public interface IAnGoupediaChannelCallback {
    void onSuccess(List<Channel> channels);

    void onFailure(ArrownockException e);
}