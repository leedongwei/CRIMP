package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by user on 02-Jul-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportResponse {
    @JsonProperty("admin_id")
    private String adminId;
    @JsonProperty("admin_name")
    private String adminName;
    @JsonProperty("route_id")
    private String routeId;
    @JsonProperty("state")
    private int state;

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\tadmin_id: '"+adminId+"',\n");
        sb.append("\tadmin_name: '"+adminName+"',\n");
        sb.append("\troute_id: '"+routeId+"',\n");
        sb.append("\tstate: "+state+"\n");
        sb.append("}");

        return sb.toString();
    }

    /*=========================================================================
     * Getter/Setter
     *=======================================================================*/
    public String getAdminId() {
        return adminId;
    }
    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }
    public String getAdminName() {
        return adminName;
    }
    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
    public String getRouteId() {
        return routeId;
    }
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }
}
