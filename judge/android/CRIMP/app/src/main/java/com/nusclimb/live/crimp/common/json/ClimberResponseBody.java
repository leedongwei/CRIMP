package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.IOException;

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
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String prettyString = null;
        try {
            prettyString = ow.writeValueAsString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prettyString;
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
