package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.LoginResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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

    /**
     * Jackson POJO for Login request body.
     */
    private static class HttpBody {
        @JsonProperty("accessToken")
        private String accessToken;
        @JsonProperty("isProductionApp")
        private boolean isProductionApp;

        public HttpBody(String accessToken, boolean isProductionApp){
            this.accessToken = accessToken;
            this.isProductionApp = isProductionApp;
        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("\taccessToken: "+accessToken+",\n");
            sb.append("\tisProductionApp: "+isProductionApp+"\n");
            sb.append("}");

            return sb.toString();
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public boolean isProductionApp() {
            return isProductionApp;
        }

        public void setIsProductionApp(boolean isProductionApp) {
            this.isProductionApp = isProductionApp;
        }
    }
}