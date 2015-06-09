package com.nusclimb.live.crimp.common.json;

/**
 * Jackson POJO
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class Session {
    private String sessionToken;

    public String getSessionToken(){
        return sessionToken;
    }

    public void setSessionToken(String sessionToken){
        this.sessionToken = sessionToken;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{ sessionToken="+sessionToken);
        sb.append(" }");

        return sb.toString();
    }
}
