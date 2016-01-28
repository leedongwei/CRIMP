package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.ClimberResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Spice request for GET '/api/judge/climber/:climber_id'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ClimberRequest extends SpringAndroidSpiceRequest<ClimberResponseBody> {
    private static final String TAG = ClimberRequest.class.getSimpleName();

    private Context context;
    private String xUserId;
    private String xAuthToken;
    private String climberId;
    private String url;

    public ClimberRequest(String xUserId, String xAuthToken, String climberId, Context context) {
        super(ClimberResponseBody.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.climberId = climberId;
        this.context = context;
        this.url = context.getString(R.string.crimp_base_url) + context.getString(R.string.climber_api)
                + climberId;
    }

    @Override
    public ClimberResponseBody loadDataFromNetwork() throws Exception {
        ClimberResponseBody response = new ClimberResponseBody();
        response.setClimberId(climberId);
        response.setClimberName("climbername");
        response.setTotalScore("totalscore");
        return response;
    }
}