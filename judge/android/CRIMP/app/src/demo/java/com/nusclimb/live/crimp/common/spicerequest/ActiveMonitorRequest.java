package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.ActiveMonitorResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Spice request for POST '/api/judge/activemonitor'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ActiveMonitorRequest extends SpringAndroidSpiceRequest<ActiveMonitorResponseBody> {
    private static final String TAG = ActiveMonitorRequest.class.getSimpleName();

    private Context context;
    private String xUserId;
    private String xAuthToken;
    private String categoryId;
    private String routeId;
    private String climberId;
    private boolean insert;
    private String url;

    public ActiveMonitorRequest(String xUserId, String xAuthToken, String categoryId,
                                String routeId, String climberId, boolean insert, Context context) {
        super(ActiveMonitorResponseBody.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.categoryId = categoryId;
        this.routeId = routeId;
        this.climberId = climberId;
        this.insert = insert;
        this.context = context;

        this.url = context.getString(R.string.crimp_base_url)+context.getString(R.string.activemonitor_api);
    }

    @Override
    public ActiveMonitorResponseBody loadDataFromNetwork() throws Exception {
        return new ActiveMonitorResponseBody();
    }
}
