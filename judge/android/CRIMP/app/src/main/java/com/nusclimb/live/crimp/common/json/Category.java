package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by user on 30-Jun-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {
    @JsonProperty("name")
    private String categoryName;
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("route_count")
    private int routeCount;
    @JsonProperty("scores_finalized")
    private boolean scoresFinalized;
    @JsonProperty("time_start")
    private String timeStart;
    @JsonProperty("time_end")
    private String timeEnd;

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\tcategory_name: '"+categoryName+"',\n");
        sb.append("\tcategory_id: '"+categoryId+"',\n");
        sb.append("\troute_count: "+routeCount+",\n");
        sb.append("\tscores_finalized: "+scoresFinalized+",\n");
        sb.append("\ttime_start: "+timeStart+",\n");
        sb.append("\ttime_end: "+timeEnd+",\n");
        sb.append("}");

        return sb.toString();
    }

    /*=========================================================================
     * Getter/Setter
     *=======================================================================*/
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public int getRouteCount() {
        return routeCount;
    }

    public void setRouteCount(int routeCount) {
        this.routeCount = routeCount;
    }

    public boolean isScoresFinalized() {
        return scoresFinalized;
    }

    public void setScoresFinalized(boolean scoresFinalized) {
        this.scoresFinalized = scoresFinalized;
    }

    public String getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(String timeStart) {
        this.timeStart = timeStart;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }
}
