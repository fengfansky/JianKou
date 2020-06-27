package com.arrownock.internal.social;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.arrownock.exception.ArrownockException;
import com.arrownock.internal.device.DeviceManager;
import com.arrownock.internal.util.Constants;
import com.arrownock.social.AnSocialFile;
import com.arrownock.social.AnSocialMethod;
import com.arrownock.social.IAnSocialCallback;

public class AnSocialHttpClientWrapper {
    public final static String ANSOCIALFILE_FILE = "file";
    public final static String ANSOCIALFILE_PHOTO = "photo";
    public final static String CUSTOM_FIELDS = "custom_fields";
    public final static String PROPERTIES = "properties";
    public final static String CHOICES = "choices";
    public final static String RESOLUTIONS = "resolutions";
    public final static String CREATE_USER = "users/create.json";
    public final static String LOGIN_USER = "users/auth.json";
    private Context context = null;
    private String appKey = null;
    
    private AnSocialHttpClient client = null;

    public AnSocialHttpClientWrapper() {
        client = new AnSocialHttpClient();
    }

    public AnSocialHttpClientWrapper(String appKey, Context androidContext) {
        client = new AnSocialHttpClient();
        client.setAppKey(appKey);
        this.context = androidContext;
        this.appKey = appKey;
    }

    public void setSecure(Boolean secure) {
        client.setSecure(secure);
    }

    public void setKey(String key) throws ArrownockException {
        if (key == null || "".equals(key.trim())) {
            throw new ArrownockException("Invalid key ", ArrownockException.SOCIAL_INVALID_APP_KEY);
        }
        client.setAppKey(key);
    }
    
    public void setApiSecret(String secret) throws ArrownockException {
        if ( secret == null || "".equals( secret.trim())) {
            throw new ArrownockException("Invalid API secret ", ArrownockException.SOCIAL_INVALID_API_SECRET);
        }
        client.setApiSecret(secret);
    }

    public void setHost(String socialHost) throws ArrownockException {
        if (socialHost == null || "".equals(socialHost.trim())) {
            throw new ArrownockException("Invalid host ", ArrownockException.SOCIAL_INVALID_HOST);
        }
        client.setHost(socialHost);
    }

    public void setTimeout(int millisTimeout) {
        client.setMillisTimeout(millisTimeout);
    }

    private JSONObject checkParams(AnSocialMethod methodType, Map<String, Object> params, String urlString,
            final IAnSocialCallback callback) throws ArrownockException {
        if (null == urlString || "".equals(urlString.trim())) {
            handlerFailure("Invalid path", ArrownockException.SOCIAL_INVALID_PATH, callback);
            return null;
        }
        if (methodType != AnSocialMethod.GET && methodType != AnSocialMethod.POST) {
            handlerFailure("Invalid methodType", ArrownockException.SOCIAL_INVALID_METHOD_TYPE, callback);
            return null;
        }
        JSONObject jsonParams = new JSONObject();

        Iterator<Entry<String, Object>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = (Entry) iterator.next();
            // is photo or file instanceof AnSocialFile
            if (ANSOCIALFILE_PHOTO.equals(entry.getKey()) || ANSOCIALFILE_FILE.equals(entry.getKey())) {
                if (!(entry.getValue() instanceof AnSocialFile)) {
                    handlerFailure("Invalid AnSocialFile", ArrownockException.SOCIAL_INVALID_PARAMS, callback);
                    return null;
                }
                AnSocialFile file = (AnSocialFile) entry.getValue();
                if (file == null || (file.getData() == null && file.getInputStream() == null)) {
                    handlerFailure("Invalid AnSocialFile", ArrownockException.SOCIAL_INVALID_PARAMS, callback);
                    return null;
                }
            }

            // custom_fields
            try {
                if (CUSTOM_FIELDS.equals(entry.getKey())) {
                	if (entry.getValue() instanceof String) {
                		jsonParams.put(CUSTOM_FIELDS, entry.getValue());
                	} else {
	                    if (!(entry.getValue() instanceof Map)) {
	                        handlerFailure("Invalid custom_fields", ArrownockException.SOCIAL_INVALID_PARAMS, callback);
	                        return null;
	                    } else {
	                        Map<String, String> customFieldsMap = (Map<String, String>) entry.getValue();
	                        JSONObject customFieldsJson = new JSONObject();
	                        Iterator<Entry<String, String>> it = customFieldsMap.entrySet().iterator();
	                        while (it.hasNext()) {
	                            Entry<String, String> en = (Entry) it.next();
	                            customFieldsJson.put(en.getKey(), en.getValue());
	                        }
	                        jsonParams.put(CUSTOM_FIELDS, customFieldsJson);
	                    }
                	}
                    // properties
                } else if (PROPERTIES.equals(entry.getKey())) {
                	if (entry.getValue() instanceof String) {
                		jsonParams.put(PROPERTIES, entry.getValue());
                	} else {
	                    if (!(entry.getValue() instanceof Map)) {
	                        handlerFailure("Invalid properties", ArrownockException.SOCIAL_INVALID_PARAMS, callback);
	                        return null;
	                    } else {
	                        Map<String, String> propertiesMap = (Map<String, String>) entry.getValue();
	                        JSONObject propertiesJson = new JSONObject();
	                        Iterator<Entry<String, String>> it = propertiesMap.entrySet().iterator();
	                        while (it.hasNext()) {
	                            Entry<String, String> en = (Entry) it.next();
	                            propertiesJson.put(en.getKey(), en.getValue());
	                        }
	                        jsonParams.put(PROPERTIES, propertiesJson);
	                    }
                	}
                    // choices
                } else if (CHOICES.equals(entry.getKey())) {
                	if (entry.getValue() instanceof String) {
                		jsonParams.put(CHOICES, entry.getValue());
                	} else {
	                    if (!(entry.getValue() instanceof Map)) {
	                        handlerFailure("Invalid choices", ArrownockException.SOCIAL_INVALID_PARAMS, callback);
	                        return null;
	                    } else {
	                        Map<String, String> choicesMap = (Map<String, String>) entry.getValue();
	                        JSONObject choicesJson = new JSONObject();
	                        Iterator<Entry<String, String>> it = choicesMap.entrySet().iterator();
	                        while (it.hasNext()) {
	                            Entry<String, String> en = (Entry) it.next();
	                            choicesJson.put(en.getKey(), en.getValue());
	                        }
	                        jsonParams.put(CHOICES, choicesJson);
	                    }
                	}
                    // photo_size
                } else if (RESOLUTIONS.equals(entry.getKey())) {
                	if (entry.getValue() instanceof String) {
                		jsonParams.put(RESOLUTIONS, entry.getValue());
                	} else {
	                    if (!(entry.getValue() instanceof Map)) {
	                        handlerFailure("Invalid resolutions", ArrownockException.SOCIAL_INVALID_PARAMS, callback);
	                        return null;
	                    } else {
	                        Map<String, String> photoSizeMap = (Map<String, String>) entry.getValue();
	                        JSONObject photoSizeJson = new JSONObject();
	                        Iterator<Entry<String, String>> it = photoSizeMap.entrySet().iterator();
	                        while (it.hasNext()) {
	                            Entry<String, String> en = (Entry) it.next();
	                            photoSizeJson.put(en.getKey(), en.getValue());
	                        }
	                        jsonParams.put(RESOLUTIONS, photoSizeJson);
	                    }
                	}
                } else {
                    // add normal params
                    jsonParams.put(entry.getKey(), entry.getValue());
                }
            } catch (JSONException e) {
                handlerFailure("Invalid params", ArrownockException.SOCIAL_INVALID_PARAMS, callback);
                return null;
            }
        }

        return jsonParams;
    }

    private void handlerFailure(String message, int exceptionCode, final IAnSocialCallback callback) {
        final JSONObject response = new JSONObject();
        JSONObject meta = new JSONObject();
        try {
            meta.put("status", "fail");
            meta.put("message", message);
            meta.put("errorCode", exceptionCode);
            meta.put("code", ArrownockException.SOCIAL_ERROR_CODE);
            response.put("meta", meta);
        } catch (JSONException e1) {
            e1.printStackTrace();
        } finally {
            if (null != callback) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(response);
                    }
                });
                t.run();
            }
        }
    }

    public void sendRequest(String path, AnSocialMethod methodType, Map<String, Object> params,
            IAnSocialCallback callback) throws ArrownockException {
        if (null == callback) {
            handlerFailure("Invalid callback", ArrownockException.SOCIAL_INVALID_CALLBACK, callback);
            return;
        }
        
        // for users/create.json and users/auth.json add deviceId
        if (Constants.DM_ENABLED) {
            if (CREATE_USER.equals(path) || LOGIN_USER.equals(path)) {
                String deviceId = DeviceManager.getInstance(context, appKey).getDeviceId();
                if (!"".equals(deviceId)){
                    params.put("device_id", deviceId);
                }
            }
        }
        
        JSONObject jsonParams = checkParams(methodType, params, path, callback);
        if (null == jsonParams) {
            return;
        }
        // for get
        if (AnSocialMethod.GET == methodType) {
            client.get(null, path, jsonParams, callback);
            // for post
        } else if (AnSocialMethod.POST == methodType) {
            // post with photo
            if (null != params.get(ANSOCIALFILE_PHOTO)) {
                AnSocialFile file = (AnSocialFile) params.get(ANSOCIALFILE_PHOTO);
                try {
                    jsonParams.put(ANSOCIALFILE_PHOTO, file.getFileName());
                } catch (JSONException e) {
                    handlerFailure("Invalid photo", ArrownockException.SOCIAL_INVALID_PHOTO, callback);
                    return;
                }
                if (null != file.getData()) {
                    client.post(null, path, jsonParams, file.getData(), callback);
                } else if (null != file.getInputStream()) {
                    client.post(null, path, jsonParams, file.getInputStream(), callback);
                }
                // post with file
            } else if (null != params.get(ANSOCIALFILE_FILE)) {
                AnSocialFile file = (AnSocialFile) params.get(ANSOCIALFILE_FILE);
                try {
                    jsonParams.put(ANSOCIALFILE_FILE, file.getFileName());
                } catch (JSONException e) {
                    handlerFailure("Invalid file", ArrownockException.SOCIAL_INVALID_FILE, callback);
                    return;
                }
                if (null != file.getData()) {
                    client.post(null, path, jsonParams, file.getData(), callback);
                } else if (null != file.getInputStream()) {
                    client.post(null, path, jsonParams, file.getInputStream(), callback);
                }
                // normal post
            } else {
                client.post(null, path, jsonParams, callback);
            }
        }
    }
}
