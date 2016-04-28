package com.nusclimb.live.crimp.persistence;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public interface LocalModel {
    boolean isDataExist(String key);
    <T> T fetch(String txId, Class<T> valueType);
    boolean putData(String key, Serializable value);
}
