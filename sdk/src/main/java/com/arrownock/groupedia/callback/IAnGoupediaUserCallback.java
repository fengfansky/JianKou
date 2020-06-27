package com.arrownock.groupedia.callback;

import com.arrownock.exception.ArrownockException;
import com.arrownock.groupedia.model.User;

public interface IAnGoupediaUserCallback {
    void onSuccess(User user);

    void onFailure(ArrownockException e);
}