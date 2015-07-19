package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;
import android.util.Log;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.HeaderJSONInjector;
import com.nusclimb.live.crimp.common.Helper;
import com.nusclimb.live.crimp.common.KeyValuePair;
import com.nusclimb.live.crimp.common.json.ActiveClimbersResponse;
import com.nusclimb.live.crimp.common.json.GetScoreResponse;
import com.nusclimb.live.crimp.common.json.PostScoreResponse;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 17-Jul-15.
 */
public class PostScoreRequest extends SpringAndroidSpiceRequest<PostScoreResponse> {
    private static final String TAG = PostScoreRequest.class.getSimpleName();

    // Information needed to craft a LoginRequest
    private String xUserId;
    private String xAuthToken;
    private String routeId;
    private String climberId;
    private String score;
    private Context context;

    private String baseUrl;

    public PostScoreRequest(String xUserId, String xAuthToken, String routeId, String climberId, String score, Context context) {
        super(PostScoreResponse.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.routeId = routeId;
        this.climberId = climberId;
        this.score = score;
        this.context = context;

        baseUrl = context.getString(R.string.crimp_url);
    }

    @Override
    public PostScoreResponse loadDataFromNetwork() throws Exception {
        // Craft URL.
        String address = baseUrl+context.getString(R.string.post_score_api)+routeId+"/"+climberId+"?"+ Helper.nextAlphaNumeric(20);

        // Prepare message (body)
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("score_string", score);


        // Prepare message (header)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-user-id", xUserId);
        headers.add("x-auth-token", xAuthToken);
        HttpEntity<Map<String,String>> request = new HttpEntity<Map<String, String>>(parameters, headers);

        // Actual network calls.
        PostScoreResponse content = getRestTemplate().postForObject(address, request, PostScoreResponse.class);

        Log.v(TAG, "Address=" + address + "\ncontent=" + content.toString());

        return content;
    }

    public String createCacheKey() {
        // CacheKey too long will cause exception.
        return xUserId+routeId;
    }

}