package com.nusclimb.live.crimp.common.spicerequest;

import android.util.Log;

import com.nusclimb.live.crimp.common.json.Session;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Spice request for score of a climber for a route.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class LoginRequest extends SpringAndroidSpiceRequest<Session> {
    private static final String TAG = LoginRequest.class.getSimpleName();
    private static final String BASE_URL = "http://httpbin.org/";

    private String accessToken;

    /**
     *
     *
     * @param accessToken
     */
    public LoginRequest(String accessToken) {
        super(Session.class);
        this.accessToken = accessToken;
    }

    @Override
    public Session loadDataFromNetwork() throws Exception {
        // Craft URL.
        String address = BASE_URL+"get";

        // Actual network calls.
        Session content = getRestTemplate().getForObject(address, Session.class);

        Log.v(TAG, "Address=" + address + "\ncontent=" + content.toString());

        return content;
    }

    public String getAccessToken(){
        return accessToken;
    }

    public String createCacheKey() {
        return accessToken;
    }
}
