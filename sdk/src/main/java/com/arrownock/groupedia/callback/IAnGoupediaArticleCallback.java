package com.arrownock.groupedia.callback;

import com.arrownock.exception.ArrownockException;
import com.arrownock.groupedia.model.Article;

public interface IAnGoupediaArticleCallback {
    void onSuccess(Article article);

    void onFailure(ArrownockException e);
}