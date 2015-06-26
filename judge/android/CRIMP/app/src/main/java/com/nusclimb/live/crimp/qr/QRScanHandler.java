package com.nusclimb.live.crimp.qr;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.qr.DecodeHandler;
import com.nusclimb.live.crimp.qr.DecodeThread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class QRScanHandler extends Handler{
	private static final String TAG = QRScanHandler.class.getSimpleName();
	
	private QRScanActivity activity;
	private DecodeThread decodeThread;
	private boolean running;
	
	public QRScanHandler(QRScanActivity activity){
		this.activity = activity;
		decodeThread = new DecodeThread(activity);
		decodeThread.start();
		
		Log.d(TAG, "QRScanHandler constructed");
	}
	
	public void setRunning(boolean running){
		this.running = running;
	}
	
	public DecodeHandler getDecodeHandler(){
		return decodeThread.getHandler();
	}
	
	@Override
	public void handleMessage(Message message) {
	    if(!running){
	    	Log.d(TAG, "QRScanHandler received msg but not running.");
	    	return;
	    }
		switch (message.what) {
	    	case R.id.decode_succeeded:
	    		Log.d(TAG, "QRScanHandler receive msg 'succeed'.");
	    		activity.getCameraManager().stopPreview();
	    		activity.setState(R.id.decode_succeeded);
	    		String result = (String) message.obj;
	    		activity.updateStatusView(result);
	    		//TODO should release camera?
	    		break;
	    	case R.id.decode_failed:
	    		//TODO maybe need check and start preview.
	    		if(activity.getCameraManager().isPreviewing()){
	    			activity.getCameraManager().startScan();
	    		}
	    		else{
	    			Log.w(TAG, "QRScanHandler received decode fail msg. Start scan failed due to not previewing." );
	    		}
	    		break;
    		default:
    			Log.w(TAG, "QRScanHandler received unknown msg.");
    			break;
    	}
  	}
	
	/**
	 * This method will be called when QRScanActivity enter onPause() state. 
	 * Kills the DecodeThread and do stuff. 
	 */
	public void onPause(){
		Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
	    quit.sendToTarget();
	    
	    try {
	    	// Wait at most half a second; should be enough time, and onPause() will timeout quickly
	        decodeThread.join(500L);
	    } catch (InterruptedException e) {
	    	// continue
	    }

	    // Be absolutely sure we don't send any queued up messages
	    removeMessages(R.id.decode_succeeded);
	    removeMessages(R.id.decode_failed);
	}
}
