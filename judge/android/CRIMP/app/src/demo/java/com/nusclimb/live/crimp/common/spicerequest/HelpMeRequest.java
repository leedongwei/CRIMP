package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.HelpMeResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Spice request for POST '/api/judge/helpme'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelpMeRequest extends SpringAndroidSpiceRequest<HelpMeResponseBody>{
    private static final String TAG = HelpMeRequest.class.getSimpleName();

    private Context context;
    private String xUserId;
    private String xAuthToken;
    private String categoryId;
    private String routeId;
    private String url;

    public HelpMeRequest(String xUserId, String xAuthToken, String categoryId,
                         String routeId, Context context) {
        super(HelpMeResponseBody.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.categoryId = categoryId;
        this.routeId = routeId;
        this.context = context;

        this.url = context.getString(R.string.crimp_base_url)+context.getString(R.string.helpme_api);
    }

    @Override
    public HelpMeResponseBody loadDataFromNetwork() throws Exception {
        return new HelpMeResponseBody();
    }
}
