package rocks.crimp.crimp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import io.branch.referral.Branch;
import rocks.crimp.crimp.common.MainThreadBus;
import rocks.crimp.crimp.common.event.CurrentUploadTask;
import rocks.crimp.crimp.network.CrimpWS;
import rocks.crimp.crimp.network.CrimpWsImpl;
import rocks.crimp.crimp.persistence.LocalModel;
import rocks.crimp.crimp.persistence.LocalModelImpl;
import rocks.crimp.crimp.service.CrimpService;
import rocks.crimp.crimp.service.RestHandler;
import rocks.crimp.crimp.service.ScoreHandler;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CrimpApplication extends Application {
    private static final String APP_SHARED_PREF = "crimp_state";
    private static final String PERMISSION_SHARED_PREF = "permission_shared_pref";
    public static final String FB_ACCESS_TOKEN = "fb_access_token";
    public static final String FB_USER_NAME = "fb_user_name";
    public static final String X_USER_ID = "x_user_id";
    public static final String X_AUTH_TOKEN = "x_auth_token";
    public static final String ROLES = "ROLES";
    public static final String CAN_DISPLAY = "can_display";                 //update when refresh + rescan
    public static final String MARKER_ID = "marker_id";                     //erase when refresh + rescan
    public static final String CLIMBER_NAME = "climber_name";               //erase when refresh + rescan
    public static final String SHOULD_SCAN = "should_scan";                 //erase when refresh + rescan
    public static final String COMMITTED_CATEGORY = "committed_category";   //erase when refresh categories
    public static final String COMMITTED_ROUTE = "committed_route";         //erase when refresh categories
    public static final String CATEGORY_POSITION = "category_position";     //erase when refresh categories
    public static final String ROUTE_POSITION = "route_position";           //erase when refresh categories
    public static final String CURRENT_SCORE = "current_score";             //erase when refresh categories
    public static final String ACCUMULATED_SCORE = "accumulated_score";     //erase when refresh categories
    public static final String IMAGE_HEIGHT = "image_height";

    private static Context mContext;
    private static Bus bus;
    private static CrimpWS mCrimpWs;
    private static LocalModel mLocalModel;
    private static SharedPreferences mAppState;
    private static SharedPreferences mPermissionPreferences;
    private static HandlerThread mRestHandlerThread;
    private static HandlerThread mScoreHandlerThread;
    private static ScoreHandler mScoreHandler;
    private static RestHandler mRestHandler;
    private static NetworkChangeReceiver mNetworkChangeReceiver;
    private static int mUploadTaskCount;
    private static boolean mShouldUpdateNotificationCount = true;

    @Override
    public void onCreate(){
        super.onCreate();
        mContext = this;
        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }
        Branch.getAutoInstance(this);
        getBusInstance().register(this);

        Timber.d("mRestHandlerThread started");
        mRestHandlerThread = new HandlerThread("RestThread");
        mRestHandlerThread.start();
        Looper restThreadLooper = mRestHandlerThread.getLooper();
        mRestHandler = new RestHandler(restThreadLooper, this);

        Timber.d("mScoreHandlerThread started");
        mScoreHandlerThread = new HandlerThread("ScoreThread");
        mScoreHandlerThread.start();
        Looper scoreThreadLooper = mScoreHandlerThread.getLooper();
        mScoreHandler = new ScoreHandler(scoreThreadLooper, this);

        Intent intent = new Intent(this, CrimpService.class);
        intent.setAction(CrimpService.ACTION_BOOT_NO_INTENT);
        startService(intent);
    }

    @Subscribe
    public void receivedCurrentUploadTask(CurrentUploadTask event){
        switch(event.status){
            case CurrentUploadTask.IDLE:
                mShouldUpdateNotificationCount = true;
                break;
            case CurrentUploadTask.UPLOADING:
                mShouldUpdateNotificationCount = true;
                break;
            case CurrentUploadTask.ERROR_HTTP_STATUS:
                mShouldUpdateNotificationCount = false;
                break;
            case CurrentUploadTask.ERROR_EXCEPTION:
                mShouldUpdateNotificationCount = false;
                break;
            case CurrentUploadTask.ERROR_NO_NETWORK:
                mShouldUpdateNotificationCount = false;
                break;
            case CurrentUploadTask.UPLOAD_SUCCEED:
                mUploadTaskCount--;
                if(mUploadTaskCount > 0){
                    CrimpNotification.createAndShowUploading(this, mUploadTaskCount);
                    mShouldUpdateNotificationCount = true;
                }
                break;
            case CurrentUploadTask.DROP_TASK:
                mUploadTaskCount--;
                if(mUploadTaskCount > 0){
                    CrimpNotification.createAndShowUploading(this, mUploadTaskCount);
                    mShouldUpdateNotificationCount = true;
                }
        }
    }

    public static void setUploadTaskCount(int uploadTaskCount){
        mUploadTaskCount = uploadTaskCount;
        if(mShouldUpdateNotificationCount){
            if(mUploadTaskCount > 0){
                CrimpNotification.createAndShowUploading(getContext(), mUploadTaskCount);
            }
        }
    }

    public static int getUploadTaskCount(){
        return mUploadTaskCount;
    }

    public static NetworkChangeReceiver getNetworkChangeReceiver(){
        if(mNetworkChangeReceiver == null){
            mNetworkChangeReceiver = new NetworkChangeReceiver();
        }

        return mNetworkChangeReceiver;
    }

    public static RestHandler getRestHandler(){
        return mRestHandler;
    }

    public static ScoreHandler getScoreHandler(){
        return mScoreHandler;
    }

    public static HandlerThread getRestHandlerThread(){
        return mRestHandlerThread;
    }

    public static HandlerThread getScoreHandlerThread(){
        return mScoreHandlerThread;
    }

    public static SharedPreferences getAppState(){
        if(mAppState == null){
            mAppState = mContext.getSharedPreferences(APP_SHARED_PREF, Context.MODE_PRIVATE);
        }

        return mAppState;
    }

    public static SharedPreferences getPermissionPreferences(){
        if(mPermissionPreferences == null){
            mPermissionPreferences =
                    mContext.getSharedPreferences(PERMISSION_SHARED_PREF, Context.MODE_PRIVATE);
        }

        return mPermissionPreferences;
    }

    public static Context getContext(){
        return mContext;
    }

    public static LocalModel getLocalModel(){
        if(mLocalModel == null){
            mLocalModel = LocalModelImpl.getInstance();
            mLocalModel.setupModel(LocalModelImpl.getLocalModelDir(getContext()));
        }

        return mLocalModel;
    }

    public static CrimpWS getCrimpWS(){
        if(mCrimpWs == null){
            //mCrimpWs = new StubWS();
            mCrimpWs = new CrimpWsImpl(CrimpWsImpl.BASE_URL);
        }

        return mCrimpWs;
    }

    public static Bus getBusInstance(){
        if(bus == null){
            bus = new MainThreadBus();
        }

        return bus;
    }
}
