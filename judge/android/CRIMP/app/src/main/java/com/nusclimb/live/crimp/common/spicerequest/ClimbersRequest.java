package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;
import android.util.Log;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.HeaderJSONInjector;
import com.nusclimb.live.crimp.common.Helper;
import com.nusclimb.live.crimp.common.KeyValuePair;
import com.nusclimb.live.crimp.common.json.CategoriesResponse;
import com.nusclimb.live.crimp.common.json.ClimbersResponse;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by user on 17-Jul-15.
 */
public class ClimbersRequest extends SpringAndroidSpiceRequest<ClimbersResponse> {
    private static final String TAG = ClimbersRequest.class.getSimpleName();

    // Information necessary to craft a request
    private String xUserId;
    private String xAuthToken;
    private String categoryId;
    private Context context;

    private String baseUrl;

    public ClimbersRequest(String xUserId, String xAuthToken, String categoryId, Context context){
        super(ClimbersResponse.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.categoryId = categoryId;
        this.context = context;

        baseUrl = context.getString(R.string.crimp_url);
    }

    @Override
    public ClimbersResponse loadDataFromNetwork() throws Exception {
        // Craft URL.
        String address = baseUrl+context.getString(R.string.climbers_api)+categoryId+"?"+ Helper.nextAlphaNumeric(20);

        // Prepare parameters
        ArrayList<KeyValuePair> parameters = new ArrayList<KeyValuePair>();
        parameters.add(new KeyValuePair("x-user-id", xUserId));
        parameters.add(new KeyValuePair("x-auth-token", xAuthToken));

        ClientHttpRequestInterceptor interceptor = new HeaderJSONInjector(parameters);
        RestTemplate mRestTemplate = getRestTemplate();
        mRestTemplate.setInterceptors(Collections.singletonList(interceptor));

        // Actual network calls.
        ClimbersResponse content = mRestTemplate.getForObject(address, ClimbersResponse.class);

        Log.v(TAG+".loadDataFromNetwork()", "Address=" + address + "\ncontent=" + content.toString());

        return content;
    }

    public String createCacheKey() {
        return xUserId + categoryId;
    }

    public String getxUserId() {
        return xUserId;
    }

    public String getxAuthToken() {
        return xAuthToken;
    }
}