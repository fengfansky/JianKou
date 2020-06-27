package com.arrownock.live;

import com.arrownock.internal.live.MediaStreamsViewListener;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class VideoView extends GLSurfaceView {
	protected MediaStreamsViewListener listener = null;
	private MediaStreamsRenderer renderer = null;
	private int videoWidth = 0;
	private int videoHeight = 0;
	
	public VideoView(Context context) {
		super(context);
	}
	
	public int getVideoWidth() {
		return this.videoWidth;
	}
	
	public int getVideoHeight() {
		return this.videoHeight;
	}
	
	void setMediaStreamsViewListener(MediaStreamsViewListener listener) {
		this.listener = listener;
	}
	
	void setVideoSize(final int width, final int height) {
		boolean isLocal = this instanceof LocalVideoView;
		boolean isFirstTime = false;
		if(this.videoWidth <= 0 && this.videoHeight <= 0) {
	        isFirstTime = true;
	    }
	    if(isFirstTime || (this.videoWidth != width || this.videoHeight != height)) {
	    	this.videoWidth = width;
			this.videoHeight = height;
			this.listener.onVideoSizeChanged(width, height, isLocal, isFirstTime);
	    }
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
		super.onSizeChanged(width, height, oldWidth, oldHeight);
		this.renderer.onSurfaceChanged(null, width, height);
	}
	
	@Override
	public void setRenderer(Renderer renderer) {
		super.setRenderer(renderer);
		this.renderer = (MediaStreamsRenderer)renderer;
	}
}
