package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.ReportResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Spice request for POST '/api/judge/report'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ReportRequest extends SpringAndroidSpiceRequest<ReportResponseBody>{
    private static final String TAG = ReportRequest.class.getSimpleName();

    private Context context;
    private String xUserId;
    private String xAuthToken;
    private String categoryId;
    private String routeId;
    private boolean force;
    private String url;

    public ReportRequest(String xUserId, String xAuthToken, String categoryId,
                         String routeId, boolean force, Context context) {
        super(ReportResponseBody.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.categoryId = categoryId;
        this.routeId = routeId;
        this.force = force;
        this.context = context;
        this.url = context.getString(R.string.crimp_base_url)+context.getString(R.string.report_api);
    }

    @Override
    public ReportResponseBody loadDataFromNetwork() throws Exception {
        ReportResponseBody response = new ReportResponseBody();
        response.setAdminId(xUserId);
        response.setAdminName("John Smith");
        response.setCategoryId(categoryId);
        response.setRouteId(routeId);
        if(force)
            response.setState(1);
        else
            response.setState(0);

        return response;
    }
}
