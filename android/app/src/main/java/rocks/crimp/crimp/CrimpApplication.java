package rocks.crimp.crimp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.squareup.otto.Bus;

import rocks.crimp.crimp.common.MainThreadBus;
import rocks.crimp.crimp.network.CrimpWS;
import rocks.crimp.crimp.network.StubWS;
import rocks.crimp.crimp.persistence.LocalModel;
import rocks.crimp.crimp.persistence.LocalModelImpl;
import rocks.crimp.crimp.persistence.StubLocalModel;
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
    public static final String CATEGORY_ID = "route_id";
    public static final String ROUTE_ID = "route_id";
    public static final String CLIMBER_ID = "climber_id";

    private static Context mContext;
    private static Bus bus;
    private static CrimpWS mCrimpWs;
    private static LocalModel mLocalModel;
    private static SharedPreferences mAppState;

    @Override
    public void onCreate(){
        super.onCreate();
        mContext = this;
        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }
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
