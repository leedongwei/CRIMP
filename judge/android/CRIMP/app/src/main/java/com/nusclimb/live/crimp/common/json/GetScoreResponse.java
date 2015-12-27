package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by user on 17-Jul-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetScoreResponse {
    @JsonProperty("route_id")
    private String routeId;
    @JsonProperty("climber_id")
    private String climberId;
    @JsonProperty("climber_name")
    private String climberName;
    @JsonProperty("score_string")
    private String score;

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\troute_id: '"+routeId+"',\n");
        sb.append("\tclimber_id: '"+climberId+"',\n");
        sb.append("\tclimber_name: '"+climberName+"',\n");
        sb.append("\tscore: '"+score+"'\n");
        sb.append("}");

        return sb.toString();
    }


    /*=========================================================================
     * Getter/Setter
     *=======================================================================*/
    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getClimberId(){
        return climberId;
    }

    public void setClimberId(String climberId){
        this.climberId = climberId;
    }

    public String getClimberName() {
        return climberName;
    }

    public void setClimberName(String climberName){
        this.climberName = climberName;
    }

    public String getScore(){
        return score;
    }

    public void setScore(String score){
        this.score = score;
    }
}