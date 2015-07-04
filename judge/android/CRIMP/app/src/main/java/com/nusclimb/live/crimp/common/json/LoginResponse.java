package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

/**
 * Created by user on 30-Jun-15.
 */
public class LoginResponse {
    @JsonProperty("x-user-id")
    private String xUserId;
    @JsonProperty("x-auth-token")
    private String xAuthToken;
    @JsonProperty("roles")
    private ArrayList<String> roles;

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("x-user-id: '"+xUserId+"',\n");
        sb.append("x-auth-token: '"+xAuthToken+",\n");
        sb.append("roles: [");

        if(roles.size()<=1){
            for(String s:roles){
                sb.append("'"+s+"'");
            }
        }
        else{
            sb.append("'"+roles.get(0).toString()+"'");

            for(int i=1; i<roles.size(); i++){
                sb.append(", '"+roles.get(i).toString()+"'");
            }
        }

        sb.append("]");
        return sb.toString();
    }


    /*=========================================================================
     * Getter/Setter
     *=======================================================================*/
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


    public ArrayList<String> getRoles() {
        return roles;
    }

    public void setRoles(ArrayList<String> roles) {
        this.roles = roles;
    }
}