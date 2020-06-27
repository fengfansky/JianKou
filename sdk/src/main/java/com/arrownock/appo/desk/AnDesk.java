package com.arrownock.appo.desk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.arrownock.appo.desk.internals.IMCallback;
import com.arrownock.appo.desk.internals.Utils;
import com.arrownock.exception.ArrownockException;
import com.arrownock.im.AnIM;
import com.arrownock.internal.util.ANEmojiUtil;
import com.arrownock.internal.util.Constants;
import com.arrownock.push.ANBase64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AnDesk {
    public static String ANDESK_IMAGE_TYPE = "image";
    public static String ANDESK_AUDIO_TYPE = "audio";
    private com.arrownock.internal.desk.IAnDeskCallback internalCallback = null;
    private User user = null;
    private String appKey = null;
    private Context context = null;
    private AnIM im = null;

    public AnDesk(User user, String appKey, AnIM im, Context context, IAnDeskCallback callback)
            throws ArrownockException {
        if (appKey == null || "".equals(appKey.trim())) {
            throw new ArrownockException("Invalid value of " + Constants.APP_KEY, ArrownockException.IM_INVALID_APP_KEY);
        }
        if (im == null) {
            throw new ArrownockException("Invalid anIM", ArrownockException.DESK_INVALID_ANIM);
        }
        if (user == null) {
            throw new ArrownockException("Invalid user", ArrownockException.DESK_INVALID_USER);
        } else {
            if (user.getId() == null || "".equals(user.getId().trim())) {
                throw new ArrownockException("user id can not be empty.", ArrownockException.DESK_INVALID_USER_ID);
            }
            if (user.getName() == null || "".equals(user.getName().trim())) {
                throw new ArrownockException("user name can not be empty.", ArrownockException.DESK_INVALID_USER_NAME);
            }
            if (user.getAge() != -1 && user.getAge() < 0) {
                throw new ArrownockException("user age should greater than 0", ArrownockException.DESK_INVALID_USER_AGE);
            }
        }
        this.user = user;
        this.appKey = appKey;
        this.context = context;
        this.im = im;
        internalCallback = new IMCallback(callback, im, context);
        im.setDeskCallback(internalCallback);
    }

    public void getGroups(final IAnDeskGetGroupsCallback callback) {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        Runnable getGroupsThread = new Runnable() {
            public void run() {
                String response = Utils.sendRequest(context, "v1/desks/groups/query.json", "GET",
                        im.isSecureConnection(), params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject data = json.getJSONObject("response");
                    if (data != null) {
                        List<Group> result = new ArrayList<Group>();
                        JSONArray groups = data.getJSONArray("groups");
                        for (int i = 0; i < groups.length(); i++) {
                            JSONObject group = groups.getJSONObject(i);
                            if (group != null) {
                                Group g = new Group();
                                g.setId(group.getString("id"));
                                g.setName(group.getString("name"));
                                JSONArray tags = group.optJSONArray("tags");
                                if (tags != null && tags.length() > 0) {
                                    List<String> gtags = new ArrayList<String>();
                                    for (int j = 0; j < tags.length(); j++) {
                                        gtags.add(tags.getString(j));
                                    }
                                    g.setTags(gtags);
                                }
                                result.add(g);
                            }
                        }
                        callback.onSuccess(result);
                    } else {
                        JSONObject meta = json.getJSONObject("meta");
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(getGroupsThread);
        thread.start();
    }

    public String getCurrentSessionId(String groupId, String clientId) throws ArrownockException {
        if (groupId == null || "".equals(groupId.trim())) {
            throw new ArrownockException("groupId can not be empty.", ArrownockException.DESK_INVALID_GROUP_ID);
        }
        if (clientId == null || "".equals(clientId.trim())) {
            throw new ArrownockException("clientId can not be empty.", ArrownockException.DESK_INVALID_CLIENT_ID);
        }
        String key = groupId + "_" + clientId;
        String session = getFromLocalStorage(key);
        if (session != null && !session.isEmpty()) {
            try {
                JSONObject s = new JSONObject(session);
                return s.optString("session_id");
            } catch (JSONException e) {
                removeFromLocalStorage(key);
            }
        }
        return null;
    }

    public void createSession(final String groupId, final String clientId, final IAnDeskCreateSessionCallback callback) {
        if (groupId == null || "".equals(groupId.trim())) {
            if (callback != null) {
                callback.onFailure(new ArrownockException("groupId can not be empty.", ArrownockException.DESK_INVALID_GROUP_ID));
            }
        }
        if (clientId == null || "".equals(clientId.trim())) {
            if (callback != null) {
                callback.onFailure(new ArrownockException("clientId can not be empty.", ArrownockException.DESK_INVALID_CLIENT_ID));
            }
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("group_id", groupId);
        params.put("client_id", clientId);
        if (user.getName() != null) {
            params.put("name", user.getName());
        }
        if (user.getId() != null) {
            params.put("user_id", user.getId());
        }
        if (user.getPhoto() != null) {
            params.put("photo", user.getPhoto());
        }
        if (user.getPhone() != null) {
            params.put("phone", user.getPhone());
        }
        if (user.getGender() != null) {
            params.put("gender", user.getGender());
        }
        if (user.getAge() != -1) {
            params.put("age", String.valueOf(user.getAge()));
        }
        Runnable createSessionThread = new Runnable() {
            public void run() {
                try {
                    String response = Utils.sendRequest(context, "v1/desks/sessions/create.json", "POST",
                            im.isSecureConnection(), params);
                    JSONObject json = new JSONObject(response);
                    JSONObject data = json.getJSONObject("response");
                    if (data != null) {
                        JSONObject session = data.getJSONObject("session");
                        String sessionId = session.getString("id");
                        String accountId = session.optString("account_id");
                        String name = session.optString("name");
                        String date = session.optString("created_at");

                        JSONObject value = new JSONObject();
                        value.put("session_id", sessionId);
                        value.put("account_id", accountId);
                        value.put("created_at", date);
                        saveToLocalStorage(groupId + "_" + clientId, value.toString());
                        try {
                            callback.onSuccess(sessionId, accountId, name);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        JSONObject meta = json.getJSONObject("meta");
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(createSessionThread);
        thread.start();
    }

    public void closeSession(String sessionId) {

    }

    public String sendMessage(String sessionId, String message) throws ArrownockException {
        if (sessionId == null || "".equals(sessionId.trim())) {
            throw new ArrownockException("sessionId can not be empty.", ArrownockException.DESK_INVALID_SESSION_ID);
        }
        if (message == null || "".equals(message.trim())) {
            throw new ArrownockException("message can not be empty.", ArrownockException.DESK_INVALID_MESSAGE);
        }
        Map<String, String> customData = new HashMap<String, String>();
        if (user.getName() != null) {
            customData.put("name", user.getName());
        }
        if (user.getId() != null) {
            customData.put("id", user.getId());
        }
        if (user.getPhoto() != null) {
            customData.put("photo", user.getPhoto());
        }
        if (user.getPhone() != null) {
            customData.put("phone", user.getPhone());
        }
        if (user.getGender() != null) {
            customData.put("gender", user.getGender());
        }
        if (user.getAge() != -1) {
            customData.put("age", String.valueOf(user.getAge()));
        }

        return im.sendMessageToTopic(sessionId, message, customData, false, null, "D");
    }

    public String sendImage(String sessionId, byte[] data) throws ArrownockException {
        return sendImage(sessionId, data, null);
    }

    public String sendImage(final String sessionId, final byte[] data, final String originalImageUrl)
            throws ArrownockException {
        if (sessionId == null || "".equals(sessionId.trim())) {
            throw new ArrownockException("sessionId can not be empty.", ArrownockException.DESK_INVALID_SESSION_ID);
        }
        if (data == null || data.length == 0) {
            throw new ArrownockException("data can not be empty.", ArrownockException.DESK_INVALID_DATA);
        }
        final Map<String, String> customData = new HashMap<String, String>();
        if (user.getName() != null) {
            customData.put("name", user.getName());
        }
        if (user.getId() != null) {
            customData.put("id", user.getId());
        }
        if (user.getPhoto() != null) {
            customData.put("photo", user.getPhoto());
        }
        if (user.getPhone() != null) {
            customData.put("phone", user.getPhone());
        }
        if (user.getGender() != null) {
            customData.put("gender", user.getGender());
        }
        if (user.getAge() != -1) {
            customData.put("age", String.valueOf(user.getAge()));
        }
        if (originalImageUrl != null && originalImageUrl.length() > 0) {
            customData.put("originalImage", originalImageUrl);
        }
        String msgId = im.generateMsgID();
        msgId = "D" + msgId;
        final String tempId = msgId;
        Runnable sendMessageThread = new Runnable() {
            public void run() {
                byte[] compressedData = Utils.compressImageDataByOriginalImage(data, 50f);
                try {
                    im.sendBinaryToTopic(sessionId, compressedData, ANDESK_IMAGE_TYPE, customData, false, tempId);
                } catch (ArrownockException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(sendMessageThread);
        thread.start();
        return msgId;
    }

    public String sendAudio(String sessionId, byte[] data) throws ArrownockException {
        if (sessionId == null || "".equals(sessionId.trim())) {
            throw new ArrownockException("sessionId can not be empty.", ArrownockException.DESK_INVALID_SESSION_ID);
        }
        if (data == null || data.length == 0) {
            throw new ArrownockException("data can not be empty.", ArrownockException.DESK_INVALID_DATA);
        }
        Map<String, String> customData = new HashMap<String, String>();
        if (user.getName() != null) {
            customData.put("name", user.getName());
        }
        if (user.getId() != null) {
            customData.put("id", user.getId());
        }
        if (user.getPhoto() != null) {
            customData.put("photo", user.getPhoto());
        }
        if (user.getPhone() != null) {
            customData.put("phone", user.getPhone());
        }
        if (user.getGender() != null) {
            customData.put("gender", user.getGender());
        }
        if (user.getAge() != -1) {
            customData.put("age", String.valueOf(user.getAge()));
        }
        return im.sendBinaryToTopic(sessionId, data, ANDESK_AUDIO_TYPE, customData, false, "D");
    }

    public void getOfflineMessage(final String clientId, final int limit, final IAnDeskHistoryCallback callback) {
        if (clientId == null || "".equals(clientId.trim())) {
            if (callback != null) {
                callback.onError(new ArrownockException("clientId can not be empty.", ArrownockException.DESK_INVALID_CLIENT_ID));
                return;
            }
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("type", "topic");
        params.put("all", "1");
        params.put("offline", "1");
        params.put("b", "1");
        params.put("me", clientId);
        params.put("device_type", "mobile");
        params.put("ext_type", "desk");
        if (limit > 0) {
            params.put("limit", String.valueOf(limit));
        }
        Runnable getOfflineMessageThread = new Runnable() {
            public void run() {
                try {
                    String response = Utils.sendRequest(context, "v1/im/history.json", "GET", im.isSecureConnection(),
                            params);
                    JSONObject json = new JSONObject(response);
                    JSONObject meta = json.getJSONObject("meta");
                    JSONObject data = json.getJSONObject("response");
                    if (data != null) {
                        JSONArray jsonArray = data.optJSONArray("messages");
                        List<AnDeskMessage> logs = new ArrayList<AnDeskMessage>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            if (obj != null) {
                                String type = obj.optString("content_type");
                                String msgId = obj.optString("msg_id");
                                String tid = obj.optString("topic_id");
                                String message = obj.optString("message");
                                long ts = obj.optLong("timestamp");
                                JSONObject customObj = obj.optJSONObject("customData");
                                Map<String, String> customData = null;
                                if (customObj != null) {
                                    Iterator<?> nameItr = customObj.keys();
                                    customData = new HashMap<String, String>();
                                    while (nameItr.hasNext()) {
                                        String key = (String) nameItr.next();
                                        String value = customObj.getString(key);
                                        customData.put(key, value);
                                    }
                                }

                                if ("text".equals(type)) {
                                    message = ANEmojiUtil.stringConvertToEmoji(message);
                                    AnDeskMessage m = new AnDeskMessage(AnDeskMessageType.AnDeskText, msgId,
                                            customData.get("groupId"), customData.get("accId"), customData.get("name"),
                                            message, null, ts);
                                    logs.add(m);
                                } else if ("binary".equals(type)) {
                                    String fileType = obj.optString("fileType");
                                    byte[] d = null;
                                    if (message != null && message.length() > 0) {
                                        d = ANBase64.decode(message);
                                    }
                                    if (ANDESK_IMAGE_TYPE.equals(fileType)) {
                                        AnDeskMessage m = new AnDeskMessage(AnDeskMessageType.AnDeskImage, msgId,
                                                customData.get("groupId"), customData.get("accId"),
                                                customData.get("name"), null, d, ts);
                                        logs.add(m);
                                    } else if (ANDESK_AUDIO_TYPE.equals(fileType)) {
                                        AnDeskMessage m = new AnDeskMessage(AnDeskMessageType.AnDeskAudio, msgId,
                                                customData.get("groupId"), customData.get("accId"),
                                                customData.get("name"), null, d, ts);
                                        logs.add(m);
                                    }
                                }
                            }
                        }
                        if (callback != null) {
                            if (meta != null && meta.has("leftCount")) {
                                callback.onSuccess(logs, meta.getInt("leftCount"));
                            } else {
                                callback.onSuccess(logs, -1);
                            }
                        }
                    } else {
                        callback.onError(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onError(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(getOfflineMessageThread);
        thread.start();
    }

    private String getFromLocalStorage(final String key) {
        SharedPreferences pref = context.getSharedPreferences(AnDesk.class.getName(), Context.MODE_PRIVATE);
        return pref.getString(key, "");
    }

    private void saveToLocalStorage(final String key, final String value) {
        Editor editor = context.getSharedPreferences(AnDesk.class.getName(), Context.MODE_PRIVATE).edit();
        editor.putString(key, value).commit();
    }

    private void removeFromLocalStorage(final String key) {
        Editor editor = context.getSharedPreferences(AnDesk.class.getName(), Context.MODE_PRIVATE).edit();
        editor.remove(key).commit();
    }
}
