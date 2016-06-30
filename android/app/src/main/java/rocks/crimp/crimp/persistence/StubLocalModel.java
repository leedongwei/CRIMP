package rocks.crimp.crimp.persistence;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import rocks.crimp.crimp.network.model.CategoriesJs;
import timber.log.Timber;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class StubLocalModel implements LocalModel {
    @Override
    public boolean setupModel(File directory) {
        return false;
    }

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
    public boolean deleteModel() {
        return true;
    }

    @Override
    public boolean saveCategoriesAndCloseStream(ObjectOutputStream outputStream, CategoriesJs categoriesJs) {
        return false;
    }

    @Override
    public CategoriesJs loadCategoriesAndCloseStream(ObjectInputStream inputStream) {
        return null;
    }
}
