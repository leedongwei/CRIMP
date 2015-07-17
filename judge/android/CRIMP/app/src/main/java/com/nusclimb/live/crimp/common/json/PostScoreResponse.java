package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by user on 17-Jul-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostScoreResponse {
    @JsonProperty("error")
    private String error;

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\terror: '"+error+"'\n");
        sb.append("}");

        return sb.toString();
    }


    /*=========================================================================
     * Getter/Setter
     *=======================================================================*/
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
