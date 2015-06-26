package com.nusclimb.live.crimp.common.spicerequest;

import android.util.Log;

import com.nusclimb.live.crimp.common.json.LoginResponse;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Spice request for score of a climber for a route.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class LoginRequest extends SpringAndroidSpiceRequest<LoginResponse> {
    private static final String TAG = LoginRequest.class.getSimpleName();
    private static final String BASE_URL = "http://posttestserver.com/post.php?dir=crimp";

    private String accessToken;
    private String expiresAt;

    /**
     *
     * @param accessToken
     * @param expiresAt
     */
    public LoginRequest(String accessToken, String expiresAt) {
        super(LoginResponse.class);
    }

    @Override
    public LoginResponse loadDataFromNetwork() throws Exception {
        // Craft URL.
        String address = BASE_URL;

        // Stuff to post
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject requestBody = new JSONObject();
        requestBody.put("accessToken", accessToken);
        requestBody.put("expiresAt", expiresAt);
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        // Actual network calls.
        LoginResponse content = getRestTemplate().postForObject(address, entity, LoginResponse.class);

        Log.v(TAG, "Address=" + address + "\ncontent=" + content.toString());

        return content;
    }

    public String getAccessToken(){
        return accessToken;
    }

    public String getExpiresAt(){
        return expiresAt;
    }

    public String createCacheKey() {
        return accessToken+expiresAt;
    }
}
