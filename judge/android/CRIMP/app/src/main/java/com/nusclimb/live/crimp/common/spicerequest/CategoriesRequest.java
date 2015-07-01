package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;
import android.util.Log;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.HeaderJSONInjector;
import com.nusclimb.live.crimp.common.KeyValuePair;
import com.nusclimb.live.crimp.common.json.CategoriesResponse;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by user on 30-Jun-15.
 */
public class CategoriesRequest extends SpringAndroidSpiceRequest<CategoriesResponse>{
    private static final String TAG = CategoriesRequest.class.getSimpleName();

    public void setXuserid(String xuserid) {
        this.xuserid = xuserid;
    }

    // Information necessary to craft a request
    private String xuserid;

    public void setXauthtoken(String xauthtoken) {
        this.xauthtoken = xauthtoken;
    }

    private String xauthtoken;
    private Context context;

    private String baseUrl;

    public CategoriesRequest(String xUserId, String xAuthToken, Context context){
        super(CategoriesResponse.class);
        this.xuserid = xUserId;
        this.xauthtoken = xAuthToken;
        this.context = context;

        baseUrl = context.getString(R.string.crimp_url);
    }

    @Override
    public CategoriesResponse loadDataFromNetwork() throws Exception {
        // Craft URL.
        String address = baseUrl+context.getString(R.string.categories_api);

        // Prepare parameters
        ArrayList<KeyValuePair> parameters = new ArrayList<KeyValuePair>();
        parameters.add(new KeyValuePair("x-user-id", xuserid));
        parameters.add(new KeyValuePair("x-auth-token", xauthtoken));

        ClientHttpRequestInterceptor interceptor = new HeaderJSONInjector(parameters);
        RestTemplate mRestTemplate = getRestTemplate();
        mRestTemplate.setInterceptors(Collections.singletonList(interceptor));

        // Actual network calls.
        CategoriesResponse content = mRestTemplate.getForObject(address, CategoriesResponse.class);

        Log.v(TAG, "Address=" + address + "\ncontent=" + content.toString());

        return content;
    }

    public String createCacheKey() {
        return xuserid + xauthtoken;
    }

    public String getXuserid() {
        return xuserid;
    }

    public String getXauthtoken() {
        return xauthtoken;
    }
}
