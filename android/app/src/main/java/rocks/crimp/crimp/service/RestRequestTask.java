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
public class RestRequestTask implements Task<RestRequestTask.Callback> {
    private static final long serialVersionUID = 1L;

    private final String action;
    private final UUID txId;
    private final RequestBean requestBean;

    public RestRequestTask(Intent intent){
        action = intent.getAction();
        txId = (UUID) intent.getSerializableExtra(CrimpService.SERIALIZABLE_UUID);
        requestBean = (RequestBean)intent.getSerializableExtra(CrimpService.SERIALIZABLE_REQUEST);
    }

    @Override
    public void execute(Callback callback) {
        Serializable responseObject = null;
        try {
            // TODO ADD NEW METHODS HERE
            // We need to hit server. Perform a blocking call to webservice.
            switch(action){
                case CrimpService.ACTION_GET_CATEGORIES:
                    responseObject = CrimpApplication.getCrimpWS().getCategories();
                    break;
                case CrimpService.ACTION_GET_SCORE:
                    responseObject = CrimpApplication.getCrimpWS().getScore(requestBean);
                    break;
                case CrimpService.ACTION_SET_ACTIVE:
                    responseObject = CrimpApplication.getCrimpWS().setActive(requestBean);
                    break;
                case CrimpService.ACTION_CLEAR_ACTIVE:
                    responseObject = CrimpApplication.getCrimpWS().clearActive(requestBean);
                    break;
                case CrimpService.ACTION_LOGIN:
                    responseObject = CrimpApplication.getCrimpWS().login(requestBean);
                    break;
                case CrimpService.ACTION_REPORT_IN:
                    responseObject = CrimpApplication.getCrimpWS().reportIn(requestBean);
                    break;
                case CrimpService.ACTION_REQUEST_HELP:
                    responseObject = CrimpApplication.getCrimpWS().requestHelp(requestBean);
                    break;
                case CrimpService.ACTION_LOGOUT:
                    responseObject = CrimpApplication.getCrimpWS().logout(requestBean);
                    break;
                default:
                    Timber.d("Unknown action: %s", action);
            }
        } catch (IOException e) {
            Timber.d(e, "IOException trying to hit server");
        }

        if(responseObject != null){
            // Great! We received stuff from server. Storing it in our local model
            CrimpApplication.getLocalModel().putData(txId.toString(), responseObject);
            callback.onRestSuccess(txId, responseObject);
        }
        else{
            Timber.e("Failed to receive response from server");
            callback.onRestFailure(txId);
        }
    }

    public interface Callback {
        void onRestSuccess(UUID txId, Object response);
        void onRestFailure(UUID txId);
    }
}

