package com.nusclimb.live.crimp.common.event;

import java.util.UUID;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ResponseReceived {
    public final UUID txId;

    public ResponseReceived(UUID txId){
        this.txId = txId;
    }
}
