package com.arrownock.push;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.arrownock.exception.ArrownockException;
import com.arrownock.internal.push.Connectivity;
import com.arrownock.internal.push.DaemonConfigurations;
import com.arrownock.internal.push.IDaemonStrategy;
import com.arrownock.internal.push.LogUtil;
import com.arrownock.internal.util.Constants;
import com.arrownock.internal.util.KeyValuePair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PushService extends Service {
	public final static String LOG_TAG = PushService.class.getName();
	private final static String LOG_NAME = "ArrownockSDK";
	private final static int DEFAULT_KEEPALIVE = 240;

	// Intent actions for push service
	private final static String ACTION_START = PushConstants.BROKER_CLIENTID_SUFFIX + ".START";
	private final static String ACTION_STOP = PushConstants.BROKER_CLIENTID_SUFFIX + ".STOP";
	public final static String ACTION_CHECK_START = PushConstants.BROKER_CLIENTID_SUFFIX + ".CHECK_START";
	private final static String ACTION_RESTART = PushConstants.BROKER_CLIENTID_SUFFIX + ".RESTART";
	private final static String ACTION_KEEPALIVE = PushConstants.BROKER_CLIENTID_SUFFIX + ".KEEP_ALIVE";
	private final static String ACTION_RECONNECT = PushConstants.BROKER_CLIENTID_SUFFIX + ".RECONNECT";
	private final static String ACTION_CONNECTIVITY_CHANGED = PushConstants.BROKER_CLIENTID_SUFFIX + ".CONNECTIVITY_CHANGED";
	public final static String ACTION_MSG_ARRIVAL = PushService.class.getName() + ".MSG_ARRIVAL";

	// Whether or not the service has been enabled.
	private boolean mEnabled;
	
	// Retry intervals, when the connection is lost.
	private final static long INITIAL_RETRY_INTERVAL = 500 * 5;	// 5 secs indeed
	private final static long MAXIMUM_RETRY_INTERVAL = 1000 * 60 * 15;	// 15 mins
	private final static long MAXIMUM_PUSH_HOST_RETRYTIME = 4;	// 2 times retry, then push host will be cleared

	// Shared Preferences instance
	private SharedPreferences mPrefs;
	// Store in the preferences, whether or not the service has been enabled
	public final static String PREF_ENABLED = "isEnabled";
	// Store the deviceToken and appID in preferences
	public final static String PREF_DEVICE_TOKEN = "deviceToken";
    public final static String PREF_DEVICE_ID = "ANID";
    public final static String PREF_ALREADY_REGISTER = "hasAlreadyRegistered";
	// Store push hostname here
	public final static String PREF_PUSH_HOST = "pushHost";
	public final static String PREF_PUSH_PORT = "pushPort";
	public final static String PREF_PUSH_HOST_EXPIRATION = "pushHostExpiration";
	public final static String PREF_PUSH_HOST_RETRYTIME = "pushHostRetrytime";
	// Store scheduled stop/start connection time here
	public final static String PREF_PUSH_SCHEDULED_HOUR = "pushScheduledHour";
	public final static String PREF_PUSH_SCHEDULED_MINUTE = "pushScheduledMinue";
	public final static String PREF_PUSH_SCHEDULED_DURATION = "pushScheduledDuration";
	// Store the last retry interval in preferences
	public final static String PREF_RETRY = "retryInterval";
	public final static String PREF_SECURE_CONNECTION = "secureConnection";
	public final static String PREF_API_HOST = "apiHost";
	public final static String PREF_DS_HOST = "dsHost";
	public final static String PREF_API_SECRET = "apiSecret";
	public final static String PREF_KEEPALIVE = "networkKeepalive";
	
	// Store the SSL certificates
	public final static String PREF_SERVER_CERT = "serverCert";
	public final static String PREF_CLIENT_CERT = "clientCert";
	public final static String PREF_CLIENT_KEY = "clientKey";
	
	// keep alive interval for different kinds of network connections
	// This is the application level keep-alive interval, that is used by the AlarmManager
	// to keep the connection active, even when the device goes to sleep.
	public final static String PREF_INTERVAL_WIFI = "intervalWiFi";
	public final static String PREF_INTERVAL_2G = "interval2G";
	public final static String PREF_INTERVAL_3G = "interval3G";
	
	// This is the instance of an MQTT connection.
	private static IMQTTAgent mqttAgent;
	private long mStartTime;
	private long keepAliveInterval = 4 * 60 * 1000; 
	
	// The client connection thread
	private Thread connectionThread = null;
	private enum ConnectionCommand { NONE, START, STOP, RECONNECT, RESTART, KEEPALIVE, CHECK_START, DESTROY, CONNECTIVITY_CHANGED };
	private ConnectionCommand currentCommand = ConnectionCommand.NONE;
	private final static String COMMAND_LOCK = "COMMAND_LOCK";
	private final static String COMMAND_LOCKER = "COMMAND_LOCKER";
	
	protected static int KEEPALIVE_ID = 20001;
	
	private PendingIntent restartServicePendingIntent = null;
	private BroadcastReceiver pushDaemonReceiver = null;
	private DaemonConfigurations mConfigurations = null; 
	private BufferedReader mBufferedReader = null;
	private boolean isDaemonInited = false;
	private JobInfo keepaliveJobInfo;
	private JobScheduler keepaliveScheduler;
	
	// Service Start Point outside
	public static void actionStart(Context ctx) {
		Intent i = new Intent(ctx, PushService.class);
		i.setAction(ACTION_START);
		ctx.startService(i);
	}

	// Service Stop Point outside
	public static void actionStop(Context ctx) {
		Intent i = new Intent(ctx, PushService.class);
		i.setAction(ACTION_STOP);
		ctx.startService(i);
	}
	
	public static void actionRestart(Context ctx) {
		Intent i = new Intent(ctx, PushService.class);
		i.setAction(ACTION_RESTART);
		ctx.startService(i);
	}
	
	public static void actionCheckStart(Context ctx) {
		Intent i = new Intent(ctx, PushService.class);
		i.setAction(ACTION_CHECK_START);
		ctx.startService(i);
	}
	
	// Service Keep-alive outside
	public static void actionPing(Context ctx) {
		Intent i = new Intent(ctx, PushService.class);
		i.setAction(ACTION_KEEPALIVE);
		ctx.startService(i);
	}
	
	public static void actionConnectivityChanged(Context ctx) {
		Intent i = new Intent(ctx, PushService.class);
		i.setAction(ACTION_CONNECTIVITY_CHANGED);
		ctx.startService(i);
	}
	
	public static boolean isEnabled() {
		return mqttAgent != null && MQTTConnectionStatus.Connected.equals(mqttAgent.getStatus());
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(LOG_TAG, "Creating push service.");
		mStartTime = System.currentTimeMillis();

		// Get instances of preferences, connectivity manager and notification manager
		mPrefs = getSharedPreferences(LOG_TAG, MODE_PRIVATE);

		if (wasEnabled() == true) {
			Log.i(LOG_TAG, "Handling crashed service...");
			// stop the keep alive signal
			stopKeepAlives();
		}
	}
	
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		// start job for sdk >= 20
		startKeepAliveJob();
		
		try {
			if (Build.VERSION.SDK_INT < 19) {
				((AlarmManager)getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), restartServicePendingIntent);
			} else {
				((AlarmManager)getSystemService(Context.ALARM_SERVICE)).setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), restartServicePendingIntent);
			}
			Log.d(LOG_TAG, "Push service is being removed (enabled=" + mEnabled + ")");
		} catch (Exception e) {
			Log.w(LOG_TAG, "Try restart push service error: " + e);
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			Log.d(LOG_TAG, "Sleeping before being killed error...");
		}
		super.onTaskRemoved(rootIntent);
	}

	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "Push service destroyed (enabled=" + mEnabled + ")");
		
		// stop the connection thread
		synchronized (COMMAND_LOCK) {
			currentCommand = ConnectionCommand.DESTROY;
			connectionThread = null;
		}
		
		// Stop the services, if it has been enabled
		if (mEnabled == true) {
			stop(true);
		}
		
		// recreate the service if this service is destroyed unexpectedly
		if(wasEnabled()) {
			Log.d(LOG_TAG, "Restart service for recoverying from unexpectedly destoryed...");
			if (Build.VERSION.SDK_INT < 19) {
				((AlarmManager)getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), restartServicePendingIntent);
			} else {
				((AlarmManager)getSystemService(Context.ALARM_SERVICE)).setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), restartServicePendingIntent);
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Build.VERSION.SDK_INT >= 20 && !isEmulator()) {
			startForeground(11, new Notification());
			startService(new Intent(this, NotificationService.class));
		}
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if(connectionThread == null) {
			connectionThread = new Thread(new Runnable() {
				@Override
				public void run() {
					boolean isStop = false;
					while(!isStop) {
						int temp = 0;
						synchronized (COMMAND_LOCK) {
							if(currentCommand != ConnectionCommand.NONE) {
								temp = currentCommand.ordinal();
								currentCommand = ConnectionCommand.NONE;
							}
						}
						switch(temp) {
							case 1:	// START
								start();
								break;
							case 2:	// STOP
								stop();
								stopSelf();
								break;
							case 3:	// RECONNECT
								if (isNetworkAvailable()) {
									reconnectIfNecessary();
								}
								break;
							case 4:	// RESTART
								if (isNetworkAvailable()) {
									reconnect();
								}
								break;
							case 5:	// KEEPALIVE
								keepAlive();
								break;
							case 6:	// CHECK_START
								if(wasEnabled()) {
									start();
								} else {
									stopSelf();
								}
								break;
							case 7:	// DESTROY
								isStop = true;
								break;
							case 8: // CONNECTIVITY_CHANGED
								onConnectivityChanged();
								break;
						}
						if(currentCommand == ConnectionCommand.NONE) {
							synchronized (COMMAND_LOCKER) {
								try {
									Log.d(LOG_TAG, "Push connection thread into wait state...");
									COMMAND_LOCKER.wait();
									Log.d(LOG_TAG, "Push connection thread into running state...");
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
					Log.d(LOG_TAG, "Push connection thread stopped!");
				}
			});
			connectionThread.setName("Arrownock Push Service");
			connectionThread.start();
		}
		
		if (intent == null) {
			Log.d(LOG_TAG, "Received null intent! The OS restarted us.");
			// Regenerate AnPush instance
			try {
				AnPush.getInstance(getApplicationContext());
			} catch (ArrownockException ex) {
				Log.e(LOG_TAG, "Error when restart push service.", ex);
			}
			return;
		}
		
		final String intentAction = intent.getAction();
		
		LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "Push service status changed. ACTION=" + intentAction);
		super.onStart(intent, startId);
		
		Intent restartServiceIntent = new Intent(this, SignalReceiver.class);
		restartServiceIntent.setAction(SignalReceiver.PUSH_SIGNAL);
		restartServiceIntent.putExtra("command", 1);
		restartServicePendingIntent = PendingIntent.getBroadcast(this, 0, restartServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (COMMAND_LOCK) {
					// Do an appropriate action based on the intent.
					if (ACTION_STOP.equals(intentAction) == true) {
						currentCommand = ConnectionCommand.STOP;
					} else if (ACTION_START.equals(intentAction) == true) {
						currentCommand = ConnectionCommand.START;
					} else if (ACTION_KEEPALIVE.equals(intentAction) == true) {
						currentCommand = ConnectionCommand.KEEPALIVE;
					} else if (ACTION_RECONNECT.equals(intentAction) == true) {
						currentCommand = ConnectionCommand.RECONNECT;
					} else if (ACTION_RESTART.equals(intentAction) == true) {
						currentCommand = ConnectionCommand.RESTART;
					} else if (ACTION_CHECK_START.equals(intentAction) == true) {
						currentCommand = ConnectionCommand.CHECK_START;
					} else if (ACTION_CONNECTIVITY_CHANGED.equals(intentAction) == true) {
						currentCommand = ConnectionCommand.CONNECTIVITY_CHANGED;
					}
					if(currentCommand != ConnectionCommand.NONE) {
						if(connectionThread != null) {
							synchronized (COMMAND_LOCKER) {
								COMMAND_LOCKER.notifyAll();
							}
						}
					}
				}
			}
		}).start();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	// Reads whether or not the service has been enabled from the preferences
	private boolean wasEnabled() {
		return mPrefs.getBoolean(PREF_ENABLED, false);
	}

	// Sets whether or not the services has been enabled in the preferences.
	private void setEnabled(boolean enabled) {
		mPrefs.edit().putBoolean(PREF_ENABLED, enabled).commit();
		mEnabled = enabled;
	}
	
	private String getServerCert() {
		return mPrefs.getString(PREF_SERVER_CERT, Constants.SSL_SERVER_CERT);
	}
	
	private String getClientCert() {
		return mPrefs.getString(PREF_CLIENT_CERT, Constants.SSL_CLIENT_CERT);
	}
	
	private String getClientKey() {
		return mPrefs.getString(PREF_CLIENT_KEY, Constants.SSL_CLIENT_KEY);
	}
	
	private int getNetworkKeepalive() {
		return mPrefs.getInt(PushService.PREF_KEEPALIVE, PushService.DEFAULT_KEEPALIVE);
	}
	
	private synchronized void start() {
		LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "Starting push service...");

		boolean forceReconnect = false;
		
		// Do nothing, if the service is already running.
		if (mEnabled == true && mqttAgent != null) {
			LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "Current connection status: " + mqttAgent.getStatus().name());
			if(mqttAgent.getStatus().equals(MQTTConnectionStatus.Connected)) {
				LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "Connection to messaging server has already been active.");
				
				try {
					AnPush instance = AnPush.getInstance(getApplicationContext());
					if(instance != null && instance.getCallback() != null) {
						instance.getCallback().statusChanged(AnPushStatus.ENABLE, null);
					}
				} catch (ArrownockException e) {
					//mute
				}
				return;
			} else if(mqttAgent.getStatus().equals(MQTTConnectionStatus.Connecting)) {
				LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "Connection to messaging server is ongoing...");
				return;
			} else if(mqttAgent.getStatus().equals(MQTTConnectionStatus.Problematic)) {
				LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "Connection to messaging server has error, force reconnect...");
				forceReconnect = true;
				mqttAgent.terminate();
				mqttAgent = null;
			}
		}
		
		// Enable Connection
		setEnabled(true);
		
		// trigger first time connection
		onConnectivityChanged();
		
		// Register a keepalive receiver
        registerReceiver(keepAliveSender, new IntentFilter(ACTION_KEEPALIVE));
        
        if(forceReconnect) {
        	reconnect();
        }
        
        // start the daemon if needed
        startDaemon();
	}
	
	private void startDaemon() {
		if (!isDaemonInited && !isEmulator()) {
			if (pushDaemonReceiver == null) {
				pushDaemonReceiver = new PushReceiver();
			}
			this.registerReceiver(pushDaemonReceiver, new IntentFilter());

			String persistProcess = getPackageName() + ":push";
			String daemonProcess = getPackageName() + ":daemon";
			if (mConfigurations == null) {
				DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(persistProcess, PushService.class.getCanonicalName(), PushReceiver.class.getCanonicalName());
				DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(daemonProcess, DaemonService.class.getCanonicalName(), DaemonReceiver.class.getCanonicalName());
				mConfigurations = new DaemonConfigurations(configuration1, configuration2, null);
			}
			IDaemonStrategy.Fetcher.fetchStrategy().onInitialization(this.getBaseContext());

			String processName = getProcessName();
			if (processName.startsWith(mConfigurations.PERSISTENT_CONFIG.PROCESS_NAME)) {
				IDaemonStrategy.Fetcher.fetchStrategy().onPersistentCreate(this.getBaseContext(), mConfigurations);
			}
			releaseIO();
			initKeepAliveJob();
			isDaemonInited = true;
		}
	}
	
	private void stop() {
		stop(false);
	}
	
	private synchronized void stop(boolean isTemp) {
		// Do nothing, if the service is not running.
		if (mEnabled == false) {
			LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "Connection is not active. Ingore stop action.");
			return;
		}

		if(!isTemp) {
			// Save stopped state in the preferences
			setEnabled(false);
		}
		
		// Remove the keepalive receiver
		unregisterReceiver(keepAliveSender);
		
		// Any existing reconnect timers should be removed, since we explicitly stopping the service.
		cancelReconnect();

		// Destroy the MQTT connection if there is one
		if (mqttAgent != null) {
			final String deviceToken = mPrefs.getString(PREF_DEVICE_TOKEN, null);
			if (deviceToken == null) {
				Log.w(LOG_TAG, "Device token not found.");
			} else {
				// Run clearSession in new thread
				Runnable mqttClearSessionThread = new Runnable() {
					@Override
					public void run() {
						String initTopic = PushConstants.BROKER_CLIENTID_SUFFIX + "/" + deviceToken;
						mqttAgent.clearSession(initTopic);
					}
				};
				Thread thread = new Thread(mqttClearSessionThread);
				thread.start();
			}
			// Run disconnect in new thread
			Runnable mqttDisconnectThread = new Runnable(){
				public void run() {
					mqttAgent.disconnect();
					mqttAgent = null;
				}
			};
			Thread thread = new Thread(mqttDisconnectThread);
			thread.start();
		}
	}
	
	private void connect(){
		String deviceToken = mPrefs.getString(PREF_DEVICE_TOKEN, null);
		if (deviceToken == null) {
			LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, "Device token is missing.");
		}
		
		// Find broker server
		getServiceHostAsync(getBaseContext(), deviceToken, new GetPushHostCallback(){
			@Override
			public void receivedPushHostInfo(String pushHostName, int pushPort) {
				if(pushHostName != null && !"".equals(pushHostName.trim())){
					//LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "Push Host: " + pushHostName + ", Push Port: " + pushPort);
					connect(pushHostName, pushPort);
				}else{
					LogUtil.getInstance().error(LOG_TAG, LOG_NAME, "Cannot find push server.");
					scheduleReconnect(mStartTime);
				}
			}
			@Override
			public void failedReceivePushHostInfo(Throwable exception) {
				//TODO retry needed??
				LogUtil.getInstance().error(LOG_TAG, LOG_NAME, "Failed to find push server. Will retry later.", exception);
				scheduleReconnect(mStartTime);
			}
		});
	}

	private synchronized void connect(String pushHostname, int pushPort) {
		// Log.i(LOG_TAG, "Connecting... MQTT Instance:" + (mqttAgent == null ? "NULL" : mqttAgent.toString()));
		LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "Connecting...");
		
		// fetch the device ID from the preferences.
		String deviceToken = mPrefs.getString(PREF_DEVICE_TOKEN, null);
		// Create a new connection only if the device id is not NULL
		if (deviceToken == null) {
			LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, "Device token is missing.");
		} else {
			String initTopic = PushConstants.BROKER_CLIENTID_SUFFIX + "/" + deviceToken;
			String initSenderName = deviceToken;
			
			//LogUtil.getInstance().info(LOG_NAME, "InitTopic: " + initTopic + ". Server: " + pushHostname + ". Port: " + pushPort);
			//TODO
			synchronized (LOG_TAG) {
				if (mqttAgent != null) {
					if(mqttAgent.getStatus().equals(MQTTConnectionStatus.Connecting)) {
						LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "Connection to messaging server is ongoing...");
						return;
					} else if(mqttAgent.getStatus().equals(MQTTConnectionStatus.Connected)) {
						LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "Already connected");
						return;
					} else {
						try {
							mqttAgent.disconnect();
							mqttAgent.terminate();
						} catch(Exception e) {
							// mute
						}
						mqttAgent = null;
					}
					
				}
				MQTTEvent mqttEvent = new MQTTEvent();
				try {
					if (AnPush.getInstance(getApplicationContext()).isSecureConnection()) {
						mqttAgent = new PahoAgent(pushHostname, pushPort, true, getServerCert(), getClientCert(), getClientKey(), initSenderName, initTopic, mqttEvent, "BKS");
					} else {
						//TODO 处理MQttId的赋值
						mqttAgent = new PahoAgent(pushHostname, pushPort, "mqttClientID", initTopic, mqttEvent);
					}
				} catch(ArrownockException ex) {
					Log.e(LOG_TAG, "Error from connecting to push server", ex);
				}
			}
		}
	}
	
	private void setKeepAliveInterval() {
		if (Connectivity.isConnectedWifi(this)) {
			Log.d(LOG_TAG, "network type: " + "WIFI");
			keepAliveInterval = mPrefs.getLong(PREF_INTERVAL_WIFI, 4 * 60 * 1000);
		} else if (Connectivity.isConnectedMobile(this)) {
			if (Connectivity.isConnectedFast(this)) {
				Log.d(LOG_TAG, "network type: " + "3G");
				keepAliveInterval = mPrefs.getLong(PREF_INTERVAL_3G, 4 * 60 * 1000);
			} else {
				Log.d(LOG_TAG, "network type: " + "2G");
				keepAliveInterval = mPrefs.getLong(PREF_INTERVAL_2G, 4 * 60 * 1000);
			}
		}
	}
	
    /*
     * Schedule the next time that you want the phone to wake up and ping the 
     *  message broker server
     */
    private void scheduleNextKeepAlive()
    {
        // When the phone is off, the CPU may be stopped. This means that our 
        //   code may stop running.
        // When connecting to the message broker, we specify a 'keep alive' 
        //   period - a period after which, if the client has not contacted
        //   the server, even if just with a ping, the connection is considered
        //   broken.
        // To make sure the CPU is woken at least once during each keep alive
        //   period, we schedule a wake up to manually ping the server
        //   thereby keeping the long-running connection open
        // Normally when using this Java MQTT client library, this ping would be
        //   handled for us. 
        // Note that this may be called multiple times before the next scheduled
        //   ping has fired. This is good - the previously scheduled one will be
        //   cancelled in favour of this one.
        // This means if something else happens during the keep alive period, 
        //   (e.g. we receive an MQTT message), then we start a new keep alive
        //   period, postponing the next ping.
		
		Intent intent = new Intent(ACTION_KEEPALIVE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, KEEPALIVE_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        
        // in case it takes us a little while to do this, we try and do it 
        //  shortly before the keep alive period expires
        // it means we're pinging slightly more frequently than necessary 
        
        AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);  
        if (Build.VERSION.SDK_INT < 19) {
        	alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + keepAliveInterval, pendingIntent);
        } else {
        	alarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + keepAliveInterval, pendingIntent);
        }
    }
    
    /*
     * Used to implement a keep-alive protocol at this Service level - it sends 
     *  a PUBLISH message to the server, then schedules another PUBLISH after an 
     *  interval defined by keepAliveInterval
     */
    private BroadcastReceiver keepAliveSender = new BroadcastReceiver() 
    {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            // Note that we don't need a wake lock for this method (even though
            //  it's important that the phone doesn't switch off while we're
            //  doing this).
            // According to the docs, "Alarm Manager holds a CPU wake lock as 
            //  long as the alarm receiver's onReceive() method is executing. 
            //  This guarantees that the phone will not sleep until you have 
            //  finished handling the broadcast."
            // This is good enough for our needs.
            
    		keepAlive();

            // start the next keep alive period 
            scheduleNextKeepAlive();
        }
    };
	
	private void keepAlive() {
		if (mEnabled == true && mqttAgent != null) {
			PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "anPush");
            wl.acquire();
			Runnable keepAliveThread = new Runnable() {
				public void run() {
					PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
					WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "anPush");
		            wl.acquire();
					// Send a keep alive, if there is a connection.
					Log.d(LOG_TAG, "start to send keepAlive");
					mqttAgent.sendKeepAlive();
					Log.d(LOG_TAG, "finish send keepAlive. releasing wakelock");
					wl.release();
				}
			};
			Thread thread = new Thread(keepAliveThread);
			thread.start();
			wl.release();
		}
	}

	// Remove all scheduled keep-alive
	private void stopKeepAlives() {
		LogUtil.getInstance().debug(LOG_NAME, "Stop keepAlive");
		Intent intent = new Intent(ACTION_KEEPALIVE);
		PendingIntent pi = PendingIntent.getBroadcast(this, KEEPALIVE_ID, intent, 0);
		AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmMgr.cancel(pi);
	}

	// We schedule a reconnect based on the start time of the service
	public void scheduleReconnect(long startTime) {
		if(!mEnabled){
			LogUtil.getInstance().warn(LOG_TAG, LOG_NAME, "Push service is disabled. Will not schedule reconnection.");
			return;
		}
		long interval = mPrefs.getLong(PREF_RETRY, INITIAL_RETRY_INTERVAL);
		int pushRetrytime = mPrefs.getInt(PREF_PUSH_HOST_RETRYTIME, 0);
		interval = Math.min(interval * 2, MAXIMUM_RETRY_INTERVAL);
		Log.i(LOG_TAG, "Rescheduling connection in " + (interval / 1000) + "secs.");
		Editor editor = mPrefs.edit();
		editor.putLong(PREF_RETRY, interval);
		editor.putInt(PREF_PUSH_HOST_RETRYTIME, pushRetrytime + 1);
		if(pushRetrytime > MAXIMUM_PUSH_HOST_RETRYTIME){
			// If reaches max retry time, push host will be cleared
			editor.remove(PushService.PREF_PUSH_HOST);
			editor.remove(PushService.PREF_PUSH_PORT);
			editor.remove(PushService.PREF_PUSH_HOST_EXPIRATION);
		}
		editor.commit();
		long now = System.currentTimeMillis();

		// Schedule a reconnect using the alarm manager.
		Intent i = new Intent();
		i.setClass(this, PushService.class);
		i.setAction(ACTION_RECONNECT);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
		if (Build.VERSION.SDK_INT < 19) {
			alarmMgr.set(AlarmManager.RTC_WAKEUP, now + interval, pi);
		} else {
			alarmMgr.setExact(AlarmManager.RTC_WAKEUP, now + interval, pi);
		}
	}

	// Remove the scheduled reconnect
	public void cancelReconnect() {
		Intent i = new Intent();
		i.setClass(this, PushService.class);
		i.setAction(ACTION_RECONNECT);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmMgr.cancel(pi);
	}
	
	private void reconnect() {
		LogUtil.getInstance().debug(
				LOG_TAG,
				LOG_NAME,
				"[Reconnect] current state: " + mEnabled);
		
		if (mEnabled == true) {
			Log.d(LOG_TAG, "Reconnect push service");
			if (mqttAgent != null) {
				Log.d(LOG_TAG, "Current status: " + mqttAgent.getStatus().name());
				if(mqttAgent.getStatus().equals(MQTTConnectionStatus.Connected)) {
					Runnable mqttDisconnectThread = new Runnable(){
						public void run() {
							mqttAgent.disconnect();
						}
					};
					Thread thread = new Thread(mqttDisconnectThread);
					thread.start();
				}
			}
			cancelReconnect();
			connect();
		}
	}
	
	private synchronized void reconnectIfNecessary() {
		if (mEnabled == true) {
			Log.d(LOG_TAG, "Push service status:" + (mqttAgent == null ? "N/A" : mqttAgent.getStatus().name()));
			if (mqttAgent == null || mqttAgent.getStatus().equals(MQTTConnectionStatus.Disconnect)
					|| mqttAgent.getStatus().equals(MQTTConnectionStatus.Problematic)) {
				LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "Reconnecting...");
				connect();
			}
		}
	}

	private void onConnectivityChanged() {
		LogUtil.getInstance().debug(LOG_TAG, LOG_NAME, "On connectivity changed...");
		
		// Get network info
		NetworkInfo info = ((ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		    
		// Is there connectivity?
		boolean hasConnectivity = (info != null && info.isConnected()) ? true : false;
		String typeName = (info != null) ? info.getTypeName() : "notype";
		String reason = (info != null) ? info.getReason() : "noreason";
		
		boolean networkAvailability = (info != null && info.isConnected());

		LogUtil.getInstance().debug(
				LOG_TAG,
				LOG_NAME,
				"Connectivity changed. Network availability:" + networkAvailability + "; Has connectivity:" + hasConnectivity
						+ "; type:" + typeName + "; reason:" + reason);
		
		if (networkAvailability && hasConnectivity) {
			// sometimes we can not get connectionLost notification from mqtt client when network changing between wifi and 2g
			// in this case, mqttAgent.getStatus() will return MQTTConnectionStatus.Connected which is not true
			// we have to do force reconnection
			reconnect();
		} else {
			if (mqttAgent != null) {
				// if there no connectivity, make sure MQTT connection is destroyed
				cancelReconnect();
				mqttAgent.terminate();
			}
		}
	}

	// Invoke notification shower from listener
	private void showNotification(JSONObject payload) {
		Intent intent = new Intent(ACTION_MSG_ARRIVAL);
		intent.setPackage(getBaseContext().getPackageName());
		intent.putExtra("payload", payload.toString());
		
		sendBroadcast(intent);
	}

	// Check if we are online
	private boolean isNetworkAvailable() {
		NetworkInfo info = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		return info.isConnected();
	}
	
	private interface GetPushHostCallback {
		void receivedPushHostInfo(String pushHostName, int pushPort);
		void failedReceivePushHostInfo(Throwable expception);
	}
	
	private void getServiceHostAsync(final Context androidContext, final String deviceToken, final GetPushHostCallback callback) {
		if (androidContext == null) {
			throw new NullPointerException("Android Context cannot be null!");
		}
		if (callback == null) {
			throw new NullPointerException("DeviceTokenCallback cannot be null!");
		}
		
		Runnable getPushHostThread = new Runnable(){
			public void run() {
				try{
					IAnPushUtility util = new AnPushUtility(androidContext);
					KeyValuePair<String, Integer> pushHostInfo = util.getServiceHost(deviceToken);
					if (pushHostInfo!=null && pushHostInfo.getKey() != null && !"".equals(pushHostInfo.getKey())) {
						callback.receivedPushHostInfo(pushHostInfo.getKey(), pushHostInfo.getValue());
					} else {
						Log.w(LOG_TAG, "Push server is null.");
					}
				}catch(ArrownockException ex){
					callback.failedReceivePushHostInfo(ex);
				}
			}
		};
		Thread thread = new Thread(getPushHostThread);
		thread.start();

	}
	
	private class MQTTEvent implements IMQTTEvent {
		public void generalFail(Throwable exception) {
			LogUtil.getInstance().error(
					LOG_TAG,
					LOG_NAME,
					"General Problem occured. Exception Type: " + exception.getClass().getName() + "; Exception Message: "
							+ exception.getMessage(), exception);
		}

		public void connected() {
			LogUtil.getInstance().info(LOG_TAG, LOG_NAME, "Push service connected");
			Editor editor = mPrefs.edit();
			editor.putLong(PREF_RETRY, INITIAL_RETRY_INTERVAL);
			editor.putInt(PREF_PUSH_HOST_RETRYTIME, 0);
			editor.commit();
			// Save start time
			mStartTime = System.currentTimeMillis();
			// Star the keepalives
			setKeepAliveInterval();
			scheduleNextKeepAlive();
		}

		@Override
		public void disconnected() {

		}

		public void disconnected(Throwable exception) {
			try {
				AnPush instance = AnPush.getInstance(getApplicationContext());
				if(instance != null && instance.getCallback() != null) {
					instance.getCallback().statusChanged(getCurrentStatus(), null);
				}
			} catch (ArrownockException e) {
				//mute
			}
			LogUtil.getInstance().info(LOG_TAG, LOG_NAME, "Push service disonnected");
			mqttAgent.terminate();
			
			if (wasEnabled()) {
				Log.w(LOG_TAG, "Push Service status:" + wasEnabled());
				// TODO
				// mqttAgent.disconnect();
				// mqttAgent.connect();
				connect();
			}
			stopKeepAlives();
		}

		public void failConnect(Throwable exception) {
			try {
				AnPush instance = AnPush.getInstance(getApplicationContext());
				if(instance != null && instance.getCallback() != null) {
					instance.getCallback().statusChanged(getCurrentStatus(), new ArrownockException(exception.getMessage(), ArrownockException.PUSH_FAILED_CONNECT));
				}
			} catch (ArrownockException e) {
				//mute
			}
			LogUtil.getInstance().error(
					LOG_TAG,
					LOG_NAME,
					"Failed to connect to push server. Exception Type: " + exception.getClass().getName() + "; Exception Message: "
							+ exception.getMessage(), exception);
			
			if (mEnabled && isNetworkAvailable()) {
				scheduleReconnect(mStartTime);
			}
		}

		public void failDisconnect(Throwable exception) {
			try {
				AnPush instance = AnPush.getInstance(getApplicationContext());
				if(instance != null && instance.getCallback() != null) {
					instance.getCallback().statusChanged(getCurrentStatus(), new ArrownockException(exception.getMessage(), ArrownockException.PUSH_FAILED_DISCONNECT));
				}
			} catch (ArrownockException e) {
				//mute
			}
			LogUtil.getInstance().error(
					LOG_TAG,
					LOG_NAME,
					"Failed to disconnect from push server. Exception Type: " + exception.getClass().getName()
							+ "; Exception Message: " + exception.getMessage(), exception);
		}

		public void messageArrived(String topic, String payload) {
			// Show a notification
			if (payload == null || payload.equals(""))
				return;

			Log.d(LOG_TAG, "New message arrived. Creating wakelock.");
			PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
			WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "anPush");
            wl.acquire();
            
			JSONObject payloadJson = null;
			try {
				payloadJson = new JSONObject(payload);
			} catch (JSONException ex) {
				// Not a Json
				payloadJson = new JSONObject();
				JSONObject androidJsonPart = new JSONObject();
				try {
					androidJsonPart.put("alert", payload);
					payloadJson.put("android", androidJsonPart);
				} catch (JSONException ex1) {
				}
			}

			//LogUtil.getInstance().debug(LOG_NAME, "Ready to show notification");
			showNotification(payloadJson);
			// receiving this message will have kept the connection alive for us, so
	        //  we take advantage of this to postpone the next scheduled ping
	        scheduleNextKeepAlive();
	        
	        wl.release();
	        Log.d(LOG_TAG, "Releasing wakelock.");
		}

		@Override
		public void messagePublished(String s) {

		}

		public void messagePublished(String msgid, String extraPayload) {
			if ("keepalive".equals(msgid)) {
				Log.d(LOG_TAG, "send keepalive complete");
			}
		}

		public void failPublish(String msgid, Throwable exception) {
			if ("keepalive".equals(msgid)) {
				Log.d(LOG_TAG, "send keepalive failed");
			}
		}

		public void topicSubscribed(String topicName, int qos) {
			try {
				AnPush instance = AnPush.getInstance(getApplicationContext());
				if(instance != null && instance.getCallback() != null) {
					instance.getCallback().statusChanged(AnPushStatus.ENABLE, null);
				}
			} catch (ArrownockException e) {
				//mute
			}
		}

		public void failSubscribe(String topicName, Throwable exception) {
			try {
				AnPush instance = AnPush.getInstance(getApplicationContext());
				if(instance != null && instance.getCallback() != null) {
					instance.getCallback().statusChanged(getCurrentStatus(), new ArrownockException(exception.getMessage(), ArrownockException.PUSH_FAILED_CONNECT));
				}
			} catch (ArrownockException e) {
				//mute
			}
		}
		
		private AnPushStatus getCurrentStatus() {
			if(mqttAgent != null) {
				if(MQTTConnectionStatus.Connected.equals(mqttAgent.getStatus())) {
					return AnPushStatus.ENABLE;
				}
			}
			return AnPushStatus.DISABLE;
		}
	}
	
	private String getProcessName() {
		try {
			File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
			mBufferedReader = new BufferedReader(new FileReader(file));
			return mBufferedReader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void releaseIO(){
		if(mBufferedReader != null){
			try {
				mBufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mBufferedReader = null;
		}
	}
	
	private boolean isEmulator() {
		String abi = Build.CPU_ABI;
		if (abi.startsWith("armeabi-v7a")) {
			return false;
		}else if(abi.startsWith("x86")) {
			return true;
		}else{
			return false;
		}
	}
	
	private void initKeepAliveJob() {
		if(Build.VERSION.SDK_INT > 20) {
			try {
				keepaliveJobInfo = new JobInfo.Builder(1, new ComponentName(getPackageName(), NotificationService.class.getName()))
	        	.setBackoffCriteria(2000, JobInfo.BACKOFF_POLICY_LINEAR)
	        	.setPersisted(true)
	        	.setPeriodic(100)
	        	.build();
			
				keepaliveScheduler = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
				keepaliveScheduler.cancelAll();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void startKeepAliveJob() {
		if(Build.VERSION.SDK_INT > 20) {
			try {
				int result = keepaliveScheduler.schedule(keepaliveJobInfo);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			Log.d(LOG_TAG, "Starting keep alive job...");
		}
	}
}