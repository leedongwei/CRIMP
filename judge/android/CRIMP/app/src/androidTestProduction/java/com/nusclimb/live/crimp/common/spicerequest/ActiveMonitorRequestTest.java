package com.nusclimb.live.crimp.common.spicerequest;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.nusclimb.live.crimp.network.model.ActiveMonitorJackson;
import com.octo.android.robospice.JacksonSpringAndroidSpiceService;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class ActiveMonitorRequestTest extends InstrumentationTestCase{
    private ActiveMonitorJackson serverResponseObject;
    private String serverResponseString;

    @Before
    public void setup() throws IOException {
        // Prepare server responses
        InputStream inputStream = InstrumentationRegistry.getContext().getResources()
                .getAssets().open("api/judge/POSTactivemonitor");
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
        serverResponseObject = mapper.readValue(inputStream, ActiveMonitorJackson.class);
        serverResponseString = mapper.writeValueAsString(serverResponseObject);
        inputStream.close();
    }

    @Test
    public void testLoadDataFromNetwork() throws Exception {
        // Prepare server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(serverResponseString)
                .setHeader("Content-Type", "application/json; charset=utf-8"));
        server.start();
        HttpUrl baseUrl = server.url("/api/judge/activemonitor");

        JacksonSpringAndroidSpiceService mService = new JacksonSpringAndroidSpiceService();
        ActiveMonitorRequest mRequest = new ActiveMonitorRequest("userId", "authToken",
                "categoryId", "routeId", "climberId", true, baseUrl.toString());
        mRequest.setRestTemplate(mService.createRestTemplate());
        ActiveMonitorJackson actualResponse = mRequest.loadDataFromNetwork();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
        String actualResponseString = mapper.writeValueAsString(actualResponse);
        assertThat(actualResponseString, is(equalTo(serverResponseString)));

        server.shutdown();
    }
}