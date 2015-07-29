package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by user on 17-Jul-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Climber {
    @JsonProperty("climber_id")
    private String climberId;
    @JsonProperty("climber_name")
    private String climberName;

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\tclimber_id: '"+climberId+"',\n");
        sb.append("\tclimber_name: '"+climberName+"',\n");
        sb.append("}");

        return sb.toString();
    }

    /*=========================================================================
     * Getter/Setter
     *=======================================================================*/
    public String getClimberId() {
        return climberId;
    }

    public void setClimberId(String climberId) {
        this.climberId = climberId;
    }

    public String getClimberName(){
        return climberName;
    }

    public void setClimberName(String climberName){
        this.climberName = climberName;
    }
}
