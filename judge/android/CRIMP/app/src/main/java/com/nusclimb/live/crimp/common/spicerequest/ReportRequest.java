package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;
import android.util.Log;

import com.nusclimb.live.crimp.R;
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
public class ReportRequest extends SpringAndroidSpiceRequest<ReportResponse>{
    private static final String TAG = ReportRequest.class.getSimpleName();

    // Information needed to craft a LoginRequest
    private String xUserId;
    private String xAuthToken;
    private String routeId;
    private boolean force;
    private Context context;

    private String baseUrl;

    public ReportRequest(String xUserId, String xAuthToken, String routeId, boolean force, Context context) {
        super(ReportResponse.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.routeId = routeId;
        this.force = force;
        this.context = context;

        baseUrl = context.getString(R.string.crimp_url);
    }

    @Override
    public ReportResponse loadDataFromNetwork() throws Exception {
        // Craft URL.
        String address = baseUrl+context.getString(R.string.report_api);

        // Prepare message
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("route_id", routeId);
        parameters.put("force", String.valueOf(force));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-user-id", xUserId);
        headers.add("x-auth-token", xAuthToken);
        HttpEntity<Map<String,String>> request = new HttpEntity<Map<String, String>>(parameters, headers);

        // Actual network calls.
        ReportResponse content = getRestTemplate().postForObject(address, request, ReportResponse.class);

        Log.v(TAG, "Address=" + address + "\ncontent=" + content.toString());

        return content;
    }

    public String createCacheKey() {
        // CacheKey too long will cause exception.
        return xUserId+routeId+force;
    }

}
