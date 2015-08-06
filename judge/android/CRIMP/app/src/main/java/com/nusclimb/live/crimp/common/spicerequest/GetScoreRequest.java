package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.KeyValuePair;
import com.nusclimb.live.crimp.common.json.ActiveClimbersResponse;
import com.nusclimb.live.crimp.common.json.CategoriesResponse;
import com.nusclimb.live.crimp.common.json.GetScoreResponse;
import com.nusclimb.live.crimp.common.json.GetScoreResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Spice request for GET '/api/judge/score/:category_id/:route_id/:climber_id'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class GetScoreRequest extends SpringAndroidSpiceRequest<GetScoreResponseBody> {
    private static final String TAG = GetScoreRequest.class.getSimpleName();

    private String xUserId;
    private String xAuthToken;
    private String categoryId;
    private String routeId;
    private String climberId;
    private String url;

    public GetScoreRequest(String xUserId, String xAuthToken, String categoryId, String routeId,
                           String climberId, Context context) {
        super(GetScoreResponseBody.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;

        url = context.getString(R.string.crimp_url)+context.getString(R.string.get_score_api)
                +categoryId+"/"+routeId+"/"+climberId;
    }

    @Override
    public GetScoreResponseBody loadDataFromNetwork() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cache-Control", "no-cache");
        headers.set("x-user-id", xUserId);
        headers.set("x-auth-token", xAuthToken);

        HttpEntity request = new HttpEntity(headers);

        RestTemplate mRestTemplate = getRestTemplate();
        ResponseEntity<GetScoreResponseBody> response = mRestTemplate.exchange(url,
                HttpMethod.GET, request, GetScoreResponseBody.class);

        return response.getBody();
    }

    /**
     * Create a cache key for this request. Cache key will allow us to
     * cancel/aggregate/cache this request.
     *
     * @return Cache key for this request.
     */
    public String createCacheKey() {
        return categoryId+routeId+climberId;
    }

}