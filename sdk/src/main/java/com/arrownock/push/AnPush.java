package com.arrownock.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.arrownock.exception.ArrownockException;
import com.arrownock.internal.device.DeviceManager;
import com.arrownock.internal.push.LogUtil;
import com.arrownock.internal.util.Constants;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

public class AnPush {
	private final static String LOG_NAME = "ArrownockSDK";
	private final static String LOG_TAG = AnPush.class.getName();
	private final static String SERVICE_TYPE = "com.arrownock.push.SERVICE_TYPE";
	private final static String GCM_SENDER_ID = "com.arrownock.push.gcm.SENDER_ID";
	private final static String INTERVAL_WIFI = "com.arrownock.push.KEEPALIVE_INTERVAL_WIFI";
	private final static String INTERVAL_2G = "com.arrownock.push.KEEPALIVE_INTERVAL_2G";
	private final static String INTERVAL_3G = "com.arrownock.push.KEEPALIVE_INTERVAL_3G";
	private final static String GCM_TYPE = "GCM";
	private final static String ARROWNOCK_TYPE = "ARROWNOCK";
	
	private final static String ENABLE_MIPUSH = "com.arrownock.push.ENABLE_MIPUSH";
	public final static String MIPUSH_APPID = "com.arrownock.push.MIPUSH_APPID";
	public final static String MIPUSH_APPKEY = "com.arrownock.push.MIPUSH_APPKEY";
	public final static String MIPUSH_SERVICE_STATUS = "com.arrownock.push.MIPUSH_SERVICE_STATUS";
	
	private static AnPush instance;
	private String serviceType = null;
	private String appKey = null;
	private String regChannels = null;
	private Context androidContext;
	private IAnPushCallback callback = null;
	private String senderId = null;
	private String deviceId = null;
	private IAnPushUtility util = null;
	private long intervalWiFi = 4 * 60 * 1000;
	private long interval2G = 4 * 60 * 1000;
	private long interval3G = 4 * 60 * 1000;
	private boolean enableMiPush = false;
	
	private AnPush(Context androidContext) throws ArrownockException {
		this.androidContext = androidContext;
		util = new AnPushUtility(androidContext);
		try{
		    ApplicationInfo ai = androidContext.getPackageManager().getApplicationInfo(androidContext.getPackageName(), PackageManager.GET_META_DATA);
		    Bundle bundle = ai.metaData;
		    if (bundle != null) {
        		serviceType = bundle.getString(SERVICE_TYPE);
        		senderId = bundle.getString(GCM_SENDER_ID);
        		setAppKey(bundle.getString(Constants.APP_KEY));
        		if(bundle.containsKey(ENABLE_MIPUSH)) {
        		    enableMiPush = bundle.getBoolean(ENABLE_MIPUSH);
        		}
        		if(enableMiPush && isMIUI()) {
        			if(bundle.getString(MIPUSH_APPID) == null || bundle.getString(MIPUSH_APPKEY) == null) {
        				throw new Exception("MiPush APPID or APPKEY is empty.");
        		   	}
        		}
        		intervalWiFi = bundle.getInt(INTERVAL_WIFI) == 0 ? intervalWiFi : bundle.getInt(INTERVAL_WIFI) * 60 * 1000 ;
        		interval2G = bundle.getInt(INTERVAL_2G) == 0 ? interval2G : bundle.getInt(INTERVAL_2G) * 60 * 1000;
        		interval3G = bundle.getInt(INTERVAL_3G) == 0 ? interval3G : bundle.getInt(INTERVAL_3G) * 60 * 1000;
        		Log.d(LOG_TAG, "Customized keepalive interval: wifi=" + intervalWiFi + " ; 2G=" + interval2G + " ; 3G=" + interval3G);
        		
        		Editor editor = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE).edit();
				editor.putLong(PushService.PREF_INTERVAL_WIFI, intervalWiFi);
				editor.putLong(PushService.PREF_INTERVAL_2G, interval2G);
				editor.putLong(PushService.PREF_INTERVAL_3G, interval3G);
				if(bundle.containsKey(MIPUSH_APPID)) {
		    		    editor.putString(MIPUSH_APPID, bundle.getString(MIPUSH_APPID));
				}
				if(bundle.containsKey(MIPUSH_APPKEY)) {
		    		    editor.putString(MIPUSH_APPKEY, bundle.getString(MIPUSH_APPKEY));
				}
				editor.commit();
		    }
		} catch (Exception e) {
		    throw new ArrownockException("Failed to initialize SDK.", e, ArrownockException.PUSH_FAILED_INITIALIZE);
		}
		if (serviceType == null) {
			serviceType = ARROWNOCK_TYPE;
		}
		if (!(GCM_TYPE.equals(serviceType)) && !(ARROWNOCK_TYPE.equals(serviceType))) {
			throw new ArrownockException("Invalid value of " + SERVICE_TYPE + ". Should be GCM or ARROWNOCK", ArrownockException.PUSH_INVALID_SERVICE_TYPE);
		}
		if (GCM_TYPE.equals(serviceType) && (senderId == null || "".equals(senderId.trim()))) {
			throw new ArrownockException(GCM_SENDER_ID + " must be set for GCM service", ArrownockException.PUSH_INVALID_GCM_SENDER_ID);
		}
		if (ARROWNOCK_TYPE.equals(serviceType)) {
			try {
				if(isServiceEnabled() && !isConnected()) {
				    if (!isMIUI()) {
				        enable();
				    }
				}
			} catch(Exception e) {
				// mute
			}
		}
	}
	
	public static AnPush getInstance(Context androidContext) throws ArrownockException {
		if (instance == null) {
			instance = new AnPush(androidContext);
		}
		return instance;
	}
	
	public void setAppKey(String appKey) {
		this.appKey = appKey;
		if(Constants.DM_ENABLED) {
			try {
				DeviceManager.getInstance(androidContext, appKey).reportDeviceData();
			} catch (Exception e) {
				Log.w("DeviceManager", e.getMessage());
			}
		}
	}
	
	public void setSecureConnection(boolean isSecure) {
		Editor editor = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE).edit();
		editor.putBoolean(PushService.PREF_SECURE_CONNECTION, isSecure);
		editor.commit();
	}
	
	public boolean isSecureConnection() {
		SharedPreferences pref = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE);
		return (pref.getBoolean(PushService.PREF_SECURE_CONNECTION, true));
	}
	
	public void setCallback(IAnPushCallback callback) {
		this.callback = callback;
	}
	
	public IAnPushCallback getCallback() {
		return this.callback;
	}
	
	public void setId(String id) throws ArrownockException {
		if (id == null || "".equals(id.trim())) {
			throw new ArrownockException("Cannot set empty value as device id", ArrownockException.PUSH_INVALID_DEVICE_ID);
		}
		this.deviceId = id.trim();
	}
	
	public void setHosts(String api, String ds) {
		if(api != null && !"".equals(api.trim()) && ds != null && !"".equals(ds.trim())) {
			Editor editor = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE).edit();
			editor.putString(PushService.PREF_API_HOST, api);
			editor.putString(PushService.PREF_DS_HOST, ds);
			editor.commit();
		}
	}
	
	public void setSecret(String secret) {
		if(secret != null && !"".equals(secret.trim())) {
			Editor editor = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE).edit();
			editor.putString(PushService.PREF_API_SECRET, secret);
			editor.commit();
		}
	}
	
	public void setNetworkKeepalive(int keepalive) {
		Editor editor = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE).edit();
		editor.putInt(PushService.PREF_KEEPALIVE, keepalive);
		editor.commit();
	}
	
	public void setSSLCertificates(String serverCert, String clientCert, String clientKey) throws ArrownockException {
		if(serverCert == null || "".equals(serverCert.trim())) {
			throw new ArrownockException("Invalid server certificate", ArrownockException.PUSH_INVALID_CERTIFICATE);
		}
		if(clientCert == null || "".equals(clientCert.trim())) {
			throw new ArrownockException("Invalid client certificate", ArrownockException.PUSH_INVALID_CERTIFICATE);
		}
		if(clientKey == null || "".equals(clientKey.trim())) {
			throw new ArrownockException("Invalid client private key", ArrownockException.PUSH_INVALID_CERTIFICATE);
		}
		if (ARROWNOCK_TYPE.equals(serviceType) && isSecureConnection()) {
			if (androidContext == null) {
				throw new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT);
			}
			SharedPreferences pref = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE);
			String serverPublic = pref.getString(PushService.PREF_SERVER_CERT, "");
			String clientPublic = pref.getString(PushService.PREF_CLIENT_CERT, "");
			String clientPrivate= pref.getString(PushService.PREF_CLIENT_KEY, "");
			
			if(!serverCert.equals(serverPublic) || !clientCert.equals(clientPublic) || !clientKey.equals(clientPrivate)) {
				Editor editor = pref.edit();
				editor.putString(PushService.PREF_SERVER_CERT, serverCert);
				editor.putString(PushService.PREF_CLIENT_CERT, clientCert);
				editor.putString(PushService.PREF_CLIENT_KEY, clientKey);
				editor.commit();
				
				// restart the push service if it is currently connecting
				if(isConnected()) {
					Log.d(LOG_TAG, "SSL certificates changed, restarting push service...");
					PushService.actionRestart(androidContext);
				}
			}
		}
	}
	
	public String getSenderId() {
		return this.senderId;
	}
	
	/**
	 * @deprecated use {@link #register(List, IAnPushRegistrationCallback)} instead
	 */
	public void register(final List<String> channels) throws ArrownockException {
        register(channels, false);
    }
	
	public void register(final List<String> channels, final IAnPushRegistrationCallback callback) {
        register(channels, false, callback);
    }
	
	/**
     * @deprecated use {@link #register(List, boolean, IAnPushRegistrationCallback)} instead
     */
	public void register(final List<String> channels, final boolean overwrite) throws ArrownockException {
        if(androidContext == null) {
            throw new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT);
        }
        if(appKey == null || "".equals(appKey.trim())) {
            throw new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY);
        }
        if(channels == null || channels.size() == 0) {
            throw new ArrownockException("Device has to be registered with at list one channel", ArrownockException.PUSH_INVALID_CHANNELS);
        }
        String type = null;
        if(GCM_TYPE.equals(serviceType)) {
            type = "android-gcm";
        } else if(ARROWNOCK_TYPE.equals(serviceType)) {
            type = "android-arrownock";
            if(enableMiPush && isMIUI()) {
                type = "android-mipush";
            }
        } else {
            throw new ArrownockException("Wrong service type" + serviceType + "should be GCM or ARROWNOCK", ArrownockException.PUSH_INVALID_SERVICE_TYPE);
        }
        
        StringBuffer buffer = new StringBuffer();
        for(String channel : channels) {
            if(channel != null && !"".equals(channel.trim())) {
                buffer.append(channel.trim());
                buffer.append(",");
            }
        }
        if(buffer.length() == 1) {
            throw new ArrownockException("Device has to be registered with at list one channel", ArrownockException.PUSH_INVALID_CHANNELS);
        }
        regChannels = buffer.substring(0, buffer.length() - 1);
        util.registerDeviceAsync(appKey.trim(), regChannels, callback, overwrite, type, this.deviceId);
    }
	
	public void register(final List<String> channels, final boolean overwrite, final IAnPushRegistrationCallback callback) {
        if(androidContext == null) {
            callback.onError(new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT));
        }
        if(appKey == null || "".equals(appKey.trim())) {
            callback.onError(new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY));
        }
        if(channels == null || channels.size() == 0) {
            callback.onError(new ArrownockException("Device has to be registered with at list one channel", ArrownockException.PUSH_INVALID_CHANNELS));
        }
        String type = null;
        if(GCM_TYPE.equals(serviceType)) {
            type = "android-gcm";
        } else if(ARROWNOCK_TYPE.equals(serviceType)) {
            type = "android-arrownock";
            if(enableMiPush && isMIUI()) {
                type = "android-mipush";
            }
        } else {
            callback.onError(new ArrownockException("Wrong service type" + serviceType + "should be GCM or ARROWNOCK", ArrownockException.PUSH_INVALID_SERVICE_TYPE));
        }
        
        StringBuffer buffer = new StringBuffer();
        for(String channel : channels) {
            if(channel != null && !"".equals(channel.trim())) {
                buffer.append(channel.trim());
                buffer.append(",");
            }
        }
        if(buffer.length() == 1) {
            callback.onError(new ArrownockException("Device has to be registered with at list one channel", ArrownockException.PUSH_INVALID_CHANNELS));
        }
        regChannels = buffer.substring(0, buffer.length() - 1);
        util.registerDeviceAsync(appKey.trim(), regChannels, callback, overwrite, type, getAnID());
    }
	
	/**
	 * @deprecated use {@link #unregister(IAnPushRegistrationCallback)} instead
	 */
	public void unregister() throws ArrownockException {
		unregister(null, true);
	}
	
	public void unregister(final IAnPushRegistrationCallback callback) {
        unregister(null, true, callback);
    }
	
	/**
     * @deprecated use {@link #unregister(List, IAnPushRegistrationCallback)} instead
     */
	public void unregister(final List<String> channels) throws ArrownockException {
        if(channels == null || channels.size() == 0) {
            unregister(null, true);
        } else {
            unregister(channels, false);
        }
    }
	
	public void unregister(final List<String> channels, IAnPushRegistrationCallback callback) {
        if(channels == null || channels.size() == 0) {
            unregister(null, true, callback);
        } else {
            unregister(channels, false, callback);
        }
    }
	
	/**
     * @deprecated use {@link #unregister(List, boolean, IAnPushRegistrationCallback)} instead
     */
	private void unregister(final List<String> channels, final boolean removeDevice) throws ArrownockException {
		if(androidContext == null) {
			throw new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT);
		}
		if(appKey == null || "".equals(appKey.trim())) {
			throw new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY);
		}
		// Check whether using GCM or Arrownock
		String token = "";
		String type = null;
		
		if (ARROWNOCK_TYPE.equals(serviceType)) {
			token = util.getFromLocalStorage(PushService.PREF_DEVICE_TOKEN);
			type = "android-arrownock";
		} else if(GCM_TYPE.equals(serviceType)) {
			token = util.getFromLocalStorage("GCM_REG_ID");
			type = "android-gcm";
		}
		
		if("".equals(token)) {
			throw new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED);
		}
		
		if(!removeDevice) {
			if(channels == null || channels.size() == 0) {
				throw new ArrownockException("Has to be at least one channel for unregister", ArrownockException.PUSH_INVALID_CHANNELS);
			}
			
			StringBuffer buffer = new StringBuffer();
			for(String channel : channels) {
				if(channel != null && !"".equals(channel.trim())) {
					buffer.append(channel.trim());
					buffer.append(",");
				}
	 		}
			if(buffer.length() == 1) {
				throw new ArrownockException("Has to be at least one channel for unregister", ArrownockException.PUSH_INVALID_CHANNELS);
			}
			final String unregChannels = buffer.substring(0, buffer.length() - 1);
			
			util.unregisterDeviceAsync(token, appKey.trim(), unregChannels, callback, removeDevice, type);
		} else {
			util.unregisterDeviceAsync(token, appKey.trim(), null, callback, true, type);
		}
	}
	
	private void unregister(final List<String> channels, final boolean removeDevice, IAnPushRegistrationCallback callback) {
        if(androidContext == null) {
            callback.onError(new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT));
        }
        if(appKey == null || "".equals(appKey.trim())) {
            callback.onError(new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY));
        }
        // Check whether using GCM or Arrownock
        String token = "";
        String type = null;
        
        if (ARROWNOCK_TYPE.equals(serviceType)) {
            token = util.getFromLocalStorage(PushService.PREF_DEVICE_TOKEN);
            type = "android-arrownock";
        } else if(GCM_TYPE.equals(serviceType)) {
            token = util.getFromLocalStorage("GCM_REG_ID");
            type = "android-gcm";
        }
        
        if("".equals(token)) {
            callback.onError(new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED));
        }
        
        if(!removeDevice) {
            if(channels == null || channels.size() == 0) {
                callback.onError(new ArrownockException("Has to be at least one channel for unregister", ArrownockException.PUSH_INVALID_CHANNELS));
            }
            
            StringBuffer buffer = new StringBuffer();
            for(String channel : channels) {
                if(channel != null && !"".equals(channel.trim())) {
                    buffer.append(channel.trim());
                    buffer.append(",");
                }
            }
            if(buffer.length() == 1) {
                callback.onError(new ArrownockException("Has to be at least one channel for unregister", ArrownockException.PUSH_INVALID_CHANNELS));
            }
            final String unregChannels = buffer.substring(0, buffer.length() - 1);
            
            util.unregisterDeviceAsync(token, appKey.trim(), unregChannels, callback, removeDevice, type);
        } else {
            util.unregisterDeviceAsync(token, appKey.trim(), null, callback, true, type);
        }
    }
	
	/**
	 * Start push service
	 * @param androidContext Android Context
	 * @throws ArrownockException PushService related exception. Device token needs to be generated before starting service
	 */
	public void enable() throws ArrownockException {
        //get anid then register anid without channel
        String type = "";
        if(GCM_TYPE.equals(serviceType)) {
            type = "android-gcm";
            util.registerInEnable(androidContext, appKey, type, 0, this.deviceId, new IAnPushRegisterAnIdCallback() {
                @Override
                public void onSuccess() {
                    LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "registerInEnable successful in android-gcm");
                }
                @Override
                public void onError(ArrownockException e, int count, String type) {
                    if (count >= PushConstants.MAX_REQUEST_TIME) {
                        LogUtil.getInstance().error(LOG_TAG, LOG_NAME, "registerInEnable failed. reason: over limit");
                        if (getCallback() != null){
                            getCallback().statusChanged(AnPushStatus.DISABLE, new ArrownockException("registerInEnable failed.", ArrownockException.PUSH_FAILED_REGISTER));
                        }
                    } else {
                        util.registerInEnable(androidContext, appKey, type, ++count, deviceId, this);
                    }
                }
            });
        } else if (ARROWNOCK_TYPE.equals(serviceType)) {
            if (androidContext == null) {
                throw new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT);
            }
            if(enableMiPush && isMIUI()) {
                type = "android-mipush";
                util.registerInEnable(androidContext, appKey, type, 0, this.deviceId, new IAnPushRegisterAnIdCallback() {
                    @Override
                    public void onSuccess() {
                        LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "registerInEnable successful in android-mipush");
                        MiPushClient.resumePush(androidContext, null);
                    }
                    @Override
                    public void onError(ArrownockException e, int count, String type) {
                        if (count >= PushConstants.MAX_REQUEST_TIME) {
                            LogUtil.getInstance().error(LOG_TAG, LOG_NAME, "registerInEnable failed. reason: over limit");
                            if (getCallback() != null){
                                getCallback().statusChanged(AnPushStatus.DISABLE, new ArrownockException("registerInEnable failed.", ArrownockException.PUSH_FAILED_REGISTER));
                            }
                        } else {
                            util.registerInEnable(androidContext, appKey, type, ++count, deviceId, this);
                        }
                    }
                });
            } else {
                type = "android-arrownock";
                util.registerInEnable(androidContext, appKey, type, 0, this.deviceId, new IAnPushRegisterAnIdCallback() {
                    @Override
                    public void onSuccess() {
                        LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "registerInEnable successful in android-arrownock");
                        SharedPreferences pref = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE);
                        String token = pref.getString(PushService.PREF_DEVICE_TOKEN, null);
                        if (token == null || "".equals(token.trim())){
                            if (getCallback() != null){
                                getCallback().statusChanged(AnPushStatus.DISABLE, new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED));
                            }
                        }
                        LogUtil.getInstance().debug(LOG_NAME, "Call push service start");
                        PushService.actionStart(androidContext);
                    }
                    @Override
                    public void onError(ArrownockException e, int count, String type) {
                        if (count >= PushConstants.MAX_REQUEST_TIME) {
                            LogUtil.getInstance().error(LOG_TAG, LOG_NAME, "registerInEnable failed. reason: over limit");
                            if (getCallback() != null){
                                getCallback().statusChanged(AnPushStatus.DISABLE, new ArrownockException("registerInEnable failed.", ArrownockException.PUSH_DEVICE_NOT_REGISTERED));
                            }
                        } else {
                            util.registerInEnable(androidContext, appKey, type, ++count, deviceId, this);
                        }
                    }
                });
            }
        }
    }

	/**
	 * Stop push service
	 * @param androidContext Android Context
	 */
	public void disable() {
		if (ARROWNOCK_TYPE.equals(serviceType)) {
			if(enableMiPush && isMIUI()) {
//				SharedPreferences pref = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE);
//				pref.edit().putBoolean(MIPUSH_SERVICE_STATUS, false).commit();
				MiPushClient.pausePush(androidContext, null);
			} else {
				LogUtil.getInstance().debug(LOG_NAME, "Call push service stop.");
				PushService.actionStop(androidContext);
			}
		}
	}

	/**
	 * Check if push service is enabled
	 * @param androidContext Android Context
	 * @throws ArrownockException 
	 */
	public boolean isEnabled() {
		boolean isEnabled = false;
		if (ARROWNOCK_TYPE.equals(serviceType)) {
			try {
				isEnabled = isServiceEnabled();
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				isEnabled = !util.getRegistrationId(androidContext).isEmpty();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return isEnabled;
	}
	
	/**
	 * Check if device is currently connecting to push notification server
	 */
	public boolean isConnected() {
		return PushService.isEnabled();
	}

	boolean isServiceEnabled() throws ArrownockException {
		if (ARROWNOCK_TYPE.equals(serviceType)) {
			if(!enableMiPush || !isMIUI()) {
				if (androidContext == null) {
					throw new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT);
				}
				SharedPreferences p = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_MULTI_PROCESS);
				boolean started = p.getBoolean(PushService.PREF_ENABLED, false);
				return started;
			}
		}
		return true;
	}
	
	/**
     * @deprecated use {@link #setMute(IAnPushSettingsCallback)} instead
     */
	public void setMute() throws ArrownockException {
		if(androidContext == null) {
			throw new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT);
		}
		if(appKey == null || "".equals(appKey.trim())) {
			throw new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY);
		}
		
		String token = getDeviceToken();
		if("".equals(token)) {
			throw new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED);
		}
		util.setMuteAsync(appKey.trim(), token, callback);
	}
	
	public void setMute(IAnPushSettingsCallback callback) {
        if(androidContext == null) {
            callback.onError(new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT));
        }
        if(appKey == null || "".equals(appKey.trim())) {
            callback.onError(new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY));
        }
        
        String token = getDeviceToken();
        if("".equals(token)) {
            callback.onError(new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED));
        }
        util.setMuteAsync(appKey.trim(), token, callback);
    }

	/**
	 * @deprecated use {@link #setScheduledMute(int, int, int, IAnPushSettingsCallback)} instead
	 */
	public void setScheduledMute(int startHour, int startMinute, int duration) throws ArrownockException {
        if(androidContext == null) {
            throw new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT);
        }
        if(appKey == null || "".equals(appKey.trim())) {
            throw new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY);
        }
        
        String token = getDeviceToken();
        if("".equals(token)) {
            throw new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED);
        }
        // validate time, hour[0, 23], minute[0, 59], duration[0, 1439]
        if ((startHour < 0 || startHour > 23) || (startMinute < 0 || startMinute > 59) || (duration < 0 || duration > 1439)) {
            throw new ArrownockException("Invalid parameter, valid time should be startHour[0, 23], startMinute[0, 59], duration[0, 1439]", ArrownockException.PUSH_INVALID_TIME_RANGE);
        }
        
        // change startHour in Local time to UTC time
        Calendar start = Calendar.getInstance(TimeZone.getDefault());
        start.set(Calendar.HOUR_OF_DAY, startHour);
        start.set(Calendar.MINUTE, startMinute);
        start.set(Calendar.SECOND, 0);
        Calendar startGMT = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        startGMT.setTime(start.getTime());
        int startGMTHour = startGMT.get(Calendar.HOUR_OF_DAY);
        int startGMTMinute = startGMT.get(Calendar.MINUTE);
        util.setScheduledMuteAsync(appKey.trim(), token, startGMTHour, startGMTMinute, duration, callback);
    }
	
	public void setScheduledMute(int startHour, int startMinute, int duration, IAnPushSettingsCallback callback) throws ArrownockException {
        if(androidContext == null) {
            callback.onError(new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT));
        }
        if(appKey == null || "".equals(appKey.trim())) {
            callback.onError(new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY));
        }
        
        String token = getDeviceToken();
        if("".equals(token)) {
            callback.onError(new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED));
        }
        // validate time, hour[0, 23], minute[0, 59], duration[0, 1439]
        if ((startHour < 0 || startHour > 23) || (startMinute < 0 || startMinute > 59) || (duration < 0 || duration > 1439)) {
            callback.onError(new ArrownockException("Invalid parameter, valid time should be startHour[0, 23], startMinute[0, 59], duration[0, 1439]", ArrownockException.PUSH_INVALID_TIME_RANGE));
        }
        
        // change startHour in Local time to UTC time
        Calendar start = Calendar.getInstance(TimeZone.getDefault());
        start.set(Calendar.HOUR_OF_DAY, startHour);
        start.set(Calendar.MINUTE, startMinute);
        start.set(Calendar.SECOND, 0);
        Calendar startGMT = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        startGMT.setTime(start.getTime());
        int startGMTHour = startGMT.get(Calendar.HOUR_OF_DAY);
        int startGMTMinute = startGMT.get(Calendar.MINUTE);
        util.setScheduledMuteAsync(appKey.trim(), token, startGMTHour, startGMTMinute, duration, callback);
    }

	/**
	 * @deprecated use {@link #clearMute(IAnPushSettingsCallback)} instead
	 */
	public void clearMute() throws ArrownockException {
        if(androidContext == null) {
            throw new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT);
        }
        if(appKey == null || "".equals(appKey.trim())) {
            throw new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY);
        }
        
        String token = getDeviceToken();
        if("".equals(token)) {
            throw new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED);
        }
        
        util.clearMuteAsync(appKey.trim(), token, callback);
    }
	
	public void clearMute(IAnPushSettingsCallback callback) {
        if(androidContext == null) {
            callback.onError(new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT));
        }
        if(appKey == null || "".equals(appKey.trim())) {
            callback.onError(new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY));
        }
        String token = getDeviceToken();
        if("".equals(token)) {
            callback.onError(new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED));
        }
        
        util.clearMuteAsync(appKey.trim(), token, callback);
    }

	/**
	 * @deprecated use {@link #setSilentPeriod(int, int, int, boolean, IAnPushSettingsCallback)} instead
	 */
	public void setSilentPeriod(int startHour, int startMinute, int duration, boolean resend) throws ArrownockException {
		if(androidContext == null) {
			throw new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT);
		}
		if(appKey == null || "".equals(appKey.trim())) {
			throw new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY);
		}
		
		String token = getDeviceToken();
		if("".equals(token)) {
			throw new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED);
		}
		// validate time, hour[0, 23], minute[0, 59], duration[0, 1439]
		if ((startHour < 0 || startHour > 23) || (startMinute < 0 || startMinute > 59) || (duration < 0 || duration > 1439)) {
			throw new ArrownockException("Invalid parameter, valid time should be startHour[0, 23], startMinute[0, 59], duration[0, 1439]", ArrownockException.PUSH_INVALID_TIME_RANGE);
		}
		
		// change startHour in Local time to UTC time
		Calendar start = Calendar.getInstance(TimeZone.getDefault());
		start.set(Calendar.HOUR_OF_DAY, startHour);
		start.set(Calendar.MINUTE, startMinute);
		start.set(Calendar.SECOND, 0);
		Calendar startGMT = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		startGMT.setTime(start.getTime());
		int startGMTHour = startGMT.get(Calendar.HOUR_OF_DAY);
		int startGMTMinute = startGMT.get(Calendar.MINUTE);
		util.setSilentPeriodAsync(appKey.trim(), token, startGMTHour, startGMTMinute, duration, resend, callback);
	}
	
	public void setSilentPeriod(int startHour, int startMinute, int duration, boolean resend, IAnPushSettingsCallback callback) {
        if(androidContext == null) {
            callback.onError(new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT));
        }
        if(appKey == null || "".equals(appKey.trim())) {
            callback.onError(new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY));
        }
        
        String token = getDeviceToken();
        if("".equals(token)) {
            callback.onError(new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED));
        }
        // validate time, hour[0, 23], minute[0, 59], duration[0, 1439]
        if ((startHour < 0 || startHour > 23) || (startMinute < 0 || startMinute > 59) || (duration < 0 || duration > 1439)) {
            callback.onError(new ArrownockException("Invalid parameter, valid time should be startHour[0, 23], startMinute[0, 59], duration[0, 1439]", ArrownockException.PUSH_INVALID_TIME_RANGE));
        }
        
        // change startHour in Local time to UTC time
        Calendar start = Calendar.getInstance(TimeZone.getDefault());
        start.set(Calendar.HOUR_OF_DAY, startHour);
        start.set(Calendar.MINUTE, startMinute);
        start.set(Calendar.SECOND, 0);
        Calendar startGMT = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        startGMT.setTime(start.getTime());
        int startGMTHour = startGMT.get(Calendar.HOUR_OF_DAY);
        int startGMTMinute = startGMT.get(Calendar.MINUTE);
        util.setSilentPeriodAsync(appKey.trim(), token, startGMTHour, startGMTMinute, duration, resend, callback);
    }

	/**
	 * @deprecated use {@link #clearSilentPeriod(IAnPushSettingsCallback)} instead
	 */
	public void clearSilentPeriod() throws ArrownockException {
        if(androidContext == null) {
            throw new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT);
        }
        if(appKey == null || "".equals(appKey.trim())) {
            throw new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY);
        }
        
        String token = getDeviceToken();
        if("".equals(token)) {
            throw new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED);
        }
        
        util.clearSilentPeriodAsync(appKey.trim(), token, callback);
    }
	
	public void clearSilentPeriod(IAnPushSettingsCallback callback) {
        if(androidContext == null) {
            callback.onError(new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT));
        }
        if(appKey == null || "".equals(appKey.trim())) {
            callback.onError(new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY));
        }
        
        String token = getDeviceToken();
        if("".equals(token)) {
            callback.onError(new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED));
        }
        
        util.clearSilentPeriodAsync(appKey.trim(), token, callback);
    }
	
	/**
	 * @deprecated use {@link #setBadge(int, IAnPushSettingsCallback)} instead
	 */
	public void setBadge(int number) throws ArrownockException {
		if(androidContext == null) {
			throw new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT);
		}
		if(appKey == null || "".equals(appKey.trim())) {
			throw new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY);
		}
		if(number < 0) {
			throw new ArrownockException("Badge number should equal or larger than 0.", ArrownockException.PUSH_INVALID_BADGE);
		}
		String token = getDeviceToken();
		if("".equals(token)) {
			throw new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED);
		}
		
		util.setBadgeAsync(appKey.trim(), token, number, callback);
	}
	
	public void setBadge(int number, IAnPushSettingsCallback callback) {
        if(androidContext == null) {
            callback.onError(new ArrownockException("Invalid application context", ArrownockException.PUSH_INVALID_APP_CONTEXT));
        }
        if(appKey == null || "".equals(appKey.trim())) {
            callback.onError(new ArrownockException("Invalid app key.", ArrownockException.PUSH_INVALID_APP_KEY));
        }
        if(number < 0) {
            callback.onError(new ArrownockException("Badge number should equal or larger than 0.", ArrownockException.PUSH_INVALID_BADGE));
        }
        String token = getDeviceToken();
        if("".equals(token)) {
            callback.onError(new ArrownockException("Device is not registered yet", ArrownockException.PUSH_DEVICE_NOT_REGISTERED));
        }
        
        util.setBadgeAsync(appKey.trim(), token, number, callback);
    }
	
	public String getAnID() {
	    String anid = util.getFromLocalStorage(PushService.PREF_DEVICE_ID);
		if (null != anid && !"".equals(anid)) {
		    LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "getAnID cache=" + anid);
		    return anid;  
		} 
		String deviceId = this.deviceId;
		if (null != deviceId && !"".equals(deviceId)) {
		    LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "getAnID deviceId=" + deviceId);
		    return deviceId;
		}
		String uniqueId = util.getDeviceUniqueId(androidContext, appKey);
		if (null != uniqueId && !"".equals(uniqueId)) {
		    LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "getAnID uniqueId=" + uniqueId);
		    return uniqueId;
		}
		LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "getAnID ");
		return "";
	}

	void keepaliveService() {
		PushService.actionPing(androidContext);
	}

	private String getDeviceToken() {
		// Check whether using GCM or Arrownock
		String token = "";
		
		if (ARROWNOCK_TYPE.equals(serviceType)) {
			token = util.getFromLocalStorage(PushService.PREF_DEVICE_TOKEN);
		} else if(GCM_TYPE.equals(serviceType)) {
			token = util.getFromLocalStorage("GCM_REG_ID");
		}
		
		return token;
	}
	
	synchronized void registerMiPushDevice(Context context, String regId) {
		try {
			util.saveToLocalStorage(AnPushUtility.MIPUSH_REG_ID, regId);
			util.saveToLocalStorage(AnPushUtility.MIPUSH_APP, util.getFromLocalStorage(AnPush.MIPUSH_APPID) + util.getFromLocalStorage(AnPush.MIPUSH_APPKEY));
			AnPush.getInstance(context).notifyAll();
		} catch (ArrownockException e) {
			// Ignore
		}
	}
	
	private boolean isMIUI() {
		String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
		String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
		String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
			return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
						|| prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
						|| prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
		} catch (final IOException e) {
			return false;
		}
	}
	/*
	private void fetchPendingPushNotifications() {
		new Thread(new Runnable(){
			public void run() {
				SharedPreferences sp = androidContext.getSharedPreferences(MiPushReceiver.PENDING_PUSH_STORAGE, Context.MODE_PRIVATE);
				String storedPayloads = sp.getString(MiPushReceiver.PENDING_PUSH_NOTIFICATIONS, null);
				JSONArray payloads = null;
				if(storedPayloads != null) {
					try {
						payloads = new JSONArray(storedPayloads);
					} catch(Exception e) {
						// parse old payloads failed
					}
				}
				
				if (payloads != null && payloads.length() > 0) {
					Log.d(LOG_TAG, "Pending message arrived. Creating wakelock.");
					PowerManager pm = (PowerManager) androidContext.getSystemService(Context.POWER_SERVICE);
					WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "anPush");
					wl.acquire();
					
					for(int i = 0; i < payloads.length(); i++) {
						try {
						String payload = payloads.getString(i);
							if(payload != null) {
								Intent newIntent = new Intent();
								newIntent.setAction(PushService.ACTION_MSG_ARRIVAL);
								newIntent.setPackage(androidContext.getPackageName());
								newIntent.putExtra("payload", payload);
								androidContext.sendBroadcast(newIntent);
							}
						} catch(Exception e) {
							//mute
						}
					}
					
					// clear pending notifications
					sp.edit().remove(MiPushReceiver.PENDING_PUSH_NOTIFICATIONS).commit();
					wl.release();
					Log.d(LOG_TAG, "Releasing wakelock.");
				}
			}
		}).start();
	}
	*/
}