package com.nusclimb.live.crimp.common.event;

import java.util.UUID;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RequestFailed {
    public final UUID txId;

    public RequestFailed(UUID txId){
        this.txId = txId;
    }
}
