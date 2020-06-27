package com.arrownock.groupedia.model;

import java.io.Serializable;

public class Article implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String columnId;
    private String columnName;
    private String columnDescription;
    private String columnPhotoUrl;
    private String title;
    private String description;
    private String url;
    private String content;
    private String photoUrl;
    private long createdAt;
    private int readCount;
    private int likeCount;
    private boolean isLike;
    private User user;

    public Article() {
        super();
    }

    public Article(String id, String columnId, String columnName, String columnDescription, String columnPhotoUrl,
            String title, String description, String url, String content, String photoUrl, long createdAt,
            int readCount, int likeCount, boolean isLike, User user) {
        super();
        this.id = id;
        this.columnId = columnId;
        this.columnName = columnName;
        this.columnDescription = columnDescription;
        this.columnPhotoUrl = columnPhotoUrl;
        this.title = title;
        this.description = description;
        this.url = url;
        this.content = content;
        this.photoUrl = photoUrl;
        this.createdAt = createdAt;
        this.readCount = readCount;
        this.likeCount = likeCount;
        this.isLike = isLike;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnDescription() {
        return columnDescription;
    }

    public void setColumnDescription(String columnDescription) {
        this.columnDescription = columnDescription;
    }

    public String getColumnPhotoUrl() {
        return columnPhotoUrl;
    }

    public void setColumnPhotoUrl(String columnPhotoUrl) {
        this.columnPhotoUrl = columnPhotoUrl;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean isLike) {
        this.isLike = isLike;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
