package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;
import android.util.Log;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.Helper;
import com.nusclimb.live.crimp.common.json.LoginResponse;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Spice request for score of a climber for a route.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class LoginRequest extends SpringAndroidSpiceRequest<LoginResponse> {
    private static final String TAG = LoginRequest.class.getSimpleName();

    // Information needed to craft a LoginRequest
    private String accessToken;
    private String expiresAt;
    private String userId;
    private Context context;

    private String baseUrl;

    /**
     *
     * @param accessToken
     * @param expiresAt
     */
    public LoginRequest(String accessToken, String expiresAt, String userId, Context context) {
        super(LoginResponse.class);
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
        this.userId = userId;
        this.context = context;

        baseUrl = context.getString(R.string.crimp_url);
    }

    @Override
    public LoginResponse loadDataFromNetwork() throws Exception {
        // Craft URL.
        String address = baseUrl+context.getString(R.string.login_api)+"?"+ Helper.nextAlphaNumeric(20);

        // Prepare message
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("accessToken", accessToken);
        parameters.put("expiresAt", expiresAt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,String>> request = new HttpEntity<Map<String, String>>(parameters, headers);

        // Actual network calls.
        LoginResponse content = getRestTemplate().postForObject(address, request, LoginResponse.class);

        Log.v(TAG, "Address=" + address + "\ncontent=" + content.toString());

        return content;
    }

    public String createCacheKey() {
        // CacheKey too long will cause exception.
        return userId+expiresAt;
    }
}