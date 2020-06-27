package com.arrownock.im;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.callback.AnIMMessageSentCallbackData;
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
import com.arrownock.internal.util.EmojiFilter;
import com.arrownock.internal.util.TokenHelper;
import com.arrownock.push.ANBase64;

public class AnIM implements ISignalController {
	private String appKey = null;
	private Context androidContext;
	private List<IAnIMCallback> callbacks = null;
	private boolean secureConnection = true;
	private IAnIMUtility util = null;
	private final long sizeLimitation = Long.parseLong(Constants.IM_SIZE_LIMIT);
	private String connectingClientId = null;
	private IAnDeskCallback deskCallback = null;
	private IAnGroupediaCallback groupediaCallback = null;
	
	public AnIM(Context androidContext) throws ArrownockException {
		this.androidContext = androidContext;
		util = new AnMessageUtility();
		util.setContext(androidContext);
		callbacks = new ArrayList<IAnIMCallback>();
		util.setCallbacks(callbacks);
		try{
		    ApplicationInfo ai = androidContext.getPackageManager().getApplicationInfo(androidContext.getPackageName(), PackageManager.GET_META_DATA);
		    Bundle bundle = ai.metaData;
		    if (bundle != null) {
		    	setAppKey(bundle.getString(Constants.APP_KEY));
		    }
		} catch (Exception e) {
		    throw new ArrownockException("Failed to initialize SDK.", e, ArrownockException.IM_FAILED_INITIALIZE);
		}
		if (appKey == null || "".equals(appKey.trim())) {
			throw new ArrownockException("Invalid value of " + Constants.APP_KEY, ArrownockException.IM_INVALID_APP_KEY);
		}
	}
	
	public AnIM(Context androidContext, String AppKey) throws ArrownockException {
		this.androidContext = androidContext;
		appKey = AppKey;
		util = new AnMessageUtility();
		util.setContext(androidContext);
		callbacks = new ArrayList<IAnIMCallback>();
		util.setCallbacks(callbacks);
		try{
			util.setAppKey(appKey);
		} catch (Exception e) {
		    throw new ArrownockException("Failed to initialize SDK.", e, ArrownockException.IM_FAILED_INITIALIZE);
		}
		if (appKey == null || "".equals(appKey.trim())) {
			throw new ArrownockException("Invalid value of " + Constants.APP_KEY, ArrownockException.IM_INVALID_APP_KEY);
		}
	}
	
	public void setSecureConnection(boolean isSecure) {
		this.secureConnection = isSecure;
		util.setSecureConnection(isSecure);
	}
	
	public boolean isSecureConnection() {
		return this.secureConnection;
	}
	
	public void setCallback(IAnIMCallback callback) {
		this.callbacks.remove(callback);
		this.callbacks.add(callback);
	}
	
	public void addCallback(IAnIMCallback callback) {
		if(callback != null) {
			this.callbacks.add(callback);
		}
	}
	
	public void removeCallback(IAnIMCallback callback) {
		if(callback != null) {
			this.callbacks.remove(callback);
		}
	}
	
	public void setDeskCallback(IAnDeskCallback callback) {
        this.deskCallback = callback;
        util.setDeskCallback(callback);
    }
	
	public void setGroupediaCallback(IAnGroupediaCallback callback) {
        this.groupediaCallback = callback;
        util.setGroupediaCallback(callback);
    }
	
	public void setAppKey(String appKey) {
		this.appKey = appKey;
		util.setAppKey(appKey);
		if(Constants.DM_ENABLED) {
			try {
				DeviceManager.getInstance(androidContext, appKey).reportDeviceData();
			} catch (Exception e) {
				Log.w("DeviceManager", e.getMessage());
			}
		}
	}
	
	public void setHosts(String api, String ds) {
		if(api != null && !"".equals(api.trim()) && ds != null && !"".equals(ds.trim())) {
			util.setHostsUtil(androidContext, api, ds);
		}
	}
	
	/**
     * @deprecated use {@link #getClientId(String, IAnIMGetClientIdCallback)} instead
     */
	public void getClientId(String userId) throws ArrownockException {
		//Log.d(LOG_TAG, "Starting to get clientId ...");
		if(androidContext == null) {
			throw new ArrownockException("Invalid application context.", ArrownockException.IM_INVALID_APP_CONTEXT);
		}
		if(appKey == null || "".equals(appKey.trim())) {
			throw new ArrownockException("Invalid app key.", ArrownockException.IM_INVALID_APP_KEY);
		}
		if(userId == null || "".equals(userId.trim())) {
			throw new ArrownockException("Invalid user id.", ArrownockException.IM_INVALID_USER_ID);
		}
		util.getClientIdAsync(appKey.trim(), userId);
	}
	
	public void getClientId(String userId, IAnIMGetClientIdCallback callback) {
        if(androidContext == null) {
            if(callback != null) {
                callback.onError(new ArrownockException("Invalid application context.", ArrownockException.IM_INVALID_APP_CONTEXT));
                return;
            }
        }
        if(appKey == null || "".equals(appKey.trim())) {
            if(callback != null) {
                callback.onError(new ArrownockException("Invalid app key.", ArrownockException.IM_INVALID_APP_KEY));
                return;
            }
        }
        if(userId == null || "".equals(userId.trim())) {
            if(callback != null) {
                callback.onError(new ArrownockException("Invalid user id.", ArrownockException.IM_INVALID_USER_ID));
                return;
            }
        }
        util.getClientIdAsync(appKey.trim(), userId, callback);
    }
	
	public String getRemoteClientId(String id) {
		return TokenHelper.getInstance().getToken(id, this.appKey, 0, Constants.IM_TOKEN_PREFIX);
	}
	
	public void connect(String clientId) throws ArrownockException {
		if(androidContext == null) {
			throw new ArrownockException("Invalid application context.", ArrownockException.IM_INVALID_APP_CONTEXT);
		}
		if(appKey == null || "".equals(appKey.trim())) {
			throw new ArrownockException("Invalid app key.", ArrownockException.IM_INVALID_APP_KEY);
		}
		if(clientId == null || "".equals(clientId.trim())) {
			throw new ArrownockException("Invalid user id.", ArrownockException.IM_INVALID_USER_ID);
		}		
		this.connectingClientId = clientId;
		util.connectAsync(appKey.trim(), clientId);
	}

	public void disconnect() throws ArrownockException {
		if(androidContext == null) {
			throw new ArrownockException("Invalid application context.", ArrownockException.IM_INVALID_APP_CONTEXT);
		}
		if(appKey == null || "".equals(appKey.trim())) {
			throw new ArrownockException("Invalid app key.", ArrownockException.IM_INVALID_APP_KEY);
		}
		util.disconnectAsync(appKey);
	}
	
	public String getConnectingClientId() {
		return connectingClientId;
	}

	/**
	 * Send a private chat message to anther user
	 * @param clientId
	 * @param message
	 * @return The message id
	 * @throws ArrownockException
	 */
	public String sendMessage(String clientId, String message) throws ArrownockException {
		Set<String> clientIds = new HashSet<String>();
		clientIds.add(clientId);
		return sendMessage(clientIds, message, null, false);
	}
	
	public String sendMessage(String clientId, String message, boolean receiveACK) throws ArrownockException {
		Set<String> clientIds = new HashSet<String>();
		clientIds.add(clientId);
		return sendMessage(clientIds, message, null, receiveACK);
	}
	
	public String sendMessage(String clientId, String message, Map<String, String> customData) throws ArrownockException {
		Set<String> clientIds = new HashSet<String>();
		clientIds.add(clientId);
		return sendMessage(clientIds, message, customData, false);
	}
	
	public String sendMessage(String clientId, String message, Map<String, String> customData, boolean receiveACK) throws ArrownockException {
		Set<String> clientIds = new HashSet<String>();
		clientIds.add(clientId);
		return sendMessage(clientIds, message, customData, receiveACK);
	}
	
	/**
	 * @deprecated use {@link #sendMessage(String, String)} instead
	 */
	public String sendMessage(Set<String> clientIds, String message) throws ArrownockException {
		return sendMessage(clientIds, message, null, false);
	}
	
	/**
	 * @deprecated use {@link #sendMessage(String, String, boolean)} instead
	 */
	public String sendMessage(Set<String> clientIds, String message, boolean receiveACK) throws ArrownockException {
		return sendMessage(clientIds, message, null, receiveACK);
	}
	
	/**
	 * @deprecated use {@link #sendMessage(String, String, Map)} instead
	 */
	public String sendMessage(Set<String> clientIds, String message, Map<String, String> customData) throws ArrownockException {
		return sendMessage(clientIds, message, customData, false);
	}
	
	/**
	 * @deprecated use {@link #sendMessage(String, String, Map, boolean)} instead
	 */
	public String sendMessage(Set<String> clientIds, String message, Map<String, String> customData, boolean receiveACK) throws ArrownockException {
		String msgId = null;
		if (clientIds == null || clientIds.size() == 0) {
			throw new ArrownockException("Message has to be sent to at least one client.", ArrownockException.IM_INVALID_CLIENTS);
		}
		if (message == null || "".equals(message)) {
			throw new ArrownockException("Message can not be empty.", ArrownockException.IM_INVALID_MESSAGE_SIZE);
		}
		if (message.length() > sizeLimitation) {
			throw new ArrownockException("Message size can not be larger than " + sizeLimitation + "Bytes.", ArrownockException.IM_INVALID_MESSAGE_SIZE);
		}
		message = message.replace("\\u", "\\u]");
		message = ANEmojiUtil.emojiConvertToString(message);
		if (EmojiFilter.containsEmoji(message)) {
			throw new ArrownockException("Message contains invalid charactar.", ArrownockException.IM_INVALID_MESSAGE_FORMAT);
		}
		if(util.getClientId() == null || util.getCurrentStatus() == AnIMStatus.OFFLINE) {
			final String tempId = util.generateMsgID();
			Runnable sendMessageThread = new Runnable() {
				public void run() {
					AnIMMessageSentCallbackData data = new AnIMMessageSentCallbackData(true, new ArrownockException("Failed to send message, client is offline.", ArrownockException.IM_FAILED_PUBLISH), tempId, -1);
					for(IAnIMCallback c : callbacks) {
						if(c != null) {
							c.messageSent(data);
						}
					}
				}
			};
			Thread thread = new Thread(sendMessageThread);
			thread.start();
			return tempId;
		}
		for (String clientId : clientIds) {
			if(clientId != null && !"".equals(clientId.trim())) {
				msgId = util.generateMsgID();
				String topicName = "ANIM/" + clientId + '/' + appKey;
				JSONObject payload = new JSONObject();
				try {
					payload.put("recipients", new JSONArray(clientIds));
					payload.put("message", message);
					if (customData != null) {
						convertEmoji(customData);
						payload.put("customData", new JSONObject(customData));
					}
					payload.put("receiveACK", receiveACK);
					payload.put("msg_id", msgId);
					payload.put("msg_type", 1);
					payload.put("app_key", appKey);
				} catch (JSONException ex) {
					throw new ArrownockException("Failed to send message.", ex, ArrownockException.IM_INVALID_MESSAGE_FORMAT);
				}
				util.sendMessageToSessionAsync(appKey, msgId, topicName, payload.toString());
				break;
			}
		}
		
		return msgId;
	}
	
	public String sendBinary(String clientId, byte[] content, String fileType) throws ArrownockException {
		Set<String> clientIds = new HashSet<String>();
		clientIds.add(clientId);
		return sendBinary(clientIds, content, fileType, null, false);
	}
	
	public String sendBinary(String clientId, byte[] content, String fileType, boolean receiveACK) throws ArrownockException {
		Set<String> clientIds = new HashSet<String>();
		clientIds.add(clientId);
		return sendBinary(clientIds, content, fileType, null, receiveACK);
	}
	
	public String sendBinary(String clientId, byte[] content, String fileType, Map<String, String> customData) throws ArrownockException {
		Set<String> clientIds = new HashSet<String>();
		clientIds.add(clientId);
		return sendBinary(clientIds, content, fileType, customData, false);
	}
	
	public String sendBinary(String clientId, byte[] content, String fileType, Map<String, String> customData, boolean receiveACK) throws ArrownockException {
		Set<String> clientIds = new HashSet<String>();
		clientIds.add(clientId);
		return sendBinary(clientIds, content, fileType, customData, receiveACK);
	}
	
	/**
	 * @deprecated
	 */
	public String sendBinary(Set<String> clientIds, byte[] content, String fileType) throws ArrownockException {
		return sendBinary(clientIds, content, fileType, null, false);
	}
	
	/**
	 * @deprecated
	 */
	public String sendBinary(Set<String> clientIds, byte[] content, String fileType, boolean receiveACK) throws ArrownockException {
		return sendBinary(clientIds, content, fileType, null, receiveACK);
	}
	
	/**
	 * @deprecated
	 */
	public String sendBinary(Set<String> clientIds, byte[] content, String fileType, Map<String, String> customData) throws ArrownockException {
		return sendBinary(clientIds, content, fileType, customData, false);
	}
	
	/**
	 * @deprecated
	 */
	public String sendBinary(Set<String> clientIds, byte[] content, String fileType, Map<String, String> customData, boolean receiveACK) throws ArrownockException {
		String msgId = null;
		if (clientIds == null || clientIds.size() == 0) {
			throw new ArrownockException("Message has to be sent to at least one client.", ArrownockException.IM_INVALID_CLIENTS);
		}
		if (content.length > sizeLimitation) {
			throw new ArrownockException("Binary data size can not be larger than " + sizeLimitation + "Bytes.", ArrownockException.IM_INVALID_MESSAGE_SIZE);
		}
		if(util.getClientId() == null || util.getCurrentStatus() == AnIMStatus.OFFLINE) {
			final String tempId = util.generateMsgID();
			Runnable sendMessageThread = new Runnable() {
				public void run() {
					AnIMMessageSentCallbackData data = new AnIMMessageSentCallbackData(true, new ArrownockException("Failed to send message, client is offline.", ArrownockException.IM_FAILED_PUBLISH), tempId, -1);
					for(IAnIMCallback c : callbacks) {
						if(c != null) {
							c.messageSent(data);
						}
					}
				}
			};
			Thread thread = new Thread(sendMessageThread);
			thread.start();
			return tempId;
		}
		for (String clientId : clientIds) {
			if(clientId != null && !"".equals(clientId.trim())) {
				msgId = util.generateMsgID();
				String topicName = "ANIM/" + clientId + '/' + appKey;
				JSONObject payload = new JSONObject();
				try {
					payload.put("recipients", new JSONArray(clientIds));
					payload.put("message", ANBase64.encode(content));
					payload.put("fileType", fileType);
					if (customData != null) {
						convertEmoji(customData);
						payload.put("customData", new JSONObject(customData));
					}
					payload.put("receiveACK", receiveACK);
					payload.put("msg_id", msgId);
					payload.put("msg_type", 2);
					payload.put("app_key", appKey);
				} catch (JSONException ex) {
					throw new ArrownockException("Failed to send message.", ex, ArrownockException.IM_INVALID_MESSAGE_FORMAT);
				}
				util.sendMessageToSessionAsync(appKey, msgId, topicName, payload.toString());
				break;
			}
		}
		
		return msgId;
	}
	
	/**
     * @deprecated use {@link #createTopic(String, IAnIMTopicCallback)} instead
     */
	public void createTopic(String topicName) throws ArrownockException {
		String clientId = util.getClientId();
		Set<String> clientIds = new HashSet<String>();
		createTopic(topicName, clientIds);
	}
	
	/**
     * @deprecated use {@link #createTopic(String, Set, IAnIMTopicCallback)} instead
     */
	public void createTopic(String topicName, Set<String> clientIds) throws ArrownockException {
		createTopic(topicName, null, clientIds);
	}
	
	/**
     * @deprecated use {@link #createTopic(String, String, Set, IAnIMTopicCallback)} instead
     */
	public void createTopic(String topicName, String owner, Set<String> clientIds) throws ArrownockException {
		if (topicName == null || "".equals(topicName)) {
			throw new ArrownockException("Topic name can not be empty.", ArrownockException.IM_INVALID_TOPIC_NAME);
		}
		
		String clientsStr = "";
		if (clientIds != null && !clientIds.isEmpty()) {
		    StringBuffer buffer = new StringBuffer();
	        for (String client : clientIds) {
	            if (client != null && !"".equals(client.trim())) {
	                buffer.append(client.trim());
	                buffer.append(",");
	            }
	        }
	        if (buffer.length() != 0){
	            clientsStr = buffer.substring(0, buffer.length() - 1);
	        }
		}
		
		util.createTopicAsync(appKey.trim(), topicName, clientsStr, owner);
	}
	
	public void createTopic(String topicName, IAnIMTopicCallback callback) {
        Set<String> clientIds = new HashSet<String>();
        createTopic(topicName, clientIds, callback);
    }
	
	public void createTopic(String topicName, Map<String, String> customData, IAnIMTopicCallback callback) {
        Set<String> clientIds = new HashSet<String>();
        createTopic(topicName, clientIds, customData, callback);
    }
    
    public void createTopic(String topicName, Set<String> clientIds, IAnIMTopicCallback callback) {
        createTopic(topicName, null, clientIds, callback);
    }
    
    public void createTopic(String topicName, Set<String> clientIds, Map<String, String> customData, IAnIMTopicCallback callback) {
        createTopic(topicName, null, clientIds, customData, callback);
    }
    
    public void createTopic(String topicName, String owner, Set<String> clientIds, IAnIMTopicCallback callback) {
        createTopic(topicName, owner, clientIds, false, null, callback);
    }
    
    public void createTopic(String topicName, String owner, Set<String> clientIds, Map<String, String> customData, IAnIMTopicCallback callback) {
        createTopic(topicName, owner, clientIds, customData, false, null, callback);
    }
    
    public void createTopic(String topicName, String owner, Set<String> clientIds, boolean isNeedNotice, 
        String currentClientId, IAnIMTopicCallback callback){
        if (topicName == null || "".equals(topicName)) {
            if(callback != null) {
                callback.onError(new ArrownockException("Topic name can not be empty.", ArrownockException.IM_INVALID_TOPIC_NAME));
                return;
            }
        }
        
        String clientsStr = "";
        if (clientIds != null && !clientIds.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            for (String client : clientIds) {
                if (client != null && !"".equals(client.trim())) {
                    buffer.append(client.trim());
                    buffer.append(",");
                }
            }
            if (buffer.length() != 0){
                clientsStr = buffer.substring(0, buffer.length() - 1);
            }
        }
        util.createTopicAsync(appKey.trim(), topicName, clientsStr, owner, null, isNeedNotice, currentClientId, callback);
    }
    
    public void createTopic(String topicName, String owner, Set<String> clientIds, Map<String, String> customData, boolean isNeedNotice, 
            String currentClientId, IAnIMTopicCallback callback){
            if (topicName == null || "".equals(topicName)) {
                if(callback != null) {
                    callback.onError(new ArrownockException("Topic name can not be empty.", ArrownockException.IM_INVALID_TOPIC_NAME));
                    return;
                }
            }
            
            String clientsStr = "";
            if (clientIds != null && !clientIds.isEmpty()) {
                StringBuffer buffer = new StringBuffer();
                for (String client : clientIds) {
                    if (client != null && !"".equals(client.trim())) {
                        buffer.append(client.trim());
                        buffer.append(",");
                    }
                }
                if (buffer.length() != 0){
                    clientsStr = buffer.substring(0, buffer.length() - 1);
                }
            }
            util.createTopicAsync(appKey.trim(), topicName, clientsStr, owner, customData, isNeedNotice, currentClientId, callback);
        }
	
    /**
     * @deprecated use {@link #updateTopic(String, String, String, IAnIMTopicCallback)} instead
     */
	public void updateTopic(String topicId, String topicName, String owner) throws ArrownockException {
		if (topicId == null || "".equals(topicId)) {
			throw new ArrownockException("Failed to get clientId for local user.", ArrownockException.IM_INVALID_TOPIC);
		}
		if (topicName == null && owner == null) {
			throw new ArrownockException("Either topic name or owner should be with value.", ArrownockException.IM_INVALID_TOPIC_NAME);
		}
		util.updateTopicAsync(appKey.trim(), topicId, topicName, owner);
	}
	
	public void updateTopic(String topicId, String topicName, String owner, IAnIMTopicCallback callback) {
	    updateTopic(topicId, topicName, owner, null, false, null, callback);
    }
	
	public void updateTopic(String topicId, String topicName, String owner, Map<String, String> customData, IAnIMTopicCallback callback) {
	    updateTopic(topicId, topicName, owner, customData, false, null, callback);
    }
	
	public void updateTopic(String topicId, String topicName, String owner, boolean isNeedNotice, String currentClientId, IAnIMTopicCallback callback) {
         if (topicId == null || "".equals(topicId)) {
             if(callback != null) {
                 callback.onError(new ArrownockException("Failed to get clientId for local user.", ArrownockException.IM_INVALID_TOPIC));
                 return;
             }
         }
         if (topicName == null && owner == null) {
             if(callback != null) {
                 callback.onError(new ArrownockException("Either topic name or owner should be with value.", ArrownockException.IM_INVALID_TOPIC_NAME));
                 return;
             }
         }
         util.updateTopicAsync(appKey.trim(), topicId, topicName, owner, null, isNeedNotice, currentClientId, callback);
     }
	
	public void updateTopic(String topicId, String topicName, String owner, Map<String, String> customData, boolean isNeedNotice, 
	           String currentClientId, IAnIMTopicCallback callback) {
        if (topicId == null || "".equals(topicId)) {
            if(callback != null) {
                callback.onError(new ArrownockException("Failed to get clientId for local user.", ArrownockException.IM_INVALID_TOPIC));
                return;
            }
        }
        if (topicName == null && owner == null && customData == null) {
            if(callback != null) {
                callback.onError(new ArrownockException("Either topic name or owner or customData should be with value.", ArrownockException.IM_INVALID_TOPIC_NAME));
                return;
            }
        }
        util.updateTopicAsync(appKey.trim(), topicId, topicName, owner, customData, isNeedNotice, currentClientId, callback);
    }
	
	/**
     * @deprecated use {@link #addClientsToTopic(String, Set, IAnIMTopicCallback)} instead
     */
	public void addClientsToTopic(String topicId, Set<String> clientIds) throws ArrownockException {
		if (topicId == null || "".equals(topicId)) {
			throw new ArrownockException("Topic can not be empty.", ArrownockException.IM_INVALID_TOPIC);
		}
		if (clientIds == null || clientIds.isEmpty()) {
		    throw new ArrownockException("Topic has to be with at least one client.", ArrownockException.IM_INVALID_CLIENTS);
        }
		
		StringBuffer buffer = new StringBuffer();
		for(String client : clientIds) {
			if(client != null && !"".equals(client.trim())) {
				buffer.append(client.trim());
				buffer.append(",");
			}
 		}
		if (buffer.length() == 0){
			throw new ArrownockException("Topic has to be with at least one client.", ArrownockException.IM_INVALID_CLIENTS);
        }
		
		String clientsStr = buffer.substring(0, buffer.length() - 1);
		util.addClientsAsync(appKey.trim(), topicId, clientsStr);
	}
	
	public void addClientsToTopic(String topicId, Set<String> clientIds, IAnIMTopicCallback callback) {
        if (topicId == null || "".equals(topicId)) {
            if(callback != null) {
                callback.onError(new ArrownockException("Topic can not be empty.", ArrownockException.IM_INVALID_TOPIC));
                return;
            }
        }
        if (clientIds == null || clientIds.isEmpty()) {
            if(callback != null) {
                callback.onError(new ArrownockException("Topic has to be with at least one client.", ArrownockException.IM_INVALID_CLIENTS));
                return;
            }
        }
        
        StringBuffer buffer = new StringBuffer();
        for(String client : clientIds) {
            if(client != null && !"".equals(client.trim())) {
                buffer.append(client.trim());
                buffer.append(",");
            }
        }
        if (buffer.length() == 0){
            if (callback != null) {
                callback.onError(new ArrownockException("Topic has to be with at least one client.", ArrownockException.IM_INVALID_CLIENTS));
                return;
            }
        }
        String clientsStr = buffer.substring(0, buffer.length() - 1);
        util.addClientsAsync(appKey.trim(), topicId, clientsStr, false, null, callback);
    }
	
   public void addClientsToTopic(String topicId, Set<String> clientIds, boolean isNeedNotice, 
           String currentClientId, IAnIMTopicCallback callback) {
        if (topicId == null || "".equals(topicId)) {
            if(callback != null) {
                callback.onError(new ArrownockException("Topic can not be empty.", ArrownockException.IM_INVALID_TOPIC));
                return;
            }
        }
        if (clientIds == null || clientIds.isEmpty()) {
            if(callback != null) {
                callback.onError(new ArrownockException("Topic has to be with at list one client.", ArrownockException.IM_INVALID_CLIENTS));
                return;
            }
        }
        
        StringBuffer buffer = new StringBuffer();
        for(String client : clientIds) {
            if(client != null && !"".equals(client.trim())) {
                buffer.append(client.trim());
                buffer.append(",");
            }
        }
        if (buffer.length() == 0){
            if (callback != null) {
                callback.onError(new ArrownockException("Topic has to be with at least one client.", ArrownockException.IM_INVALID_CLIENTS));
                return;
            }
        }
        String clientsStr = buffer.substring(0, buffer.length() - 1);
        util.addClientsAsync(appKey.trim(), topicId, clientsStr, isNeedNotice, currentClientId, callback);
    }
	
	/**
     * @deprecated use {@link #removeClientsFromTopic(String, Set, IAnIMTopicCallback)} instead
     */
	public void removeClientsFromTopic(String topicId, Set<String> clientIds) throws ArrownockException {
        if (topicId == null || "".equals(topicId)) {
            throw new ArrownockException("Topic can not be empty.", ArrownockException.IM_INVALID_TOPIC);
        }
        if (clientIds == null || clientIds.isEmpty()) {
            throw new ArrownockException("Topic has to be with at list one client.", ArrownockException.IM_INVALID_CLIENTS);
        }
        
        StringBuffer buffer = new StringBuffer();
        for(String client : clientIds) {
            if(client != null && !"".equals(client.trim())) {
                buffer.append(client.trim());
                buffer.append(",");
            }
        }
        if (buffer.length() == 0){
        	throw new ArrownockException("Topic has to be with at list one client.", ArrownockException.IM_INVALID_CLIENTS);
        }
        String clientsStr = buffer.substring(0, buffer.length() - 1);
        util.removeClientsAsync(appKey.trim(), topicId, clientsStr);
    }
	
	public void removeClientsFromTopic(String topicId, Set<String> clientIds, IAnIMTopicCallback callback) {
        if (topicId == null || "".equals(topicId)) {
            if(callback != null) {
                callback.onError(new ArrownockException("Topic can not be empty.", ArrownockException.IM_INVALID_TOPIC));
                return;
            }
        }
        if (clientIds == null || clientIds.isEmpty()) {
            if(callback != null) {
                callback.onError(new ArrownockException("Topic has to be with at least one client.", ArrownockException.IM_INVALID_CLIENTS));
                return;
            }
        }
        
        StringBuffer buffer = new StringBuffer();
        for(String client : clientIds) {
            if(client != null && !"".equals(client.trim())) {
                buffer.append(client.trim());
                buffer.append(",");
            }
        }
        if (buffer.length() == 0){
            if (callback != null) {
                callback.onError(new ArrownockException("Topic has to be with at least one client.", ArrownockException.IM_INVALID_CLIENTS));
                return;
            }
        }
        String clientsStr = buffer.substring(0, buffer.length() - 1);
        util.removeClientsAsync(appKey.trim(), topicId, clientsStr, false, null, callback);
    }
	
    public void removeClientsFromTopic(String topicId, Set<String> clientIds, boolean isNeedNotice, 
            String currentClientId, IAnIMTopicCallback callback) {
        if (topicId == null || "".equals(topicId)) {
            if(callback != null) {
                callback.onError(new ArrownockException("Topic can not be empty.", ArrownockException.IM_INVALID_TOPIC));
                return;
            }
        }
        if (clientIds == null || clientIds.isEmpty()) {
            if(callback != null) {
                callback.onError(new ArrownockException("Topic has to be with at least one client.", ArrownockException.IM_INVALID_CLIENTS));
                return;
            }
        }
        
        StringBuffer buffer = new StringBuffer();
        for(String client : clientIds) {
            if(client != null && !"".equals(client.trim())) {
                buffer.append(client.trim());
                buffer.append(",");
            }
        }
        if (buffer.length() == 0){
            if (callback != null) {
                callback.onError(new ArrownockException("Topic has to be with at least one client.", ArrownockException.IM_INVALID_CLIENTS));
                return;
            }
        }
        String clientsStr = buffer.substring(0, buffer.length() - 1);
        util.removeClientsAsync(appKey.trim(), topicId, clientsStr, isNeedNotice, currentClientId, callback);
    }
	
	/**
     * @deprecated use {@link #removeTopic(String, IAnIMTopicCallback)} instead
     */
	public void removeTopic(String topicId) throws ArrownockException {
		if (topicId == null || "".equals(topicId)) {
			throw new ArrownockException("Topic can not be empty.", ArrownockException.IM_INVALID_TOPIC);
		}
		util.removeTopic(appKey.trim(), topicId);
	}
	
	public void removeTopic(String topicId, IAnIMTopicCallback callback) {
        if (topicId == null || "".equals(topicId)) {
            if(callback != null) {
                callback.onError(new ArrownockException("Topic can not be empty.", ArrownockException.IM_INVALID_TOPIC));
                return;
            }
        }
        util.removeTopic(appKey.trim(), topicId, false, null, callback);
    }
	
    public void removeTopic(String topicId, boolean isNeedNotice, String currentClientId, IAnIMTopicCallback callback) {
        if (topicId == null || "".equals(topicId)) {
            if(callback != null) {
                callback.onError(new ArrownockException("Topic can not be empty.", ArrownockException.IM_INVALID_TOPIC));
                return;
            }
        }
        util.removeTopic(appKey.trim(), topicId, isNeedNotice, currentClientId, callback);
    }
	
	public String sendMessageToTopic(String topic, String message) throws ArrownockException {
		return sendMessageToTopic(topic, message, null, false, null);
	}
	
	public String sendMessageToTopic(String topic, String message, List<String> mentionedClientIds) throws ArrownockException {
		return sendMessageToTopic(topic, message, null, false, mentionedClientIds);
	}
	
	public String sendMessageToTopic(String topic, String message, boolean receiveACK) throws ArrownockException {
		return sendMessageToTopic(topic, message, null, receiveACK, null);
	}
	
	public String sendMessageToTopic(String topic, String message, boolean receiveACK, List<String> mentionedClientIds) throws ArrownockException {
		return sendMessageToTopic(topic, message, null, receiveACK, mentionedClientIds);
	}
	
	public String sendMessageToTopic(String topic, String message, Map<String, String> customData) throws ArrownockException {
		return sendMessageToTopic(topic, message, customData, false, null);
	}
	
	public String sendMessageToTopic(String topic, String message, Map<String, String> customData, List<String> mentionedClientIds) throws ArrownockException {
		return sendMessageToTopic(topic, message, customData, false, mentionedClientIds);
	}
	
	public String sendMessageToTopic(String topic, String message, Map<String, String> customData, boolean receiveACK) throws ArrownockException {
		return sendMessageToTopic(topic, message, customData, receiveACK, null);
	}
	
	public String sendMessageToTopic(String topic, String message, Map<String, String> customData, boolean receiveACK, List<String> mentionedClientIds) throws ArrownockException {
		return sendMessageToTopic(topic, message, customData, receiveACK, mentionedClientIds, null);
	}
	
	public String sendMessageToTopic(String topic, String message, Map<String, String> customData, boolean receiveACK, List<String> mentionedClientIds, final String msgIdPrefix) throws ArrownockException {
		String msgId = null;
		if (topic == null || "".equals(topic)) {
			throw new ArrownockException("Topic can not be empty.", ArrownockException.IM_INVALID_TOPIC);
		}
		if (message == null || "".equals(message)) {
			throw new ArrownockException("Message can not be empty.", ArrownockException.IM_INVALID_MESSAGE_SIZE);
		}
		if (message.length() > sizeLimitation) {
			throw new ArrownockException("Message size can not be larger than " + sizeLimitation + "Bytes.", ArrownockException.IM_INVALID_MESSAGE_SIZE);
		}
		message = message.replace("\\u", "\\u]");
		message = ANEmojiUtil.emojiConvertToString(message);
		if (EmojiFilter.containsEmoji(message)) {
			throw new ArrownockException("Message contains invalid charactar.", ArrownockException.IM_INVALID_MESSAGE_FORMAT);
		}
		if(util.getClientId() == null || util.getCurrentStatus() == AnIMStatus.OFFLINE) {
			final String tempId = util.generateMsgID();
			
			Runnable sendMessageThread = new Runnable() {
				public void run() {
				    ArrownockException exception = new ArrownockException("Failed to send message, client is offline.", ArrownockException.IM_FAILED_PUBLISH);
				    String msgId = (msgIdPrefix != null)? msgIdPrefix + tempId: tempId;
					AnIMMessageSentCallbackData data = new AnIMMessageSentCallbackData(true, exception, msgId, -1);
					if(msgId.startsWith("D")) {
	                    if(deskCallback != null) {
	                        try {
	                            deskCallback.messageSent(msgId, -1, exception);
	                        } catch(Exception e) {
	                            e.printStackTrace();
	                        }
	                    }
	                } else {
        					for(IAnIMCallback c : callbacks) {
        						if(c != null) {
        							c.messageSent(data);
        						}
        					}
	                }
				}
			};
			Thread thread = new Thread(sendMessageThread);
			thread.start();
			return tempId;
		}
		
		msgId = util.generateMsgID();
		if(msgIdPrefix != null) {
			msgId = msgIdPrefix + msgId;
		}
		
		String topicName = "ANIM/" + topic + '/' + appKey;
		JSONObject payload = new JSONObject();
		try {
			payload.put("topic", topic);
			payload.put("message", message);
			if (customData != null) {
				convertEmoji(customData);
				payload.put("customData", new JSONObject(customData));
			}
			if (mentionedClientIds != null && mentionedClientIds.size() > 0) {
				payload.put("mlist", new JSONArray(mentionedClientIds));
			}
			payload.put("receiveACK", receiveACK);
			payload.put("msg_id", msgId);
			payload.put("msg_type", 3);
			payload.put("app_key", appKey);
		} catch (JSONException ex) {
			throw new ArrownockException("Failed to send message.", ex, ArrownockException.IM_INVALID_MESSAGE_FORMAT);
		}
		util.sendMessageAsync(msgId, topicName, payload.toString());
		
		return msgId;
	}
	
	public String sendBinaryToTopic(String topic, byte[] content, String fileType) throws ArrownockException {
		return sendBinaryToTopic(topic, content, fileType, null, false);
	}
	
	public String sendBinaryToTopic(String topic, byte[] content, String fileType, boolean receiveACK) throws ArrownockException {
		return sendBinaryToTopic(topic, content, fileType, null, receiveACK);
	}
	
	public String sendBinaryToTopic(String topic, byte[] content, String fileType, Map<String, String> customData) throws ArrownockException {
		return sendBinaryToTopic(topic, content, fileType, customData, false);
	}
	
	public String sendBinaryToTopic(String topic, byte[] content, String fileType, Map<String, String> customData, boolean receiveACK) throws ArrownockException {
		return sendBinaryToTopic(topic, content, fileType, customData, receiveACK, null);
	}
	
	public String sendBinaryToTopic(String topic, byte[] content, String fileType, Map<String, String> customData, boolean receiveACK, final String msgIdPrefix) throws ArrownockException {
		String msgId = null;
		if (topic == null || "".equals(topic)) {
			throw new ArrownockException("Topic can not be empty.", ArrownockException.IM_INVALID_TOPIC);
		}
		if (content.length > sizeLimitation) {
			throw new ArrownockException("Binary data size can not be larger than " + sizeLimitation + "Bytes.", ArrownockException.IM_INVALID_MESSAGE_SIZE);
		}
		if(util.getClientId() == null || util.getCurrentStatus() == AnIMStatus.OFFLINE) {
			final String tempId = util.generateMsgID();
			Runnable sendMessageThread = new Runnable() {
				public void run() {
                    ArrownockException exception = new ArrownockException("Failed to send message, client is offline.", ArrownockException.IM_FAILED_PUBLISH);
                    String msgId = (msgIdPrefix != null)? msgIdPrefix + tempId: tempId;
                    AnIMMessageSentCallbackData data = new AnIMMessageSentCallbackData(true, exception, msgId, -1);
                    if(msgId.startsWith("D")) {
                        if(deskCallback != null) {
                            try {
                                deskCallback.messageSent(msgId, -1, exception);
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                            for(IAnIMCallback c : callbacks) {
                                if(c != null) {
                                    c.messageSent(data);
                                }
                            }
                    }
                }
			};
			Thread thread = new Thread(sendMessageThread);
			thread.start();
			return tempId;
		}
		
		msgId = util.generateMsgID();
		if(msgIdPrefix != null) {
			msgId = msgIdPrefix + msgId;
		}
		String topicName = "ANIM/" + topic+ '/' + appKey;
		JSONObject payload = new JSONObject();
		try {
			payload.put("topic", topic);
			payload.put("message", ANBase64.encode(content));
			payload.put("fileType", fileType);
			if (customData != null) {
				convertEmoji(customData);
				payload.put("customData", new JSONObject(customData));
			}
			payload.put("receiveACK", receiveACK);
			payload.put("msg_id", msgId);
			payload.put("msg_type", 4);
			payload.put("app_key", appKey);
		} catch (JSONException ex) {
			throw new ArrownockException("Failed to send message.", ex, ArrownockException.IM_INVALID_MESSAGE_FORMAT);
		}
		util.sendMessageAsync(msgId, topicName, payload.toString());
		
		return msgId;
	}
	
	/**
     * @deprecated use {@link #bindAnPushService(String, String, String, IAnIMPushBindingCallback)} instead
     */
	public void bindAnPushService(String anid, String anPushAppKey) throws ArrownockException {
		bindAnPushService(anid, anPushAppKey, null, AnPushType.AnPushTypeAndroid);
	}
	
	/**
     * @deprecated use {@link #bindAnPushService(String, String, String, IAnIMPushBindingCallback)} instead
     */
	public void bindAnPushService(String anid, String anPushAppKey, String clientId) throws ArrownockException {
		bindAnPushService(anid, anPushAppKey, clientId, AnPushType.AnPushTypeAndroid);
	}
	
	/**
     * @deprecated use {@link #bindAnPushService(String, String, String, IAnIMPushBindingCallback)} instead
     */
	public void bindAnPushService(String anid, String anPushAppKey, AnPushType deviceType) throws ArrownockException {
		bindAnPushService(anid, anPushAppKey, null, deviceType);
	}
	
    public void bindAnPushService(String anid, String anPushAppKey, String clientId, IAnIMPushBindingCallback callback){
        bindAnPushService(anid, anPushAppKey, clientId, AnPushType.AnPushTypeAndroid, callback);
    }
	
	private void bindAnPushService(String anid, String anPushAppKey, String clientId, AnPushType deviceType, IAnIMPushBindingCallback callback){
        if (anid == null || "".equals(anid)) {
            if(callback != null) {
                callback.onError(new ArrownockException("anid can not be empty.", ArrownockException.IM_INVALID_ANID));
                return;
            }
        }
        
        if (anPushAppKey == null || "".equals(anPushAppKey)) {
            if(callback != null) {
                callback.onError(new ArrownockException("anPushAppKey can not be empty.", ArrownockException.IM_INVALID_ANPUSH_KEY));
                return;
            }
        }
        
        String deviceTypeStr = null;
        switch(deviceType) {
        case AnPushTypeiOS:
            deviceTypeStr = "ios";
            break;
        case AnPushTypeAndroid:
            deviceTypeStr = "android";
            break;
        case AnPushTypeWP8:
            deviceTypeStr = "wp8";
            break;
        default:
            break;
        }
        
        if(clientId == null) {
            clientId = util.getClientId();
        }
        if ("".equals(clientId.trim())) {
            if(callback != null) {
                callback.onError(new ArrownockException("Failed to get clientId for local user.", ArrownockException.IM_FAILED_GET_CLIENT_ID_LOCALLY));
                return;
            }
        }
        util.bindAnPushAsync(appKey.trim(), clientId, anid, anPushAppKey, deviceTypeStr, callback);
    }
	
	/**
     * @deprecated use {@link #bindAnPushService(String, String, String, IAnIMPushBindingCallback)} instead
     */
	public void bindAnPushService(String anid, String anPushAppKey, String clientId, AnPushType deviceType) throws ArrownockException {
		if (anid == null || "".equals(anid)) {
			throw new ArrownockException("anid can not be empty.", ArrownockException.IM_INVALID_ANID);
		}
		
		if (anPushAppKey == null || "".equals(anPushAppKey)) {
			throw new ArrownockException("anPushAppKey can not be empty.", ArrownockException.IM_INVALID_ANPUSH_KEY);
		}
		String deviceTypeStr = null;
		switch(deviceType) {
		case AnPushTypeiOS:
			deviceTypeStr = "ios";
			break;
		case AnPushTypeAndroid:
			deviceTypeStr = "android";
			break;
		case AnPushTypeWP8:
			deviceTypeStr = "wp8";
			break;
		default:
			break;
		}
		
		if(clientId == null) {
			clientId = util.getClientId();
		}
		if ("".equals(clientId.trim())) {
			throw new ArrownockException("Failed to get clientId for local user.", ArrownockException.IM_FAILED_GET_CLIENT_ID_LOCALLY);
		}
		util.bindAnPushAsync(appKey.trim(), clientId, anid, anPushAppKey, deviceTypeStr);
	}
	
	/**
	 * @deprecated use {@link #unbindAnPushService(String, IAnIMPushBindingCallback)} instead
	 */
	public void unbindAnPushService() throws ArrownockException {
		unbindAnPushService(null);
	}
	
	public void unbindAnPushService(String clientId, IAnIMPushBindingCallback callback){
	    if(clientId == null) {
            clientId = util.getClientId();
        }
        if ("".equals(clientId.trim())) {
            if(callback != null) {
                callback.onError(new ArrownockException("Failed to get clientId for local user.", ArrownockException.IM_FAILED_GET_CLIENT_ID_LOCALLY));
                return;
            }
        }
        util.unbindAnPushAsync(appKey.trim(), clientId, callback);
    }
	
	/**
     * @deprecated use {@link #unbindAnPushService(String, IAnIMPushBindingCallback)} instead
     */
	public void unbindAnPushService(String clientId) throws ArrownockException {
		if(clientId == null) {
			clientId = util.getClientId();
		}
		if ("".equals(clientId.trim())) {
			throw new ArrownockException("Failed to get clientId for local user.", ArrownockException.IM_FAILED_GET_CLIENT_ID_LOCALLY);
		}
		util.unbindAnPushAsync(appKey.trim(), clientId);
	}
	
	public String sendNotice(Set<String> clientIds, String notice) throws ArrownockException {
		return sendNotice(clientIds, notice, null, false);
	}
	
	public String sendNotice(Set<String> clientIds, String notice, boolean receiveACK) throws ArrownockException {
		return sendNotice(clientIds, notice, null, receiveACK);
	}
	
	public String sendNotice(Set<String> clientIds, String notice, Map<String, String> customData) throws ArrownockException {
		return sendNotice(clientIds, notice, customData, false);
	}

	public String sendNotice(Set<String> clientIds, String notice, Map<String, String> customData, boolean receiveACK) throws ArrownockException {
		String msgId = null;
		if (notice == null || "".equals(notice)) {
			throw new ArrownockException("Notice can not be empty.", ArrownockException.IM_INVALID_MESSAGE_SIZE);
		}
		if (notice.length() > sizeLimitation) {
			throw new ArrownockException("Notice size can not be larger than " + sizeLimitation + "Bytes.", ArrownockException.IM_INVALID_MESSAGE_SIZE);
		}
		notice = notice.replace("\\u", "\\u]");
		notice = ANEmojiUtil.emojiConvertToString(notice);
		if (EmojiFilter.containsEmoji(notice)) {
			throw new ArrownockException("Notice contains invalid charactar.", ArrownockException.IM_INVALID_MESSAGE_FORMAT);
		}
		if (clientIds == null || clientIds.isEmpty()) {
            throw new ArrownockException("Notice has to be sent to at list one client.", ArrownockException.IM_INVALID_CLIENTS);
        }
		
		msgId = util.generateMsgID();
		StringBuffer buffer = new StringBuffer();
		for(String client : clientIds) {
			if(client != null && !"".equals(client.trim())) {
				buffer.append(client.trim());
				buffer.append(",");
			}
 		}
        if (buffer.length() == 0){
        	throw new ArrownockException("Notice has to be sent to at list one client.", ArrownockException.IM_INVALID_CLIENTS);
        }
		String clientsStr = buffer.substring(0, buffer.length() - 1);
		
		String clientId = util.getClientId();
		if (clientId == null || "".equals(clientId)) {
			throw new ArrownockException("Failed to get clientId for local user.", ArrownockException.IM_FAILED_GET_CLIENT_ID_LOCALLY);
		}
		if(customData != null) {
			convertEmoji(customData);
		}
		String customDataStr = customData == null ? null : (new JSONObject(customData).toString());
		util.sendNoticeAsync(appKey.trim(), clientId, notice, clientsStr, null, customDataStr, msgId, receiveACK);
		
		return msgId;
	}
	
	public String sendNoticeToTopic(String topic, String notice) throws ArrownockException {
		return sendNoticeToTopic(topic, notice, null, false);
	}
	
	public String sendNoticeToTopic(String topic, String notice, boolean receiveACK) throws ArrownockException {
		return sendNoticeToTopic(topic, notice, null, receiveACK);
	}
	
	public String sendNoticeToTopic(String topic, String notice, Map<String, String>customData) throws ArrownockException {
		return sendNoticeToTopic(topic, notice, customData, false);
	}
	
	public String sendNoticeToTopic(String topic, String notice, Map<String, String>customData, boolean receiveACK) throws ArrownockException {
		String msgId;
		if (topic == null || "".equals(topic)) {
			throw new ArrownockException("Topic can not be empty.", ArrownockException.IM_INVALID_TOPIC);
		}
		if (notice == null || "".equals(notice)) {
			throw new ArrownockException("Notice can not be empty.", ArrownockException.IM_INVALID_MESSAGE_SIZE);
		}
		notice = notice.replace("\\u", "\\u]");
		notice = ANEmojiUtil.emojiConvertToString(notice);
        if (EmojiFilter.containsEmoji(notice)) {
            throw new ArrownockException("Notice contains invalid charactar.", ArrownockException.IM_INVALID_MESSAGE_FORMAT);
        }
		if (notice.length() > sizeLimitation) {
			throw new ArrownockException("Notice size can not be larger than " + sizeLimitation + "Bytes.", ArrownockException.IM_INVALID_MESSAGE_SIZE);
		}
		
		msgId = util.generateMsgID();
		
		String clientId = util.getClientId();
		if (clientId == null || "".equals(clientId)) {
			throw new ArrownockException("Failed to get clientId for local user.", ArrownockException.IM_FAILED_GET_CLIENT_ID_LOCALLY);
		}
		
		if(customData != null) {
			convertEmoji(customData);
		}
		String customDataStr = customData == null ? null : (new JSONObject(customData).toString());
		util.sendNoticeAsync(appKey.trim(), clientId, notice, null, topic, customDataStr, msgId, receiveACK);
		
		return msgId;
	}
	
	/**
     * @deprecated use {@link getTopicInfo(String, IAnIMGetTopicInfoCallback)} instead.  
     */
	public void getTopicInfo(String topicId) throws ArrownockException {
		if (topicId == null || "".equals(topicId)) {
			throw new ArrownockException("Topic can not be empty.", ArrownockException.IM_INVALID_TOPIC);
		}
		util.getTopicInfoAsync(appKey.trim(), topicId);
	}
	
	public void getTopicInfo(String topicId, IAnIMGetTopicInfoCallback callback) {
        if (topicId == null || "".equals(topicId)) {
            if(callback != null) {
                callback.onError(new ArrownockException("Topic can not be empty.", ArrownockException.IM_INVALID_TOPIC));
                return;
            }
        }
        util.getTopicInfoAsync(appKey.trim(), topicId, callback);
    }
	
	/**
	 * @deprecated use {@link getTopicHistory()} instead.  
	 */
	@Deprecated
	public void getTopicLog(String topic, Date start, Date end) {
		return;
	};
	
	public void getFullTopicHistory(String topicId, int limit, long timestamp, IAnIMHistoryCallback callback) {
		getFullTopicHistory(topicId, null, limit, timestamp, callback);
	}
	
	public void getFullTopicHistory(String topicId, String clientId, int limit, long timestamp, IAnIMHistoryCallback callback) {
		if (topicId == null || "".equals(topicId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("topicId can not be empty.", ArrownockException.IM_INVALID_TOPIC));
				return;
			}
		}
		util.getFullTopicLogAsync(appKey.trim(), topicId, clientId, limit, timestamp, callback);
	}
	
	public void getTopicHistory(String topicId, String clientId, int limit, long timestamp, IAnIMHistoryCallback callback) {
		if (topicId == null || "".equals(topicId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("topicId can not be empty.", ArrownockException.IM_INVALID_TOPIC));
				return;
			}
		}
		if (clientId == null || "".equals(clientId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("clientId can not be empty.", ArrownockException.IM_INVALID_CLIENT_ID));
				return;
			}
		}
		util.getTopicLogAsync(appKey.trim(), topicId, clientId, limit, timestamp, false, callback);
	}
	
	public void getOfflineTopicHistory(String topicId, String clientId, int limit, IAnIMHistoryCallback callback) {
		if (topicId == null || "".equals(topicId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("topicId can not be empty.", ArrownockException.IM_INVALID_TOPIC));
				return;
			}
		}
		if (clientId == null || "".equals(clientId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("clientId can not be empty.", ArrownockException.IM_INVALID_CLIENT_ID));
				return;
			}
		}
		util.getTopicLogAsync(appKey.trim(), topicId, clientId, limit, -1, true, callback);
	}
	
	public void getOfflineTopicHistory(String clientId, int limit, IAnIMHistoryCallback callback) {
		if (clientId == null || "".equals(clientId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("clientId can not be empty.", ArrownockException.IM_INVALID_CLIENT_ID));
				return;
			}
		}
		util.getTopicLogAsync(appKey.trim(), null, clientId, limit, -1, true, callback);
	}
	
	public void getHistory(Set<String> clientIds, String clientId, int limit, long timestamp, IAnIMHistoryCallback callback) {
		if (clientIds == null || clientIds.size() == 0) {
			if(callback != null) {
				callback.onError(new ArrownockException("clientIds should contain at least one clientId.", ArrownockException.IM_INVALID_CLIENTS));
				return;
			}
		}
		if (clientId == null || "".equals(clientId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("clientId can not be empty.", ArrownockException.IM_INVALID_CLIENT_ID));
				return;
			}
		}
		util.getLogAsync(appKey.trim(), clientIds, clientId, limit, timestamp, false, callback);
	}
	
	public void getOfflineHistory(String remoteClientId, String me, int limit, IAnIMHistoryCallback callback) {
		Set<String> clientIds = new HashSet<String>();
		clientIds.add(remoteClientId);
		getOfflineHistory(clientIds, me, limit, callback);
	}
	
	/**
	 * @deprecated use {@link #getOfflineHistory(String, String, int, IAnIMHistoryCallback)} instead
	 */
	public void getOfflineHistory(Set<String> clientIds, String clientId, int limit, IAnIMHistoryCallback callback) {
		if (clientIds == null || clientIds.size() == 0) {
			if(callback != null) {
				callback.onError(new ArrownockException("clientIds should contain at least one clientId.", ArrownockException.IM_INVALID_CLIENTS));
				return;
			}
		}
		if (clientId == null || "".equals(clientId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("clientId can not be empty.", ArrownockException.IM_INVALID_CLIENT_ID));
				return;
			}
		}
		util.getLogAsync(appKey.trim(), clientIds, clientId, limit, -1, true, callback);
	}
	
	public void getOfflineHistory(String clientId, int limit, IAnIMHistoryCallback callback) {
		if (clientId == null || "".equals(clientId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("clientId can not be empty.", ArrownockException.IM_INVALID_CLIENT_ID));
				return;
			}
		}
		util.getLogAsync(appKey.trim(), null, clientId, limit, -1, true, callback);
	}
	
	public void syncHistory(String me, int limit, long timestamp, IAnIMHistoryCallback callback) {
		if (me == null || "".equals(me)) {
			if(callback != null) {
				callback.onError(new ArrownockException("Current user's clientId can not be empty.", ArrownockException.IM_INVALID_CLIENT_ID));
				return;
			}
		}
		util.syncHistoryAsync(appKey.trim(), me, limit, timestamp, callback);
	}
	
	/**
     * @deprecated use {@link getClientsStatus(Set, IAnIMGetClientsStatusCallback)} instead.  
     */
	public void getClientsStatus(Set<String> clientIds) throws ArrownockException {
	    if (clientIds == null || clientIds.isEmpty()) {
	        throw new ArrownockException("ClientIds can not be empty.", ArrownockException.IM_INVALID_CLIENTS);
        }
        StringBuffer buffer = new StringBuffer();
        for(String client : clientIds) {
            if(client != null && !"".equals(client.trim())) {
                buffer.append(client.trim());
                buffer.append(",");
            }
        }
        if (buffer.length() == 0){
        	throw new ArrownockException("ClientIds can not be empty.", ArrownockException.IM_INVALID_CLIENTS);
        }
        String clientsStr = buffer.substring(0, buffer.length() - 1);
        util.getClientsStatusAsync(appKey.trim(), clientsStr);
    }
	
	public void getClientsStatus(Set<String> clientIds, IAnIMGetClientsStatusCallback callback) {
	    if (clientIds == null || clientIds.isEmpty()) {
	        if(callback != null) {
        	        callback.onError(new ArrownockException("ClientIds can not be empty.", ArrownockException.IM_INVALID_CLIENTS));
        	        return;
	        }
	    }
        StringBuffer buffer = new StringBuffer();
        for(String client : clientIds) {
            if(client != null && !"".equals(client.trim())) {
                buffer.append(client.trim());
                buffer.append(",");
            }
        }
        if (buffer.length() == 0){
            if (callback != null) {
                callback.onError(new ArrownockException("Topic has to be with at least one client.", ArrownockException.IM_INVALID_CLIENTS));
                return;
            }
        }
        String clientsStr = buffer.substring(0, buffer.length() - 1);
        util.getClientsStatusAsync(appKey.trim(), clientsStr, callback);
    }
	
	/**
     * @deprecated use {@link getClientsStatus(String, IAnIMGetClientsStatusCallback)} instead.  
     */
	public void getClientsStatus(String topicId) throws ArrownockException {
        if(null == topicId || "".equals(topicId.trim())) {
            throw new ArrownockException("TopicId can not be empty.", ArrownockException.IM_INVALID_TOPIC);
        }
        util.getTopicStatusAsync(appKey.trim(), topicId);
    }
	
	public void getClientsStatus(String topicId, IAnIMGetClientsStatusCallback callback) {
        if(null == topicId || "".equals(topicId.trim())) {
            if(callback != null) {
                callback.onError(new ArrownockException("TopicId can not be empty.", ArrownockException.IM_INVALID_TOPIC));
                return;
            }
        }
        util.getTopicStatusAsync(appKey.trim(), topicId, callback);
    }
	
	public String sendReadACK(String clientId, String msgId) throws ArrownockException {
		Set<String> clientIds = new HashSet<String>();
		clientIds.add(clientId);
		return sendReadACK(clientIds, msgId);
	}
	
    public String sendReadACK(String clientId, Set<String> msgIds) throws ArrownockException {
        if (clientId == null || "".equals(clientId.trim())) {
            throw new ArrownockException("Invalid clientId.", ArrownockException.IM_INVALID_CLIENTS);
        }
        if (msgIds == null || msgIds.size() == 0) {
            throw new ArrownockException("Message Id can not be empty.", ArrownockException.IM_INVALID_MESSAGE_ID);
        } else {
            for (String msgId : msgIds) {
                if (msgId == null || "".equals(msgId.trim())) {
                    throw new ArrownockException("Invalid Message Id.", ArrownockException.IM_INVALID_MESSAGE_ID);
                }
            }
        }
        String messageId = util.generateMsgID();
        if(clientId != null && !"".equals(clientId.trim())) {
            String topicName = "ANIM/" + clientId + '/' + appKey;
            JSONObject payload = new JSONObject();
            JSONArray clientIds = new JSONArray();
            clientIds.put(clientId);
            JSONArray msgId = new JSONArray(msgIds);
            try {
                payload.put("recipients", clientIds);
                payload.put("msg_id", msgId);
                payload.put("msg_type", 12);
                payload.put("app_key", appKey);
            } catch (JSONException ex) {
                throw new ArrownockException("Failed to send read ACK.", ex, ArrownockException.IM_INVALID_MESSAGE_FORMAT);
            }
            util.sendMessageToSessionAsync(appKey, messageId, topicName, payload.toString());
        }
        
        return messageId;
    }	
	
	/**
     * @deprecated use {@link sendReadACK(String, String)} instead.  
     */
	public String sendReadACK(Set<String> clientIds, String msgId) throws ArrownockException {
		String messageId = null;
		if (clientIds == null || clientIds.size() == 0) {
			throw new ArrownockException("Read ACK has to be sent to at least one client.", ArrownockException.IM_INVALID_CLIENTS);
		}
		if (msgId == null || "".equals(msgId)) {
			throw new ArrownockException("Message Id can not be empty.", ArrownockException.IM_INVALID_MESSAGE_ID);
		}
		
		for (String clientId : clientIds) {
			if(clientId != null && !"".equals(clientId.trim())) {
				messageId = util.generateMsgID();
				String topicName = "ANIM/" + clientId + '/' + appKey;
				JSONObject payload = new JSONObject();
				try {
					payload.put("recipients", new JSONArray(clientIds));
					payload.put("msg_id", msgId);
					payload.put("msg_type", 12);
					payload.put("app_key", appKey);
				} catch (JSONException ex) {
					throw new ArrownockException("Failed to send read ACK.", ex, ArrownockException.IM_INVALID_MESSAGE_FORMAT);
				}
				util.sendMessageToSessionAsync(appKey, messageId, topicName, payload.toString());
				break;
			}
		}
		
		return messageId;
	}
	
	public AnIMStatus getCurrentStatus() {
		return util.getCurrentStatus();
	}
	
	public void getSessionInfo(String session) throws ArrownockException {
		if (session == null || "".equals(session)) {
			throw new ArrownockException("sessionId can not be empty.", ArrownockException.IM_INVALID_SESSIONID);
		}
		util.getSessionInfoAsync(appKey.trim(), session);
	}
	
	// Get all topics for the app
	/**
     * @deprecated use {@link getTopicList(IAnIMGetTopicListCallback)} instead.  
     */
	public void getTopicList() throws ArrownockException {
        util.getTopicListAsync(appKey.trim(), null);
    }
	
	public void getTopicList(IAnIMGetTopicListCallback callback) {
        util.getTopicListAsync(appKey.trim(), null, callback);
    }
	
	public void getTopicList(String clientId, IAnIMGetTopicListCallback callback) {
	    if (clientId == null || "".equals(clientId)) {
	        if(callback != null) {
        	        callback.onError(new ArrownockException("Failed to get topics for user.", ArrownockException.IM_FAILED_GET_CLIENT_ID_LOCALLY));
        	        return;
	        }
	    }
        util.getTopicListAsync(appKey.trim(), clientId, callback);
    }
	
	// Get topic list that current user joined
	/**
     * @deprecated use {@link getTopicList(String, IAnIMGetTopicListCallback)} instead.  
     */
	public void getMyTopicList() throws ArrownockException {
		String clientId = util.getClientId();
		if (clientId == null || "".equals(clientId)) {
			throw new ArrownockException("Failed to get topics for current user.", ArrownockException.IM_FAILED_GET_CLIENT_ID_LOCALLY);
		}
		
		util.getTopicListAsync(appKey.trim(), clientId);
	}
	
	public void setPushNotificationForChatSession(String clientId, boolean isEnable, IAnIMPushNotificationSettingsCallback callback) {
		if (clientId == null || "".equals(clientId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("clientId can not be empty.", ArrownockException.IM_INVALID_CLIENT_ID));
				return;
			}
		}
		util.setPushNotificationAsync(clientId, isEnable, 1, null, callback);
	}
	
	public void setPushNotificationForTopic(String clientId, boolean isEnable, IAnIMPushNotificationSettingsCallback callback) {
		if (clientId == null || "".equals(clientId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("clientId can not be empty.", ArrownockException.IM_INVALID_CLIENT_ID));
				return;
			}
		}
		util.setPushNotificationAsync(clientId, isEnable, 2, null, callback);
	}
	
	public void setPushNotificationForNotice(String clientId, boolean isEnable, IAnIMPushNotificationSettingsCallback callback) {
		if (clientId == null || "".equals(clientId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("clientId can not be empty.", ArrownockException.IM_INVALID_CLIENT_ID));
				return;
			}
		}
		util.setPushNotificationAsync(clientId, isEnable, 3, null, callback);
	}
	
	public void disablePushNotificationForTopics(String clientId, List<String> topicIds, IAnIMPushNotificationSettingsCallback callback) {
		if (clientId == null || "".equals(clientId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("clientId can not be empty.", ArrownockException.IM_INVALID_CLIENT_ID));
				return;
			}
		}
		util.setPushNotificationAsync(clientId, false, 4, topicIds, callback);
	}
	
	public void enablePushNotificationForTopics(String clientId, List<String> topicIds, IAnIMPushNotificationSettingsCallback callback) {
		if (clientId == null || "".equals(clientId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("clientId can not be empty.", ArrownockException.IM_INVALID_CLIENT_ID));
				return;
			}
		}
		util.setPushNotificationAsync(clientId, false, 5, topicIds, callback);
	}
	
	public void setPushNotificationForMentioning(String clientId, boolean isEnable, IAnIMPushNotificationSettingsCallback callback) {
		if (clientId == null || "".equals(clientId)) {
			if(callback != null) {
				callback.onError(new ArrownockException("clientId can not be empty.", ArrownockException.IM_INVALID_CLIENT_ID));
				return;
			}
		}
		util.setPushNotificationAsync(clientId, isEnable, 6, null, callback);
	}
	
	@Override
	public boolean isOnline() {
		return getCurrentStatus() == AnIMStatus.ONLINE;
	}

	@Override
	public void createSession(List<String> partyIds, String type) {
		util.createLiveSession(appKey.trim(), util.getClientId(), partyIds, type);
	}
	
	@Override
	public void validateSession(String sessionId) {
		util.validateLiveSession(appKey.trim(), sessionId);
	}

	@Override
	public void terminateSession(String sessionId) {
		util.terminateLiveSession(appKey.trim(), sessionId);
	}
	
	@Override
	public void sendInvitations(String sessionId, List<String> partyIds, String type, Map<String, String> notificationData) {
		Map<String, String> customData = new HashMap<String, String>();
		customData.put("sid", sessionId);
		customData.put("st", type);
		if(notificationData != null && notificationData.size() > 0) {
			if(notificationData.containsKey("st")) {
				notificationData.remove("st");
			}
			customData.putAll(notificationData);
		}
		String self = util.getClientId();
		for(String partyId : partyIds) {
			if(!partyId.equals(self)) {
				sendSignalData(partyId, "invitation", 31, customData, 2);
			}
		}
	}

	@Override
	public void sendOffer(String partyId, String sdp, int orientation) {
		Map<String, String> customData = new HashMap<String, String>();
		customData.put("o", String.valueOf(orientation));
		sendSignalData(partyId, sdp, 32, customData, 1);
	}

	@Override
	public void sendAnswer(String partyId, String sdp, int orientation) {
		Map<String, String> customData = new HashMap<String, String>();
		customData.put("o", String.valueOf(orientation));
		sendSignalData(partyId, sdp, 33, customData, 1);
	}

	@Override
	public void sendICECandidate(String partyId, String candidateJson) {
		sendSignalData(partyId, candidateJson, 34, null, 1);
	}
	
	private void sendSignalData(String partyId, String data, int type, Map<String, String> customData, int QoS) {
		String msgId = "-" + String.valueOf(type) + "-" + util.generateMsgID();
		String topicName = "ANIM/" + partyId + '/' + appKey;
		JSONObject payload = new JSONObject();
		String self = util.getClientId();
		try {
			payload.put("message", data);
			payload.put("msg_id", msgId);
			payload.put("msg_type", type);
			payload.put("app_key", appKey);
			payload.put("party", partyId);
			payload.put("from", self);
			if (customData != null) {
				payload.put("customData", new JSONObject(customData));
			}
		} catch (JSONException ex) {
			
		}
		util.sendMessageAsync(msgId, topicName, payload.toString(), QoS);
	}
	
	@Override
	public void setCallbacks(Callbacks callbacks) {
		util.setSignalControllerCallback(callbacks);
	}

	@Override
	public String getPartyId() {
		return util.getClientId();
	}

	@Override
	public void sendHangup(List<String> partyIds) {
		for(String partyId : partyIds) {
			sendSignalData(partyId, "", 35, null, 1);
		}
	}
	
	public void addBlacklist(String currentClientId, String targetClientId, IAnIMBlacklistCallback callback) {
        if (currentClientId == null || "".equals(currentClientId)) {
            if(callback != null) {
                callback.onError(new ArrownockException("Invalid parameter: currentClientId", ArrownockException.IM_INVALID_CLIENT_ID));
                return;
            }
        }
        if (targetClientId == null && targetClientId == null) {
            if(callback != null) {
                callback.onError(new ArrownockException("Invalid parameter: targetClientId", ArrownockException.IM_INVALID_CLIENT_ID));
                return;
            }
        }
        util.addBlacklistAsync(appKey.trim(), currentClientId, targetClientId, callback);
    }
    
    public void removeBlacklist(String currentClientId, String targetClientId, IAnIMBlacklistCallback callback) {
        if (currentClientId == null || "".equals(currentClientId)) {
            if(callback != null) {
                callback.onError(new ArrownockException("Invalid parameter: currentClientId", ArrownockException.IM_INVALID_CLIENT_ID));
                return;
            }
        }
        if (targetClientId == null && targetClientId == null) {
            if(callback != null) {
                callback.onError(new ArrownockException("Invalid parameter: targetClientId", ArrownockException.IM_INVALID_CLIENT_ID));
                return;
            }
        }
        util.removeBlacklistAsync(appKey.trim(), currentClientId, targetClientId, callback);
    }
    
    public void listBlacklists(String currentClientId, IAnIMListBlacklistsCallback callback) {
        if (currentClientId == null || "".equals(currentClientId)) {
            if(callback != null) {
                callback.onError(new ArrownockException("Invalid parameter: currentClientId", ArrownockException.IM_INVALID_CLIENT_ID));
                return;
            }
        }
        util.listBlacklistsAsync(appKey.trim(), currentClientId, callback);
    }
	
	private void convertEmoji(Map<String, String> cdata) {
		if(cdata != null && !cdata.isEmpty()) {
			Set<String> keys = cdata.keySet();
			if(keys != null) {
				Iterator<String> itr = keys.iterator();
				while(itr.hasNext()) {
					String key = itr.next();
					String val = cdata.get(key);
					if(val != null) {
						try{
						    val = val.replace("\\u", "\\u]");
							cdata.put(key, ANEmojiUtil.emojiConvertToString(val));
						}catch(Exception e){
						}
					}
				}
			}
		}
	}
	
	public String generateMsgID(){
	    return util.generateMsgID();
	}
}
