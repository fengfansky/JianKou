package com.arrownock.internal.live;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.arrownock.exception.ArrownockException;

public interface ISignalController {
	boolean isOnline();
	String getPartyId();
	void createSession(List<String> partyIds, String type);
	void validateSession(String sessionId);
	void terminateSession(String sessionId);
	void sendInvitations(String sessionId, List<String> partyIds, String type, Map<String, String> data);
	void sendHangup(List<String> partyIds);
	
	void sendOffer(String partyId, String sdp, int orientation);
	void sendAnswer(String partyId, String sdp, int orientation);
	void sendICECandidate(String partyId, String candidateJson);
	void setCallbacks(Callbacks callbacks);
	
	interface Callbacks {
		void onSessionCreated(String sessionId, List<String> partyIds, String type, ArrownockException e);
		void onSessionValidated(boolean isValid, String sessionId, List<String> partyIds, String type, Date date);
		void onInvitationRecieved(String sessionId);
		void onRemoteHangup(String partyId);
		void onOfferRecieved(String partyId, String offerJson, int orientation);
		void onAnswerRecieved(String partyId, String answerJson, int orientation);
		void onICECandidate(String partyId, String candidateJson);
	}
}
