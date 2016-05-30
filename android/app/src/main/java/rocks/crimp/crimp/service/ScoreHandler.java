package rocks.crimp.crimp.service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.squareup.otto.Produce;

import java.util.UUID;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.common.event.CurrentUploadTask;
import rocks.crimp.crimp.common.event.RequestFailed;
import rocks.crimp.crimp.common.event.RequestSucceed;
import rocks.crimp.crimp.network.model.RequestBean;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ScoreHandler extends Handler implements ScoreUploadTask.Callback{
    public static final int AWAIT = 1;
    public static final int DO_WORK = 2;
    public static final int NEW_UPLOAD = 3;
    public static final int RESUME_UPLOAD = 4;

    public static final int TOTAL_ATTEMPTS = 3;
    public static final int BASE_BACKOFF = 2000;
    public int currentBackoff = BASE_BACKOFF;


    private int attemptsLeft = TOTAL_ATTEMPTS;
    private volatile ScoreUploadTaskQueue mScoreUploadTaskQueue;
    private boolean isExecutingTask;
    private Context mContext;
    private CurrentUploadTask mCurrentTaskEvent;

    public ScoreHandler(Looper looper, Context context){
        super(looper);
        mContext = context;
        mScoreUploadTaskQueue = ScoreUploadTaskQueue.create(context);
        CrimpApplication.getBusInstance().register(this);
    }

    @Produce
    public CurrentUploadTask produceCurrentUploadTask(){
        if(mCurrentTaskEvent == null){
            int count = mScoreUploadTaskQueue.size();
            RequestBean task = null;
            if(count>0){
                task = mScoreUploadTaskQueue.peek().getRequestBean();
            }
            mCurrentTaskEvent = new CurrentUploadTask(mScoreUploadTaskQueue.size(), task,
                    CurrentUploadTask.IDLE);
        }

        return mCurrentTaskEvent;
    }

    @Override
    public void handleMessage(Message msg){
        switch(msg.what){
            case DO_WORK:
                Timber.d("DO_WORK message");
                executeTask();
                break;

            case NEW_UPLOAD:
                Timber.d("NEW_TASK message");
                Intent intent = (Intent) msg.obj;
                if(intent == null){
                    throw new NullPointerException("ScoreHandler message should include obj field");
                }
                mScoreUploadTaskQueue.add(new ScoreUploadTask(intent));
                Message newMsg = this.obtainMessage(DO_WORK);
                this.sendMessage(newMsg);
                break;

            case RESUME_UPLOAD:
                Timber.d("RESUME_UPLOAD message");

                // some assertion
                if(attemptsLeft != 0){
                    throw new IllegalStateException("attemptsLeft != 0");
                }
                if(isExecutingTask){
                    throw new IllegalStateException("isExecutingTask is true");
                }

                attemptsLeft = TOTAL_ATTEMPTS;
                currentBackoff = BASE_BACKOFF;
                executeTask();
                break;

            default:
                Timber.d("unknown message");
        }
    }

    private void executeTask(){
        if(isExecutingTask || attemptsLeft<=0){
            return;
        }

        int queueSize = mScoreUploadTaskQueue.size();
        Timber.d("executeTask(). queueSize: %d", queueSize);
        if(queueSize != 0){
            ScoreUploadTask task = mScoreUploadTaskQueue.peek();

            mCurrentTaskEvent = new CurrentUploadTask(queueSize, task.getRequestBean(),
                    CurrentUploadTask.UPLOADING);
            CrimpApplication.getBusInstance().post(mCurrentTaskEvent);

            if (task != null) {
                isExecutingTask = true;

                // Determine network connection. Execute task only if there is network.
                // Set a BroadcastReceiver to listen for network state changes if we are not connected.
                ConnectivityManager cm =
                        (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();

                if(isConnected){
                    task.execute(this);
                }
                else{
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                    mContext.registerReceiver(CrimpApplication.getNetworkChangeReceiver(), filter);
                    isExecutingTask = false;
                    onScoreUploadFailureNetwork(task.getTxId());
                }
            }
            else{
                throw new NullPointerException("We can't deserialize task");
            }
        }
        else{
            mCurrentTaskEvent = new CurrentUploadTask(queueSize, null, CurrentUploadTask.IDLE);
            CrimpApplication.getBusInstance().post(mCurrentTaskEvent);
            tryAndQuitService();
        }
    }

    public int getThreadTaskCount(){
        return mScoreUploadTaskQueue.size();
    }


    private void tryAndQuitService(){
        int totalTaskCount = CrimpApplication.getRestHandler().getThreadTaskCount() +
                mScoreUploadTaskQueue.size();
        if(totalTaskCount == 0) {
            Timber.d("Tried to stop service");
            Intent stopServiceIntent = new Intent(mContext, CrimpService.class);
            mContext.stopService(stopServiceIntent);
        }
    }


    @Override
    public void onScoreUploadSuccess(UUID txId, Object response) {
        attemptsLeft = TOTAL_ATTEMPTS;
        currentBackoff = BASE_BACKOFF;
        isExecutingTask = false;
        mScoreUploadTaskQueue.remove();
        CrimpApplication.getBusInstance().post(new RequestSucceed(txId, response));

        Message msg = this.obtainMessage(DO_WORK);
        this.sendMessage(msg);
    }

    @Override
    public void onScoreUploadFailureException(UUID txId, Exception e) {
        attemptsLeft--;
        Timber.d("Score upload fail. Attempts left: %d", attemptsLeft);
        isExecutingTask = false;
        CrimpApplication.getBusInstance().post(new RequestFailed(txId));

        if(attemptsLeft > 0) {
            try {
                Timber.d("Backing off for %dms", currentBackoff);
                Thread.sleep(currentBackoff);
            } catch (InterruptedException e1) {
                // No-op
            }
            currentBackoff = currentBackoff * 2;

            Message msg = this.obtainMessage(DO_WORK);
            this.sendMessage(msg);
        }
        else{
            mCurrentTaskEvent = new CurrentUploadTask(mScoreUploadTaskQueue.size(),
                    mScoreUploadTaskQueue.peek().getRequestBean(), e);
            CrimpApplication.getBusInstance().post(mCurrentTaskEvent);
        }
    }

    @Override
    public void onScoreUploadFailureHttp(UUID txId, int statusCode, String message) {
        attemptsLeft--;
        Timber.d("Score upload fail. Attempts left: %d", attemptsLeft);
        isExecutingTask = false;
        CrimpApplication.getBusInstance().post(new RequestFailed(txId));

        if(attemptsLeft > 0) {
            try {
                Timber.d("Backing off for %dms", currentBackoff);
                Thread.sleep(currentBackoff);
            } catch (InterruptedException e1) {
                // No-op
            }
            currentBackoff = currentBackoff * 2;

            Message msg = this.obtainMessage(DO_WORK);
            this.sendMessage(msg);
        }
        else{
            mCurrentTaskEvent = new CurrentUploadTask(mScoreUploadTaskQueue.size(),
                    mScoreUploadTaskQueue.peek().getRequestBean(), statusCode, message);
            CrimpApplication.getBusInstance().post(mCurrentTaskEvent);
        }
    }

    @Override
    public void onScoreUploadFailureNetwork(UUID txId) {
        attemptsLeft = 0;
        Timber.d("Score upload fail due to no network. Ignore attempts left");
        isExecutingTask = false;
        CrimpApplication.getBusInstance().post(new RequestFailed(txId));

        if(attemptsLeft > 0) {
            try {
                Timber.d("Backing off for %dms", currentBackoff);
                Thread.sleep(currentBackoff);
            } catch (InterruptedException e1) {
                // No-op
            }
            currentBackoff = currentBackoff * 2;

            Message msg = this.obtainMessage(DO_WORK);
            this.sendMessage(msg);
        }
        else{
            mCurrentTaskEvent = new CurrentUploadTask(mScoreUploadTaskQueue.size(),
                    mScoreUploadTaskQueue.peek().getRequestBean(), CurrentUploadTask.ERROR_NO_NETWORK);
            CrimpApplication.getBusInstance().post(mCurrentTaskEvent);
        }
    }
}
