package com.arrownock.live;

import com.arrownock.exception.ArrownockException;

import java.util.Date;

public interface IAnLiveEventListener {
	void onReceivedInvitation(boolean isValid, String sessionId, String partyId, String type, Date createdAt);
	
	void onLocalVideoViewReady(LocalVideoView view);
	void onLocalVideoSizeChanged(int width, int height);
	
	void onRemotePartyConnected(String partyId);
	void onRemotePartyDisconnected(String partyId);
	
	void onRemotePartyVideoViewReady(String partyId, VideoView view);
	void onRemotePartyVideoSizeChanged(String partyId, int width, int height);
	void onRemotePartyVideoStateChanged(String partyId, VideoState state);
	void onRemotePartyAudioStateChanged(String partyId, AudioState state);
	
	void onError(String partyId, ArrownockException e);
}
