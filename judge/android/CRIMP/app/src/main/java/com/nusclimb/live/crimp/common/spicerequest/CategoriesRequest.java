package com.nusclimb.live.crimp.common.spicerequest;

import android.util.Log;

import com.nusclimb.live.crimp.common.json.CategoriesResponse;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Created by user on 30-Jun-15.
 */
public class CategoriesRequest extends SpringAndroidSpiceRequest<CategoriesResponse>{
    private static final String TAG = CategoriesRequest.class.getSimpleName();
    private static final String BASE_URL = "";

    // Information necessary to craft a request
    private String xUserId;
    private String xAuthToken;

    public CategoriesRequest(String xUserId, String xAuthToken){
        super(CategoriesResponse.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
    }

    @Override
    public CategoriesResponse loadDataFromNetwork() throws Exception {
        // Craft URL.
        String address = BASE_URL;

        // Actual network calls.
        CategoriesResponse content = getRestTemplate().getForObject(address, CategoriesResponse.class);

        Log.v(TAG, "Address=" + address + "\ncontent=" + content.toString());

        return content;
    }

    public String createCacheKey() {
        return xUserId+xAuthToken;
    }
}
