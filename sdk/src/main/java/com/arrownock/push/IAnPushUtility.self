package com.arrownock.push;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.arrownock.exception.ArrownockException;
import com.arrownock.internal.device.DeviceManager;
import com.arrownock.internal.push.LogUtil;
import com.arrownock.internal.util.Constants;
import com.arrownock.internal.util.DefaultHostnameVerifier;
import com.arrownock.internal.util.KeyValuePair;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.xiaomi.mipush.sdk.MiPushClient;

public interface IAnPushUtility {
    String getDeviceUniqueId(Context context, String appKey);
    @Deprecated
    void registerDeviceAsync(final String appKey, final String channels, final IAnPushCallback callback, final boolean overwrite, final String type, final String id);
    void registerDeviceAsync(final String appKey, final String channels, final IAnPushRegistrationCallback callback, final boolean overwrite, final String type, final String id);
    @Deprecated
    void unregisterDeviceAsync(final String deviceToken, final String appKey, final String channels, final IAnPushCallback callback, final boolean removeDevice, final String type);
    void unregisterDeviceAsync(final String deviceToken, final String appKey, final String channels, final IAnPushRegistrationCallback callback, final boolean removeDevice, final String type);
    void saveToLocalStorage(final String key, final String value);
	String getFromLocalStorage(final String key);
	void removeFromLocalStorage(final String key);
	String registerDevice(String appKey, String deviceToken, String channels, boolean overwrite, String type, String id) throws ArrownockException;
	void unregisterDevice(String appKey, String deviceToken, String channels, boolean removeDevice, String type, String id) throws ArrownockException;
	void updateDeviceToken(String appKey, String oldToken, String newToken) throws ArrownockException;
	@Deprecated
	void setMuteAsync(final String appKey, final String deviceToken, final IAnPushCallback callback);
	void setMuteAsync(final String appKey, final String deviceToken, final IAnPushSettingsCallback callback);
	@Deprecated
	void setScheduledMuteAsync(final String appKey, final String deviceToken, int startGMTHour, int startGMTMinute, int duration, final IAnPushCallback callback);
	void setScheduledMuteAsync(final String appKey, final String deviceToken, int startGMTHour, int startGMTMinute, int duration, final IAnPushSettingsCallback callback);
	@Deprecated
	void clearMuteAsync(final String appKey, final String deviceToken, final IAnPushCallback callback);
	void clearMuteAsync(final String appKey, final String deviceToken, final IAnPushSettingsCallback callback);
	@Deprecated
	void setSilentPeriodAsync(final String appKey, final String deviceToken, int startGMTHour, int startGMTMinute, int duration, boolean resend, final IAnPushCallback callback);
	void setSilentPeriodAsync(final String appKey, final String deviceToken, int startGMTHour, int startGMTMinute, int duration, boolean resend, final IAnPushSettingsCallback callback);
    @Deprecated
    void clearSilentPeriodAsync(final String appKey, final String deviceToken, final IAnPushCallback callback);
    void clearSilentPeriodAsync(final String appKey, final String deviceToken, final IAnPushSettingsCallback callback);
    @Deprecated
    void setBadgeAsync(final String appKey, final String deviceToken, final int number, final IAnPushCallback callback);
    void setBadgeAsync(final String appKey, final String deviceToken, final int number, final IAnPushSettingsCallback callback);
    KeyValuePair<String, Integer> getServiceHost(String deviceID) throws ArrownockException;
	void scheduleStartStopConnection() throws ArrownockException;
	String getRegistrationId(Context context) throws ArrownockException;
	void registerInEnable(Context context, String appKey, String type, int count, String deviceId, IAnPushRegisterAnIdCallback callback);
}

class AnPushUtility implements IAnPushUtility {
	private final static String API_BASE_URL = Constants.ARROWNOCK_API_URL;
	private final static String API_VERSION = Constants.ARROWNOCK_API_VERSION;
	private final static String SIG_SEC = Constants.ARROWNOCK_API_SECRET;
	private final static String REGISTER_ENDPOINT = "push_notification/signed_register.json";
	private final static String UNREGISTER_ENDPOINT = "push_notification/signed_unregister.json";
	private final static String UPDATE_TOKEN_ENDPOINT = "push_notification/signed_update_token.json";
	private final static String MUTE_ENDPOINT = "push_notification/signed_mute.json";
	private final static String SILENT_ENDPOINT = "push_notification/signed_silent_period.json";
	public final static String DEVICE_TOKEN_URL = "push_notification/device_token.json";
	public final static String SET_BADGE_ENDPOINT = "push_notification/signed_set_badge.json";
	public final static String HOST_URL = "push_notification/host.json";
	private final static String REGISTER_API_URL = "/" + API_VERSION + "/" + REGISTER_ENDPOINT;
	private final static String UNREGISTER_API_URL = "/" + API_VERSION + "/" + UNREGISTER_ENDPOINT;
	private final static String UPDATE_TOKEN_API_URL = "/" + API_VERSION + "/" + UPDATE_TOKEN_ENDPOINT;
	private final static String MUTE_API_URL = "/" + API_VERSION + "/" + MUTE_ENDPOINT;
	private final static String SILENT_API_URL = "/" + API_VERSION + "/" + SILENT_ENDPOINT;
	private final static String SET_BADGE_API_URL = "/" + API_VERSION + "/" + SET_BADGE_ENDPOINT;
	private final static String LOG_TAG = AnPush.class.getName();
	private final static String LOG_NAME = "ArrownockSDK";
	
	private final static HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();
	
	private final static String RELOAD_DEVICE_TOKEN = "RELOAD_DEVICE_TOKEN";
	private final static String GCM_REG_ID = "GCM_REG_ID";
	private final static String GCM_REG_VERSION_CODE = "GCM_REG_VERSION_CODE";
	private final static String GCM_SENDER_ID = "GCM_SENDER_ID";
	
	final static String MIPUSH_REG_ID = "MIPUSH_REG_ID";
	final static String MIPUSH_APP = "MIPUSH_APP";
	
	// Device id type
	private static enum AnPushDeviceIdType {
		placeholder,
		imei,		// IMEI
		android_id,	// ANDROID_ID
		none,		// EMULATOR
		a			// ANDROID CUSTOM ID
	}
	
	private Context androidContext = null;
	
	public AnPushUtility(Context androidContext) {
		this.androidContext = androidContext;
	}
	
	/**
	 * Retrieve device token from server asynchronously
	 * @deprecated
	 * @param androidContext Android Context
	 * @param appKey Arrownock AppKey
	 * @param callback Callback when device token successfully retrieved
	 */
	public void registerDeviceAsync(final String appKey, final String channels, final IAnPushCallback callback, final boolean overwrite, final String type, final String id) {
		Runnable getDeviceTokenThread = new Runnable(){
			public void run() {
				try{
					if(type.equals("android-gcm")) {
						if (checkPlayServices()) {
							String regId = getRegistrationId(androidContext);
							if (regId.isEmpty()) {
								// do gcm registration
								GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(androidContext);
								try {
									// invalidate the previous regid first
									gcm.unregister();
									regId = gcm.register(AnPush.getInstance(androidContext).getSenderId());
									Log.d("GCM", "Got registration id: " + regId);
									storeRegistrationId(androidContext, regId);
					            } catch (IOException ex) {
					            	if(callback != null) {
					            		callback.register(true, null, new ArrownockException(ex.getMessage(), ArrownockException.PUSH_FAILED_REGISTER));
					            		return;
					            	}
					            }
							} else {
								String senderId = getFromLocalStorage(GCM_SENDER_ID);
							    if(!senderId.equals(AnPush.getInstance(androidContext).getSenderId())) {
							    	// sender id mismatched, need new registerId
							    	try {
							    		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(androidContext);
								    	gcm.unregister();
								    	
								    	regId = gcm.register(AnPush.getInstance(androidContext).getSenderId());
										Log.d("GCM", "Sender ID mismatched. Got new registration id: " + regId);
										storeRegistrationId(androidContext, regId);
							    	}catch (IOException ex) {
						            	if(callback != null) {
						            		callback.register(true, null, new ArrownockException(ex.getMessage(), ArrownockException.PUSH_FAILED_REGISTER));
						            		return;
						            	}
						            }
							    }
							}
							String anid = getFromLocalStorage(PushService.PREF_DEVICE_ID, null);
							String localDeviceId = null;
							if(id == null) {
								if(anid == null) {
                                    // this must be the first time to register
                                    String tempId = getDeviceUniqueId(androidContext, appKey);
                                    if (tempId != null) {
                                        localDeviceId = tempId;
                                    }
                                } else {
									localDeviceId = anid;
								}
							} else {
								localDeviceId = id;
							}
							anid = registerDevice(appKey, regId, channels, overwrite, type, localDeviceId);
				            saveToLocalStorage(PushService.PREF_DEVICE_ID, anid);
							if(callback != null) {
								callback.register(false, anid, null);
							}
						} else {
							callback.register(true, null, new ArrownockException("No valid Google Play Services APK found.", ArrownockException.PUSH_FAILED_NO_GOOGLE_PLAY_SERVICES));
						}
					} else if(type.equals("android-arrownock")) {
						//final String type = "android-arrownock";
						String deviceToken = getDeviceToken(appKey, id);
						if (deviceToken != null && !"".equals(deviceToken.trim())) {
							String anid = getFromLocalStorage(PushService.PREF_DEVICE_ID, null);
							String localDeviceId = null;
							if(id == null) {
								if(anid == null) {
                                    // this must be the first time to register
                                    String tempId = getDeviceUniqueId(androidContext, appKey);
                                    if (tempId != null) {
                                        localDeviceId = tempId;
                                    }
                                } else {
									localDeviceId = anid;
								}
							} else {
								localDeviceId = id;
							}
							anid = registerDevice(appKey, deviceToken, channels, overwrite, type, localDeviceId);
							if(RELOAD_DEVICE_TOKEN.equals(anid)) {
								LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "Device token is deprecated, apply for a new one.");
								removeFromLocalStorage(PushService.PREF_DEVICE_TOKEN);
								registerDeviceAsync(appKey, channels, callback, overwrite, type, localDeviceId);
								return;
							}
							saveToLocalStorage(PushService.PREF_DEVICE_ID, anid);
							if(callback != null) {
								callback.register(false, anid, null);
							}
						} else {
							LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, "Device token is null.");
						}
					} else if(type.equals("android-mipush")) {
						String regId = getMiPushRegId(androidContext);
						synchronized(AnPush.getInstance(androidContext)) {
							if("".equals(regId)) {
								MiPushClient.registerPush(androidContext, getFromLocalStorage(AnPush.MIPUSH_APPID), getFromLocalStorage(AnPush.MIPUSH_APPKEY));
								while (regId.equals("")) {
						            try {
										AnPush.getInstance(androidContext).wait();
									} catch (InterruptedException e) {
										// Ignore
									}
						            regId = getMiPushRegId(androidContext);
					            }
							}
							String anid = getFromLocalStorage(PushService.PREF_DEVICE_ID, null);
							String localDeviceId = null;
							if(id == null) {
								if(anid == null) {
                                    // this must be the first time to register
                                    String tempId = getDeviceUniqueId(androidContext, appKey);
                                    if (tempId != null) {
                                        localDeviceId = tempId;
                                    }
                                } else {
									localDeviceId = anid;
								}
							} else {
								localDeviceId = id;
							}
							anid = registerDevice(appKey, regId, channels, overwrite, type, localDeviceId);
					        saveToLocalStorage(PushService.PREF_DEVICE_ID, anid);
							if(callback != null) {
								callback.register(false, anid, null);
							}
						}
					}
				} catch(ArrownockException ex){
					if(callback != null) {
						callback.register(true, null, ex);
					}
				}
			}
		};
		Thread thread = new Thread(getDeviceTokenThread);
		thread.start();
	}
	
	public void registerInEnable(Context context, String appKey, String type, int count, String deviceId, IAnPushRegisterAnIdCallback callback) {
	    LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "try_time=" + count + " type=" + type);
	    
	    String hasAlreadyRegistered = getFromLocalStorage(PushService.PREF_ALREADY_REGISTER, "no");
	    LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "hasAlreadyRegistered=" + hasAlreadyRegistered);
        if ("no".equals(hasAlreadyRegistered)) {
            String anid = getFromLocalStorage(PushService.PREF_DEVICE_ID, "");
            if (!"".equals(anid)) {
                LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "anid=" + anid);
                saveToLocalStorage(PushService.PREF_ALREADY_REGISTER, "yes");
                callback.onSuccess();
                return;
            }
            if (null != deviceId && !"".equals(deviceId)) {
                anid = deviceId;
            } else {
                anid = getDeviceUniqueId(context, appKey);
            }
            LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "anid=" + anid);
            registerAnId(context, anid, appKey, type, count, callback);
        } else {
            callback.onSuccess();
        }
	}
	

    private void registerAnId(final Context context, final String anid, final String appKey, final String type, final int count, final IAnPushRegisterAnIdCallback callback) {
        Runnable registerAnId = new Runnable(){
            public void run() {
                try{
                    if(type.equals("android-gcm")) {
                        if (checkPlayServices()) {
                            String regId = getRegistrationId(androidContext);
                            if (regId.isEmpty()) {
                                // do gcm registration
                                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(androidContext);
                                try {
                                    // invalidate the previous regid first
                                    gcm.unregister();
                                    regId = gcm.register(AnPush.getInstance(androidContext).getSenderId());
                                    Log.d("GCM", "Got registration id: " + regId);
                                    storeRegistrationId(androidContext, regId);
                                } catch (IOException ex) {
                                    LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, "registerInEnable anid failed. reason : some bugs appear when call gcm (regId is empty) ");
                                    callback.onError(null, count, type);
                                }
                            } else {
                                String senderId = getFromLocalStorage(GCM_SENDER_ID);
                                if(!senderId.equals(AnPush.getInstance(androidContext).getSenderId())) {
                                    // sender id mismatched, need new registerId
                                    try {
                                        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(androidContext);
                                        gcm.unregister();
                                        
                                        regId = gcm.register(AnPush.getInstance(androidContext).getSenderId());
                                        Log.d("GCM", "Sender ID mismatched. Got new registration id: " + regId);
                                        storeRegistrationId(androidContext, regId);
                                    }catch (IOException ex) {
                                        LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, "registerInEnable anid failed. reason : some bugs appear when call gcm (regId is not empty)");
                                        callback.onError(null, count, type);
                                    }
                                }
                            }
                            String localDeviceId = anid;
                            String result = registerDevice(appKey, regId, "", false, type, localDeviceId);
                            if (RELOAD_DEVICE_TOKEN.equals(result)) {
                                LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, "RELOAD_DEVICE_TOKEN true");
                                callback.onError(null, count, type);
                            } else {
                                LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "registeranId successful");
                                saveToLocalStorage(PushService.PREF_DEVICE_ID, result);
                                saveToLocalStorage(PushService.PREF_ALREADY_REGISTER, "yes");
                                callback.onSuccess();
                            }
                        } else {
                            LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, "registerInEnable anid failed. reason : no playServices");
                            callback.onError(null, count, type);
                        }
                    } else if(type.equals("android-arrownock")) {
                        String deviceToken = getDeviceToken(appKey, anid);
                        if (deviceToken != null && !"".equals(deviceToken.trim())) {
                            String localDeviceId = anid;
                            String result = registerDevice(appKey, deviceToken, "", false, type, localDeviceId);
                            if (RELOAD_DEVICE_TOKEN.equals(result)) {
                                LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, "RELOAD_DEVICE_TOKEN true");
                                callback.onError(null, count, type);
                            } else {
                                LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "registeranId successful");
                                saveToLocalStorage(PushService.PREF_DEVICE_ID, result);
                                saveToLocalStorage(PushService.PREF_ALREADY_REGISTER, "yes");
                                callback.onSuccess();
                            }
                        } else {
                            LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, "registerInEnable anid failed. reason : no deviceToken");
                            callback.onError(null, count, type);
                        }
                    } else if(type.equals("android-mipush")) {
                        String regId = getMiPushRegId(androidContext);
                        synchronized(AnPush.getInstance(androidContext)) {
                            if("".equals(regId)) {
                                MiPushClient.registerPush(androidContext, getFromLocalStorage(AnPush.MIPUSH_APPID), getFromLocalStorage(AnPush.MIPUSH_APPKEY));
                                while (regId.equals("")) {
                                    try {
                                        AnPush.getInstance(androidContext).wait();
                                    } catch (InterruptedException e) {
                                        // Ignore
                                    }
                                    regId = getMiPushRegId(androidContext);
                                }
                            }
                            String localDeviceId = anid;
                            String result = registerDevice(appKey, regId, "", false, type, localDeviceId);
                            if (RELOAD_DEVICE_TOKEN.equals(result)) {
                                LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, "RELOAD_DEVICE_TOKEN true");
                                callback.onError(null, count, type);
                            } else {
                                LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "registeranId successful");
                                saveToLocalStorage(PushService.PREF_DEVICE_ID, result);
                                saveToLocalStorage(PushService.PREF_ALREADY_REGISTER, "yes");
                                callback.onSuccess();
                            }
                        }
                    }
                } catch(ArrownockException ex){
                    LogUtil.getInstance().error(LOG_TAG, LOG_NAME, "registerInEnable anid failed. reason : " + ex.getMessage() + "errorCode=" + ex.getErrorCode());
                    callback.onError(ex, count, type);
                }
            }
        };
        Thread thread = new Thread(registerAnId);
        thread.start();
    }

    /**
     * Retrieve device token from server asynchronously
     * @param androidContext Android Context
     * @param appKey Arrownock AppKey
     * @param callback Callback when device token successfully retrieved
     */
    public void registerDeviceAsync(final String appKey, final String channels, final IAnPushRegistrationCallback callback, final boolean overwrite, final String type, final String id) {
        Runnable getDeviceTokenThread = new Runnable(){
            public void run() {
                try{
                    if(type.equals("android-gcm")) {
                        if (checkPlayServices()) {
                            String regId = getRegistrationId(androidContext);
                            if (regId.isEmpty()) {
                                // do gcm registration
                                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(androidContext);
                                try {
                                    // invalidate the previous regid first
                                    gcm.unregister();
                                    regId = gcm.register(AnPush.getInstance(androidContext).getSenderId());
                                    Log.d("GCM", "Got registration id: " + regId);
                                    storeRegistrationId(androidContext, regId);
                                } catch (IOException ex) {
                                    if(callback != null) {
                                        callback.onError(new ArrownockException(ex.getMessage(), ArrownockException.PUSH_FAILED_REGISTER));
                                        return;
                                    }
                                }
                            } else {
                                String senderId = getFromLocalStorage(GCM_SENDER_ID);
                                if(!senderId.equals(AnPush.getInstance(androidContext).getSenderId())) {
                                    // sender id mismatched, need new registerId
                                    try {
                                        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(androidContext);
                                        gcm.unregister();
                                        
                                        regId = gcm.register(AnPush.getInstance(androidContext).getSenderId());
                                        Log.d("GCM", "Sender ID mismatched. Got new registration id: " + regId);
                                        storeRegistrationId(androidContext, regId);
                                    }catch (IOException ex) {
                                        if(callback != null) {
                                            callback.onError(new ArrownockException(ex.getMessage(), ArrownockException.PUSH_FAILED_REGISTER));
                                            return;
                                        }
                                    }
                                }
                            }
                            String anid = getFromLocalStorage(PushService.PREF_DEVICE_ID, null);
                            String localDeviceId = null;
                            if(id == null) {
                                if(anid == null) {
                                    // this must be the first time to register
                                    String tempId = getDeviceUniqueId(androidContext, appKey);
                                    if (tempId != null) {
                                        localDeviceId = tempId;
                                    }
                                } else {
                                    localDeviceId = anid;
                                }
                            } else {
                                localDeviceId = id;
                            }
                            anid = registerDevice(appKey, regId, channels, overwrite, type, localDeviceId);
                            saveToLocalStorage(PushService.PREF_DEVICE_ID, anid);
                            if(callback != null) {
                                callback.onSuccess(anid);
                            }
                        } else {
                            if(callback != null) {
                                callback.onError(new ArrownockException("No valid Google Play Services APK found.", ArrownockException.PUSH_FAILED_REGISTER));
                            }
                        }
                    } else if(type.equals("android-arrownock")) {
                        //final String type = "android-arrownock";
                        String deviceToken = getDeviceToken(appKey, id);
                        if (deviceToken != null && !"".equals(deviceToken.trim())) {
                            String anid = getFromLocalStorage(PushService.PREF_DEVICE_ID, null);
                            String localDeviceId = null;
                            if(id == null) {
                                if(anid == null) {
                                    // this must be the first time to register
                                    String tempId = getDeviceUniqueId(androidContext, appKey);
                                    if (tempId != null) {
                                        localDeviceId = tempId;
                                    }
                                } else {
                                    localDeviceId = anid;
                                }
                            } else {
                                localDeviceId = id;
                            }
                            anid = registerDevice(appKey, deviceToken, channels, overwrite, type, localDeviceId);
                            if(RELOAD_DEVICE_TOKEN.equals(anid)) {
                                LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "Device token is deprecated, apply for a new one.");
                                removeFromLocalStorage(PushService.PREF_DEVICE_TOKEN);
                                registerDeviceAsync(appKey, channels, callback, overwrite, type, localDeviceId);
                                return;
                            }
                            saveToLocalStorage(PushService.PREF_DEVICE_ID, anid);
                            if(callback != null) {
                                callback.onSuccess(anid);
                            }
                        } else {
                            LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, "Device token is null.");
                        }
                    } else if(type.equals("android-mipush")) {
                        String regId = getMiPushRegId(androidContext);
                        synchronized(AnPush.getInstance(androidContext)) {
                            if("".equals(regId)) {
                                MiPushClient.registerPush(androidContext, getFromLocalStorage(AnPush.MIPUSH_APPID), getFromLocalStorage(AnPush.MIPUSH_APPKEY));
                                while (regId.equals("")) {
                                    try {
                                        AnPush.getInstance(androidContext).wait();
                                    } catch (InterruptedException e) {
                                        // Ignore
                                    }
                                    regId = getMiPushRegId(androidContext);
                                }
                            }
                            String anid = getFromLocalStorage(PushService.PREF_DEVICE_ID, null);
                            String localDeviceId = null;
                            if(id == null) {
                                if(anid == null) {
                                    // this must be the first time to register
                                    String tempId = getDeviceUniqueId(androidContext, appKey);
                                    if (tempId != null) {
                                        localDeviceId = tempId;
                                    }
                                } else {
                                    localDeviceId = anid;
                                }
                            } else {
                                localDeviceId = id;
                            }
                            anid = registerDevice(appKey, regId, channels, overwrite, type, localDeviceId);
                            saveToLocalStorage(PushService.PREF_DEVICE_ID, anid);
                            if(callback != null) {
                                callback.onSuccess(anid);
                            }
                        }
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(getDeviceTokenThread);
        thread.start();
    }
	
    @Deprecated
    public void unregisterDeviceAsync(final String deviceToken, final String appKey, final String channels, final IAnPushCallback callback, final boolean removeDevice, final String type) {
        Runnable runnable = new Runnable(){
            public void run() {
                try{
                    if (deviceToken != null && !"".equals(deviceToken.trim())) {
                        if(type.equals("android-gcm")) {
                            String anid = getFromLocalStorage(PushService.PREF_DEVICE_ID);
                            if(anid != null) {
                                unregisterDevice(appKey, null, channels, removeDevice, type, anid);
                            } else {
                                unregisterDevice(appKey, deviceToken, channels, removeDevice, type, null);
                            }
                        } else {
                            unregisterDevice(appKey, deviceToken, channels, removeDevice, type, null);
                            removeFromLocalStorage(PushService.PREF_DEVICE_ID);
                        }
                        if(callback != null) {
                            callback.unregister(false, null);
                        }
                    } else {
                        LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, "Device Token is null.");
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.unregister(true, ex);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
    
    public void unregisterDeviceAsync(final String deviceToken, final String appKey, final String channels, final IAnPushRegistrationCallback callback, final boolean removeDevice, final String type) {
        Runnable runnable = new Runnable(){
            public void run() {
                try{
                    if (deviceToken != null && !"".equals(deviceToken.trim())) {
                        String anid = getFromLocalStorage(PushService.PREF_DEVICE_ID);
                        if(type.equals("android-gcm")) {
                            if(anid != null) {
                                unregisterDevice(appKey, null, channels, removeDevice, type, anid);
                            } else {
                                unregisterDevice(appKey, deviceToken, channels, removeDevice, type, null);
                            }
                        } else {
                            unregisterDevice(appKey, deviceToken, channels, removeDevice, type, null);
                            removeFromLocalStorage(PushService.PREF_DEVICE_ID);
                        }
                        if(callback != null) {
                            callback.onSuccess(anid);
                        }
                    } else {
                        LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, "Device Token is null.");
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
	
	public void saveToLocalStorage(final String key, final String value){
		Editor editor = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE).edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public String getFromLocalStorage(final String key) {
		return getFromLocalStorage(key, "");
	}
	
	public String getFromLocalStorage(final String key, final String defaultValue) {
		SharedPreferences pref = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE);
		return pref.getString(key, defaultValue);
	}
	
	public void removeFromLocalStorage(final String key) {
		SharedPreferences pref = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE);
		pref.edit().remove(key).commit();
	}

	private String getDeviceToken(final String appKey, final String id) throws ArrownockException {
		String token = getFromLocalStorage(PushService.PREF_DEVICE_TOKEN);
		if (token.equals("")) {
			String mDeviceID = null;
			String mDeviceIDType = null;
			if(id == null) {
			    try {
			        mDeviceID = ((TelephonyManager) androidContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                } catch (Exception ex) {
                	// mute exception on android 6
                    // throw new ArrownockException(ex, ArrownockException.PUSH_FAILED_REGISTER);
                }
				if (mDeviceID == null || mDeviceID.equals("000000000000000") || "".equals(mDeviceID.trim())) {
					mDeviceID = Settings.Secure.getString(androidContext.getContentResolver(), Settings.Secure.ANDROID_ID);
					if (mDeviceID == null || "".equals(mDeviceID.trim())) {
						// No IMEI and ANDROID_ID
						mDeviceID = "000000000000000";
						mDeviceIDType = String.valueOf(AnPushDeviceIdType.none.ordinal());
					} else {
						mDeviceIDType = String.valueOf(AnPushDeviceIdType.android_id.ordinal());
					}
				} else {
					mDeviceIDType = String.valueOf(AnPushDeviceIdType.imei.ordinal());
				}
			} else {
				mDeviceID = id;
				mDeviceIDType = AnPushDeviceIdType.a.name();
			}
			
			StringBuilder urlBuilder = new StringBuilder(); 
			urlBuilder.append(this.getDSHost() + "/" + Constants.PUSH_DS_VERSION + "/" + DEVICE_TOKEN_URL + "?key=" + appKey + "&package=" + androidContext.getPackageName() + "&client_id=" + mDeviceID + "&type=" + mDeviceIDType);
			Map<String, String> phoneStatusMap = getPhoneStatus(androidContext);
			for(String key : phoneStatusMap.keySet()){
				urlBuilder.append("&" + key + "=" + URLEncoder.encode(phoneStatusMap.get(key)));
			}
			urlBuilder.append("&version=" + Constants.SDK_VERSION);
			String url = urlBuilder.toString();
			
			int count = 0;
			while (token.equals("") && ++count <= PushConstants.MAX_REQUEST_TIME) {
				String urlplus = url + "&requesttime=" + count;
				Map<String, Object> requestResult = requestForDeviceToken(urlplus);
				boolean valid = (requestResult.get("valid").equals(true) ? true : false);
				if (valid) {
					// Valid Token
					token = requestResult.get("token").toString();
				} else {
					int status = (Integer) requestResult.get("status");
					if (status == 503) {
						// Service unavailable on server side. Should retry again.
						String message = requestResult.get("message").toString();
						int retry_after = -1;
						try{
							retry_after = (Integer) requestResult.get("retry_after");
						}catch(Exception ex){}
						if(retry_after<=0){
							retry_after = PushConstants.REQUEST_RETRY_TIME;
						}
						int sleep_secs = (retry_after > PushConstants.MAX_REQUEST_RETRY_TIME ? PushConstants.MAX_REQUEST_RETRY_TIME
								: retry_after);
						String warning_message = "Getting device token failed. StatusCode:" + status + ", Message:" + message
								+ ". Will try again in " + sleep_secs + " seconds.";
						LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, warning_message);
						try {
							Thread.sleep(sleep_secs * 1000);
							if(!AnPush.getInstance(androidContext).isServiceEnabled()){
								return null;
							}
						} catch (InterruptedException ex) {
							throw new ArrownockException(ex, ArrownockException.PUSH_FAILED_REGISTER);
						}
					} else if (status == 400) {
						// Bad request. Will stop here.
						String message = requestResult.get("message").toString();
						LogUtil.getInstance().error(LOG_TAG, LOG_NAME, message);
						throw new ArrownockException(message, ArrownockException.PUSH_FAILED_REGISTER);
					} else if (status == -1) {
						// Exception occurs, will stop here.
						Exception exception = (Exception) requestResult.get("exception");
						LogUtil.getInstance().error(LOG_TAG, LOG_NAME, ". Exception:" + exception.getMessage(), exception);
						throw new ArrownockException(exception, ArrownockException.PUSH_FAILED_REGISTER);
					} else {
						// Other response status, will try again.
						String data = requestResult.get("data").toString();
						String warning_message = "Getting device token failed. StatusCode:" + status + ", Raw Data:" + data
								+ ". Will try again in " + PushConstants.REQUEST_RETRY_TIME + " seconds.";
						LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, warning_message);
						try {
							Thread.sleep(PushConstants.REQUEST_RETRY_TIME * 1000);
							if(!AnPush.getInstance(androidContext).isServiceEnabled()){
								return null;
							}
						} catch (InterruptedException ex) {
							throw new ArrownockException(ex, ArrownockException.PUSH_FAILED_REGISTER);
						}
					}
				}
			}
			if (!token.equals("")) {
				saveToLocalStorage(PushService.PREF_DEVICE_TOKEN, token);
			}else{
				// Reaches maximum retry time. End here.
				String error_message = "Retry time limit reached";
				LogUtil.getInstance().error(LOG_TAG, LOG_NAME, error_message);
				throw new ArrownockException(error_message, ArrownockException.PUSH_FAILED_REGISTER);
			}
		}
		return token;
	}
	
	private Map<String, Object> requestForDeviceToken(String urlplus) {
		Map<String, Object> result = new HashMap<String, Object>();
		HttpURLConnection urlConnection = null;
		try {
			if (AnPush.getInstance(androidContext).isSecureConnection()) {
				URL url = new URL(Constants.HTTPS + urlplus);
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + urlplus);
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.connect();
			int statusCode = urlConnection.getResponseCode();
			if(statusCode==200){	// Service OK
				InputStream is = new BufferedInputStream(urlConnection.getInputStream());
				String res = convertStreamToString(is);
				JSONObject json = new JSONObject(res);
				String token = json.getString("device_token");
				result.put("valid", true);
				result.put("status", 200);
				result.put("token", token);
			}else {
				InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
				String res = convertStreamToString(es);
				if(statusCode==400){	// Bad Request
					JSONObject json = new JSONObject(res);
					String message = json.getString("message");
					result.put("valid", false);
					result.put("status", 400);
					result.put("message", message);
				}else if(statusCode==503){	// Service Unavilable
					JSONObject json = new JSONObject(res);
					String message = json.getString("message");
					int retry_after = json.optInt("retry_after", -1);
					result.put("valid", false);
					result.put("status", 503);
					result.put("message", message);
					result.put("retry_after", retry_after);
				}else{
					result.put("valid", false);
					result.put("status", statusCode);
					result.put("data", res);
				}
			}
		} catch (Exception ex) {
			result.put("valid", false);
			result.put("status", -1);
			result.put("exception", ex);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}  
		}
		return result;
	}
	
	public String registerDevice(String appKey, String deviceToken, String channels, boolean overwrite, String type, String id) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			if (AnPush.getInstance(androidContext).isSecureConnection()) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost() + REGISTER_API_URL + "?key=" + appKey.trim());
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost() + REGISTER_API_URL + "?key=" + appKey.trim());
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			
		    try {
		    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
		    	String date = format.format(new Date());
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			    nameValuePairs.add(new BasicNameValuePair("device_token", deviceToken));
			    nameValuePairs.add(new BasicNameValuePair("date", date));
			    nameValuePairs.add(new BasicNameValuePair("type", type));
			    nameValuePairs.add(new BasicNameValuePair("channel", channels));
			    nameValuePairs.add(new BasicNameValuePair("id", id == null? "": id.trim()));
			    
			    String signatureString = null;
			    
			    //add device_id when register
			    if (Constants.DM_ENABLED) {
        			    String real_device_id = DeviceManager.getInstance(androidContext, appKey).getDeviceId();
        			    if ("".equals(real_device_id)){
        			        if(overwrite) {
                            nameValuePairs.add(new BasicNameValuePair("overwrite", "true"));
                            signatureString = "/" + API_VERSION + "/" + REGISTER_ENDPOINT + "channel=" + channels + "&date=" + date + "&device_token=" + deviceToken + "&id=" + (id == null? "": id.trim()) + "&key=" + appKey + "&overwrite=true" + "&type=" + type;
                        } else {
                            signatureString = "/" + API_VERSION + "/" + REGISTER_ENDPOINT + "channel=" + channels + "&date=" + date + "&device_token=" + deviceToken + "&id=" + (id == null? "": id.trim()) + "&key=" + appKey + "&type=" + type;
                        }
        			    } else {
        			        nameValuePairs.add(new BasicNameValuePair("real_device_id", real_device_id));
                            if(overwrite) {
                            nameValuePairs.add(new BasicNameValuePair("overwrite", "true"));
                            signatureString = "/" + API_VERSION + "/" + REGISTER_ENDPOINT + "channel=" + channels + "&date=" + date + "&device_token=" + deviceToken + "&id=" + (id == null? "": id.trim()) + "&key=" + appKey + "&overwrite=true" + "&real_device_id=" + real_device_id + "&type=" + type;
                        } else {
                            signatureString = "/" + API_VERSION + "/" + REGISTER_ENDPOINT + "channel=" + channels + "&date=" + date + "&device_token=" + deviceToken + "&id=" + (id == null? "": id.trim()) + "&key=" + appKey + "&real_device_id=" + real_device_id + "&type=" + type;
                        }
        			    }
			    } else {
			        if(overwrite) {
                        nameValuePairs.add(new BasicNameValuePair("overwrite", "true"));
                        signatureString = "/" + API_VERSION + "/" + REGISTER_ENDPOINT + "channel=" + channels + "&date=" + date + "&device_token=" + deviceToken + "&id=" + (id == null? "": id.trim()) + "&key=" + appKey + "&overwrite=true" + "&type=" + type;
                    } else {
                        signatureString = "/" + API_VERSION + "/" + REGISTER_ENDPOINT + "channel=" + channels + "&date=" + date + "&device_token=" + deviceToken + "&id=" + (id == null? "": id.trim()) + "&key=" + appKey + "&type=" + type;
                    }
			    }
			    
			    String HMAC_SHA1_ALGORITHM = "HmacSHA1";
			    String signature = null;
			    
			    try {         
			    	Key signingKey = new SecretKeySpec(getAPISecret().getBytes(), HMAC_SHA1_ALGORITHM);
			    	Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			    	mac.init(signingKey);
			    	byte[] rawHmac = mac.doFinal(signatureString.getBytes());
			    	signature = Base64.encodeToString(rawHmac, Base64.NO_WRAP);
			    	nameValuePairs.add(new BasicNameValuePair("signature", signature));
			    } catch (Exception e) {
			    	throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_REGISTER);
			    }
		      
			    OutputStream out = urlConnection.getOutputStream();
			    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			    writer.write(getQuery(nameValuePairs));
			    writer.close();
			    out.close();
			    
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode == 200) {
					    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					    String s = convertStreamToString(in);
						JSONObject json = new JSONObject(s);
						//JSONObject meta = json.getJSONObject("meta");
						JSONObject data = json.getJSONObject("response");
						if(data != null && data.getJSONObject("subscription") != null) {
							JSONObject sub = data.getJSONObject("subscription");
							if(sub != null && sub.getString("anid") != null) {
								return sub.getString("anid");
							} else {
								throw new ArrownockException("Failed to acquire registration.", ArrownockException.PUSH_FAILED_REGISTER);
							}
						} else {
							throw new ArrownockException("Failed to acquire ANID.", ArrownockException.PUSH_FAILED_REGISTER);
						}
				    } else {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
					    if (statusCode == 503) {
					    	return RELOAD_DEVICE_TOKEN;
				    	} else {
				    		JSONObject json = new JSONObject(s);
							JSONObject meta = json.getJSONObject("meta");
							throw new ArrownockException(meta.getString("message"), ArrownockException.PUSH_FAILED_REGISTER);
				    	}
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_REGISTER);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_REGISTER);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_REGISTER);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}
	}
	
	public void unregisterDevice(String appKey, String deviceToken, String channels, boolean removeDevice, String type, String id) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			if (AnPush.getInstance(androidContext).isSecureConnection()) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost() + UNREGISTER_API_URL + "?key=" + appKey.trim());
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost() + UNREGISTER_API_URL + "?key=" + appKey.trim());
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
		    
		    try {
		    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
		    	String date = format.format(new Date());
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    	if(deviceToken != null) {
		    		nameValuePairs.add(new BasicNameValuePair("device_token", deviceToken));
		    	}
		    	if(id != null) {
		    		nameValuePairs.add(new BasicNameValuePair("anid", id));
		    	}
			    nameValuePairs.add(new BasicNameValuePair("date", date));
			    nameValuePairs.add(new BasicNameValuePair("type", type));
			    
			    String signatureString = null;
			    if(removeDevice) {
			    	nameValuePairs.add(new BasicNameValuePair("remove", "true"));
			    	signatureString = "/" + API_VERSION + "/" + UNREGISTER_ENDPOINT + getSortedSignatureString(nameValuePairs, appKey.trim());
			    } else {
			    	nameValuePairs.add(new BasicNameValuePair("channel", channels));
			    	signatureString = "/" + API_VERSION + "/" + UNREGISTER_ENDPOINT + getSortedSignatureString(nameValuePairs, appKey.trim());
			    }
			    String HMAC_SHA1_ALGORITHM = "HmacSHA1";
			    String signature = null;
	
			    try {         
			    	Key signingKey = new SecretKeySpec(getAPISecret().getBytes(), HMAC_SHA1_ALGORITHM);
			    	Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			    	mac.init(signingKey);
			    	byte[] rawHmac = mac.doFinal(signatureString.getBytes());
			    	signature = Base64.encodeToString(rawHmac, Base64.NO_WRAP);
			    	nameValuePairs.add(new BasicNameValuePair("signature", signature));
			    } catch (Exception e) {
			    	throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_UNREGISTER);
			    }
		      
			    OutputStream out = urlConnection.getOutputStream();
			    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			    writer.write(getQuery(nameValuePairs));
			    writer.close();
			    out.close();
			    
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode != 200) {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						throw new ArrownockException(meta.getString("message"), ArrownockException.PUSH_FAILED_UNREGISTER);
					} else {
						InputStream is = new BufferedInputStream(urlConnection.getInputStream());
					    String s = convertStreamToString(is);
					}
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_UNREGISTER);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_UNREGISTER);
		    }
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_UNREGISTER);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}
	}
	
	public void updateDeviceToken(String appKey, String oldToken, String newToken) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			if (AnPush.getInstance(androidContext).isSecureConnection()) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost() + UPDATE_TOKEN_API_URL + "?key=" + appKey.trim());
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost() + UPDATE_TOKEN_API_URL + "?key=" + appKey.trim());
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
		    try {
		    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
		    	String date = format.format(new Date());
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			    nameValuePairs.add(new BasicNameValuePair("old", oldToken));
			    nameValuePairs.add(new BasicNameValuePair("new", newToken));
			    nameValuePairs.add(new BasicNameValuePair("date", date));
			    
			    String signatureString = null;
		    	signatureString = "/" + API_VERSION + "/" + UPDATE_TOKEN_ENDPOINT + "date=" + date + "&key=" + appKey + "&new=" + newToken + "&old=" + oldToken + "&type=android-gcm";
			    
			    String HMAC_SHA1_ALGORITHM = "HmacSHA1";
			    String signature = null;
	
			    try {         
			    	Key signingKey = new SecretKeySpec(getAPISecret().getBytes(), HMAC_SHA1_ALGORITHM);
			    	Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			    	mac.init(signingKey);
			    	byte[] rawHmac = mac.doFinal(signatureString.getBytes());
			    	signature = Base64.encodeToString(rawHmac, Base64.NO_WRAP);
			    	nameValuePairs.add(new BasicNameValuePair("signature", signature));
			    } catch (Exception e) {
			    	throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_UPDATE_REGISTRATION);
			    }
		      
			    OutputStream out = urlConnection.getOutputStream();
			    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			    writer.write(getQuery(nameValuePairs));
			    writer.close();
			    out.close();
			    
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode != 200) {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						throw new ArrownockException(meta.getString("message"), ArrownockException.PUSH_FAILED_UPDATE_REGISTRATION);
					}
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_UPDATE_REGISTRATION);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_UPDATE_REGISTRATION);
		    }
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_UPDATE_REGISTRATION);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}
	}
	
	public KeyValuePair<String, Integer> getServiceHost(String deviceID) throws ArrownockException {
		SharedPreferences pref = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE);
		String pushHost = pref.getString(PushService.PREF_PUSH_HOST, null);
		int pushPort = pref.getInt(PushService.PREF_PUSH_PORT, 1883);
		long pushHostExpiration = pref.getLong(PushService.PREF_PUSH_HOST_EXPIRATION, 0);
		long currentTimeMillis = System.currentTimeMillis();
		boolean hostExpired = (currentTimeMillis > pushHostExpiration ? true : false);
		if(hostExpired){
			pushHost = null;
			// Remove stored push host
			Editor editor = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE).edit();
			editor.remove(PushService.PREF_PUSH_HOST);
			editor.remove(PushService.PREF_PUSH_PORT);
			editor.remove(PushService.PREF_PUSH_HOST_EXPIRATION);
			editor.commit();
		}
		
		if(pushHost == null || hostExpired) {
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(this.getDSHost() + "/" +  Constants.PUSH_DS_VERSION + "/" + HOST_URL + "?token=" + deviceID);
			Map<String, String> phoneStatusMap = getPhoneStatus(androidContext);
			for(String key : phoneStatusMap.keySet()){
				urlBuilder.append("&" + key + "=" + URLEncoder.encode(phoneStatusMap.get(key)));
			}
			urlBuilder.append("&version=" + Constants.SDK_VERSION);
			String url = urlBuilder.toString();
			
			int count = 0;
			while (pushHost == null && ++count <= PushConstants.MAX_REQUEST_TIME) {
				String urlplus = url + "&requesttime=" + count;
				Map<String, Object> requestResult = requestForHost(urlplus);
				boolean valid = (requestResult.get("valid").equals(true) ? true : false);
				if (valid) {
					// Valid Token
					pushHost = requestResult.get("hostname").toString();
					if (AnPush.getInstance(androidContext).isSecureConnection()) {
						pushPort = (Integer) requestResult.get("secure_port");
					} else {
						pushPort = (Integer) requestResult.get("port");
					}
					pushHostExpiration = (Long) requestResult.get("expiration");
				} else {
					int status = (Integer) requestResult.get("status");
					if (status == 503) {
						// Service unavailable on server side. Should retry again.
						String message = requestResult.get("message").toString();
						int retry_after = -1;
						try{
							retry_after = (Integer) requestResult.get("retry_after");
						}catch(Exception ex){}
						if(retry_after<=0){
							retry_after = PushConstants.REQUEST_RETRY_TIME;
						}
						int sleep_secs = (retry_after > PushConstants.MAX_REQUEST_RETRY_TIME ? PushConstants.MAX_REQUEST_RETRY_TIME
								: retry_after);
						String warning_message = "Getting push server failed. StatusCode:" + status + ", Message:" + message
								+ ". Will try again in " + sleep_secs + " seconds.";
						LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, warning_message);
						try {
							Thread.sleep(sleep_secs * 1000);
							if(!AnPush.getInstance(androidContext).isServiceEnabled()){
								return null;
							}
						} catch (InterruptedException ex) {
							throw new ArrownockException(ex, ArrownockException.PUSH_SERVICE_UNAVAILABLE);
						}
					} else if (status == 403) {
						// Forbidden. Will stop here. Maybe need to delete device token
						String message = requestResult.get("message").toString();
						boolean remove_token = (Boolean) requestResult.get("remove_token");
						String error_message = "Get host request has ben forbidden. Message:" + message
								+ (remove_token ? ". Device token needs to be removed and regenerated." : "");
						LogUtil.getInstance().error(LOG_TAG, LOG_NAME, error_message);
						if(remove_token){
							//TODO remove token
							removeFromLocalStorage(PushService.PREF_DEVICE_TOKEN);
							PushService.actionStop(androidContext);
						}
						return null;
					} else if (status == 400) {
						// Bad request. Will stop here.
						String message = requestResult.get("message").toString();
						LogUtil.getInstance().error(LOG_TAG, LOG_NAME, message);
						throw new ArrownockException(message, ArrownockException.PUSH_SERVICE_UNAVAILABLE);
					} else if (status == -1) {
						// Exception occurs, will stop here.
						Exception exception = (Exception) requestResult.get("exception");
						LogUtil.getInstance().error(LOG_TAG, LOG_NAME, exception.getMessage(), exception);
						throw new ArrownockException(exception, ArrownockException.PUSH_SERVICE_UNAVAILABLE);
					} else {
						// Other response status, will try again.
						String data = requestResult.get("data").toString();
						String warning_message = "Getting push server failed. StatusCode:" + status + ", Raw Data:" + data
								+ ". Will try again in " + PushConstants.REQUEST_RETRY_TIME + " seconds.";
						LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, warning_message);
						try {
							Thread.sleep(PushConstants.REQUEST_RETRY_TIME * 1000);
							if(!AnPush.getInstance(androidContext).isServiceEnabled()){
								return null;
							}
						} catch (InterruptedException ex) {
							throw new ArrownockException(ex, ArrownockException.PUSH_SERVICE_UNAVAILABLE);
						}
					}
				}

			}
			if (pushHost != null) {
				Editor editor = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE).edit();
				editor.putString(PushService.PREF_PUSH_HOST, pushHost);
				editor.putInt(PushService.PREF_PUSH_PORT, pushPort);
				editor.putLong(PushService.PREF_PUSH_HOST_EXPIRATION, pushHostExpiration);
				editor.commit();
			}else{
				// Reaches maximum retry time. End here.
				String error_message = "Max retry time reached";
				LogUtil.getInstance().error(LOG_TAG, LOG_NAME, error_message);
				throw new ArrownockException(error_message, ArrownockException.PUSH_SERVICE_UNAVAILABLE);
			}
		}
		return new KeyValuePair<String, Integer>(pushHost, pushPort);
	}
	
	public Map<String, Object> requestForHost(String urlplus) {
		Map<String, Object> result = new HashMap<String, Object>();
		HttpURLConnection urlConnection = null;
		try {
			if (AnPush.getInstance(androidContext).isSecureConnection()) {
				URL url = new URL(Constants.HTTPS + urlplus);
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + urlplus);
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.connect();
			
			int statusCode = urlConnection.getResponseCode();
			if(statusCode==200){	// Service OK
				InputStream is = new BufferedInputStream(urlConnection.getInputStream());
				String res = convertStreamToString(is);
				JSONObject json = new JSONObject(res);
				String hostname = json.getString("hostname");
				int port = json.optInt("port", 1883);
				int secure_port = json.optInt("secure_port", 8883);
				long expiration = json.optLong("expiration", 0);
				result.put("valid", true);
				result.put("status", 200);
				result.put("hostname", hostname);
				result.put("port", port);
				result.put("secure_port", secure_port);
				result.put("expiration", expiration);
			}else {
				InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
				String res = convertStreamToString(es);
				if(statusCode==400){	// Bad Request
					JSONObject json = new JSONObject(res);
					String message = json.getString("message");
					result.put("valid", false);
					result.put("status", 400);
					result.put("message", message);
				}else if(statusCode==403){	// Forbidden
					JSONObject json = new JSONObject(res);
					String message = json.getString("message");
					boolean remove_token = json.optBoolean("remove_token", false);
					result.put("valid", false);
					result.put("status", 403);
					result.put("message", message);
					result.put("remove_token", remove_token);
				}else if(statusCode==503){	// Service Unavilable
					JSONObject json = new JSONObject(res);
					String message = json.getString("message");
					int retry_after = json.optInt("retry_after", -1);
					result.put("valid", false);
					result.put("status", 503);
					result.put("message", message);
					result.put("retry_after", retry_after);
				}else{
					result.put("valid", false);
					result.put("status", statusCode);
					result.put("data", res);
				}
			}
		} catch (Exception ex) {
			result.put("valid", false);
			result.put("status", -1);
			result.put("exception", ex);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}  
		}
		return result;
	}
	
	public static Map<String, String> getPhoneStatus(Context context) {
		TelephonyManager phoneMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		Map<String, String> phoneStatusMap = new HashMap<String, String>();
		try {
			phoneStatusMap.put("phone_model", Build.MODEL);
			phoneStatusMap.put("manufacturer", Build.MANUFACTURER);
			phoneStatusMap.put("sdk_version", String.valueOf(Build.VERSION.SDK_INT));
			phoneStatusMap.put("release_version", Build.VERSION.RELEASE);
			phoneStatusMap.put("network_operator", phoneMgr.getNetworkOperatorName());
			phoneStatusMap.put("network_type", String.valueOf(phoneMgr.getNetworkType()));
			phoneStatusMap.put("sim_operator", phoneMgr.getSimOperatorName());
		} catch (Exception ex) {
			LogUtil.getInstance().error(LOG_TAG, LOG_NAME, "Error on getPhoneStatus()", ex);
		}
		return phoneStatusMap;
	}
	
	private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
	    StringBuilder result = new StringBuilder();
	    boolean first = true;

	    for (NameValuePair pair : params)
	    {
	        if (first)
	            first = false;
	        else
	            result.append("&");

	        result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
	        result.append("=");
	        result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
	    }

	    return result.toString();
	}
	
	private String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	// Scheduling related functions
	public void setMuteAsync(final String appKey, final String deviceToken, final IAnPushCallback callback) {
		Runnable runnable = new Runnable(){
			public void run() {
				try{
					setMuteUtil(appKey, deviceToken, true);
					if(callback != null) {
						callback.setMute(false, null);
					}
				} catch(ArrownockException ex){
					if(callback != null) {
						callback.setMute(true, ex);
					}
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}
	
	public void setMuteAsync(final String appKey, final String deviceToken, final IAnPushSettingsCallback callback) {
        Runnable runnable = new Runnable(){
            public void run() {
                try{
                    setMuteUtil(appKey, deviceToken, true);
                    if(callback != null) {
                        callback.onSuccess();
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
	}
	
	@Deprecated
	public void setScheduledMuteAsync(final String appKey, final String deviceToken, final int startGMTHour, final int startGMTMinute, final int duration, final IAnPushCallback callback) {
		Runnable runnable = new Runnable(){
			public void run() {
				try{
					setMuteUtil(appKey, deviceToken, true, startGMTHour, startGMTMinute, duration);
					if(callback != null) {
						callback.setScheduledMute(false, null);
					}
				} catch(ArrownockException ex){
					if(callback != null) {
						callback.setScheduledMute(true, ex);
					}
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}
	
	public void setScheduledMuteAsync(final String appKey, final String deviceToken, final int startGMTHour, final int startGMTMinute, final int duration, final IAnPushSettingsCallback callback) {
	    Runnable runnable = new Runnable(){
            public void run() {
                try{
                    setMuteUtil(appKey, deviceToken, true, startGMTHour, startGMTMinute, duration);
                    if(callback != null) {
                        callback.onSuccess();
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
	}
	
	@Deprecated
	public void clearMuteAsync(final String appKey, final String deviceToken, final IAnPushCallback callback) {
        Runnable runnable = new Runnable(){
            public void run() {
                try{
                    setMuteUtil(appKey, deviceToken, false);
                    if(callback != null) {
                        callback.clearMute(false, null);
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.clearMute(true, ex);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
	
	public void clearMuteAsync(final String appKey, final String deviceToken, final IAnPushSettingsCallback callback) {
        Runnable runnable = new Runnable(){
            public void run() {
                try{
                    setMuteUtil(appKey, deviceToken, false);
                    if(callback != null) {
                        callback.onSuccess();
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
	
	private void setMuteUtil(String appKey, String deviceToken, boolean mute) throws ArrownockException {
		setMuteUtil(appKey, deviceToken, mute, -1, -1, -1);
	}
	
	private void setMuteUtil(String appKey, String deviceToken, boolean mute, int startGMTHour, int startGMTMinute, int duration) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			if (AnPush.getInstance(androidContext).isSecureConnection()) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost() + MUTE_API_URL + "?key=" + appKey.trim());
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost() + MUTE_API_URL + "?key=" + appKey.trim());
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
		    
		    try {
		    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
		    	String date = format.format(new Date());
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			    nameValuePairs.add(new BasicNameValuePair("device_token", deviceToken));
			    nameValuePairs.add(new BasicNameValuePair("date", date));
			    if (startGMTHour != -1) {
			    	nameValuePairs.add(new BasicNameValuePair("hour", Integer.valueOf(startGMTHour).toString()));
			    	nameValuePairs.add(new BasicNameValuePair("minute", Integer.valueOf(startGMTMinute).toString()));
			    	nameValuePairs.add(new BasicNameValuePair("duration", Integer.valueOf(duration).toString()));
			    }
			    
			    String signatureString = null;
			    if (mute) {
			    	nameValuePairs.add(new BasicNameValuePair("mute", "true"));
			    	if (startGMTHour == -1) {
			    		signatureString = "/" + API_VERSION + "/" + MUTE_ENDPOINT + "date=" + date + "&device_token=" + deviceToken + "&key=" + appKey + "&mute=true";
			    	} else {
			    		signatureString = "/" + API_VERSION + "/" + MUTE_ENDPOINT + "date=" + date + "&device_token=" + deviceToken + "&duration=" + Integer.valueOf(duration).toString() + "&hour=" + Integer.valueOf(startGMTHour).toString() + "&key=" + appKey + "&minute=" + Integer.valueOf(startGMTMinute) + "&mute=true";
			    	}
			    } else {
			    	nameValuePairs.add(new BasicNameValuePair("mute", "false"));
				    signatureString = "/" + API_VERSION + "/" + MUTE_ENDPOINT + "date=" + date + "&device_token=" + deviceToken + "&key=" + appKey + "&mute=false";
			    }
			   
			    String HMAC_SHA1_ALGORITHM = "HmacSHA1";
			    String signature = null;
	
			    try {         
			    	Key signingKey = new SecretKeySpec(getAPISecret().getBytes(), HMAC_SHA1_ALGORITHM);
			    	Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			    	mac.init(signingKey);
			    	byte[] rawHmac = mac.doFinal(signatureString.getBytes());
			    	signature = Base64.encodeToString(rawHmac, Base64.NO_WRAP);
			    	nameValuePairs.add(new BasicNameValuePair("signature", signature));
			    } catch (Exception e) {
			    	throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_SET_MUTE);
			    }
		      
			    OutputStream out = urlConnection.getOutputStream();
			    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			    writer.write(getQuery(nameValuePairs));
			    writer.close();
			    out.close();
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode != 200) {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						throw new ArrownockException(meta.getString("message"), ArrownockException.PUSH_FAILED_SET_MUTE);
					}
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_SET_MUTE);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_SET_MUTE);
		    }
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_SET_MUTE);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}	
	}
	
	@Deprecated
	public void setSilentPeriodAsync(final String appKey, final String deviceToken, final int startGMTHour, final int startGMTMinute, final int duration, final boolean resend, final IAnPushCallback callback) {
        Runnable runnable = new Runnable(){
            public void run() {
                try{
                    setSilentUtil(appKey, deviceToken, startGMTHour, startGMTMinute, duration, resend, true);
                    if(callback != null) {
                        callback.setSilentPeriod(false, null);
                    }
                    
                    // save start/stop connection time info to local storage
                    Editor editor = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE).edit();
                    editor.putInt(PushService.PREF_PUSH_SCHEDULED_HOUR, startGMTHour);
                    editor.putInt(PushService.PREF_PUSH_SCHEDULED_MINUTE, startGMTMinute);
                    editor.putLong(PushService.PREF_PUSH_SCHEDULED_DURATION, duration*60*1000);
                    editor.commit();
                    
                    scheduleStartStopConnection();
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.setSilentPeriod(true, ex);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
	
	public void setSilentPeriodAsync(final String appKey, final String deviceToken, final int startGMTHour, final int startGMTMinute, final int duration, final boolean resend, final IAnPushSettingsCallback callback) {
        Runnable runnable = new Runnable(){
            public void run() {
                try{
                    setSilentUtil(appKey, deviceToken, startGMTHour, startGMTMinute, duration, resend, true);
                    if(callback != null) {
                        callback.onSuccess();
                    }
                    
                    // save start/stop connection time info to local storage
                    Editor editor = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE).edit();
                    editor.putInt(PushService.PREF_PUSH_SCHEDULED_HOUR, startGMTHour);
                    editor.putInt(PushService.PREF_PUSH_SCHEDULED_MINUTE, startGMTMinute);
                    editor.putLong(PushService.PREF_PUSH_SCHEDULED_DURATION, duration*60*1000);
                    editor.commit();
                    
                    scheduleStartStopConnection();
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
	
	@Deprecated
	public void clearSilentPeriodAsync(final String appKey, final String deviceToken, final IAnPushCallback callback) {
		Runnable runnable = new Runnable(){
			public void run() {
				try{
					setSilentUtil(appKey, deviceToken, false);
					if(callback != null) {
						callback.clearSilentPeriod(false, null);
					}
					
					// remove start/stop connection time info to local storage
					Editor editor = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE).edit();
					editor.remove(PushService.PREF_PUSH_SCHEDULED_HOUR);
					editor.remove(PushService.PREF_PUSH_SCHEDULED_MINUTE);
					editor.remove(PushService.PREF_PUSH_SCHEDULED_DURATION);
					editor.commit();
					
					cancelStartStop();
				} catch(ArrownockException ex){
					if(callback != null) {
						callback.clearSilentPeriod(true, ex);
					}
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}
	
	public void clearSilentPeriodAsync(final String appKey, final String deviceToken, final IAnPushSettingsCallback callback) {
        Runnable runnable = new Runnable(){
            public void run() {
                try{
                    setSilentUtil(appKey, deviceToken, false);
                    if(callback != null) {
                        callback.onSuccess();
                    }
                    
                    // remove start/stop connection time info to local storage
                    Editor editor = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE).edit();
                    editor.remove(PushService.PREF_PUSH_SCHEDULED_HOUR);
                    editor.remove(PushService.PREF_PUSH_SCHEDULED_MINUTE);
                    editor.remove(PushService.PREF_PUSH_SCHEDULED_DURATION);
                    editor.commit();
                    
                    cancelStartStop();
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
	
	private void setSilentUtil(String appKey, String deviceToken, boolean silent) throws ArrownockException {
		setSilentUtil(appKey, deviceToken, -1, -1, -1, false, silent);
	}
	
	private void setSilentUtil(String appKey, String deviceToken, int startGMTHour, int startGMTMinute, int duration, boolean resend, boolean set) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			if (AnPush.getInstance(androidContext).isSecureConnection()) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost() + SILENT_API_URL + "?key=" + appKey.trim());
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost() + SILENT_API_URL + "?key=" + appKey.trim());
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
		    
		    try {
		    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
		    	String date = format.format(new Date());
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			    nameValuePairs.add(new BasicNameValuePair("device_token", deviceToken));
			    nameValuePairs.add(new BasicNameValuePair("date", date));
			    
			    String signatureString = null;
			    if (set) {
			    	nameValuePairs.add(new BasicNameValuePair("hour", Integer.valueOf(startGMTHour).toString()));
			    	nameValuePairs.add(new BasicNameValuePair("minute", Integer.valueOf(startGMTMinute).toString()));
			    	nameValuePairs.add(new BasicNameValuePair("duration", Integer.valueOf(duration).toString()));
			    	nameValuePairs.add(new BasicNameValuePair("set", "true"));
			    	if (resend) {
			    		nameValuePairs.add(new BasicNameValuePair("resend", "true"));
			    		signatureString = "/" + API_VERSION + "/" + SILENT_ENDPOINT + "date=" + date + "&device_token=" + deviceToken + "&duration=" + Integer.valueOf(duration).toString() + "&hour=" + Integer.valueOf(startGMTHour).toString() + "&key=" + appKey + "&minute=" + Integer.valueOf(startGMTMinute).toString() + "&resend=true" + "&set=true";
			    	} else {
			    		nameValuePairs.add(new BasicNameValuePair("resend", "false"));
			    		signatureString = "/" + API_VERSION + "/" + SILENT_ENDPOINT + "date=" + date + "&device_token=" + deviceToken + "&duration=" + Integer.valueOf(duration).toString() + "&hour=" + Integer.valueOf(startGMTHour).toString() + "&key=" + appKey + "&minute=" + Integer.valueOf(startGMTMinute).toString() + "&resend=false" + "&set=true";
			    	}
			    } else {
			    	nameValuePairs.add(new BasicNameValuePair("set", "false"));
				    signatureString = "/" + API_VERSION + "/" + SILENT_ENDPOINT + "date=" + date + "&device_token=" + deviceToken + "&key=" + appKey + "&set=false";
			    }
			   
			    String HMAC_SHA1_ALGORITHM = "HmacSHA1";
			    String signature = null;
	
			    try {         
			    	Key signingKey = new SecretKeySpec(getAPISecret().getBytes(), HMAC_SHA1_ALGORITHM);
			    	Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			    	mac.init(signingKey);
			    	byte[] rawHmac = mac.doFinal(signatureString.getBytes());
			    	signature = Base64.encodeToString(rawHmac, Base64.NO_WRAP);
			    	nameValuePairs.add(new BasicNameValuePair("signature", signature));
			    } catch (Exception e) {
			    	throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_SET_SILENT);
			    }
		      
			    OutputStream out = urlConnection.getOutputStream();
			    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			    writer.write(getQuery(nameValuePairs));
			    writer.close();
			    out.close();
			    
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode != 200) {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						throw new ArrownockException(meta.getString("message"),  ArrownockException.PUSH_FAILED_SET_SILENT);
					}
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(),  ArrownockException.PUSH_FAILED_SET_SILENT);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(),  ArrownockException.PUSH_FAILED_SET_SILENT);
		    }
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(),  ArrownockException.PUSH_FAILED_SET_SILENT);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}	
	}
	
	@Override
	@Deprecated
	public void setBadgeAsync(final String appKey, final String deviceToken, final int number, final IAnPushCallback callback) {
        Runnable runnable = new Runnable(){
            public void run() {
                try{
                    setBadgeUtil(appKey, deviceToken, number);
                    if(callback != null) {
                        callback.setBadge(false, null);
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.setBadge(true, ex);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
	
	public void setBadgeAsync(final String appKey, final String deviceToken, final int number, final IAnPushSettingsCallback callback) {
        Runnable runnable = new Runnable(){
            public void run() {
                try{
                    setBadgeUtil(appKey, deviceToken, number);
                    if(callback != null) {
                        callback.onSuccess();
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
	
	private void setBadgeUtil(String appKey, String deviceToken, int number) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			if (AnPush.getInstance(androidContext).isSecureConnection()) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost() + SET_BADGE_API_URL + "?key=" + appKey.trim());
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost() + SET_BADGE_API_URL + "?key=" + appKey.trim());
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
		    
		    try {
		    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
		    	String date = format.format(new Date());
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			    nameValuePairs.add(new BasicNameValuePair("device_token", deviceToken));
			    nameValuePairs.add(new BasicNameValuePair("date", date));
			    nameValuePairs.add(new BasicNameValuePair("badge", String.valueOf(number)));
			    
			    String signatureString = null;
			    signatureString = "/" + API_VERSION + "/" + SET_BADGE_ENDPOINT + "badge=" + number + "&date=" + date + "&device_token=" + deviceToken + "&key=" + appKey;
			   
			    String HMAC_SHA1_ALGORITHM = "HmacSHA1";
			    String signature = null;
	
			    try {         
			    	Key signingKey = new SecretKeySpec(getAPISecret().getBytes(), HMAC_SHA1_ALGORITHM);
			    	Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			    	mac.init(signingKey);
			    	byte[] rawHmac = mac.doFinal(signatureString.getBytes());
			    	signature = Base64.encodeToString(rawHmac, Base64.NO_WRAP);
			    	nameValuePairs.add(new BasicNameValuePair("signature", signature));
			    } catch (Exception e) {
			    	throw new ArrownockException(e.getMessage(), ArrownockException.PUSH_FAILED_SET_BADGE);
			    }
		      
			    OutputStream out = urlConnection.getOutputStream();
			    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			    writer.write(getQuery(nameValuePairs));
			    writer.close();
			    out.close();
			    
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode != 200) {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						throw new ArrownockException(meta.getString("message"),  ArrownockException.PUSH_FAILED_SET_BADGE);
					}
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(),  ArrownockException.PUSH_FAILED_SET_BADGE);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(),  ArrownockException.PUSH_FAILED_SET_BADGE);
		    }
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(),  ArrownockException.PUSH_FAILED_SET_BADGE);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}
	}
	
	public void scheduleStartStopConnection() throws ArrownockException {
		SharedPreferences mPrefs = androidContext.getSharedPreferences(PushService.LOG_TAG, Context.MODE_PRIVATE);
		int startGMTHour = mPrefs.getInt(PushService.PREF_PUSH_SCHEDULED_HOUR, -1);
	    int startGMTMinute = mPrefs.getInt(PushService.PREF_PUSH_SCHEDULED_MINUTE, -1);
	    long duration = mPrefs.getLong(PushService.PREF_PUSH_SCHEDULED_DURATION, -1);
	    
	    if (startGMTHour != -1 && startGMTMinute != -1 && duration != -1) {
			// set AlarmManager to start/stop push service to save server resource
			Calendar start = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			start.set(Calendar.HOUR_OF_DAY, startGMTHour);
			start.set(Calendar.MINUTE, startGMTMinute);
			start.set(Calendar.SECOND, 0);
			
			Calendar nowLocal = Calendar.getInstance();

			long msInDay = 24 * 60 * 60 * 1000;
			long startMill = start.getTime().getTime();
			long nowLocalMill = nowLocal.getTime().getTime();
			long nowLocalMillPlus24 = nowLocalMill + msInDay;
			long endMill = startMill + duration;
			
			long startServiceTime;
			long stopServiceTime;
			
			if ((startMill <= nowLocalMill && endMill > nowLocalMill) || (startMill <= nowLocalMillPlus24 && endMill > nowLocalMillPlus24)){
				// check whether connection is enabled now, if yes, it's in silent period, disable connection
				try {
					if (AnPush.getInstance(androidContext).isServiceEnabled()) {
						Intent stopIntent = new Intent();
						stopIntent.setClass(androidContext, PushService.class);
						stopIntent.setAction(PushConstants.BROKER_CLIENTID_SUFFIX + ".STOP");
						PendingIntent piStop = PendingIntent.getService(androidContext, 0, stopIntent, 0);
						AlarmManager alarmMgr = (AlarmManager) androidContext.getSystemService(PushService.ALARM_SERVICE);
						alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()-1, piStop);
					}
				} catch (ArrownockException e) {
					throw (ArrownockException) e;
				}
				
				startServiceTime = startMill < nowLocalMill ? endMill - nowLocalMill : endMill - nowLocalMillPlus24;
				stopServiceTime = startServiceTime - duration + msInDay;
			} else {
				stopServiceTime = startMill < nowLocalMill ? startMill + msInDay - nowLocalMill : startMill - nowLocalMill;
				startServiceTime = stopServiceTime + duration;
			}
						
			// schedule alarmManager to stop/start connection
			Log.d(LOG_TAG, "Stop connection in " + stopServiceTime/1000  +" seconds ....");
			Intent stopIntent = new Intent();
			stopIntent.setClass(androidContext, PushService.class);
			stopIntent.setAction(PushConstants.BROKER_CLIENTID_SUFFIX + ".STOP");
			PendingIntent piStop = PendingIntent.getService(androidContext, 0, stopIntent, 0);
			AlarmManager alarmMgr = (AlarmManager) androidContext.getSystemService(PushService.ALARM_SERVICE);
			alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + stopServiceTime, msInDay, piStop);
			
			Log.d(LOG_TAG, "Start connection in " + startServiceTime/1000 + " seconds ....");
			Intent startIntent = new Intent();
			startIntent.setClass(androidContext, PushService.class);
			startIntent.setAction(PushConstants.BROKER_CLIENTID_SUFFIX + ".START");
			PendingIntent piStart = PendingIntent.getService(androidContext, 0, startIntent, 0);
			alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + startServiceTime, msInDay, piStart);
	    }
	    
	}
	
	private void cancelStartStop() throws ArrownockException {
		// check whether connection is disabled, if disabled, enable connection
		try {
			if (!AnPush.getInstance(androidContext).isServiceEnabled()) {
				Intent startIntent = new Intent();
				startIntent.setClass(androidContext, PushService.class);
				startIntent.setAction(PushConstants.BROKER_CLIENTID_SUFFIX + ".START");
				PendingIntent piStart = PendingIntent.getService(androidContext, 0, startIntent, 0);
				AlarmManager alarmMgr = (AlarmManager) androidContext.getSystemService(PushService.ALARM_SERVICE);
				alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()-1, piStart);
			}
		} catch (ArrownockException e) {
			throw (ArrownockException) e;
		}
		
		// clear all the AlarmManagers
		Log.d(LOG_TAG, "Cancel AlarmManagers for start/stop connection ....");
		Intent stopIntent = new Intent();
		stopIntent.setClass(androidContext, PushService.class);
		stopIntent.setAction(PushConstants.BROKER_CLIENTID_SUFFIX + ".STOP");
		PendingIntent piStop = PendingIntent.getService(androidContext, 0, stopIntent, 0);
		AlarmManager alarmMgr = (AlarmManager) androidContext.getSystemService(PushService.ALARM_SERVICE);
		
		Intent startIntent = new Intent();
		startIntent.setClass(androidContext, PushService.class);
		startIntent.setAction(PushConstants.BROKER_CLIENTID_SUFFIX + ".START");
		PendingIntent piStart = PendingIntent.getService(androidContext, 0, startIntent, 0);
		//AlarmManager alarmMgr = (AlarmManager) androidContext.getSystemService(PushService.ALARM_SERVICE);
		
		alarmMgr.cancel(piStop);
		alarmMgr.cancel(piStart);
	}
	
	private String getAPIHost() {
		String api = getFromLocalStorage(PushService.PREF_API_HOST);
		return "".equals(api)? API_BASE_URL : api;
	}
	
	private String getDSHost() {
		String ds = getFromLocalStorage(PushService.PREF_DS_HOST);
		return "".equals(ds)? Constants.PUSH_DS_URL : ds;
	}
	
	private String getAPISecret() {
		String secret = getFromLocalStorage(PushService.PREF_API_SECRET);
		return "".equals(secret)? SIG_SEC : secret;
	}
	
	public String getRegistrationId(Context context) throws ArrownockException {
	    String registrationId = getFromLocalStorage(GCM_REG_ID);
	    if (registrationId.isEmpty()) {
	        return "";
	    }
	    String versionCode = getFromLocalStorage(GCM_REG_VERSION_CODE);
	    int registeredVersion = Integer.MIN_VALUE;
	    if(!versionCode.isEmpty()) {
	    	try {
	    		registeredVersion = Integer.parseInt(versionCode);
	    	} catch(Exception e) {
	    		//mute
	    	}
	    }
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        return "";	// App version changed
	    }
	    return registrationId;
	}
	
	public String getMiPushRegId(Context context) throws ArrownockException {
		String regId = getFromLocalStorage(MIPUSH_REG_ID);
	    if (regId.isEmpty()) {
	        return "";
	    }
	    String app = getFromLocalStorage(MIPUSH_APP);
	    if(app.isEmpty()) {
	    	return "";
	    }
	    String currentApp = getFromLocalStorage(AnPush.MIPUSH_APPID) + getFromLocalStorage(AnPush.MIPUSH_APPKEY);
	    if (!currentApp.equals(app)) {
	        return "";	// MiPush Appid & Appkey changed
	    }
	    return regId;
	}
	
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(androidContext);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        return false;
	    }
	    return true;
	}
	
	private void storeRegistrationId(Context context, String regId) throws ArrownockException {
	    saveToLocalStorage(GCM_REG_ID, regId);
	    saveToLocalStorage(GCM_SENDER_ID, AnPush.getInstance(androidContext).getSenderId());
	    int appVersion = getAppVersion(context);
	    saveToLocalStorage(GCM_REG_VERSION_CODE, String.valueOf(appVersion));
	}
	
	private String getServerCert() {
		return getFromLocalStorage(PushService.PREF_SERVER_CERT, Constants.SSL_SERVER_CERT);
	}
	
	private String getClientCert() {
		return getFromLocalStorage(PushService.PREF_CLIENT_CERT, Constants.SSL_CLIENT_CERT);
	}
	
	private String getClientKey() {
		return getFromLocalStorage(PushService.PREF_CLIENT_KEY, Constants.SSL_CLIENT_KEY);
	}
	
	public String getDeviceUniqueId(Context context, String appKey) {
		try {
			String deviceId = null;
			try {
				TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				deviceId = tm.getDeviceId();
			} catch(Exception e) {
				// android 6 might throw exception here, mute
			}
			String serial = android.os.Build.SERIAL;
			String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
			
			if(deviceId == null || deviceId.equals("000000000000000") || "".equals(deviceId.trim())) {
				deviceId = "";
			} else {
				deviceId = "D" + deviceId;
			}
	
			if(serial == null || serial.trim().equals("unknown")){
				serial = "";
			}else{
				serial = "S" + serial;
			}
			
			if (androidId == null || "".equals(androidId.trim())) {
				androidId = "";
			} else {
				androidId = "A" + androidId;
			}
			
			if("".equals(deviceId) && "".equals(serial) && "".equals(androidId)) {
                return "";
			} else {
			    String id = deviceId + serial + androidId + "K" + appKey;
			    MessageDigest md;
	            try {
	                md = MessageDigest.getInstance("MD5");
	                byte[] array = md.digest(id.getBytes());
	                StringBuffer sb = new StringBuffer();
	                for (int i = 0; i < array.length; ++i) {
	                    sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
	                }
	                id = sb.toString();
	            } catch (NoSuchAlgorithmException e) {
	                // no md5
	                id = UUID.nameUUIDFromBytes(id.getBytes()).toString();
	            }
	            id = "2" + id;
				return id;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        return "";
	}
	
	private String md5Java(String id){ 
		String digest = null; 
		try { 
			MessageDigest md = MessageDigest.getInstance("MD5"); 
			byte[] hash = md.digest(id.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder(2 * hash.length); 
			for(byte b : hash){ 
				sb.append(String.format("%02x", b&0xff)); 
			} 
			digest = sb.toString(); 
		} catch (UnsupportedEncodingException ex) { 
			ex.printStackTrace();
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		} 
		return digest; 
	}
	
	private String convertToBase64String(String input) {
		String rawString = Base64.encodeToString(input.getBytes(), Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
		if(rawString != null) {
			if(rawString.endsWith("==")) {
				rawString.substring(0, rawString.length() - 2);
			} else if(rawString.endsWith("=")) {
				rawString.substring(0, rawString.length() - 1);
			}
		}
		return rawString;
	}
	
	private String getSortedSignatureString(List<NameValuePair> nameValuePairs, String appKey) {
		StringBuffer sb = new StringBuffer();
		Map<String, String> map = new TreeMap<String, String>();
		map.put("key", appKey);
		for(NameValuePair pair : nameValuePairs) {
			map.put(pair.getName(), pair.getValue());
		}
		for(String key : map.keySet()) {
			sb.append(key + "=" + map.get(key) + "&");
		}
		if(sb.length() > 0) {
			return sb.substring(0, sb.length() - 1);
		} else {
			return "";
		}
	}
}