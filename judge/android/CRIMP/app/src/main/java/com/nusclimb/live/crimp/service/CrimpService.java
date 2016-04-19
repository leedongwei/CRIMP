package com.nusclimb.live.crimp.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nusclimb.live.crimp.CrimpApplication2;
import com.nusclimb.live.crimp.common.event.RequestFailed;
import com.nusclimb.live.crimp.common.event.ResponseReceived;
import com.nusclimb.live.crimp.network.model.CategoriesJs;
import com.nusclimb.live.crimp.network.model.GetScoreJs;
import com.nusclimb.live.crimp.network.model.LoginJs;
import com.nusclimb.live.crimp.network.model.RequestBean;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CrimpService extends IntentService{
    private static final String TAG = "CrimpService";
    private static final boolean DEBUG = true;

    public static final String ACTION_GET_CATEGORIES = "action_get_categories";
    public static final String ACTION_GET_SCORE = "action_get_score";
    public static final String ACTION_SET_ACTIVE = "action_set_active";
    public static final String ACTION_CLEAR_ACTIVE = "action_clear_active";
    public static final String ACTION_LOGIN = "action_login";
    public static final String ACTION_REPORT_IN = "action_report_in";
    public static final String ACTION_REQUEST_HELP = "action_request_help";
    public static final String ACTION_POST_SCORE = "action_post_score";
    public static final String ACTION_LOGOUT = "action_logout";

    public static final String SERIALIZABLE_UUID = "serializable_uuid";
    public static final String SERIALIZABLE_REQUEST = "serializable_request";
    public static final String LONG_CLIMBER_ID = "long_climber_id";
    public static final String LONG_CATEGORY_ID = "long_category_id";
    public static final String LONG_ROUTE_ID = "long_route_id";
    public static final String LONG_FB_USER_ID = "long_fb_user_id";
    public static final String LONG_SEQUENTIAL_TOKEN = "long_sequential_token";
    public static final String STRING_MARKER_ID = "string_marker_id";
    public static final String STRING_FB_ACCESS_TOKEN = "string_fb_access_token";
    public static final String STRING_SCORE = "string_score";
    public static final String BOOL_FORCE_LOGIN = "bool_force_login";
    public static final String BOOL_FORCE = "bool_force";

    private static final int MIN_BACKOFF = 0;    //TODO SET BACK TO 2000
    private static final int RETRY = 3;
    private int backOff;


    public CrimpService() {
        super("CrimpService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        UUID txId = (UUID)intent.getSerializableExtra(SERIALIZABLE_UUID);
        RequestBean bean = (RequestBean)intent.getSerializableExtra(SERIALIZABLE_REQUEST);
        backOff = MIN_BACKOFF;

        if(DEBUG) Log.d(TAG, "Received intent action:"+intent.getAction()+" txId:"+txId);

        // Received an intent. Check what is the work to be done.
        switch(intent.getAction()){
            case ACTION_GET_CATEGORIES:
                CategoriesJs categoriesJs = getCategories();

                if(categoriesJs != null){
                    //TODO UPDATE TRANSACTION LOG

                    // write to local model
                    CrimpApplication2.getLocalModel().putData(txId.toString(), categoriesJs);
                    if(DEBUG) Log.d(TAG, "Posting responseReceived: "+txId);
                    CrimpApplication2.getBusInstance().post(new ResponseReceived(txId));
                }
                else{
                    //TODO UPDATE TRANSACTION LOG

                    if(DEBUG) Log.d(TAG, "Posting requestFailed: "+txId);
                    CrimpApplication2.getBusInstance().post(new RequestFailed(txId));
                }
                break;
            case ACTION_GET_SCORE:
                GetScoreJs getScoreJs = getScore(bean);

                if(getScoreJs != null){
                    //TODO UPDATE TRANSACTION LOG

                    // write to local model
                    CrimpApplication2.getLocalModel().putData(txId.toString(), getScoreJs);
                    if(DEBUG) Log.d(TAG, "Posting responseReceived: "+txId);
                    CrimpApplication2.getBusInstance().post(new ResponseReceived(txId));
                }
                else{
                    //TODO UPDATE TRANSACTION LOG

                    if(DEBUG) Log.d(TAG, "Posting requestFailed: "+txId);
                    CrimpApplication2.getBusInstance().post(new RequestFailed(txId));
                }
                break;
            case ACTION_SET_ACTIVE:
                break;
            case ACTION_CLEAR_ACTIVE:
                break;
            case ACTION_LOGIN:
                LoginJs loginJs = login(bean);
                loginJs = new LoginJs();    //TODO INJECTION

                if(loginJs != null){
                    //TODO UPDATE TRANSACTION LOG

                    // write to local model
                    CrimpApplication2.getLocalModel().putData(txId.toString(), loginJs);
                    if(DEBUG) Log.d(TAG, "Posting responseReceived: "+txId);
                    CrimpApplication2.getBusInstance().post(new ResponseReceived(txId));
                }
                else{
                    //TODO UPDATE TRANSACTION LOG

                    if(DEBUG) Log.d(TAG, "Posting requestFailed: "+txId);
                    CrimpApplication2.getBusInstance().post(new RequestFailed(txId));
                }
                break;
            case ACTION_REPORT_IN:
                break;
            case ACTION_REQUEST_HELP:
                break;
            case ACTION_POST_SCORE:
                break;
            case ACTION_LOGOUT:
                break;
            default:
        }

    }

    @Nullable
    private CategoriesJs getCategories(){
        CategoriesJs categoriesJs = null;
        try {
            categoriesJs = CrimpApplication2.getCrimpWS().getCategories();
        } catch (IOException e){
            if(DEBUG) Log.d(TAG, "IOE while doing getCategories. e:"+e.getMessage());
        }

        for(int i=1; categoriesJs==null && i<=RETRY; i++){
            if(DEBUG) Log.d(TAG, "getCategories returns null. Retry("+(i)+") in "+backOff+"ms...");

            try {
                Thread.sleep(backOff);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }

            try {
                categoriesJs = CrimpApplication2.getCrimpWS().getCategories();
            } catch (IOException e){
                if(DEBUG) Log.d(TAG, "IOE while doing getCategories. e:"+e.getMessage());
            }

            backOff = backOff * 2;
        }

        return categoriesJs;
    }

    @Nullable
    private GetScoreJs getScore(RequestBean requestBean){
        GetScoreJs getScoreJs = null;
        try {
            getScoreJs = CrimpApplication2.getCrimpWS().getScore(requestBean);
        } catch (IOException e){
            if(DEBUG) Log.d(TAG, "IOE while doing getScore. e:"+e.getMessage());
        }

        for(int i=1; getScoreJs==null && i<=RETRY; i++){
            if(DEBUG) Log.d(TAG, "getScore returns null. Retry("+(i)+") in "+backOff+"ms...");

            try {
                Thread.sleep(backOff);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }

            try {
                getScoreJs = CrimpApplication2.getCrimpWS().getScore(requestBean);
            } catch (IOException e){
                if(DEBUG) Log.d(TAG, "IOE while doing getScore. e:"+e.getMessage());
            }

            backOff = backOff * 2;
        }

        return getScoreJs;
    }

    @Nullable
    private LoginJs login(RequestBean requestBean){
        LoginJs loginJs = null;
        try {
            loginJs = CrimpApplication2.getCrimpWS().login(requestBean);
        } catch (IOException e){
            if(DEBUG) Log.d(TAG, "IOE while doing login. e:"+e.getMessage());
        }

        for(int i=1; loginJs==null && i<=RETRY; i++){
            if(DEBUG) Log.d(TAG, "login returns null. Retry("+(i)+") in "+backOff+"ms...");

            try {
                Thread.sleep(backOff);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }

            try {
                loginJs = CrimpApplication2.getCrimpWS().login(requestBean);
            } catch (IOException e){
                if(DEBUG) Log.d(TAG, "IOE while doing login. e:"+e.getMessage());
            }

            backOff = backOff * 2;
        }

        return loginJs;
    }
}
