package com.arrownock.push;

import com.arrownock.exception.ArrownockException;

public interface IAnPushCallback {
    @Deprecated
	void register(boolean err, final String anid, ArrownockException exception);
    @Deprecated
	void unregister(boolean err, ArrownockException exception);
    @Deprecated
	void setMute(boolean err, ArrownockException expception);
    @Deprecated
	void setScheduledMute(boolean err, ArrownockException expception);
    @Deprecated
	void clearMute(boolean err, ArrownockException expception);
    @Deprecated
	void setSilentPeriod(boolean err, ArrownockException expception);
    @Deprecated
	void clearSilentPeriod(boolean err, ArrownockException expception);
	void statusChanged(AnPushStatus currentStatus, ArrownockException exception);
	@Deprecated
	void setBadge(boolean err, ArrownockException exception);
}
