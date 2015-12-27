package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by user on 17-Jul-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActiveClimbersResponse {
    public String toString(){
        return "{}";
    }
}
