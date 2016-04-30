package rocks.crimp.crimp.persistence;

import java.io.Serializable;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public interface LocalModel {
    boolean isDataExist(String key);
    <T> T fetch(String txId, Class<T> valueType);
    boolean putData(String key, Serializable value);
    void deleteModel();
}
