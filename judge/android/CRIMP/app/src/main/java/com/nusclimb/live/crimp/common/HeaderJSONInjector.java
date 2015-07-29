package com.nusclimb.live.crimp.common;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by user on 01-Jul-15.
 */
public class HeaderJSONInjector implements ClientHttpRequestInterceptor {
    private List<KeyValuePair> parameterList;

    public HeaderJSONInjector(List<KeyValuePair> parameterList) {
        this.parameterList = parameterList;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request);

        for(KeyValuePair k:parameterList){
            requestWrapper.getHeaders().set(k.getKey(), k.getValue());
        }

        return execution.execute(requestWrapper, body);
    }
}