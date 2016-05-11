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
public class RestHandler extends Handler implements RestRequestTask.Callback{
    public static final int DO_WORK = 1;
    public static final int FETCH_LOCAL = 2;

    private RestRequestTaskQueue mRestRequestTaskQueue;
    private boolean isExecutingTask;
    private Context mContext;

    public RestHandler(Looper looper, Context context){
        super(looper);
        mContext = context;
        mRestRequestTaskQueue = RestRequestTaskQueue.create();
    }

    @Override
    public void handleMessage(Message msg){
        switch(msg.what){
            case DO_WORK:
                executeTask();
                break;

            case FETCH_LOCAL:
                Intent intent = (Intent) msg.obj;
                UUID txId = (UUID) intent.getSerializableExtra(CrimpService.SERIALIZABLE_UUID);
                Object value = CrimpApplication.getLocalModel().fetch(txId.toString(), Object.class);
                if(value != null){
                    // We have data in our local model.
                    CrimpApplication.getBusInstance().post(new RequestSucceed(txId, value));
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
                else{
                    // We do not have data in our local model.
                    mRestRequestTaskQueue.add(new RestRequestTask(intent));
                    executeTask();
                }
                break;
        }
    }

    private void executeTask(){
        if(isExecutingTask){
            return;
        }

        int queueSize = mRestRequestTaskQueue.size();
        Timber.d("executeTask(). queueSize: %d", queueSize);
        if(queueSize != 0){
            RestRequestTask task = mRestRequestTaskQueue.peek();
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
    public void onRestSuccess(UUID txId, Object response) {
        isExecutingTask = false;
        mRestRequestTaskQueue.remove();
        CrimpApplication.getBusInstance().post(new RequestSucceed(txId, response));

        Message msg = this.obtainMessage(DO_WORK);
        this.sendMessage(msg);
    }

    @Override
    public void onRestFailure(UUID txId) {
        isExecutingTask = false;
        mRestRequestTaskQueue.remove();
        CrimpApplication.getBusInstance().post(new RequestFailed(txId));

        Message msg = this.obtainMessage(DO_WORK);
        this.sendMessage(msg);
    }
}
