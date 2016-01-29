package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.IOException;

/**
 * Response body for POST '/api/judge/login'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LoginResponseBody {
    @JsonProperty("x-user-id")
    private String xUserId;
    @JsonProperty("x-auth-token")
    private String xAuthToken;

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

    public String getxUserId() {
        return xUserId;
    }

    public void setxUserId(String xUserId) {
        this.xUserId = xUserId;
    }

    public String getxAuthToken() {
        return xAuthToken;
    }

    public void setxAuthToken(String xAuthToken) {
        this.xAuthToken = xAuthToken;
    }
}
