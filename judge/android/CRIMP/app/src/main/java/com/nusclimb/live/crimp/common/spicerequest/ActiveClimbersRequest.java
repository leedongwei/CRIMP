package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;
import android.util.Log;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.Helper;
import com.nusclimb.live.crimp.common.json.ActiveClimbersResponse;
import com.nusclimb.live.crimp.common.json.ReportResponse;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 17-Jul-15.
 */
public class ActiveClimbersRequest extends SpringAndroidSpiceRequest<ActiveClimbersResponse> {
    private static final String TAG = ActiveClimbersRequest.class.getSimpleName();

    // Information needed to craft a LoginRequest
    private String xUserId;
    private String xAuthToken;
    private String routeId;
    private String climberId;
    private boolean insert;
    private Context context;

    private String baseUrl;

    public ActiveClimbersRequest(String xUserId, String xAuthToken, String routeId, String climberId, boolean insert, Context context) {
        super(ActiveClimbersResponse.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.routeId = routeId;
        this.climberId = climberId;
        this.insert = insert;
        this.context = context;

        baseUrl = context.getString(R.string.crimp_url);
    }

    @Override
    public ActiveClimbersResponse loadDataFromNetwork() throws Exception {
        // Craft URL.
        String address = baseUrl+context.getString(R.string.activeclimbers_api)+"?"+ Helper.nextAlphaNumeric(20);

        // Prepare message (body)
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("route_id", routeId);
        parameters.put("climber_id", climberId);
        parameters.put("insert", String.valueOf(insert));

        // Prepare message (header)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-user-id", xUserId);
        headers.add("x-auth-token", xAuthToken);
        HttpEntity<Map<String,String>> request = new HttpEntity<Map<String, String>>(parameters, headers);

        // Actual network calls.
        ActiveClimbersResponse content = getRestTemplate().postForObject(address, request, ActiveClimbersResponse.class);

        Log.v(TAG+".loadDataFromNetwork()", "Address=" + address + "\ncontent=" + content.toString());

        return content;
    }

    public String createCacheKey() {
        // CacheKey too long will cause exception.
        return routeId + climberId;
    }

}
