package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonProperty;

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
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\tx-user-id: "+xUserId+",\n");
        sb.append("\tx-auth-token: "+xAuthToken+"\n");
        sb.append("}");

        return sb.toString();
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
