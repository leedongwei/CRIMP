package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;
import android.util.Log;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.Helper;
import com.nusclimb.live.crimp.common.json.HelpMeResponse;
import com.nusclimb.live.crimp.common.json.LoginResponse;
import com.nusclimb.live.crimp.common.json.ReportResponse;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 02-Jul-15.
 */
public class HelpMeRequest extends SpringAndroidSpiceRequest<HelpMeResponse>{
    private static final String TAG = HelpMeRequest.class.getSimpleName();

    // Information needed to craft a HelpMeRequest
    private String xUserId;
    private String xAuthToken;
    private String routeId;
    private Context context;

    private String baseUrl;

    public HelpMeRequest(String xUserId, String xAuthToken, String routeId, Context context) {
        super(HelpMeResponse.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.routeId = routeId;
        this.context = context;

        baseUrl = context.getString(R.string.crimp_url);
    }

    @Override
    public HelpMeResponse loadDataFromNetwork() throws Exception {
        // Craft URL.
        String address = baseUrl+context.getString(R.string.helpme_api)+"?"+ Helper.nextAlphaNumeric(20);

        // Prepare message (body)
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("admin_id", xUserId);
        parameters.put("route_id", routeId);

        // Prepare message (header)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-user-id", xUserId);
        headers.add("x-auth-token", xAuthToken);
        HttpEntity<Map<String,String>> request = new HttpEntity<Map<String, String>>(parameters, headers);

        // Actual network calls.
        HelpMeResponse content = getRestTemplate().postForObject(address, request, HelpMeResponse.class);

        Log.v(TAG, "Address=" + address + "\ncontent=" + content.toString());

        return content;
    }

    public String createCacheKey() {
        // CacheKey too long will cause exception.
        return xUserId+routeId;
    }

}
