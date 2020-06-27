package com.arrownock.groupedia.callback;

import com.arrownock.exception.ArrownockException;

public interface IAnGoupediaCommonCallback {
    void onSuccess();

    void onFailure(ArrownockException e);
}