package rocks.crimp.crimp.persistence;

import android.util.Log;

import java.io.Serializable;

import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class StubLocalModel implements LocalModel {
    @Override
    public boolean isDataExist(String key) {
        Timber.d("isDataExist for key:%s", key);

        return false;
    }

    @Override
    public <T> T fetch(String txId, Class<T> valueType) {
        Timber.d("fetch for key:%s class:%s", txId, valueType.getSimpleName());

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
