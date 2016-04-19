package com.nusclimb.live.crimp.network.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class PostScoreJs implements Serializable{
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty("climber_id")
    private long climberId;
    @JsonProperty("climber_name")
    private String climberName;
    @JsonProperty("category_id")
    private long categoryId;
    @JsonProperty("route_id")
    private long routeId;
    @JsonProperty("score")
    private String score;

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

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
