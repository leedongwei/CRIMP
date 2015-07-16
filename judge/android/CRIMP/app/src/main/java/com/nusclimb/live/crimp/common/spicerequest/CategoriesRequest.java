package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;
import android.util.Log;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.HeaderJSONInjector;
import com.nusclimb.live.crimp.common.Helper;
import com.nusclimb.live.crimp.common.KeyValuePair;
import com.nusclimb.live.crimp.common.json.CategoriesResponse;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by user on 30-Jun-15.
 */
public class CategoriesRequest extends SpringAndroidSpiceRequest<CategoriesResponse>{
    private static final String TAG = CategoriesRequest.class.getSimpleName();

    // Information necessary to craft a request
    private String xUserId;
    private String xAuthToken;
    private Context context;

    private String baseUrl;

    public CategoriesRequest(String xUserId, String xAuthToken, Context context){
        super(CategoriesResponse.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.context = context;

        baseUrl = context.getString(R.string.crimp_url);
    }

    @Override
    public CategoriesResponse loadDataFromNetwork() throws Exception {
        // Craft URL.
        String address = baseUrl+context.getString(R.string.categories_api)+"?"+ Helper.nextAlphaNumeric(20);

        // Prepare parameters
        ArrayList<KeyValuePair> parameters = new ArrayList<KeyValuePair>();
        parameters.add(new KeyValuePair("x-user-id", xUserId));
        parameters.add(new KeyValuePair("x-auth-token", xAuthToken));

        ClientHttpRequestInterceptor interceptor = new HeaderJSONInjector(parameters);
        RestTemplate mRestTemplate = getRestTemplate();
        mRestTemplate.setInterceptors(Collections.singletonList(interceptor));

        // Actual network calls.
        CategoriesResponse content = mRestTemplate.getForObject(address, CategoriesResponse.class);

        Log.v(TAG, "Address=" + address + "\ncontent=" + content.toString());

        return content;
    }

    public String createCacheKey() {
        return xUserId + xAuthToken;
    }

    public String getxUserId() {
        return xUserId;
    }

    public String getxAuthToken() {
        return xAuthToken;
    }
}
