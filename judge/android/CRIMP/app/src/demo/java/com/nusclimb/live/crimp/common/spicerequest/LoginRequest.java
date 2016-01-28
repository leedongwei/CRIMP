package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.LoginResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Spice request for POST '/api/judge/login'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class LoginRequest extends SpringAndroidSpiceRequest<LoginResponseBody> {
    private static final String TAG = LoginRequest.class.getSimpleName();

    private Context context;
    private String accessToken;
    private boolean isProductionApp;
    private String url;

    public LoginRequest(String accessToken, Context context) {
        super(LoginResponseBody.class);
        this.context = context;
        this.accessToken = accessToken;
        this.url = context.getString(R.string.crimp_base_url)+context.getString(R.string.login_api);
    }

    @Override
    public LoginResponseBody loadDataFromNetwork() throws Exception {
        LoginResponseBody response = new LoginResponseBody();
        response.setxUserId("debuguserid");
        response.setxAuthToken("debugauthtoken");

        return response;
    }
}