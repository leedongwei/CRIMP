package com.nusclimb.live.crimp;

import java.util.LinkedList;
import java.util.Queue;

import com.nusclimb.live.crimp.activity.UploadListActivity;
import com.nusclimb.live.crimp.json.Score;
import com.nusclimb.live.crimp.request.UploadScoreRequest;
import com.nusclimb.live.crimp.request.UploadScoreSubmit;
import com.nusclimb.live.crimp.service.CrimpService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * Crimp application. Provides a queue for upload request.
 * 
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class CrimpApplication extends Application {
	private static final String TAG = CrimpApplication.class.getSimpleName();
	private static final int NOTIFICATION_ID = 001;
	
	private ArrayAdapter<QueueObject> queueAdapter;
	private SpiceManager spiceManager;
	private Queue<QueueObject> uploadQueue;
	private boolean isPause;					// True: Block task from entering robospice service
	private boolean isUploading;				// True: An upload task is in robospice service
	private boolean isSpiceManagerStarted;
	private int spiceManagerToken = 1;
	private int uploadSuccessCount, uploadTotalCount;
	private UploadListActivity uploadListActivity;
	private final Handler handler = new Handler();
	private NotificationCompat.Builder mBuilder;
	private Runnable checkSpiceManagerRunnable;
	private NetworkStateReceiver receiver;
	
	/*=========================================================================
	 * Inner class
	 *=======================================================================*/
	public class NetworkStateReceiver extends BroadcastReceiver {
		@Override
	    public void onReceive(final Context context, final Intent intent) {
	        Log.d(TAG, "Network connectivity change");

	        if (intent.getExtras() != null) {
	            final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	            final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
	            
	            // Connected
	            if (ni != null && ni.isConnectedOrConnecting()) {
	                Log.i(TAG, "Network " + ni.getTypeName() + " connected");
	                
	                unregisterReceiver(getNetworkStateReceiver());
	                
	                resumeUpload();
	            }
	        }
		}
	}
	
	/**
	 * RequestListener for UploadScoreSubmit. 
	 * 
	 * @author Lin Weizhi (ecc.weizhi@gmail.com)
	 *
	 */
	private class UploadScoreSubmitListener implements RequestListener<Object> {
		@Override
		public void onRequestFailure(SpiceException e) {
			Log.w(TAG, "UploadScoreSubmitListener: "+e);
			
			if(e instanceof NoNetworkException){
				// Update status
				getQueue().peek().setStatus(UploadStatus.ERROR_NO_NETWORK);
				if(queueAdapter != null){
					queueAdapter.notifyDataSetChanged();
				}
				
				// Cancel just in case.
				UploadScoreSubmit newScoreSubmit = getQueue().peek().getSubmit();
				spiceManager.cancel(Object.class, newScoreSubmit.createCacheKey());
				
				// Register receiver
				registerReceiver(getNetworkStateReceiver(), new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
			}
			else{
				isUploading = false;
				getQueue().peek().setStatus(UploadStatus.ERROR_UPLOAD);
				isPause = true;
				if(uploadListActivity != null){
					uploadListActivity.setButtonState(isPause);
				}
				if(queueAdapter != null){
					queueAdapter.notifyDataSetChanged();
				}
			}
		}

		@Override
		public void onRequestSuccess(Object result) {
			isUploading = false;
			getQueue().poll().setStatus(UploadStatus.FINISHED);
			modifyUploadSuccessCount(1);
			if(queueAdapter != null){
				queueAdapter.notifyDataSetChanged();
	 		}
	    	 
			tryProcessRequest();
		}
	}
	
	/**
	 * RequestListener for UploadScoreRequest. Set climber score in 
	 * UploadScoreSubmit when succeed.
	 * 
	 * @author Lin Weizhi (ecc.weizhi@gmail.com)
	 *
	 */
	private class UploadScoreRequestListener implements RequestListener<Score> {
		@Override
		public void onRequestFailure(SpiceException e) {
			Log.w(TAG, "UploadScoreRequestListener: "+e);
			
			if(e instanceof NoNetworkException){
				// Update status
				getQueue().peek().setStatus(UploadStatus.ERROR_NO_NETWORK);
				if(queueAdapter != null){
					queueAdapter.notifyDataSetChanged();
				}
				
				// Cancel just in case.
				UploadScoreRequest oldScoreRequest = getQueue().peek().getRequest();
				spiceManager.cancel(Score.class, oldScoreRequest.createCacheKey());
				
				// Register receiver
				registerReceiver(getNetworkStateReceiver(), new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
			}
			else{
				getQueue().peek().setStatus(UploadStatus.ERROR_DOWNLOAD);
				isPause = true;
				if(uploadListActivity != null){
					uploadListActivity.setButtonState(isPause);
				}
				if(queueAdapter != null){
					queueAdapter.notifyDataSetChanged();
				}
			}
		}

		@Override
		public void onRequestSuccess(Score result) {
			// Update status
			getQueue().peek().setStatus(UploadStatus.UPLOAD);
			if(queueAdapter != null){
				queueAdapter.notifyDataSetChanged();
			}
			
			// Update score
			getQueue().peek().getSubmit().updateScoreWithOld(result.getC_score());
			
			// Execute upload new score.
			UploadScoreSubmit newScoreSubmit = getQueue().peek().getSubmit();
			
			spiceManager.execute(newScoreSubmit, newScoreSubmit.createCacheKey(),
					DurationInMillis.ALWAYS_EXPIRED, new UploadScoreSubmitListener());
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
	 * Add a QueueObject to queue.
	 * 
	 * @param request Request to be added.
	 */
	public void addRequest(QueueObject request){
		// Start the spiceManager if it is not started.
		if(!isSpiceManagerStarted && spiceManagerToken > 0){
			Log.d(TAG, "spiceManager starting...");
			spiceManagerToken--;
			spiceManager.start(this);

			handler.postDelayed(getCheckSpiceManagerRunnable(), 100);
		}
		
		// Add QueueObject to queue. 
		getQueue().offer(request);
		modifyUploadTotalCount(1);
		if(queueAdapter != null){
			queueAdapter.notifyDataSetChanged();
		}
		
		tryProcessRequest();
	}
	
	/**
	 * Get upload request queue. Create one if it does not exist.
	 * 
	 * @return Upload request queue.
	 */
	public Queue<QueueObject> getQueue(){
		if(uploadQueue == null){
			uploadQueue = new LinkedList<QueueObject>();
		}
		
		return uploadQueue;		
	}
	
	public void setQueueAdapter(ArrayAdapter<QueueObject> adapter){
		queueAdapter = adapter;
	}
	
	public void setUploadListActivity(UploadListActivity activity){
		uploadListActivity = activity;
	}
	
	public boolean getIsPause(){
		return isPause;
	}
	
	public void resumeUpload(){
		Log.d(TAG, "Attempt to resume uploads.");
		this.isPause = false;
		isUploading = false;
		
		if(uploadListActivity != null){
			uploadListActivity.setButtonState(isPause);
		}
		
		tryProcessRequest();
	}
	
	public void modifyUploadTotalCount(int i){
		uploadTotalCount += i;
		
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
	}
	
	public void modifyUploadSuccessCount(int i){
		uploadSuccessCount += i;
		
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
			// Creates an Intent for the Activity
			Intent intent = new Intent(this, UploadListActivity.class);
			
			// Sets the Activity to start in a new, empty task
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			// Creates the PendingIntent
			PendingIntent notifyIntent =
			        PendingIntent.getActivity(
			        this,
			        0,
			        intent,
			        PendingIntent.FLAG_UPDATE_CURRENT
			);

			mBuilder = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.ic_launcher)
			        .setContentTitle("CRIMP Score upload")
			        .setContentText("CRIMP service is running!")
			        .setContentIntent(notifyIntent);
		}
		
		return mBuilder;
	}
	
	/**
	 * Test and process request if possible. No-op if 1) already uploading
	 * and/or 2) queue is empty and/or 3) spiceManager not started.
	 */
	private void tryProcessRequest(){
		if(!isUploading && !getQueue().isEmpty() && isSpiceManagerStarted && !isPause){
			UploadScoreRequest oldScoreRequest = getQueue().peek().getRequest();
			
			// Update status
			getQueue().peek().setStatus(UploadStatus.DOWNLOAD);
			if(queueAdapter != null){
				queueAdapter.notifyDataSetChanged();
			}
			
			// Execute download old score.
			isUploading = true;
			spiceManager.execute(oldScoreRequest, oldScoreRequest.createCacheKey(),
					DurationInMillis.ALWAYS_EXPIRED, new UploadScoreRequestListener());
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
	
	/**
	 * Get NetworkStateReceiver. Create one if it does not exist.
	 * 
	 * @return A NetworkStateReceiver.
	 */
	private NetworkStateReceiver getNetworkStateReceiver(){
		if(receiver == null){
			receiver = new NetworkStateReceiver();
		}
		
		return receiver;
	}
	
	/*
	private void appendLog(String text){       
	    //to create a Text file name "log.txt" in SDCard  
	    File sdCard = Environment.getExternalStorageDirectory();  
	    File dir = new File (sdCard.getAbsolutePath() + "/CRIMP");  
	    dir.mkdirs();  
	    File logFile = new File(dir, "log.txt");  
		
		if (!logFile.exists()){
			try {
				logFile.createNewFile();
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			//BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
			buf.append(text);
			buf.newLine();
			buf.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
}
