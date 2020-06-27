package com.arrownock.groupedia;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.arrownock.exception.ArrownockException;
import com.arrownock.groupedia.callback.IAnGoupediaArticleCallback;
import com.arrownock.groupedia.callback.IAnGoupediaArticlesCallback;
import com.arrownock.groupedia.callback.IAnGoupediaChannelCallback;
import com.arrownock.groupedia.callback.IAnGoupediaCommentCallback;
import com.arrownock.groupedia.callback.IAnGoupediaCommentsCallback;
import com.arrownock.groupedia.callback.IAnGoupediaCommonCallback;
import com.arrownock.groupedia.callback.IAnGoupediaTopicCallback;
import com.arrownock.groupedia.callback.IAnGoupediaUserCallback;
import com.arrownock.groupedia.callback.IAnGroupediaCallback;
import com.arrownock.groupedia.callback.IAnGroupediaHistoryCallback;
import com.arrownock.groupedia.internals.GPUtils;
import com.arrownock.groupedia.internals.IMCallback;
import com.arrownock.groupedia.internals.Utils;
import com.arrownock.groupedia.model.AnGroupediaMessage;
import com.arrownock.groupedia.model.AnGroupediaMessageType;
import com.arrownock.groupedia.model.Article;
import com.arrownock.groupedia.model.Channel;
import com.arrownock.groupedia.model.Comment;
import com.arrownock.groupedia.model.Topic;
import com.arrownock.groupedia.model.User;
import com.arrownock.im.AnIM;
import com.arrownock.internal.util.ANEmojiUtil;
import com.arrownock.internal.util.Constants;
import com.arrownock.push.ANBase64;

public class AnGroupedia {
    public static String ANGROUPEDIA_IMAGE_TYPE = "image";
    public static String ANGROUPEDIA_AUDIO_TYPE = "audio";
    private com.arrownock.internal.groupedia.IAnGroupediaCallback internalCallback = null;
    private String appKey = null;
    private Context context = null;
    private AnIM im = null;
    private boolean isLinkUser = false;
    private Calendar c;
    private SimpleDateFormat sdf;
    private boolean isSecure = true;

    public AnGroupedia(String appKey, AnIM im, Context context, IAnGroupediaCallback callback)
            throws ArrownockException {
        if (appKey == null || "".equals(appKey.trim())) {
            throw new ArrownockException("Invalid value of " + Constants.APP_KEY, ArrownockException.IM_INVALID_APP_KEY);
        }
        if (im == null) {
            throw new ArrownockException("Invalid anIM", ArrownockException.DESK_INVALID_ANIM);
        }
        this.appKey = appKey;
        this.context = context;
        this.im = im;
        internalCallback = new IMCallback(callback, im, context);
        im.setGroupediaCallback(internalCallback);
        c = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public AnGroupedia(String appKey, Context context) throws ArrownockException {
        if (appKey == null || "".equals(appKey.trim())) {
            throw new ArrownockException("Invalid value of " + Constants.APP_KEY, ArrownockException.IM_INVALID_APP_KEY);
        }
        this.appKey = appKey;
        this.context = context;
        c = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void setSecure(boolean secure) {
        this.isSecure = secure;
    }

    public void initUser(final String extId, final String name, final String avatar,
            final IAnGoupediaUserCallback callback) {
        isLinkUser = false;
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        if (extId == null || "".equals(extId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of ext_id",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("userid", extId);
        if (name != null && !"".equals(name.trim())) {
            params.put("name", name);
        }
        if (avatar != null && !"".equals(avatar.trim())) {
            params.put("avatar", avatar);
        }
        Runnable initUserThread = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/users/info", "GET", isSecure, params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject data = json.getJSONObject("response");
                    if (data != null) {
                        JSONObject userJson = data.getJSONObject("user");
                        String id = userJson.getString("id");
                        String imId = userJson.getString("clientId");
                        User user = new User(id, imId, extId, name, avatar, null);
                        callback.onSuccess(user);
                    } else {
                        JSONObject meta = json.getJSONObject("meta");
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(initUserThread);
        thread.start();
    }

    public void linkUser(final String userId, final IAnGoupediaUserCallback callback) {
        isLinkUser = true;
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        if (userId == null || "".equals(userId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of userId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("userid", userId);
        Runnable linkUserThread = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/users/link", "GET", isSecure, params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject data = json.getJSONObject("response");
                    if (data != null) {
                        JSONObject userJson = data.getJSONObject("user");
                        String id = userJson.getString("id");
                        String imId = userJson.getString("clientId");
                        User user = new User(id, imId, null, null, null, userJson);
                        callback.onSuccess(user);
                    } else {
                        JSONObject meta = json.getJSONObject("meta");
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(linkUserThread);
        thread.start();
    }

    public void updateUser(final String userId, final String name, final String avatar,
            final IAnGoupediaCommonCallback callback) {
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        if (userId == null || "".equals(userId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of userId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        if ((name == null || "".equals(name.trim())) && (avatar == null || "".equals(avatar.trim()))) {
            callback.onFailure(new ArrownockException("Name and avatar cannot be both empty",
                    ArrownockException.GROUPEDIA_INVALID_NAME_AVATAR));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("id", userId);
        if (name != null && !"".equals(name.trim())) {
            params.put("name", name);
        }
        if (avatar != null && !"".equals(avatar.trim())) {
            params.put("avatar", avatar);
        }
        Runnable linkUserThread = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/users/update", "POST", isSecure, params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject meta = json.getJSONObject("meta");
                    int code = meta.getInt("code");
                    if (code == 200) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(linkUserThread);
        thread.start();
    }

    public void getChannels(final IAnGoupediaChannelCallback callback) {
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        Runnable runnable = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/channels", "GET", isSecure, params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject data = json.getJSONObject("response");
                    if (data != null) {
                        JSONArray channelsArray = data.getJSONArray("channels");
                        String id = "";
                        String name = "";
                        List<Channel> channels = new ArrayList<Channel>();
                        for (int i = 0; i < channelsArray.length(); i++) {
                            JSONObject channelJson = channelsArray.getJSONObject(i);
                            id = channelJson.getString("id");
                            name = channelJson.getString("name");
                            Channel channel = new Channel(id, name);
                            channels.add(channel);
                        }
                        callback.onSuccess(channels);
                    } else {
                        JSONObject meta = json.getJSONObject("meta");
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void getArticles(final String channelId, final int page, final int limit,
            final IAnGoupediaArticlesCallback callback) {
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        if (channelId == null || "".equals(channelId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of channelId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        if (page <= 0) {
            callback.onFailure(new ArrownockException("Invalid value of page",
                    ArrownockException.GROUPEDIA_INVALID_PAGE_OR_LIMIT));
            return;
        }
        if (limit <= 0) {
            callback.onFailure(new ArrownockException("Invalid value of limit",
                    ArrownockException.GROUPEDIA_INVALID_PAGE_OR_LIMIT));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("page", page + "");
        params.put("limit", limit + "");
        Runnable runnable = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/articles/channel/" + channelId, "GET", isSecure,
                        params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject data = json.getJSONObject("response");
                    if (data != null) {
                        JSONArray articlesArray = data.getJSONArray("articles");
                        String id = "";
                        String columnId = "";
                        String columnName = "";
                        String columnPhotoUrl = "";
                        String columnDescription = "";
                        String title = "";
                        String description = "";
                        String url = "";
                        String content = "";
                        String photoUrl = "";
                        long createdAt = 0l;
                        boolean isLike = false;
                        int readCount = 0;
                        int likeCount = 0;
                        List<Article> articles = new ArrayList<Article>();
                        for (int i = 0; i < articlesArray.length(); i++) {
                            JSONObject articleJson = articlesArray.getJSONObject(i);
                            id = articleJson.optString("id");
                            if (articleJson.has("wall")) {
                                JSONObject columnJson = articleJson.getJSONObject("wall");
                                columnId = columnJson.optString("id");
                                columnName = columnJson.optString("name");
                                columnDescription = columnJson.optString("description");
                                columnPhotoUrl = columnJson.optString("cover");
                            } else {
                                columnId = "";
                                columnName = "";
                                columnPhotoUrl = "";
                                columnDescription = "";
                            }
                           
                            title = articleJson.optString("title");
                            description = articleJson.optString("description");
//                            url = articleJson.optString("url");
//                            content = articleJson.optString("content");
                            photoUrl = articleJson.optString("cover");
                            createdAt = articleJson.optLong("date");
                            isLike = false;
                            readCount = articleJson.optInt("views");
                            likeCount = articleJson.optInt("likes");
                            User user = null;
                            if (articleJson.has("user")) {
                                JSONObject userJson = articleJson.getJSONObject("user");
                                String userId = userJson.optString("id");
                                String imId = userJson.optString("clientId");
                                String name = userJson.optString("username");
                                String avatar = "";
                                if (userJson.has("photo")) {
                                    avatar = userJson.getJSONObject("photo").optString("url");
                                }
                                user = new User(userId, imId, null, name, avatar, null);
                            }

                            Article article = new Article(id, columnId, columnName, columnDescription, columnPhotoUrl,
                                    title, description, url, content, photoUrl, createdAt, readCount, likeCount,
                                    isLike, user);
                            articles.add(article);
                        }
                        callback.onSuccess(articles);
                    } else {
                        JSONObject meta = json.getJSONObject("meta");
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
    
    public void getArticleByArticleId(final String articleId, final String userId,
            final IAnGoupediaArticleCallback callback) {
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        if (articleId == null || "".equals(articleId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of articleId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        if (userId == null || "".equals(userId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of userId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("like_user_id", userId);
        Runnable runnable = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/articles/" + articleId, "GET", isSecure, params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject data = json.getJSONObject("response");
                    if (data != null) {
                        JSONObject articleJson = data.getJSONObject("article");
                        String id = articleJson.optString("id");
                        String title = articleJson.optString("title");
                        String description = articleJson.optString("description");
                        String columnId = "";
                        String columnName = "";
                        String columnPhotoUrl = "";
                        String columnDescription = "";
                        if (articleJson.has("wall")) {
                            JSONObject columnJson = articleJson.getJSONObject("wall");
                            columnId = columnJson.optString("id");
                            columnName = columnJson.optString("name");
                            columnPhotoUrl = columnJson.optString("cover");
                            columnDescription = columnJson.optString("description");
                        }
                        
                        String url = articleJson.optString("url");
                        String content = articleJson.optString("content");
                        String photoUrl = articleJson.optString("cover");
                        long createdAt = articleJson.optLong("date");
                        boolean isLike = articleJson.optBoolean("isLiked", false);
                        int readCount = articleJson.optInt("views");
                        int likeCount = articleJson.optInt("likes");
                        User user = null;
                        if (articleJson.has("user")) {
                            JSONObject userJson = articleJson.getJSONObject("user");
                            String userId = userJson.optString("id");
                            String imId = userJson.optString("clientId");
                            String name = userJson.optString("username");
                            String avatar = "";
                            if (userJson.has("photo")) {
                                avatar = userJson.getJSONObject("photo").optString("url");
                            }
                            user = new User(userId, imId, null, name, avatar, null);
                        }

                        Article article = new Article(id, columnId, columnName, columnDescription, columnPhotoUrl,
                                title, description, url, content, photoUrl, createdAt, readCount, likeCount, isLike,
                                user);
                        callback.onSuccess(article);
                    } else {
                        JSONObject meta = json.getJSONObject("meta");
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void createLike(final String articleId, final String userId, final IAnGoupediaCommonCallback callback) {
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        if (articleId == null || "".equals(articleId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of articleId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        if (userId == null || "".equals(userId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of userId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("id", userId);
        Runnable runnable = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/likes/" + articleId + "/add", "POST", isSecure,
                        params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject meta = json.getJSONObject("meta");
                    int code = meta.getInt("code");
                    if (code == 200) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void cancelLike(final String articleId, final String userId, final IAnGoupediaCommonCallback callback) {
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        if (articleId == null || "".equals(articleId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of articleId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        if (userId == null || "".equals(userId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of userId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("id", userId);
        Runnable runnable = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/likes/" + articleId + "/remove", "POST", isSecure,
                        params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject meta = json.getJSONObject("meta");
                    int code = meta.getInt("code");
                    if (code == 200) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void getComments(final String articleId, final int page, final int limit,
            final IAnGoupediaCommentsCallback callback) {
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        if (articleId == null || "".equals(articleId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of articleId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        if (page <= 0) {
            callback.onFailure(new ArrownockException("Invalid value of page",
                    ArrownockException.GROUPEDIA_INVALID_PAGE_OR_LIMIT));
            return;
        }
        if (limit <= 0) {
            callback.onFailure(new ArrownockException("Invalid value of limit",
                    ArrownockException.GROUPEDIA_INVALID_PAGE_OR_LIMIT));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("page", page + "");
        params.put("limit", limit + "");
        Runnable runnable = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/comments/" + articleId, "GET", isSecure, params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject data = json.getJSONObject("response");
                    if (data != null) {
                        JSONArray commentsArray = data.getJSONArray("comments");
                        String id = "";
                        String content = "";
                        long createdAt = 0l;

                        List<Comment> comments = new ArrayList<Comment>();
                        for (int i = 0; i < commentsArray.length(); i++) {
                            JSONObject commentJson = commentsArray.getJSONObject(i);
                            id = commentJson.optString("id");
                            content = commentJson.optString("content");
                            User user = null;
                            if (commentJson.has("user")) {
                                JSONObject userJson = commentJson.getJSONObject("user");
                                String userId = userJson.optString("id");
                                String imId = userJson.optString("clientId");
                                if (isLinkUser) {
                                    user = new User(id, imId, null, null, null, userJson);
                                } else {
                                    String extId = userJson.optString("extUserId");
                                    String name = userJson.optString("firstName");
                                    if (name == null || name.length() == 0) {
                                        name = userJson.optString("username");
                                    }
                                    String avatar = "";
                                    if (userJson.has("properties")) {
                                        avatar = userJson.getJSONObject("properties").optString("avatar");
                                    }
                                    if (avatar.length() == 0 && userJson.has("photo")) {
                                        avatar = userJson.getJSONObject("photo").optString("url");
                                    }
                                    user = new User(userId, imId, extId, name, avatar, null);
                                }
                            } else if (commentJson.has("username")) {
                                String username = commentJson.optString("username");
                                user = new User("", "", "", username, "", null);
                            } else {
                                user = new User("", "", "", "", "", null);
                            }
                            c.setTime(sdf.parse(commentJson.getString("created_at")));
                            createdAt = c.getTimeInMillis();

                            Comment comment = new Comment(id, content, user, createdAt);
                            comments.add(comment);
                        }
                        callback.onSuccess(comments);
                    } else {
                        JSONObject meta = json.getJSONObject("meta");
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void createComment(final String articleId, final String userId, final String content,
            final IAnGoupediaCommentCallback callback) {
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        if (articleId == null || "".equals(articleId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of articleId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        if (userId == null || "".equals(userId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of userId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        if (content == null || "".equals(content.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of content",
                    ArrownockException.GROUPEDIA_INVALID_CONTENT));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("content", content);
        params.put("id", userId);
        Runnable runnable = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/comments/" + articleId + "/add", "POST", isSecure,
                        params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject data = json.getJSONObject("response");
                    if (data != null) {
                        String id = "";
                        String content = "";
                        long createdAt = 0l;

                        JSONObject commentJson = data.getJSONObject("comment");
                        id = commentJson.optString("id");
                        content = commentJson.optString("content");
                        User user = null;
                        if (commentJson.has("user")) {
                            JSONObject userJson = commentJson.getJSONObject("user");
                            String userId = userJson.optString("id");
                            String imId = userJson.optString("clientId");
                            if (isLinkUser) {
                                user = new User(id, imId, null, null, null, userJson);
                            } else {
                                String extId = userJson.optString("extUserId");
                                String name = userJson.optString("firstName");
                                String avatar = "";
                                if (userJson.has("properties")) {
                                    avatar = userJson.getJSONObject("properties").optString("avatar");
                                }
                                user = new User(userId, imId, extId, name, avatar, null);
                            }
                        }
                        c.setTime(sdf.parse(commentJson.getString("created_at")));
                        createdAt = c.getTimeInMillis();

                        Comment comment = new Comment(id, content, user, createdAt);
                        callback.onSuccess(comment);
                    } else {
                        JSONObject meta = json.getJSONObject("meta");
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void removeComment(final String articleId, final String commentId, final IAnGoupediaCommonCallback callback) {
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        if (articleId == null || "".equals(articleId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of articleId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        if (commentId == null || "".equals(commentId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of commentId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        Runnable runnable = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/comments/" + articleId + "/remove/" + commentId,
                        "POST", isSecure, params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject meta = json.getJSONObject("meta");
                    int code = meta.getInt("code");
                    if (code == 200) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void joinTopic(final String userImId, final String topicId, final IAnGoupediaCommonCallback callback) {
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        if (userImId == null || "".equals(userImId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of userImId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        if (topicId == null || "".equals(topicId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of topicId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("client_id", userImId);
        Runnable runnable = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/im/topics/" + topicId + "/join", "POST", isSecure,
                        params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject joinMeta = json.getJSONObject("meta");
                    int code = joinMeta.getInt("code");
                    if (code == 200) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(new ArrownockException(joinMeta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void quitTopic(final String userImId, final String topicId, final IAnGoupediaCommonCallback callback) {
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        if (userImId == null || "".equals(userImId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of userImId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        if (topicId == null || "".equals(topicId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of topicId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("client_id", userImId);
        Runnable runnable = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/im/topics/" + topicId + "/leave", "POST", isSecure,
                        params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject joinMeta = json.getJSONObject("meta");
                    int code = joinMeta.getInt("code");
                    if (code == 200) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(new ArrownockException(joinMeta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void getTopic(final String userImId, final String columnId, final IAnGoupediaTopicCallback callback) {
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        if (userImId == null || "".equals(userImId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of userImId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        if (columnId == null || "".equals(columnId.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of columnId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("client_id", userImId);
        Runnable runnable = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/im/topics/get/" + columnId, "GET", isSecure, params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject data = json.getJSONObject("response");
                    if (data != null) {
                        String id = data.getString("topic_id");
                        String topicName = data.getString("topic_name");
                        int members = data.getInt("members");
                        boolean isJoin = data.getBoolean("joined");
                        Topic topic = new Topic(id, topicName, members);
                        List<AnGroupediaMessage> messages = new ArrayList<AnGroupediaMessage>();
                        JSONArray jsonArray = data.getJSONArray("messages");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            if (obj != null) {
                                String type = obj.optString("content_type");
                                String msgId = obj.optString("msg_id");
                                String tid = id;
                                String message = obj.optString("message");
                                String from = obj.optString("from");
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

                                JSONObject fields = null;
                                String fieldsStr = null;
                                String userId = customData.get("id");
                                String imId = from;
                                String name = "";
                                String avatar = "";
                                String extId = "";
                                if (customData.containsKey("fields")) {
                                    fieldsStr = customData.get("fields");
                                    fields = new JSONObject(fieldsStr);
                                } else {
                                    name = customData.get("name");
                                    avatar = customData.get("avatar");
                                    extId = customData.get("ext_id");
                                }
                                User user = new User(userId, imId, extId, name, avatar, fields);

                                if ("text".equals(type)) {
                                    message = ANEmojiUtil.stringConvertToEmoji(message);
                                    AnGroupediaMessage m = new AnGroupediaMessage(
                                            AnGroupediaMessageType.AnGroupediaText, msgId, tid, user, message, null, ts);
                                    messages.add(m);
                                } else if ("binary".equals(type)) {
                                    String fileType = obj.optString("fileType");
                                    byte[] d = null;
                                    if (message != null && message.length() > 0) {
                                        d = ANBase64.decode(message);
                                    }
                                    if (ANGROUPEDIA_IMAGE_TYPE.equals(fileType)) {
                                        AnGroupediaMessage m = new AnGroupediaMessage(
                                                AnGroupediaMessageType.AnGroupediaImage, msgId, tid, user, null, d, ts);
                                        messages.add(m);
                                    } else if (ANGROUPEDIA_AUDIO_TYPE.equals(fileType)) {
                                        AnGroupediaMessage m = new AnGroupediaMessage(
                                                AnGroupediaMessageType.AnGroupediaAudio, msgId, tid, user, null, d, ts);
                                        messages.add(m);
                                    }
                                }
                            }
                        }

                        callback.onSuccess(topic, isJoin, messages);
                    } else {
                        JSONObject meta = json.getJSONObject("meta");
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public String sendMessage(String topicId, String message, User user) throws ArrownockException {
        if (topicId == null || "".equals(topicId.trim())) {
            throw new ArrownockException("topicId can not be empty.", ArrownockException.GROUPEDIA_INVALID_ID);
        }
        if (message == null || "".equals(message.trim())) {
            throw new ArrownockException("message can not be empty.", ArrownockException.GROUPEDIA_INVALID_MESSAGE);
        }
        if (user == null || user.getId() == null || "".equals(user.getId().trim())) {
            throw new ArrownockException("user can not be empty.", ArrownockException.GROUPEDIA_INVALID_ID);
        }
        if (im == null) {
            throw new ArrownockException("init anIM first", ArrownockException.GROUPEDIA_INVALID_ANIM);
        }
        Map<String, String> customData = new HashMap<String, String>();
        customData.put("message_type", "gp");
        if (isLinkUser) {
            if (user.getId() != null) {
                customData.put("id", user.getId());
            }
            if (user.getFields() != null) {
                customData.put("fields", user.getFields().toString());
            }
        } else {
            if (user.getId() != null) {
                customData.put("id", user.getId());
            }
            if (user.getName() != null) {
                customData.put("name", user.getName());
            }
            if (user.getExtId() != null) {
                customData.put("ext_id", user.getExtId());
            }
            if (user.getAvatar() != null) {
                customData.put("avatar", user.getAvatar());
            }
        }
        return im.sendMessageToTopic(topicId, message, customData, false, null);
    }

    public String sendImage(final String topicId, final byte[] data, final User user) throws ArrownockException {
        if (topicId == null || "".equals(topicId.trim())) {
            throw new ArrownockException("topicId can not be empty.", ArrownockException.GROUPEDIA_INVALID_ID);
        }
        if (data == null || data.length == 0) {
            throw new ArrownockException("data can not be empty.", ArrownockException.GROUPEDIA_INVALID_DATA);
        }
        if (user == null || user.getId() == null || "".equals(user.getId().trim())) {
            throw new ArrownockException("user can not be empty.", ArrownockException.GROUPEDIA_INVALID_ID);
        }
        if (im == null) {
            throw new ArrownockException("init anIM first", ArrownockException.GROUPEDIA_INVALID_ANIM);
        }

        final Map<String, String> customData = new HashMap<String, String>();
        customData.put("message_type", "gp");
        if (isLinkUser) {
            if (user.getId() != null) {
                customData.put("id", user.getId());
            }
            if (user.getFields() != null) {
                customData.put("fields", user.getFields().toString());
            }
        } else {
            if (user.getId() != null) {
                customData.put("id", user.getId());
            }
            if (user.getName() != null) {
                customData.put("name", user.getName());
            }
            if (user.getExtId() != null) {
                customData.put("ext_id", user.getExtId());
            }
            if (user.getAvatar() != null) {
                customData.put("avatar", user.getAvatar());
            }
        }

        String msgId = im.sendBinaryToTopic(topicId, data, ANGROUPEDIA_IMAGE_TYPE, customData, false);
        return msgId;
    }

    public String sendAudio(final String topicId, final byte[] data, final User user) throws ArrownockException {
        if (topicId == null || "".equals(topicId.trim())) {
            throw new ArrownockException("topicId can not be empty.", ArrownockException.GROUPEDIA_INVALID_ID);
        }
        if (data == null || data.length == 0) {
            throw new ArrownockException("data can not be empty.", ArrownockException.GROUPEDIA_INVALID_DATA);
        }
        if (user == null || user.getId() == null || "".equals(user.getId().trim())) {
            throw new ArrownockException("user can not be empty.", ArrownockException.GROUPEDIA_INVALID_ID);
        }
        if (im == null) {
            throw new ArrownockException("init anIM first", ArrownockException.GROUPEDIA_INVALID_ANIM);
        }

        final Map<String, String> customData = new HashMap<String, String>();
        customData.put("message_type", "gp");
        if (isLinkUser) {
            if (user.getId() != null) {
                customData.put("id", user.getId());
            }
            if (user.getFields() != null) {
                customData.put("fields", user.getFields().toString());
            }
        } else {
            if (user.getId() != null) {
                customData.put("id", user.getId());
            }
            if (user.getName() != null) {
                customData.put("name", user.getName());
            }
            if (user.getExtId() != null) {
                customData.put("ext_id", user.getExtId());
            }
            if (user.getAvatar() != null) {
                customData.put("avatar", user.getAvatar());
            }
        }

        String msgId = im.sendBinaryToTopic(topicId, data, ANGROUPEDIA_AUDIO_TYPE, customData, false);
        return msgId;
    }

    public void getTopicOfflineHistory(final String userImId, final int limit,
            final IAnGroupediaHistoryCallback callback) {
        if (userImId == null || "".equals(userImId.trim())) {
            callback.onError(new ArrownockException("Invalid value of userImId",
                    ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("type", "topic");
        params.put("all", "1");
        params.put("offline", "1");
        params.put("b", "1");
        params.put("me", userImId);
        params.put("device_type", "mobile");
        params.put("ext_type", "gp");
        if (limit > 0) {
            params.put("limit", String.valueOf(limit));
        }
        Runnable getOfflineMessageThread = new Runnable() {
            public void run() {
                try {
                    String response = Utils.sendRequest(context, "v1/im/history.json", "GET", isSecure, params);
                    JSONObject json = new JSONObject(response);
                    JSONObject meta = json.getJSONObject("meta");
                    JSONObject data = json.getJSONObject("response");
                    if (data != null) {
                        JSONArray jsonArray = data.optJSONArray("messages");
                        List<AnGroupediaMessage> logs = new ArrayList<AnGroupediaMessage>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            if (obj != null) {
                                String type = obj.optString("content_type");
                                String msgId = obj.optString("msg_id");
                                String tid = obj.optString("topic_id");
                                String message = obj.optString("message");
                                String from = obj.optString("from");
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
                                    name = customData.get("name");
                                    avatar = customData.get("avatar");
                                    extId = customData.get("ext_id");
                                }
                                User user = new User(id, imId, extId, name, avatar, fields);

                                if ("text".equals(type)) {
                                    message = ANEmojiUtil.stringConvertToEmoji(message);
                                    AnGroupediaMessage m = new AnGroupediaMessage(
                                            AnGroupediaMessageType.AnGroupediaText, msgId, tid, user, message, null, ts);
                                    logs.add(m);
                                } else if ("binary".equals(type)) {
                                    String fileType = obj.optString("fileType");
                                    byte[] d = null;
                                    if (message != null && message.length() > 0) {
                                        d = ANBase64.decode(message);
                                    }
                                    if (ANGROUPEDIA_IMAGE_TYPE.equals(fileType)) {
                                        AnGroupediaMessage m = new AnGroupediaMessage(
                                                AnGroupediaMessageType.AnGroupediaImage, msgId, tid, user, null, d, ts);
                                        logs.add(m);
                                    } else if (ANGROUPEDIA_AUDIO_TYPE.equals(fileType)) {
                                        AnGroupediaMessage m = new AnGroupediaMessage(
                                                AnGroupediaMessageType.AnGroupediaAudio, msgId, tid, user, null, d, ts);
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

    public void getTopicHistory(final String topicId, final String imId, final int limit, long timestamp,
            final IAnGroupediaHistoryCallback callback) {
        if (topicId == null || "".equals(topicId.trim())) {
            callback.onError(new ArrownockException("Invalid value of topicId", ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        if (imId == null || "".equals(imId.trim())) {
            callback.onError(new ArrownockException("Invalid value of imId", ArrownockException.GROUPEDIA_INVALID_ID));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("type", "topic");
        params.put("topic_id", topicId);
        params.put("b", "1");
        params.put("me", imId);
        params.put("device_type", "mobile");
        params.put("ext_type", "gp");
        if (limit > 0) {
            params.put("limit", String.valueOf(limit));
        }
        if (timestamp > 0) {
            params.put("timestamp", String.valueOf(timestamp));
        }
        Runnable getOfflineMessageThread = new Runnable() {
            public void run() {
                try {
                    String response = Utils.sendRequest(context, "v1/im/history.json", "GET", isSecure, params);
                    JSONObject json = new JSONObject(response);
                    JSONObject meta = json.getJSONObject("meta");
                    JSONObject data = json.getJSONObject("response");
                    if (data != null) {
                        JSONArray jsonArray = data.optJSONArray("messages");
                        List<AnGroupediaMessage> logs = new ArrayList<AnGroupediaMessage>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            if (obj != null) {
                                String type = obj.optString("content_type");
                                String msgId = obj.optString("msg_id");
                                String tid = topicId;
                                String message = obj.optString("message");
                                String from = obj.optString("from");
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
                                    name = customData.get("name");
                                    avatar = customData.get("avatar");
                                    extId = customData.get("ext_id");
                                }
                                User user = new User(id, imId, extId, name, avatar, fields);

                                if ("text".equals(type)) {
                                    message = ANEmojiUtil.stringConvertToEmoji(message);
                                    AnGroupediaMessage m = new AnGroupediaMessage(
                                            AnGroupediaMessageType.AnGroupediaText, msgId, tid, user, message, null, ts);
                                    logs.add(m);
                                } else if ("binary".equals(type)) {
                                    String fileType = obj.optString("fileType");
                                    byte[] d = null;
                                    if (message != null && message.length() > 0) {
                                        d = ANBase64.decode(message);
                                    }
                                    if (ANGROUPEDIA_IMAGE_TYPE.equals(fileType)) {
                                        AnGroupediaMessage m = new AnGroupediaMessage(
                                                AnGroupediaMessageType.AnGroupediaImage, msgId, tid, user, null, d, ts);
                                        logs.add(m);
                                    } else if (ANGROUPEDIA_AUDIO_TYPE.equals(fileType)) {
                                        AnGroupediaMessage m = new AnGroupediaMessage(
                                                AnGroupediaMessageType.AnGroupediaAudio, msgId, tid, user, null, d, ts);
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

    public void searchArticles(final String content, final int page, final int limit,
            final IAnGoupediaArticlesCallback callback) {
        if (appKey == null || "".equals(appKey.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.IM_INVALID_APP_KEY));
            return;
        }
        if (content == null || "".equals(content.trim())) {
            callback.onFailure(new ArrownockException("Invalid value of content",
                    ArrownockException.GROUPEDIA_INVALID_CONTENT));
            return;
        }
        if (page <= 0) {
            callback.onFailure(new ArrownockException("Invalid value of page",
                    ArrownockException.GROUPEDIA_INVALID_PAGE_OR_LIMIT));
            return;
        }
        if (limit <= 0) {
            callback.onFailure(new ArrownockException("Invalid value of limit",
                    ArrownockException.GROUPEDIA_INVALID_PAGE_OR_LIMIT));
            return;
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key", appKey);
        params.put("condition", content);
        params.put("page", page + "");
        params.put("limit", limit + "");
        Runnable runnable = new Runnable() {
            public void run() {
                String response = GPUtils.sendRequest(context, "v1/search/app/articles", "GET", isSecure, params);
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject data = json.getJSONObject("response");
                    if (data != null) {
                        JSONArray datasArray = data.getJSONArray("results");
                        String id = "";
                        String columnId = "";
                        String columnName = "";
                        String columnPhotoUrl = "";
                        String columnDescription = "";
                        String title = "";
                        String description = "";
                        String url = "";
                        String content = "";
                        String photoUrl = "";
                        long createdAt = 0l;
                        boolean isLike = false;
                        List<Article> articles = new ArrayList<Article>();
                        for (int i = 0; i < datasArray.length(); i++) {
                            JSONObject articleJson = datasArray.getJSONObject(i).getJSONObject("data");
                            // JSONObject highlightJson =
                            // datasArray.getJSONObject(i).getJSONObject("highlight");
                            id = articleJson.optString("id");
                            columnId = articleJson.optString("wall_id");
                            title = articleJson.optString("title");
                            description = articleJson.optString("description");
//                            url = articleJson.optString("url");
//                            content = articleJson.optString("content");
                            photoUrl = articleJson.optString("cover");
                            createdAt = articleJson.optLong("date");
                            isLike = false;
                            User user = null;
                            if (articleJson.has("user")) {
                                JSONObject userJson = articleJson.getJSONObject("user");
                                String userId = userJson.optString("id");
                                String imId = userJson.optString("clientId");
                                String name = userJson.optString("username");
                                String avatar = "";
                                if (userJson.has("photo")) {
                                    avatar = userJson.getJSONObject("photo").optString("url");
                                }
                                user = new User(userId, imId, null, name, avatar, null);
                            }

                            Article article = new Article(id, columnId, columnName, columnDescription, columnPhotoUrl,
                                    title, description, url, content, photoUrl, createdAt, 0, 0, isLike, user);
                            articles.add(article);
                        }
                        callback.onSuccess(articles);
                    } else {
                        JSONObject meta = json.getJSONObject("meta");
                        callback.onFailure(new ArrownockException(meta.getString("message"), -1));
                    }
                } catch (Exception e) {
                    callback.onFailure(new ArrownockException(e.getMessage(), e, -1));
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
