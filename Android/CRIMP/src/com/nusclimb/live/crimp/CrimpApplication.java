package com.nusclimb.live.crimp;

import java.util.LinkedList;
import java.util.Queue;

import com.nusclimb.live.crimp.request.UploadRequest;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import android.app.Application;
import android.app.NotificationManager;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Crimp application. Provides a queue for upload request.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class CrimpApplication extends Application {
	private static final String TAG = CrimpApplication.class.getSimpleName();
	private static final int NOTIFICATION_ID = 001;
	
	private SpiceManager spiceManager;
	private Queue<UploadRequest> uploadQueue;
	private boolean isUploading;
	private boolean isSpiceManagerStarted;
	private int spiceManagerToken = 1;
	private int uploadSuccessCount, uploadTotalCount;
	private final Handler handler = new Handler();
	NotificationCompat.Builder mBuilder;
	Runnable checkSpiceManagerRunnable;
	
	/*=========================================================================
	 * Inner class
	 *=======================================================================*/
	private class AppUploadRequestListener implements RequestListener<Object> {
		@Override
		public void onRequestFailure(SpiceException e) {
			Log.e(TAG, "Upload failed.");
			
			isUploading = false;
			
			tryProcessRequest();
		}

	     @Override
	     public void onRequestSuccess(Object result) {
	    	 Log.i(TAG, "Upload succeed.");
	    	 
	    	 isUploading = false;
	    	 getQueue().poll();
	    	 uploadSuccessCount++;
	    	 
	    	 if(uploadSuccessCount == uploadTotalCount){
	    		 // Update notification
	    		 getNotificationBuilder().setContentText("Upload complete: "+uploadSuccessCount+"/"+uploadTotalCount)
		    	 .setProgress(0, 0, false);
		    	 
		    	 // Stop spiceManager.
		    	 if(isSpiceManagerStarted && spiceManagerToken == 0){
		    		 Log.d(TAG, "spiceManager should stop...");
		    		 spiceManager.shouldStop();
		    		 spiceManagerToken++;
		 		
		    		 handler.postDelayed(getCheckSpiceManagerRunnable(), 100);
		    	 }
	    	 }
	    	 else{
		    	 // Update notification
	    		 getNotificationBuilder().setContentText("Upload in progress: "+uploadSuccessCount+"/"+uploadTotalCount)
		    	 .setProgress(uploadTotalCount, uploadSuccessCount, false);
	    	 }
	    	 
	    	 // Gets an instance of the NotificationManager service
			 NotificationManager mNotifyMgr =
					 (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			 // Builds the notification and issues it.
			 mNotifyMgr.notify(NOTIFICATION_ID, getNotificationBuilder().build());
	    	 
	    	 tryProcessRequest();
	     }
	}
	
	
	
	/*=========================================================================
	 * Public methods
	 *=======================================================================*/
	@Override
	public void onCreate(){
		super.onCreate();
		
		uploadSuccessCount = 0;
		uploadTotalCount = 0;
		
		spiceManager = new SpiceManager(CrimpService.class);
	}
	
	/**
	 * Add an upload request to queue.
	 * 
	 * @param request Request to be added.
	 */
	public void addRequest(UploadRequest request){
		if(!isSpiceManagerStarted && spiceManagerToken > 0){
			Log.d(TAG, "spiceManager starting...");
			spiceManagerToken--;
			spiceManager.start(this);

			handler.postDelayed(getCheckSpiceManagerRunnable(), 100);
		}
		
		getQueue().offer(request);
		uploadTotalCount++;
		
		// Update notification
		getNotificationBuilder().setContentTitle("CRIMP Score upload")
		.setContentText("Upload in progress: "+uploadSuccessCount+"/"+uploadTotalCount)
		.setProgress(uploadTotalCount, uploadSuccessCount, false);
		
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr = 
				(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Builds the notification and issues it.
		mNotifyMgr.notify(NOTIFICATION_ID, getNotificationBuilder().build());
		
		tryProcessRequest();
	}
	
	
	
	/*=========================================================================
	 * Private methods
	 *=======================================================================*/
	/**
	 * Get the notification builder. Create one if it does not exist.
	 * 
	 * @return Notification builder used for displaying upload status.
	 */
	private NotificationCompat.Builder getNotificationBuilder(){
		if(mBuilder == null){
			mBuilder = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.ic_launcher)
			        .setContentTitle("CRIMP")
			        .setContentText("CRIMP service is running!");
		}
		
		return mBuilder;
	}
	
	/**
	 * Get upload request queue. Create one if it does not exist.
	 * 
	 * @return Upload request queue.
	 */
	private Queue<UploadRequest> getQueue(){
		if(uploadQueue == null){
			uploadQueue = new LinkedList<UploadRequest>();
		}
		
		return uploadQueue;		
	}
	
	/**
	 * Test and process request if possible. No-op if 1) already uploading
	 * and/or 2) queue is empty and/or 3) spiceManager not started.
	 */
	private void tryProcessRequest(){
		if(!isUploading && !getQueue().isEmpty() && isSpiceManagerStarted){
			UploadRequest nextRequest = getQueue().peek();
			
			isUploading = true;
			spiceManager.execute(nextRequest, nextRequest.createCacheKey(),
					DurationInMillis.ALWAYS_EXPIRED, new AppUploadRequestListener());
		}
	}
	
	/**
	 * Get the runnable object for checking spiceManager. Create one if it 
	 * does not exist.
	 * 
	 * @return A runnable object that checks spiceManager for state change.
	 */
	private Runnable getCheckSpiceManagerRunnable(){
		if(checkSpiceManagerRunnable == null){
			checkSpiceManagerRunnable = new Runnable() {
				@Override 
			    public void run() {
					if(isSpiceManagerStateChange()){
						// State changed. Try process queue.
						Log.d(TAG, "spiceManager change: "+isSpiceManagerStarted);
						tryProcessRequest();
					}
					else{
						handler.postDelayed(getCheckSpiceManagerRunnable(), 100);
					}
			    }
			};
		}
		
		return checkSpiceManagerRunnable;
	}
	
	/**
	 * Check for spiceManager toggle between start and not started.
	 * 
	 * @return True if spiceManager state change.
	 */
	private boolean isSpiceManagerStateChange(){
		boolean state = spiceManager.isStarted();
		if(state != isSpiceManagerStarted){
			isSpiceManagerStarted = state;
			return true;
		}
		else{
			return false;
		}
	}
	
}
