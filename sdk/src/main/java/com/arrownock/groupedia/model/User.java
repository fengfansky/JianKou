package com.arrownock.groupedia.model;

import java.io.Serializable;

import org.json.JSONObject;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String imId;
    private String name;
    private String avatar;
    private JSONObject fields;
    private String extId;

    public User(String id, String imId, String extId, String name, String avatar, JSONObject fields) {
        super();
        this.id = id;
        this.imId = imId;
        this.extId = extId;
        this.name = name;
        this.avatar = avatar;
        this.fields = fields;
    }

    public User() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImId() {
        return imId;
    }

    public void setImId(String imId) {
        this.imId = imId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public JSONObject getFields() {
        return fields;
    }

    public void setFields(JSONObject fields) {
        this.fields = fields;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

}
