package com.nusclimb.live.crimp.network.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class SetActiveJs implements Serializable{
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty("climber_id")
    private long climberId;
    @JsonProperty("climber_name")
    private String climberName;
    @JsonProperty("active_route")
    private long activeRoute;

    public long getClimberId() {
        return climberId;
    }

    public void setClimberId(long climberId) {
        this.climberId = climberId;
    }

    public String getClimberName() {
        return climberName;
    }

    public void setClimberName(String climberName) {
        this.climberName = climberName;
    }

    public long getActiveRoute() {
        return activeRoute;
    }

    public void setActiveRoute(long activeRoute) {
        this.activeRoute = activeRoute;
    }
}
