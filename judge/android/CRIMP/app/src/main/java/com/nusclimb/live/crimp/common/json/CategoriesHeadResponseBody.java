package com.nusclimb.live.crimp.common.json;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Response body for GET '/api/judge/categories_head'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CategoriesHeadResponseBody {
    @JsonProperty("signature")
    private String signature;
    @JsonProperty("time_stamp")
    private String timeStamp;   // TODO Decide type for time_stamp

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\tsignature: "+signature+",\n");
        sb.append("\ttime_stamp: "+timeStamp+"\n");
        sb.append("}");

        return sb.toString();
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
