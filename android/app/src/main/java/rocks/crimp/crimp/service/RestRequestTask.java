package rocks.crimp.crimp.service;

import android.content.Intent;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.tape.Task;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import retrofit2.Response;
import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.network.model.ErrorJs;
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
        String errorBody = null;

        /**
         * IMPORTANT: YOU CAN ONLY CALL response.errorBody().string() ONCE.
         * SUBSEQUENT CALL WILL JUST RETURN AN EMPTY STRING.
         **/
        try {
            // TODO ADD NEW METHODS HERE
            // We need to hit server. Perform a blocking call to webservice.
            switch(action){
                case CrimpService.ACTION_GET_CATEGORIES:
                    response = CrimpApplication.getCrimpWS().getCategories();
                    if(!response.isSuccessful()){
                        errorBody = response.errorBody().string();
                        Timber.e("Get categories error response: %s", errorBody);
                    }
                    break;
                case CrimpService.ACTION_GET_SCORE:
                    response = CrimpApplication.getCrimpWS().getScore(requestBean);
                    if(!response.isSuccessful()){
                        errorBody = response.errorBody().string();
                        Timber.e("Get score error response: %s", errorBody);
                    }
                    break;
                case CrimpService.ACTION_SET_ACTIVE:
                    response = CrimpApplication.getCrimpWS().setActive(requestBean);
                    if(!response.isSuccessful()){
                        errorBody = response.errorBody().string();
                        Timber.e("Set active error response: %s", errorBody);
                    }
                    break;
                case CrimpService.ACTION_CLEAR_ACTIVE:
                    response = CrimpApplication.getCrimpWS().clearActive(requestBean);
                    if(!response.isSuccessful()){
                        errorBody = response.errorBody().string();
                        Timber.e("Clear active error response: %s", errorBody);
                    }
                    break;
                case CrimpService.ACTION_LOGIN:
                    response = CrimpApplication.getCrimpWS().login(requestBean);
                    if(!response.isSuccessful()){
                        errorBody = response.errorBody().string();
                        Timber.e("Login error response: %s", errorBody);
                    }
                    break;
                case CrimpService.ACTION_REPORT_IN:
                    response = CrimpApplication.getCrimpWS().reportIn(requestBean);
                    if(!response.isSuccessful()){
                        errorBody = response.errorBody().string();
                        Timber.e("Report error response: %s", errorBody);
                    }
                    break;
                case CrimpService.ACTION_REQUEST_HELP:
                    response = CrimpApplication.getCrimpWS().requestHelp(requestBean);
                    if(!response.isSuccessful()){
                        errorBody = response.errorBody().string();
                        Timber.e("Help me error response: %s", errorBody);
                    }
                    break;
                case CrimpService.ACTION_LOGOUT:
                    response = CrimpApplication.getCrimpWS().logout(requestBean);
                    if(!response.isSuccessful()){
                        errorBody = response.errorBody().string();
                        Timber.e("Logout error response: %s", errorBody);
                    }
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
            ObjectMapper objectMapper = new ObjectMapper();
            ErrorJs errorJs = null;
            try {
                errorJs = objectMapper.readValue(errorBody, ErrorJs.class);
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            callback.onRestFailure(txId, errorJs);
        }
    }

    public interface Callback {
        void onRestSuccess(UUID txId, Object response);
        void onRestFailure(UUID txId, ErrorJs errorJs);
    }
}

