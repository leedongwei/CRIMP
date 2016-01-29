package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.HelpMeResponseBody;
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
 * Spice request for POST '/api/judge/helpme'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelpMeRequest extends SpringAndroidSpiceRequest<HelpMeResponseBody>{
    private static final String TAG = HelpMeRequest.class.getSimpleName();

    private String xUserId;
    private String xAuthToken;
    private String categoryId;
    private String routeId;
    private String url;

    public HelpMeRequest(String xUserId, String xAuthToken, String categoryId,
                         String routeId, String url) {
        super(HelpMeResponseBody.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.categoryId = categoryId;
        this.routeId = routeId;
        this.url = url;
    }

    @Override
    public HelpMeResponseBody loadDataFromNetwork() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cache-Control", "no-cache");
        headers.set("x-user-id", xUserId);
        headers.set("x-auth-token", xAuthToken);

        HttpBody body = new HttpBody(categoryId, routeId);
        HttpEntity<HttpBody> request = new HttpEntity<>(body, headers);

        RestTemplate mRestTemplate = getRestTemplate();
        ResponseEntity<HelpMeResponseBody> response = mRestTemplate.exchange(url, HttpMethod.POST,
                request, HelpMeResponseBody.class);

        return response.getBody();
    }

    /**
     * Jackson POJO for HelpMe request body.
     */
    private static class HttpBody {
        @JsonProperty("category_id")
        private String categoryId;
        @JsonProperty("route_id")
        private String routeId;

        public HttpBody(String categoryId, String routeId){
            this.categoryId = categoryId;
            this.routeId = routeId;
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
