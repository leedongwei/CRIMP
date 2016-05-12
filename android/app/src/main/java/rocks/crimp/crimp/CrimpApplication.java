package rocks.crimp.crimp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.os.Looper;

import com.squareup.otto.Bus;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;

import rocks.crimp.crimp.common.MainThreadBus;
import rocks.crimp.crimp.network.CrimpWS;
import rocks.crimp.crimp.network.StubWS;
import rocks.crimp.crimp.persistence.LocalModel;
import rocks.crimp.crimp.persistence.LocalModelImpl;
import rocks.crimp.crimp.persistence.StubLocalModel;
import rocks.crimp.crimp.service.CrimpService;
import rocks.crimp.crimp.service.RestHandler;
import rocks.crimp.crimp.service.ScoreHandler;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CrimpApplication extends Application {
    private static final String APP_SHARED_PREF = "crimp_state";
    public static final String FB_USER_ID = "fb_user_id";
    public static final String FB_ACCESS_TOKEN = "fb_access_token";
    public static final String FB_USER_NAME = "fb_user_name";
    public static final String SEQUENTIAL_TOKEN = "sequential_token";
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
    public static final String MARKER_ID_TEMP = "marker_id_temp";

    private static Context mContext;
    private static Bus bus;
    private static CrimpWS mCrimpWs;
    private static LocalModel mLocalModel;
    private static SharedPreferences mAppState;
    private static HandlerThread mRestHandlerThread;
    private static HandlerThread mScoreHandlerThread;
    private static ScoreHandler mScoreHandler;
    private static RestHandler mRestHandler;

    @Override
    public void onCreate(){
        super.onCreate();
        mContext = this;
        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }

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
            mCrimpWs = new StubWS();
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
