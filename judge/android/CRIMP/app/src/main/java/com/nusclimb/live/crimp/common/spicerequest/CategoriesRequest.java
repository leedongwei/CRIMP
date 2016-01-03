package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.CategoriesResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 * Spice request for GET '/api/judge/categories'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CategoriesRequest extends SpringAndroidSpiceRequest<CategoriesResponseBody> {
    private static final String TAG = CategoriesRequest.class.getSimpleName();

    private Context context;
    private String xUserId;
    private String xAuthToken;
    private String url;

    public CategoriesRequest(String xUserId, String xAuthToken, Context context) {
        super(CategoriesResponseBody.class);
        this.xUserId = xUserId;
        this.xAuthToken = xAuthToken;
        this.context = context;

        if(context.getResources().getBoolean(R.bool.is_production_app))
            this.url = context.getString(R.string.crimp_production)+context.getString(R.string.categories_api);
        else
            this.url = context.getString(R.string.crimp_staging)+context.getString(R.string.categories_api);

    }

    @Override
    public CategoriesResponseBody loadDataFromNetwork() throws Exception {
        if(context.getResources().getBoolean(R.bool.is_debug)){
            CategoriesResponseBody response = new CategoriesResponseBody();

            CategoriesResponseBody.Category aaa = new CategoriesResponseBody.Category();
            aaa.setCategoryId("NMQ");
            aaa.setCategoryName("Novice Man Qualifier");
            aaa.setScoresFinalized(false);
            aaa.setTimeStart("start");
            aaa.setTimeEnd("end");
            CategoriesResponseBody.Category.Route aaa1 = new CategoriesResponseBody.Category.Route();
            aaa1.setRouteId("NMQ1");
            aaa1.setRouteName("Route 1");
            aaa1.setScore("0");
            CategoriesResponseBody.Category.Route aaa2 = new CategoriesResponseBody.Category.Route();
            aaa2.setRouteId("NMQ2");
            aaa2.setRouteName("Route 2");
            aaa2.setScore("0");
            CategoriesResponseBody.Category.Route aaa3 = new CategoriesResponseBody.Category.Route();
            aaa3.setRouteId("NMQ3");
            aaa3.setRouteName("Route 3");
            aaa3.setScore("0");
            ArrayList<CategoriesResponseBody.Category.Route> a = new ArrayList<>();
            a.add(aaa1);
            a.add(aaa2);
            a.add(aaa3);
            aaa.setRoutes(a);

            CategoriesResponseBody.Category bbb = new CategoriesResponseBody.Category();
            bbb.setCategoryId("OMF");
            bbb.setCategoryName("Open Man Final");
            bbb.setScoresFinalized(false);
            bbb.setTimeStart("start");
            bbb.setTimeEnd("end");
            CategoriesResponseBody.Category.Route bbb1 = new CategoriesResponseBody.Category.Route();
            bbb1.setRouteId("OMF1");
            bbb1.setRouteName("Route 1");
            bbb1.setScore("1");
            CategoriesResponseBody.Category.Route bbb2 = new CategoriesResponseBody.Category.Route();
            bbb2.setRouteId("OMF2");
            bbb2.setRouteName("Route 2");
            bbb2.setScore("1");
            CategoriesResponseBody.Category.Route bbb3 = new CategoriesResponseBody.Category.Route();
            bbb3.setRouteId("OMF");
            bbb3.setRouteName("Route 3");
            bbb3.setScore("1");
            ArrayList<CategoriesResponseBody.Category.Route> b = new ArrayList<>();
            b.add(bbb1);
            b.add(bbb2);
            b.add(bbb3);
            bbb.setRoutes(b);

            ArrayList<CategoriesResponseBody.Category> mArr = new ArrayList<>();
            mArr.add(aaa);
            mArr.add(bbb);
            response.setCategories(mArr);

            return response;
        }
        else {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Cache-Control", "no-cache");
            headers.set("x-user-id", xUserId);
            headers.set("x-auth-token", xAuthToken);

            HttpEntity request = new HttpEntity(headers);

            RestTemplate mRestTemplate = getRestTemplate();
            ResponseEntity<CategoriesResponseBody> response = mRestTemplate.exchange(url,
                    HttpMethod.GET, request, CategoriesResponseBody.class);

            return response.getBody();
        }
    }
}