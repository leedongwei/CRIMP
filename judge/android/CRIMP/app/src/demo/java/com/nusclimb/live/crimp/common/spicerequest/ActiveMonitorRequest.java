package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.ActiveMonitorResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Spice request for POST '/api/judge/activemonitor'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ActiveMonitorRequest extends SpringAndroidSpiceRequest<ActiveMonitorResponseBody> {
    private static final String TAG = ActiveMonitorRequest.class.getSimpleName();

    private Context context;
    private String xUserId;
    private String xAuthToken;
    private String categoryId;
    private String routeId;
    private String climberId;
    private boolean insert;
    private String url;

    public ActiveMonitorRequest(String xUserId, String xAuthToken, String categoryId,
                                String routeId, String climberId, boolean insert, Context context) {
        super(ActiveMonitorResponseBody.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.categoryId = categoryId;
        this.routeId = routeId;
        this.climberId = climberId;
        this.insert = insert;
        this.context = context;

        this.url = context.getString(R.string.crimp_base_url)+context.getString(R.string.activemonitor_api);
    }

    @Override
    public ActiveMonitorResponseBody loadDataFromNetwork() throws Exception {
        return new ActiveMonitorResponseBody();
    }

    /**
     * Jackson POJO for ActiveClimber request body.
     */
    private static class HttpBody {
        @JsonProperty("category_id")
        private String categoryId;
        @JsonProperty("route_id")
        private String routeId;
        @JsonProperty("climber_id")
        private String climberId;
        @JsonProperty("insert")
        private boolean insert;

        public HttpBody(String categoryId, String routeId, String climberId, boolean insert){
            this.categoryId = categoryId;
            this.routeId = routeId;
            this.climberId = climberId;
            this.insert = insert;
        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("\tcategory_id: "+categoryId+",\n");
            sb.append("\troute_id: "+routeId+",\n");
            sb.append("\tclimber_id: "+climberId+",\n");
            sb.append("\tinsert: "+insert+"\n");
            sb.append("}");

            return sb.toString();
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

        public String getClimberId() {
            return climberId;
        }

        public void setClimberId(String climberId) {
            this.climberId = climberId;
        }

        public boolean isInsert() {
            return insert;
        }

        public void setInsert(boolean insert) {
            this.insert = insert;
        }
    }
}
