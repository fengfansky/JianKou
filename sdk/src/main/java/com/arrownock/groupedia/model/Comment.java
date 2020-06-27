package com.arrownock.groupedia.model;

import java.io.Serializable;

public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String content;
    private User user;
    private long createdAt;

    public Comment() {
        super();
    }

    public Comment(String id, String content, User user, long createdAt) {
        super();
        this.id = id;
        this.content = content;
        this.user = user;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

}
