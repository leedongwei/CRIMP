package rocks.crimp.crimp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;

import rocks.crimp.crimp.CrimpApplication;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CrimpService extends Service {
    public static final String ACTION_GET_CATEGORIES = "action_get_categories";
    public static final String ACTION_GET_SCORE = "action_get_score";
    public static final String ACTION_SET_ACTIVE = "action_set_active";
    public static final String ACTION_CLEAR_ACTIVE = "action_clear_active";
    public static final String ACTION_LOGIN = "action_login";
    public static final String ACTION_REPORT_IN = "action_report_in";
    public static final String ACTION_REQUEST_HELP = "action_request_help";
    public static final String ACTION_POST_SCORE = "action_post_score";
    public static final String ACTION_LOGOUT = "action_logout";
    public static final String ACTION_BOOT_NO_INTENT = "action_boot_no_intent";

    public static final String SERIALIZABLE_UUID = "serializable_uuid";
    public static final String SERIALIZABLE_REQUEST = "serializable_request";

    @Override
    public void onCreate() {
        Timber.d("onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We are checking if the service is started because we want to make a network call.
        // When this service is recreated by system, it gets a null intent.
        if(intent != null && !intent.getAction().equals(ACTION_BOOT_NO_INTENT)){
            String action = intent.getAction();
            Timber.d("Received intent action: %s, txId: %s",
                    action, intent.getSerializableExtra(SERIALIZABLE_UUID));

            if(action.equals(ACTION_POST_SCORE)){
                Message msg = CrimpApplication.getScoreHandler()
                        .obtainMessage(ScoreHandler.NEW_UPLOAD, intent);
                CrimpApplication.getScoreHandler().sendMessage(msg);
                CrimpApplication.setUploadTaskCount(CrimpApplication.getUploadTaskCount()+1);
            }
            else{
                Message msg = CrimpApplication.getRestHandler()
                        .obtainMessage(RestHandler.FETCH_LOCAL, intent);
                CrimpApplication.getRestHandler().sendMessage(msg);
            }
        }
        else{
            Timber.d("service started without making new request.");
            //TODO
            CrimpApplication.getScoreHandler().obtainMessage(ScoreHandler.DO_WORK).sendToTarget();
            CrimpApplication.getRestHandler().obtainMessage(RestHandler.DO_WORK).sendToTarget();
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy");
    }
}