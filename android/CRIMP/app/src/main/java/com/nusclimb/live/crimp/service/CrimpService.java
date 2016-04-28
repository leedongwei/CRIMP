package com.nusclimb.live.crimp.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nusclimb.live.crimp.CrimpApplication2;
import com.nusclimb.live.crimp.common.event.RequestFailed;
import com.nusclimb.live.crimp.common.event.RequestSucceed;
import com.nusclimb.live.crimp.network.model.CategoriesJs;
import com.nusclimb.live.crimp.network.model.CategoryJs;
import com.nusclimb.live.crimp.network.model.GetScoreJs;
import com.nusclimb.live.crimp.network.model.LoginJs;
import com.nusclimb.live.crimp.network.model.RequestBean;
import com.nusclimb.live.crimp.network.model.RouteJs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import timber.log.Timber;

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
                //TODO INJECTION
                RouteJs route1a = new RouteJs();
                route1a.setRouteName("route 1a");
                RouteJs route1b = new RouteJs();
                route1b.setRouteName("route 1b");
                RouteJs route2a = new RouteJs();
                route2a.setRouteName("route 2a");
                RouteJs route2b = new RouteJs();
                route2b.setRouteName("route 2b");

                CategoryJs category1 = new CategoryJs();
                category1.setCategoryName("category 1");
                ArrayList<RouteJs> cat1Route = new ArrayList<>();
                cat1Route.add(route1a);
                cat1Route.add(route1b);
                category1.setRoutes(cat1Route);

                CategoryJs category2 = new CategoryJs();
                category2.setCategoryName("category 2");
                ArrayList<RouteJs> cat2Route = new ArrayList<>();
                cat2Route.add(route2a);
                cat2Route.add(route2b);
                category2.setRoutes(cat2Route);

                ArrayList<CategoryJs> categoryList = new ArrayList<>();
                categoryList.add(category1);
                categoryList.add(category2);

                categoriesJs = new CategoriesJs();  //TODO INJECTION
                categoriesJs.setCategories(categoryList);

                if(categoriesJs != null){
                    //TODO UPDATE TRANSACTION LOG

                    // write to local model
                    CrimpApplication2.getLocalModel().putData(txId.toString(), categoriesJs);
                    Timber.d("Posting responseReceived: %s", txId);
                    CrimpApplication2.getBusInstance().post(new RequestSucceed(txId));
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
                    CrimpApplication2.getBusInstance().post(new RequestSucceed(txId));
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
                    CrimpApplication2.getBusInstance().post(new RequestSucceed(txId));
                }
                else{
                    //TODO UPDATE TRANSACTION LOG

                    if(DEBUG) Log.d(TAG, "Posting requestFailed: "+txId);
                    CrimpApplication2.getBusInstance().post(new RequestFailed(txId));
                }
                break;
            case ACTION_REPORT_IN:
                CrimpApplication2.getBusInstance().post(new RequestSucceed(txId));
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
            Timber.e(e, "IOE while doing getCategories");
        }

        for(int i=1; categoriesJs==null && i<=RETRY; i++){
            Timber.d("getCategories returns null. Retry(%d) in %dms...",i ,backOff);

            try {
                Thread.sleep(backOff);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }

            try {
                categoriesJs = CrimpApplication2.getCrimpWS().getCategories();
            } catch (IOException e){
                Timber.d(e, "IOE while doing getCategories");
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
