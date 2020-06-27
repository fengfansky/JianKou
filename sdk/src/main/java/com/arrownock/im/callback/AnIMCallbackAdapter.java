package com.arrownock.im.callback;

import com.arrownock.im.AnIMMessage;

public class AnIMCallbackAdapter implements IAnIMCallback {

	@Override
	@Deprecated
	public void getClientId(AnIMGetClientIdCallbackData daa) {

	}

	@Override
	public void statusUpdate(AnIMStatusUpdateCallbackData data) {

	}

	@Override
	public void messageSent(AnIMMessageSentCallbackData data) {

	}

	@Override
	public void receivedReceiveACK(AnIMReceiveACKCallbackData data) {

	}

	@Override
	public void receivedReadACK(AnIMReadACKCallbackData data) {

	}

	@Override
	public void receivedMessage(AnIMMessageCallbackData data) {

	}

	@Override
	public void receivedBinary(AnIMBinaryCallbackData data) {

	}

	@Override
	public void receivedTopicMessage(AnIMTopicMessageCallbackData data) {

	}

	@Override
	public void receivedTopicBinary(AnIMTopicBinaryCallbackData data) {

	}

	@Override
	public void receivedNotice(AnIMNoticeCallbackData data) {

	}

	@Override
	@Deprecated
	public void createTopic(AnIMCreateTopicCallbackData data) {

	}

	@Override
	@Deprecated
	public void addClientsToTopic(AnIMAddClientsCallbackData data) {

	}

	@Override
	@Deprecated
	public void removeClientsFromTopic(AnIMRemoveClientsCallbackData data) {

	}

	@Override
	@Deprecated
	public void bindAnPushService(AnIMBindAnPushServiceCallbackData data) {

	}

	@Override
	@Deprecated
	public void unbindAnPushService(AnIMUnbindAnPushServiceCallbackData data) {

	}

	@Override
	@Deprecated
	public void getTopicInfo(AnIMGetTopicInfoCallbackData data) {

	}

	@Override
	@Deprecated
	public void getClientsStatus(AnIMGetClientsStatusCallbackData data) {

	}
	
	@Override
	public void getSessionInfo(AnIMGetSessionInfoCallbackData data) {

	}
	
	@Override
	@Deprecated
	public void getTopicList(AnIMGetTopicListCallbackData data) {

	}

	@Override
	@Deprecated
	public void updateTopic(AnIMUpdateTopicCallbackData data) {
		
	}

	@Override
	@Deprecated
	public void removeTopic(AnIMRemoveTopicCallbackData data) {
		
	}

    @Override
    public void receivedCreateTopicEvent(AnIMCreateTopicEventData data) {
        
    }

    @Override
    public void receivedUpdateTopicEvent(AnIMUpdateTopicEventData data) {
        
    }

    @Override
    public void receivedAddClientsToTopicEvent(AnIMAddClientsEventData data) {
        
    }

    @Override
    public void receivedRemoveClientsFromTopicEvent(AnIMRemoveClientsEventData data) {
        
    }

    @Override
    public void receivedRemoveTopicEvent(AnIMRemoveTopicEventData data) {
        
    }

	@Override
	public void messageSentFromOtherDevice(AnIMMessage message) {
		
	}
}
