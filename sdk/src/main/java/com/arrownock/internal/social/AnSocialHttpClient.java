package com.arrownock.internal.social;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Base64;

import com.arrownock.exception.ArrownockException;
import com.arrownock.internal.util.Constants;
import com.arrownock.social.IAnSocialCallback;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class AnSocialHttpClient {
    private final static AnSocialHostnameVerifier hostnameVerifier = new AnSocialHostnameVerifier();

    private String APP_KEY = null;
    private String API_SECRET = "";
    private AsyncHttpClient client = null;
    private boolean secure = true;
    private String socialHost = null;

    public AnSocialHttpClient() {
        client = new AsyncHttpClient();
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public void setAppKey(String appKey) {
        APP_KEY = appKey;
    }
    
    public void setApiSecret(String apiSecret) {
    	API_SECRET = apiSecret;
    }

    public void setHost(String socialHost) {
        this.socialHost = socialHost;
    }

    public void setMillisTimeout(int millisTimeout) {
        client.setTimeout(millisTimeout);
    }

    public void get(final Context ctx, final String urlString, JSONObject params, final IAnSocialCallback callback) throws ArrownockException {
        Map<String, String> stringParams = getStringParams(params);
        stringParams.put("key", APP_KEY);
        RequestParams requestParams = null;
        BasicHeader[] headers = null;
        if(!"".equals(API_SECRET)) {
        	requestParams = new RequestParams();
        	stringParams.put("req_checker", "1");
        	
        	String encryptedParams = encryptParameters(API_SECRET, stringParams);
        	if(encryptedParams == null) {
        		throw new ArrownockException("API Secret is invalid.", ArrownockException.SOCIAL_INVALID_API_SECRET);
        	}
        	requestParams.put("d", encryptedParams);
        	BasicHeader authHeader = new BasicHeader("an-auth-token" , APP_KEY);
        	headers = new BasicHeader[]{authHeader};
        } else {
        	requestParams = new RequestParams(stringParams);
        }
        
        client.get(ctx, getAbsoluteUrl(urlString), headers, requestParams, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(Throwable e, final JSONObject errorResponse) {
                if (null != callback) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(errorResponse);
                        }
                    });
                    t.run();
                }
            }

            @Override
            public void onSuccess(int statusCode, final JSONObject response) {
                if (null != callback) {
                    Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            callback.onSuccess(response);
                        }
                    });
                    t.run();
                }
            }

            @Override
            public void onFailure(Throwable e, String content) {
                final JSONObject response = new JSONObject();
                JSONObject meta = new JSONObject();
                try {
                    meta.put("status", "fail");
                    meta.put("message", content);
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

            @Override
            public void onFinish() {
            }

            @Override
            public void onStart() {
            }
        });
    }

    public void post(final Context ctx, final String urlString, JSONObject params, final IAnSocialCallback callback) throws ArrownockException {
        Map<String, String> stringParams = getStringParams(params);
        stringParams.put("key", APP_KEY);
        RequestParams requestParams = null;
        BasicHeader[] headers = null;
        if(!"".equals(API_SECRET)) {
        	requestParams = new RequestParams();
        	stringParams.put("req_checker", "1");
        	
        	String encryptedParams = encryptParameters(API_SECRET, stringParams);
        	if(encryptedParams == null) {
        		throw new ArrownockException("API Secret is invalid.", ArrownockException.SOCIAL_INVALID_API_SECRET);
        	}
        	requestParams.put("d", encryptedParams);
        	BasicHeader authHeader = new BasicHeader("an-auth-token" , APP_KEY);
        	headers = new BasicHeader[]{authHeader};
        } else {
        	requestParams = new RequestParams(stringParams);
        }
        
        client.post(ctx, getAbsoluteUrl(urlString), headers, requestParams, null, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(Throwable e, final JSONObject errorResponse) {
                if (null != callback) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(errorResponse);
                        }
                    });
                    t.run();
                }
            }

            @Override
            public void onSuccess(int statusCode, final JSONObject response) {
                if (null != callback) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(response);
                        }
                    });
                    t.run();
                }
            }

            @Override
            public void onFailure(Throwable e, String content) {
                final JSONObject response = new JSONObject();
                JSONObject meta = new JSONObject();
                try {
                    meta.put("status", "fail");
                    meta.put("message", content);
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

            @Override
            public void onFinish() {
            }

            @Override
            public void onStart() {
            }
        });
    }

    public void post(final Context ctx, final String urlString, JSONObject params, byte[] data, final IAnSocialCallback callback) throws ArrownockException {
        boolean isFile = urlString.contains("files");
        String fileName = "";
        try {
            if (isFile) {
                fileName = params.getString("file");
            } else {
                fileName = params.getString("photo");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            handlerFailure("FileName is invalid", ArrownockException.SOCIAL_INVALID_FILENAME, callback);
            return;
        }
        if ("".equals(fileName)) {
            handlerFailure("FileName is invalid", ArrownockException.SOCIAL_INVALID_FILENAME, callback);
            return;
        }
        
        Map<String, String> stringParams = getStringParams(params);
        stringParams.put("key", APP_KEY);
        RequestParams requestParams = null;
        BasicHeader[] headers = null;
        if(!"".equals(API_SECRET)) {
        	requestParams = new RequestParams();
        	stringParams.put("req_checker", "1");
        	
        	String encryptedParams = encryptParameters(API_SECRET, stringParams);
        	if(encryptedParams == null) {
        		throw new ArrownockException("API Secret is invalid.", ArrownockException.SOCIAL_INVALID_API_SECRET);
        	}
        	requestParams.put("d", encryptedParams);
        	BasicHeader authHeader = new BasicHeader("an-auth-token" , APP_KEY);
        	headers = new BasicHeader[]{authHeader};
        } else {
        	requestParams = new RequestParams(stringParams);
        }
        requestParams.put(isFile ? "file" : "photo", new ByteArrayInputStream(data), fileName);
        
        final RequestParams finalParams = requestParams;
        final BasicHeader[] finalHeaders = headers;
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                client.post(ctx, getAbsoluteUrl(urlString), finalHeaders, finalParams, null, new JsonHttpResponseHandler() {

                    @Override
                    public void onFailure(Throwable e, final JSONObject errorResponse) {
                        if (null != callback) {
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onFailure(errorResponse);
                                }
                            });
                            t.run();
                        }
                    }

                    @Override
                    public void onSuccess(int statusCode, final JSONObject response) {
                        if (null != callback) {
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(response);
                                }
                            });
                            t.run();
                        }
                    }

                    @Override
                    public void onFailure(Throwable e, String content) {
                        final JSONObject response = new JSONObject();
                        JSONObject meta = new JSONObject();
                        try {
                            meta.put("status", "fail");
                            meta.put("message", content);
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

                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onStart() {
                    }
                });
            }
        });

        t.run();
    }

    public void post(final Context ctx, final String urlString, JSONObject params, InputStream data, final IAnSocialCallback callback) throws ArrownockException {
        boolean isFile = urlString.contains("files");
        String fileName = "";
        try {
            if (isFile) {
                fileName = params.getString("file");
            } else {
                fileName = params.getString("photo");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            handlerFailure("FileName is invalid", ArrownockException.SOCIAL_INVALID_FILENAME, callback);
            return;
        }
        if ("".equals(fileName)) {
            handlerFailure("FileName is invalid", ArrownockException.SOCIAL_INVALID_FILENAME, callback);
            return;
        }
        
        Map<String, String> stringParams = getStringParams(params);
        stringParams.put("key", APP_KEY);
        RequestParams requestParams = null;
        BasicHeader[] headers = null;
        if(!"".equals(API_SECRET)) {
        	requestParams = new RequestParams();
        	stringParams.put("req_checker", "1");
        	
        	String encryptedParams = encryptParameters(API_SECRET, stringParams);
        	if(encryptedParams == null) {
        		throw new ArrownockException("API Secret is invalid.", ArrownockException.SOCIAL_INVALID_API_SECRET);
        	}
        	requestParams.put("d", encryptedParams);
        	BasicHeader authHeader = new BasicHeader("an-auth-token" , APP_KEY);
        	headers = new BasicHeader[]{authHeader};
        } else {
        	requestParams = new RequestParams(stringParams);
        }
        requestParams.put(isFile ? "file" : "photo", data, fileName);
        
        final RequestParams finalParams = requestParams;
        final BasicHeader[] finalHeaders = headers;
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                client.post(ctx, getAbsoluteUrl(urlString), finalHeaders, finalParams, null, new JsonHttpResponseHandler() {

                    @Override
                    public void onFailure(Throwable e, final JSONObject errorResponse) {
                        if (null != callback) {
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onFailure(errorResponse);
                                }
                            });
                            t.run();
                        }
                    }

                    @Override
                    public void onSuccess(int statusCode, final JSONObject response) {
                        if (null != callback) {
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(response);
                                }
                            });
                            t.run();
                        }
                    }

                    @Override
                    public void onFailure(Throwable e, String content) {
                        final JSONObject response = new JSONObject();
                        JSONObject meta = new JSONObject();
                        try {
                            meta.put("status", "fail");
                            meta.put("message", content);
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

                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onStart() {
                    }

                });
            }
        });

        t.run();
    }

    private RequestParams getRequestParams(JSONObject params) throws ArrownockException {
        if (params == null) {
            return new RequestParams();
        }

        RequestParams requestParams = null;
        Map<String, Object> objectMap = new HashMap<String, Object>();

        Iterator<?> keys = params.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            Object object = null;
            try {
                object = params.get(key);
            } catch (JSONException e) {
                e.printStackTrace();
                throw new ArrownockException("Invalid JSON format of " + key, e, ArrownockException.SOCIAL_INVALID_JSON);
            }
            if (object != null) {
                objectMap.put(key, object);
            }
        }

        Map<String, String> stringParams = new HashMap<String, String>();
        // 如果object是JSONArray或者JSONObject，需要对params进行一个组装，采用递归的方法
        for (String key : objectMap.keySet()) {
            if (objectMap.get(key) instanceof JSONArray || objectMap.get(key) instanceof JSONObject) {
                try {
                    setParams("", key, objectMap.get(key), stringParams);
                } catch (Exception e) {
                    throw new ArrownockException("Invalid JSON format of " + key, e,
                            ArrownockException.SOCIAL_INVALID_JSON);
                }
            } else {
                stringParams.put(key, objectMap.get(key).toString());
            }
        }
        requestParams = new RequestParams(stringParams);

        return requestParams;
    }
    
    private Map<String, String> getStringParams(JSONObject params) throws ArrownockException {
        if (params == null) {
            return new HashMap<String, String>();
        }

        Map<String, Object> objectMap = new HashMap<String, Object>();
        Iterator<?> keys = params.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            Object object = null;
            try {
                object = params.get(key);
            } catch (JSONException e) {
                e.printStackTrace();
                throw new ArrownockException("Invalid JSON format of " + key, e, ArrownockException.SOCIAL_INVALID_JSON);
            }
            if (object != null) {
                objectMap.put(key, object);
            }
        }

        Map<String, String> stringParams = new HashMap<String, String>();
        // 如果object是JSONArray或者JSONObject，需要对params进行一个组装，采用递归的方法
        for (String key : objectMap.keySet()) {
            if (objectMap.get(key) instanceof JSONArray || objectMap.get(key) instanceof JSONObject) {
                try {
                    setParams("", key, objectMap.get(key), stringParams);
                } catch (Exception e) {
                    throw new ArrownockException("Invalid JSON format of " + key, e,
                            ArrownockException.SOCIAL_INVALID_JSON);
                }
            } else {
                stringParams.put(key, objectMap.get(key).toString());
            }
        }
        
        return stringParams;
    }

    // JSONArray,最后组装出来的结果形如: key[0]=value1; key[1][0]=value2
    // JSONObject,最后组装出来的结果形如: key[jsKey]=jsValue1;key[jsKey][jsChildKey]=jsValue2
    private String setParams(String keys, String key, Object object, Map<String, String> stringParams) throws Exception {
        if (object instanceof JSONArray) {
            JSONArray array = (JSONArray) object;
            String[] keyArray = keys.split(",");
            String keyStr = key;
            for (int i = 0; i < array.length(); i++) {
                if (!"".equals(keys)) {
                    keyStr = key;
                    for (int j = 0; j < keyArray.length; j++) {
                        keyStr = keyStr + "[" + keyArray[j] + "]";
                    }
                }
                if ("".equals(keys)) {
                    stringParams.put(keyStr + "[" + i + "]", setParams(i + "", key, array.get(i), stringParams));
                } else {
                    stringParams.put(keyStr + "[" + i + "]", setParams(keys + "," + i, key, array.get(i), stringParams));
                }
            }
            return null;
        } else if (object instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) object;
            Iterator it = jsonObject.keys();
            String keyStr = key;
            String[] keyArray = keys.split(",");
            while (it.hasNext()) {
                String jsonKey = (String) it.next();
                Object jsonValue = jsonObject.get(jsonKey);
                keyStr = key;
                if (!"".equals(keys)) {
                    for (int j = 0; j < keyArray.length; j++) {
                        keyStr = keyStr + "[" + keyArray[j] + "]";
                    }
                }
                if ("".equals(keys)) {
                    stringParams.put(keyStr + "[" + jsonKey + "]", setParams(jsonKey, key, jsonValue, stringParams));
                } else {
                    stringParams.put(keyStr + "[" + jsonKey + "]", setParams(keys + "," + jsonKey, key, jsonValue, stringParams));
                }
            }
            return null;
        } else {
            return object.toString();
        }
    }

    private String getAbsoluteUrl(String relativeUrl) {
        if (relativeUrl == null) {
            relativeUrl = "";
        }

        String baseURL = null;
        if (secure) {
            baseURL = Constants.HTTPS + getHost() + "/" + Constants.SOCIAL_VERSION + "/";
            try {
                client.setSSLSocketFactory(AnSocialCASSLSocketFactory.getCASocketFactory());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            baseURL = Constants.HTTP + getHost() + "/" + Constants.SOCIAL_VERSION + "/";
        }

        String result = baseURL + relativeUrl; // + "?key=" + APP_KEY;
        return result;
    }

    private String getHost() {
        return socialHost == null ? Constants.SOCIAL_HOST : socialHost;
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
    
    private String encryptParameters(String privateKey, Map<String, String> params) {
        try {
        	StringBuilder stringBuilder = new StringBuilder();
        	for (String key : params.keySet()) {
        		if (stringBuilder.length() > 0) {
        			stringBuilder.append("&");
        		}
        		String value = params.get(key);
        		stringBuilder.append((key != null ? key : ""));
        		stringBuilder.append("=");
        		if (value != null) {
        		    value = URLEncoder.encode(value, "utf-8");
        		    stringBuilder.append(value);
        		} else {
        		    stringBuilder.append("");
        		}
        	}
        	String value = stringBuilder.toString();
        
        	// hash private key
        	String pkey = null;
    		MessageDigest md = MessageDigest.getInstance("SHA-256");
    	    md.update(privateKey.getBytes("UTF-8"));
    	    byte[] digest = md.digest();
    	    StringBuffer result = new StringBuffer();
    	    for (byte b : digest) {
    	        result.append(String.format("%02x", b)); //convert to hex
    	    }
    	    if(32 > result.toString().length()) {
    	    	pkey = result.toString();
    	    } else {
    	    	pkey = result.toString().substring(0, 32);
    	    }
    	    
    	    int index = new Random().nextInt(8);
    	    String iv = pkey.substring(index, index + 16);
    	    String _out = "";
    	    byte[] _key = new byte[32];
    	    byte[] _iv = new byte[16];
    		int len = pkey.getBytes("UTF-8").length; // length of the key	provided
    		if (pkey.getBytes("UTF-8").length > _key.length) {
    			len = _key.length;
    		}
    		int ivlen = iv.getBytes("UTF-8").length;
    		if(iv.getBytes("UTF-8").length > _iv.length) {
    			ivlen = _iv.length;
    		}
    		System.arraycopy(pkey.getBytes("UTF-8"), 0, _key, 0, len);
    		System.arraycopy(iv.getBytes("UTF-8"), 0, _iv, 0, ivlen);
    		SecretKeySpec keySpec = new SecretKeySpec(_key, "AES");
    		IvParameterSpec ivSpec = new IvParameterSpec(_iv);

    		// encryption
    		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
    		byte[] results = cipher.doFinal(value.getBytes("UTF-8"));
    		_out = String.valueOf((index + 1)) + Base64.encodeToString(results, Base64.DEFAULT);
    		return _out; 
        } catch (Exception ex) {
            // mute
        }
        return null;
    }
}
