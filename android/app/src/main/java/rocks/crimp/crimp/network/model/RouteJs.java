package rocks.crimp.crimp.network.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteJs implements Serializable{
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty("route_id")
    private long routeId;
    @JsonProperty("route_name")
    private String routeName;
    @JsonProperty("score_type")
    private String scoreType;
    @JsonProperty("score_finalized")
    private boolean scoreFinalized;
    @JsonProperty("time_start")
    private Date timeStart;
    @JsonProperty("time_end")
    private Date timeEnd;

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getScoreType() {
        return scoreType;
    }

    public void setScoreType(String scoreType) {
        this.scoreType = scoreType;
    }

    public boolean isScoreFinalized() {
        return scoreFinalized;
    }

    public void setScoreFinalized(boolean scoreFinalized) {
        this.scoreFinalized = scoreFinalized;
    }

    public Date getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(Date timeStart) {
        this.timeStart = timeStart;
    }

    public Date getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(Date timeEnd) {
        this.timeEnd = timeEnd;
    }
}
