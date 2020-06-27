package com.arrownock.internal.push;

public class NativeAPIs {
	static{
		try {
			System.loadLibrary("AnPush");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void onDaemonDead(){
		IDaemonStrategy.Fetcher.fetchStrategy().onDaemonDead();
    }
	
	public native void runDaemon(String pkgName, String svcName, String daemonPath);
	public native void startDaemon(String indicatorSelfPath, String indicatorDaemonPath, String observerSelfPath, String observerDaemonPath);
	
}
