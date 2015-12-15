package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.PostScoreResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Spice request for POST '/api/judge/score/:category_id/:route_id/:climber_id'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class PostScoreRequest extends SpringAndroidSpiceRequest<PostScoreResponseBody> {
    private static final String TAG = PostScoreRequest.class.getSimpleName();

    private Context context;
    private String xUserId;
    private String xAuthToken;
    private String categoryId;
    private String routeId;
    private String climberId;
    private String scoreString;
    private String url;

    public PostScoreRequest(String xUserId, String xAuthToken, String categoryId,
                            String routeId, String climberId, String scoreString, Context context) {
        super(PostScoreResponseBody.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.categoryId = categoryId;
        this.routeId = routeId;
        this.climberId = climberId;
        this.scoreString = scoreString;
        this.context = context;

        if(context.getResources().getBoolean(R.bool.is_production_app)) {
            this.url = context.getString(R.string.crimp_production) + context.getString(R.string.post_score_api)
                    + categoryId + "/" + routeId + "/" + climberId;;
        }
        else {
            this.url = context.getString(R.string.crimp_staging) + context.getString(R.string.post_score_api)
                    + categoryId + "/" + routeId + "/" + climberId;;
        }
    }

    @Override
    public PostScoreResponseBody loadDataFromNetwork() throws Exception {
        if(context.getResources().getBoolean(R.bool.is_debug)){
            PostScoreResponseBody response = new PostScoreResponseBody();
            return response;
        }
        else {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Cache-Control", "no-cache");
            headers.set("x-user-id", xUserId);
            headers.set("x-auth-token", xAuthToken);

            HttpBody body = new HttpBody(scoreString);
            HttpEntity<HttpBody> request = new HttpEntity<HttpBody>(body, headers);

            RestTemplate mRestTemplate = getRestTemplate();
            ResponseEntity<PostScoreResponseBody> response = mRestTemplate.exchange(url,
                    HttpMethod.POST, request, PostScoreResponseBody.class);

            return response.getBody();
        }
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
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("\tscore_string: " + scoreString + "\n");
            sb.append("}");

            return sb.toString();
        }

        public String getScoreString() {
            return scoreString;
        }

        public void setScoreString(String scoreString) {
            this.scoreString = scoreString;
        }
    }
}