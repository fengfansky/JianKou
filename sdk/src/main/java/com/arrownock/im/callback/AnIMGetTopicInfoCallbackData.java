package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class AnIMGetTopicInfoCallbackData extends AnIMBaseRequestCallbackData {
    private String topicId = null;
    private String topicName = null;
    private Set<String> parties = null;
    private Map<String, String> customData = null;
    private Date createdAt = null;
    private String owner = null;

    public AnIMGetTopicInfoCallbackData(boolean error, ArrownockException exception, String topicId, String topicName,
            String owner, Set<String> parties, Date createdAt, Map<String, String> customData) {
        super(error, exception);
        this.topicId = topicId;
        this.topicName = topicName;
        this.parties = parties;
        this.createdAt = createdAt;
        this.owner = owner;
        this.customData = customData;
    }

    public String getTopicId() {
        return topicId;
    }
    
    public String getTopicName() {
        return topicName;
    }

    public Set<String> getParties() {
        return parties;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getOwner() {
        return owner;
    }
    
    public Map<String, String> getCustomData() {
        return customData;
    }
}
