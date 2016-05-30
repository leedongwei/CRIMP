package rocks.crimp.crimp.service;

import android.content.Intent;

import com.squareup.tape.Task;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import retrofit2.Response;
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

    public UUID getTxId(){
        return txId;
    }

    @Override
    public void execute(Callback callback) {
        Response response = null;
        try {
            // TODO ADD NEW METHODS HERE
            // We need to hit server. Perform a blocking call to webservice.
            switch(action){
                case CrimpService.ACTION_GET_CATEGORIES:
                    response = CrimpApplication.getCrimpWS().getCategories();
                    break;
                case CrimpService.ACTION_GET_SCORE:
                    response = CrimpApplication.getCrimpWS().getScore(requestBean);
                    break;
                case CrimpService.ACTION_SET_ACTIVE:
                    response = CrimpApplication.getCrimpWS().setActive(requestBean);
                    break;
                case CrimpService.ACTION_CLEAR_ACTIVE:
                    response = CrimpApplication.getCrimpWS().clearActive(requestBean);
                    break;
                case CrimpService.ACTION_LOGIN:
                    response = CrimpApplication.getCrimpWS().login(requestBean);
                    break;
                case CrimpService.ACTION_REPORT_IN:
                    response = CrimpApplication.getCrimpWS().reportIn(requestBean);
                    break;
                case CrimpService.ACTION_REQUEST_HELP:
                    response = CrimpApplication.getCrimpWS().requestHelp(requestBean);
                    break;
                case CrimpService.ACTION_LOGOUT:
                    response = CrimpApplication.getCrimpWS().logout(requestBean);
                    break;
                default:
                    Timber.d("Unknown action: %s", action);
            }
        } catch (IOException e) {
            Timber.d(e, "IOException trying to hit server");
        }

        if(response != null && response.body() != null && response.body() instanceof Serializable){
            // Great! We received stuff from server. Storing it in our local model
            CrimpApplication.getLocalModel().putData(txId.toString(), (Serializable) response.body());
            callback.onRestSuccess(txId, response.body());
        }
        else{
            Timber.e("Failed to receive correct response from server");
            callback.onRestFailure(txId);
        }
    }

    public interface Callback {
        void onRestSuccess(UUID txId, Object response);
        void onRestFailure(UUID txId);
    }
}

