package com.arrownock.im.callback;

import com.arrownock.im.AnIMMessage;

public interface IAnIMCallback {
    @Deprecated
	void getClientId(AnIMGetClientIdCallbackData data);
	void statusUpdate(AnIMStatusUpdateCallbackData data);
	void messageSent(AnIMMessageSentCallbackData data);
	void receivedReceiveACK(AnIMReceiveACKCallbackData data);
	void receivedReadACK(AnIMReadACKCallbackData data);
	void receivedMessage(AnIMMessageCallbackData data);
	void receivedBinary(AnIMBinaryCallbackData data);
	void receivedTopicMessage(AnIMTopicMessageCallbackData data);
	void receivedTopicBinary(AnIMTopicBinaryCallbackData data);
	void receivedNotice(AnIMNoticeCallbackData data);
	@Deprecated
	void createTopic(AnIMCreateTopicCallbackData data);
	@Deprecated
	void updateTopic(AnIMUpdateTopicCallbackData data);
	@Deprecated
	void addClientsToTopic(AnIMAddClientsCallbackData data);
	@Deprecated
	void removeClientsFromTopic(AnIMRemoveClientsCallbackData data);
	@Deprecated
	void removeTopic(AnIMRemoveTopicCallbackData data);
	@Deprecated
	void bindAnPushService(AnIMBindAnPushServiceCallbackData data);
	@Deprecated
	void unbindAnPushService(AnIMUnbindAnPushServiceCallbackData data);
	@Deprecated
	void getTopicInfo(AnIMGetTopicInfoCallbackData data);
	@Deprecated
	void getClientsStatus(AnIMGetClientsStatusCallbackData data);
	void getSessionInfo(AnIMGetSessionInfoCallbackData data);
	@Deprecated
	void getTopicList(AnIMGetTopicListCallbackData data);
	void receivedCreateTopicEvent(AnIMCreateTopicEventData data);
	void receivedUpdateTopicEvent(AnIMUpdateTopicEventData data);
	void receivedAddClientsToTopicEvent(AnIMAddClientsEventData data);
	void receivedRemoveClientsFromTopicEvent(AnIMRemoveClientsEventData data);
	void receivedRemoveTopicEvent(AnIMRemoveTopicEventData data);
	
	void messageSentFromOtherDevice(AnIMMessage message);
}
