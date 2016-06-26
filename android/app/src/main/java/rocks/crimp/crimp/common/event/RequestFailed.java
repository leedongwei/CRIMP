package rocks.crimp.crimp.common.event;

import java.util.UUID;

import rocks.crimp.crimp.network.model.ErrorJs;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RequestFailed {
    public final UUID txId;
    public final ErrorJs errorJs;

    public RequestFailed(UUID txId, ErrorJs errorJs){
        this.txId = txId;
        this.errorJs = errorJs;
    }
}
