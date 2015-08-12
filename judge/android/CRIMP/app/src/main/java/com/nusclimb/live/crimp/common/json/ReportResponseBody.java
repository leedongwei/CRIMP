package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Response body for POST '/api/judge/report'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ReportResponseBody {
    @JsonProperty("admin_id")
    private String adminId;
    @JsonProperty("admin_name")
    private String adminName;
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("route_id")
    private String routeId;
    @JsonProperty("state")
    private int state;

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\tadmin_id: "+adminId+",\n");
        sb.append("\tadmin_name: "+adminName+",\n");
        sb.append("\tcategory_id: "+categoryId+",\n");
        sb.append("\troute_id: "+routeId+",\n");
        sb.append("\tstate: "+state+"\n");
        sb.append("}");

        return sb.toString();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

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

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
}
