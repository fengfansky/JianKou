package com.arrownock.groupedia.internals;

import java.util.Map;

import org.json.JSONObject;

import android.content.Context;

import com.arrownock.groupedia.AnGroupedia;
import com.arrownock.groupedia.model.User;
import com.arrownock.im.AnIM;
import com.arrownock.internal.groupedia.IAnGroupediaCallback;

public class IMCallback implements IAnGroupediaCallback {
    private com.arrownock.groupedia.callback.IAnGroupediaCallback callback = null;
    private Context context = null;
    private AnIM im = null;

    public IMCallback(com.arrownock.groupedia.callback.IAnGroupediaCallback callback, AnIM im, Context context) {
        this.callback = callback;
        this.context = context;
        this.im = im;
    }

    @Override
    public void receivedBinary(String messageId, byte[] content, String fileType, long timestamp, String from,
            String topicId, Map<String, String> customData) {
        if (callback != null) {
            try {
                JSONObject fields = null;
                String fieldsStr = null;
                String id = customData.get("id");
                String imId = from;
                String name = "";
                String avatar = "";
                String extId = "";
                if (customData.containsKey("fields")) {
                    fieldsStr = customData.get("fields");
                    fields = new JSONObject(fieldsStr);
                } else {
                    if (customData.containsKey("name")) {
                        name = customData.get("name");
                    }
                    if (customData.containsKey("avatar")) {
                        avatar = customData.get("avatar");
                    }
                    if (customData.containsKey("ext_id")) {
                        extId = customData.get("ext_id");
                    }
                }
                User user = new User(id, imId, extId, name, avatar, fields);
                if (AnGroupedia.ANGROUPEDIA_IMAGE_TYPE.equals(fileType)) {
                    callback.receivedImage(topicId, messageId, content, timestamp, user);
                } else if (AnGroupedia.ANGROUPEDIA_AUDIO_TYPE.equals(fileType)) {
                    callback.receivedAudio(topicId, messageId, content, timestamp, user);
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
                JSONObject fields = null;
                String fieldsStr = null;
                String id = customData.get("id");
                String imId = from;
                String name = "";
                String avatar = "";
                String extId = "";
                if (customData.containsKey("fields")) {
                    fieldsStr = customData.get("fields");
                    fields = new JSONObject(fieldsStr);
                } else {
                    if (customData.containsKey("name")) {
                        name = customData.get("name");
                    }
                    if (customData.containsKey("avatar")) {
                        avatar = customData.get("avatar");
                    }
                    if (customData.containsKey("ext_id")) {
                        extId = customData.get("ext_id");
                    }
                }
                User user = new User(id, imId, extId, name, avatar, fields);
                callback.receivedMessage(topicId, messageId, message, timestamp, user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}