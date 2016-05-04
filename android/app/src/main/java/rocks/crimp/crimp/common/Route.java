package rocks.crimp.crimp.common;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

import rocks.crimp.crimp.network.model.RouteJs;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class Route implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long routeId;
    private final String routeName;
    private final String scoreType;
    private final Date timeStart;
    private final Date timeEnd;

    public Route(@NonNull Long routeId, @NonNull String routeName, @NonNull String scoreType,
                 @NonNull Date timeStart, @NonNull Date timeEnd) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.scoreType = scoreType;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
    }

    public Route(RouteJs routeJs){
        this.routeId = routeJs.getRouteId();
        this.routeName = routeJs.getRouteName();
        this.scoreType = routeJs.getScoreType();
        this.timeStart = routeJs.getTimeStart();
        this.timeEnd = routeJs.getTimeEnd();
    }

    @NonNull
    public Long getRouteId() {
        return routeId;
    }

    @NonNull
    public String getRouteName() {
        return routeName;
    }

    @NonNull
    public String getScoreType() {
        return scoreType;
    }

    @NonNull
    public Date getTimeStart() {
        return timeStart;
    }

    @NonNull
    public Date getTimeEnd() {
        return timeEnd;
    }
}
