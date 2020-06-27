package com.arrownock.groupedia.callback;

import java.util.List;

import com.arrownock.exception.ArrownockException;
import com.arrownock.groupedia.model.Comment;

public interface IAnGoupediaCommentsCallback {
    void onSuccess(List<Comment> comments);

    void onFailure(ArrownockException e);
}