package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.ActiveClimberResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Spice request for POST '/api/judge/activeclimber'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ActiveClimberRequest extends SpringAndroidSpiceRequest<ActiveClimberResponseBody> {
    private static final String TAG = ActiveClimberRequest.class.getSimpleName();

    private String xUserId;
    private String xAuthToken;
    private String categoryId;
    private String routeId;
    private String climberId;
    private boolean insert;
    private String url;

    public ActiveClimberRequest(String xUserId, String xAuthToken, String categoryId,
                         String routeId, String climberId, boolean insert, Context context) {
        super(ActiveClimberResponseBody.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.categoryId = categoryId;
        this.routeId = routeId;
        this.climberId = climberId;
        this.insert = insert;

        url = context.getString(R.string.crimp_url)+context.getString(R.string.activeclimber_api);
    }

    @Override
    public ActiveClimberResponseBody loadDataFromNetwork() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cache-Control", "no-cache");
        headers.set("x-user-id", xUserId);
        headers.set("x-auth-token", xAuthToken);

        HttpBody body = new HttpBody(categoryId, routeId, climberId, insert);
        HttpEntity<HttpBody> request = new HttpEntity<HttpBody>(body, headers);

        RestTemplate mRestTemplate = getRestTemplate();
        ResponseEntity<ActiveClimberResponseBody> response = mRestTemplate.exchange(url,
                HttpMethod.POST, request, ActiveClimberResponseBody.class);

        return response.getBody();
    }

    /**
     * Create a cache key for this request. Cache key will allow us to
     * cancel/aggregate/cache this request.
     *
     * @return Cache key for this request.
     */
    public String createCacheKey() {
        return categoryId+routeId+climberId+insert;
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
