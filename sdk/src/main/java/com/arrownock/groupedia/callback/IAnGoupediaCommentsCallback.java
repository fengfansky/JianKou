package com.arrownock.groupedia.callback;

import com.arrownock.exception.ArrownockException;
import com.arrownock.groupedia.model.Comment;

import java.util.List;

public interface IAnGoupediaCommentsCallback {
    void onSuccess(List<Comment> comments);

    void onFailure(ArrownockException e);
}