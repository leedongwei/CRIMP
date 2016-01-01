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
            aaa.setCategoryId("aaa");
            aaa.setCategoryName("aaaname");
            aaa.setScoresFinalized(false);
            aaa.setTimeStart("start");
            aaa.setTimeEnd("end");
            CategoriesResponseBody.Category.Route aaa1 = new CategoriesResponseBody.Category.Route();
            aaa1.setRouteId("aaa1");
            aaa1.setRouteName("aaa1name");
            aaa1.setScore("0");
            CategoriesResponseBody.Category.Route aaa2 = new CategoriesResponseBody.Category.Route();
            aaa2.setRouteId("aaa2");
            aaa2.setRouteName("aaa2name");
            aaa2.setScore("0");
            CategoriesResponseBody.Category.Route aaa3 = new CategoriesResponseBody.Category.Route();
            aaa3.setRouteId("aaa3");
            aaa3.setRouteName("aaa3name");
            aaa3.setScore("1");
            ArrayList<CategoriesResponseBody.Category.Route> a = new ArrayList<>();
            a.add(aaa1);
            a.add(aaa2);
            a.add(aaa3);
            aaa.setRoutes(a);

            CategoriesResponseBody.Category bbb = new CategoriesResponseBody.Category();
            bbb.setCategoryId("bbb");
            bbb.setCategoryName("bbbname");
            bbb.setScoresFinalized(false);
            bbb.setTimeStart("start");
            bbb.setTimeEnd("end");
            CategoriesResponseBody.Category.Route bbb1 = new CategoriesResponseBody.Category.Route();
            bbb1.setRouteId("bbb1");
            bbb1.setRouteName("bbb1name");
            bbb1.setScore("0");
            CategoriesResponseBody.Category.Route bbb2 = new CategoriesResponseBody.Category.Route();
            bbb2.setRouteId("bbb2");
            bbb2.setRouteName("bbb2name");
            bbb2.setScore("0");
            CategoriesResponseBody.Category.Route bbb3 = new CategoriesResponseBody.Category.Route();
            bbb3.setRouteId("bbb3");
            bbb3.setRouteName("bbb3name");
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