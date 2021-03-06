package com.arrownock.im;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.callback.AnIMAddClientsCallbackData;
import com.arrownock.im.callback.AnIMAddClientsEventData;
import com.arrownock.im.callback.AnIMBinaryCallbackData;
import com.arrownock.im.callback.AnIMBindAnPushServiceCallbackData;
import com.arrownock.im.callback.AnIMCreateTopicCallbackData;
import com.arrownock.im.callback.AnIMCreateTopicEventData;
import com.arrownock.im.callback.AnIMGetClientIdCallbackData;
import com.arrownock.im.callback.AnIMGetClientsStatusCallbackData;
import com.arrownock.im.callback.AnIMGetSessionInfoCallbackData;
import com.arrownock.im.callback.AnIMGetTopicInfoCallbackData;
import com.arrownock.im.callback.AnIMGetTopicListCallbackData;
import com.arrownock.im.callback.AnIMMessageCallbackData;
import com.arrownock.im.callback.AnIMMessageSentCallbackData;
import com.arrownock.im.callback.AnIMNoticeCallbackData;
import com.arrownock.im.callback.AnIMReadACKCallbackData;
import com.arrownock.im.callback.AnIMReceiveACKCallbackData;
import com.arrownock.im.callback.AnIMRemoveClientsCallbackData;
import com.arrownock.im.callback.AnIMRemoveClientsEventData;
import com.arrownock.im.callback.AnIMRemoveTopicCallbackData;
import com.arrownock.im.callback.AnIMRemoveTopicEventData;
import com.arrownock.im.callback.AnIMStatusUpdateCallbackData;
import com.arrownock.im.callback.AnIMTopicBinaryCallbackData;
import com.arrownock.im.callback.AnIMTopicMessageCallbackData;
import com.arrownock.im.callback.AnIMUnbindAnPushServiceCallbackData;
import com.arrownock.im.callback.AnIMUpdateTopicCallbackData;
import com.arrownock.im.callback.AnIMUpdateTopicEventData;
import com.arrownock.im.callback.IAnIMBlacklistCallback;
import com.arrownock.im.callback.IAnIMCallback;
import com.arrownock.im.callback.IAnIMGetClientIdCallback;
import com.arrownock.im.callback.IAnIMGetClientsStatusCallback;
import com.arrownock.im.callback.IAnIMGetTopicInfoCallback;
import com.arrownock.im.callback.IAnIMGetTopicListCallback;
import com.arrownock.im.callback.IAnIMHistoryCallback;
import com.arrownock.im.callback.IAnIMListBlacklistsCallback;
import com.arrownock.im.callback.IAnIMPushBindingCallback;
import com.arrownock.im.callback.IAnIMPushNotificationSettingsCallback;
import com.arrownock.im.callback.IAnIMTopicCallback;
import com.arrownock.internal.desk.IAnDeskCallback;
import com.arrownock.internal.device.DeviceManager;
import com.arrownock.internal.groupedia.IAnGroupediaCallback;
import com.arrownock.internal.live.ISignalController;
import com.arrownock.internal.util.ANEmojiUtil;
import com.arrownock.internal.util.Constants;
import com.arrownock.internal.util.DefaultHostnameVerifier;
import com.arrownock.internal.util.KeyValuePair;
import com.arrownock.push.ANBase64;
import com.arrownock.push.IMQTTAgent;
import com.arrownock.push.IMQTTEvent;
import com.arrownock.push.MQTTConnectionStatus;
import com.arrownock.push.PahoAgent;
import com.arrownock.push.PahoSocketFactory;
import com.arrownock.push.PushService;

public interface IAnIMUtility {
    @Deprecated
	void getClientIdAsync(final String appKey, final String userId);
    void getClientIdAsync(final String appKey, final String userId, final IAnIMGetClientIdCallback callback);
	void connectAsync(final String appKey, final String clientId);
	void disconnectAsync(final String appKey);
	void sendMessageAsync(final String msgId, final String topicName, final String payload);
	void sendMessageAsync(final String msgId, final String topicName, final String payload, final int Qos);
	void sendMessageToSessionAsync(final String appKey, final String msgId, final String topicName, final String payload);
	@Deprecated
	void createTopicAsync(final String appKey, final String topicName, final String clientsStr, final String owner);
    void createTopicAsync(final String appKey, final String topicName, final String clientsStr, final String owner, final Map<String, String> customData, 
            final boolean isNeedNotice, final String currentClientId, final IAnIMTopicCallback callback);
	@Deprecated
	void updateTopicAsync(final String appKey, final String topicId, final String topicName, final String owner);
	void updateTopicAsync(final String appKey, final String topicId, final String topicName, final String owner, final Map<String, String> customData, 
	        final boolean isNeedNotice, final String currentClientId, final IAnIMTopicCallback callback);
    @Deprecated
	void addClientsAsync(final String appKey, final String topic, final String clientsStr);
	void addClientsAsync(final String appKey, final String topic, final String clientsStr, final boolean isNeedNotice, 
            final String currentClientId, final IAnIMTopicCallback callback);
	@Deprecated
	void removeClientsAsync(final String appKey, final String topic, final String clientsStr);
	void removeClientsAsync(final String appKey, final String topic, final String clientsStr, final boolean isNeedNotice, 
            final String currentClientId, final IAnIMTopicCallback callback);
	@Deprecated
	void removeTopic(final String appKey, final String topic);
	void removeTopic(final String appKey, final String topic, final boolean isNeedNotice, final String currentClientId, final IAnIMTopicCallback callback);
	@Deprecated
	void bindAnPushAsync(final String appKey, final String clientId, final String anid, final String anPushAppKey, final String deviceTypeStr); 
	void bindAnPushAsync(final String appKey, final String clientId, final String anid, final String anPushAppKey, final String deviceTypeStr, final IAnIMPushBindingCallback callback); 
	@Deprecated
	void unbindAnPushAsync(final String appKey, final String clientId);
	void unbindAnPushAsync(final String appKey, final String clientId, final IAnIMPushBindingCallback callback);
	void sendNoticeAsync(final String appKey, final String clientId, final String notice, final String clientsStr, final String topic, final String customDataStr, final String msgId, final boolean receiveACK);
	@Deprecated
	void getTopicInfoAsync(final String appKey, final String topic);
	void getTopicInfoAsync(final String appKey, final String topic, final IAnIMGetTopicInfoCallback callback);
	void getTopicLogAsync(final String appKey, final String topicId, final String clientId, final int limit, final long timestamp, final boolean isOffline, final IAnIMHistoryCallback callback);
	void getFullTopicLogAsync(final String appKey, final String topicId, final String clientId, final int limit, final long timestamp, final IAnIMHistoryCallback callback);
	void getLogAsync(final String appKey, final Set<String> clientIds, final String clientId, final int limit, final long timestamp, final boolean isOffline, final IAnIMHistoryCallback callback);
	void syncHistoryAsync(final String appKey, final String me, final int limit, final long timestamp, final IAnIMHistoryCallback callback);
	
	@Deprecated
	void getClientsStatusAsync(final String appKey, final String clientsStr);
	void getClientsStatusAsync(final String appKey, final String clientsStr, final IAnIMGetClientsStatusCallback callback);
	@Deprecated
	void getTopicStatusAsync(final String appKey, final String topicId);
	void getTopicStatusAsync(final String appKey, final String topicId, final IAnIMGetClientsStatusCallback callback);
	void getSessionInfoAsync(final String appKey, final String sessionId);
	@Deprecated
	void getTopicListAsync(final String appKey, final String clientId);
	void getTopicListAsync(final String appKey, final String clientId, final IAnIMGetTopicListCallback callback);
	void setPushNotificationAsync(final String clientId, final boolean isEnable, final int type, final List<String> topicIds, final IAnIMPushNotificationSettingsCallback callback);
	
	String generateMsgID();
	String getClientId();
	
	void saveToLocalStorage(final Context androidContext, final String key, final String value);
	String getFromLocalStorage(final Context androidContext, final String key);
	void removeFromLocalStorage(final Context androidContext, final String key);
	void setHostsUtil(Context androidContext, String api, String ds);
	void setCallbacks(List<IAnIMCallback> callbacks);
	void setDeskCallback(IAnDeskCallback callback);
	void setGroupediaCallback(IAnGroupediaCallback callback);
	void setContext(Context context);
	void setSecureConnection(boolean isSecure);
	void setAppKey(String appKey);
	AnIMStatus getCurrentStatus();
	void setSignalControllerCallback(ISignalController.Callbacks callback);
	void createLiveSession(final String appKey, final String owner, final List<String> partyIds, final String type);
	void validateLiveSession(final String appKey, String sessionId);
	void terminateLiveSession(final String appKey, String sessionId);
	
	void addBlacklistAsync(final String appKey, final String currentClientId, final String targetClientId, final IAnIMBlacklistCallback callback);
	void removeBlacklistAsync(final String appKey, final String currentClientId, final String targetClientId, final IAnIMBlacklistCallback callback);
	void listBlacklistsAsync(final String appKey, final String currentClientId, final IAnIMListBlacklistsCallback callback);
}

class AnMessageUtility implements IAnIMUtility {
	private final static String LOG_TAG_CLASS = AnIM.class.getName();
	private final static String IM_DISPATCH_BASE_URL = Constants.IM_DS_URL;
	private final static String DISPATCH_VERSION = Constants.IM_DS_VERSION;
	private final static String API_BASE_URL = Constants.ARROWNOCK_API_URL;
	private final static String API_VERSION = Constants.ARROWNOCK_API_VERSION;
	private final static String TOKEN_ENDPOINT = "im/token.json";
	private final static String SERVER_ENDPOINT = "im/server.json";
	private final static String TOPIC_ENDPOINT = "im/create_topic.json";
    private final static String REPORT_ENDPOINT = "im/report_device_id.json";
	private final static String TOPIC_UPDATE_ENDPOINT = "im/update_topic.json";
	private final static String SESSION_ENDPOINT = "im/create_session.json";
	private final static String ADD_CLIENTS_ENDPOINT = "im/add_clients.json";
	private final static String REMOVE_CLIENTS_ENDPOINT = "im/remove_clients.json";
	private final static String REMOVE_TOPIC_ENDPOINT = "im/remove_topic.json";
	private final static String BIND_SERVICE_ENDPOINT = "im/signed_bind_service.json";
	private final static String NOTICE_ENDPOINT = "im/notice.json";
	private final static String TOPIC_INFO_ENDPOINT = "im/topic_info.json";
	private final static String SESSION_INFO_ENDPOINT = "im/session_info.json";
	private final static String HISTORY_ENDPOINT = "im/history.json";
	private final static String FULL_HISTORY_ENDPOINT = "im/topics/history.json";
	private final static String CLIENT_STATUS_ENDPOINT = "im/client_status.json";
	private final static String TOPIC_LIST_ENDPOINT = "im/topic_list.json";
	private final static String PUSH_SETTINGS_ENDPOINT = "im/signed_push_settings.json";
	private final static String SYNC_HISTORY_ENDPOINT = "im/sync_history.json";
	private final static String LIVE_CREATE_SESSION_ENDPOINT = "lives/create.json";
	private final static String LIVE_TERMINATE_SESSION_ENDPOINT = "lives/terminate.json";
	private final static String LIVE_VALIDATE_SESSION_ENDPOINT = "lives/validate.json";
	private final static String ADD_BLACKLIST_ENDPOINT = "im/add_blacklist.json";
	private final static String REMOVE_BLACKLIST_ENDPOINT = "im/remove_blacklist.json";
	private final static String LIST_BLACKLIST_ENDPOINT = "im/blacklist.json";
	private final static int DEFAULT_KEEPALIVE = 60;
	
	private final static String SIG_SEC = Constants.ARROWNOCK_API_SECRET;
	
	private IMQTTAgent mqttAgent = null;
	public final static String PREF_IM_HOST = "imHost";
	public final static String PREF_IM_PORT = "imPort";
	public final static String PREF_IM_HOST_EXPIRATION = "imHostExpiration";
	public final static String PREF_IM_HOST_RETRYTIME = "imHostRetrytime";
	public final static String PREF_IM_API = "imAPI";
	public final static String PREF_IM_DS = "imDS";
	
	private String LOG_TAG = LOG_TAG_CLASS;
	private boolean forceClose = false;
	private boolean noDisconnectCallback = false;
	
	private List<IAnIMCallback> callbacks = null;
	private IAnDeskCallback deskCallback = null;
	private IAnGroupediaCallback groupediaCallback = null;
	private MQTTEvent mqttEvent = new MQTTEvent();
	private Context context = null;
	private boolean secureConnection = true; 
	private String appKey = null;
	private boolean isReconnect = false;
	private String clientId = null;
	private String clientIdForReport = null;
	private ISignalController.Callbacks signalControllerCallback = null;
	private static final Map<String, Boolean> liveSignalMap;
    static
    {
    	liveSignalMap = new HashMap<String, Boolean>();
    	liveSignalMap.put("31", Boolean.TRUE);
    	liveSignalMap.put("32", Boolean.TRUE);
    	liveSignalMap.put("33", Boolean.TRUE);
    	liveSignalMap.put("34", Boolean.TRUE);
    	liveSignalMap.put("35", Boolean.TRUE);
    }
	
	private final static HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();
	
	@Override
	public void setHostsUtil(Context androidContext, String api, String ds) {
		Editor editor = androidContext.getSharedPreferences(LOG_TAG_CLASS, Context.MODE_PRIVATE).edit();
		editor.putString(PREF_IM_API, api);
		editor.putString(PREF_IM_DS, ds);
		editor.commit();
	}
	
	@Override
	public void setCallbacks(List<IAnIMCallback> callbacks) {
		this.callbacks = callbacks;
	}
	
	@Override
	public void setDeskCallback(IAnDeskCallback callback) {
		this.deskCallback = callback;
	}
	
	@Override
    public void setGroupediaCallback(IAnGroupediaCallback callback) {
        this.groupediaCallback = callback;
    }
	
	@Override 
	public void setContext(Context context) {
		this.context = context;
	}
	
	@Override 
	public void setSecureConnection(boolean isSecure) {
		this.secureConnection = isSecure;
	}
	
	@Override 
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	
    private String getAPISecret() {
        String secret = getFromLocalStorage(context, PushService.PREF_API_SECRET);
        return "".equals(secret) ? SIG_SEC : secret;
    }
	
    private String getSignature(String url, List<NameValuePair> params) throws Exception {
        Collections.sort(params,new Comparator<NameValuePair>() {
            @Override
            public int compare(NameValuePair lhs, NameValuePair rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        
        String signatureString = "";
        for (NameValuePair param : params) {
            signatureString = signatureString + "&" + param.getName() + "=" + param.getValue();
        }
        signatureString = url + signatureString.substring(1);
        String HMAC_SHA1_ALGORITHM = "HmacSHA1";
        Key signingKey = new SecretKeySpec(getAPISecret().getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(signatureString.getBytes());
        return Base64.encodeToString(rawHmac, Base64.NO_WRAP);
    }
	
	@Override
	@Deprecated
	public void getClientIdAsync(final String appKey, final String userId) {
		SharedPreferences pref = context.getSharedPreferences(LOG_TAG_CLASS, Context.MODE_PRIVATE);
		String api = pref.getString(PREF_IM_API, "");
		String ds = pref.getString(PREF_IM_DS, "");

		this.LOG_TAG = LOG_TAG_CLASS + userId;
		saveToLocalStorage(context, PREF_IM_API, api);
		saveToLocalStorage(context, PREF_IM_DS, ds);
		Runnable getClientIdThread = new Runnable(){
			public void run() {
				try{
					String clientId = getClientIdUtil(appKey, userId);
					//Log.d(LOG_TAG, "clientId: " + clientId);
					if(callbacks != null) {
						AnIMGetClientIdCallbackData data = new AnIMGetClientIdCallbackData(false, null, clientId);
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.getClientId(data);
							}
						}
					}
				} catch(ArrownockException ex){
					if(callbacks != null) {
						AnIMGetClientIdCallbackData data = new AnIMGetClientIdCallbackData(true, ex, null);
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.getClientId(data);
							}
						}
					}
				}
			}
		};
		Thread thread = new Thread(getClientIdThread);
		thread.start();
	}
	
	@Override
	public void getClientIdAsync(final String appKey, final String userId, final IAnIMGetClientIdCallback callback){
        SharedPreferences pref = context.getSharedPreferences(LOG_TAG_CLASS, Context.MODE_PRIVATE);
        String api = pref.getString(PREF_IM_API, "");
        String ds = pref.getString(PREF_IM_DS, "");

        this.LOG_TAG = LOG_TAG_CLASS + userId;
        saveToLocalStorage(context, PREF_IM_API, api);
        saveToLocalStorage(context, PREF_IM_DS, ds);
        Runnable getClientIdThread = new Runnable(){
            public void run() {
                try{
                    String clientId = getClientIdUtil(appKey, userId);
                    //Log.d(LOG_TAG, "clientId: " + clientId);
                    if(callback != null) {
                        callback.onSuccess(clientId);
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(getClientIdThread);
        thread.start();
	}
	
	String getClientIdUtil(String appKey, String userId) throws ArrownockException{
		//String clientId = getFromLocalStorage(context, PREF_CLIENT_ID);
		if (clientId == null) {
			HttpURLConnection urlConnection = null;
			try {
				if (secureConnection) {
					URL url = new URL(Constants.HTTPS + this.getDSHost(context) + "/" + DISPATCH_VERSION + "/" + TOKEN_ENDPOINT + "?key=" + appKey + "&id=" +  URLEncoder.encode(userId, "UTF-8") + "&type=" + "android");
					urlConnection = (HttpsURLConnection) url.openConnection();
					((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
					((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
				} else {
					URL url = new URL(Constants.HTTP + this.getDSHost(context) + "/" + DISPATCH_VERSION + "/" + TOKEN_ENDPOINT + "?key=" + appKey + "&id=" + URLEncoder.encode(userId, "UTF-8") + "&type=" + "android");
					urlConnection = (HttpURLConnection) url.openConnection();
				}
				urlConnection.connect();
				
				int statusCode = urlConnection.getResponseCode();
				if(statusCode==200){	// Service OK
					InputStream is = new BufferedInputStream(urlConnection.getInputStream());
					String res = convertStreamToString(is);
					JSONObject json = new JSONObject(res);
					clientId = json.getString("token");
				}else {
					InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					String res = convertStreamToString(es);
					JSONObject json = new JSONObject(res);
					throw new ArrownockException("Failed to get Client Id: " + json, ArrownockException.IM_FAILED_GET_CLIENT_ID);
				}
			} catch (Exception ex) {
				throw new ArrownockException(ex.getMessage(), ex, ArrownockException.IM_FAILED_GET_CLIENT_ID);
			} finally {
				if (urlConnection != null) {
					urlConnection.disconnect();
				}  
			}
//			if (!clientId.equals("")) {
//				saveToLocalStorage(context, PREF_CLIENT_ID, clientId);
//			}
		}
		return clientId;
	}
	
	@Override
	public void connectAsync(final String appKey, final String clientId) {
		// Two tasks here:
		// 1. Get server information
		// 2. connect to the server
		this.clientId = clientId;
		clientIdForReport = clientId;
		Runnable connectThread = new Runnable(){
			public void run() {
				try{
					KeyValuePair<String, Integer> server = getServerUtil(clientId);
					if (server != null && server.getKey() != null && !"".equals(server.getKey())) {
						//Log.d(LOG_TAG, "IM server: " + server);
						String host = server.getKey();
						int port = server.getValue();
						String initTopic = "ANIM/" + clientId + '/' + appKey;
						String initSenderName = clientId;
						connectUtil(host, port, initTopic, initSenderName, getDeviceId(), appKey);
					} else {
						if(callbacks != null) {
							AnIMStatusUpdateCallbackData data = new AnIMStatusUpdateCallbackData(getCurrentStatus(), new ArrownockException("Failed to get server info", ArrownockException.IM_SERVICE_UNAVAILABLE));
							for(IAnIMCallback c : callbacks) {
								if(c != null) {
									c.statusUpdate(data);
								}
							}
						}
					}
				} catch(ArrownockException ex){
					AnIMStatusUpdateCallbackData data = new AnIMStatusUpdateCallbackData(getCurrentStatus(), ex);
					if(callbacks != null) {
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.statusUpdate(data);
							}
						}
					}
				}
			}
		};
		Thread thread = new Thread(connectThread);
		thread.start();
	}
	
	private void reportDeviceIdToIM(final String clientId) {
	    Runnable reportThread = new Runnable(){
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    if (secureConnection) {
                        URL url = new URL(Constants.HTTPS + getAPIHost(context) + "/" + API_VERSION + "/" + REPORT_ENDPOINT + "?key=" + appKey.trim());
                        urlConnection = (HttpsURLConnection) url.openConnection();
                        ((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
                        ((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
                    } else {
                        URL url = new URL(Constants.HTTP + getAPIHost(context) + "/" + API_VERSION + "/" + REPORT_ENDPOINT + "?key=" + appKey.trim());
                        urlConnection = (HttpURLConnection) url.openConnection();
                    }
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    
                    try {
                        String device_id = DeviceManager.getInstance(context, appKey).getDeviceId();
                        if ("".equals(device_id)){
                            return;
                        }
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                        nameValuePairs.add(new BasicNameValuePair("device_id", device_id));
                        nameValuePairs.add(new BasicNameValuePair("client_id", clientId));
                        
                        OutputStream out = urlConnection.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                        writer.write(getQuery(nameValuePairs));
                        writer.close();
                        out.close();
                        
                        int statusCode = urlConnection.getResponseCode();
                        if (statusCode == 200) {
                        } else {
                        }
                    } catch (IOException e) {
                        
                    } 
                } catch (Exception e) {
                   
                }   
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }       
                }
            }
        };
	    
	    Thread thread = new Thread(reportThread);
        thread.start();
    }
	
	KeyValuePair<String, Integer> getServerUtil(String clientId) throws ArrownockException {
		String imHost = getFromLocalStorage(context, PREF_IM_HOST);
		SharedPreferences pref = context.getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE);
		int imPort = pref.getInt(PREF_IM_PORT, 1341);
		long imHostExpiration = pref.getLong(PREF_IM_HOST_EXPIRATION, 0);
		long currentTimeMillis = System.currentTimeMillis();
		boolean hostExpired = (currentTimeMillis > imHostExpiration ? true : false);
		if(hostExpired){
			imHost = "";
			// Remove stored push host
			Editor editor = context.getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE).edit();
			editor.remove(PREF_IM_HOST);
			editor.remove(PREF_IM_PORT);
			editor.remove(PREF_IM_HOST_EXPIRATION);
			editor.commit();
		}
		
		if("".equals(imHost) || hostExpired) {
			HttpURLConnection urlConnection = null;
			try {
				if (secureConnection) {
					URL url = new URL(Constants.HTTPS + this.getDSHost(context) + "/" + DISPATCH_VERSION + "/" + SERVER_ENDPOINT + "?token=" + clientId);
					urlConnection = (HttpsURLConnection) url.openConnection();
					((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
					((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
				} else {
					URL url = new URL(Constants.HTTP + this.getDSHost(context) + "/" + DISPATCH_VERSION + "/" + SERVER_ENDPOINT + "?token=" + clientId);
					urlConnection = (HttpURLConnection) url.openConnection();
				}
				urlConnection.connect();
				
				int statusCode = urlConnection.getResponseCode();
				if(statusCode==200){	// Service OK
					InputStream is = new BufferedInputStream(urlConnection.getInputStream());
					String res = convertStreamToString(is);
					JSONObject json = new JSONObject(res);
					imHost = json.getString("host");
					if (secureConnection) {
						imPort = json.getInt("secure_port");
					} else {
						imPort = json.getInt("port");
					}
					imHostExpiration = json.getLong("expiration");
				} else {
					InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					String res = convertStreamToString(es);
					JSONObject json = new JSONObject(res);
					throw new ArrownockException("Failed to get IM server: " + json, ArrownockException.IM_SERVICE_UNAVAILABLE);
				}
			} catch (Exception ex) {
				throw new ArrownockException(ex, ArrownockException.IM_SERVICE_UNAVAILABLE);
			} finally {
				if (urlConnection != null) {
					urlConnection.disconnect();
				}  
			}
			
			if (imHost != null && !"".equals(imHost)) {
				Editor editor = context.getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE).edit();
				editor.putString(PREF_IM_HOST, imHost);
				editor.putInt(PREF_IM_PORT, imPort);
				editor.putLong(PREF_IM_HOST_EXPIRATION, imHostExpiration);
				editor.commit();
			}
		}
		return new KeyValuePair<String, Integer>(imHost, imPort);
	}
	
	void connectUtil(String host, int port, String initTopic, String initSenderName, String deviceId, String appKey) throws ArrownockException {
		synchronized (LOG_TAG) {
			if (!isNetworkAvailable(context)) {
				if(callbacks != null) {
					AnIMStatusUpdateCallbackData data = new AnIMStatusUpdateCallbackData(getCurrentStatus(), new ArrownockException("Network is not available", ArrownockException.IM_FAILED_CONNECT));
					for(IAnIMCallback c : callbacks) {
						if(c != null) {
							c.statusUpdate(data);
						}
					}
				}
				return;
			}
			if (mqttAgent != null) {
				if(mqttAgent.getStatus().equals(MQTTConnectionStatus.Connected)) {
				    if (callbacks != null) {
				    	AnIMStatusUpdateCallbackData data = new AnIMStatusUpdateCallbackData(AnIMStatus.ONLINE, null);
				    	for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.statusUpdate(data);
							}
						}
					}
					return;
				}
				if(mqttAgent.getStatus().equals(MQTTConnectionStatus.Connecting)) {
					return;
				}
				noDisconnectCallback = true;
				mqttAgent.disconnect();
				mqttAgent = null;
			}
			
			if (secureConnection) {
				mqttAgent = new PahoAgent(host, port, true, getServerCert(), getClientCert(), getClientKey(), initSenderName, initTopic, mqttEvent, "BKS", DEFAULT_KEEPALIVE, deviceId, appKey);
				mqttAgent.setAutoReconnect(false);
			} else {
				mqttAgent = new PahoAgent(host, port, initSenderName, initTopic, mqttEvent, DEFAULT_KEEPALIVE, deviceId, appKey);
				mqttAgent.setAutoReconnect(false);
			}
		}
	}
	
	@Override
	public void disconnectAsync(final String appKey) {
		if (mqttAgent != null && clientId != null) {
//			String clientId = getFromLocalStorage(context, PREF_CLIENT_ID);
			String initTopic = "ANIM/" + clientId + '/' + appKey;
			mqttAgent.clearSession(initTopic);
			// Run disconnect in new thread
			Runnable disconnectThread = new Runnable(){
				public void run() {
					mqttAgent.disconnect();
					mqttAgent = null;
				}
			};
			Thread thread = new Thread(disconnectThread);
			thread.start();
		}
	}
	
	@Override
	public void sendMessageToSessionAsync(final String appKey, final String msgId, final String topicName, final String payload) {
		Runnable sendMessageToSessionThread = new Runnable(){
			public void run() {
				try {
					JSONObject payloadJson = new JSONObject(payload);
					Set<String> parties = new HashSet<String>();
					JSONArray jsonArray = payloadJson.optJSONArray("recipients");
					if (jsonArray != null) {
						parties = new HashSet<String>();
						for (int i=0; i<jsonArray.length(); i++) {
							parties.add(jsonArray.getString(i));
						}
					}
//					String clientId = getFromLocalStorage(context, PREF_CLIENT_ID);
					parties.add(clientId);
					String sessionId = calculateSessionId(parties);
					if (sessionId != null) {
						// check whether the session information is already in local storage
						String partyStr = getFromLocalStorage(context, sessionId);
						if ("".equals(partyStr)) {
							// 1. not in local, check it on server
							partyStr = setJoin(parties, ",");
							try {
								createSessionInfo(appKey, sessionId, partyStr);
								// 1.1 it's already on server or successfully created on sever
								saveToLocalStorage(context, sessionId, partyStr);
								payloadJson.remove("recipients");
								payloadJson.put("session_key", sessionId);
								sendMessageAsync(msgId, topicName, payloadJson.toString());
							} catch (ArrownockException ex) {
							    if (callbacks != null) {
        							AnIMMessageSentCallbackData data = new AnIMMessageSentCallbackData(true, ex, msgId, -1);
        							for(IAnIMCallback c : callbacks) {
        								if(c != null) {
        									c.messageSent(data);
        								}
        							}
							    }
							}
						} else {
							// 2. find from local, send message using sessionId
							payloadJson.remove("recipients");
							payloadJson.put("session_key", sessionId);
							sendMessageAsync(msgId, topicName, payloadJson.toString());
						}
					} else {
					    if (callbacks != null) {
        					AnIMMessageSentCallbackData data = new AnIMMessageSentCallbackData(true, new ArrownockException("Failed to send message, error when calculating session info.", ArrownockException.IM_FAILED_PUBLISH), msgId, -1);
        					for(IAnIMCallback c : callbacks) {
								if(c != null) {
									c.messageSent(data);
								}
							}
					    }
					}
					
				} catch (JSONException e) {
				    if (callbacks != null) {
        				AnIMMessageSentCallbackData data = new AnIMMessageSentCallbackData(true, new ArrownockException("Failed to send message.", e, ArrownockException.IM_FAILED_PUBLISH), msgId, -1);
        				for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.messageSent(data);
							}
						}
				    }
				}
			}
		};
		Thread thread = new Thread(sendMessageToSessionThread);
		thread.start();
	}
	
	private void createSessionInfo(String appKey, String sessionId, String partyStr) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			if (secureConnection) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + SESSION_ENDPOINT + "?key=" + appKey.trim());
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + SESSION_ENDPOINT + "?key=" + appKey.trim());
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			
		    try {
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			    nameValuePairs.add(new BasicNameValuePair("id", sessionId));
			    nameValuePairs.add(new BasicNameValuePair("client", partyStr));
			    
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
						throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_CREATE_SESSION);
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_CREATE_SESSION);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_CREATE_SESSION);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_CREATE_SESSION);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}
	}
	
	@Override
	public void sendMessageAsync(final String msgId, final String topicName, final String payload) {
		sendMessageAsync(msgId, topicName, payload, 2);
	}
	
	@Override
	public void sendMessageAsync(final String msgId, final String topicName, final String payload, final int QoS) {
		if (getCurrentStatus() == AnIMStatus.OFFLINE) {
			Runnable sendMessageThread = new Runnable() {
				public void run() {
				    ArrownockException exception = new ArrownockException("Failed to send message, client is offline.", ArrownockException.IM_FAILED_PUBLISH);
                    if(msgId.startsWith("D")) {
                        if(deskCallback != null) {
                            try {
                                deskCallback.messageSent(msgId, -1, exception);
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if (callbacks != null) {
                            AnIMMessageSentCallbackData data = new AnIMMessageSentCallbackData(true, exception, msgId, -1);
                            for(IAnIMCallback c : callbacks) {
                                if(c != null) {
                                    c.messageSent(data);
                                }
                            }
                        }
                    }
				}
			};
			Thread thread = new Thread(sendMessageThread);
			thread.start();
		} else if (getCurrentStatus() == AnIMStatus.ONLINE) {
			Runnable sendMessageThread = new Runnable() {
				public void run() {
					boolean retain = true;
					mqttAgent.publish(msgId, topicName, payload, retain, QoS);
				}
			};
			Thread thread = new Thread(sendMessageThread);
			thread.start();
		}
	}
	
	@Deprecated
	public void createTopicAsync(final String appKey, final String topicName, final String clientsStr, final String owner) {
        Runnable createTopicThread = new Runnable(){
            public void run() {
                try{
                    String[] topic = createTopicUtil(appKey, topicName, clientsStr, owner, null, false, null);
                    if(callbacks != null) {
                        AnIMCreateTopicCallbackData data = new AnIMCreateTopicCallbackData(false, null, topic[0]);
                        for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.createTopic(data);
							}
						}
                    }
                } catch(ArrownockException ex){
                    if(callbacks != null) {
                        AnIMCreateTopicCallbackData data = new AnIMCreateTopicCallbackData(true, ex, null);
                        for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.createTopic(data);
							}
						}
                    }
                }
            }
        };
        Thread thread = new Thread(createTopicThread);
        thread.start();
    }
	
	public void createTopicAsync(final String appKey, final String topicName, final String clientsStr, final String owner, final Map<String, String> customData,
	        final boolean isNeedNotice, final String currentClientId, final IAnIMTopicCallback callback) {
        Runnable createTopicThread = new Runnable(){
            public void run() {
                try {
                    String topic[] = createTopicUtil(appKey, topicName, clientsStr, owner, customData, isNeedNotice, currentClientId);
                    if(callback != null) {
                        callback.onSuccess(topic[0], Long.valueOf(topic[1]), Long.valueOf(topic[2]));
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(createTopicThread);
        thread.start();
    }
	
	String[] createTopicUtil(String appKey, String topicName, String clientsStr, String owner, Map<String, String> customData, boolean isNeedNotice, 
	        String currentClientId) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			if (secureConnection) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + TOPIC_ENDPOINT);
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + TOPIC_ENDPOINT);
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			
		    try {
		    	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			    nameValuePairs.add(new BasicNameValuePair("name", topicName));
			    nameValuePairs.add(new BasicNameValuePair("client", clientsStr));
			    if (owner != null) {
			        nameValuePairs.add(new BasicNameValuePair("owner", owner));
			    }
			    if (isNeedNotice) {
			        nameValuePairs.add(new BasicNameValuePair("is_need_notice", "true"));
			        if (currentClientId != null && currentClientId.trim().length() != 0) {
			            nameValuePairs.add(new BasicNameValuePair("current_client_id", currentClientId));
			        }
			        String msg_id = generateMsgID();
			        nameValuePairs.add(new BasicNameValuePair("msg_id", msg_id));
			    }
			    if (customData != null) {
                    JSONObject customDataJson = new JSONObject();
                    for (Map.Entry<String, String> entry : customData.entrySet()) {  
                        customDataJson.put(entry.getKey(), entry.getValue());
                    }
                    nameValuePairs.add(new BasicNameValuePair("customData", customDataJson.toString()));
                }
			    
			    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
                String date = format.format(new Date());
                nameValuePairs.add(new BasicNameValuePair("date", date));
                nameValuePairs.add(new BasicNameValuePair("key", appKey));
                String signature = getSignature("/" + API_VERSION + "/" + TOPIC_ENDPOINT, nameValuePairs);
                nameValuePairs.add(new BasicNameValuePair("signature", signature));
                
			    
			    OutputStream out = urlConnection.getOutputStream();
			    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			    writer.write(getQuery(nameValuePairs));
			    writer.close();
			    out.close();
			    
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode == 200) {
				        String results[] = new String[3];
					    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					    String s = convertStreamToString(in);
						JSONObject json = new JSONObject(s);
						//JSONObject meta = json.getJSONObject("meta");
						JSONObject data = json.getJSONObject("response");
						if(data != null && data.getJSONObject("topic") != null) {
							JSONObject sub = data.getJSONObject("topic");
							if(sub != null && sub.getString("topic_id") != null) {
							    results[0] = sub.getString("topic_id");
							} else {
								throw new ArrownockException("Unable to create topic. Failed to acquire topic_id.", ArrownockException.IM_FAILED_CREATE_TOPIC);
							}
							if(sub != null && sub.has("created_at")) {
                                results[1] = sub.getLong("created_at") + "";
                             } else {
                                 throw new ArrownockException("Unable to create topic. Failed to acquire created_at.", ArrownockException.IM_FAILED_CREATE_TOPIC);
                             }
							if(sub != null && sub.has("updated_at")) {
                                results[2] = sub.getLong("updated_at") + "";
                             } else {
                                 throw new ArrownockException("Unable to create topic. Failed to acquire updated_at.", ArrownockException.IM_FAILED_CREATE_TOPIC);
                             }
							return results;
						} else {
							throw new ArrownockException("Unable to create topic. Failed to acquire topic.", ArrownockException.IM_FAILED_CREATE_TOPIC);
							}
				    } else {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_CREATE_TOPIC);
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_CREATE_TOPIC);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_CREATE_TOPIC);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_CREATE_TOPIC);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}
	}
	
	@Deprecated
	public void updateTopicAsync(final String appKey, final String topicId, final String topicName, final String owner) {
        Runnable createTopicThread = new Runnable(){
            public void run() {
                try{
                    updateTopicUtil(appKey, topicId, topicName, owner, null, false, null);
                    if(callbacks != null) {
                        AnIMUpdateTopicCallbackData data = new AnIMUpdateTopicCallbackData(false, null, topicId);
                        for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.updateTopic(data);
							}
						}
                    }
                } catch(ArrownockException ex){
                    if(callbacks != null) {
                        AnIMUpdateTopicCallbackData data = new AnIMUpdateTopicCallbackData(true, ex, null);
                        for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.updateTopic(data);
							}
						}
                    }
                }
            }
        };
        Thread thread = new Thread(createTopicThread);
        thread.start();
    }
	
	public void updateTopicAsync(final String appKey, final String topicId, final String topicName, final String owner, final Map<String, String> customData, 
	        final boolean isNeedNotice, final String currentClientId, final IAnIMTopicCallback callback) {
        Runnable createTopicThread = new Runnable(){
            public void run() {
                try{
                    long[] time = updateTopicUtil(appKey, topicId, topicName, owner, customData, isNeedNotice, currentClientId);
                    if(callback != null) {
                        callback.onSuccess(topicId, time[0], time[1]);
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(createTopicThread);
        thread.start();
    }
	
	long[] updateTopicUtil(String appKey, String topicId, String topicName, String owner, Map<String, String> customData, boolean isNeedNotice, 
            String currentClientId) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			if (secureConnection) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + TOPIC_UPDATE_ENDPOINT);
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + TOPIC_UPDATE_ENDPOINT);
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			
		    try {
        		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        		    	nameValuePairs.add(new BasicNameValuePair("id", topicId));
        		    	if(topicName != null) {
        		    		nameValuePairs.add(new BasicNameValuePair("name", topicName));
        		    	}
			    if(owner != null) {
			        nameValuePairs.add(new BasicNameValuePair("owner", owner));
			    }
			    if (customData != null) {
                    JSONObject customDataJson = new JSONObject();
                    for (Map.Entry<String, String> entry : customData.entrySet()) {  
                        customDataJson.put(entry.getKey(), entry.getValue());
                    }
                    nameValuePairs.add(new BasicNameValuePair("customData", customDataJson.toString()));
                }
			    if (isNeedNotice) {
                    nameValuePairs.add(new BasicNameValuePair("is_need_notice", "true"));
                    if (currentClientId != null && currentClientId.trim().length() != 0) {
                        nameValuePairs.add(new BasicNameValuePair("current_client_id", currentClientId));
                    }
                    String msg_id = generateMsgID();
                    nameValuePairs.add(new BasicNameValuePair("msg_id", msg_id));
                }
			    
			    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
                String date = format.format(new Date());
                nameValuePairs.add(new BasicNameValuePair("date", date));
                nameValuePairs.add(new BasicNameValuePair("key", appKey));
                String signature = getSignature("/" + API_VERSION + "/" + TOPIC_UPDATE_ENDPOINT, nameValuePairs);
                nameValuePairs.add(new BasicNameValuePair("signature", signature));
			    
			    OutputStream out = urlConnection.getOutputStream();
			    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			    writer.write(getQuery(nameValuePairs));
			    writer.close();
			    out.close();
			    
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode == 200) {
				        long[] results = new long[2];
					    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					    String s = convertStreamToString(in);
						JSONObject json = new JSONObject(s);
						//JSONObject meta = json.getJSONObject("meta");
						JSONObject data = json.getJSONObject("response");
						if(data != null && data.getJSONObject("topic") != null) {
							JSONObject sub = data.getJSONObject("topic");
							if(sub != null && sub.has("created_at")) {
                                results[0] = sub.getLong("created_at");
                             } else {
                                 throw new ArrownockException("Unable to update topic. Failed to acquire created_at.", ArrownockException.IM_FAILED_UPDATE_TOPIC);
                             }
                            if(sub != null && sub.has("updated_at")) {
                                results[1] = sub.getLong("updated_at");
                             } else {
                                 throw new ArrownockException("Unable to update topic. Failed to acquire updated_at.", ArrownockException.IM_FAILED_UPDATE_TOPIC);
                             }
                            return results;
						} else {
							throw new ArrownockException("Unable to update topic. Failed to acquire topic.", ArrownockException.IM_FAILED_UPDATE_TOPIC);
							}
				    } else {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_UPDATE_TOPIC);
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_UPDATE_TOPIC);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_UPDATE_TOPIC);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_UPDATE_TOPIC);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}
	}
	
	@Deprecated
	public void addClientsAsync(final String appKey, final String topic, final String clientsStr) {
        Runnable addClientsThread = new Runnable(){
            public void run() {
                try{
                    addClientsUtil(appKey, topic, clientsStr, false, null);
                    if(callbacks != null) {
                        AnIMAddClientsCallbackData data = new AnIMAddClientsCallbackData(false, null);
                        for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.addClientsToTopic(data);
							}
						}
                    }
                } catch(ArrownockException ex){
                    if(callbacks != null) {
                        AnIMAddClientsCallbackData data = new AnIMAddClientsCallbackData(true, ex);
                        for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.addClientsToTopic(data);
							}
						}
                    }
                }
            }
        };
        Thread thread = new Thread(addClientsThread);
        thread.start(); 
    }
	
	public void addClientsAsync(final String appKey, final String topic, final String clientsStr, final boolean isNeedNotice, 
	        final String currentClientId, final IAnIMTopicCallback callback) {
        Runnable addClientsThread = new Runnable(){
            public void run() {
                try{
                    long[] time = addClientsUtil(appKey, topic, clientsStr, isNeedNotice, currentClientId);
                    if(callback != null) {
                        callback.onSuccess(topic, time[0], time[1]);
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(addClientsThread);
        thread.start(); 
    }
	
	long[] addClientsUtil(String appKey, String topic, String clientsStr, boolean isNeedNotice, String currentClientId) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			if (secureConnection) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + ADD_CLIENTS_ENDPOINT);
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + ADD_CLIENTS_ENDPOINT);
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			
		    try {
		    	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			    nameValuePairs.add(new BasicNameValuePair("id", topic));
			    nameValuePairs.add(new BasicNameValuePair("client", clientsStr));
			    if (isNeedNotice) {
                    nameValuePairs.add(new BasicNameValuePair("is_need_notice", "true"));
                    if (currentClientId != null && currentClientId.trim().length() != 0) {
                        nameValuePairs.add(new BasicNameValuePair("current_client_id", currentClientId));
                    }
                    String msg_id = generateMsgID();
                    nameValuePairs.add(new BasicNameValuePair("msg_id", msg_id));
                }
			    
			    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
                String date = format.format(new Date());
                nameValuePairs.add(new BasicNameValuePair("date", date));
                nameValuePairs.add(new BasicNameValuePair("key", appKey));
                String signature = getSignature("/" + API_VERSION + "/" + ADD_CLIENTS_ENDPOINT, nameValuePairs);
                nameValuePairs.add(new BasicNameValuePair("signature", signature));
			    
			    OutputStream out = urlConnection.getOutputStream();
			    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			    writer.write(getQuery(nameValuePairs));
			    writer.close();
			    out.close();
			    
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode == 200) {
				        long[] results = new long[2];
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        String s = convertStreamToString(in);
                        JSONObject json = new JSONObject(s);
                        // JSONObject meta = json.getJSONObject("meta");
                        JSONObject data = json.getJSONObject("response");
                        if (data != null && data.getJSONObject("topic") != null) {
                            JSONObject sub = data.getJSONObject("topic");
                            if (sub != null && sub.has("created_at")) {
                                results[0] = sub.getLong("created_at");
                            } else {
                                throw new ArrownockException("Unable to addClientsToTopic. Failed to acquire created_at.",
                                        ArrownockException.IM_FAILED_ADD_CLIENTS);
                            }
                            if (sub != null && sub.has("updated_at")) {
                                results[1] = sub.getLong("updated_at");
                            } else {
                                throw new ArrownockException("Unable to addClientsToTopic. Failed to acquire updated_at.",
                                        ArrownockException.IM_FAILED_ADD_CLIENTS);
                            }
                            return results;
                        } else {
                            throw new ArrownockException("Unable to addClientsToTopic. Failed to acquire topic.",
                                    ArrownockException.IM_FAILED_ADD_CLIENTS);
                        }
				    } else {
				        InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
                        String s = convertStreamToString(es);
                        JSONObject json = new JSONObject(s);
                        JSONObject meta = json.getJSONObject("meta");
                        throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_ADD_CLIENTS);
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_ADD_CLIENTS);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_ADD_CLIENTS);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_ADD_CLIENTS);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}
	}
	
	@Deprecated
	public void removeClientsAsync(final String appKey, final String topic, final String clientsStr) {
		Runnable removeClientsThread = new Runnable(){
			public void run() {
				try{
					removeClientsUtil(appKey, topic, clientsStr, false, null);
					if(callbacks != null) {
						AnIMRemoveClientsCallbackData data = new AnIMRemoveClientsCallbackData(false, null);
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.removeClientsFromTopic(data);
							}
						}
					}
				} catch(ArrownockException ex){
					if(callbacks != null) {
						AnIMRemoveClientsCallbackData data = new AnIMRemoveClientsCallbackData(true, ex);
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.removeClientsFromTopic(data);
							}
						}
					}
				}
			}
		};
		Thread thread = new Thread(removeClientsThread);
		thread.start();	
	}
	
	public void removeClientsAsync(final String appKey, final String topic, final String clientsStr, final boolean isNeedNotice, 
            final String currentClientId, final IAnIMTopicCallback callback) {
        Runnable removeClientsThread = new Runnable(){
            public void run() {
                try{
                    long[] time = removeClientsUtil(appKey, topic, clientsStr, isNeedNotice, currentClientId);
                    if(callback != null) {
                        callback.onSuccess(topic, time[0], time[1]);
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(removeClientsThread);
        thread.start(); 
    }
	
	long[] removeClientsUtil(String appKey, String topic, String clientsStr, boolean isNeedNotice, String currentClientId) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			if (secureConnection) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + REMOVE_CLIENTS_ENDPOINT);
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + REMOVE_CLIENTS_ENDPOINT);
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			
		    try {
		    	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			    nameValuePairs.add(new BasicNameValuePair("id", topic));
			    nameValuePairs.add(new BasicNameValuePair("client", clientsStr));
			    if (isNeedNotice) {
                    nameValuePairs.add(new BasicNameValuePair("is_need_notice", "true"));
                    if (currentClientId != null && currentClientId.trim().length() != 0) {
                        nameValuePairs.add(new BasicNameValuePair("current_client_id", currentClientId));
                    }
                    String msg_id = generateMsgID();
                    nameValuePairs.add(new BasicNameValuePair("msg_id", msg_id));
                }
			    
			    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
                String date = format.format(new Date());
                nameValuePairs.add(new BasicNameValuePair("date", date));
                nameValuePairs.add(new BasicNameValuePair("key", appKey));
                String signature = getSignature("/" + API_VERSION + "/" + REMOVE_CLIENTS_ENDPOINT, nameValuePairs);
                nameValuePairs.add(new BasicNameValuePair("signature", signature));
			    
			    OutputStream out = urlConnection.getOutputStream();
			    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			    writer.write(getQuery(nameValuePairs));
			    writer.close();
			    out.close();
			    
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode == 200) {
				        long[] results = new long[2];
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        String s = convertStreamToString(in);
                        JSONObject json = new JSONObject(s);
                        // JSONObject meta = json.getJSONObject("meta");
                        JSONObject data = json.getJSONObject("response");
                        if (data != null && data.getJSONObject("topic") != null) {
                            JSONObject sub = data.getJSONObject("topic");
                            if (sub != null && sub.has("created_at")) {
                                results[0] = sub.getLong("created_at");
                            } else {
                                throw new ArrownockException("Unable to removeClientsFromTopic. Failed to acquire created_at.",
                                        ArrownockException.IM_FAILED_REMOVE_CLIENTS);
                            }
                            if (sub != null && sub.has("updated_at")) {
                                results[1] = sub.getLong("updated_at");
                            } else {
                                throw new ArrownockException("Unable to removeClientsFromTopic. Failed to acquire updated_at.",
                                        ArrownockException.IM_FAILED_REMOVE_CLIENTS);
                            }
                            return results;
                        } else {
                            throw new ArrownockException("Unable to removeClientsFromTopic. Failed to acquire topic.",
                                    ArrownockException.IM_FAILED_REMOVE_CLIENTS);
                        }
				    } else {
				        InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
                        String s = convertStreamToString(es);
                        JSONObject json = new JSONObject(s);
                        JSONObject meta = json.getJSONObject("meta");
                        throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_REMOVE_CLIENTS);
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_REMOVE_CLIENTS);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_REMOVE_CLIENTS);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_REMOVE_CLIENTS);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}
	}
	
	public void bindAnPushAsync(final String appKey, final String clientId, final String anid, final String anPushAppKey, final String deviceTypeStr) {
		Runnable bindAnPushThread = new Runnable(){
			public void run() {
				try{
					bindServiceUtil(appKey, clientId, anid, anPushAppKey, deviceTypeStr, true);
					if(callbacks != null) {
						AnIMBindAnPushServiceCallbackData data = new AnIMBindAnPushServiceCallbackData(false, null);
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.bindAnPushService(data);
							}
						}
					}
				} catch(ArrownockException ex){
					if(callbacks != null) {
						AnIMBindAnPushServiceCallbackData data = new AnIMBindAnPushServiceCallbackData(true, ex);
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.bindAnPushService(data);
							}
						}
					}
				}
			}
		};
		Thread thread = new Thread(bindAnPushThread);
		thread.start();	
	}
	
	public void bindAnPushAsync(final String appKey, final String clientId, final String anid, final String anPushAppKey, final String deviceTypeStr, final IAnIMPushBindingCallback callback) {
        Runnable bindAnPushThread = new Runnable(){
            public void run() {
                try{
                    bindServiceUtil(appKey, clientId, anid, anPushAppKey, deviceTypeStr, true);
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
        Thread thread = new Thread(bindAnPushThread);
        thread.start(); 
    }
	
	public void unbindAnPushAsync(final String appKey, final String clientId) {
		Runnable unbindAnPushThread = new Runnable(){
			public void run() {
				try{
					bindServiceUtil(appKey, clientId, null, null, null, false);
					if(callbacks != null) {
						AnIMUnbindAnPushServiceCallbackData data = new AnIMUnbindAnPushServiceCallbackData(false, null);
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.unbindAnPushService(data);
							}
						}
					}
				} catch(ArrownockException ex){
					if(callbacks != null) {
						AnIMUnbindAnPushServiceCallbackData data = new AnIMUnbindAnPushServiceCallbackData(true, ex);
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.unbindAnPushService(data);
							}
						}
					}
				}
			}
		};
		Thread thread = new Thread(unbindAnPushThread);
		thread.start();	
	}
	
	public void unbindAnPushAsync(final String appKey, final String clientId, final IAnIMPushBindingCallback callback) {
        Runnable unbindAnPushThread = new Runnable(){
            public void run() {
                try{
                    bindServiceUtil(appKey, clientId, null, null, null, false);
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
        Thread thread = new Thread(unbindAnPushThread);
        thread.start(); 
    }
	
	void bindServiceUtil(String appKey, String clientId, String anid, String anPushAppKey, String deviceTypeStr, boolean bind) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			if (secureConnection) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + BIND_SERVICE_ENDPOINT + "?key=" + appKey.trim());
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + BIND_SERVICE_ENDPOINT + "?key=" + appKey.trim());
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			
		    try {
		    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
		    	String date = format.format(new Date());
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			    nameValuePairs.add(new BasicNameValuePair("service", "anpush"));
			    nameValuePairs.add(new BasicNameValuePair("date", date));
			    nameValuePairs.add(new BasicNameValuePair("client", clientId));
			    nameValuePairs.add(new BasicNameValuePair("bind", String.valueOf(bind)));
			    String signatureString = null;
			    if (bind) {
				    nameValuePairs.add(new BasicNameValuePair("service_id", anid));
				    nameValuePairs.add(new BasicNameValuePair("appkey", anPushAppKey));
				    nameValuePairs.add(new BasicNameValuePair("device_type", deviceTypeStr));
				    signatureString = "/" + API_VERSION + "/" + BIND_SERVICE_ENDPOINT + "appkey=" + anPushAppKey + "&bind=true" + "&client=" + clientId + "&date=" + date + "&device_type=" + deviceTypeStr + "&key=" + appKey + "&service=anpush" + "&service_id=" + anid;
			    } else {
				    signatureString = "/" + API_VERSION + "/" + BIND_SERVICE_ENDPOINT + "bind=false" + "&client=" + clientId + "&date=" + date + "&key=" + appKey + "&service=anpush";
			    }
			    
			    String HMAC_SHA1_ALGORITHM = "HmacSHA1";
			    String signature = null;
			    
			    try {         
			    	Key signingKey = new SecretKeySpec(SIG_SEC.getBytes(), HMAC_SHA1_ALGORITHM);
			    	Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			    	mac.init(signingKey);
			    	byte[] rawHmac = mac.doFinal(signatureString.getBytes());
			    	signature = Base64.encodeToString(rawHmac, Base64.NO_WRAP);
			    	nameValuePairs.add(new BasicNameValuePair("signature", signature));
			    } catch (Exception e) {
			    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_BIND_SERVICE);
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
						throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_BIND_SERVICE);
				    } else {
				    	InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					    String s = convertStreamToString(in);
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_BIND_SERVICE);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_BIND_SERVICE);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_BIND_SERVICE);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}
	}
	
	public void sendNoticeAsync(final String appKey, final String clientId, final String notice, final String clientsStr, final String topic, 
			final String customDataStr, final String msgId, final boolean receiveACK) {
		Runnable sendNoticeThread = new Runnable(){
			public void run() {
				try{
					long timestamp = sendNoticeUtil(appKey, clientId, notice, clientsStr, topic, customDataStr, msgId, receiveACK);
					if(callbacks != null) {
						AnIMMessageSentCallbackData data = new AnIMMessageSentCallbackData(false, null, msgId, timestamp);
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.messageSent(data);
							}
						}
					}
				} catch(ArrownockException ex){
					if(callbacks != null) {
						AnIMMessageSentCallbackData data = new AnIMMessageSentCallbackData(true, ex, null, -1);
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.messageSent(data);
							}
						}
					}
				}
			}
		};
		Thread thread = new Thread(sendNoticeThread);
		thread.start();	
	}
	
	long sendNoticeUtil(String appKey, String clientId, String notice, String clientsStr, String topic, 
			String customDataStr, String msgId, boolean receiveACK) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			if (secureConnection) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + NOTICE_ENDPOINT);
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + NOTICE_ENDPOINT);
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			
		    try {
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			    nameValuePairs.add(new BasicNameValuePair("from", clientId));
			    nameValuePairs.add(new BasicNameValuePair("notice", notice));
			    if (clientsStr != null) {
			    	nameValuePairs.add(new BasicNameValuePair("client", clientsStr));
			    } else if (topic != null) {
			    	nameValuePairs.add(new BasicNameValuePair("topic", topic));
			    }
			    if (customDataStr != null) {
			    	nameValuePairs.add(new BasicNameValuePair("custom_data", customDataStr));
			    }
			    nameValuePairs.add(new BasicNameValuePair("receive_ack", String.valueOf(receiveACK)));
			    nameValuePairs.add(new BasicNameValuePair("msg_id", msgId));
			    
			    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
                String date = format.format(new Date());
                nameValuePairs.add(new BasicNameValuePair("date", date));
                nameValuePairs.add(new BasicNameValuePair("key", appKey));
                String signature = getSignature("/" + API_VERSION + "/" + NOTICE_ENDPOINT, nameValuePairs);
                nameValuePairs.add(new BasicNameValuePair("signature", signature));
			    
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
						throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_SEND_NOTICE);
				    } else if (statusCode == 200) {
				        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        String s = convertStreamToString(in);
                        JSONObject json = new JSONObject(s);
                        JSONObject data = json.getJSONObject("response");
				        if (data != null) {
				            long ts = data.optLong("timestamp");
				            return ts;
				        } else {
				            return -1;
				        }
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_SEND_NOTICE);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_SEND_NOTICE);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_SEND_NOTICE);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}
        return -1;
	}
	
	@Deprecated
	public void getTopicInfoAsync(final String appKey, final String topic) {
		Runnable getTopicInfoThread = new Runnable(){
			public void run() {
			    AnIMGetTopicInfoCallbackData data = getTopicInfoUtil(appKey, topic);
				if(callbacks != null) {
					for(IAnIMCallback c : callbacks) {
						if(c != null) {
							c.getTopicInfo(data);
						}
					}
				}
			}
		};
		Thread thread = new Thread(getTopicInfoThread);
		thread.start();	
	}
	
	public void getTopicInfoAsync(final String appKey, final String topic, final IAnIMGetTopicInfoCallback callback) {
        Runnable getTopicInfoThread = new Runnable(){
            public void run() {
                AnIMGetTopicInfoCallbackData data = getTopicInfoUtil(appKey, topic);
                if (callback != null) {
                    if (data.isError()){
                        callback.onError(data.getException());
                    }else {
                        callback.onSuccess(data);
                    }
                } 
            }
        };
        Thread thread = new Thread(getTopicInfoThread);
        thread.start(); 
    }
	
	private AnIMGetTopicInfoCallbackData getTopicInfoUtil(String appKey, String topic){
		HttpURLConnection urlConnection = null;
		AnIMGetTopicInfoCallbackData callbackData = null;
		try {
			if (secureConnection) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + TOPIC_INFO_ENDPOINT + "?key=" + appKey.trim() + "&id=" + topic);
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + TOPIC_INFO_ENDPOINT + "?key=" + appKey.trim() + "&id=" + topic);
				urlConnection = (HttpURLConnection) url.openConnection();
			}
		    try {
		    	urlConnection.connect();
			    
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode == 200) {
					    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					    String s = convertStreamToString(in);
						JSONObject json = new JSONObject(s);
						JSONObject data = json.getJSONObject("response");
						if(data != null) {
							JSONObject sub = data.getJSONObject("topic");
							if(sub != null) {
								JSONArray jsonArray = sub.optJSONArray("parties");
								Set<String> parties = new HashSet<String>();
								for (int i=0; i<jsonArray.length(); i++) {
									parties.add(jsonArray.getString(i));
								}
								Map<String, String> customData = new HashMap<String, String>();
								JSONObject jsonMap = sub.optJSONObject("customData");
								if(jsonMap != null) { 
									Iterator<String> it = jsonMap.keys();  
							        while (it.hasNext()) {  
								        String key = it.next();  
								        String value = jsonMap.optString(key);  
								        customData.put(key, value);  
								    }  
								}
								String topicName = sub.optString("name", null);
								String owner = sub.optString("owner", null);
								SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
								String dateStr = sub.optString("created_at", null);
								String dateStr1 = dateStr.substring(0, dateStr.length()-1) + "+0000";
								Date createdAt = format.parse(dateStr1);
								callbackData = new AnIMGetTopicInfoCallbackData(false, null, topic, topicName, owner, parties, createdAt, customData);
							} else {
								throw new ArrownockException("Unable to get topic info. Failed to acquire topic.", ArrownockException.IM_FAILED_GET_TOPIC_INFO);
							}
						} else {
							throw new ArrownockException("Unable to get topic info. Failed to acquire response.", ArrownockException.IM_FAILED_GET_TOPIC_INFO);
							}
				    } else {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_GET_TOPIC_INFO);
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_TOPIC_INFO);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_TOPIC_INFO);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
			    callbackData = new AnIMGetTopicInfoCallbackData(true, (ArrownockException) e, null, null, null, null, null, null);
			} else {
			    callbackData = new AnIMGetTopicInfoCallbackData(true, 
			            new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_TOPIC_INFO), null, null, null, null, null, null);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}
		return callbackData;
	}
	
	public void getSessionInfoAsync(final String appKey, final String sessionId) {
		Runnable getSessionInfoThread = new Runnable(){
			public void run() {
				AnIMGetSessionInfoCallbackData data = getSessionInfoUtil(appKey, sessionId);
				if (callbacks != null) {
					for(IAnIMCallback c : callbacks) {
						if(c != null) {
							c.getSessionInfo(data);
						}
					}
				}
			}
		};
		Thread thread = new Thread(getSessionInfoThread);
		thread.start();
	}
	
	AnIMGetSessionInfoCallbackData getSessionInfoUtil(String appKey, String sessionId) {
		String partyStr = getFromLocalStorage(context, sessionId);
		AnIMGetSessionInfoCallbackData callbackData = null;
		if (!"".equals(partyStr)) {
			Set<String> parties = new HashSet<String>(Arrays.asList(partyStr.split(",")));
			callbackData = new AnIMGetSessionInfoCallbackData(false, null, sessionId, parties, null);
		} else {
			HttpURLConnection urlConnection = null;
			try {
				if (secureConnection) {
					URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + SESSION_INFO_ENDPOINT + "?key=" + appKey.trim() + "&id=" + sessionId);
					urlConnection = (HttpsURLConnection) url.openConnection();
					((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
					((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
				} else {
					URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + SESSION_INFO_ENDPOINT + "?key=" + appKey.trim() + "&id=" + sessionId);
					urlConnection = (HttpURLConnection) url.openConnection();
				}
			    try {
			    	urlConnection.connect();
				    
				    int statusCode = urlConnection.getResponseCode();
				    try {
					    if (statusCode == 200) {
						    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
						    String s = convertStreamToString(in);
							JSONObject json = new JSONObject(s);
							JSONObject data = json.getJSONObject("response");
							if(data != null) {
								JSONObject sub = data.getJSONObject("session");
								if(sub != null) {
									JSONArray jsonArray = sub.optJSONArray("parties");
									Set<String> parties = new HashSet<String>();
									for (int i=0; i<jsonArray.length(); i++) {
										parties.add(jsonArray.getString(i));
									}
//									SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
//									String dateStr = sub.optString("created_at", null);
//									String dateStr1 = dateStr.substring(0, dateStr.length()-1) + "+0000";
//									Date createdAt = format.parse(dateStr1);
									saveToLocalStorage(context, sessionId, setJoin(parties, ","));
									callbackData = new AnIMGetSessionInfoCallbackData(false, null, sessionId, parties, null);
								} else {
									throw new ArrownockException("Unable to get session info. Failed to acquire topic.", ArrownockException.IM_FAILED_GET_SESSION_INFO);
								}
							} else {
								throw new ArrownockException("Unable to get session info. Failed to acquire response.", ArrownockException.IM_FAILED_GET_SESSION_INFO);
								}
					    } else {
						    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
						    String s = convertStreamToString(es);
							JSONObject json = new JSONObject(s);
							JSONObject meta = json.getJSONObject("meta");
							throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_GET_SESSION_INFO);
					    }
					} catch (JSONException e) {
						throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_SESSION_INFO);
					}
			    } catch (IOException e) {
			    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_SESSION_INFO);
			    } 
			} catch (Exception e) {
				if (e instanceof ArrownockException) {
					callbackData = new AnIMGetSessionInfoCallbackData(true, (ArrownockException)e, sessionId, null, null);
				} else {
					callbackData = new AnIMGetSessionInfoCallbackData(true, new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_SESSION_INFO), sessionId, null, null);
				}
			}	
			finally {
				if (urlConnection != null) {
					urlConnection.disconnect();
				}   	
			}
		}
		
		return callbackData;
	}
	
	public void getTopicLogAsync(final String appKey, final String topicId, final String clientId, final int limit, final long timestamp, final boolean isOffline, final IAnIMHistoryCallback callback) {
		Runnable getTopicLogThread = new Runnable(){
			public void run() {
				try{
					getTopicLogUtil(appKey, topicId, clientId, limit, timestamp, isOffline, callback);
				} catch(ArrownockException ex){
					if(callback != null) {
						callback.onError(ex);
					}
				}
			}
		};
		Thread thread = new Thread(getTopicLogThread);
		thread.start();	
	}
	
	void getTopicLogUtil(String appKey, String topicId, String clientId, int limit, long timestamp, boolean isOffline, IAnIMHistoryCallback callback) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			StringBuffer path = new StringBuffer();
			path.append(this.getAPIHost(context));
			path.append("/" + API_VERSION + "/" + HISTORY_ENDPOINT + "?key=" + appKey.trim());
			path.append("&type=topic");
			path.append("&sdk=true");
			if(topicId == null) {
				path.append("&all=1");
			} else {
				path.append("&topic_id=");
				path.append(topicId);
			}
			path.append("&me=");
			path.append(clientId);
			path.append("&device_type=mobile");
			if(limit > 0) {
				path.append("&limit=" + limit);
			}
			if(timestamp > 0) {
				path.append("&timestamp=" + timestamp);
			}
			if(isOffline) {
				path.append("&offline=1");
			}
			path.append("&b=1");
			if (secureConnection) {
				URL url = new URL(Constants.HTTPS + path.toString());
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + path.toString());
				urlConnection = (HttpURLConnection) url.openConnection();
			}
		    try {
		    	urlConnection.connect();
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode == 200) {
					    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					    String s = convertStreamToString(in);
						JSONObject json = new JSONObject(s);
						JSONObject data = json.getJSONObject("response");
						JSONObject meta = json.getJSONObject("meta");
						if(data != null) {
							JSONArray jsonArray = data.optJSONArray("messages");
							List<AnIMMessage> logs = new ArrayList<AnIMMessage>();
							for (int i=0; i<jsonArray.length(); i++) {
								JSONObject obj = jsonArray.getJSONObject(i);
								if(obj != null) {
									String type = obj.optString("content_type");
									String msgId = obj.optString("msg_id");
									String tid = obj.optString("topic_id");
									String from = obj.optString("from");
									String message = obj.optString("message");
									long ts = obj.optLong("timestamp");
									JSONObject customObj = obj.optJSONObject("customData");
									Map<String, String> customData = null;
									if (customObj != null) {
										Iterator<?> nameItr = customObj.keys();
										customData = new HashMap<String, String>();
										while(nameItr.hasNext()) {
											String key = (String)nameItr.next();
											Object value = customObj.get(key);
											if(value != null && value instanceof String) {
												customData.put(key, ANEmojiUtil.stringConvertToEmoji((String)value));
											} else {
												try {
													customData.put(key, String.valueOf(value));
												} catch(Exception e) {
													e.printStackTrace();
												}
											}
										}
									}
									
									if("text".equals(type)) {
										message = ANEmojiUtil.stringConvertToEmoji(message);
										AnIMMessage m = new AnIMMessage(AnIMMessageType.AnIMTextMessage, msgId, tid, message, null, null, from, null, ts, customData);
										logs.add(m);
									} else if("binary".equals(type)) {
										String fileType = obj.optString("fileType");
										byte[] d = null;
										if(message != null && message.length() > 0) {
											d = ANBase64.decode(message);
										}
										AnIMMessage m = new AnIMMessage(AnIMMessageType.AnIMBinaryMessage, msgId, tid, null, d, fileType, from, null, ts, customData);
										logs.add(m);
									}
								}
							}
							if(callback != null) {
								if(meta != null && meta.has("leftCount")) {
									callback.onSuccess(logs, meta.getInt("leftCount"));
								} else {
									callback.onSuccess(logs, -1);
								}
							}
						} else {
							throw new ArrownockException("Unable to get topic history. ", ArrownockException.IM_FAILED_GET_TOPIC_HISTORY);
						}
				    } else {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}	
	}
	
	public void getLogAsync(final String appKey, final Set<String> clientIds, final String clientId, final int limit, final long timestamp, final boolean isOffline, final IAnIMHistoryCallback callback) {
		Runnable getTopicLogThread = new Runnable() {
			public void run() {
				try{
					getLogUtil(appKey, clientIds, clientId, limit, timestamp, isOffline, callback);
				} catch(ArrownockException ex){
					if(callback != null) {
						callback.onError(ex);
					}
				}
			}
		};
		Thread thread = new Thread(getTopicLogThread);
		thread.start();	
	}
	
	void getLogUtil(String appKey, Set<String> clientIds, String clientId, int limit, long timestamp, boolean isOffline, IAnIMHistoryCallback callback) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			StringBuffer path = new StringBuffer();
			path.append(this.getAPIHost(context));
			path.append("/" + API_VERSION + "/" + HISTORY_ENDPOINT + "?key=" + appKey.trim());
			path.append("&type=private");
			path.append("&sdk=true");
			if(clientIds == null) {
				path.append("&all=1");
			} else {
				path.append("&parties=");
				Iterator<String> itr = clientIds.iterator();
				while(itr.hasNext()) {
					path.append(itr.next());
					path.append(",");
				}
			}
			path.append("&me=");
			path.append(clientId);
			path.append("&device_type=mobile");
			if(limit > 0) {
				path.append("&limit=" + limit);
			}
			if(timestamp > 0) {
				path.append("&timestamp=" + timestamp);
			}
			if(isOffline) {
				path.append("&offline=1");
			}
			path.append("&b=1");
			if (secureConnection) {
				URL url = new URL(Constants.HTTPS + path.toString());
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + path.toString());
				urlConnection = (HttpURLConnection) url.openConnection();
			}
		    try {
		    	urlConnection.connect();
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode == 200) {
					    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					    String s = convertStreamToString(in);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						JSONObject data = json.getJSONObject("response");
						if(data != null) {
							JSONArray jsonArray = data.optJSONArray("messages");
							List<AnIMMessage> logs = new ArrayList<AnIMMessage>();
							for (int i=0; i<jsonArray.length(); i++) {
								JSONObject obj = jsonArray.getJSONObject(i);
								if(obj != null) {
									String type = obj.optString("content_type");
									String msgId = obj.optString("msg_id");
									String from = obj.optString("from");
									String message = obj.optString("message");
									long ts = obj.optLong("timestamp");
									JSONObject customObj = obj.optJSONObject("customData");
									Map<String, String> customData = null;
									if (customObj != null) {
										Iterator<?> nameItr = customObj.keys();
										customData = new HashMap<String, String>();
										while(nameItr.hasNext()) {
											String key = (String)nameItr.next();
											Object value = customObj.get(key);
											if(value != null && value instanceof String) {
												customData.put(key, ANEmojiUtil.stringConvertToEmoji((String)value));
											} else {
												try {
													customData.put(key, String.valueOf(value));
												} catch(Exception e) {
													e.printStackTrace();
												}
											}
										}
									}
									
									if("text".equals(type)) {
										message = ANEmojiUtil.stringConvertToEmoji(message);
										AnIMMessage m = new AnIMMessage(AnIMMessageType.AnIMTextMessage, msgId, null, message, null, null, from, null, ts, customData);
										logs.add(m);
									} else if("binary".equals(type)) {
										String fileType = obj.optString("fileType");
										byte[] d = null;
										if(message != null && message.length() > 0) {
											d = ANBase64.decode(message);
										}
										AnIMMessage m = new AnIMMessage(AnIMMessageType.AnIMBinaryMessage, msgId, null, null, d, fileType, from, null, ts, customData);
										logs.add(m);
									}
								}
							}
							if(callback != null) {
								if(meta != null && meta.has("leftCount")) {
									callback.onSuccess(logs, meta.getInt("leftCount"));
								} else {
									callback.onSuccess(logs, -1);
								}
							}
						} else {
							throw new ArrownockException("Unable to get chat history. ", ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
						}
				    } else {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}	
	}
	
	public void syncHistoryAsync(final String appKey, final String clientId, final int limit, final long timestamp, final IAnIMHistoryCallback callback) {
		Runnable getTopicLogThread = new Runnable() {
			public void run() {
				try{
					syncHistoryUtil(appKey, clientId, limit, timestamp, callback);
				} catch(ArrownockException ex){
					if(callback != null) {
						callback.onError(ex);
					}
				}
			}
		};
		Thread thread = new Thread(getTopicLogThread);
		thread.start();	
	}
	
	void syncHistoryUtil(String appKey, String clientId, int limit, long timestamp, IAnIMHistoryCallback callback) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			StringBuffer path = new StringBuffer();
			path.append(this.getAPIHost(context));
			path.append("/" + API_VERSION + "/" + SYNC_HISTORY_ENDPOINT + "?key=" + appKey.trim());
			path.append("&b=1");
			path.append("&sdk=true");
			path.append("&me=");
			path.append(clientId);
			path.append("&device_type=mobile");
			if(limit > 0) {
				path.append("&limit=" + limit);
			}
			if(timestamp > 0) {
				path.append("&timestamp=" + timestamp);
			}
			if (secureConnection) {
				URL url = new URL(Constants.HTTPS + path.toString());
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + path.toString());
				urlConnection = (HttpURLConnection) url.openConnection();
			}
		    try {
		    	urlConnection.connect();
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode == 200) {
					    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					    String s = convertStreamToString(in);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						JSONObject data = json.getJSONObject("response");
						if(data != null) {
							JSONArray jsonArray = data.optJSONArray("messages");
							List<AnIMMessage> logs = new ArrayList<AnIMMessage>();
							for (int i=0; i<jsonArray.length(); i++) {
								JSONObject obj = jsonArray.getJSONObject(i);
								if(obj != null) {
									String type = obj.optString("content_type");
									String msgId = obj.optString("msg_id");
									String from = obj.optString("from", "");
									String message = obj.optString("message");
									long ts = obj.optLong("timestamp");
									JSONObject customObj = obj.optJSONObject("customData");
									Map<String, String> customData = null;
									if (customObj != null) {
										Iterator<?> nameItr = customObj.keys();
										customData = new HashMap<String, String>();
										while(nameItr.hasNext()) {
											String key = (String)nameItr.next();
											Object value = customObj.get(key);
											if(value != null && value instanceof String) {
												customData.put(key, ANEmojiUtil.stringConvertToEmoji((String)value));
											} else {
												try {
													customData.put(key, String.valueOf(value));
												} catch(Exception e) {
													e.printStackTrace();
												}
											}
										}
									}
									
									String to = null;
									JSONArray parties = obj.optJSONArray("parties");
									if(!from.isEmpty() && from.equals(clientId) && parties != null) {
										for(int index = 0; index < parties.length(); index++) {
											String temp = parties.optString(index);
											if(temp != null && !temp.equals(from)) {
												to = temp;
												break;
											}
										}
									}
									
									if("text".equals(type)) {
										message = ANEmojiUtil.stringConvertToEmoji(message);
										AnIMMessage m = new AnIMMessage(AnIMMessageType.AnIMTextMessage, msgId, null, message, null, null, from, to, ts, customData);
										logs.add(m);
									} else if("binary".equals(type)) {
										String fileType = obj.optString("fileType");
										byte[] d = null;
										if(message != null && message.length() > 0) {
											d = ANBase64.decode(message);
										}
										AnIMMessage m = new AnIMMessage(AnIMMessageType.AnIMBinaryMessage, msgId, null, null, d, fileType, from, to, ts, customData);
										logs.add(m);
									}
								}
							}
							if(callback != null) {
								if(meta != null && meta.has("leftCount")) {
									callback.onSuccess(logs, meta.getInt("leftCount"));
								} else {
									callback.onSuccess(logs, -1);
								}
							}
						} else {
							throw new ArrownockException("Unable to sync chat history. ", ArrownockException.IM_FAILED_SYNC_CHAT_HISTORY);
						}
				    } else {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_SYNC_CHAT_HISTORY);
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_SYNC_CHAT_HISTORY);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_SYNC_CHAT_HISTORY);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_SYNC_CHAT_HISTORY);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}	
	}
	
	@Deprecated
	public void getClientsStatusAsync(final String appKey, final String clientsStr) {
        Runnable getClientsStatusThread = new Runnable(){
            public void run() {
                AnIMGetClientsStatusCallbackData data = getClientsStatusUtil(appKey, clientsStr);
                if(callbacks != null) {
                    for(IAnIMCallback c : callbacks) {
						if(c != null) {
							c.getClientsStatus(data);
						}
					}
                }
            }
        };
        Thread thread = new Thread(getClientsStatusThread);
        thread.start(); 
    }
	
	public void getClientsStatusAsync(final String appKey, final String clientsStr, final IAnIMGetClientsStatusCallback callback){
        Runnable getClientsStatusThread = new Runnable(){
            public void run() {
                AnIMGetClientsStatusCallbackData data = getClientsStatusUtil(appKey, clientsStr);
                if (callback != null) {
                    if (data.isError()){
                        callback.onError(data.getException());
                    }else {
                        callback.onSuccess(data);
                    }
                } 
            }
        };
        Thread thread = new Thread(getClientsStatusThread);
        thread.start(); 
	}
	
	private AnIMGetClientsStatusCallbackData getClientsStatusUtil(String appKey, String clientsStr) {
		HttpURLConnection urlConnection = null;
		AnIMGetClientsStatusCallbackData callbackData = null;
		try {
			if (secureConnection) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + CLIENT_STATUS_ENDPOINT + "?key=" + appKey.trim() + "&client=" + clientsStr);
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + CLIENT_STATUS_ENDPOINT + "?key=" + appKey.trim() + "&client=" + clientsStr);
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			
		    try {
		    	urlConnection.connect();
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode == 200) {
					    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					    String s = convertStreamToString(in);
						JSONObject json = new JSONObject(s);
						JSONObject data = json.getJSONObject("response");
						if(data != null) {
							JSONArray jsonArray = data.optJSONArray("status");
							Map<String, Boolean> map = new HashMap<String, Boolean>();
							for (int i=0; i<jsonArray.length(); i++) {
								JSONObject obj = jsonArray.getJSONObject(i);
								Iterator<?> it = obj.keys();
								while (it.hasNext()) {
									String clientId = (String)it.next();
									Boolean status = obj.getBoolean(clientId);
									map.put(clientId, status);
								}
							}
						    callbackData = new AnIMGetClientsStatusCallbackData(false, null, map);
						} else {
							throw new ArrownockException("Unable to get clients status. Failed to acquire status.", ArrownockException.IM_FAILED_GET_CLIENTS_STATUS);
							}
				    } else {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_GET_CLIENTS_STATUS);
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CLIENTS_STATUS);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CLIENTS_STATUS);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
			    callbackData = new AnIMGetClientsStatusCallbackData(true, (ArrownockException)e, null);
			} else {
			    callbackData = new AnIMGetClientsStatusCallbackData(true, 
			            new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CLIENTS_STATUS), null);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}	
		return callbackData;
	}
	
	@Deprecated
	public void getTopicStatusAsync(final String appKey, final String topicId) {
        Runnable getTopicStatusThread = new Runnable(){
            public void run() {
                AnIMGetClientsStatusCallbackData data = getTopicStatusUtil(appKey, topicId);
                if (callbacks != null) {
                    for(IAnIMCallback c : callbacks) {
						if(c != null) {
							c.getClientsStatus(data);
						}
					}
                }
            }
        };
        Thread thread = new Thread(getTopicStatusThread);
        thread.start(); 
    }
	
    public void getTopicStatusAsync(final String appKey, final String topicId, final IAnIMGetClientsStatusCallback callback) {
        Runnable getTopicStatusThread = new Runnable(){
            public void run() {
                AnIMGetClientsStatusCallbackData data = getTopicStatusUtil(appKey, topicId);
                if (callback != null) {
                    if (data.isError()){
                        callback.onError(data.getException());
                    }else {
                        callback.onSuccess(data);
                    }
                } 
            }
        };
        Thread thread = new Thread(getTopicStatusThread);
        thread.start(); 
    }
	
	private AnIMGetClientsStatusCallbackData getTopicStatusUtil(String appKey, String topicId){
	    HttpURLConnection urlConnection = null;
	    AnIMGetClientsStatusCallbackData callbackData = null;
        try {
            if (secureConnection) {
                URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + CLIENT_STATUS_ENDPOINT + "?key=" + appKey.trim() + "&topic=" + topicId);
                urlConnection = (HttpsURLConnection) url.openConnection();
                ((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
                ((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
            } else {
                URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + CLIENT_STATUS_ENDPOINT + "?key=" + appKey.trim() + "&topic=" + topicId);
                urlConnection = (HttpURLConnection) url.openConnection();
            }
            
            try {
                urlConnection.connect();
                int statusCode = urlConnection.getResponseCode();
                try {
                    if (statusCode == 200) {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        String s = convertStreamToString(in);
                        JSONObject json = new JSONObject(s);
                        JSONObject data = json.getJSONObject("response");
                        if(data != null) {
                            JSONArray jsonArray = data.optJSONArray("status");
                            Map<String, Boolean> map = new HashMap<String, Boolean>();
                            for (int i=0; i<jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                Iterator<?> it = obj.keys();
                                while (it.hasNext()) {
                                    String clientId = (String)it.next();
                                    Boolean status = obj.getBoolean(clientId);
                                    map.put(clientId, status);
                                }
                            }
                            callbackData = new AnIMGetClientsStatusCallbackData(false, null, map);
                        } else {
                            throw new ArrownockException("Unable to get clients status. Failed to acquire status.", ArrownockException.IM_FAILED_GET_CLIENTS_STATUS);
                            }
                    } else {
                        InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
                        String s = convertStreamToString(es);
                        JSONObject json = new JSONObject(s);
                        JSONObject meta = json.getJSONObject("meta");
                        throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_GET_CLIENTS_STATUS);
                    }
                } catch (JSONException e) {
                    throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CLIENTS_STATUS);
                }
            } catch (IOException e) {
                throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CLIENTS_STATUS);
            } 
        } catch (Exception e) {
            if (e instanceof ArrownockException) {
                callbackData = new AnIMGetClientsStatusCallbackData(true, (ArrownockException) e, null);
            } else {
                callbackData = new AnIMGetClientsStatusCallbackData(true, 
                        new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CLIENTS_STATUS), null);
            }
        }   
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }       
        }   
        return callbackData;
    }
	
	@Override 
	@Deprecated
	public void getTopicListAsync(final String appKey, final String clientId) {
		Runnable getTopicListThread = new Runnable(){
			public void run() {
				AnIMGetTopicListCallbackData data = getTopicListUtil(appKey, clientId);
				if (callbacks != null) {
					for(IAnIMCallback c : callbacks) {
						if(c != null) {
							c.getTopicList(data);
						}
					}
				}
			}
		};
		Thread thread = new Thread(getTopicListThread);
		thread.start();	
	}
	
	@Override
	public void getTopicListAsync(final String appKey, final String clientId, final IAnIMGetTopicListCallback callback){
        Runnable getTopicListThread = new Runnable(){
            public void run() {
                    AnIMGetTopicListCallbackData data = getTopicListUtil(appKey, clientId);
                    if (callback != null) {
                        if (data.isError()){
                            callback.onError(data.getException());
                        }else {
                            callback.onSuccess(data);
                        }
                    } 
            }
        };
        Thread thread = new Thread(getTopicListThread);
        thread.start(); 
	}
	
	private AnIMGetTopicListCallbackData getTopicListUtil(String appKey, String clientId) {
		AnIMGetTopicListCallbackData callbackData = null;
		HttpURLConnection urlConnection = null;
		try {
			if (secureConnection) {
				if (clientId == null) {
					URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + TOPIC_LIST_ENDPOINT + "?key=" + appKey.trim());
					urlConnection = (HttpsURLConnection) url.openConnection();
				} else {
					URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + TOPIC_LIST_ENDPOINT + "?key=" + appKey.trim() + "&client=" + clientId);
					urlConnection = (HttpsURLConnection) url.openConnection();
				}
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				if (clientId == null) {
					URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + TOPIC_LIST_ENDPOINT + "?key=" + appKey.trim());
					urlConnection = (HttpURLConnection) url.openConnection();
				} else {
					URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + TOPIC_LIST_ENDPOINT + "?key=" + appKey.trim() + "&client=" + clientId);
					urlConnection = (HttpURLConnection) url.openConnection();
				}
			}
			
		    try {
		    	urlConnection.connect();
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode == 200) {
					    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					    String s = convertStreamToString(in);
						JSONObject json = new JSONObject(s);
						JSONObject data = json.getJSONObject("response");
						if(data != null) {
							JSONArray jsonArray = data.optJSONArray("list");
							List<JSONObject> lists = new ArrayList<JSONObject>();
							for (int i=0; i<jsonArray.length(); i++) {
								lists.add(jsonArray.getJSONObject(i));
							}
							callbackData = new AnIMGetTopicListCallbackData(false, null, lists, clientId);
						} else {
							throw new ArrownockException("Unable to get topic list. Failed to acquire list.", ArrownockException.IM_FAILED_GET_TOPIC_LIST);
						}
				    } else {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_GET_TOPIC_LIST);
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_TOPIC_LIST);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_TOPIC_LIST);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				callbackData = new AnIMGetTopicListCallbackData(true, (ArrownockException) e, null, clientId);
			} else {
				callbackData = new AnIMGetTopicListCallbackData(true, new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_TOPIC_LIST), null, clientId);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}
		
		return callbackData;
	}

	private class MQTTEvent implements IMQTTEvent {
		public void generalFail(Throwable exception) {
			Log.e(
					LOG_TAG,
					"General Problem occured. Exception Type: " + exception.getClass().getName() + "; Exception Message: "
							+ exception.getMessage(), exception);
		}
		
		public void connected() {
			if(callbacks != null) {
				final AnIMStatusUpdateCallbackData data = new AnIMStatusUpdateCallbackData(AnIMStatus.ONLINE, null);
				//when anIM connect successfully, report device_id to im_client_tokens 
                if (Constants.DM_ENABLED) {
                    if (!"".equals(clientIdForReport)){
                        reportDeviceIdToIM(clientIdForReport);
                    }
                }
				try {
					Runnable callbackThread = new Runnable(){
						@Override
						public void run() {
							for(IAnIMCallback c : callbacks) {
								if(c != null) {
									c.statusUpdate(data);
								}
							}
						}
					};
					new Thread(callbackThread).start();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public void disconnected(Throwable exception) {
			if (isReconnect) {
				isReconnect = false;
			} else {
				mqttAgent = null;
				if(noDisconnectCallback) {
					noDisconnectCallback = false;
					return;
				}
				if(callbacks != null) {
					AnIMStatusUpdateCallbackData data = null;
					if(exception != null && exception instanceof Exception) {
						data = new AnIMStatusUpdateCallbackData(getCurrentStatus(), new ArrownockException("Connection to IM server is broken.", (Exception)exception, ArrownockException.IM_CONNECTION_BROKEN));
					} else if (forceClose) {
						data = new AnIMStatusUpdateCallbackData(getCurrentStatus(), new ArrownockException("User logged in from other device, client force closed.", ArrownockException.IM_FORCE_CLOSED));
						forceClose = false;
					} else {
						data =  new AnIMStatusUpdateCallbackData(getCurrentStatus(), null);
					}
					try {
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.statusUpdate(data);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		public void failConnect(Throwable exception) {
			Log.e(
					LOG_TAG,
					"Failed to connect to IM Server. Exception Type: " + exception.getClass().getName() + "; Exception Message: "
							+ exception.getMessage(), exception);
			mqttAgent = null;
			if(exception instanceof MqttException) {
				if(((MqttException)exception).getReasonCode() == 5) {
					return; // simply ignore
				}
			}
			if(callbacks != null) {
				AnIMStatusUpdateCallbackData data = new AnIMStatusUpdateCallbackData(getCurrentStatus(), new ArrownockException(exception.getMessage(), ArrownockException.IM_FAILED_CONNECT));
				try {
					for(IAnIMCallback c : callbacks) {
						if(c != null) {
							c.statusUpdate(data);
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}	
		}
		public void failDisconnect(Throwable exception) {
			if (isReconnect) {
				isReconnect = false;
			} else {
				Log.e(
						LOG_TAG,
						"Failed to disconnect to IM Server. Exception Type: " + exception.getClass().getName()
								+ "; Exception Message: " + exception.getMessage(), exception);
				if(callbacks != null) {
					AnIMStatusUpdateCallbackData data = new AnIMStatusUpdateCallbackData(getCurrentStatus(), new ArrownockException(exception.getMessage(), ArrownockException.IM_FAILED_DISCONNECT));
					try {
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.statusUpdate(data);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}	
			}
		}
		
		@Override
		public void messageArrived(String mqttTopic, String payload) {
			if (payload == null || payload.equals(""))
				return;
			String from = null;
			Set<String> parties = null;
			String message = null;
			String fileType = null;
			String topic = null;
			String sessionKey = null;
			String msgId = null;
			String topicName = null;
			String owner = null;
			Map<String, String> customData = null;
			Set<String> clients = null;
			long timestamp = -1;
			int msg_type = -1;
			String type = null;
			String gpType = "";
			try {
				JSONObject payloadJson = new JSONObject(payload);
				from = payloadJson.optString("from", null);
				message = payloadJson.optString("message", null);
				fileType = payloadJson.optString("fileType", null);
				topic = payloadJson.optString("topic", null);
				sessionKey = payloadJson.optString("session_key", null);
				timestamp = payloadJson.optLong("timestamp", -1);
				topicName = payloadJson.optString("topic_name", null);
				owner = payloadJson.optString("owner", null);
				JSONArray clientsObj = payloadJson.optJSONArray("clients");
				type = payloadJson.optString("type", null);
                if (clientsObj != null) {
                    clients = new HashSet<String>();
                    for (int i=0; i<clientsObj.length(); i++) {
                        String clientStr = clientsObj.getString(i);
                        clients.add(clientStr);
                    }
                }
				if (sessionKey != null) {
					AnIMGetSessionInfoCallbackData data = getSessionInfoUtil(appKey, sessionKey);
					if (!data.isError()) {
						parties = data.getParties();
					}
				}
				msgId = payloadJson.optString("msg_id", null);
				JSONObject customObj = payloadJson.optJSONObject("customData");
				if (customObj != null) {
					Iterator<?> nameItr = customObj.keys();
					customData = new HashMap<String, String>();
					while(nameItr.hasNext()) {
						String key = (String)nameItr.next();
						String value = customObj.getString(key);
					    customData.put(key, value);
					}
				}
				msg_type = payloadJson.optInt("msg_type", -1);
			} catch (JSONException ex) {
			}
			switch (msg_type) {
			case 1:   // private text message
				if(callbacks != null) {
				    message = ANEmojiUtil.stringConvertToEmoji(message);
				    convertCustomDataToEmoji(customData);
					AnIMMessageCallbackData data = new AnIMMessageCallbackData(msgId, from, parties, message, customData, timestamp);
					try {
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.receivedMessage(data);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case 2: // private binary
				byte[] content = new byte[0];
				if (message.length() != 0) { 
					content = ANBase64.decode(message);
				} else {
				}
				if(callbacks != null) {
					AnIMBinaryCallbackData data = new AnIMBinaryCallbackData(msgId, from, parties, content, fileType, customData, timestamp);
					try { 
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.receivedBinary(data);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case 3:  // public text
			    message = ANEmojiUtil.stringConvertToEmoji(message);
			    convertCustomDataToEmoji(customData);
			    if (customData.containsKey("message_type")) {
			        gpType = customData.get("message_type");
			    }
			    
			    if ("gp".equals(gpType)) {
			        if(groupediaCallback != null) {
                        try {
                            groupediaCallback.receivedMessage(msgId, message, timestamp, from, topic, customData);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
			    } else {
			        if(msgId.startsWith("D")) {
                        if(deskCallback != null) {
                            try {
                                deskCallback.receivedMessage(msgId, message, timestamp, from, topic, customData);
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if(callbacks != null) {
                            AnIMTopicMessageCallbackData data = new AnIMTopicMessageCallbackData(msgId, from, topic, message, customData, timestamp);
                            try {
                                for(IAnIMCallback c : callbacks) {
                                    if(c != null) {
                                        c.receivedTopicMessage(data);
                                    }
                                }
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
			    }
				break;
			case 4: // public binary
				content = new byte[0];
				if (message.length() != 0) { 
					content = ANBase64.decode(message);
				} else {
				}
                if (customData.containsKey("message_type")) {
                    gpType = customData.get("message_type");
                }
                
                if ("gp".equals(gpType)) {
                    if(groupediaCallback != null) {
                        try {
                            groupediaCallback.receivedBinary(msgId, content, fileType, timestamp, from, topic, customData);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if(msgId.startsWith("D")) {
                        if(deskCallback != null) {
                            try {
                                deskCallback.receivedBinary(msgId, content, fileType, timestamp, from, topic, customData);
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if(callbacks != null) {
                            AnIMTopicBinaryCallbackData data = new AnIMTopicBinaryCallbackData(msgId, from, topic, content, fileType, customData, timestamp);
                            try {
                                for(IAnIMCallback c : callbacks) {
                                    if(c != null) {
                                        c.receivedTopicBinary(data);
                                    }
                                }
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
				break;
			case 5: // notice
			    message = ANEmojiUtil.stringConvertToEmoji(message);
			    convertCustomDataToEmoji(customData);
				if(callbacks != null) {
					AnIMNoticeCallbackData data = new AnIMNoticeCallbackData(msgId, from, message, customData, timestamp, topic);
					try {
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.receivedNotice(data);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case 11: // receive ACK
				if(callbacks != null) {
					AnIMReceiveACKCallbackData data = new AnIMReceiveACKCallbackData(msgId, from, type);
					try {
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.receivedReceiveACK(data);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case 12: // read ACK
				if(callbacks != null) {
					AnIMReadACKCallbackData data = new AnIMReadACKCallbackData(msgId, from, type);
					try {
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.receivedReadACK(data);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case 21: // force close
				if (mqttAgent != null) {
					// Run disconnect in new thread
					Runnable disconnectThread = new Runnable(){
						public void run() {
							forceClose = true;
							mqttAgent.disconnect();
							mqttAgent = null;
						}
					};
					Thread thread = new Thread(disconnectThread);
					thread.start();
				}
				break;
			case 22: // client deprecated
				if (mqttAgent != null) {
					Runnable disconnectThread = new Runnable(){
						public void run() {
							noDisconnectCallback = true;
							mqttAgent.disconnect();
							mqttAgent = null;
						}
					};
					Thread thread = new Thread(disconnectThread);
					thread.start();
				}
				break;
			case 31: // anLive invitation
				if(signalControllerCallback != null) {
					String sessionId = customData.get("sid");
					signalControllerCallback.onInvitationRecieved(sessionId);
				}
				break;
			case 32: // anLive offer
				if(signalControllerCallback != null) {
					String o = customData.get("o");
					int orientation = 0;
					try {
						orientation = Integer.valueOf(o);
					} catch(Exception e) {
						//mute
					}
					signalControllerCallback.onOfferRecieved(from, message, orientation);
				}
				break;
			case 33: // anLive answer
				if(signalControllerCallback != null) {
					String o = customData.get("o");
					int orientation = 0;
					try {
						orientation = Integer.valueOf(o);
					} catch(Exception e) {
						//mute
					}
					signalControllerCallback.onAnswerRecieved(from, message, orientation);
				}
				break;
			case 34: // ice candidate
				if(signalControllerCallback != null) {
					signalControllerCallback.onICECandidate(from, message);
				}
				break;
			case 35: // remote party hangup
                if(signalControllerCallback != null) {
                    signalControllerCallback.onRemoteHangup(from);
                }
                break;
			case 51: // create topic message
				if(msgId.startsWith("D")) {
					
				} else {
	                if(callbacks != null) {
	                    try {
	                        Map<String, String> topicCustomData = getTopicCustomData(customData);
        	                    AnIMCreateTopicEventData data = new AnIMCreateTopicEventData(msgId, from, topic, topicName, owner, clients, timestamp, topicCustomData);
	                        
        	                    for(IAnIMCallback c : callbacks) {
								if(c != null) {
									c.receivedCreateTopicEvent(data);
								}
							}
	                    } catch(Exception e) {
	                        e.printStackTrace();
	                    }
	                }
				}
                break;
			case 52: // update topic message
				if(msgId.startsWith("D")) {
					
				} else {
	                if(callbacks != null) {
	                    try {
        	                    Map<String, String> topicCustomData = getTopicCustomData(customData);
        	                    AnIMUpdateTopicEventData data = new AnIMUpdateTopicEventData(msgId, from, topic, topicName, owner, timestamp, topicCustomData);
	                    
	                        for(IAnIMCallback c : callbacks) {
								if(c != null) {
									c.receivedUpdateTopicEvent(data);
								}
							}
	                    } catch(Exception e) {
	                        e.printStackTrace();
	                    }
	                }
				}
                break;
			case 53: // add clients from topic message
				if(msgId.startsWith("D")) {
				    if(deskCallback != null) {
                        try {
                            deskCallback.accountAddedToSession(topic, timestamp, customData);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
				} else {
	                if(callbacks != null) {
	                    try {
        	                    Map<String, String> topicCustomData = getTopicCustomData(customData);
        	                    AnIMAddClientsEventData data = new AnIMAddClientsEventData(msgId, from, topic, clients, timestamp, topicCustomData);
	                    
	                        for(IAnIMCallback c : callbacks) {
								if(c != null) {
									c.receivedAddClientsToTopicEvent(data);
								}
							}
	                    } catch(Exception e) {
	                        e.printStackTrace();
	                    }
	                }
				}
                break;
			case 54: // remove clients from topic message
				if(msgId.startsWith("D")) {
					
				} else {
	                if(callbacks != null) {
	                    try {
        	                    Map<String, String> topicCustomData = getTopicCustomData(customData);
        	                    AnIMRemoveClientsEventData data = new AnIMRemoveClientsEventData(msgId, from, topic, clients, timestamp, topicCustomData);
	                    
	                        for(IAnIMCallback c : callbacks) {
								if(c != null) {
									c.receivedRemoveClientsFromTopicEvent(data);
								}
							}
	                    } catch(Exception e) {
	                        e.printStackTrace();
	                    }
	                }
				}
                break;
			case 55: // remove topic message
				if(msgId.startsWith("D")) {
					if(deskCallback != null) {
						try {
							deskCallback.topicClosed(topic, timestamp, customData);
						} catch(Exception e) {
	                        e.printStackTrace();
	                    }
					}
				} else {
	                if(callbacks != null) {
	                    try {
        	                    Map<String, String> topicCustomData = getTopicCustomData(customData);
        	                    AnIMRemoveTopicEventData data = new AnIMRemoveTopicEventData(msgId, from, topic, timestamp, topicCustomData);
	                    
	                        for(IAnIMCallback c : callbacks) {
								if(c != null) {
									c.receivedRemoveTopicEvent(data);
								}
							}
	                    } catch(Exception e) {
	                        e.printStackTrace();
	                    }
	                }
				}
                break;
			case 61:	// sent text message on other device
				if(callbacks != null) {
					String to = null;
					if(clients != null && clients.size() > 0) {
						clients.remove(from);
						if(clients.size() > 0) {
							to = clients.iterator().next();
						}
					}
					AnIMMessage m = new AnIMMessage(AnIMMessageType.AnIMTextMessage, msgId, null, message, null, null, from, to, timestamp, customData);
					try {
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.messageSentFromOtherDevice(m);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case 62:	// sent binary message on other device
				if(callbacks != null) {
					byte[] d = null;
					if(message != null && message.length() > 0) {
						d = ANBase64.decode(message);
					}
					String to = null;
					if(clients != null && clients.size() > 0) {
						clients.remove(from);
						if(clients.size() > 0) {
							to = clients.iterator().next();
						}
					}
					AnIMMessage m = new AnIMMessage(AnIMMessageType.AnIMBinaryMessage, msgId, null, null, d, fileType, from, to, timestamp, customData);
					try {
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.messageSentFromOtherDevice(m);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case 63:	// sent topic text message on other device
				if(callbacks != null) {
					AnIMMessage m = new AnIMMessage(AnIMMessageType.AnIMTextMessage, msgId, topic, message, null, null, from, null, timestamp, customData);
					try {
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.messageSentFromOtherDevice(m);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			case 64:	// sent topic binary message on other device
				if(callbacks != null) {
					byte[] d = null;
					if(message != null && message.length() > 0) {
						d = ANBase64.decode(message);
					}
					AnIMMessage m = new AnIMMessage(AnIMMessageType.AnIMBinaryMessage, msgId, topic, null, d, fileType, from, null, timestamp, customData);
					try {
						for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.messageSentFromOtherDevice(m);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				break;
			default:
				Log.i(LOG_TAG, "Wrong message type: " + msg_type);
				break;
			}		
		}

        public void messagePublished(String msgId, String extraPayload) {
			//Log.d(LOG_TAG, "message published: " + msgid);
			if(msgId.startsWith("-")) {
				//signal data, don't call the callback method
				/*
				String signalType = msgId.substring(1, msgId.lastIndexOf("-"));
				if(liveSignalMap.get(signalType) == null) {
					
				}
				*/
			} else {
				if(callbacks != null && !"clearSession".equals(msgId)) {
					long timestamp = -1;
					boolean isInBlacklist = false;
					String keyword = null;
					if(extraPayload != null) {
						try {
							JSONObject json = new JSONObject(extraPayload);
							String mid = json.optString("msgId", null);
							Long tempTS = json.optLong("timestamp", -1);
							int tempIsInBlacklist = json.optInt("bl", 0);
							String tempKeyword = json.optString("sk");
							if(msgId.equals(mid)) {
								timestamp = tempTS;
								isInBlacklist = tempIsInBlacklist == 1 ? true : false;
								keyword = (tempKeyword != null && !"".equals(tempKeyword)) ? tempKeyword : "";
							}
						} catch (JSONException e) {
							// mute
						}
					}
					
					if(msgId.startsWith("D")) {
						try {
							if(deskCallback != null) {
								deskCallback.messageSent(msgId, timestamp, null);
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
					} else {
					    AnIMMessageSentCallbackData data;
					    if (keyword != null && !"".equals(keyword)) {
					        ArrownockException ex = new ArrownockException(keyword, ArrownockException.IM_SENSITIVE);
                            data = new AnIMMessageSentCallbackData(true, ex, msgId, timestamp);
                        } else if (isInBlacklist) {
                            ArrownockException ex = new ArrownockException("The client is in blacklist", ArrownockException.IM_IN_BLACKLIST);
                            data = new AnIMMessageSentCallbackData(true, ex, msgId, timestamp);
                        } else {
                            data = new AnIMMessageSentCallbackData(false, null, msgId, timestamp);
                        }
						try {
							for(IAnIMCallback c : callbacks) {
								if(c != null) {
									c.messageSent(data);
								}
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		public void failPublish(String msgId, Throwable exception) {
			//Log.d(LOG_TAG, "failed to publish message: " + msgid, exception);
			if(liveSignalMap.get(msgId) == null) {
				if(msgId.startsWith("D")) {
					try {
						if(deskCallback != null) {
							deskCallback.messageSent(msgId, -1, new ArrownockException(exception.getMessage(), ArrownockException.IM_FAILED_PUBLISH));
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				} else {
					if(callbacks != null) {
						AnIMMessageSentCallbackData data = new AnIMMessageSentCallbackData(true, new ArrownockException(exception.getMessage(), ArrownockException.IM_FAILED_PUBLISH), msgId, -1);
						try {
							for(IAnIMCallback c : callbacks) {
								if(c != null) {
									c.messageSent(data);
								}
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		public void topicSubscribed(String topicName, int qos) {
			/*
			if(callback != null) {
				final AnIMStatusUpdateCallbackData data = new AnIMStatusUpdateCallbackData(AnIMStatus.ONLINE, null);
				//when anIM connect successfully, report device_id to im_client_tokens 
                if (Constants.DM_ENABLED) {
                    if (!"".equals(clientIdForReport)){
                        reportDeviceIdToIM(clientIdForReport);
                    }
                }
				try {
					Runnable callbackThread = new Runnable(){
						@Override
						public void run() {
							callback.statusUpdate(data);
						}
					};
					new Thread(callbackThread).start();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			*/
		}

		public void failSubscribe(String topicName, Throwable exception) {
			/*
			if(callback != null) {
				final AnIMStatusUpdateCallbackData data = new AnIMStatusUpdateCallbackData(getCurrentStatus(), new ArrownockException(exception.getMessage(), ArrownockException.IM_FAILED_CONNECT));
				try {
					Runnable callbackThread = new Runnable(){
						@Override
						public void run() {
							callback.statusUpdate(data);
						}
					};
					new Thread(callbackThread).start();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}	
			*/
		}
	}
	
	String convertStreamToString(InputStream is) {
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
	
	public String generateMsgID(){
		Random rand = new Random();
		int min = 1;
		int max = 65535;
		int id = rand.nextInt(max - min + 1) + min;
		long ms = (new Date()).getTime();
		String clientId = getClientId();
		String plainStr = clientId + ms + id;
		String msgid = Long.toString(hash(plainStr));
		if (msgid.startsWith("-")) {
			msgid = msgid.replace("-", "");
		}
		return msgid;
	}

	public String getClientId() {
		return clientId;
//		return getFromLocalStorage(androidContext, PREF_CLIENT_ID);
	}
	
	public void saveToLocalStorage(final Context androidContext, final String key, final String value){
		Editor editor = androidContext.getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE).edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public String getFromLocalStorage(final Context androidContext, final String key) {
		SharedPreferences pref = androidContext.getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE);
		return pref.getString(key, "");
	}
	
	public void removeFromLocalStorage(final Context androidContext, final String key) {
		SharedPreferences pref = androidContext.getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE);
		pref.edit().remove(key).commit();
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
	
	//Check if we are online
	private boolean isNetworkAvailable(Context context) {
		NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			if (info == null) {
			return false;
		}
		return info.isConnected();
	}
	
	public AnIMStatus getCurrentStatus() {
		if(mqttAgent != null) {
			if(mqttAgent.getStatus().equals(MQTTConnectionStatus.Connected)) {
				return AnIMStatus.ONLINE;
			}
		}
		return AnIMStatus.OFFLINE;
	}
	
	private long hash(String str)
	{
		long hash = 5381;

		int length = str.length();
		for (int i = 0; i < length; i++ ) {
			hash = ((hash << 5) + hash) + str.charAt(i); /* hash * 33 + c */
		}

		return hash;
	}
	
	private String getAPIHost(Context context) {
		String api = getFromLocalStorage(context, PREF_IM_API);
		return "".equals(api)? API_BASE_URL : api;
	}
	
	private String getDSHost(Context context) {
		String ds = getFromLocalStorage(context, PREF_IM_DS);
		return "".equals(ds)? IM_DISPATCH_BASE_URL : ds;
	}
	
	/**
	 * Seed array which is used to construct characters array through bytes array which contains array index information.
	 */
	private final char[] sourceData = {'0','1','2','3','4','5','6','7','8','9',
								       'A','B','C','D','E','F','G','H','I','J','K','L','M',
								       'N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	
	public String calculateSessionId(Set<String> clientIds) {
		String clientsStr = setJoin(clientIds, "-");
		String encryptDevicedID = Encrypt("SHA1", clientsStr);
		return encryptDevicedID;
	}
	
	public String setJoin(Set<String> clientIds, String glue) {
		Object[] clientsArray = clientIds.toArray();
		StringBuffer sb = new StringBuffer();
		int length = clientsArray.length;
		if (length == 0) {
			return null;
		}
		Arrays.sort(clientsArray);
		sb.append(clientsArray[0]);
		for (int i = 1; i<length; i++) {
			sb = sb.append(glue).append(clientsArray[i]);
		}
		
		return sb.toString();
	}
	
	private String Encrypt(String encryptType, String str) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(encryptType);
			messageDigest.update(str.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			return null;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		
		final byte[] digest = messageDigest.digest();
		return bytesToCharacters(digest);
	}
	
	/**
	 * Interpret the bytes array to a String array which are from sourceData char array.
	 * @param resultBytes
	 * @return the converted characters array, length is 20
	 */
	private String bytesToCharacters( byte[] resultBytes ){
	    char result;
	    StringBuffer sb = new StringBuffer();
	    int index = 0;
	    int offset = 0;
	    for(int i = 0; i < resultBytes.length; i++){
	    	index = resultBytes[i];
	    	if( index < 0 ) 
	    		index = Math.abs(index);
		    offset = index / 36;

		    if( offset < 1 ) {
			    result = sourceData[index];
		    } else { 
		    	result = sourceData[ (index + offset) % 36 ];
		    }
		    sb.append(result);
	    }
	    String str =  sb.toString();
	    return str;
	}
	
	private String getServerCert() {
		return Constants.SSL_SERVER_CERT;
	}
	
	private String getClientCert() {
		return Constants.SSL_CLIENT_CERT;
	}
	
	private String getClientKey() {
		return Constants.SSL_CLIENT_KEY;
	}
	
	public void setSignalControllerCallback(ISignalController.Callbacks callback) {
		this.signalControllerCallback = callback;
	}
	
	public void createLiveSession(final String appKey, final String owner, final List<String> partyIds, final String type) {
		Runnable createLiveSessionThread = new Runnable(){
			public void run() {
				try {
					HttpURLConnection urlConnection = null;
					try {
						if (secureConnection) {
							URL url = new URL(Constants.HTTPS + getAPIHost(context) + "/" + API_VERSION + "/" + LIVE_CREATE_SESSION_ENDPOINT + "?key=" + appKey.trim());
							urlConnection = (HttpsURLConnection) url.openConnection();
							((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
							((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
						} else {
							URL url = new URL(Constants.HTTP + getAPIHost(context) + "/" + API_VERSION + "/" + LIVE_CREATE_SESSION_ENDPOINT + "?key=" + appKey.trim());
							urlConnection = (HttpURLConnection) url.openConnection();
						}
						urlConnection.setRequestMethod("POST");
						urlConnection.setDoInput(true);
						urlConnection.setDoOutput(true);
						
						String parties = setJoin(new TreeSet<String>(partyIds), ",");
					    try {
					    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						    nameValuePairs.add(new BasicNameValuePair("owner", owner));
						    nameValuePairs.add(new BasicNameValuePair("parties", parties));
						    nameValuePairs.add(new BasicNameValuePair("type", type));
						    
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
									String errorDetail = null;
									if(meta != null) {
										errorDetail = meta.optString("message", null);
									} 
									if(errorDetail != null) {
										signalControllerCallback.onSessionCreated(null, null, null, new ArrownockException(errorDetail, ArrownockException.LIVE_FAILED_CREATE_SESSION));
									} else {
										signalControllerCallback.onSessionCreated(null, null, null, new ArrownockException("Internal server error", ArrownockException.LIVE_FAILED_CREATE_SESSION));
									}
							    } else {
							    	InputStream in = new BufferedInputStream(urlConnection.getInputStream());
								    String s = convertStreamToString(in);
									JSONObject json = new JSONObject(s);
									JSONObject data = json.getJSONObject("response");
									if(data != null) {
										JSONObject ls = data.getJSONObject("live_session");
										if(ls != null) {
											String sessionId = ls.getString("id");
											JSONArray p = ls.getJSONArray("parties");
											List<String> ids = new ArrayList<String>();
											for(int i = 0; i<p.length(); i++) {
												ids.add(p.getString(i));
											}
											try {
												signalControllerCallback.onSessionCreated(sessionId, ids, type, null);
											} catch (Exception e) {
												//e.printStackTrace();
											}
										}
									}
							    }
							} catch (JSONException e) {
								throw new ArrownockException(e.getMessage(), ArrownockException.LIVE_FAILED_CREATE_SESSION);
							}
					    } catch (IOException e) {
					    	throw new ArrownockException(e.getMessage(), ArrownockException.LIVE_FAILED_CREATE_SESSION);
					    } 
					} catch (Exception e) {
						if (e instanceof ArrownockException) {
							throw (ArrownockException) e;
						} else {
							throw new ArrownockException(e.getMessage(), ArrownockException.LIVE_FAILED_CREATE_SESSION);
						}
					}	
					finally {
						if (urlConnection != null) {
							urlConnection.disconnect();
						}   	
					}
				} catch(Exception e) {
					if(e instanceof ArrownockException) {
					    if (signalControllerCallback != null) {
					        signalControllerCallback.onSessionCreated(null, null, null, (ArrownockException)e);
					    }
					}
				}
			}
		};
		Thread thread = new Thread(createLiveSessionThread);
		thread.start();
	}
	
	public void validateLiveSession(final String appKey, final String sessionId) {
		Runnable validateLiveSessionThread = new Runnable(){
			public void run() {
				try {
					HttpURLConnection urlConnection = null;
					try {
						if (secureConnection) {
							URL url = new URL(Constants.HTTPS + getAPIHost(context) + "/" + API_VERSION + "/" + LIVE_VALIDATE_SESSION_ENDPOINT + "?key=" + appKey.trim() + "&id=" + sessionId);
							urlConnection = (HttpsURLConnection) url.openConnection();
							((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
							((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
						} else {
							URL url = new URL(Constants.HTTP + getAPIHost(context) + "/" + API_VERSION + "/" + LIVE_VALIDATE_SESSION_ENDPOINT + "?key=" + appKey.trim() + "&id=" + sessionId);
							urlConnection = (HttpURLConnection) url.openConnection();
						}
					    try {
						    int statusCode = urlConnection.getResponseCode();
						    try {
							    if (statusCode != 200) {
								    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
								    String s = convertStreamToString(es);
									JSONObject json = new JSONObject(s);
									JSONObject meta = json.getJSONObject("meta");
									try {
									    if (signalControllerCallback != null) {
									        signalControllerCallback.onSessionValidated(false, null, null, null, null);
									    }
									} catch (Exception e) {
										e.printStackTrace();
									}
							    } else {
							    	InputStream in = new BufferedInputStream(urlConnection.getInputStream());
								    String s = convertStreamToString(in);
									JSONObject json = new JSONObject(s);
									JSONObject data = json.getJSONObject("response");
									if(data != null) {
										JSONObject ls = data.getJSONObject("live_session");
										if(ls != null) {
											String sessionId = ls.getString("id");
											String type = ls.getString("type");
											boolean isExpired = ls.getBoolean("expired");
											SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
											String dateStr = ls.optString("created_at", null);
											String dateStr1 = dateStr.substring(0, dateStr.length()-1) + "+0000";
											Date createdAt = format.parse(dateStr1);
											JSONArray p = ls.getJSONArray("parties");
											List<String> partyIds = new ArrayList<String>();
											for(int i = 0; i<p.length(); i++) {
												String id = p.getString(i);
												if(id != null && !id.equals(getClientId())) {
													partyIds.add(id);
												}
											}
											try {
											    if (signalControllerCallback != null) {
											        signalControllerCallback.onSessionValidated(!isExpired, sessionId, partyIds, type, createdAt);
											    }
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}
							    }
							} catch (JSONException e) {
								throw new ArrownockException(e.getMessage(), ArrownockException.LIVE_FAILED_VALIDATE_SESSION);
							}
					    } catch (IOException e) {
					    	throw new ArrownockException(e.getMessage(), ArrownockException.LIVE_FAILED_VALIDATE_SESSION);
					    } 
					} catch (Exception e) {
						if (e instanceof ArrownockException) {
							throw (ArrownockException) e;
						} else {
							throw new ArrownockException(e.getMessage(), ArrownockException.LIVE_FAILED_VALIDATE_SESSION);
						}
					}	
					finally {
						if (urlConnection != null) {
							urlConnection.disconnect();
						}   	
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		};
		Thread thread = new Thread(validateLiveSessionThread);
		thread.start();
	}
	
	public void terminateLiveSession(final String appKey, final String sessionId) {
		Runnable terminateLiveSessionThread = new Runnable(){
			public void run() {
				try {
					HttpURLConnection urlConnection = null;
					try {
						if (secureConnection) {
							URL url = new URL(Constants.HTTPS + getAPIHost(context) + "/" + API_VERSION + "/" + LIVE_TERMINATE_SESSION_ENDPOINT + "?key=" + appKey.trim());
							urlConnection = (HttpsURLConnection) url.openConnection();
							((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
							((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
						} else {
							URL url = new URL(Constants.HTTP + getAPIHost(context) + "/" + API_VERSION + "/" + LIVE_TERMINATE_SESSION_ENDPOINT + "?key=" + appKey.trim());
							urlConnection = (HttpURLConnection) url.openConnection();
						}
						urlConnection.setRequestMethod("POST");
						urlConnection.setDoInput(true);
						urlConnection.setDoOutput(true);
						
					    try {
					    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						    nameValuePairs.add(new BasicNameValuePair("id", sessionId));
						    
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
							    } else {
							    	InputStream in = new BufferedInputStream(urlConnection.getInputStream());
								    String s = convertStreamToString(in);
							    }
							} catch (JSONException e) {
								throw new ArrownockException(e.getMessage(), ArrownockException.LIVE_FAILED_TERMINATE_SESSION);
							}
					    } catch (IOException e) {
					    	throw new ArrownockException(e.getMessage(), ArrownockException.LIVE_FAILED_TERMINATE_SESSION);
					    } 
					} catch (Exception e) {
						if (e instanceof ArrownockException) {
							throw (ArrownockException) e;
						} else {
							throw new ArrownockException(e.getMessage(), ArrownockException.LIVE_FAILED_TERMINATE_SESSION);
						}
					}	
					finally {
						if (urlConnection != null) {
							urlConnection.disconnect();
						}   	
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		};
		Thread thread = new Thread(terminateLiveSessionThread);
		thread.start();
	}

	@Override
	public void getFullTopicLogAsync(final String appKey, final String topicId, final String clientId, final int limit, final long timestamp, final IAnIMHistoryCallback callback) {
		Runnable getTopicLogThread = new Runnable(){
			public void run() {
				try{
					HttpURLConnection urlConnection = null;
					try {
						StringBuffer path = new StringBuffer();
						path.append(AnMessageUtility.this.getAPIHost(context));
						path.append("/" + API_VERSION + "/" + FULL_HISTORY_ENDPOINT + "?key=" + appKey.trim());
						path.append("&topic_id=" + topicId);
						path.append("&sdk=true");
						if(clientId != null) {
							path.append("&me=" + clientId);
						}
						path.append("&device_type=mobile");
						if(limit > 0) {
							path.append("&limit=" + limit);
						}
						if(timestamp > 0) {
							path.append("&timestamp=" + timestamp);
						}
						path.append("&b=1");
						if (secureConnection) {
							URL url = new URL(Constants.HTTPS + path.toString());
							urlConnection = (HttpsURLConnection) url.openConnection();
							((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
							((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
						} else {
							URL url = new URL(Constants.HTTP + path.toString());
							urlConnection = (HttpURLConnection) url.openConnection();
						}
					    try {
					    	urlConnection.connect();
						    int statusCode = urlConnection.getResponseCode();
						    try {
							    if (statusCode == 200) {
								    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
								    String s = convertStreamToString(in);
									JSONObject json = new JSONObject(s);
									JSONObject data = json.getJSONObject("response");
									JSONObject meta = json.getJSONObject("meta");
									if(data != null) {
										JSONArray jsonArray = data.optJSONArray("messages");
										List<AnIMMessage> logs = new ArrayList<AnIMMessage>();
										for (int i=0; i<jsonArray.length(); i++) {
											JSONObject obj = jsonArray.getJSONObject(i);
											if(obj != null) {
												String type = obj.optString("content_type");
												String msgId = obj.optString("msg_id");
												String tid = obj.optString("topic_id");
												String from = obj.optString("from");
												String message = obj.optString("message");
												long ts = obj.optLong("timestamp");
												JSONObject customObj = obj.optJSONObject("customData");
												Map<String, String> customData = null;
												if (customObj != null) {
													Iterator<?> nameItr = customObj.keys();
													customData = new HashMap<String, String>();
													while(nameItr.hasNext()) {
														String key = (String)nameItr.next();
														Object value = customObj.get(key);
														if(value != null && value instanceof String) {
															customData.put(key, ANEmojiUtil.stringConvertToEmoji((String)value));
														} else {
															try {
																customData.put(key, String.valueOf(value));
															} catch(Exception e) {
																e.printStackTrace();
															}
														}
													}
												}
												
												if("text".equals(type)) {
													message = ANEmojiUtil.stringConvertToEmoji(message);
													AnIMMessage m = new AnIMMessage(AnIMMessageType.AnIMTextMessage, msgId, tid, message, null, null, from, null, ts, customData);
													logs.add(m);
												} else if("binary".equals(type)) {
													String fileType = obj.optString("fileType");
													byte[] d = null;
													if(message != null && message.length() > 0) {
														d = ANBase64.decode(message);
													}
													AnIMMessage m = new AnIMMessage(AnIMMessageType.AnIMBinaryMessage, msgId, tid, null, d, fileType, from, null, ts, customData);
													logs.add(m);
												}
											}
										}
										if(callback != null) {
											callback.onSuccess(logs, -1);
										}
									} else {
										throw new ArrownockException("Unable to get full topic history. ", ArrownockException.IM_FAILED_GET_TOPIC_HISTORY);
									}
							    } else {
								    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
								    String s = convertStreamToString(es);
									JSONObject json = new JSONObject(s);
									JSONObject meta = json.getJSONObject("meta");
									throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
							    }
							} catch (JSONException e) {
								throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
							}
					    } catch (IOException e) {
					    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
					    } 
					} catch (Exception e) {
						if (e instanceof ArrownockException) {
							throw (ArrownockException) e;
						} else {
							throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
						}
					}	
					finally {
						if (urlConnection != null) {
							urlConnection.disconnect();
						}   	
					}	
				} catch(ArrownockException ex){
					if(callback != null) {
						callback.onError(ex);
					}
				}
			}
		};
		Thread thread = new Thread(getTopicLogThread);
		thread.start();	
	}

	@Override
	public void setPushNotificationAsync(final String clientId, final boolean isEnable, final int type, final List<String> topicIds, final IAnIMPushNotificationSettingsCallback callback) {
		Runnable setPushNotificationThread = new Runnable(){
			public void run() {
				HttpURLConnection urlConnection = null;
				try {
					StringBuffer path = new StringBuffer();
					path.append(AnMessageUtility.this.getAPIHost(context));
					path.append("/" + API_VERSION + "/" + PUSH_SETTINGS_ENDPOINT + "?key=" + appKey.trim());
					if (secureConnection) {
						URL url = new URL(Constants.HTTPS + path.toString());
						urlConnection = (HttpsURLConnection) url.openConnection();
						((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
						((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
					} else {
						URL url = new URL(Constants.HTTP + path.toString());
						urlConnection = (HttpURLConnection) url.openConnection();
					}
					urlConnection.setRequestMethod("POST");
					urlConnection.setDoInput(true);
					urlConnection.setDoOutput(true);
					
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
			    	String date = format.format(new Date());
			    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				    nameValuePairs.add(new BasicNameValuePair("type", String.valueOf(type)));
				    nameValuePairs.add(new BasicNameValuePair("client", clientId));
				    nameValuePairs.add(new BasicNameValuePair("date", date));
				    nameValuePairs.add(new BasicNameValuePair("service", "anpush"));
				    String signatureString = null;
				    switch (type) {
				    case 1:
				    case 2:
				    case 3:
				    case 6:
				    	nameValuePairs.add(new BasicNameValuePair("value", String.valueOf(isEnable)));
				    	signatureString = "/" + API_VERSION + "/" + PUSH_SETTINGS_ENDPOINT + "client=" + clientId + "&date=" + date + "&key=" + appKey + "&service=anpush" + "&type=" + type + "&value=" + isEnable;
				    	break;
				    case 4:
				    case 5:
				    	StringBuffer buffer = new StringBuffer();
				    	for(String topicId : topicIds) {
				    		buffer.append(topicId).append(",");
				    	}
				    	String topicParam = "";
				    	if(buffer.length() > 1) {
				    		topicParam = buffer.substring(0, buffer.length() - 1);
				    	}
				    	nameValuePairs.add(new BasicNameValuePair("value", topicParam));
				    	signatureString = "/" + API_VERSION + "/" + PUSH_SETTINGS_ENDPOINT + "client=" + clientId + "&date=" + date + "&key=" + appKey + "&service=anpush" + "&type=" + type + "&value=" + topicParam;
				    	break;
				    }
				    String HMAC_SHA1_ALGORITHM = "HmacSHA1";
				    String signature = null;
				    
				    try {         
				    	Key signingKey = new SecretKeySpec(SIG_SEC.getBytes(), HMAC_SHA1_ALGORITHM);
				    	Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
				    	mac.init(signingKey);
				    	byte[] rawHmac = mac.doFinal(signatureString.getBytes());
				    	signature = Base64.encodeToString(rawHmac, Base64.NO_WRAP);
				    	nameValuePairs.add(new BasicNameValuePair("signature", signature));
				    } catch (Exception e) {
				    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_BIND_SERVICE);
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
							if(callback != null) {
								callback.onSuccess();
							}
						} else {
							InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
							String s = convertStreamToString(es);
							JSONObject json = new JSONObject(s);
							JSONObject meta = json.getJSONObject("meta");
							throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
						}
					} catch (JSONException e) {
						throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CHAT_HISTORY);
					}
				} catch (Exception e) {
					if (e instanceof ArrownockException) {
						if(callback != null) {
							callback.onError((ArrownockException)e);
						}
					} else {
						if(callback != null) {
							callback.onError(new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_GET_CHAT_HISTORY));
						}
					}
				} finally {
					if (urlConnection != null) {
						urlConnection.disconnect();
					}   	
				}
			}
		};
		Thread thread = new Thread(setPushNotificationThread);
		thread.start();	
	}

	@Override
    @Deprecated
    public void removeTopic(final String appKey, final String topic) {
        Runnable removeTopicThread = new Runnable(){
            public void run() {
                try{
                    removeTopicUtil(appKey, topic, false, null);
                    if(callbacks != null) {
                        AnIMRemoveTopicCallbackData data = new AnIMRemoveTopicCallbackData(false, null, topic);
                        for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.removeTopic(data);
							}
						}
                        
                    }
                } catch(ArrownockException ex){
                    if(callbacks != null) {
                        AnIMRemoveTopicCallbackData data = new AnIMRemoveTopicCallbackData(true, ex, null);
                        for(IAnIMCallback c : callbacks) {
							if(c != null) {
								c.removeTopic(data);
							}
						}
                    }
                }
            }
        };
        Thread thread = new Thread(removeTopicThread);
        thread.start();
    }
	
	@Override
    public void removeTopic(final String appKey, final String topic, final boolean isNeedNotice, final String currentClientId, final IAnIMTopicCallback callback) {
        Runnable removeTopicThread = new Runnable(){
            public void run() {
                try{
                    long[] time = removeTopicUtil(appKey, topic, isNeedNotice, currentClientId);
                    if(callback != null) {
                        callback.onSuccess(topic, time[0], time[1]);
                    }
                } catch(ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(removeTopicThread);
        thread.start();
    }
	
	long[] removeTopicUtil(String appKey, String topicId, boolean isNeedNotice, String currentClientId) throws ArrownockException {
		HttpURLConnection urlConnection = null;
		try {
			if (secureConnection) {
				URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + REMOVE_TOPIC_ENDPOINT);
				urlConnection = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
				((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
			} else {
				URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + REMOVE_TOPIC_ENDPOINT);
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			
		    try {
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			    nameValuePairs.add(new BasicNameValuePair("id", topicId));
			    if (isNeedNotice) {
                    nameValuePairs.add(new BasicNameValuePair("is_need_notice", "true"));
                    if (currentClientId != null && currentClientId.trim().length() != 0) {
                        nameValuePairs.add(new BasicNameValuePair("current_client_id", currentClientId));
                    }
                    String msg_id = generateMsgID();
                    nameValuePairs.add(new BasicNameValuePair("msg_id", msg_id));
                }
			    
			    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
                String date = format.format(new Date());
                nameValuePairs.add(new BasicNameValuePair("date", date));
                nameValuePairs.add(new BasicNameValuePair("key", appKey));
                String signature = getSignature("/" + API_VERSION + "/" + REMOVE_TOPIC_ENDPOINT, nameValuePairs);
                nameValuePairs.add(new BasicNameValuePair("signature", signature));
			    
			    OutputStream out = urlConnection.getOutputStream();
			    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			    writer.write(getQuery(nameValuePairs));
			    writer.close();
			    out.close();
			    
			    int statusCode = urlConnection.getResponseCode();
			    try {
				    if (statusCode == 200) {
                        long[] results = new long[2];
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        String s = convertStreamToString(in);
                        JSONObject json = new JSONObject(s);
                        // JSONObject meta = json.getJSONObject("meta");
                        JSONObject data = json.getJSONObject("response");
                        if (data != null && data.getJSONObject("topic") != null) {
                            JSONObject sub = data.getJSONObject("topic");
                            if (sub != null && sub.has("created_at")) {
                                results[0] = sub.getLong("created_at");
                            } else {
                                throw new ArrownockException("Unable to remove topic. Failed to acquire created_at.",
                                        ArrownockException.IM_FAILED_REMOVE_TOPIC);
                            }
                            if (sub != null && sub.has("updated_at")) {
                                results[1] = sub.getLong("updated_at");
                            } else {
                                throw new ArrownockException("Unable to remove topic. Failed to acquire updated_at.",
                                        ArrownockException.IM_FAILED_REMOVE_TOPIC);
                            }
                            return results;
                        } else {
                            throw new ArrownockException("Unable to remove topic. Failed to acquire topic.",
                                    ArrownockException.IM_FAILED_REMOVE_TOPIC);
                        }
				    } else {
					    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
					    String s = convertStreamToString(es);
						JSONObject json = new JSONObject(s);
						JSONObject meta = json.getJSONObject("meta");
						throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_REMOVE_TOPIC);
				    }
				} catch (JSONException e) {
					throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_REMOVE_TOPIC);
				}
		    } catch (IOException e) {
		    	throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_REMOVE_TOPIC);
		    } 
		} catch (Exception e) {
			if (e instanceof ArrownockException) {
				throw (ArrownockException) e;
			} else {
				throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_REMOVE_TOPIC);
			}
		}	
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}   	
		}
	}
	
	@Override
	public void addBlacklistAsync(final String appKey, final String currentClientId, final String targetClientId, final IAnIMBlacklistCallback callback) {
	    Runnable addBlacklistThread = new Runnable(){
            public void run() {
                try {
                    addBlacklistUtil(appKey, currentClientId, targetClientId);
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
        Thread thread = new Thread(addBlacklistThread);
        thread.start();
	}
	
    void addBlacklistUtil(String appKey, String currentClientId, String targetClientId) throws ArrownockException {
        HttpURLConnection urlConnection = null;
        try {
            if (secureConnection) {
                URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + ADD_BLACKLIST_ENDPOINT);
                urlConnection = (HttpsURLConnection) url.openConnection();
                ((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
                ((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
            } else {
                URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + ADD_BLACKLIST_ENDPOINT);
                urlConnection = (HttpURLConnection) url.openConnection();
            }
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("current_client_id", currentClientId));
                nameValuePairs.add(new BasicNameValuePair("target_client_id", targetClientId));
                
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
                String date = format.format(new Date());
                nameValuePairs.add(new BasicNameValuePair("date", date));
                nameValuePairs.add(new BasicNameValuePair("key", appKey));
                String signature = getSignature("/" + API_VERSION + "/" + ADD_BLACKLIST_ENDPOINT, nameValuePairs);
                nameValuePairs.add(new BasicNameValuePair("signature", signature));
                
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
                        throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_ADD_BLACKLIST);
                    }
                } catch (JSONException e) {
                    throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_ADD_BLACKLIST);
                }
            } catch (IOException e) {
                throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_ADD_BLACKLIST);
            } 
        } catch (Exception e) {
            if (e instanceof ArrownockException) {
                throw (ArrownockException) e;
            } else {
                throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_ADD_BLACKLIST);
            }
        }   
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }       
        }
    }
	
	@Override
	public void removeBlacklistAsync(final String appKey, final String currentClientId, final String targetClientId, final IAnIMBlacklistCallback callback) {
	    Runnable removeBlacklistThread = new Runnable(){
            public void run() {
                try {
                    removeBlacklistUtil(appKey, currentClientId, targetClientId);
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
        Thread thread = new Thread(removeBlacklistThread);
        thread.start();
    }
	
	void removeBlacklistUtil(String appKey, String currentClientId, String targetClientId) throws ArrownockException {
        HttpURLConnection urlConnection = null;
        try {
            if (secureConnection) {
                URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + REMOVE_BLACKLIST_ENDPOINT);
                urlConnection = (HttpsURLConnection) url.openConnection();
                ((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
                ((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
            } else {
                URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + REMOVE_BLACKLIST_ENDPOINT);
                urlConnection = (HttpURLConnection) url.openConnection();
            }
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("current_client_id", currentClientId));
                nameValuePairs.add(new BasicNameValuePair("target_client_id", targetClientId));
                
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
                String date = format.format(new Date());
                nameValuePairs.add(new BasicNameValuePair("date", date));
                nameValuePairs.add(new BasicNameValuePair("key", appKey));
                String signature = getSignature("/" + API_VERSION + "/" + REMOVE_BLACKLIST_ENDPOINT, nameValuePairs);
                nameValuePairs.add(new BasicNameValuePair("signature", signature));
                
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
                        throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_REMOVE_BLACKLIST);
                    }
                } catch (JSONException e) {
                    throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_REMOVE_BLACKLIST);
                }
            } catch (IOException e) {
                throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_REMOVE_BLACKLIST);
            } 
        } catch (Exception e) {
            if (e instanceof ArrownockException) {
                throw (ArrownockException) e;
            } else {
                throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_REMOVE_BLACKLIST);
            }
        }   
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }       
        }
    }
	
	@Override
	public void listBlacklistsAsync(final String appKey, final String currentClientId, final IAnIMListBlacklistsCallback callback) {
	    Runnable listBlacklistsThread = new Runnable(){
            public void run() {
                try {
                    List<String> clients = listBlacklistsUtil(appKey, currentClientId);
                    if (callback != null) {
                        callback.onSuccess(clients);
                    } 
                } catch (ArrownockException ex){
                    if(callback != null) {
                        callback.onError(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(listBlacklistsThread);
        thread.start(); 
    }
	
	List<String> listBlacklistsUtil(String appKey, String currentClientId) throws ArrownockException {
        HttpURLConnection urlConnection = null;
        try {
            if (secureConnection) {
                URL url = new URL(Constants.HTTPS + this.getAPIHost(context) + "/" + API_VERSION + "/" + LIST_BLACKLIST_ENDPOINT + "?key=" + appKey.trim() + "&current_client_id=" + currentClientId);
                urlConnection = (HttpsURLConnection) url.openConnection();
                ((HttpsURLConnection)urlConnection).setHostnameVerifier(hostnameVerifier);
                ((HttpsURLConnection)urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
            } else {
                URL url = new URL(Constants.HTTP + this.getAPIHost(context) + "/" + API_VERSION + "/" + LIST_BLACKLIST_ENDPOINT + "?key=" + appKey.trim() + "&current_client_id=" + currentClientId);
                urlConnection = (HttpURLConnection) url.openConnection();
            }
            
            try {
                urlConnection.connect();
                int statusCode = urlConnection.getResponseCode();
                try {
                    if (statusCode == 200) {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        String s = convertStreamToString(in);
                        JSONObject json = new JSONObject(s);
                        JSONObject data = json.getJSONObject("response");
                        if(data != null) {
                            JSONArray jsonArray = data.optJSONArray("blacklist");
                            List<String> clients = new ArrayList<String>();
                            for (int i=0; i<jsonArray.length(); i++) {
                                clients.add(jsonArray.getString(i));
                            }
                            return clients;
                        } else {
                            throw new ArrownockException("Unable to get blacklists.", ArrownockException.IM_FAILED_LIST_BLACKLISTS);
                        }
                    } else {
                        InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
                        String s = convertStreamToString(es);
                        JSONObject json = new JSONObject(s);
                        JSONObject meta = json.getJSONObject("meta");
                        throw new ArrownockException(meta.getString("message"), ArrownockException.IM_FAILED_LIST_BLACKLISTS);
                    }
                } catch (JSONException e) {
                    throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_LIST_BLACKLISTS);
                }
            } catch (IOException e) {
                throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_LIST_BLACKLISTS);
            } 
        } catch (Exception e) {
            throw new ArrownockException(e.getMessage(), ArrownockException.IM_FAILED_LIST_BLACKLISTS);
        }   
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }       
        }
    }
	
	private String getDeviceId() {
		String id = this.getFromLocalStorage(context, "ANIM_DEVICE_ID");
		if("".equals(id)) {
			id = null;
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
					//return null;
				} else {
					id = deviceId + serial + androidId;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(id == null) {
				id = UUID.randomUUID().toString();
			}
			
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
			this.saveToLocalStorage(context, "ANIM_DEVICE_ID", id);
		}
		return id;
	}
	
	private static Map<String, String> jsonToMap(String jsonStr) throws JSONException{
        JSONObject jsonObj = new JSONObject(jsonStr);
        Iterator<String> nameItr = jsonObj.keys();
        String name;
        Map<String, String> outMap = new HashMap<String, String>();
        while (nameItr.hasNext()) {
            name = nameItr.next();
            outMap.put(name, jsonObj.optString(name, ""));
        }
        return outMap;
    }
	
   private void convertCustomDataToEmoji(Map<String, String> cdata) {
        if(cdata != null && !cdata.isEmpty()) {
            Set<String> keys = cdata.keySet();
            if(keys != null) {
                Iterator<String> itr = keys.iterator();
                while(itr.hasNext()) {
                    String key = itr.next();
                    String val = cdata.get(key);
                    if(val != null) {
                        try{
                            val = ANEmojiUtil.stringConvertToEmoji(val);
                            cdata.put(key, val);
                        }catch(Exception e){
                        }
                    }
                }
            }
        }
    }
   
   private Map<String, String> getTopicCustomData(Map<String, String> customData) throws Exception{
	   if(customData == null) {
		   return null;
	   }
       String customDataStr = customData.get("customData");
       Map<String, String> topicCustomData = null;
       if (customDataStr != null  && customDataStr.length() > 0) {
           JSONObject customObj = new JSONObject(customDataStr);
           if (customObj != null) {
               Iterator<?> nameItr = customObj.keys();
               topicCustomData = new HashMap<String, String>();
               while(nameItr.hasNext()) {
                   String key = (String)nameItr.next();
                   String value = customObj.getString(key);
                   topicCustomData.put(key, value);
               }
           }
       }
       return topicCustomData;
   }
}
