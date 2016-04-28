package com.nusclimb.live.crimp.common.event;

import java.util.UUID;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RequestSucceed {
    public final UUID txId;

    public RequestSucceed(UUID txId){
        this.txId = txId;
    }
}
