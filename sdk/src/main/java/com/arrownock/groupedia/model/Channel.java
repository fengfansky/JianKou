package com.arrownock.groupedia.model;

import java.io.Serializable;

public class Channel implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;

    public Channel(String id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public Channel() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
