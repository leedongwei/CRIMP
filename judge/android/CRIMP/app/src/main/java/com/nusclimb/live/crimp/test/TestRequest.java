package com.nusclimb.live.crimp.test;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.nusclimb.live.crimp.common.HeaderJSONInjector;
import com.nusclimb.live.crimp.common.KeyValuePair;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Zhi on 7/8/2015.
 */
public class TestRequest extends SpiceRequest<String> {
    private final String TAG = TestRequest.class.getSimpleName();
    public TestRequest(){
        super(String.class);
    }

    @Override
    public String loadDataFromNetwork() throws Exception {
        Log.d(TAG+".loadDateFromNetwork()", "loadDataFromNetwork");

        // With Uri.Builder class we can build our url is a safe manner
        Uri.Builder uriBuilder = Uri.parse(
                "http://crimp-testing-0625.meteor.com/api/weizhi?4").buildUpon();

        String url = uriBuilder.build().toString();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }

        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url)
                .openConnection();
        urlConnection.setUseCaches(false);
        String result = IOUtils.toString(urlConnection.getInputStream());
        urlConnection.disconnect();

        Log.d(TAG + ".loadDateFromNetwork()", "returned with result");


        return result;
    }

    public String createCacheKey(){
        return "test";
    }
}
