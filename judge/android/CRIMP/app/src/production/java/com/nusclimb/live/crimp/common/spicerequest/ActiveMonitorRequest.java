package com.nusclimb.live.crimp.common.spicerequest;

import com.nusclimb.live.crimp.network.model.ActiveMonitorJackson;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * Spice request for POST '/api/judge/activemonitor'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ActiveMonitorRequest extends SpringAndroidSpiceRequest<ActiveMonitorJackson> {
    private static final String TAG = ActiveMonitorRequest.class.getSimpleName();

    private String xUserId;
    private String xAuthToken;
    private String categoryId;
    private String routeId;
    private String climberId;
    private boolean insert;
    private String url;

    public ActiveMonitorRequest(String xUserId, String xAuthToken, String categoryId,
                                String routeId, String climberId, boolean insert, String url) {
        super(ActiveMonitorJackson.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.categoryId = categoryId;
        this.routeId = routeId;
        this.climberId = climberId;
        this.insert = insert;
        this.url = url;
    }

    @Override
    public ActiveMonitorJackson loadDataFromNetwork() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cache-Control", "no-cache");
        headers.set("x-user-id", xUserId);
        headers.set("x-auth-token", xAuthToken);

        HttpBody body = new HttpBody(categoryId, routeId, climberId, insert);
        HttpEntity<HttpBody> request = new HttpEntity<>(body, headers);

        RestTemplate mRestTemplate = getRestTemplate();
        ResponseEntity<ActiveMonitorJackson> response = mRestTemplate.exchange(url,
                HttpMethod.POST, request, ActiveMonitorJackson.class);

        return response.getBody();
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
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String prettyString = null;
            try {
                prettyString = ow.writeValueAsString(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return prettyString;
        }
    }
}
