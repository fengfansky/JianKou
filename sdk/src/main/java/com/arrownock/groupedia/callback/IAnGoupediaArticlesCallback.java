package com.arrownock.groupedia.callback;

import java.util.List;

import com.arrownock.exception.ArrownockException;
import com.arrownock.groupedia.model.Article;

public interface IAnGoupediaArticlesCallback {
    void onSuccess(List<Article> articles);

    void onFailure(ArrownockException e);
}