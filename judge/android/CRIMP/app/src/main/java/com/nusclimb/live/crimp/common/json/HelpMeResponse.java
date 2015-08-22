package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by user on 02-Jul-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HelpMeResponse {
    @JsonProperty("status")
    private String status;

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\tstatus: '"+status+"',\n");
        sb.append("}");

        return sb.toString();
    }

    /*=========================================================================
     * Getter/Setter
     *=======================================================================*/
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
