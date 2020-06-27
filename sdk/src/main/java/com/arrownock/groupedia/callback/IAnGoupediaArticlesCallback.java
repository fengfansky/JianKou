package com.arrownock.groupedia.callback;

import com.arrownock.exception.ArrownockException;
import com.arrownock.groupedia.model.Article;

import java.util.List;

public interface IAnGoupediaArticlesCallback {
    void onSuccess(List<Article> articles);

    void onFailure(ArrownockException e);
}