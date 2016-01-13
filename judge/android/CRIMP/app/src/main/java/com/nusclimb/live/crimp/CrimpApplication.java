package com.nusclimb.live.crimp;

import java.util.LinkedList;
import java.util.Queue;

import com.nusclimb.live.crimp.common.json.PostScoreResponseBody;
import com.nusclimb.live.crimp.common.spicerequest.PostScoreRequest;
import com.nusclimb.live.crimp.uploadlist.UploadListActivity;
import com.nusclimb.live.crimp.common.QueueObject;
import com.nusclimb.live.crimp.service.CrimpService;
import com.nusclimb.live.crimp.uploadlist.UploadStatus;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.exception.NoNetworkException;
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
    private int spiceManagerToken = 1;			// Used to limit the number of time we start spiceManager.
    private int uploadSuccessCount, uploadTotalCount;
    private UploadListActivity uploadListActivity;
    private final Handler handler = new Handler();
    private NotificationCompat.Builder mBuilder;
    private Runnable checkSpiceManagerRunnable;
    private NetworkStateReceiver receiver;
	
	/*=========================================================================
	 * Inner class
	 *=======================================================================*/
    /**
     * Subclass of BroadcastReceiver to listen for network state change.
     * Resumes uploading when network is available.
     *
     * @author Lin Weizhi (ecc.weizhi@gmail.com)
     *
     */
    private class NetworkStateReceiver extends BroadcastReceiver {
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
    private class PostScoreRequestListener implements RequestListener<PostScoreResponseBody> {
        @Override
        public void onRequestFailure(SpiceException e) {
            if(e instanceof NoNetworkException){
                // Update status
                getQueue().peek().setStatus(UploadStatus.ERROR_NO_NETWORK);
                if(queueAdapter != null){
                    queueAdapter.notifyDataSetChanged();
                }

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
        public void onRequestSuccess(PostScoreResponseBody result) {
            isUploading = false;
            getQueue().poll().setStatus(UploadStatus.FINISHED);
            modifyUploadSuccessCount(1);
            if(queueAdapter != null){
                queueAdapter.notifyDataSetChanged();
            }

            // Chain up next request.
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

        Log.i(TAG, "Request added.\n[Pending uploads:"+
                uploadQueue.size()+"] [isUploading:"+isUploading+"] [isPause:"+isPause+"]");

        tryProcessRequest();
    }

    /**
     * Get upload request queue. Create one if it does not exist.
     *
     * @return Upload request queue.
     */
    public Queue<QueueObject> getQueue(){
        if(uploadQueue == null){
            uploadQueue = new LinkedList<>();
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
        Log.d(TAG, "Resume");
        this.isPause = false;
        isUploading = false;

        if(uploadListActivity != null){
            uploadListActivity.setButtonState(isPause);
        }

        //TODO
        tryProcessRequest();
    }

    public void modifyUploadTotalCount(int i){
        uploadTotalCount += i;

        if(uploadSuccessCount == uploadTotalCount){
            // Update notification
            getNotificationBuilder().setContentText("Upload complete: "+uploadSuccessCount+"/"+uploadTotalCount)
                    .setProgress(0, 0, false)
                    .setOngoing(false);

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
                    .setProgress(uploadTotalCount, uploadSuccessCount, false)
                    .setOngoing(true);
        }

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(NOTIFICATION_ID, getNotificationBuilder().build());
    }

    private void modifyUploadSuccessCount(int i){
        uploadSuccessCount += i;

        if(uploadSuccessCount == uploadTotalCount){
            // Update notification
            getNotificationBuilder().setContentText("Upload complete: "+uploadSuccessCount+"/"+uploadTotalCount)
                    .setProgress(0, 0, false)
                    .setOngoing(false);

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
                    .setProgress(uploadTotalCount, uploadSuccessCount, false)
                    .setOngoing(true);
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
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("CRIMP Score upload")
                    .setContentText("CRIMP service is running!")
                    .setContentIntent(notifyIntent)
                    .setOngoing(true);
        }

        return mBuilder;
    }

    /**
     * Test and process request if possible. No-op if 1) already uploading
     * and/or 2) queue is empty and/or 3) spiceManager not started.
     */
    private void tryProcessRequest(){
        if(!isUploading && !getQueue().isEmpty() && isSpiceManagerStarted && !isPause){
            // Update status
            getQueue().peek().setStatus(UploadStatus.UPLOAD);
            if(queueAdapter != null){
                queueAdapter.notifyDataSetChanged();
            }

            // Execute upload new score.
            isUploading = true;
            PostScoreRequest request = getQueue().peek().getRequest();
            spiceManager.execute(request, new PostScoreRequestListener());
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
}
