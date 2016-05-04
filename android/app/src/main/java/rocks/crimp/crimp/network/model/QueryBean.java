package rocks.crimp.crimp.network.model;

import java.io.Serializable;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class QueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long climberId;
    private Long categoryId;
    private Long routeId;
    private String markerId;

    public Long getClimberId() {
        return climberId;
    }

    public void setClimberId(Long climberId) {
        this.climberId = climberId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getMarkerId() {
        return markerId;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }
}
