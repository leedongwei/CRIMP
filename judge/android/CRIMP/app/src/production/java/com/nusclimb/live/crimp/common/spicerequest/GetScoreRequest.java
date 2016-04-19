package com.nusclimb.live.crimp.common.spicerequest;

import com.nusclimb.live.crimp.network.model.GetScoreJackson;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Spice request for GET '/api/judge/score/:category_id/:route_id/:climber_id'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class GetScoreRequest extends SpringAndroidSpiceRequest<GetScoreJackson> {
    private static final String TAG = GetScoreRequest.class.getSimpleName();

    private String xUserId;
    private String xAuthToken;
    private String url;

    public GetScoreRequest(String xUserId, String xAuthToken, String url) {
        super(GetScoreJackson.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.url = url;
    }

    @Override
    public GetScoreJackson loadDataFromNetwork() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cache-Control", "no-cache");
        headers.set("x-user-id", xUserId);
        headers.set("x-auth-token", xAuthToken);

        HttpEntity request = new HttpEntity(headers);

        RestTemplate mRestTemplate = getRestTemplate();
        ResponseEntity<GetScoreJackson> response = mRestTemplate.exchange(url,
                HttpMethod.GET, request, GetScoreJackson.class);

        return response.getBody();
    }
}