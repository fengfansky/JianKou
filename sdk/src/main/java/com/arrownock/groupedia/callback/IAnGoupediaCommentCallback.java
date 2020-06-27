package com.arrownock.groupedia.callback;

import com.arrownock.exception.ArrownockException;
import com.arrownock.groupedia.model.Comment;

public interface IAnGoupediaCommentCallback {
    void onSuccess(Comment comment);

    void onFailure(ArrownockException e);
}