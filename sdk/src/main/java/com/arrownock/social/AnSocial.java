package com.arrownock.social;

import android.content.Context;
import android.util.Log;

import com.arrownock.exception.ArrownockException;
import com.arrownock.internal.device.DeviceManager;
import com.arrownock.internal.social.AnSocialHttpClientWrapper;
import com.arrownock.internal.util.Constants;

import java.util.Map;

public class AnSocial {
    private static final int DEFAULT_TIMEOUT = 60000;
    private AnSocialHttpClientWrapper httpClientWrapper = null;
    private Context context = null;

    public AnSocial(Context androidContext, String key) throws ArrownockException {
        this.context = androidContext;
        if (key == null || "".equals(key.trim())) {
            throw new ArrownockException("Invalid value of " + Constants.APP_KEY,
                    ArrownockException.SOCIAL_INVALID_APP_KEY);
        }
        httpClientWrapper = new AnSocialHttpClientWrapper(key, androidContext);
        httpClientWrapper.setTimeout(DEFAULT_TIMEOUT);

        if (Constants.DM_ENABLED) {
        	try {
        		DeviceManager.getInstance(androidContext, key).reportDeviceData();
        	} catch (Exception e) {
				Log.w("DeviceManager", e.getMessage());
			}
        }
    }

    public void setSecureConnection(Boolean secure) {
        httpClientWrapper.setSecure(secure);
    }

    public void setTimeout(int millisTimeout) {
        httpClientWrapper.setTimeout(millisTimeout);
    }

    public void setHost(String socialHost) throws ArrownockException {
        httpClientWrapper.setHost(socialHost);
    }
    
    public void setAPISecret(String secret) throws ArrownockException {
        httpClientWrapper.setApiSecret(secret);
    }

    public void sendRequest(String path, AnSocialMethod methodType, Map<String, Object> params,
            IAnSocialCallback callback) throws ArrownockException {
        httpClientWrapper.sendRequest(path, methodType, params, callback);
    }
}
