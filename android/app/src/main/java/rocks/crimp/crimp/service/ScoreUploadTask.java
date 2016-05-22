package rocks.crimp.crimp.service;

import android.content.Intent;

import com.squareup.tape.Task;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.network.model.RequestBean;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ScoreUploadTask implements Task<ScoreUploadTask.Callback> {
    private static final long serialVersionUID = 1L;

    private final String action;
    private final UUID txId;
    private final RequestBean requestBean;

    public ScoreUploadTask(Intent intent){
        action = intent.getAction();
        if(!action.equals(CrimpService.ACTION_POST_SCORE)){
            throw new IllegalArgumentException("ScoreUploadTask must have post score action");
        }

        txId = (UUID) intent.getSerializableExtra(CrimpService.SERIALIZABLE_UUID);
        requestBean = (RequestBean)intent.getSerializableExtra(CrimpService.SERIALIZABLE_REQUEST);
    }

    @Override
    public void execute(Callback callback) {
        Serializable responseObject = null;
        try {
            responseObject = CrimpApplication.getCrimpWS().postScore(requestBean);
        } catch (IOException e) {
            Timber.e(e, "IOException trying to hit server");
        }

        if(responseObject != null){
            // Great! We received stuff from server. Storing it in our local model
            CrimpApplication.getLocalModel().putData(txId.toString(), responseObject);
            callback.onScoreUploadSuccess(txId, responseObject);
        }
        else{
            Timber.d("Failed to receive response from server");
            callback.onScoreUploadFailure(txId);
        }
    }

    public RequestBean getRequestBean(){
        return requestBean;
    }

    public interface Callback {
        void onScoreUploadSuccess(UUID txId, Object response);
        void onScoreUploadFailure(UUID txId);
    }
}

