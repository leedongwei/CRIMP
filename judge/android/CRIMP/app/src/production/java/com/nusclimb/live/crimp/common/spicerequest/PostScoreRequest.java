package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.PostScoreResponseBody;
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
 * Spice request for POST '/api/judge/score/:category_id/:route_id/:climber_id'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class PostScoreRequest extends SpringAndroidSpiceRequest<PostScoreResponseBody> {
    private static final String TAG = PostScoreRequest.class.getSimpleName();

    private String xUserId;
    private String xAuthToken;
    private String categoryId;
    private String routeId;
    private String climberId;
    private String scoreString;
    private String url;

    public PostScoreRequest(String xUserId, String xAuthToken, String categoryId,
                            String routeId, String climberId, String scoreString, String url) {
        super(PostScoreResponseBody.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.categoryId = categoryId;
        this.routeId = routeId;
        this.climberId = climberId;
        this.scoreString = scoreString;
        this.url = url;
    }

    @Override
    public PostScoreResponseBody loadDataFromNetwork() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cache-Control", "no-cache");
        headers.set("x-user-id", xUserId);
        headers.set("x-auth-token", xAuthToken);

        HttpBody body = new HttpBody(scoreString);
        HttpEntity<HttpBody> request = new HttpEntity<>(body, headers);

        RestTemplate mRestTemplate = getRestTemplate();
        ResponseEntity<PostScoreResponseBody> response = mRestTemplate.exchange(url,
                HttpMethod.POST, request, PostScoreResponseBody.class);

        return response.getBody();
    }

    public String getxUserId() {
        return xUserId;
    }

    public String getxAuthToken() {
        return xAuthToken;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getClimberId() {
        return climberId;
    }

    public String getScoreString() {
        return scoreString;
    }

    public String getUrl() {
        return url;
    }

    public void setxUserId(String xUserId) {
        this.xUserId = xUserId;
    }

    public void setxAuthToken(String xAuthToken) {
        this.xAuthToken = xAuthToken;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setClimberId(String climberId) {
        this.climberId = climberId;
    }

    public void setScoreString(String scoreString) {
        this.scoreString = scoreString;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Jackson POJO for PostScore request body.
     */
    private static class HttpBody {
        @JsonProperty("score_string")
        private String scoreString;

        public HttpBody(String scoreString) {
            this.scoreString = scoreString;
        }

        @Override
        public String toString() {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String prettyString = null;
            try {
                prettyString = ow.writeValueAsString(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return prettyString;
        }

        public String getScoreString() {
            return scoreString;
        }

        public void setScoreString(String scoreString) {
            this.scoreString = scoreString;
        }
    }
}