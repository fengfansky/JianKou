package com.arrownock.live;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaConstraints.KeyValuePair;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.arrownock.exception.ArrownockException;
import com.arrownock.internal.live.ISignalController;
import com.arrownock.internal.live.MediaStreamsViewListener;
import com.arrownock.live.MediaStreamsRenderer.ScalingType;

public class AnLive implements MediaStreamsViewListener {
	// private static final String TAG = "AnLive";
	private static AnLive live = null;
	private ISignalController signalController = null;
	private IAnLiveEventListener listener = null;
	private Context context = null;
	private LocalVideoView localView = null;
	private IStartCallCallback startSessionCallback = null;
	
	private boolean isInSession = false;
	private String currentSessionId = null;
	private String currentMediaType = null;
	private String remotePartyId = null;
	private Map<String, String> notificationData = null;
	
	private VideoCapturer videoCapturer = null;
	private VideoSource videoSource = null;
	private VideoTrack localVideoTrack = null;
	private VideoRenderer localVideoRenderer = null;
	private AudioSource audioSource = null;
	private AudioTrack localAudioTrack = null;
	private MediaStream localMediaStream = null;
	private PeerConnectionFactory factory = null;
	private Map<String, PeerConnectionManager> pcms = null;
	private int localCameraOrientation = 0;
	
	private AnLive(Context context, ISignalController signalController, IAnLiveEventListener listener) throws ArrownockException {
		if(signalController == null) {
			throw new ArrownockException("anIM instance cannot be null", ArrownockException.LIVE_INVALID_IM_INSTANCE);
		}
		if(listener == null) {
			throw new ArrownockException("AnLiveEventListener cannot be null", ArrownockException.LIVE_INVALID_LISTENER);
		}
		this.signalController = signalController;
		this.signalController.setCallbacks(signalCallback);
		this.listener = listener;
		this.context = context;
		this.pcms = new HashMap<String, PeerConnectionManager>();
		Logging.enableTracing("logcat:", EnumSet.of(Logging.TraceLevel.TRACE_ERROR), Logging.Severity.LS_ERROR);
		//Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler());
	}
	
	public static AnLive initialize(Context context, ISignalController signalController, IAnLiveEventListener listener) throws ArrownockException {
		if(live == null) {
			live = new AnLive(context, signalController, listener);
		}
		return live;
	}
	
	public void videoCall(String partyId, boolean videoOn, Map<String, String> notificationData, IStartCallCallback callback) {
		this.call(partyId, false, videoOn, notificationData, callback);
	}
	
	public void voiceCall(String partyId, Map<String, String> notificationData, IStartCallCallback callback) {
		this.call(partyId, true, false, notificationData, callback);
	}
	
	private void call(String partyId, boolean audioOnly, boolean withVideo, Map<String, String> notificationData, IStartCallCallback callback) {
		if(isInSession) {
			if(callback != null) {
				callback.onFailure(new ArrownockException("Cannot start a new video call while still in one.", ArrownockException.LIVE_ALREADY_IN_CALL));
			}
		} else {
			try {
				if(partyId == null || partyId.trim().isEmpty()) {
					throw new ArrownockException("Invalid partyId.", ArrownockException.LIVE_INVALID_CLIENT_ID);
				}
				resetResource();
				this.notificationData = notificationData;
				isInSession = true;
				prepareLocalMedia(audioOnly, withVideo);
				startSessionCallback = callback;
				List<String> partyIds = new ArrayList<String>();
				partyIds.add(partyId);
				partyIds.add(signalController.getPartyId());
				signalController.createSession(partyIds, audioOnly?"voice":"video");
			} catch(ArrownockException e) {
				if(callback != null) {
					callback.onFailure(e);
				}
			}
		}
	}
	
	public void hangup() {
		if(isInSession) {
			if(remotePartyId != null) {
				List<String> partyIds = new ArrayList<String>();
				partyIds.add(remotePartyId);
				signalController.sendHangup(partyIds);
			}
			if(currentSessionId != null) {
				terminateSession(currentSessionId);
			}
		}
		reset();
	}
	
	public void answer(boolean enableVideo) throws ArrownockException {
		resetResource();
		if(isInSession && currentSessionId != null && remotePartyId != null) {
			if("voice".equals(currentMediaType)) {
				prepareLocalMedia(true, false);
			} else {
				prepareLocalMedia(false, enableVideo);
			}
			PeerConnectionManager pcm = new PeerConnectionManager(signalController, remotePartyId, context, listener);

			// create the peer connection for this party
			pcm.createPeerConnection(factory, localMediaStream);
			pcm.setLocalCameraOrientation(localCameraOrientation);
			
			// sending offer to whoever already in the conference
			pcm.createOffer();
			pcms.put(remotePartyId, pcm);
		}
	}
	
	public boolean isOnCall() {
		return isInSession;
	}
	
	public String getCurrentSessionType() {
		return currentMediaType;
	}
	
	private void terminateSession(String sessionId) {
		signalController.terminateSession(sessionId);
	}
	
	private synchronized void reset() {
		isInSession = false;
		currentSessionId = null;
		currentMediaType = null;
		remotePartyId = null;
		notificationData = null;
		resetResource();
	}
	
	private synchronized void resetResource() {
		try {
			if(Looper.myLooper() != Looper.getMainLooper()) {
				Handler mainHandler = new Handler(context.getMainLooper());
				mainHandler.post(new Runnable() {
				    @Override
				    public void run() {
				    	resetMediaResource();
				    }
				});
			} else {
				resetMediaResource();
			}
		} catch(Exception e) {
			// mute
		}
	}
	
	private synchronized void resetMediaResource() {
		try {
			if(localVideoTrack != null) {
				if(localMediaStream != null) {
					localMediaStream.removeTrack(localVideoTrack);
				}
				localVideoTrack.removeRenderer(localVideoRenderer);
			}
			if(videoSource != null) {
				videoSource.stop();
			}
			if(pcms != null) {
				for(PeerConnectionManager pcm : pcms.values()) {
					pcm.dispose();
				}
				pcms.clear();
			} else {
				pcms = new HashMap<String, PeerConnectionManager>();
			}
			
			if(factory != null) {
				factory.dispose();
			}
		} catch(Exception e) {
			//mute
		} finally {
			videoSource = null;
			videoCapturer = null;
			localVideoTrack = null;
			audioSource = null;
			localAudioTrack = null;
			localMediaStream = null;
			localVideoRenderer = null;
			localView = null;
			factory = null;
		}
	}
	
	private LocalVideoView prepareLocalMedia(boolean audioOnly, boolean initVideoOn) throws ArrownockException {
		if(audioOnly) {
			PeerConnectionFactory.initializeAndroidGlobals(context, true, false, false, null);
			factory = new PeerConnectionFactory();
			localMediaStream = factory.createLocalMediaStream("ARDAMS");
		} else {
			try {
				MediaConstraints videoConstraints = new MediaConstraints();
//			    videoConstraints.mandatory.add(new KeyValuePair("maxWidth", "1280"));
//			    videoConstraints.mandatory.add(new KeyValuePair("minWidth", "640"));
//			    videoConstraints.mandatory.add(new KeyValuePair("maxHeight", "720"));
//			    videoConstraints.mandatory.add(new KeyValuePair("minHeight", "480"));
				
				localView = new LocalVideoView(context);
				localView.setMediaStreamsViewListener(this);
				MediaStreamsRenderer localMediaStreamsRenderer = new MediaStreamsRenderer(localView);
				PeerConnectionFactory.initializeAndroidGlobals(context, true, true, true, localMediaStreamsRenderer.getEGLContext());
				factory = new PeerConnectionFactory();
				localMediaStream = factory.createLocalMediaStream("ARDAMS");
				
				videoCapturer = getVideoCapturer();
				if(videoCapturer == null) {
					reset();
					throw new ArrownockException("Failed to find camera", ArrownockException.LIVE_FAILED_INIT_CAMERA);
				}
				videoSource = factory.createVideoSource(videoCapturer, videoConstraints);
				localVideoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
				localVideoRenderer = new VideoRenderer(localMediaStreamsRenderer.create(0, 0, 100, 100, ScalingType.SCALE_FILL, true));
				localVideoTrack.addRenderer(localVideoRenderer);
				localMediaStream.addTrack(localVideoTrack);
			} catch (Exception e) {
				reset();
				throw new ArrownockException("Failed to initialize local media.", e, ArrownockException.LIVE_FAILED_PREPARE_LOCAL_MEDIA);
			}
		}
		
		try {
			audioSource = factory.createAudioSource(new MediaConstraints());
			localAudioTrack = factory.createAudioTrack("ARDAMSa0", audioSource);
			localMediaStream.addTrack(localAudioTrack);
		} catch (Exception e) {
			throw new ArrownockException("Failed to initialize local media.", e, ArrownockException.LIVE_FAILED_PREPARE_LOCAL_MEDIA);
		}
		return localView;
	}
	
	private VideoCapturer getVideoCapturer() {
		String[] cameraFacing = { "front", "back" };
		int[] cameraIndex = { 0, 1 };
		int[] cameraOrientation = { 0, 90, 180, 270 };
		for (String facing : cameraFacing) {
			for (int index : cameraIndex) {
				for (int orientation : cameraOrientation) {
					String name = "Camera " + index + ", Facing " + facing + ", Orientation " + orientation;
					VideoCapturer capturer = VideoCapturer.create(name);
					if (capturer != null) {
						this.localCameraOrientation = orientation;
						return capturer;
					}
				}
			}
		}
		return null;
	}
	
	public void setVideoState(VideoState state) {
		this.setVideoEnabled(state == VideoState.ON);
	}
	
	public void setAudioState(AudioState state) {
		this.setAudioEnabled(state == AudioState.ON);
	}
	
	private PeerConnectionManager getPeerConnectionManager(String partyId) {
		PeerConnectionManager pcm = pcms.get(partyId);
		if(pcm == null) {
			pcm = new PeerConnectionManager(signalController, partyId, context, listener);
			
			// create the peer connection for this party
			pcm.createPeerConnection(factory, localMediaStream);
			pcms.put(partyId, pcm);
		}
		pcm.setLocalCameraOrientation(localCameraOrientation);
		return pcm;
	}
	
	private ISignalController.Callbacks signalCallback = new ISignalController.Callbacks() {
		@Override
		public void onSessionCreated(String sessionId, List<String> partyIds, String type, ArrownockException error) {
			if(error != null) {
				if(startSessionCallback != null) {
					try {
						startSessionCallback.onFailure(error);
					} catch(Exception e) {
						//mute
					}
				}
			} else {
				currentSessionId = sessionId;
				isInSession = true;
				for(String partyId : partyIds) {
					if(partyId != null && !partyId.equals(signalController.getPartyId())) {
						remotePartyId = partyId;
						break;
					}
				}
				if(startSessionCallback != null) {
					try {
						startSessionCallback.onReady(sessionId);
					} catch(Exception e) {
						//mute
					}
				}
				if(signalController != null) {
					try {
						signalController.sendInvitations(sessionId, partyIds, type, notificationData);
					} catch (Exception e) {
						signalController.terminateSession(sessionId);
						//e.printStackTrace();
					}
				}
			}
			startSessionCallback = null;
		}
		
		@Override
		public void onOfferRecieved(String partyId, String sdp, int orientation) {
			PeerConnectionManager pcm = getPeerConnectionManager(partyId);
			pcm.setRemoteDescription(SessionDescription.Type.OFFER, sdp);
			
			// received an offer, send back my answer
			pcm.createAnswer();
		}

		@Override
		public void onAnswerRecieved(String partyId, String sdp, int orientation) {
			PeerConnectionManager pcm = getPeerConnectionManager(partyId);
			pcm.setRemoteDescription(SessionDescription.Type.ANSWER, sdp);
		}

		@Override
		public void onICECandidate(String partyId, String candidateJson) {
			try {
				JSONObject json = new JSONObject(candidateJson);
				PeerConnectionManager pcm = getPeerConnectionManager(partyId);
				pcm.setICECandidate(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onInvitationRecieved(String sessionId) {
			try {
				signalController.validateSession(sessionId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onSessionValidated(boolean isValid, String sessionId, List<String> partyIds, String type, Date date) {
			if(isValid) {
				currentSessionId = sessionId;
				isInSession = true;
				currentMediaType = type;
				remotePartyId = partyIds.get(0);
				try {
					listener.onReceivedInvitation(true, sessionId, partyIds.get(0), type, date);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					if(partyIds == null || partyIds.size() == 0) {
						listener.onReceivedInvitation(false, null, null, null, null);
					} else {
						listener.onReceivedInvitation(false, sessionId, partyIds.get(0), type, date);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onRemoteHangup(String partyId) {
			try {
				listener.onRemotePartyDisconnected(partyId);
				reset();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				
			}
		}
	};

	@Override
	public void onVideoSizeChanged(int width, int height, boolean isLocal, boolean isFirstTime) {
		if(isLocal && listener != null && localView != null) {
			if(isFirstTime) {
				listener.onLocalVideoViewReady(localView);
            } else {
            	listener.onLocalVideoSizeChanged(width, height);
            }
		}
	}

	private void setVideoEnabled(boolean enabled) {
		if(localVideoTrack != null) {
			localVideoTrack.setEnabled(enabled);
			if(pcms != null) {
				Map<String, String> data = new HashMap<String, String>();
				data.put("type", "video");
				data.put("data", enabled?"on":"off");
				for(PeerConnectionManager pcm : pcms.values()) {
					if(pcm != null) {
						pcm.sendDataToRemotePeer(data);
					}
				}
			}
		}
	}

	private void setAudioEnabled(boolean enabled) {
		if(localAudioTrack != null) {
			localAudioTrack.setEnabled(enabled);
			if(pcms != null) {
				Map<String, String> data = new HashMap<String, String>();
				data.put("type", "audio");
				data.put("data", enabled?"on":"off");
				for(PeerConnectionManager pcm : pcms.values()) {
					if(pcm != null) {
						pcm.sendDataToRemotePeer(data);
					}
				}
			}
		}
	}
}
