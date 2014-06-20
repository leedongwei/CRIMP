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
	private int uploadSuccessCount, uploadTotalCount;
	NotificationCompat.Builder mBuilder;

	
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
		    	 mBuilder.setContentText("Upload complete: "+uploadSuccessCount+"/"+uploadTotalCount)
		    	 .setProgress(0, 0, false);
	    	 }
	    	 else{
		    	 // Update notification
		    	 mBuilder.setContentText("Upload in progress: "+uploadSuccessCount+"/"+uploadTotalCount)
		    	 .setProgress(uploadTotalCount, uploadSuccessCount, false);
	    	 }
	    	 
	    	 // Gets an instance of the NotificationManager service
			 NotificationManager mNotifyMgr =
					 (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			 // Builds the notification and issues it.
			 mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
	    	 
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
		spiceManager.start(this);
		 
		buildInitialNotificationAndFire();
	}
	
	/**
	 * Add an upload request to queue.
	 * 
	 * @param request Request to be added.
	 */
	public void addRequest(UploadRequest request){
		getQueue().offer(request);
		uploadTotalCount++;
		
		// Update notification
		mBuilder.setContentTitle("CRIMP Score upload")
		.setContentText("Upload in progress: "+uploadSuccessCount+"/"+uploadTotalCount)
		.setProgress(uploadTotalCount, uploadSuccessCount, false);
		
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr = 
				(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Builds the notification and issues it.
		mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
		
		tryProcessRequest();
	}
	
	
	
	/*=========================================================================
	 * Private methods
	 *=======================================================================*/
	/**
	 * Build initial notification and issue it.
	 */
	private void buildInitialNotificationAndFire(){
		mBuilder = new NotificationCompat.Builder(this)
			        .setSmallIcon(R.drawable.ic_launcher)
			        .setContentTitle("CRIMP")
			        .setContentText("CRIMP service is running!");
		 
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr =
				(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Builds the notification and issues it.
		mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
	}
	
	private Queue<UploadRequest> getQueue(){
		if(uploadQueue == null){
			uploadQueue = new LinkedList<UploadRequest>();
		}
		
		return uploadQueue;		
	}
	
	/**
	 * Test and process request if possible. No-op if 1) already uploading
	 * and/or 2) queue is empty.
	 */
	private void tryProcessRequest(){
		if(!isUploading && !getQueue().isEmpty()){
			UploadRequest nextRequest = getQueue().peek();
			
			isUploading = true;
			spiceManager.execute(nextRequest, nextRequest.createCacheKey(),
					DurationInMillis.ALWAYS_EXPIRED, new AppUploadRequestListener());
		}
	}
	
}
