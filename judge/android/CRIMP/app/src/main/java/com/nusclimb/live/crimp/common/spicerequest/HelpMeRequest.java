package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.HelpMeResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Spice request for POST '/api/judge/helpme'
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelpMeRequest extends SpringAndroidSpiceRequest<HelpMeResponseBody>{
    private static final String TAG = HelpMeRequest.class.getSimpleName();

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

        url = context.getString(R.string.crimp_url)+context.getString(R.string.helpme_api);
    }

    @Override
    public HelpMeResponseBody loadDataFromNetwork() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cache-Control", "no-cache");
        headers.set("x-user-id", xUserId);
        headers.set("x-auth-token", xAuthToken);

        HttpBody body = new HttpBody(categoryId, routeId);
        HttpEntity<HttpBody> request = new HttpEntity<HttpBody>(body, headers);

        RestTemplate mRestTemplate = getRestTemplate();
        ResponseEntity<HelpMeResponseBody> response = mRestTemplate.exchange(url, HttpMethod.POST,
                request, HelpMeResponseBody.class);

        return response.getBody();
    }

    /**
     * Jackson POJO for HelpMe request body.
     */
    private static class HttpBody {
        @JsonProperty("category_id")
        private String categoryId;
        @JsonProperty("route_id")
        private String routeId;

        public HttpBody(String categoryId, String routeId){
            this.categoryId = categoryId;
            this.routeId = routeId;
        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("\tcategory_id: "+categoryId+",\n");
            sb.append("\troute_id: "+routeId+"\n");
            sb.append("}");

            return sb.toString();
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getRouteId() {
            return routeId;
        }

        public void setRouteId(String routeId) {
            this.routeId = routeId;
        }
    }
}
