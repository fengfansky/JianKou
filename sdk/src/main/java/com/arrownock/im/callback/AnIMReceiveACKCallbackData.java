package com.arrownock.im.callback;

public class AnIMReceiveACKCallbackData extends AnIMBaseMessageCallbackData {
    private String type = null;

    public AnIMReceiveACKCallbackData(String msgId, String from, String type) {
        super(msgId, from);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
