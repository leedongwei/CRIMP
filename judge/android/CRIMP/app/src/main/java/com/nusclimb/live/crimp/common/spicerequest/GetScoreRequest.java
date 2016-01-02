package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.GetScoreResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Spice request for GET '/api/judge/score/:category_id/:route_id/:climber_id'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class GetScoreRequest extends SpringAndroidSpiceRequest<GetScoreResponseBody> {
    private static final String TAG = GetScoreRequest.class.getSimpleName();

    private Context context;
    private String xUserId;
    private String xAuthToken;
    private String categoryId;
    private String routeId;
    private String climberId;
    private String url;

    public GetScoreRequest(String xUserId, String xAuthToken, String categoryId, String routeId,
                           String climberId, Context context) {
        super(GetScoreResponseBody.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.categoryId = categoryId;
        this.routeId = routeId;
        this.climberId = climberId;
        this.context = context;

        if(context.getResources().getBoolean(R.bool.is_production_app)) {
            this.url = context.getString(R.string.crimp_production) + context.getString(R.string.get_score_api)
                    +categoryId+"/"+routeId+"/"+climberId;
        }
        else {
            this.url = context.getString(R.string.crimp_staging) + context.getString(R.string.get_score_api)
                    +categoryId+"/"+routeId+"/"+climberId;
        }
    }

    @Override
    public GetScoreResponseBody loadDataFromNetwork() throws Exception {
        if(context.getResources().getBoolean(R.bool.is_debug)){
            GetScoreResponseBody response = new GetScoreResponseBody();
            response.setClimberName("climbername");
            response.setClimberId(climberId);
            response.setCategoryId(categoryId);
            response.setRouteId(routeId);
            if(routeId == "aaa3" || routeId == "bbb3"){
                switch (climberId) {
                    case "001":
                        response.setScoreString("");
                        break;
                    case "002":
                        response.setScoreString("");
                        break;
                    case "003":
                        response.setScoreString("");
                        break;
                    case "004":
                        response.setScoreString("B1");
                        break;
                    case "005":
                        response.setScoreString("B1");
                        break;
                    case "006":
                        response.setScoreString("B1B2");
                        break;
                    case "007":
                        response.setScoreString("B1B2");
                        break;
                    case "008":
                        response.setScoreString("B2");
                        break;
                    case "009":
                        response.setScoreString("B2");
                        break;
                    default:
                        response.setScoreString("");
                        break;
                }
            }
            else {
                switch (climberId) {
                    case "001":
                        response.setScoreString("1");
                        break;
                    case "002":
                        response.setScoreString("11");
                        break;
                    case "003":
                        response.setScoreString("111");
                        break;
                    case "004":
                        response.setScoreString("1111");
                        break;
                    case "005":
                        response.setScoreString("11111");
                        break;
                    case "006":
                        response.setScoreString("111111");
                        break;
                    case "007":
                        response.setScoreString("1111111");
                        break;
                    case "008":
                        response.setScoreString("11111111");
                        break;
                    case "009":
                        response.setScoreString("111111111");
                        break;
                    default:
                        response.setScoreString("");
                        break;
                }
            }
            return response;
        }
        else{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Cache-Control", "no-cache");
            headers.set("x-user-id", xUserId);
            headers.set("x-auth-token", xAuthToken);

            HttpEntity request = new HttpEntity(headers);

            RestTemplate mRestTemplate = getRestTemplate();
            ResponseEntity<GetScoreResponseBody> response = mRestTemplate.exchange(url,
                    HttpMethod.GET, request, GetScoreResponseBody.class);

            return response.getBody();
        }
    }
}