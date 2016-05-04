package rocks.crimp.crimp.network.model;

import java.io.Serializable;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class PathBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long routeId;
    private Long climberId;

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public Long getClimberId() {
        return climberId;
    }

    public void setClimberId(Long climberId) {
        this.climberId = climberId;
    }
}
