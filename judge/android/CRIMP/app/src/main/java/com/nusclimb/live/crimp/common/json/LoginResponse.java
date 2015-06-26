package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

/**
 * Jackson POJO
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class LoginResponse {
    @JsonProperty("x-user-id")
    private String xUserId;
    @JsonProperty("x-auth-token")
    private String xAuthToken;
    private ArrayList<String> roles;

    public String getxUserId(){
        return xUserId;
    }

    public String getxAuthToken(){
        return xAuthToken;
    }

    public ArrayList<String> getRoles(){
        return roles;
    }

    public void setxUserId(String xUserId){
        this.xUserId = xUserId;
    }

    public void setxAuthToken(String xAuthToken){
        this.xAuthToken = xAuthToken;
    }

    public void setRoles(ArrayList<String> roles){
        this.roles = roles;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{ x-user-id="+ xUserId);
        sb.append(", x-auth-token="+ xAuthToken);
        sb.append(", roles=[");

        StringBuilder rolesSB = new StringBuilder();
        if(roles.size() <= 1) {
            for (String s : roles) {
                rolesSB.append(s);
            }
        }
        else{
            rolesSB.append(roles.get(0));
            for (String s : roles) {
                rolesSB.append(", "+s);
            }
        }
        sb.append(rolesSB);

        sb.append("] }");

        return sb.toString();
    }
}
