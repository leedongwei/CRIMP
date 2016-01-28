package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.CategoriesResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 * Spice request for GET '/api/judge/categories'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CategoriesRequest extends SpringAndroidSpiceRequest<CategoriesResponseBody> {
    private static final String TAG = CategoriesRequest.class.getSimpleName();

    private Context context;
    private String xUserId;
    private String xAuthToken;
    private String url;

    public CategoriesRequest(String xUserId, String xAuthToken, Context context) {
        super(CategoriesResponseBody.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.context = context;

        this.url = context.getString(R.string.crimp_base_url)+context.getString(R.string.categories_api);
    }

    @Override
    public CategoriesResponseBody loadDataFromNetwork() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cache-Control", "no-cache");
        headers.set("x-user-id", xUserId);
        headers.set("x-auth-token", xAuthToken);

        HttpEntity request = new HttpEntity(headers);

        RestTemplate mRestTemplate = getRestTemplate();
        ResponseEntity<CategoriesResponseBody> response = mRestTemplate.exchange(url,
                HttpMethod.GET, request, CategoriesResponseBody.class);

        return response.getBody();
    }
}