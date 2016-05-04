package rocks.crimp.crimp.persistence;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import rocks.crimp.crimp.network.model.CategoriesJs;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public interface LocalModel {
    boolean setupModel(File directory);
    boolean isDataExist(String key);
    <T> T fetch(String txId, Class<T> valueType);
    boolean putData(String key, Serializable value);
    boolean deleteModel();
    boolean saveCategoriesAndCloseStream(ObjectOutputStream outputStream, CategoriesJs categoriesJs);
    CategoriesJs loadCategoriesAndCloseStream(ObjectInputStream inputStream);
}
