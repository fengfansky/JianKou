package com.arrownock.anpush;

import com.arrownock.exception.ArrownockException;

public abstract class AnPushCallbackAdapter implements IAnPushCallback {

	@Override
	@Deprecated
	public void register(boolean err, final String anid, ArrownockException exception) {}

	@Override
	@Deprecated
	public void unregister(boolean err, ArrownockException exception) {}

	@Override
	@Deprecated
	public void setMute(boolean err, ArrownockException expception) {}

	@Override
	@Deprecated
	public void setScheduledMute(boolean err, ArrownockException expception) {}

	@Override
	@Deprecated
	public void clearMute(boolean err, ArrownockException expception) {}

	@Override
	@Deprecated
	public void setSilentPeriod(boolean err, ArrownockException expception) {}

	@Override
	@Deprecated
	public void clearSilentPeriod(boolean err, ArrownockException expception) {}

	@Override
	public void statusChanged(AnPushStatus currentStatus, ArrownockException exception) {}
	
	@Override
	@Deprecated
	public void setBadge(boolean err, ArrownockException exception) {}
}
