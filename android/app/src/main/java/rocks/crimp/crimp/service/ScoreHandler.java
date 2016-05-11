package rocks.crimp.crimp.service;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.common.event.RequestFailed;
import rocks.crimp.crimp.common.event.RequestSucceed;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ScoreHandler extends Handler implements ScoreUploadTask.Callback{
    public static final int DO_WORK = 1;

    private ScoreUploadTaskQueue mScoreUploadTaskQueue;
    private boolean isExecutingTask;
    private Context mContext;

    public ScoreHandler(Looper looper, Context context){
        super(looper);
        mContext = context;
        mScoreUploadTaskQueue = ScoreUploadTaskQueue.create(context);
    }

    @Override
    public void handleMessage(Message msg){
        switch(msg.what){
            case DO_WORK:
                Intent intent = (Intent) msg.obj;
                if(intent == null){
                    throw new NullPointerException("ScoreHandler message should include obj field");
                }
                mScoreUploadTaskQueue.add(new ScoreUploadTask(intent));
                executeTask();
                break;
        }
    }

    private void executeTask(){
        if(isExecutingTask){
            return;
        }

        int queueSize = mScoreUploadTaskQueue.size();
        Timber.d("executeTask(). queueSize: %d", queueSize);
        if(queueSize != 0){
            ScoreUploadTask task = mScoreUploadTaskQueue.peek();
            if (task != null) {
                isExecutingTask = true;
                task.execute(this);
            }
            else{
                throw new NullPointerException("We can't deserialize task");
            }
        }
        else{
            try {
                CrimpApplication.getServiceQuitBarrier().await();
            } catch (InterruptedException e) {
                // Something woke us up. This is normal behavior when someone send a message to
                // this handler. We should be expected to continue.
                Timber.d(e, "We are interrupted. Continuing...");
            } catch (BrokenBarrierException e) {
                // The other thread broke off from await. This is normal behavior.
                Timber.d(e, "Someone broke out of barrier. Continuing...");
            }
        }
    }

    @Override
    public void onScoreUploadSuccess(UUID txId, Object response) {
        isExecutingTask = false;
        mScoreUploadTaskQueue.remove();
        CrimpApplication.getBusInstance().post(new RequestSucceed(txId, response));

        Message msg = this.obtainMessage(DO_WORK);
        this.sendMessage(msg);
    }

    @Override
    public void onScoreUploadFailure(UUID txId) {
        isExecutingTask = false;
        mScoreUploadTaskQueue.remove();
        CrimpApplication.getBusInstance().post(new RequestFailed(txId));

        Message msg = this.obtainMessage(DO_WORK);
        this.sendMessage(msg);
    }
}
