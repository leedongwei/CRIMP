package com.nusclimb.live.crimp.persistence;

import android.util.Log;

import java.io.Serializable;

import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class StubLocalModel implements LocalModel {
    private static final String TAG = "StubLocalModel";
    private static final boolean DEBUG = true;

    @Override
    public boolean isDataExist(String key) {
        if(DEBUG) Log.d(TAG, "isDataExist for key:" + key);

        return false;
    }

    @Override
    public <T> T fetch(String txId, Class<T> valueType) {
        if(DEBUG) Log.d(TAG, "fetch for key:" + txId + " class:"+valueType.getSimpleName());

        return null;
    }

    @Override
    public boolean putData(String key, Serializable value) {
        Timber.d("putData for key: %s", key);
        return false;
    }

    @Override
    public void deleteModel() {

    }
}
