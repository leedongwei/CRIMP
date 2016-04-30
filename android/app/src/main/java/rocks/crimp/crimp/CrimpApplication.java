package rocks.crimp.crimp;

import android.app.Application;
import android.content.Context;

import com.squareup.otto.Bus;

import rocks.crimp.crimp.common.MainThreadBus;
import rocks.crimp.crimp.network.CrimpWS;
import rocks.crimp.crimp.network.StubWS;
import rocks.crimp.crimp.persistence.LocalModel;
import rocks.crimp.crimp.persistence.StubLocalModel;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CrimpApplication extends Application {
    private static final String DB_NAME = "crimp-db";

    private static Context mContext;
    private static Bus bus;
    private static CrimpWS mCrimpWs;
    private static LocalModel mLocalModel;

    @Override
    public void onCreate(){
        super.onCreate();
        mContext = this;
        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }

    }

    public static Context getContext(){
        return mContext;
    }

    public static LocalModel getLocalModel(){
        if(mLocalModel == null){
            //mLocalModel = LocalModelImpl.getInstance(getContext());
            mLocalModel = new StubLocalModel();
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
