package com.nusclimb.live.crimp.common.spicerequest;

import com.nusclimb.live.crimp.network.model.ReportJackson;
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
 * Spice request for POST '/api/judge/report'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ReportRequest extends SpringAndroidSpiceRequest<ReportJackson>{
    private static final String TAG = ReportRequest.class.getSimpleName();

    private String xUserId;
    private String xAuthToken;
    private String categoryId;
    private String routeId;
    private boolean force;
    private String url;

    public ReportRequest(String xUserId, String xAuthToken, String categoryId,
                         String routeId, boolean force, String url) {
        super(ReportJackson.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.categoryId = categoryId;
        this.routeId = routeId;
        this.force = force;
        this.url = url;
    }

    @Override
    public ReportJackson loadDataFromNetwork() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cache-Control", "no-cache");
        headers.set("x-user-id", xUserId);
        headers.set("x-auth-token", xAuthToken);

        HttpBody body = new HttpBody(categoryId, routeId, force);
        HttpEntity<HttpBody> request = new HttpEntity<>(body, headers);

        RestTemplate mRestTemplate = getRestTemplate();
        ResponseEntity<ReportJackson> response = mRestTemplate.exchange(url, HttpMethod.POST,
                request, ReportJackson.class);

        return response.getBody();
    }

    /**
     * Jackson POJO for Report request body.
     */
    private static class HttpBody {
        @JsonProperty("category_id")
        private String categoryId;
        @JsonProperty("route_id")
        private String routeId;
        @JsonProperty("force")
        private boolean force;

        public HttpBody(String categoryId, String routeId, boolean force){
            this.categoryId = categoryId;
            this.routeId = routeId;
            this.force = force;
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
