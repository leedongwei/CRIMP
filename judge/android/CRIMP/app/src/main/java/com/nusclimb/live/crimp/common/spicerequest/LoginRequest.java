package com.nusclimb.live.crimp.common.spicerequest;

import android.util.Log;

import com.nusclimb.live.crimp.common.json.LoginResponse;
import com.nusclimb.live.crimp.common.json.Session;
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
    private static final String BASE_URL = "http://crimp-testing-0625.meteor.com/api/judge/login";

    // Information needed to craft a LoginRequest
    private String accessToken;
    private String expiresAt;

    /**
     *
     *
     * @param accessToken
     */
    public LoginRequest(String accessToken, String expiresAt) {
        super(LoginResponse.class);
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
    }

    @Override
    public LoginResponse loadDataFromNetwork() throws Exception {
        // Craft URL.
        String address = BASE_URL;

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
        return accessToken+expiresAt;
    }
}
