package com.nusclimb.live.crimp;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.nusclimb.live.crimp.common.MainThreadBus;
import com.nusclimb.live.crimp.network.CrimpWS;
import com.nusclimb.live.crimp.network.CrimpWsImpl;
import com.nusclimb.live.crimp.network.StubWS;
import com.nusclimb.live.crimp.persistence.LocalModel;
import com.nusclimb.live.crimp.persistence.LocalModelImpl;
import com.nusclimb.live.crimp.persistence.StubLocalModel;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CrimpApplication2 extends Application {
    private static final String DB_NAME = "crimp-db";

    private static Context mContext;
    private static Bus bus;
    private SQLiteDatabase db;
    //private DaoMaster mDaoMaster;
    private static CrimpWS mCrimpWs;
    private static LocalModel mLocalModel;

    @Override
    public void onCreate(){
        super.onCreate();
        mContext = this;
        //DaoMaster.DevOpenHelper mDbHelper = new DaoMaster.DevOpenHelper(this, DB_NAME, null);
        //db = mDbHelper.getWritableDatabase();
        //mDaoMaster = new DaoMaster(db);

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
