package rocks.crimp.crimp.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import rocks.crimp.crimp.CrimpApplication;
import rocks.crimp.crimp.common.event.RequestFailed;
import rocks.crimp.crimp.common.event.RequestSucceed;
import rocks.crimp.crimp.network.model.CategoriesJs;
import rocks.crimp.crimp.network.model.CategoryJs;
import rocks.crimp.crimp.network.model.GetScoreJs;
import rocks.crimp.crimp.network.model.LoginJs;
import rocks.crimp.crimp.network.model.ReportJs;
import rocks.crimp.crimp.network.model.RequestBean;
import rocks.crimp.crimp.network.model.RouteJs;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CrimpService extends IntentService{
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
        boolean hasData = CrimpApplication.getLocalModel().isDataExist(txId.toString());
        backOff = MIN_BACKOFF;

        Timber.d("Received intent action:%s txId:%s", intent.getAction(), txId);

        // Received an intent. Check what is the work to be done.
        switch(intent.getAction()){
            case ACTION_GET_CATEGORIES:
                if(hasData){
                    CrimpApplication.getBusInstance().post(new RequestSucceed(txId));
                }
                else{
                    CategoriesJs categoriesJs = getCategories();
                    if(categoriesJs != null){
                        CrimpApplication.getLocalModel().putData(txId.toString(), categoriesJs);
                        CrimpApplication.getBusInstance().post(new RequestSucceed(txId));
                    }
                    else{
                        CrimpApplication.getBusInstance().post(new RequestFailed(txId));
                    }
                }
                break;
            case ACTION_GET_SCORE:
                GetScoreJs getScoreJs = getScore(bean);

                if(getScoreJs != null){
                    //TODO UPDATE TRANSACTION LOG

                    // write to local model
                    CrimpApplication.getLocalModel().putData(txId.toString(), getScoreJs);
                    Timber.d("Posting responseReceived: %s", txId);
                    CrimpApplication.getBusInstance().post(new RequestSucceed(txId));
                }
                else{
                    //TODO UPDATE TRANSACTION LOG

                    Timber.d("Posting requestFailed: %s", txId);
                    CrimpApplication.getBusInstance().post(new RequestFailed(txId));
                }
                break;
            case ACTION_SET_ACTIVE:
                break;
            case ACTION_CLEAR_ACTIVE:
                break;
            case ACTION_LOGIN:
                if(hasData){
                    CrimpApplication.getBusInstance().post(new RequestSucceed(txId));
                }
                else{
                    LoginJs loginJs = login(bean);
                    if(loginJs != null){
                        CrimpApplication.getLocalModel().putData(txId.toString(), loginJs);
                        CrimpApplication.getBusInstance().post(new RequestSucceed(txId));
                    }
                    else{
                        CrimpApplication.getBusInstance().post(new RequestFailed(txId));
                    }
                }
                break;
            case ACTION_REPORT_IN:
                if(hasData){
                    CrimpApplication.getBusInstance().post(new RequestSucceed(txId));
                }
                else{
                    ReportJs reportJs = report(bean);
                    if(reportJs != null){
                        CrimpApplication.getLocalModel().putData(txId.toString(), reportJs);
                        CrimpApplication.getBusInstance().post(new RequestSucceed(txId));
                    }
                    else{
                        CrimpApplication.getBusInstance().post(new RequestFailed(txId));
                    }
                }

                CrimpApplication.getBusInstance().post(new RequestSucceed(txId));
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
            categoriesJs = CrimpApplication.getCrimpWS().getCategories();
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
                categoriesJs = CrimpApplication.getCrimpWS().getCategories();
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
            getScoreJs = CrimpApplication.getCrimpWS().getScore(requestBean);
        } catch (IOException e){
            Timber.e(e, "IOE while doing getScore.");
        }

        for(int i=1; getScoreJs==null && i<=RETRY; i++){
            Timber.d("getScore returns null. Retry(%d) in %dms...", i, backOff);

            try {
                Thread.sleep(backOff);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }

            try {
                getScoreJs = CrimpApplication.getCrimpWS().getScore(requestBean);
            } catch (IOException e){
                Timber.e(e, "IOE while doing getScore");
            }

            backOff = backOff * 2;
        }

        return getScoreJs;
    }

    @Nullable
    private LoginJs login(RequestBean requestBean){
        LoginJs loginJs = null;
        try {
            loginJs = CrimpApplication.getCrimpWS().login(requestBean);
        } catch (IOException e){
            Timber.e(e, "IOE while doing login");
        }

        for(int i=1; loginJs==null && i<=RETRY; i++){
            Timber.d("login returns null. Retry(%d) in %dms...", i, backOff);

            try {
                Thread.sleep(backOff);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }

            try {
                loginJs = CrimpApplication.getCrimpWS().login(requestBean);
            } catch (IOException e){
                Timber.e(e, "IOE while doing login");
            }

            backOff = backOff * 2;
        }

        return loginJs;
    }

    @Nullable
    private ReportJs report(RequestBean requestBean){
        ReportJs reportJs = null;

        try {
            reportJs = CrimpApplication.getCrimpWS().reportIn(requestBean);
        } catch (IOException e){
            Timber.e(e, "IOE while doing report");
        }

        for(int i=1; reportJs==null && i<=RETRY; i++){
            Timber.d("report returns null. Retry(%d) in %dms...", i, backOff);

            try {
                Thread.sleep(backOff);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }

            try {
                reportJs = CrimpApplication.getCrimpWS().reportIn(requestBean);
            } catch (IOException e){
                Timber.e(e, "IOE while doing report");
            }

            backOff = backOff * 2;
        }

        return reportJs;
    }
}
