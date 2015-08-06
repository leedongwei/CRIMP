package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Response body for GET '/api/judge/climber/:climber_id'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ClimberResponseBody {
    @JsonProperty("climber_id")
    private String climberId;
    @JsonProperty("climber_name")
    private String climberName;
    @JsonProperty("total_score")
    private String totalScore;

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\tclimber_id: "+climberId+",\n");
        sb.append("\tclimber_name: "+climberName+",\n");
        sb.append("\ttotal_score: "+totalScore+"\n");
        sb.append("}");

        return sb.toString();
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

    public String getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(String totalScore) {
        this.totalScore = totalScore;
    }
}
