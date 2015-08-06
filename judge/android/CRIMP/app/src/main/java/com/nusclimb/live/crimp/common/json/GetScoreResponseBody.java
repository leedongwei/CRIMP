package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Response body for GET '/api/judge/score/:category_id/:route_id/:climber_id'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class GetScoreResponseBody {
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("route_id")
    private String routeId;
    @JsonProperty("climber_id")
    private String climberId;
    @JsonProperty("climber_name")
    private String climberName;
    @JsonProperty("score_string")
    private String scoreString;

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\tcategory_id: "+categoryId+",\n");
        sb.append("\troute_id: "+routeId+",\n");
        sb.append("\tclimber_id: "+climberId+",\n");
        sb.append("\tclimber_name: "+climberName+",\n");
        sb.append("\tscore_string: "+scoreString+"\n");
        sb.append("}");

        return sb.toString();
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getClimberId() {
        return climberId;
    }

    public void setClimberId(String climberId) {
        this.climberId = climberId;
    }

    public String getClimberName() {
        return climberName;
    }

    public void setClimberName(String climberName) {
        this.climberName = climberName;
    }

    public String getScoreString() {
        return scoreString;
    }

    public void setScoreString(String scoreString) {
        this.scoreString = scoreString;
    }
}
