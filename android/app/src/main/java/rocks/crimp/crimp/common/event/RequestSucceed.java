package rocks.crimp.crimp.common.event;

import java.util.UUID;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RequestSucceed {
    public final UUID txId;
    public final Object value;

    public RequestSucceed(UUID txId, Object value){
        this.txId = txId;
        this.value = value;
    }
}
