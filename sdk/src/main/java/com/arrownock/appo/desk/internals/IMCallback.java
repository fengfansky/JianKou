package com.arrownock.appo.desk.internals;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.arrownock.appo.desk.AnDesk;
import com.arrownock.exception.ArrownockException;
import com.arrownock.im.AnIM;
import com.arrownock.internal.desk.IAnDeskCallback;

public class IMCallback implements IAnDeskCallback {
    private com.arrownock.appo.desk.IAnDeskCallback callback = null;
    private Context context = null;
    private AnIM im = null;

    public IMCallback(com.arrownock.appo.desk.IAnDeskCallback callback, AnIM im, Context context) {
        this.callback = callback;
        this.context = context;
        this.im = im;
    }

    @Override
    public void messageSent(String messageId, long timestamp, ArrownockException e) {
        if (callback != null) {
            try {
                callback.messageSent(messageId, timestamp, e);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void receivedBinary(String messageId, byte[] content, String fileType, long timestamp, String from,
            String topicId, Map<String, String> customData) {
        if (callback != null) {
            try {
                if (AnDesk.ANDESK_IMAGE_TYPE.equals(fileType)) {
                    callback.receivedImage(customData.get("groupId"), messageId, content, timestamp,
                            customData.get("accId"), customData.get("name"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void receivedMessage(String messageId, String message, long timestamp, String from, String topicId,
            Map<String, String> customData) {
        if (callback != null) {
            try {
                callback.receivedMessage(customData.get("groupId"), messageId, message, timestamp,
                        customData.get("accId"), customData.get("name"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void topicClosed(String topicId, long timestamp, Map<String, String> customData) {
        if (customData != null && customData.get("groupId") != null) {
            try {
                String groupId = customData.get("groupId");
                String key = groupId + "_" + im.getConnectingClientId();
                SharedPreferences pref = context.getSharedPreferences(AnDesk.class.getName(), Context.MODE_PRIVATE);
                String session = pref.getString(key, "");
                if (session != null && !session.isEmpty()) {
                    try {
                        JSONObject s = new JSONObject(session);
                        String sessionId = s.optString("session_id");
                        if (topicId.equals(sessionId)) {
                            Editor editor = context.getSharedPreferences(AnDesk.class.getName(), Context.MODE_PRIVATE)
                                    .edit();
                            editor.remove(key).commit();
                        }
                    } catch (JSONException e) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (callback != null) {
            try {
                callback.sessionClosed(customData.get("groupId"), topicId, timestamp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void accountAddedToSession(String topicId, long timestamp, Map<String, String> customData) {
        if (callback != null) {
            try {
                callback.accountAddedToSession(customData.get("groupId"), topicId, customData.get("accId"),
                        customData.get("name"), timestamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}