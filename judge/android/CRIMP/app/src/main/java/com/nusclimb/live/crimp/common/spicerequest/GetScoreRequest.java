package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;
import android.util.Log;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.HeaderJSONInjector;
import com.nusclimb.live.crimp.common.Helper;
import com.nusclimb.live.crimp.common.KeyValuePair;
import com.nusclimb.live.crimp.common.json.ActiveClimbersResponse;
import com.nusclimb.live.crimp.common.json.CategoriesResponse;
import com.nusclimb.live.crimp.common.json.GetScoreResponse;
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
public class GetScoreRequest extends SpringAndroidSpiceRequest<GetScoreResponse> {
    private static final String TAG = GetScoreRequest.class.getSimpleName();

    // Information needed to craft a LoginRequest
    private String xUserId;
    private String xAuthToken;
    private String routeId;
    private String climberId;
    private Context context;

    private String baseUrl;

    public GetScoreRequest(String xUserId, String xAuthToken, String routeId, String climberId, Context context) {
        super(GetScoreResponse.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.routeId = routeId;
        this.climberId = climberId;
        this.context = context;

        baseUrl = context.getString(R.string.crimp_url);
    }

    @Override
    public GetScoreResponse loadDataFromNetwork() throws Exception {
        // Craft URL.
        String address = baseUrl+context.getString(R.string.get_score_api)+routeId+"/"+climberId+"?"+ Helper.nextAlphaNumeric(20);

        // Prepare parameters
        ArrayList<KeyValuePair> parameters = new ArrayList<KeyValuePair>();
        parameters.add(new KeyValuePair("x-user-id", xUserId));
        parameters.add(new KeyValuePair("x-auth-token", xAuthToken));

        ClientHttpRequestInterceptor interceptor = new HeaderJSONInjector(parameters);
        RestTemplate mRestTemplate = getRestTemplate();
        mRestTemplate.setInterceptors(Collections.singletonList(interceptor));

        // Actual network calls.
        GetScoreResponse content = mRestTemplate.getForObject(address, GetScoreResponse.class);
        Log.v(TAG, "Address=" + address + "\ncontent=" + content.toString());

        return content;
    }

    public String createCacheKey() {
        // CacheKey too long will cause exception.
        return xUserId+routeId;
    }

}