package com.arrownock.live;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.arrownock.exception.ArrownockException;
import com.arrownock.internal.live.ISignalController;
import com.arrownock.internal.live.MediaStreamsViewListener;
import com.arrownock.internal.util.Constants;
import com.arrownock.live.MediaStreamsRenderer.ScalingType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.DataChannel.Buffer;
import org.webrtc.DataChannel.State;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnection.IceGatheringState;
import org.webrtc.PeerConnection.Observer;
import org.webrtc.PeerConnection.SignalingState;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SessionDescription.Type;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PeerConnectionManager implements MediaStreamsViewListener, DataChannel.Observer{
	private String TAG = "PeerConnectionManager";
	
	private PeerConnectionObserver pcObserver = new PeerConnectionObserver();
	private SDPObserver sdpObserver = new SDPObserver();
	
	private PeerConnection pc = null;
	private MediaConstraints sdpMediaConstraints = null;
	private ISignalController signalController = null;
	private String partyId = null;
	private int localCameraOrientation = 0;
	private int remoteCameraOrientation = 0;
	private VideoView remoteView = null;
	private boolean isConnected = false;
	private DataChannel dataChannel = null;
	private VideoTrack remoteVideoTrack = null;
	private VideoRenderer remoteVideoRenderer = null;
	private MediaStream localMediaStream = null;
	
	private Context context = null;
	private IAnLiveEventListener listener = null;
	
	public PeerConnectionManager(ISignalController signalController, String partyId, Context context, IAnLiveEventListener listener) {
		sdpMediaConstraints = new MediaConstraints();
		sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
		sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
		this.partyId = partyId;
		this.signalController = signalController;
		this.context = context;
		this.listener = listener;
	}
	
	public PeerConnection createPeerConnection(PeerConnectionFactory factory, MediaStream localMediaStream) {
		LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
	    iceServers.add(new PeerConnection.IceServer("stun:" + Constants.STUN_SERVER, "", ""));
	    iceServers.add(new PeerConnection.IceServer("turn:" + Constants.TURN_SERVER, Constants.TURN_SERVER_USERNAME, Constants.TURN_SERVER_PASSWORD));

	    MediaConstraints pcConstraints = constraintsFromJSON("{\"mandatory\": {\"DtlsSrtpKeyAgreement\": true}, \"optional\": [{\"RtpDataChannels\":true}]}");
	    pc = factory.createPeerConnection(iceServers, pcConstraints, pcObserver);
	    pc.addStream(localMediaStream);
	    this.localMediaStream = localMediaStream;
	    return pc;
	}
	
	public PeerConnection getPeerConnection() {
		return pc;
	}
	
	public void createOffer() {
		if(pc != null && !isConnected) {
			// create offerer data channel
			createDataChannel();
			
			pc.createOffer(sdpObserver, sdpMediaConstraints);
		}
	}
	
	public void createAnswer() {
		if(pc != null && !isConnected) {			
			pc.createAnswer(sdpObserver, sdpMediaConstraints);
		}
	}
	
	public void setRemoteDescription(Type type, String sdpString) {
		if(pc != null && !isConnected) {
			try {
				SessionDescription sdp = new SessionDescription(type, preferISAC(sdpString));
				pc.setRemoteDescription(sdpObserver, sdp);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setICECandidate(JSONObject json) {
		if(pc != null && !isConnected) {
			try {
				IceCandidate candidate = new IceCandidate((String) json.get("id"), json.getInt("label"), (String) json.get("candidate"));
				pc.addIceCandidate(candidate);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setLocalCameraOrientation(int localCameraOrientation) {
		this.localCameraOrientation = localCameraOrientation;
	}

	public void setRemoteCameraOrientation(int remoteCameraOrientation) {
		this.remoteCameraOrientation = remoteCameraOrientation;
	}
	
	public synchronized void dispose() {
		if(pc != null) {
			pc.dispose();
			pc = null;
			isConnected = false;
		}
		this.localMediaStream = null;
	}
	
	private void sendInitState() {
		// initial send local video/audio status to the connected remote peer
		if(localMediaStream != null) {
	    	if(localMediaStream.videoTracks != null && localMediaStream.videoTracks.size() > 0 && localMediaStream.videoTracks.get(0) != null) {
	    		VideoTrack videoTrack = localMediaStream.videoTracks.get(0);
	            String videoState = null;
	            if(videoTrack.enabled()) {
	                videoState = "on";
	            } else {
	                videoState = "off";
	            }
	            Map<String, String> data = new HashMap<String, String>();
	            data.put("type", "video");
	            data.put("data", videoState);
	            this.sendDataToRemotePeer(data);
	        }
	        if(localMediaStream.audioTracks != null && localMediaStream.audioTracks.size() > 0 && localMediaStream.audioTracks.get(0) != null) {
	            String audioState = null;
	            AudioTrack audioTrack = localMediaStream.audioTracks.get(0);
	            if(audioTrack.enabled()) {
	                audioState = "on";
	            } else {
	                audioState = "off";
	            }
	            Map<String, String> data = new HashMap<String, String>();
	            data.put("type", "audio");
	            data.put("data", audioState);
	            this.sendDataToRemotePeer(data);
	        }
	    }
	}
	
	class PeerConnectionObserver implements Observer {
		@Override
		public void onAddStream(MediaStream stream) {
			Looper.prepare();
			
			/*
			int viewOrientation = remoteCameraOrientation;
			if(remoteCameraOrientation != 0) {
				viewOrientation = 360 - remoteCameraOrientation;
			}
			*/
			
			remoteView = new VideoView(context);
			remoteView.setMediaStreamsViewListener(PeerConnectionManager.this);
			
			if (stream.videoTracks.size() == 1) {
				remoteVideoTrack = stream.videoTracks.get(0);
				remoteVideoRenderer = new VideoRenderer(new MediaStreamsRenderer(remoteView).create(0, 0, 100, 100, ScalingType.SCALE_FILL, false));
				remoteVideoTrack.addRenderer(remoteVideoRenderer);
			}
		}

		@Override
		public void onDataChannel(DataChannel dataChannel) {
			// get data channel as answerer
			PeerConnectionManager.this.dataChannel = dataChannel;
			PeerConnectionManager.this.dataChannel.registerObserver(PeerConnectionManager.this);
			
			sendInitState();
		}

		@Override
		public void onIceCandidate(IceCandidate candidate) {
			if(!isConnected) {
				JSONObject json = new JSONObject();
				try {
					json.put("label", candidate.sdpMLineIndex);
					json.put("id", candidate.sdpMid);
					json.put("candidate", candidate.sdp);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				signalController.sendICECandidate(partyId, json.toString());
			}
		}

		@Override
		public void onIceConnectionChange(IceConnectionState state) {
			switch(state.ordinal()) {
			case 2:  // connected
				isConnected = true;
				listener.onRemotePartyConnected(partyId);
				
				if(remoteView.getVideoWidth() > 0 && remoteView.getVideoHeight() > 0) {
					listener.onRemotePartyVideoViewReady(partyId, remoteView);
	            }
				break;
			case 4:  // failed
				listener.onError(partyId, new ArrownockException("Failed to establish connection", ArrownockException.LIVE_FAILED_ESTABLISH_CONN));
				dispose();
				break;
			case 5: // disconnected
				if(isConnected && partyId != null) {
					dispose();
	                listener.onRemotePartyDisconnected(partyId);
	            }
	            break;
			}
		}

		@Override
		public void onIceGatheringChange(IceGatheringState state) {
			
		}

		@Override
		public void onRemoveStream(MediaStream stream) {
			listener.onRemotePartyDisconnected(partyId);
			isConnected = false;
		}

		@Override
		public void onSignalingChange(SignalingState state) {
			
		}

		@Override
		public void onRenegotiationNeeded() {
			isConnected = false;
		}
	}
	
	class SDPObserver implements SdpObserver {
		@Override
		public void onCreateSuccess(final SessionDescription origSdp) {
			SessionDescription sdp = new SessionDescription(origSdp.type, preferISAC(origSdp.description));
			pc.setLocalDescription(sdpObserver, sdp);
			if(sdp.type == Type.ANSWER) {
				signalController.sendAnswer(partyId, sdp.description, localCameraOrientation);
			} else if(sdp.type == Type.OFFER) {
				signalController.sendOffer(partyId, sdp.description, localCameraOrientation);
			}
		}

		@Override
		public void onSetSuccess() {

		}

		@Override
		public void onCreateFailure(final String error) {
			Log.e("anLive", "create SDP failure: " + error);
		}

		@Override
		public void onSetFailure(final String error) {
			Log.e("anLive", "set SDP failure: " + error);
		}
	}
	
	private MediaConstraints constraintsFromJSON(String jsonString) {
		if (jsonString == null) {
			return null;
		}
		try {
			MediaConstraints constraints = new MediaConstraints();
			JSONObject json = new JSONObject(jsonString);
			JSONObject mandatoryJSON = json.optJSONObject("mandatory");
			if (mandatoryJSON != null) {
				JSONArray mandatoryKeys = mandatoryJSON.names();
				if (mandatoryKeys != null) {
					for (int i = 0; i < mandatoryKeys.length(); ++i) {
						String key = mandatoryKeys.getString(i);
						String value = mandatoryJSON.getString(key);
						constraints.mandatory.add(new MediaConstraints.KeyValuePair(key, value));
					}
				}
			}
			JSONArray optionalJSON = json.optJSONArray("optional");
			if (optionalJSON != null) {
				for (int i = 0; i < optionalJSON.length(); ++i) {
					JSONObject keyValueDict = optionalJSON.getJSONObject(i);
					String key = keyValueDict.names().getString(0);
					String value = keyValueDict.getString(key);
					constraints.optional.add(new MediaConstraints.KeyValuePair(key, value));
				}
			}
			return constraints;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	// Mangle SDP to prefer ISAC/16000 over any other audio codec.
	private String preferISAC(String sdpDescription) {
		String[] lines = sdpDescription.split("\n");
		int mLineIndex = -1;
		String isac16kRtpMap = null;
		Pattern isac16kPattern = Pattern
				.compile("^a=rtpmap:(\\d+) ISAC/16000[\r]?$");
		for (int i = 0; (i < lines.length)
				&& (mLineIndex == -1 || isac16kRtpMap == null); ++i) {
			if (lines[i].startsWith("m=audio ")) {
				mLineIndex = i;
				continue;
			}
			Matcher isac16kMatcher = isac16kPattern.matcher(lines[i]);
			if (isac16kMatcher.matches()) {
				isac16kRtpMap = isac16kMatcher.group(1);
				continue;
			}
		}
		if (mLineIndex == -1) {
			Log.d(TAG, "No m=audio line, so can't prefer iSAC");
			return sdpDescription;
		}
		if (isac16kRtpMap == null) {
			Log.d(TAG, "No ISAC/16000 line, so can't prefer iSAC");
			return sdpDescription;
		}
		String[] origMLineParts = lines[mLineIndex].split(" ");
		StringBuilder newMLine = new StringBuilder();
		int origPartIndex = 0;
		// Format is: m=<media> <port> <proto> <fmt> ...
		newMLine.append(origMLineParts[origPartIndex++]).append(" ");
		newMLine.append(origMLineParts[origPartIndex++]).append(" ");
		newMLine.append(origMLineParts[origPartIndex++]).append(" ");
		newMLine.append(isac16kRtpMap).append(" ");
		for (; origPartIndex < origMLineParts.length; ++origPartIndex) {
			if (!origMLineParts[origPartIndex].equals(isac16kRtpMap)) {
				newMLine.append(origMLineParts[origPartIndex]).append(" ");
			}
		}
		lines[mLineIndex] = newMLine.toString();
		StringBuilder newSdpDescription = new StringBuilder();
		for (String line : lines) {
			newSdpDescription.append(line).append("\n");
		}
		return newSdpDescription.toString();
	}

	private void createDataChannel() {
		DataChannel.Init dcInit = new DataChannel.Init();
	    dcInit.negotiated = false;
	    dcInit.ordered = false;
	    dataChannel = pc.createDataChannel("anLiveDataChannel", dcInit);
	    dataChannel.registerObserver(this);
	}
	
	void sendDataToRemotePeer(Map<String, String> data) {
	    if(dataChannel != null && dataChannel.state() == State.OPEN && data != null && data.size() > 0) {
	    	try {
	    		JSONObject json = new JSONObject(data);
	    		ByteBuffer bb = ByteBuffer.wrap(json.toString().getBytes("UTF-8"));
	    		Buffer buffer = new Buffer(bb, false);
	    		dataChannel.send(buffer);
	    	} catch (Exception e) {
				//mute
			}
	    }
	}
	
	@Override
	public void onVideoSizeChanged(int width, int height, boolean isLocal, boolean isFirstTime) {
		if(!isLocal && listener != null && partyId != null && remoteView != null)  {
            if(isFirstTime) {
            	listener.onRemotePartyVideoViewReady(partyId, remoteView);
            } else {
            	listener.onRemotePartyVideoSizeChanged(partyId, width, height);
            }
		}
	}

	@Override
	public void onStateChange() {
		switch(dataChannel.state()){
		case CONNECTING:
			break;
		case OPEN:
			sendInitState();
			break;
		case CLOSING:
			break;
		case CLOSED:
			dataChannel = null;
			break;
		}
	}

	@Override
	public void onMessage(Buffer buffer) {
		if(listener != null) {
			if(buffer != null && buffer.data != null) {
				byte[] data = new byte[buffer.data.remaining()];
				buffer.data.get(data);
				String message = new String(data, Charset.forName("UTF-8"));
				try {
					JSONObject json = new JSONObject(message);
					if(json != null) {
						String type = json.getString("type");
						String d = json.getString("data");
						if("video".equals(type)) {
							if("on".equals(d)) {
								listener.onRemotePartyVideoStateChanged(partyId, VideoState.ON);
							} else if("off".equals(d)) {
								listener.onRemotePartyVideoStateChanged(partyId, VideoState.OFF);
							}
						} else if("audio".equals(type)) {
							if("on".equals(d)) {
								listener.onRemotePartyAudioStateChanged(partyId, AudioState.ON);
							} else if("off".equals(d)) {
								listener.onRemotePartyAudioStateChanged(partyId, AudioState.OFF);
							}
						}
					}
				} catch (JSONException e) {
					//mute
				}
			}
		}
	}
}
