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

    @JsonProperty("_id")
    private String routeId;
    @JsonProperty("route_name")
    private String routeName;
    @JsonProperty("score_rules")
    private String scoreRules;

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getScoreRules() {
        return scoreRules;
    }

    public void setScoreRules(String scoreRules) {
        this.scoreRules = scoreRules;
    }
}
