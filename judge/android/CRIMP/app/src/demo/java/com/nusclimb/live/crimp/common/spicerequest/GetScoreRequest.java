package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.GetScoreResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

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
        this.url = context.getString(R.string.crimp_base_url) + context.getString(R.string.get_score_api)
                +categoryId+"/"+routeId+"/"+climberId;
    }

    @Override
    public GetScoreResponseBody loadDataFromNetwork() throws Exception {
        GetScoreResponseBody response = new GetScoreResponseBody();
        response.setClimberId(climberId);
        response.setCategoryId(categoryId);
        response.setRouteId(routeId);
        if(categoryId == "NMQ"){
            switch (climberId) {
                case "001":
                    response.setClimberName("James");
                    response.setScoreString("");
                    break;
                case "002":
                    response.setClimberName("John");
                    response.setScoreString("1");
                    break;
                case "003":
                    response.setClimberName("Robert");
                    response.setScoreString("11");
                    break;
                case "004":
                    response.setClimberName("Michael");
                    response.setScoreString("111");
                    break;
                case "005":
                    response.setClimberName("William");
                    response.setScoreString("1111");
                    break;
                case "006":
                    response.setClimberName("David");
                    response.setScoreString("1111B");
                    break;
                case "007":
                    response.setClimberName("Richard");
                    response.setScoreString("1111B1");
                    break;
                case "008":
                    response.setClimberName("Charles");
                    response.setScoreString("1111B11");
                    break;
                case "009":
                    response.setClimberName("Joseph");
                    response.setScoreString("1111B111");
                    break;
                default:
                    response.setClimberName("Generic name "+climberId);
                    response.setScoreString("");
                    break;
            }
        }
        else {
            switch (climberId) {
                case "001":
                    response.setClimberName("Thomas");
                    response.setScoreString("");
                    break;
                case "002":
                    response.setClimberName("Christopher");
                    response.setScoreString("");
                    break;
                case "003":
                    response.setClimberName("Daniel");
                    response.setScoreString("B1");
                    break;
                case "004":
                    response.setClimberName("Paul");
                    response.setScoreString("B1");
                    break;
                case "005":
                    response.setClimberName("Mark");
                    response.setScoreString("B1B2");
                    break;
                case "006":
                    response.setClimberName("Donald");
                    response.setScoreString("B1B2");
                    break;
                case "007":
                    response.setClimberName("George");
                    response.setScoreString("B2");
                    break;
                case "008":
                    response.setClimberName("Kenneth");
                    response.setScoreString("B2");
                    break;
                case "009":
                    response.setClimberName("Steven");
                    response.setScoreString("B2");
                    break;
                default:
                    response.setClimberName("Generic name "+climberId);
                    response.setScoreString("");
                    break;
            }
        }
        return response;
    }
}