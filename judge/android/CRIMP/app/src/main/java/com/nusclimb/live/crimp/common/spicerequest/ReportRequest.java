package com.nusclimb.live.crimp.common.spicerequest;

import android.content.Context;

import com.nusclimb.live.crimp.R;
import com.nusclimb.live.crimp.common.json.ReportResponseBody;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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

        boolean isProductionApp = context.getResources().getBoolean(R.bool.is_production_app);
        if(isProductionApp)
            this.url = context.getString(R.string.crimp_production)+context.getString(R.string.report_api);
        else
            this.url = context.getString(R.string.crimp_staging)+context.getString(R.string.report_api);
    }

    @Override
    public ReportResponseBody loadDataFromNetwork() throws Exception {
        if(context.getResources().getBoolean(R.bool.is_debug)){
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
        else {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Cache-Control", "no-cache");
            headers.set("x-user-id", xUserId);
            headers.set("x-auth-token", xAuthToken);

            HttpBody body = new HttpBody(categoryId, routeId, force);
            HttpEntity<HttpBody> request = new HttpEntity<>(body, headers);

            RestTemplate mRestTemplate = getRestTemplate();
            ResponseEntity<ReportResponseBody> response = mRestTemplate.exchange(url, HttpMethod.POST,
                    request, ReportResponseBody.class);

            return response.getBody();
        }
    }

    /**
     * Jackson POJO for Report request body.
     */
    private static class HttpBody {
        @JsonProperty("category_id")
        private String categoryId;
        @JsonProperty("route_id")
        private String routeId;
        @JsonProperty("force")
        private boolean force;

        public HttpBody(String categoryId, String routeId, boolean force){
            this.categoryId = categoryId;
            this.routeId = routeId;
            this.force = force;
        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("\tcategory_id: "+categoryId+",\n");
            sb.append("\troute_id: "+routeId+",\n");
            sb.append("\tforce: "+force+"\n");
            sb.append("}");

            return sb.toString();
        }

        public boolean isForce() {
            return force;
        }

        public void setForce(boolean force) {
            this.force = force;
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
